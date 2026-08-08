# Coconut Chunks V1 — Signing and Release

The release signing key is intentionally not stored in this repository.

## Permanent release identity

- Application ID: `com.coconutchunks.app`
- Key alias: `coconut-chunks-release`
- Certificate SHA-256:
  `CB:D5:28:63:AE:9F:22:A1:F1:CB:6D:C8:46:22:0D:66:46:2D:FF:0C:0A:8D:DB:6D:76:BD:41:6E:BD:37:99:7C`
- Valid until: 2053-12-24

Every long-term release APK must use this same key.

## Environment variables expected by Gradle

When building a signed release, provide:

- `COCONUT_KEYSTORE_PATH`
- `COCONUT_KEYSTORE_PASSWORD`
- `COCONUT_KEY_PASSWORD`

The alias is fixed in the build configuration as `coconut-chunks-release`.

The keystore and passwords must never be committed to Git.
