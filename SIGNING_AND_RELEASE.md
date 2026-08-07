# Signing and Release Guide

## 1. Create a keystore once

Run locally:

```bash
keytool -genkeypair \
  -v \
  -keystore coconut-chunks-release.jks \
  -alias coconut-chunks \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

Keep the `.jks` file and passwords outside the repository.

## 2. Set environment variables

macOS / Linux:

```bash
export COCONUT_KEYSTORE_PATH="/absolute/path/to/coconut-chunks-release.jks"
export COCONUT_KEYSTORE_PASSWORD="..."
export COCONUT_KEY_ALIAS="coconut-chunks"
export COCONUT_KEY_PASSWORD="..."
```

Windows PowerShell:

```powershell
$env:COCONUT_KEYSTORE_PATH="C:\path\to\coconut-chunks-release.jks"
$env:COCONUT_KEYSTORE_PASSWORD="..."
$env:COCONUT_KEY_ALIAS="coconut-chunks"
$env:COCONUT_KEY_PASSWORD="..."
```

## 3. Build signed release artifacts

APK:

```bash
gradle assembleRelease
```

AAB:

```bash
gradle bundleRelease
```

Expected outputs:

```text
app/build/outputs/apk/release/app-release.apk
app/build/outputs/bundle/release/app-release.aab
```

## 4. Verify the APK

```bash
apksigner verify --verbose app/build/outputs/apk/release/app-release.apk
```

Optional certificate printout:

```bash
apksigner verify --print-certs app/build/outputs/apk/release/app-release.apk
```

## 5. Install on a device

```bash
adb install -r app/build/outputs/apk/release/app-release.apk
```

Then complete `RELEASE_CHECKLIST.md`.

## Security note

Never commit:
- keystore files;
- keystore passwords;
- key passwords;
- release secrets.

This project reads signing credentials only from environment variables.
