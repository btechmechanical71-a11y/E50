-- E50 Phase 2: user-owned database tables and Row Level Security.
-- Apply in the Supabase SQL Editor for the E50 project.
-- Eco-point and eco-credit balances are intentionally not writable by the app.
-- Their trusted reward ledger and server-side awarding logic are Phase 3.

create table if not exists public.profiles (
    id uuid primary key references auth.users (id) on delete cascade,
    display_name text not null default 'Eco Citizen',
    city text not null default 'Ahmedabad',
    onboarding_completed boolean not null default false,
    selected_avatar text not null default '🌱 Eco Pioneer',
    unlocked_avatars text[] not null default array['🌱 Eco Pioneer']::text[],
    eco_points integer not null default 0 check (eco_points >= 0),
    eco_credits integer not null default 0 check (eco_credits >= 0),
    created_at timestamptz not null default now(),
    updated_at timestamptz not null default now()
);

-- The E50 Supabase project may already contain a minimal profiles table.
-- Add the Phase 2 fields safely without replacing or deleting existing rows.
alter table public.profiles
    add column if not exists onboarding_completed boolean not null default false,
    add column if not exists selected_avatar text not null default '🌱 Eco Pioneer',
    add column if not exists unlocked_avatars text[] not null default array['🌱 Eco Pioneer']::text[],
    add column if not exists eco_points integer not null default 0 check (eco_points >= 0),
    add column if not exists eco_credits integer not null default 0 check (eco_credits >= 0),
    add column if not exists updated_at timestamptz not null default now();

create table if not exists public.household_dna (
    user_id uuid primary key references auth.users (id) on delete cascade,
    household_size integer not null default 4 check (household_size between 1 and 50),
    housing_type text not null default 'Apartment',
    ownership text not null default 'Owner',
    monthly_kwh_estimate numeric(12, 2) not null default 260 check (monthly_kwh_estimate >= 0),
    monthly_bill_estimate numeric(12, 2) not null default 1850 check (monthly_bill_estimate >= 0),
    appliances text[] not null default '{}'::text[],
    ac_usage_hours integer not null default 6 check (ac_usage_hours between 0 and 24),
    ac_temp_preference integer not null default 22 check (ac_temp_preference between 16 and 35),
    has_rooftop_access boolean not null default false,
    has_solar_installed boolean not null default false,
    sustainability_interests text[] not null default '{}'::text[],
    updated_at timestamptz not null default now()
);

create table if not exists public.footprint_entries (
    user_id uuid not null references auth.users (id) on delete cascade,
    id text not null,
    month_name text not null,
    year integer not null check (year between 2000 and 2200),
    kwh numeric(12, 3) not null check (kwh >= 0),
    bill_amount numeric(12, 2) not null check (bill_amount >= 0),
    notes text not null default '',
    timestamp_ms bigint not null default (floor(extract(epoch from now()) * 1000)::bigint),
    created_at timestamptz not null default now(),
    primary key (user_id, id),
    unique (user_id, month_name, year)
);

create table if not exists public.user_experiments (
    user_id uuid not null references auth.users (id) on delete cascade,
    experiment_id text not null,
    status text not null default 'NOT_STARTED'
        check (status in ('NOT_STARTED', 'ACTIVE', 'COMPLETED')),
    started_timestamp_ms bigint,
    completed_days integer[] not null default '{}'::integer[],
    updated_at timestamptz not null default now(),
    primary key (user_id, experiment_id)
);

create table if not exists public.daily_checks (
    user_id uuid not null,
    experiment_id text not null,
    day_number integer not null check (day_number between 1 and 366),
    completed boolean not null default false,
    date_string text not null default '',
    reading_kwh numeric(12, 3) check (reading_kwh is null or reading_kwh >= 0),
    notes text not null default '',
    updated_at timestamptz not null default now(),
    primary key (user_id, experiment_id, day_number),
    foreign key (user_id, experiment_id)
        references public.user_experiments (user_id, experiment_id)
        on delete cascade
);

create table if not exists public.game_scores (
    user_id uuid not null references auth.users (id) on delete cascade,
    id text not null,
    game_type text not null,
    score integer not null default 0 check (score >= 0),
    accuracy_percent integer not null default 0 check (accuracy_percent between 0 and 100),
    reported_eco_credits integer not null default 0 check (reported_eco_credits >= 0),
    summary text not null default '',
    timestamp_ms bigint not null default (floor(extract(epoch from now()) * 1000)::bigint),
    created_at timestamptz not null default now(),
    primary key (user_id, id)
);

