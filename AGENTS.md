# BLOODLINK by KADU

BLOODLINK is the KADU union blood-donor directory for Kavaratti, Lakshadweep. Keep the user-facing product focused on donor profiles, blood-group search, member contact, availability, and blood-group-targeted emergency alerts. Do not reintroduce marketplace, buyer, seller, listing, payment, subscription, web-directory, or account-login flows.

The only user-facing client in this repository is the native Kotlin and Jetpack Compose Android app under `android/`. Supabase is retained only as the shared donor-data, profile-photo, availability, emergency-alert, and future push-targeting backend. Keep all user-facing copy branded as **BLOODLINK by KADU**.

Closed-app push notifications require Firebase credentials that must remain outside version control. Never commit `google-services.json`, Firebase service-account JSON, or other secrets.
