# Third-party notices

BloodLink Kavaratti uses official AndroidX and Material 3 components. No third-party photos, fonts, stock imagery, or downloaded illustrations are bundled. User-selected profile photos remain on the device and are not redistributed by the app.

| Asset/dependency | Version or file | Creator/source | URL | License | Attribution/notice | Modifications | App location |
| --- | --- | --- | --- | --- | --- | --- | --- |
| Jetpack Compose and Material 3 | BOM 2025.01.00 | Android Open Source Project / Google | https://developer.android.com/develop/ui/compose | Apache-2.0 | Include the upstream Apache-2.0 notice when redistributing the dependency set | App-specific colors, layouts, and typography | `app/src/main/java`, Gradle dependencies |
| AndroidX Core, Activity, Lifecycle, Security Crypto, SplashScreen | Project-pinned versions | Android Open Source Project / Google | https://developer.android.com/jetpack/androidx | Apache-2.0 | Include the upstream Apache-2.0 notice when redistributing the dependency set | Used through public APIs; no source modifications | Gradle dependencies |
| Material Icons Extended | Compose BOM | Google Material Icons | https://fonts.google.com/icons | Apache-2.0 | Material Symbols and icons are available under Apache License 2.0 | Rendered through the Compose icon artifact | Compose UI |
| BloodLink launcher icon | `res/drawable/ic_launcher.xml` | BLACKMARK original vector | — | Original project asset | No external attribution required | Original blood-drop vector mark | `app/src/main/res/drawable` |

Checked: 2026-08-26. The app does not hotlink or bundle external visual assets.
