-- E50 Phase 4: live community participation and privacy-aware leaderboards.
-- Apply after Phase 2 and Phase 3 migrations.

-- Explicitly close the Phase 2 profile insert path and reserve reward/avatar columns.
revoke insert on table public.profiles from authenticated;
revoke insert (id, display_name, city, onboarding_completed, selected_avatar, unlocked_avatars)
    on table public.profiles from authenticated;
revoke update (selected_avatar, unlocked_avatars, eco_points, eco_credits)
    on table public.profiles from authenticated;

alter table public.profiles
    add column if not exists leaderboard_visible boolean not null default false;

grant update (leaderboard_visible) on table public.profiles to authenticated;

create table if not exists public.community_battles (
    battle_id text primary key,
    title text not null,
    subtitle text not null default '',
    region text not null,
    target_goal text not null,
    target_metric numeric(14, 2) not null check (target_metric > 0),
    metric_unit text not null,
    reward_points integer not null default 20 check (reward_points between 0 and 500),
    starts_at timestamptz not null default now(),
    ends_at timestamptz not null,
    active boolean not null default true,
    created_at timestamptz not null default now(),
    check (ends_at > starts_at)
);

create table if not exists public.community_battle_members (
    battle_id text not null references public.community_battles (battle_id) on delete cascade,
    user_id uuid not null references auth.users (id) on delete cascade,
    joined_at timestamptz not null default now(),
    primary key (battle_id, user_id)
);

create table if not exists public.community_battle_contributions (
    id uuid primary key default gen_random_uuid(),
    battle_id text not null,
    user_id uuid not null,
    amount numeric(14, 2) not null check (amount > 0 and amount <= 100),
    created_at timestamptz not null default now(),
    foreign key (battle_id, user_id)
        references public.community_battle_members (battle_id, user_id)
        on delete cascade
);

create index if not exists community_contributions_battle_idx
    on public.community_battle_contributions (battle_id, created_at desc);
create index if not exists community_contributions_user_idx
    on public.community_battle_contributions (user_id, created_at desc);

alter table public.community_battles enable row level security;
alter table public.community_battle_members enable row level security;
alter table public.community_battle_contributions enable row level security;

revoke all on table public.community_battles from anon, authenticated;
revoke all on table public.community_battle_members from anon, authenticated;
revoke all on table public.community_battle_contributions from anon, authenticated;

grant select on table public.community_battles to authenticated;
grant select on table public.community_battle_members to authenticated;
grant select on table public.community_battle_contributions to authenticated;

drop policy if exists "community_battles_read_active" on public.community_battles;
create policy "community_battles_read_active"
    on public.community_battles for select to authenticated
    using (active is true);

drop policy if exists "community_members_read_own" on public.community_battle_members;
create policy "community_members_read_own"
    on public.community_battle_members for select to authenticated
    using ((select auth.uid()) = user_id);

drop policy if exists "community_contributions_read_own" on public.community_battle_contributions;
create policy "community_contributions_read_own"
    on public.community_battle_contributions for select to authenticated
    using ((select auth.uid()) = user_id);

-- These are real, empty-starting challenge definitions. Metrics and participant
-- totals are computed from recorded joins/contributions; no demo counts are seeded.
insert into public.community_battles (
    battle_id, title, subtitle, region, target_goal, target_metric, metric_unit,
    reward_points, starts_at, ends_at, active
)
values
    (
        'battle_ahmedabad_surat',
        'Mission LiFE: Ahmedabad vs Surat Standby Sunset',
        'Cut idle power from household appliances and chargers',
        'Gujarat Metro',
        'Record up to 100,000 standby switches turned off',
        100000, 'Switches Toggled Off', 20, now(), now() + interval '30 days', true
    ),
    (
        'battle_cool_24',
        'Vadodara 24°C Thermostat Pledge',
        'Practice efficient cooling habits during warm days',
        'Central Gujarat',
        'Record up to 250,000 kWh of community-reported conservation',
        250000, 'kWh Reported', 20, now(), now() + interval '30 days', true
    ),
    (
        'battle_solar_sprint',
        'GEDA Rooftop Solar Literacy Sprint',
        'Record household rooftop solar assessments',
        'Rajkot & Bhavnagar',
        'Complete up to 5,000 rooftop assessments',
        5000, 'Roofs Assessed', 20, now(), now() + interval '30 days', true
    )
