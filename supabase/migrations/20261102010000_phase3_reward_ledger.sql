-- E50 Phase 3: server-authoritative rewards, idempotency and avatar purchases.
-- Apply this migration in Supabase SQL Editor before installing the Phase 3 APK.
-- Client-supplied game credits are NEVER used to change profile balances.

create schema if not exists private;
revoke all on schema private from public, anon, authenticated;

create table if not exists public.reward_ledger (
    id uuid primary key default gen_random_uuid(),
    user_id uuid not null references auth.users (id) on delete cascade,
    event_key text not null check (length(event_key) between 1 and 180),
    event_type text not null,
    points_delta integer not null default 0,
    credits_delta integer not null default 0,
    reason text not null,
    metadata jsonb not null default '{}'::jsonb,
    created_at timestamptz not null default now(),
    constraint reward_ledger_nonzero_delta check (
        points_delta <> 0 or credits_delta <> 0
    ),
    constraint reward_ledger_points_bounds check (
        points_delta between -500 and 500
    ),
    constraint reward_ledger_credits_bounds check (
        credits_delta between -500 and 500
    ),
    constraint reward_ledger_user_event_unique unique (user_id, event_key)
);

create index if not exists reward_ledger_user_created_idx
    on public.reward_ledger (user_id, created_at desc);

alter table public.reward_ledger enable row level security;
revoke all on table public.reward_ledger from public, anon, authenticated;
grant select on table public.reward_ledger to authenticated;

drop policy if exists "reward_ledger_select_own" on public.reward_ledger;
create policy "reward_ledger_select_own"
    on public.reward_ledger for select to authenticated
    using ((select auth.uid()) = user_id);

-- Remove the Phase 2 client ability to modify avatar ownership/equipment.
-- Profile text and onboarding fields remain client-editable.
revoke insert on table public.profiles from authenticated;
revoke update (selected_avatar, unlocked_avatars, eco_points, eco_credits)
    on table public.profiles from authenticated;
revoke update (selected_avatar, unlocked_avatars, eco_points, eco_credits)
    on table public.profiles from anon;

-- Private, server-owned reward values for the six seven-day experiments.
create table if not exists private.experiment_reward_catalog (
    experiment_id text primary key,
    bonus_points integer not null check (bonus_points between 1 and 500)
);

revoke all on table private.experiment_reward_catalog from public, anon, authenticated;
alter table private.experiment_reward_catalog enable row level security;

insert into private.experiment_reward_catalog (experiment_id, bonus_points)
values
    ('exp_ac_efficiency', 80),
    ('exp_standby_cutoff', 70),
    ('exp_daylight', 60),
    ('exp_cooling_habits', 75),
    ('exp_smart_lighting', 65),
    ('exp_awareness', 100)
on conflict (experiment_id) do update
set bonus_points = excluded.bonus_points;

-- Prices are defined on the server. The Android client never supplies a trusted price.
create table if not exists private.avatar_catalog (
    avatar_name text primary key,
    cost_credits integer not null check (cost_credits between 0 and 10000),
    active boolean not null default true
);

revoke all on table private.avatar_catalog from public, anon, authenticated;
alter table private.avatar_catalog enable row level security;

insert into private.avatar_catalog (avatar_name, cost_credits, active)
values
    ('🌱 Eco Pioneer', 0, true),
    ('⚡ Energy Sleuth', 0, true),
    ('🍃 Green Champion', 0, true),
    ('🌳 Solar Guardian', 100, true),
    ('🏙️ Eco Planner', 200, true),
    ('👑 Net Zero Leader', 350, true)
on conflict (avatar_name) do update
set cost_credits = excluded.cost_credits,
    active = excluded.active;

-- One private function is the only path that writes either balance.
-- The unique (user_id, event_key) key makes every reward idempotent.
create or replace function private.apply_reward_event(
    p_user_id uuid,
    p_event_key text,
    p_event_type text,
    p_points_delta integer,
    p_credits_delta integer,
    p_reason text,
    p_metadata jsonb default '{}'::jsonb
)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_ledger_id uuid;
    v_updated_profile_id uuid;
begin
    if p_user_id is null
       or p_event_key is null
       or length(trim(p_event_key)) = 0
       or p_event_type is null
       or p_reason is null
       or (coalesce(p_points_delta, 0) = 0 and coalesce(p_credits_delta, 0) = 0)
       or abs(coalesce(p_points_delta, 0)) > 500
       or abs(coalesce(p_credits_delta, 0)) > 500 then
        raise exception 'Invalid reward event';
    end if;

    insert into public.reward_ledger (
        user_id,
        event_key,
        event_type,
        points_delta,
        credits_delta,
        reason,
        metadata
    )
    values (
        p_user_id,
        p_event_key,
        p_event_type,
        p_points_delta,
        p_credits_delta,
        p_reason,
        coalesce(p_metadata, '{}'::jsonb)
    )
    on conflict (user_id, event_key) do nothing
    returning id into v_ledger_id;

    -- A retry or duplicate event has no effect on either balance.
    if v_ledger_id is null then
        return false;
    end if;

    update public.profiles
    set eco_points = eco_points + p_points_delta,
        eco_credits = eco_credits + p_credits_delta,
        updated_at = now()
    where id = p_user_id
      and eco_points + p_points_delta >= 0
      and eco_credits + p_credits_delta >= 0
    returning id into v_updated_profile_id;

    if v_updated_profile_id is null then
        raise exception 'Reward balance update rejected: profile missing or insufficient balance';
    end if;

    return true;
