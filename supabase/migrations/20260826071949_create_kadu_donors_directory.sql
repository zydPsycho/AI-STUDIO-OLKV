create extension if not exists pgcrypto;

create table if not exists public.kadu_donors (
  id uuid primary key default gen_random_uuid(),
  union_name text not null default 'KADU' check (union_name = 'KADU'),
  name text not null check (char_length(btrim(name)) between 2 and 80),
  age smallint not null check (age between 18 and 70),
  blood_group text not null check (blood_group in ('A+','A−','B+','B−','O+','O−','AB+','AB−')),
  phone text not null check (char_length(regexp_replace(phone, '[^0-9+]', '', 'g')) between 7 and 16),
  photo_url text,
  is_available boolean not null default true,
  created_at timestamptz not null default timezone('utc', now()),
  updated_at timestamptz not null default timezone('utc', now())
);

create or replace function public.set_kadu_donor_updated_at()
returns trigger language plpgsql security invoker as $$
begin
  new.updated_at = timezone('utc', now());
  return new;
end;
$$;

drop trigger if exists kadu_donors_set_updated_at on public.kadu_donors;
create trigger kadu_donors_set_updated_at before update on public.kadu_donors for each row execute function public.set_kadu_donor_updated_at();

alter table public.kadu_donors enable row level security;

drop policy if exists kadu_donors_public_read on public.kadu_donors;
create policy kadu_donors_public_read on public.kadu_donors for select to anon, authenticated using (true);

drop policy if exists kadu_donors_public_create on public.kadu_donors;
create policy kadu_donors_public_create on public.kadu_donors for insert to anon, authenticated with check (union_name = 'KADU' and char_length(btrim(name)) between 2 and 80 and age between 18 and 70);

insert into storage.buckets (id, name, public, file_size_limit, allowed_mime_types)
values ('kadu-donor-photos', 'kadu-donor-photos', true, 5242880, array['image/jpeg','image/png','image/webp']::text[])
on conflict (id) do update set public = excluded.public, file_size_limit = excluded.file_size_limit, allowed_mime_types = excluded.allowed_mime_types;

drop policy if exists kadu_donor_photos_public_read on storage.objects;
create policy kadu_donor_photos_public_read on storage.objects for select to anon, authenticated using (bucket_id = 'kadu-donor-photos');

drop policy if exists kadu_donor_photos_public_upload on storage.objects;
create policy kadu_donor_photos_public_upload on storage.objects for insert to anon, authenticated with check (bucket_id = 'kadu-donor-photos');
