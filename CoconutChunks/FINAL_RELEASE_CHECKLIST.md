# Coconut Chunks 1.3.0 — Final Release Checklist

## Build
- [ ] `gradle testDebugUnitTest`
- [ ] `gradle lintDebug`
- [ ] `gradle assembleDebug`
- [ ] `gradle assembleRelease`
- [ ] `gradle bundleRelease`
- [ ] `apksigner verify --verbose app-release.apk`
- [ ] Room schema JSON is present under `app/schemas/`

## Functional smoke test
- [ ] Fresh install opens successfully
- [ ] Add chunk with 1 example
- [ ] Add chunk with 3 examples
- [ ] Edit and delete chunk
- [ ] Search chunk text
- [ ] Search example text
- [ ] Search note
- [ ] Create/rename/delete group
- [ ] Deleted group moves chunks safely
- [ ] Review hides examples before reveal
- [ ] Special works
- [ ] Mastered works
- [ ] Keep-status works
- [ ] Undo restores status metadata
- [ ] Swipe-right Mastered works
- [ ] Entire Group Review has no repeats
- [ ] Daily Review target works
- [ ] JSON export works
- [ ] JSON import restores data
- [ ] CSV export works
- [ ] Force-stop/reopen preserves data

## Accessibility
- [ ] TalkBack navigation checked
- [ ] 200% font size checked
- [ ] Large display size checked
- [ ] Buttons not clipped
- [ ] Add/Edit remains scrollable
- [ ] Review actions remain reachable

## Privacy
- [ ] Merged manifest has no INTERNET permission
- [ ] No analytics SDK
- [ ] No advertising SDK
- [ ] No tracker SDK
- [ ] App works in airplane mode

## Release
- [ ] Version name is `1.3.0`
- [ ] Version code is `6`
- [ ] Release notes reviewed
- [ ] Keystore backup stored safely
- [ ] Stable APK installed on at least one real phone
- [ ] Stable AAB archived