create table if not exists public.impact_logs (
    user_id uuid not null references auth.users (id) on delete cascade,
    id text not null,
    title text not null,
    category text not null,
    action_detail text not null default '',
    estimated_kwh_saved numeric(12, 3) not null check (estimated_kwh_saved >= 0),
    estimated_kg_co2_avoided numeric(12, 3) not null check (estimated_kg_co2_avoided >= 0),
    photo_note text not null default '',
    timestamp_ms bigint not null default (floor(extract(epoch from now()) * 1000)::bigint),
    status text not null default 'Pending',
    created_at timestamptz not null default now(),
    primary key (user_id, id)
);

-- Create a profile automatically when a Supabase Auth user is created.
create or replace function public.handle_new_user_profile()
returns trigger
language plpgsql
security definer
set search_path = ''
as $$
begin
    insert into public.profiles (id, display_name, city)
    values (
        new.id,
        coalesce(
            nullif(new.raw_user_meta_data ->> 'display_name', ''),
            nullif(split_part(coalesce(new.email, ''), '@', 1), ''),
            'Eco Citizen'
        ),
        coalesce(nullif(new.raw_user_meta_data ->> 'city', ''), 'Ahmedabad')
    )
    on conflict (id) do nothing;

    return new;
end;
$$;

drop trigger if exists on_auth_user_created_profile on auth.users;
create trigger on_auth_user_created_profile
    after insert on auth.users
    for each row execute function public.handle_new_user_profile();

-- Backfill Auth users that existed before this migration.
insert into public.profiles (id, display_name, city)
select
    u.id,
    coalesce(
        nullif(u.raw_user_meta_data ->> 'display_name', ''),
        nullif(split_part(coalesce(u.email, ''), '@', 1), ''),
        'Eco Citizen'
    ),
    coalesce(nullif(u.raw_user_meta_data ->> 'city', ''), 'Ahmedabad')
from auth.users as u
on conflict (id) do nothing;

-- Enable Row Level Security on every user-owned table.
alter table public.profiles enable row level security;
alter table public.household_dna enable row level security;
alter table public.footprint_entries enable row level security;
alter table public.user_experiments enable row level security;
alter table public.daily_checks enable row level security;
alter table public.game_scores enable row level security;
alter table public.impact_logs enable row level security;

-- Remove broad default access, then grant only the app operations required.
revoke all on table public.profiles from anon, authenticated;
revoke all on table public.household_dna from anon, authenticated;
revoke all on table public.footprint_entries from anon, authenticated;
revoke all on table public.user_experiments from anon, authenticated;
revoke all on table public.daily_checks from anon, authenticated;
revoke all on table public.game_scores from anon, authenticated;
revoke all on table public.impact_logs from anon, authenticated;

grant select on table public.profiles to authenticated;
grant insert (id, display_name, city, onboarding_completed, selected_avatar, unlocked_avatars)
    on table public.profiles to authenticated;
grant update (display_name, city, onboarding_completed, selected_avatar, unlocked_avatars)
    on table public.profiles to authenticated;

grant select, insert, update on table public.household_dna to authenticated;
grant select, insert, update, delete on table public.footprint_entries to authenticated;
grant select, insert, update, delete on table public.user_experiments to authenticated;
grant select, insert, update, delete on table public.daily_checks to authenticated;
grant select, insert on table public.game_scores to authenticated;
grant select, insert, delete on table public.impact_logs to authenticated;

-- Profiles: the signed-in user may access only their own profile row.
drop policy if exists "profiles_select_own" on public.profiles;
create policy "profiles_select_own"
    on public.profiles for select to authenticated
    using ((select auth.uid()) = id);

drop policy if exists "profiles_insert_own" on public.profiles;
create policy "profiles_insert_own"
    on public.profiles for insert to authenticated
    with check ((select auth.uid()) = id);

drop policy if exists "profiles_update_own" on public.profiles;
create policy "profiles_update_own"
    on public.profiles for update to authenticated
    using ((select auth.uid()) = id)
    with check ((select auth.uid()) = id);

-- Household DNA: only the owner can read or change their household record.
drop policy if exists "household_dna_select_own" on public.household_dna;
create policy "household_dna_select_own"
    on public.household_dna for select to authenticated
    using ((select auth.uid()) = user_id);

drop policy if exists "household_dna_insert_own" on public.household_dna;
create policy "household_dna_insert_own"
    on public.household_dna for insert to authenticated
    with check ((select auth.uid()) = user_id);

