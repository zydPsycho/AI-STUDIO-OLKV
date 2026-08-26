# BLOODLINK by KADU

BLOODLINK by KADU is a shared blood-donor directory for KADU union members in Kavaratti, Lakshadweep. The product includes donor profiles, blood-group search, donor availability, direct phone contact, profile photos, and an emergency-alert foundation.

## Project surfaces

The Android client is a native Kotlin and Jetpack Compose application under `android/`. The public web directory is a TanStack Start application under `src/`, deployed through Vercel. Supabase stores the shared KADU donor records and profile-photo assets.

## Android build

```bash
cd android
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export SUPABASE_URL=https://whvrmzfesmdmwmkxtcsg.supabase.co
export SUPABASE_PUBLISHABLE_KEY=<public-key-from-Supabase-project-settings>
./gradlew :app:assembleDebug
```

The APK is written to `android/app/build/outputs/apk/debug/app-debug.apk`.

## Web build

```bash
pnpm install
pnpm build
```

## Product boundary

BLOODLINK is not a marketplace. Do not add buyer, seller, listing, payment, subscription, or unrelated account-management flows. The current no-login directory is intended for controlled KADU distribution; anyone who receives the APK or public URL can otherwise access the shared records.
