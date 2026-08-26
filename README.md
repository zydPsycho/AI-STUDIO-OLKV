# BLOODLINK by KADU

**BLOODLINK by KADU** is an Android-only donor directory for KADU union members in Kavaratti, Lakshadweep. The app lets members browse shared donor profiles, search by name, filter by blood group, view availability, call other members, publish a targeted emergency blood request, and contact an alert sender.

## Product boundary

This repository contains only the native Android client and the Supabase backend migrations used by it. There is no public web directory, web application, Vercel user-facing surface, marketplace, buyer or seller flow, listing system, payment flow, subscription flow, or login system.

Supabase remains the shared backend for KADU donor records, public profile photos, availability updates, emergency alerts, and the future push-token targeting foundation. The Android app uses the Supabase REST and Storage APIs with the project URL and publishable key supplied at build time.

## Android build

Open the `android` directory in Android Studio, or run:

```bash
cd android
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export SUPABASE_URL=https://whvrmzfesmdmwmkxtcsg.supabase.co
export SUPABASE_PUBLISHABLE_KEY=<public-key-from-Supabase-project-settings>
./gradlew :app:assembleDebug
```

The APK is written to `android/app/build/outputs/apk/debug/app-debug.apk`.

## Implemented Android features

The APK includes one-profile-per-device ownership, self-profile exclusion from donor search, name search, blood-group filtering, donor photos, availability management in Settings, visible phone numbers for KADU coordination, safe dialer handoff, targeted emergency-alert retrieval by matching blood group, alert creation with patient and hospital details, an “I can help” contact action, and an in-app emergency tone preview.

Closed-app push notifications are not enabled yet. Completing that feature requires Firebase `google-services.json` for package `com.blackmark.bloodlink` and a server-side Firebase service-account secret. Do not commit either credential.

## Distribution and safety

The no-login APK is intended for controlled KADU distribution. Anyone who receives the APK may be able to view or publish shared records, so distribute it only within the union. BLOODLINK is not a hospital blood bank or emergency service. Users should verify donor identity, eligibility, and hospital instructions before relying on a profile or alert.