end;
$$;

revoke all on function private.apply_reward_event(
    uuid, text, text, integer, integer, text, jsonb
) from public, anon, authenticated;

-- Onboarding completion does not award points in Phase 3 because the client can
-- mark onboarding as skipped. This avoids rewarding a client-controlled skip.
-- Activity rewards below are issued only by server-side event triggers.

-- A newly inserted electricity record earns 15 points at most once per current UTC month.
-- Updating an existing month cannot mint another reward.
create or replace function private.reward_footprint_insert()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    if new.kwh > 0
       and new.bill_amount >= 0
       and new.month_name = any(array[
           'January', 'February', 'March', 'April', 'May', 'June',
           'July', 'August', 'September', 'October', 'November', 'December'
       ]::text[]) then
        perform private.apply_reward_event(
            new.user_id,
            'footprint-reward:' || to_char(timezone('UTC', now()), 'YYYY-MM'),
            'FOOTPRINT_ENTRY',
            15,
            0,
            'Logged monthly electricity reading',
            jsonb_build_object('month', new.month_name, 'year', new.year)
        );
    end if;
    return new;
end;
$$;

drop trigger if exists trg_reward_footprint_insert on public.footprint_entries;
create trigger trg_reward_footprint_insert
    after insert on public.footprint_entries
    for each row execute function private.reward_footprint_insert();

-- Server timestamps daily completions; client date_string is display metadata only.
alter table public.daily_checks
    add column if not exists completed_at timestamptz;

create or replace function private.stamp_daily_check_completion()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    if tg_op = 'INSERT' then
        if new.completed is true then
            new.completed_at := now();
        else
            new.completed_at := null;
        end if;
    else
        if old.completed is true then
            if new.completed is false then
                raise exception 'A completed daily check cannot be reset';
            end if;
            -- Never accept a client-supplied replacement timestamp.
            new.completed_at := old.completed_at;
        elsif new.completed is true then
            new.completed_at := now();
        else
            new.completed_at := null;
        end if;
    end if;
    return new;
end;
$$;

drop trigger if exists trg_stamp_daily_check_completion on public.daily_checks;
create trigger trg_stamp_daily_check_completion
    before insert or update on public.daily_checks
    for each row execute function private.stamp_daily_check_completion();

-- A real completion transition earns at most one daily reward per UTC day.
-- The seven-day bonus requires seven different server completion dates.
create or replace function private.reward_daily_check_completion()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_just_completed boolean := false;
    v_completed_count integer;
    v_distinct_days integer;
    v_bonus_points integer;
    v_utc_day text := (timezone('UTC', now())::date)::text;
begin
    -- A completed row inserted during account migration is historical data,
    -- not a newly earned reward. Only a user action changing false -> true qualifies.
    if tg_op = 'UPDATE' then
        v_just_completed := old.completed is false and new.completed is true;
    end if;

    if v_just_completed and new.completed_at is not null then
        perform private.apply_reward_event(
            new.user_id,
            'daily-check-daily:' || v_utc_day,
            'DAILY_CHECK',
            10,
            0,
            'Completed an experiment daily check (daily limit)',
            jsonb_build_object(
                'experiment_id', new.experiment_id,
                'day_number', new.day_number
            )
        );

        select
            count(*) filter (where completed_at is not null),
            count(distinct (completed_at at time zone 'UTC')::date)
        into v_completed_count, v_distinct_days
        from public.daily_checks
        where user_id = new.user_id
          and experiment_id = new.experiment_id
          and completed is true
          and day_number between 1 and 7;

        if v_completed_count >= 7 and v_distinct_days >= 7 then
            select bonus_points
            into v_bonus_points
            from private.experiment_reward_catalog
            where experiment_id = new.experiment_id;

            if v_bonus_points is not null then
                perform private.apply_reward_event(
                    new.user_id,
                    'experiment-complete:' || new.experiment_id,
                    'EXPERIMENT_COMPLETE',
                    v_bonus_points,
                    0,
                    'Completed the seven-day energy experiment',
                    jsonb_build_object('experiment_id', new.experiment_id)
                );
            end if;
        end if;
    end if;

    return new;
end;
$$;

drop trigger if exists trg_reward_daily_check_completion on public.daily_checks;
create trigger trg_reward_daily_check_completion
    after insert or update on public.daily_checks
    for each row execute function private.reward_daily_check_completion();

