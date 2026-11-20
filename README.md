<p align="center">
  <img src="brand/app-icon.svg" width="96" height="96" alt="Büffel logo">
</p>

<h1 align="center">Büffel</h1>

<p align="center">Learn any dataset. Local-only, with an open pack format that people, scripts and chatbots can write.</p>

<p align="center">
  <img src="docs/screenshots/android/01-library.png" width="180" alt="Library with packs and daily progress">
  <img src="docs/screenshots/android/03-pack-detail.png" width="180" alt="Pack detail with sections and learning progress">
  <img src="docs/screenshots/android/05-study-choice.png" width="180" alt="A multiple-choice question in a study session">
  <img src="docs/screenshots/android/08-stats.png" width="180" alt="Statistics with streak and activity">
</p>

Büffel is a spaced-repetition learning app for Android and iOS. You learn from **packs**: single JSON files that hold anything you can ask a question about, such as vocabulary, history facts, exam questions or interview preparation. Packs have an [open, documented format](format/README.md), so a chatbot can write one for you in a minute.

Everything stays on the device: no account, no sync, no analytics, no network code in the shared core (the Android app does not even request the internet permission).

## Features

- **Seven item types**: flashcards (one or both directions), multiple choice, short typed answers, cloze (fill the gaps), true/false, ordering and open questions with a self-graded key point checklist.
- **Spaced repetition**: an SM-2 family scheduler tracks every card direction separately. Learn mode asks what is due and introduces new items; practice mode drills any items (mixed, flashcards, quiz or typing) without changing the schedule.
- **Stats**: daily goal, streak, accuracy and an activity history.
- **Easy to add content**: open a `.bueffel` file (also from Files, Mail or a chat), paste text, pick from the bundled sample library, or use "Create with AI" to copy a ready-made chatbot prompt.
- **Pack updates keep your progress**: importing a newer revision of a pack keeps the progress on items that still exist ([spec section 4.2](format/README.md#42-updating-a-pack)).
- **English and German**, light and dark theme.
- **One core, two native UIs**: all logic lives in a Kotlin Multiplatform module; the apps are Jetpack Compose (Android) and SwiftUI (iOS).

## The pack format

A pack is one UTF-8 JSON file (`.bueffel`, media type `application/vnd.bueffel.pack+json`).

- Required: `format` (`"bueffel-pack"`), `version` (`1`), `id` (stable and unique), `title`, and a non-empty `items` array.
- Optional: `description`, `language`, `author`, `license`, `tags`, `icon`, `color`, `revision`, `sections` (chapters) and `study` (order, new items per session, reversible cards, ...).
- Every item has a unique `id` and a `type`: `card`, `choice`, `text`, `cloze`, `truefalse`, `order` or `open`. It may also have a `section`, a `hint` (shown before answering) and an `explanation` (shown after).
- Text is plain with `**bold**`, `*italic*` and `` `code` ``. Cloze gaps are written `{{answer}}`, with an optional hint `{{answer::hint}}` and alternatives `{{colour|color}}`.
- Progress is not part of a pack. It is kept by the app, keyed by pack `id` and item `id`.
- Writers are strict, readers are forgiving: the app skips what it cannot use, imports the rest and tells you what it skipped.

```json
{
  "format": "bueffel-pack",
  "version": 1,
  "id": "pack.capitals.9c41ab",
  "title": "Capitals",
  "language": "en",
  "items": [
    { "id": "france", "type": "card", "front": "France", "back": "Paris", "reversible": true },
    { "id": "largest-ocean", "type": "text", "question": "Which is the largest ocean?", "answers": ["Pacific", "Pacific Ocean"] },
    { "id": "berlin-wall", "type": "cloze", "text": "The Berlin Wall fell in {{1989::year}}." }
  ]
}
```

- [`format/README.md`](format/README.md): the normative specification (format version 1, draft)
- [`format/AUTHORING.md`](format/AUTHORING.md): guide for AI assistants and tool authors
- [`format/prompt.txt`](format/prompt.txt): the copy-paste chatbot prompt
- [`format/bueffel-pack.schema.json`](format/bueffel-pack.schema.json): JSON Schema 2020-12
- [`format/examples/`](format/examples/): sample packs (also bundled in the app); [`format/validate.py`](format/validate.py) and [`format/test-vectors.json`](format/test-vectors.json) check packs and readers

## Create a pack with any chatbot

1. In the app choose **Add pack**, then **Create with AI**, and copy the prompt. (It is the same text as [`format/prompt.txt`](format/prompt.txt).)
2. Paste it into any chatbot and fill in the placeholders: topic or your own notes, language, level and number of items.
3. Paste the chatbot's answer back into Büffel, or save it as `something.bueffel` and open it. The import preview shows what will be added and lists any problems.

To check a pack on a computer first: `pip install jsonschema`, then `python3 format/validate.py my-pack.bueffel`.

## Project structure

| Path | Content |
|---|---|
| `shared/` | Kotlin Multiplatform core: pack reader, scheduler, study sessions, SQLDelight storage. Entry point `BueffelSdk`; reference in [`shared/README.md`](shared/README.md). On iOS it is the `shared` framework (via SKIE). |
| `androidApp/` | Android app: Jetpack Compose and Material 3. |
| `iosApp/` | iOS app: SwiftUI. The Xcode project is generated from `project.yml` with XcodeGen. Only `iosApp/Bueffel/SDK/` talks to `shared`. |
| `format/` | The pack format: specification, schema, validator, test vectors, authoring guide, examples. |
| `brand/` | Logo and symbol (SVG). |
| `docs/screenshots/` | Screenshots used in this README. |
| `scripts/ios/` | Helper for the iOS release job. |
| `.github/` | CI and release workflows, the iOS runner setup action, Dependabot. |

## Build

### Android

Requirements: JDK 17 and the Android SDK (set `ANDROID_HOME` or `sdk.dir` in `local.properties`). `compileSdk` is 37, `minSdk` is 26.

```sh
./gradlew androidApp:assembleDebug        # debug APK in androidApp/build/outputs/apk/debug/
./gradlew shared:allTests                 # shared module tests (the iOS test targets only run on macOS)
./gradlew androidApp:testDebugUnitTest    # Android unit tests
./gradlew androidApp:lintDebug            # Android lint
./gradlew shared:ktlintCheck androidApp:ktlintCheck
python3 format/validate.py --strict       # pack format checks (needs: pip install jsonschema)
```

### iOS

Requires macOS with Xcode, JDK 17 and [XcodeGen](https://github.com/yonaskolb/XcodeGen) (`brew install xcodegen`). The deployment target is iOS 18. These are the steps CI runs:

```sh
./gradlew shared:linkDebugFrameworkIosSimulatorArm64

SDK_NAME=$(xcrun --sdk iphonesimulator --show-sdk-version)
DEST="shared/build/xcode-frameworks/Debug/iphonesimulator${SDK_NAME}"
mkdir -p "$DEST"
cp -R shared/build/bin/iosSimulatorArm64/debugFramework/shared.framework "$DEST/"

cd iosApp
xcodegen generate
xcodebuild build \
  -project Bueffel.xcodeproj \
  -scheme Bueffel \
  -destination 'generic/platform=iOS Simulator' \
  -configuration Debug \
  CODE_SIGNING_ALLOWED=NO \
  ARCHS=arm64
```

`ARCHS=arm64` because the staged framework is the arm64 simulator slice. The Kotlin/Native tests of the shared module run with `./gradlew shared:iosSimulatorArm64Test`. To run the app, open `iosApp/Bueffel.xcodeproj` after the steps above.

## Continuous integration

| Workflow | Runs on | What it does |
|---|---|---|
| `android.yml` | pull requests and pushes to `main`, manual | ktlint, shared module build, debug APK (uploaded as artifact), Android lint, shared and Android unit tests, informational Kover coverage. `android-gate` is the always-reporting required check. |
| `ios.yml` | pull requests and pushes to `main`, manual | `ios-build` (shared framework and simulator app build, the fast compile signal), then `ios-tests` (Kotlin/Native tests of `shared`). `ios-gate` is the always-reporting required check. |
| `release.yml` | published GitHub release, manual dispatch | Signed release builds, see below. |

The iOS jobs run on GitHub's `xcode-27` image with the Xcode 27.0 GM pinned and verified by `.github/actions/ios-setup`. Dependabot proposes weekly Gradle and GitHub Actions updates (Kotlin is bumped by hand together with SKIE).

## Release

Publishing a GitHub release (tag such as `v1.0.0`) runs `release.yml`. It can also be started by hand from the Actions tab (inputs: `version`, and `platforms` = both, android or ios, to retry one platform on its own). The version must look like `1.2.3` or `v1.2.3`.

- **Versions**: the tag without `v` is the Android `versionName` and the iOS `MARKETING_VERSION`. The workflow run number is the iOS build number and is folded into the Android `versionCode` (`(major*10000 + minor*100 + patch) * 1000 + run number`), so re-runs always increase.
- **Android job**: builds a signed AAB and APK, uploads both as workflow artifacts, attaches them to the GitHub release as `bueffel-<version>.apk` and `.aab`, and uploads the AAB to the Google Play internal testing track when `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` is set.
- **iOS job**: checks the stored provisioning profile, builds the shared release framework, archives with App Store Connect API key cloud signing, removes the development certificates that cloud signing created, exports with manual signing (Apple Distribution certificate and the stored profile), verifies the signature and uploads to TestFlight. The IPA is kept as workflow artifact.

A job whose required secrets are missing fails in its first step and names them. No repository variables are used.

| Secret | Job | Content |
|---|---|---|
| `ANDROID_KEYSTORE_BASE64` | Android | The signing keystore, base64 on one line: `base64 -w0 release.jks \| gh secret set ANDROID_KEYSTORE_BASE64` |
| `ANDROID_KEYSTORE_PASSWORD` | Android | Keystore password |
| `ANDROID_KEY_ALIAS` | Android | Key alias in the keystore |
| `ANDROID_KEY_PASSWORD` | Android | Password of that key |
| `GOOGLE_PLAY_SERVICE_ACCOUNT_JSON` | Android, optional | Play Console service account JSON (plain text). Without it the Play upload is skipped. The app must exist in the Play Console first; a brand-new app may refuse the `completed` release status, then do the first upload by hand. |
| `APP_STORE_CONNECT_API_KEY_BASE64` | iOS | The `.p8` App Store Connect API key (role Admin or App Manager): `openssl base64 -A -in AuthKey_XXXXXXXXXX.p8 \| gh secret set APP_STORE_CONNECT_API_KEY_BASE64` |
| `APP_STORE_CONNECT_KEY_ID` | iOS | Key ID of that API key |
| `APP_STORE_CONNECT_ISSUER_ID` | iOS | Issuer ID of the App Store Connect account |
| `APPLE_TEAM_ID` | iOS | Apple developer team ID (injected into `iosApp/ExportOptions.plist` during the job) |
| `IOS_DIST_CERT_P12_BASE64` | iOS | Apple Distribution certificate with private key, exported as `.p12`, base64 on one line |
| `IOS_DIST_CERT_PASSWORD` | iOS | Password of the `.p12` |
| `IOS_PROFILE_APP_BASE64` | iOS | App Store provisioning profile for `ch.orellbuehler.bueffel`, base64 on one line: `openssl base64 -A -in Bueffel_App_Store.mobileprovision \| gh secret set IOS_PROFILE_APP_BASE64` |

One-time iOS setup: register the App ID `ch.orellbuehler.bueffel`, create the app in App Store Connect, and create the App Store provisioning profile with the exact name `Bueffel App Store` (the name `iosApp/ExportOptions.plist` refers to). If the app gains capabilities later, regenerate the profile and update the secret.

## Status

MVP. The shared core and the Android app are built and tested in CI. The iOS app has not been compiled yet: the `ios.yml` run is the first compile gate, so expect build fixes on the first run.

No licence has been chosen yet, so nothing in this repository is licensed for reuse for now.
