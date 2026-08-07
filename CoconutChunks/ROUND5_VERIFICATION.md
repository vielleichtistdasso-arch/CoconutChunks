# Round 5 Verification — 1.3.0-rc2

This round is a release-candidate repair pass rather than a feature pass.

## Reliability fixes
- `Ungrouped` is guaranteed before chunk writes.
- New chunks with no selected group receive the real `Ungrouped` group ID instead of relying on a null/display fallback.
- Deleting a group with no explicit destination falls back to the real `Ungrouped` group.
- Group creation is case-insensitively idempotent.
- Reserved/duplicate group input no longer produces uncaught coroutine exceptions.
- Generic text dialogs now expose a stable Compose test tag.

## Regression coverage added
- Saving a chunk establishes the `Ungrouped` invariant.
- Deleting a group without an explicit target still moves chunks safely.
- Duplicate group names are idempotent case-insensitively.
- Existing persistence, Undo, search, CRUD, 10k-chunk, review-selection, and Compose UI tests remain.

## Release state
This package is `1.3.0-rc2`, not the stable release. Stable promotion still requires:
1. real Gradle/Android SDK compilation;
2. Android lint;
3. automated test pass;
4. installation and smoke test on a real Android device;
5. accessibility checklist completion.
