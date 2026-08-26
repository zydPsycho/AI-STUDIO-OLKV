# BloodLink Kavaratti

**BLOODLINK by KADU** is a native Android donor directory for KADU union members in Kavaratti, Lakshadweep. It uses a warm, minimal Material 3 interface to browse shared donor profiles, filter by blood group, and call members directly.

## Implemented

The APK connects to Supabase project `whvrmzfesmdmwmkxtcsg` through its public REST and Storage APIs. It includes shared donor cards, name search, blood-group chips for A+, A−, B+, B−, O+, O−, AB+, and AB−, availability indicators, donor detail sheets, no-login profile creation, system photo-picker integration, local profile caching, public phone details, and a safe `ACTION_DIAL` handoff. Contacts, call logs, SMS, and direct-call permissions are not requested.

A profile requires a name, age between 18 and 70, blood group, and phone number. A profile photo is optional; the app uses initials as an accessible fallback when no photo is chosen. Phone numbers are intentionally visible because this is a KADU union-use directory.

## Build

Open the `android` directory in Android Studio, or run:

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export SUPABASE_URL=https://whvrmzfesmdmwmkxtcsg.supabase.co
export SUPABASE_PUBLISHABLE_KEY=<public-key-from-Supabase-project-settings>
./gradlew :app:assembleDebug
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. The project uses Kotlin, Jetpack Compose, Material 3, AndroidX Security Crypto, and the AndroidX SplashScreen API. Minimum SDK is 26 and target/compile SDK is 35.

## Prototype limitation

This APK is shared-data enabled: donor profiles, photos, availability, and targeted emergency alerts are stored in the selected Supabase project so all distributed Android devices see the same live data. There is intentionally no login. Anyone who receives the APK can view and publish records, so distribute it only within KADU. A production release should add an invite gate, moderation/reporting, audit logs, rate limits, and emergency-service guidance.

## Safety and privacy

BLOODLINK is not an emergency service or a hospital blood bank. Users should verify donor identity, eligibility, and hospital instructions before relying on a donor profile. The app does not claim to verify donor eligibility. Phone numbers are stored and displayed as entered for union coordination.
