# Coconut Chunks — 1.3.0

A personal, offline-first Android app for collecting and reviewing German language chunks.

## Stack

- Kotlin
- Jetpack Compose + Material 3
- Room 2.8.4
- DataStore 1.2.1
- No server, account, analytics, ads, trackers, or internet permission

## Main features

- Add/edit/delete chunks with up to three personal examples and notes
- Groups with safe move-on-delete behavior
- Library search across chunk text, examples, and notes
- Sort and status filtering
- Recall-first review cards with reveal stage
- Special / Mastered / Next actions
- Optional right swipe to mark Mastered
- Weighted review priority (Special 5, Review 2, Mastered configurable; default 0.5)
- Complete-group review without repeats
- Daily Review with configurable target
- Overview counts
- JSON backup/restore with internal safety backup
- CSV export
- Adaptive coconut app icon
- GitHub Actions debug APK build

## Review selection

Normal and Daily review use weighted sampling **without replacement**. Each candidate receives an exponential-race key based on its status weight, then the lowest keys are selected. This makes Special chunks much more likely to appear while still keeping Mastered chunks eligible, and avoids immediate duplicates inside a session.

Complete Group Review ignores weights and shuffles every chunk in the selected group exactly once.

## Database

`groups`
- id
- name
- createdAt

`chunks`
- id
- chunkText
- example1/2/3
- groupId
- status
- note
- createdAt
- editedAt
- lastReviewedAt
- totalReviewCount

Foreign-key deletion uses `SET NULL` as a final safety net. Normal group deletion explicitly moves chunks to the selected destination first.

## Search

Room executes the search locally with `LIKE` across chunk text, all three examples, and note. Group/status filters and sort order are also applied in SQL.

## Backup

JSON backup preserves groups, chunk IDs, examples, statuses, notes, and review metadata. Before import replaces the database, the app writes a safety JSON backup into internal app storage.

CSV export contains:
Chunk, Example 1, Example 2, Example 3, Group, Status, Note.

## Open in Android Studio

1. Open Android Studio.
2. Choose **Open** and select the `CoconutChunks` folder.
3. Use JDK 17.
4. Let Gradle sync and install Android SDK 36 if prompted.
5. Run the `app` configuration on an emulator or Android phone (Android 6.0 / API 23+).

The repository includes `gradle-wrapper.properties` but not the binary wrapper JAR. Android Studio can use its configured Gradle installation, or run `gradle wrapper --gradle-version 8.13` once to generate the wrapper files.

## Build APK locally

With Gradle 8.13 installed:

```bash
gradle assembleDebug
```

APK:

```text
app/build/outputs/apk/debug/app-debug.apk
```

After generating the Gradle wrapper, you can instead run:

```bash
./gradlew assembleDebug
```

## GitHub Actions

Push the project to GitHub. The workflow `.github/workflows/android.yml`:

1. checks out the repository;
2. installs JDK 17;
3. installs Android SDK 36;
4. configures Gradle 8.13;
5. runs unit tests;
6. builds the debug APK;
7. uploads `app-debug.apk` as the `coconut-chunks-debug-apk` artifact.

Find it under **GitHub → Actions → latest successful run → Artifacts**.

## Install on a phone

1. Build/download `app-debug.apk`.
2. Copy it to the Android phone.
3. Open the APK.
4. Android may ask you to allow installation from that file-manager/browser source.
5. Install Coconut Chunks.

No internet permission is declared, so all app content remains local.

## Tests included

Local unit tests cover weighted selection, non-repetition, daily target, complete-group coverage, and continued Mastered eligibility.

Instrumented repository tests cover:
- add with incomplete examples;
- edit/delete;
- group deletion without chunk deletion;
- search by chunk/example/note;
- review status + metadata updates;
- a 10,000-chunk query scenario.

UI gesture/card reveal behavior is implemented in Compose and is ready for UI instrumentation expansion.


## Release Candidate verification

Round 4 adds Android lint to CI, Room schema export configuration, startup/persistence tests, accessibility semantics, and a manual release checklist.

For a release candidate build:

```bash
gradle testDebugUnitTest
gradle lintDebug
gradle assembleDebug
```

Then complete `RELEASE_CHECKLIST.md` on a real Android phone before treating the build as stable.


## Stable release package

Version: `1.3.0`  
Version code: `6`

Release preparation files:
- `RELEASE_NOTES_1.3.0.md`
- `SIGNING_AND_RELEASE.md`
- `FINAL_RELEASE_CHECKLIST.md`

The repository is configured for environment-variable-based signing so release secrets never need to be committed.
