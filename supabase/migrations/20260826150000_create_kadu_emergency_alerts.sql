create table if not exists public.kadu_emergency_alerts (
  id uuid primary key default gen_random_uuid(),
  union_name text not null default 'KADU',
  sender_name text not null,
  sender_phone text not null,
  patient_name text not null,
  admitted_in text not null,
  emergency_type text not null,
  required_blood_group text not null,
  units_needed integer not null default 1 check (units_needed between 1 and 20),
  notes text,
  is_active boolean not null default true,
  created_at timestamptz not null default now(),
  constraint kadu_alert_union_check check (union_name = 'KADU'),
  constraint kadu_alert_patient_check check (char_length(btrim(patient_name)) between 2 and 120),
  constraint kadu_alert_hospital_check check (char_length(btrim(admitted_in)) between 2 and 160),
  constraint kadu_alert_emergency_check check (char_length(btrim(emergency_type)) between 2 and 120)
);

create index if not exists kadu_emergency_alerts_created_at_idx on public.kadu_emergency_alerts (created_at desc);
create index if not exists kadu_emergency_alerts_blood_group_idx on public.kadu_emergency_alerts (required_blood_group);

alter table public.kadu_emergency_alerts enable row level security;
drop policy if exists kadu_alerts_public_read on public.kadu_emergency_alerts;
create policy kadu_alerts_public_read on public.kadu_emergency_alerts for select to anon, authenticated using (union_name = 'KADU' and is_active = true);
drop policy if exists kadu_alerts_public_create on public.kadu_emergency_alerts;
create policy kadu_alerts_public_create on public.kadu_emergency_alerts for insert to anon, authenticated with check (union_name = 'KADU' and is_active = true and char_length(btrim(patient_name)) between 2 and 120 and char_length(btrim(admitted_in)) between 2 and 160 and char_length(btrim(emergency_type)) between 2 and 120 and units_needed between 1 and 20);

create table if not exists public.kadu_push_tokens (
  token text primary key,
  platform text not null default 'android' check (platform in ('android', 'web')),
  donor_id uuid references public.kadu_donors(id) on delete set null,
  last_seen_at timestamptz not null default now(),
  created_at timestamptz not null default now()
);

alter table public.kadu_push_tokens enable row level security;
drop policy if exists kadu_push_tokens_public_register on public.kadu_push_tokens;
create policy kadu_push_tokens_public_register on public.kadu_push_tokens for insert to anon, authenticated with check (char_length(token) between 20 and 4096 and platform in ('android', 'web'));
drop policy if exists kadu_push_tokens_public_refresh on public.kadu_push_tokens;
create policy kadu_push_tokens_public_refresh on public.kadu_push_tokens for update to anon, authenticated using (true) with check (char_length(token) between 20 and 4096 and platform in ('android', 'web'));
