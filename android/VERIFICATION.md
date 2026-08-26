# BloodLink Kavaratti verification record

Date checked: 2026-08-26

## Build commands

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
export SUPABASE_URL=https://whvrmzfesmdmwmkxtcsg.supabase.co
export SUPABASE_PUBLISHABLE_KEY=<public-key-from-Supabase-project-settings>
./gradlew :app:assembleDebug
```

Result: `BUILD SUCCESSFUL`.

## APK checks

| Check | Result |
| --- | --- |
| Package | `com.blackmark.bloodlink` |
| Version | `1.1` / version code `2` |
| Compile and target SDK | 35 |
| Minimum SDK | 26 |
| Launcher activity | `com.blackmark.bloodlink.MainActivity` |
| App label | `BLOODLINK by KADU` |
| Requested runtime permissions | No runtime permissions; manifest contains `INTERNET` and the generated AndroidX internal receiver permission |
| APK | `app/build/outputs/apk/debug/app-debug.apk` |
| APK size | Approximately 20 MB |
| SHA-256 | `793c596cc8cbba902d94df6856e663cef6dd71c93b6f8d731944fb38615c1e55` |

## Functional scope verified by compilation and code review

The project contains the shared KADU donor directory, name search, blood-group filters, availability labels, donor detail sheet, one-profile-per-device creation, existing-profile shortcut in the directory header, cached system photo-picker files for Supabase upload, local profile caching, visible phone details, Settings-based availability updates, duplicate self-profile suppression by owner ID plus normalized name and phone, reliable remote photo rendering through an on-device cache, Android build-time Supabase configuration loading, Android emergency-alert creation and feed filtered by the current donor blood group, patient/hospital/emergency/blood-group/units/notes fields, direct sender dialing, in-app emergency-tone preview, privacy copy, and safe dialer handoff. The final debug build completed successfully. No emulator or physical-device interaction was available in the sandbox.

## Shared backend

| Item | Result |
| --- | --- |
| Supabase project | `whvrmzfesmdmwmkxtcsg` |
| Supabase donor read smoke test | HTTP 200 |
| Client surface | Native Android APK only |
| GitHub release commit | Updated in the APK-only release commit |

## Asset and privacy notes

The bundled visual asset is the BLOODLINK by KADU launcher vector. Profile photos are selected by the user and uploaded to the public Supabase `kadu-donor-photos` bucket. The app does not request contacts, SMS, call logs, or direct-call permission. Because there is no login, the APK should be distributed only within KADU; Supabase public policies allow anonymous donor reads, inserts, and availability updates by design. Each device locally tracks one profile and hides it from that device’s donor list. Emergency alerts are queried only for the locally owned donor’s blood group; a user without a donor profile sees no targeted alert feed. Closed-app push delivery is not enabled in this build because Firebase `google-services.json` and a trusted Firebase service-account secret have not been supplied; alerts are currently available through the in-app feed and refresh flow.
