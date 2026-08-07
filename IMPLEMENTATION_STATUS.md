# Coconut Chunks 1.3.0-rc2 — Release Candidate Status

The feature set is frozen for RC1. Round 4 focuses on verification and release readiness.

## Automated coverage
- Weighted selection behavior and no duplicates
- Complete-group coverage
- Daily target behavior
- CRUD with incomplete examples
- Group deletion without chunk loss
- Search across chunk/examples/note
- Review metadata updates
- Exact Review Undo
- 10,000 generated chunk scenario
- Database close/reopen persistence
- Compose UI add chunk
- Compose UI note search
- Compose UI recall/reveal
- Compose UI create group
- Compose UI startup/navigation smoke

## CI release gates
- Unit tests
- Android lint
- Debug APK assembly
- APK artifact upload
- Verification report upload
- Room schema export artifact

## Manual gates still required
- Real Android device installation
- TalkBack
- 200% font size
- Backup/import round trip
- Group delete/move flow
- Review Undo and swipe gesture
- Force-stop/reopen persistence

No stable-release claim should be made until the CI build and real-device checklist pass.

## Round 5 repair pass
- Hardened Ungrouped invariants.
- Made duplicate group creation idempotent.
- Stabilized dialog UI tests.
- Added regression tests for safe fallback group behavior.