drop policy if exists "household_dna_update_own" on public.household_dna;
create policy "household_dna_update_own"
    on public.household_dna for update to authenticated
    using ((select auth.uid()) = user_id)
    with check ((select auth.uid()) = user_id);

-- Footprint entries: complete CRUD access, restricted to the row owner.
drop policy if exists "footprint_entries_select_own" on public.footprint_entries;
create policy "footprint_entries_select_own"
    on public.footprint_entries for select to authenticated
    using ((select auth.uid()) = user_id);

drop policy if exists "footprint_entries_insert_own" on public.footprint_entries;
create policy "footprint_entries_insert_own"
    on public.footprint_entries for insert to authenticated
    with check ((select auth.uid()) = user_id);

drop policy if exists "footprint_entries_update_own" on public.footprint_entries;
create policy "footprint_entries_update_own"
    on public.footprint_entries for update to authenticated
    using ((select auth.uid()) = user_id)
    with check ((select auth.uid()) = user_id);

drop policy if exists "footprint_entries_delete_own" on public.footprint_entries;
create policy "footprint_entries_delete_own"
    on public.footprint_entries for delete to authenticated
    using ((select auth.uid()) = user_id);

-- Experiment state and daily checks: only the owner can access them.
drop policy if exists "user_experiments_select_own" on public.user_experiments;
create policy "user_experiments_select_own"
    on public.user_experiments for select to authenticated
    using ((select auth.uid()) = user_id);

drop policy if exists "user_experiments_insert_own" on public.user_experiments;
create policy "user_experiments_insert_own"
    on public.user_experiments for insert to authenticated
    with check ((select auth.uid()) = user_id);

drop policy if exists "user_experiments_update_own" on public.user_experiments;
create policy "user_experiments_update_own"
    on public.user_experiments for update to authenticated
    using ((select auth.uid()) = user_id)
    with check ((select auth.uid()) = user_id);

drop policy if exists "user_experiments_delete_own" on public.user_experiments;
create policy "user_experiments_delete_own"
    on public.user_experiments for delete to authenticated
    using ((select auth.uid()) = user_id);

drop policy if exists "daily_checks_select_own" on public.daily_checks;
create policy "daily_checks_select_own"
    on public.daily_checks for select to authenticated
    using ((select auth.uid()) = user_id);

drop policy if exists "daily_checks_insert_own" on public.daily_checks;
create policy "daily_checks_insert_own"
    on public.daily_checks for insert to authenticated
    with check ((select auth.uid()) = user_id);

drop policy if exists "daily_checks_update_own" on public.daily_checks;
create policy "daily_checks_update_own"
    on public.daily_checks for update to authenticated
    using ((select auth.uid()) = user_id)
    with check ((select auth.uid()) = user_id);

drop policy if exists "daily_checks_delete_own" on public.daily_checks;
create policy "daily_checks_delete_own"
    on public.daily_checks for delete to authenticated
    using ((select auth.uid()) = user_id);

-- Game scores are append-only from the mobile app. The reported credit value
-- is untrusted display metadata, not an authoritative balance.
drop policy if exists "game_scores_select_own" on public.game_scores;
create policy "game_scores_select_own"
    on public.game_scores for select to authenticated
    using ((select auth.uid()) = user_id);

drop policy if exists "game_scores_insert_own" on public.game_scores;
create policy "game_scores_insert_own"
    on public.game_scores for insert to authenticated
    with check ((select auth.uid()) = user_id);

-- Impact logs can be viewed and added by their owner; deletion is allowed.
drop policy if exists "impact_logs_select_own" on public.impact_logs;
create policy "impact_logs_select_own"
    on public.impact_logs for select to authenticated
    using ((select auth.uid()) = user_id);

drop policy if exists "impact_logs_insert_own" on public.impact_logs;
create policy "impact_logs_insert_own"
    on public.impact_logs for insert to authenticated
    with check ((select auth.uid()) = user_id);

drop policy if exists "impact_logs_delete_own" on public.impact_logs;
create policy "impact_logs_delete_own"
    on public.impact_logs for delete to authenticated
    using ((select auth.uid()) = user_id);

comment on column public.profiles.eco_points is
    'Server-authoritative reward balance. Do not grant direct client updates; Phase 3 reward RPC/ledger only.';
comment on column public.profiles.eco_credits is
    'Server-authoritative reward balance. Do not grant direct client updates; Phase 3 reward RPC/ledger only.';
comment on column public.game_scores.reported_eco_credits is
    'Untrusted client-reported game metadata. Never use this value to update the profile balance.';