on conflict (battle_id) do update
set title = excluded.title,
    subtitle = excluded.subtitle,
    region = excluded.region,
    target_goal = excluded.target_goal,
    target_metric = excluded.target_metric,
    metric_unit = excluded.metric_unit,
    reward_points = excluded.reward_points,
    active = true;

-- Community data is returned through narrow authenticated RPCs, not broad table access.
create or replace function public.get_community_battles()
returns table (
    battle_id text,
    title text,
    subtitle text,
    region text,
    target_goal text,
    current_metric numeric,
    target_metric numeric,
    metric_unit text,
    participants_count bigint,
    days_left integer,
    is_joined boolean,
    user_contribution numeric,
    reward_points integer
)
language sql
stable
security definer
set search_path = ''
as $$
    select
        b.battle_id,
        b.title,
        b.subtitle,
        b.region,
        b.target_goal,
        coalesce(sum(c.amount), 0)::numeric as current_metric,
        b.target_metric,
        b.metric_unit,
        (select count(*) from public.community_battle_members m
          where m.battle_id = b.battle_id) as participants_count,
        greatest(0, ceil(extract(epoch from (b.ends_at - now())) / 86400.0))::integer as days_left,
        exists (
            select 1 from public.community_battle_members mine
            where mine.battle_id = b.battle_id and mine.user_id = auth.uid()
        ) as is_joined,
        coalesce(sum(c.amount) filter (where c.user_id = auth.uid()), 0)::numeric as user_contribution,
        b.reward_points
    from public.community_battles b
    left join public.community_battle_contributions c on c.battle_id = b.battle_id
    where b.active is true and b.starts_at <= now() and b.ends_at > now()
    group by b.battle_id
    order by b.ends_at, b.title;
$$;

create or replace function public.join_community_battle(p_battle_id text)
returns boolean
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_inserted_battle_id text;
begin
    if v_user_id is null then
        raise exception 'Authentication required';
    end if;

    if not exists (
        select 1 from public.community_battles
        where battle_id = p_battle_id
          and active is true
          and starts_at <= now()
          and ends_at > now()
    ) then
        raise exception 'This community battle is not active';
    end if;

    insert into public.community_battle_members (battle_id, user_id)
    values (p_battle_id, v_user_id)
    on conflict (battle_id, user_id) do nothing
    returning battle_id into v_inserted_battle_id;

    if v_inserted_battle_id is not null then
        perform private.apply_reward_event(
            v_user_id,
            'community-join:' || p_battle_id,
            'COMMUNITY_JOIN',
            20,
            0,
            'Joined a community climate battle',
            jsonb_build_object('battle_id', p_battle_id)
        );
        return true;
    end if;

    return false;
end;
$$;

create or replace function public.contribute_to_community_battle(
    p_battle_id text,
    p_amount numeric
)
returns uuid
language plpgsql
security definer
set search_path = ''
as $$
declare
    v_user_id uuid := auth.uid();
    v_id uuid;
begin
    if v_user_id is null then
        raise exception 'Authentication required';
    end if;
    if p_amount is null or p_amount <= 0 or p_amount > 100 then
        raise exception 'Contribution must be greater than 0 and at most 100';
    end if;
    if not exists (
        select 1 from public.community_battle_members
        where battle_id = p_battle_id and user_id = v_user_id
    ) then
        raise exception 'Join this battle before contributing';
    end if;
    if not exists (
        select 1 from public.community_battles
        where battle_id = p_battle_id
          and active is true
          and starts_at <= now()
          and ends_at > now()
    ) then
        raise exception 'This community battle is not active';
    end if;

    insert into public.community_battle_contributions (battle_id, user_id, amount)
    values (p_battle_id, v_user_id, p_amount)
    returning id into v_id;

    -- Contributions are self-reported and earn no points until independently verified.
    return v_id;
end;
$$;

