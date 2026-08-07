# Coconut Chunks 1.3.0-rc1 — Release Checklist

## Automated gates
- [ ] `gradle testDebugUnitTest`
- [ ] `gradle lintDebug`
- [ ] `gradle assembleDebug`
- [ ] GitHub Actions uploads the debug APK
- [ ] Room schema JSON appears under `app/schemas/`
- [ ] No `INTERNET` permission is present in the merged manifest

## Manual device smoke test
- [ ] Fresh install opens to Library without a crash
- [ ] Add a chunk with only one example
- [ ] Edit that chunk and save
- [ ] Search by chunk text, example, and note
- [ ] Create, rename, and delete a group
- [ ] Confirm deleting a group moves its chunks
- [ ] Start Review and verify examples are hidden before reveal
- [ ] Mark Special, tap Undo, and verify the previous status returns
- [ ] Mark Mastered, tap Undo, and verify the previous status returns
- [ ] Swipe right after reveal and verify Mastered
- [ ] Complete a Daily Review
- [ ] Complete an Entire Group review with no repeats
- [ ] Export JSON backup
- [ ] Export CSV
- [ ] Import JSON backup and verify data is restored
- [ ] Force-stop and reopen the app; data remains present

## Accessibility / large text
- [ ] Test Android font size at 200%
- [ ] Test display size at the largest comfortable setting
- [ ] Bottom navigation remains understandable
- [ ] Primary buttons are not clipped
- [ ] Add/Edit fields remain scrollable
- [ ] Review actions remain reachable after reveal
- [ ] TalkBack announces navigation destinations
- [ ] TalkBack announces review status
- [ ] Icon-only Settings / Back / Group actions have useful labels
- [ ] Decorative icons do not create noisy announcements

## Privacy
- [ ] Airplane-mode test passes
- [ ] No login/account UI
- [ ] No analytics/tracker SDK
- [ ] No ads
- [ ] Backup files are only created when the user explicitly exports/imports

## Release decision
Promote RC to 1.3.0 only after all automated gates pass and the manual smoke test succeeds on at least one real Android device.
