# DUA SMART APP

Offline-first Android application for school administration, daily attendance
tracking, and monthly fee management — built with **Kotlin + Jetpack Compose +
Room** so it compiles reliably in GitHub Actions with no manual GUI setup.

Branding: **DUA SMART APP** (app name, launcher label, header logo/title and a
custom vector monogram icon). UI: high-contrast grey glassmorphism with
thin-light stroke icons.

## Tech stack

- Language: Kotlin
- UI: Jetpack Compose (Material 3)
- Database: Room (SQLite) — 100% offline
- Build: Gradle (JDK 17, Android SDK command-line tools in CI)
- Excel reports: zero-dependency XLSX writer (ZIP + XML)
- Database handoff: single-file JSON backup (.json) via Android Share Sheet
- WhatsApp alerts via `https://wa.me/<number>?text=...` intents

## Deliverable layout

```
DUA_SMART_APP/                      <- project root (this folder)
├── app/
│   ├── build.gradle.kts            -> app module (Compose, Room, serialization)
│   ├── proguard-rules.pro
│   └── src/main/
│       ├── AndroidManifest.xml
│       ├── java/com/duasmartapp/
│       │   ├── di/
│       │   │   ├── DuaApp.kt             -> Application class + default data seeder
│       │   │   ├── AppDatabase.kt        -> Room database
│       │   │   ├── DatabaseSeeder.kt     -> default admin/teacher + settings
│       │   │   └── Repository.kt         -> all business logic (auth, students,
│       │   │                              attendance, fees, holidays, backup)
│       │   ├── model/                    -> Room entities (@Serializable)
│       │   │   ├── Student.kt            -> incl. individual monthlyFeeRate
│       │   │   ├── AppUser.kt            -> admin/teacher credentials
│       │   │   ├── AttendanceRecord.kt
│       │   │   ├── Holiday.kt
│       │   │   └── FeeRecord.kt
│       │   ├── model/dao/                -> Room DAOs
│       │   ├── ui/
│       │   │   ├── Screen.kt             -> navigation routes
│       │   │   ├── AppViewModel.kt       -> state, navigation, WhatsApp, Excel
│       │   │   ├── MainActivity.kt
│       │   │   ├── theme/                -> Color.kt, Type.kt, Theme.kt
│       │   │   │                         -> glass colours, ink (#0F172A), pills
│       │   │   ├── components/Glass.kt   -> GlassCard, StatusPill, BottomNav…
│       │   │   └── screens/              -> Login, Dashboard, Attendance,
│       │   │                              StudentCRUD, Fees, Holidays, Users, Backup
│       │   └── utils/XlsxWriter.kt       -> .xlsx register generator
│       └── res/
│           ├── drawable/                 -> vector monogram launcher icon +
│           │                              thin-light stroke UI icons (1.5px)
│           ├── values/                   -> strings.xml, colors.xml, themes.xml
│           └── xml/                      -> filepaths.xml (FileProvider)
├── build.gradle.kts
├── settings.gradle.kts
├── gradle.properties
├── gradlew / gradlew.bat
├── gradle/wrapper/gradle-wrapper.properties
├── .github/workflows/build-apk.yml     -> CI: assembleDebug + upload artifact
├── push.bat                            -> one-click credential-embedded push
└── .gitignore
```

## Pre-populated default data (strings.xml)

Edit `app/src/main/res/values/strings.xml` before publishing:

- School name: `Dua Public School Chak Siyal`
- Classes: `KG through Class 8`
- Currency: `PKR` / `Rs.`
- Default Admin: `admin` / `admin123`
- Default Teacher: `teacher` / `teacher123`

## GitHub Actions CI/CD

`.github/workflows/build-apk.yml` triggers on every push to `main`:

1. Checks out the repo
2. Sets up JDK 17 (temurin)
3. Sets up Gradle and the Android SDK (cmdline-tools, platform 34, build-tools 34.0.0)
4. Regenerates the Gradle wrapper, then runs `./gradlew assembleDebug`
5. Uploads `app/build/outputs/apk/debug/app-debug.apk` as a workflow artifact
   (`app-debug-apk`, retained 30 days)

## One-click push to GitHub

1. Create a GitHub repository named `dua-smart-app` (empty, no README).
2. Create a **Personal Access Token** (classic or fine-grained) with `repo` scope
   at https://github.com/settings/tokens.
3. Edit `push.bat` and replace:
   - `YOUR_GITHUB_USERNAME` -> your GitHub username
   - `YOUR_GITHUB_TOKEN`      -> your personal access token
4. Double-click **push.bat** (or run it in cmd/PowerShell). It runs:
   ```
   git init
   git add .
   git commit -m "Initial release: DUA SMART APP ..."
   git remote add origin https://<user>:<token>@github.com/<user>/dua-smart-app.git
   git branch -M main
   git push -u origin main --force
   ```
5. The push triggers the Actions workflow automatically. No password prompts.

## Download app-debug.apk from GitHub Actions

1. Open your repository on GitHub.
2. Click the **Actions** tab → **Build Debug APK** → latest run.
3. Scroll to **Artifacts** → download **app-debug-apk**.
4. Unzip → `app-debug.apk` → install on any Android device (enable
   "Install unknown apps" for the source).

## Key features

- **Login & roles:** Admin (full access) / Teacher (attendance, fees, WhatsApp,
  backup — no student deletion, fee-rate editing, or admin settings).
- **Students:** CRUD with per-student admin-assigned monthly fee rate (discounts
  supported), roll no, guardian, class, WhatsApp (+92 format), admission date.
- **Attendance:** one-tap P / A / L per student; Sundays auto-locked; custom
  holidays via Admin Holiday Manager; month-by-month register-style Excel
  export (P / A / L / SUN / HOL + totals + %).
- **2-day absence alert:** students absent 2 consecutive school days are pinned
  to the top with a **Send Message** WhatsApp button.
- **Fees:** per-student monthly ledger; on/after the 4th of the month unpaid
  fees move to Arrears with accumulated balance and a **Send Message** reminder.
- **Offline handoff:** Export / Share Database Backup (single JSON) to a
  substitute teacher via WhatsApp/Bluetooth; Import Database File to resume;
  export again at handover back.

## Permissions

- `INTERNET` — declared for WhatsApp `wa.me` links (opened via intent; no
  network calls made by the app itself).
- Files are shared via `FileProvider` (`com.duasmartapp.fileprovider`) with URI
  permissions — no scoped-storage runtime permissions required.
