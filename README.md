# Coconut Chunks

Minimal native Android app for collecting and reviewing German language chunks.

## Round 2 status

Implemented:

- Single Android app module
- Kotlin + Jetpack Compose + Material 3 shell
- Portrait orientation
- Home screen with four large buttons
- No INTERNET permission
- Room 2.8.4 with KSP
- One `chunks` table only
- `ChunkEntity`, `ChunkStatus`, explicit enum converter
- DAO for insert/update/delete/read/search/groups/count
- Thin `ChunkRepository`
- Room database version 1
- Android instrumentation tests for core Room behavior and Unicode

Not implemented yet:

- Navigation routes
- Add / Edit UI
- Library UI
- Review pool / session
- Settings data count UI
- Compose UI tests

## Toolchain

- JDK 17
- Android Gradle Plugin 8.13.2
- Gradle 8.13
- Kotlin 2.2.21
- Compose BOM 2026.06.00
- Activity Compose 1.12.4
- Room 2.8.4
- KSP 2.2.21-2.0.5
- compileSdk 36
- targetSdk 36
- minSdk 26

## Important environment note

This generated archive intentionally does not contain `gradle-wrapper.jar`.
Open it in Android Studio and let Android Studio sync the project, or generate
the wrapper locally with a compatible Gradle installation.

The current generation environment has Java but no Android SDK, adb, sdkmanager,
or system Gradle installation, so an actual Android build cannot be executed here.


## Database

Database file: `coconut_chunks.db`

Schema version: `1`

Only one table is used: `chunks`.

No destructive migration fallback is configured. Future schema changes must add
an explicit migration rather than silently deleting personal data.

## Round 2 verification

The Room model and DAO are covered by instrumentation tests using an in-memory
database. The generation environment still has no Android SDK / adb / emulator,
so these tests and `assembleDebug` cannot be executed here. Run them from an
Android SDK-enabled machine with:

```bash
./gradlew assembleDebug
./gradlew connectedAndroidTest
```


## Round 3 status

Implemented:

- Navigation Compose
- Home route
- Add route
- Library route
- Edit route with Long `chunkId` argument
- Review route
- Settings route
- Explicit Back buttons that call `popBackStack()`
- Normal system Back behavior remains handled by Navigation Compose
- Placeholder screen content only; no CRUD or review business logic yet

Route map:

```text
home
├── add
├── library
│   └── edit/{chunkId}
├── review
└── settings
```

The temporary "Open edit route (test)" button on Library exists only to validate
the `edit/{chunkId}` route before real Library rows are implemented.


## Round 4 status

Implemented the complete Add Chunk write path:

```text
AddChunkScreen
    ↓
AddChunkViewModel
    ↓
ChunkRepository
    ↓
ChunkDao
    ↓
Room
```

Behavior:

- `Chunk` is required; Save is disabled while blank.
- Empty examples are stored as empty strings.
- Empty Group is normalized to `Ungrouped`.
- Group can be typed freely.
- Existing groups can be selected from a small dropdown.
- German Unicode text is handled as normal Kotlin/SQLite text.
- Save is disabled while an insert is running, preventing double-save duplicates.
- Successful save pops the current navigation destination.
- Cancel pops without saving.
- No Snackbar is used in V1.
- No Library/Edit/Review business logic was added in this round.


## Round 5 status

Implemented Library:

- Observes Room through `ChunkRepository.observeAll()` and `observeGroups()`.
- Search matches `chunkText` only and is case-insensitive.
- Group filter contains `All`, `Ungrouped`, and existing groups.
- Search and group filtering are small pure Kotlin functions.
- Library rows show only Chunk text, Group, and Status.
- Example sentences are not shown in the list.
- `LazyColumn` uses `key = { it.id }`.
- Tapping a real chunk navigates to `edit/{chunkId}`.
- Empty database and empty search results are handled explicitly.
- Added JVM unit tests for search and group filtering.
- Edit remains a placeholder screen until the next round.


## Round 6 status

Implemented Edit / Delete:

- `EditChunkViewModel` observes the chunk by its real Room `id`.
- Chunk, Example 1-3, Group, and Status are editable.
- Existing groups can still be selected, or Group can be typed manually.
- Empty Group is normalized to `Ungrouped`.
- Save uses `repository.update(...)` on the original entity copy.
- `id` and `createdAt` are preserved; `updatedAt` changes.
- Save is guarded against blank Chunk and repeated taps.
- Delete uses a simple confirmation dialog:
  - `Delete this chunk?`
  - `Cancel`
  - `Delete`
