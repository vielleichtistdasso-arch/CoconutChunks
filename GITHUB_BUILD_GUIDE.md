# GitHub Build Guide — Coconut Chunks

You do not need Android Studio for this path.

## What GitHub Actions will do

The workflow `.github/workflows/android-ci.yml` runs two jobs:

1. `Build, JVM tests, Debug APK`
   - JDK 17
   - Android SDK 36
   - Gradle 8.13
   - `:app:testDebugUnitTest`
   - `:app:assembleDebug`
   - uploads `app-debug.apk`

2. `Android emulator tests`
   - boots an Android API 35 x86_64 emulator
   - runs `:app:connectedDebugAndroidTest`
   - uploads instrumentation test reports

The project intentionally does not require a local `gradle-wrapper.jar` for CI.
GitHub installs Gradle 8.13 through `gradle/actions/setup-gradle`.

## First upload to GitHub

1. Create a new empty repository on GitHub, for example:
   `CoconutChunks`
2. Extract the provided zip.
3. Upload the **contents** of the `CoconutChunks` folder to the repository root.
   Make sure these are at the repository root:
   - `app/`
   - `.github/`
   - `build.gradle.kts`
   - `settings.gradle.kts`
   - `gradle.properties`
4. Commit to `main`.

Pushing to `main` automatically starts the workflow.

You can also run it manually:

- Open the repository.
- Open **Actions**.
- Select **Android CI**.
- Choose **Run workflow**.

## Download the APK

After the `Build, JVM tests, Debug APK` job becomes green:

1. Open that Actions run.
2. Scroll to **Artifacts**.
3. Download:
   `Coconut-Chunks-debug-APK`
4. Extract it.
5. The file inside is:
   `app-debug.apk`

## Install on your Android phone

Transfer `app-debug.apk` to the phone.

Open the APK from the phone's Files app. Android may ask you to allow
"Install unknown apps" for the browser/files app being used.

This is a debug APK signed automatically with the Android debug key.
It is suitable for personal testing.

## What to send back if GitHub fails

Open the failed Actions job and copy the first meaningful error block,
preferably starting at:

- `FAILURE: Build failed with an exception.`
- `e: file:///...`
- `Execution failed for task ...`
- `Android resource linking failed`

Send that error text back for the next fix round.
