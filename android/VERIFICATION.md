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
| Version | `1.0` / version code `1` |
| Compile and target SDK | 35 |
| Minimum SDK | 26 |
| Launcher activity | `com.blackmark.bloodlink.MainActivity` |
| App label | `BLOODLINK by KADU` |
| Requested runtime permissions | No runtime permissions; manifest contains `INTERNET` and the generated AndroidX internal receiver permission |
| APK | `app/build/outputs/apk/debug/app-debug.apk` |
| APK size | Approximately 20 MB |
| SHA-256 | `a895fbbcf0a6f8958a143b40726fa3b143eebd7e977cf7cfa75d99d909e553a8` |

## Functional scope verified by compilation and code review

The project contains the shared KADU donor directory, name search, blood-group filters, availability labels, donor detail sheet, no-login profile creation, system photo picker, Supabase photo uploads, local profile caching, visible phone details, privacy copy, and safe dialer handoff. The final debug build completed successfully. No emulator or physical-device interaction was available in the sandbox.

## Public deployment

| Item | Result |
| --- | --- |
| Supabase project | `whvrmzfesmdmwmkxtcsg` |
| Supabase donor read smoke test | HTTP 200; empty directory at first deployment |
| Vercel production URL | https://bloodlink-by-kadu.vercel.app |
| GitHub release commit | `1555eb4` |

## Asset and privacy notes

The bundled visual assets are the BLOODLINK by KADU blood-drop launcher vector and the matching web favicon. Profile photos are selected by the user and uploaded to the public Supabase `kadu-donor-photos` bucket. The app does not request contacts, SMS, call logs, or direct-call permission. Because there is no login, the APK and public URL must be distributed only within KADU; Supabase public policies allow anonymous donor reads and inserts by design.
