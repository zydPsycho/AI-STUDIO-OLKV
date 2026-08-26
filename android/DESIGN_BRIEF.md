# BloodLink Kavaratti — Design Brief

## Product intent

BloodLink Kavaratti is a focused community directory for people in Kavaratti, Lakshadweep who are willing to donate blood. The first release prioritizes speed, trust, and privacy over feature depth. A user can create a donor card with their name, age, blood group, profile photo, availability, and optional phone number. Other users can browse and filter donors, open a profile, and request or reveal a phone number only when the donor has explicitly enabled contact visibility.

The app is scoped to **Kavaratti, Lakshadweep**. The location is visible throughout the product so people understand the community boundary and do not mistake the directory for a wider emergency service.

## Audience and primary task

The primary audience is a Kavaratti resident looking for an available blood donor quickly. The primary task is: open the app, choose a blood group, scan trustworthy donor cards, and contact an available donor without exposing private phone numbers by default.

The secondary task is: create or update a donor profile in under one minute, select a profile photo, choose a blood group, and control whether the phone number is visible.

## Screen inventory

| Screen | Purpose | Key actions |
| --- | --- | --- |
| Home / Donor directory | Show local donors and the main search/filter task | Search by name, filter by blood group, open a donor |
| Donor detail | Build trust and provide a privacy-respecting contact action | View profile, request/reveal number if allowed, open phone dialer |
| Create profile | Collect donor identity and preferences | Add photo, name, age, group, phone, availability, contact visibility |
| My profile | Review and edit the current donor card | Edit profile, toggle availability, toggle contact visibility |
| About & safety | Explain scope, privacy, and emergency limitations | Read safety note and data behavior |

Navigation uses a compact bottom navigation bar with **Donors**, **My profile**, and **About**. The create-profile action is a prominent floating action button or top-right add action depending on available width.

## Information hierarchy

1. Location and trust context: “Kavaratti, Lakshadweep” and “Community donor directory”.
2. Immediate utility: blood-group filter chips and availability status.
3. Donor cards: photo, name, age, blood group, availability, and distance/location label.
4. Contact privacy: phone hidden by default; reveal only if the donor has opted in.
5. Safety: the app is not a replacement for emergency services or a hospital blood bank.

## Visual direction

The visual language is calm, warm, and community-oriented rather than clinical. Use an off-white canvas, deep ink text, a coral-red primary accent, and a dark green availability accent. Cards are rounded but not overly soft, with clear borders and minimal elevation. Blood groups are represented by compact chips, not decorative medical imagery. Profile photos are circular with a soft coral fallback avatar showing the user’s initials.

## Design tokens

| Token | Value | Usage |
| --- | --- | --- |
| Ink | `#171817` | Main text, dark surfaces |
| Canvas | `#F8F7F3` | App background |
| Surface | `#FFFFFF` | Cards and sheets |
| Coral | `#C94747` | Primary action, blood badge, key accent |
| Coral-dark | `#8F2D32` | Pressed/contrast state |
| Coral-soft | `#FCE8E5` | Selected chips, hero panel |
| Green | `#246B4A` | Available status |
| Green-soft | `#E3F2E8` | Availability background |
| Muted | `#6C706B` | Supporting copy |
| Outline | `#E2E3DE` | Borders and dividers |
| Radius | 18–24 dp | Cards and primary containers |
| Spacing | 8 dp base | Consistent rhythm |

Typography uses the platform sans-serif with Material 3 typography. Headings are bold and compact; supporting text is readable at 14–16sp. All controls maintain at least a 48dp touch target.

## Interaction map

The directory starts with seeded sample donors for an immediately understandable first frame. A user can filter using chips such as **All**, **A+**, **A−**, **B+**, **B−**, **O+**, **O−**, **AB+**, and **AB−**. Search is case-insensitive. Tapping a card opens a donor detail sheet.

The donor detail sheet shows the phone number only when the donor has enabled “Let other users see my number”. If disabled, the sheet explains that the number is private and presents a neutral “Contact not shared” state rather than exposing partial information. When visible, the action opens the device dialer instead of silently placing a call.

The profile form requires name, age, and blood group. The profile photo is chosen through the system photo picker. Phone number is optional but required to enable contact visibility. The profile is stored locally in this prototype and can be edited or deleted. A clear banner explains that a production multi-device release needs a secure shared backend and verified accounts.

## States

The directory has loading, empty, filtered-empty, and error-safe states. The empty state encourages creating a donor profile. A saved profile shows a compact success snackbar. An invalid age or missing required field produces inline validation text. The app remains usable offline because the prototype’s demo directory and saved profile are local.

## Privacy and safety

Phone numbers are private by default. The app does not request contacts, SMS, call logs, or direct-call permissions. It uses the Android photo picker and the dialer intent only. The About screen states that users should verify donor identity, eligibility, and hospital instructions, and that the app is not an emergency service.

## Responsive behavior

Compact portrait uses one column and bottom navigation. In landscape or medium width, the directory widens to a centered content column with larger cards, while the form uses two columns for age and blood group. Expanded windows can use a navigation rail or a two-pane list/detail layout, but the first implementation stays within a stable single-activity Compose architecture.

## Motion and accessibility

Use short fade/scale transitions for the detail sheet and profile save feedback. Do not communicate availability or privacy status through color alone; always include text labels and icons. Provide content descriptions for profile images and action icons, visible focus states, and readable contrast in both light and dark themes. Keep system status and navigation bars visible and inset-aware.

## Prototype limitation

The APK is implemented as a polished, local-first prototype: profile creation, editing, persistence, seeded donors, filtering, photo selection, contact visibility, and dialer handoff work on-device. Multi-device account sync, backend authentication, verified phone ownership, moderation, and push notifications are intentionally not claimed as complete until a secure production backend is connected.