- Delete uses the original Room entity and returns after success.
- Missing/deleted chunks show `Chunk not found.` instead of crashing.
- Save/delete failures stay on-screen with a small error message.
- Review logic is still untouched.


## Round 7 status

Implemented the Review Pool as pure Kotlin logic only.

Rules:

```text
REVIEW   -> 1 copy
SPECIAL  -> 2 copies
MASTERED -> 1 copy only when Include mastered is enabled
```

Selection:

- `All` includes eligible chunks from every group.
- A specific group includes only chunks whose `groupName` matches exactly.
- MASTERED is excluded by default.
- The weighted list is shuffled once when the pool is built.
- There is no repeated database querying for each next card.
- There is no per-card random selection.
- There is no SPECIAL adjacency correction in V1.
- The input list is never mutated.

The builder accepts a `Random` parameter only to make JVM tests deterministic.
Production code will use `Random.Default`.

Added JVM unit tests for:
- empty database
- REVIEW appears once
- SPECIAL appears twice
- MASTERED excluded by default
- MASTERED included when enabled
- group selection
- Review All
- exact weighting
- non-mutating input


## Round 8 status

Implemented Review UI and Review Session ViewModel.

Setup:
- `All` or one existing group.
- `Include mastered chunks` checkbox, default off.
- `Start Review`.
- Empty eligible pool shows `No chunks available for review.`

Session:
- The current Room list is captured when Start Review builds the pool.
- Pool construction uses the pure Round 7 builder and shuffles once.
- Cards do not query Room for each next item.
- Card initially shows Chunk, Group, Status, and `Tap to reveal examples`.
- Clicking the card reveals only non-empty examples.
- After reveal, exactly three controls appear:
  - `Special`
  - `Next`
  - `Mastered`
- `Special` and `Mastered` persist to Room before advancing.
- `Next` advances without changing status.
- Status updates also refresh any later duplicate of the same SPECIAL chunk inside the current session.
- A failed status write stays on the same card and shows `Could not update status.`

Completion:
- `Review complete`
- `Reviewed: X`
- `Marked Special: X`
- `Marked Mastered: X`
- `Finish`
- `Review Again`
- Review Again rebuilds and reshuffles the same current selection from latest local data.

No swipe gestures, long press, animations, background work, or internet features were added.


## Round 9 status

Implemented Basic Settings:

- `About Coconut Chunks`
- A short offline/private description
- Live `Database item count`
- No reset button in the simplified V1 plan
- No extra settings

Testing cleanup:

- Added a file-backed Room reopen test for persistence across database reopen.
- Added `TEST_MATRIX.md` mapping all original 18 requested tests to current coverage.
- Pure logic stays in JVM tests.
- Room CRUD/persistence stays in instrumentation tests.
- End-to-end Compose/emulator verification remains pending because this environment has no Android SDK/emulator.


## Round 10 final audit

Final static audit changes:

- Review session startup now performs an explicit one-shot Room read with `getAllOnce()`.
- The review pool is built from that one snapshot, then the session advances only by index.
- Review Again performs one new snapshot read and reshuffle.
- This removes the small race where a cached Flow value could still be empty when Start Review is tapped immediately.
- Review setup disables controls while the one-shot load is running.
- Removed the remaining force-unwrap pattern from instrumentation tests.
- Added a DAO test for `getAllOnce()` snapshot ordering.

Environment limitation remains:

- Java is available.
- Android SDK, adb, sdkmanager, Gradle executable, and emulator are not available.
- Therefore `assembleDebug`, `test`, and `connectedAndroidTest` cannot be truthfully executed in this environment.
- The project also cannot include a genuine `gradle-wrapper.jar` because none is installed locally; no fake wrapper binary was created.


## GitHub CI / APK build

A GitHub Actions workflow is now included at:

```text
.github/workflows/android-ci.yml
```

It can build and test Coconut Chunks without Android Studio on the user's computer.

CI uses:
- JDK 17
- Android SDK 36
- Gradle 8.13 installed by GitHub Actions
- JVM unit tests
- debug APK build
- Android emulator instrumentation tests
- APK artifact upload

See `GITHUB_BUILD_GUIDE.md`.