-- Leaderboards are opt-in. A user can always see their own row, but other
-- users see the profile only after the user enables leaderboard visibility.
create or replace function public.get_user_leaderboard(p_limit integer default 50)
returns table (
    rank bigint,
    display_name text,
    city text,
    selected_avatar text,
    eco_points integer,
    actions_count bigint,
    is_current_user boolean
)
language sql
stable
security definer
set search_path = ''
as $$
    with visible_profiles as (
        select p.id, p.display_name, p.city, p.selected_avatar, p.eco_points
        from public.profiles p
        where p.leaderboard_visible is true or p.id = auth.uid()
    ),
    ranked as (
        select
            vp.*,
            coalesce(fp.n, 0) + coalesce(dc.n, 0) + coalesce(gs.n, 0)
                + coalesce(il.n, 0) + coalesce(bc.n, 0) as actions_count,
            row_number() over (order by vp.eco_points desc, vp.display_name asc) as user_rank
        from visible_profiles vp
        left join lateral (
            select count(*)::bigint as n from public.footprint_entries x where x.user_id = vp.id
        ) fp on true
        left join lateral (
            select count(*)::bigint as n from public.daily_checks x
            where x.user_id = vp.id and x.completed is true
        ) dc on true
        left join lateral (
            select count(*)::bigint as n from public.game_scores x where x.user_id = vp.id
        ) gs on true
        left join lateral (
            select count(*)::bigint as n from public.impact_logs x where x.user_id = vp.id
        ) il on true
        left join lateral (
            select count(*)::bigint as n from public.community_battle_contributions x where x.user_id = vp.id
        ) bc on true
    )
    select
        r.user_rank,
        coalesce(nullif(r.display_name, ''), 'Eco Citizen'),
        coalesce(nullif(r.city, ''), 'Not specified'),
        coalesce(nullif(r.selected_avatar, ''), '🌱 Eco Pioneer'),
        r.eco_points,
        r.actions_count,
        r.id = auth.uid()
    from ranked r
    order by r.user_rank
    limit greatest(1, least(coalesce(p_limit, 50), 100));
$$;

create or replace function public.get_city_leaderboard(p_limit integer default 50)
returns table (
    rank bigint,
    city_name text,
    active_citizens bigint,
    total_kwh_saved numeric,
    eco_points_total bigint,
    is_user_city boolean
)
language sql
stable
security definer
set search_path = ''
as $$
    with city_totals as (
        select
            lower(trim(p.city)) as city_key,
            min(trim(p.city)) as city_name,
            count(*)::bigint as active_citizens,
            sum(p.eco_points)::bigint as eco_points_total
        from public.profiles p
        where p.leaderboard_visible is true
          and p.city is not null and trim(p.city) <> ''
        group by lower(trim(p.city))
    ),
    city_impact as (
        select lower(trim(p.city)) as city_key,
               sum(i.estimated_kwh_saved)::numeric as reported_kwh
        from public.profiles p
        join public.impact_logs i on i.user_id = p.id
        where p.leaderboard_visible is true
          and p.city is not null and trim(p.city) <> ''
        group by lower(trim(p.city))
    ),
    ranked as (
        select
            c.*,
            coalesce(ci.reported_kwh, 0) as reported_kwh,
            row_number() over (order by c.eco_points_total desc, c.city_name asc) as city_rank
        from city_totals c
        left join city_impact ci on ci.city_key = c.city_key
    )
    select
        r.city_rank,
        r.city_name,
        r.active_citizens,
        r.reported_kwh,
        r.eco_points_total,
        lower(r.city_name) = lower(coalesce((
            select p.city from public.profiles p where p.id = auth.uid()
        ), ''))
    from ranked r
    order by r.city_rank
    limit greatest(1, least(coalesce(p_limit, 50), 100));
$$;

revoke all on function public.get_community_battles() from public, anon;
revoke all on function public.join_community_battle(text) from public, anon;
revoke all on function public.contribute_to_community_battle(text, numeric) from public, anon;
revoke all on function public.get_user_leaderboard(integer) from public, anon;
revoke all on function public.get_city_leaderboard(integer) from public, anon;

grant execute on function public.get_community_battles() to authenticated;
grant execute on function public.join_community_battle(text) to authenticated;
grant execute on function public.contribute_to_community_battle(text, numeric) to authenticated;
grant execute on function public.get_user_leaderboard(integer) to authenticated;
grant execute on function public.get_city_leaderboard(integer) to authenticated;

comment on table public.community_battle_contributions is
    'Self-reported personal contributions. They are not verified and do not award EcoPoints.';
comment on column public.profiles.leaderboard_visible is
    'Opt-in setting controlling whether this profile appears in other users community leaderboards.';
