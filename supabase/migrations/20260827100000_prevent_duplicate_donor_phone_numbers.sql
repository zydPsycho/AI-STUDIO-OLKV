create unique index if not exists kadu_donors_normalized_phone_uidx
on public.kadu_donors ((regexp_replace(phone, '[^0-9]', '', 'g')));
