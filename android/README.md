# LAK SHIP BOOK

**BLACKMARK** · Native Android app for compliant ship-ticket booking assistance.

## What is implemented

LAK SHIP BOOK opens the official Lakshadweep IRCTC portal at `https://lakshadweep.irctc.co.in/` inside a hardened HTTPS-only WebView. It stores passenger profiles and local booking records in Android encrypted storage, supports multiple passengers, collects trip details, and provides a reusable label/accessibility-based form assistant.

The form assistant fills only compatible passenger fields. It never fills or submits CAPTCHA, OTP, password, payment, card, CVV, UPI PIN, or other security fields. If the portal changes or a security step is detected, the app pauses and shows a manual continuation message. Final review, booking submission, and payment authorization remain with the user on the official portal.

The app does not hard-code schedules, ship names, fares, or availability. Booking history is saved only after an apparent official confirmation page is detected and the user explicitly chooses **SAVE RECORD**. The app does not generate tickets or claim a booking from an uncertain result.

## Build

Open the `android` directory in Android Studio, or run:

```bash
./gradlew :app:assembleDebug
```

The project uses Kotlin, Jetpack Compose, Material 3, AndroidX Security Crypto, Biometric, DataStore-compatible dependencies, and Android WebView. Minimum SDK is 26 and target/compile SDK is 35.

## Security notes

The WebView disables cleartext traffic, file access, and content access, cancels SSL errors, and allows navigation only to `lakshadweep.irctc.co.in` and its `*.irctc.co.in` subdomains. If the live portal redirects to a required payment host outside those domains, add that host only after confirming it is an official payment domain and document the decision in `MainActivity.kt`; do not broaden the allowlist to arbitrary HTTPS sites.

No backend, Firebase, analytics, browsing-history collection, password storage, OTP interception, payment credential capture, CAPTCHA bypass, queue bypass, rate-limit bypass, or undocumented booking endpoint is used.
