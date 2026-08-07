# Coconut Chunks 1.3.0 — Stable Candidate Status

The source package is prepared as version `1.3.0` / versionCode `6`.

What is complete:
- feature set frozen;
- offline/privacy architecture retained;
- release signing configuration added;
- unsigned Release APK/AAB are strict CI build gates;
- signed builds use environment variables only;
- unit-test, lint, debug APK, release APK, and release AAB tasks are wired into CI;
- final release notes and installation/signing documentation are included;
- signing material is excluded by `.gitignore`.

What is intentionally not claimed:
- this environment did not execute Gradle;
- no APK/AAB from this package has been installed on a real phone here;
- TalkBack and 200% font-scale verification still require a real Android device.

Promote this source package to a fully verified stable release only after the checks in `FINAL_RELEASE_CHECKLIST.md` pass.
