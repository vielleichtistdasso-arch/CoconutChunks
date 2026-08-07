# Changelog

## 1.3.0 — Round 6: Stable release preparation

### Release packaging
- Version name promoted to `1.3.0`.
- Version code promoted to `6`.
- Added environment-variable-based release signing.
- Keystores and signing-property files are gitignored.
- Added signed APK/AAB build instructions.
- Added final release notes and final device checklist.

### CI
- Debug unit tests remain a required gate.
- Android lint remains a required gate.
- Debug APK build remains a required gate.
- Unsigned Release APK build is now a required gate.
- Unsigned Release AAB build is now a required gate.
- Release failures are no longer swallowed.

### Release state
The source tree is a stable candidate. A verified stable binary still requires the included CI and real-device gates to pass.

## 1.3.0-rc2 — Round 5: RC verification and repair

### Fixed
- Guaranteed the real `Ungrouped` group exists before chunk writes.
- New ungrouped chunks now store the actual group ID.
- Group deletion with a missing destination safely falls back to `Ungrouped`.
- Duplicate group names are handled case-insensitively and idempotently.
- User-entered reserved/duplicate group names no longer risk uncaught coroutine exceptions.
- Group dialog instrumentation uses a stable test tag instead of a broad text-field matcher.

### Regression tests
- Ungrouped invariant on save.
- Safe fallback on group deletion.
- Case-insensitive duplicate group creation.

### Release policy
RC2 remains a candidate until CI and real-device verification pass.

## 1.3.0-rc1 — Round 4: Release Candidate

### Verification
- CI now runs unit tests, Android lint, and debug APK assembly.
- CI uploads verification reports as artifacts.
- Added app-start/navigation smoke coverage.
- Added disk database reopen/persistence coverage.

### Database lifecycle
- Configured Room schema export to `app/schemas/`.
- Kept database schema at version 1; no unnecessary migration was introduced.
- Added release guidance to commit generated schema JSON before future schema changes.

### Accessibility
- Added semantic heading information to the main app title.
- Added semantic Review-status descriptions.
- Improved labels for important icon-only actions.
- Preserved button alternatives for all swipe behavior.
- Added a real-device TalkBack / 200% font-scale checklist.

### Release process
- Added `RELEASE_CHECKLIST.md`.
- Added `ACCESSIBILITY_NOTES.md`.
- Version bumped to `1.3.0-rc1`, versionCode 4.

### Architecture
- Still fully offline.
- No INTERNET permission.
- No account, server, ads, analytics, or trackers.
