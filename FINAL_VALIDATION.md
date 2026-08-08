# Coconut Chunks V1 Final Validation

- [x] No INTERNET permission
- [x] No Firebase dependency
- [x] No analytics
- [x] No ads
- [x] One Room entity only
- [x] Room version 1
- [x] No destructive migration fallback
- [x] No production force unwrap
- [x] Review uses one-shot DB load
- [x] Review pool has one shuffle call
- [x] Stable Library keys
- [x] Portrait lock

## Build execution status

Not executed in this environment:

- `./gradlew test`
- `./gradlew assembleDebug`
- `./gradlew connectedAndroidTest`
- Emulator launch / manual interaction check

Reason: this runtime has Java but does not contain the Android SDK, adb, sdkmanager, a Gradle executable, or an emulator.

## Required local final commands

After opening/syncing the project in Android Studio with JDK 17 and Android SDK 36 installed:

```bash
./gradlew test
./gradlew assembleDebug
./gradlew connectedAndroidTest
```

Then install/run the debug build on an emulator or Android device and manually verify:

1. Add a chunk.
2. Add with blank examples and blank Group.
3. Search and Group filter.
4. Edit and Delete.
5. Review All and a selected group.
6. Reveal examples.
7. Special / Next / Mastered.
8. Review completion and Review Again.
9. Restart app and confirm persisted data.
10. Confirm airplane/offline use works.

## APK location

After a successful debug build:

```text
app/build/outputs/apk/debug/app-debug.apk
```
