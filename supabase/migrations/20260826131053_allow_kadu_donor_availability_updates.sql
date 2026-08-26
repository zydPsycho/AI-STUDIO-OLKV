drop policy if exists kadu_donors_public_update on public.kadu_donors;
create policy kadu_donors_public_update
on public.kadu_donors
for update
to anon, authenticated
using (true)
with check (
  union_name = 'KADU'
  and char_length(btrim(name)) between 2 and 80
  and age between 18 and 70
);