-- Impact entries are self-reported. Reward at most one impact submission per user per UTC day.
create or replace function private.reward_impact_insert()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_utc_day text := (timezone('UTC', now())::date)::text;
begin
    if length(trim(new.title)) >= 3 and new.estimated_kwh_saved > 0 then
        perform private.apply_reward_event(
            new.user_id,
            'impact-daily:' || v_utc_day,
            'IMPACT_LOG',
            25,
            0,
            'Submitted an impact log (daily reward limit)',
            jsonb_build_object('impact_id', new.id)
        );
    end if;
    return new;
end;
$$;

drop trigger if exists trg_reward_impact_insert on public.impact_logs;
create trigger trg_reward_impact_insert
    after insert on public.impact_logs
    for each row execute function private.reward_impact_insert();

-- Game results are client-reported and are not anti-cheat verified in Phase 3.
-- The client-provided reported_eco_credits is ignored; the server grants a fixed
-- 10 credits at most once per game type per UTC day.
alter table public.game_scores
    drop constraint if exists game_scores_supported_type_check;

alter table public.game_scores
    add constraint game_scores_supported_type_check
    check (game_type in ('ECO_SORT', 'ENERGY_DETECTIVE', 'GREEN_CITY'));

create or replace function private.reward_game_score_insert()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_utc_day text := (timezone('UTC', now())::date)::text;
begin
    if new.score > 0 and new.accuracy_percent > 0 then
        perform private.apply_reward_event(
            new.user_id,
            'game-daily:' || new.game_type || ':' || v_utc_day,
            'GAME_DAILY_REWARD',
            0,
            10,
            'First positive-score game completion reward for this game type today',
            jsonb_build_object(
                'game_type', new.game_type,
                'game_score_id', new.id,
                'reported_credits_ignored', new.reported_eco_credits
            )
        );
    end if;
    return new;
end;
$$;

drop trigger if exists trg_reward_game_score_insert on public.game_scores;
create trigger trg_reward_game_score_insert
    after insert on public.game_scores
    for each row execute function private.reward_game_score_insert();

-- Avatar ownership and spending happen atomically on the server.
create or replace function public.purchase_avatar(p_avatar_name text)
returns jsonb
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_cost integer;
    v_unlocked text[];
    v_balance integer;
begin
    if v_user_id is null then
        raise exception 'Authentication required';
    end if;

    select cost_credits
    into v_cost
    from private.avatar_catalog
    where avatar_name = p_avatar_name
      and active is true;

    if v_cost is null then
        raise exception 'Unknown or unavailable avatar';
    end if;

    select unlocked_avatars, eco_credits
    into v_unlocked, v_balance
    from public.profiles
    where id = v_user_id
    for update;

    if not found then
        raise exception 'Profile not found';
    end if;

    if p_avatar_name = any(coalesce(v_unlocked, array[]::text[])) then
        update public.profiles
        set selected_avatar = p_avatar_name,
            updated_at = now()
        where id = v_user_id;

        return jsonb_build_object(
            'success', true,
            'already_unlocked', true,
            'eco_credits', v_balance
        );
    end if;

    if v_balance < v_cost then
        raise exception 'Not enough EcoCredits. Required: %, available: %', v_cost, v_balance;
    end if;

    if v_cost > 0 then
        perform private.apply_reward_event(
            v_user_id,
            'avatar-purchase:' || p_avatar_name,
            'AVATAR_PURCHASE',
            0,
            -v_cost,
            'Purchased avatar ' || p_avatar_name,
            jsonb_build_object('avatar_name', p_avatar_name, 'cost_credits', v_cost)
        );
    end if;

    update public.profiles
    set unlocked_avatars = array_append(
            coalesce(unlocked_avatars, array[]::text[]),
            p_avatar_name
        ),
        selected_avatar = p_avatar_name,
        updated_at = now()
    where id = v_user_id
    returning eco_credits into v_balance;

    return jsonb_build_object(
        'success', true,
        'already_unlocked', false,
        'eco_credits', v_balance
    );
end;
$$;

create or replace function public.select_avatar(p_avatar_name text)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
begin
    if v_user_id is null then
        raise exception 'Authentication required';
    end if;

    update public.profiles
    set selected_avatar = p_avatar_name,
        updated_at = now()
    where id = v_user_id
      and p_avatar_name = any(coalesce(unlocked_avatars, array[]::text[]));

    if not found then
        raise exception 'Avatar is not unlocked for this account';
    end if;

    return true;
end;
$$;

revoke all on function public.purchase_avatar(text) from public, anon;
revoke all on function public.select_avatar(text) from public, anon;
grant execute on function public.purchase_avatar(text) to authenticated;
grant execute on function public.select_avatar(text) to authenticated;

comment on table public.reward_ledger is
    'Immutable server-created reward and purchase ledger. The authenticated client may read only its own events.';
comment on column public.game_scores.reported_eco_credits is
    'Untrusted client metadata. Phase 3 reward triggers ignore this field and apply a fixed daily cap.';
