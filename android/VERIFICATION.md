# BloodLink Kavaratti verification record

Date checked: 2026-08-26

## Build commands

```bash
export JAVA_HOME=/usr/lib/jvm/java-21-openjdk-amd64
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
| App label | `BloodLink Kavaratti` |
| Requested runtime permissions | No runtime permissions; manifest contains `INTERNET` and the generated AndroidX internal receiver permission |
| APK | `app/build/outputs/apk/debug/app-debug.apk` |
| APK size | Approximately 18 MB |
| SHA-256 | `dbbd94b2596d88263a21055cfa33b8289da4fd5fdbc7f871353d46e35d508580` |

## Functional scope verified by compilation and code review

The project contains the donor directory, seeded Kavaratti profiles, name search, blood-group filters, availability labels, donor detail sheet, profile creation and editing, system photo picker, encrypted local storage, phone sharing toggle, privacy copy, and safe dialer handoff. The current build is a local-first prototype; no emulator or physical-device interaction was available in the sandbox, and multi-device synchronization is not implemented.

## Asset and privacy notes

The only bundled visual asset is the original BloodLink blood-drop launcher vector. Profile photos are chosen by the user through the system picker and are referenced locally. The app does not request contacts, SMS, call logs, or direct-call permission.
