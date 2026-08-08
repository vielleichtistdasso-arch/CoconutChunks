# Coconut Chunks V1 Test Matrix

| # | Requirement | Coverage |
|---|---|---|
| 1 | Add a chunk | Room instrumentation test |
| 2 | Add with no examples | Room instrumentation test |
| 3 | Add with no group | Room instrumentation test + Add normalization logic |
| 4 | Edit a chunk | Room instrumentation test + edit semantics JVM test |
| 5 | Delete a chunk | Room instrumentation test |
| 6 | Search a chunk | JVM LibraryFilter test + DAO search test |
| 7 | Filter by group | JVM LibraryFilter test |
| 8 | Start review with empty database | JVM ReviewPool test |
| 9 | Review one chunk | Review session logic implemented; UI/emulator verification pending |
| 10 | Reveal examples | JVM nonEmptyExamples test; UI/emulator verification pending |
| 11 | Mark Special | Review ViewModel persists immediately; UI/emulator verification pending |
| 12 | Mark Mastered | Review ViewModel persists immediately; UI/emulator verification pending |
| 13 | Press Next without status change | Review ViewModel path implemented; UI/emulator verification pending |
| 14 | Finish a review session | Review completion state implemented; UI/emulator verification pending |
| 15 | Restart app and saved data persists | File-backed Room instrumentation reopen test |
| 16 | SPECIAL appears twice | JVM ReviewPool test |
| 17 | MASTERED excluded by default | JVM ReviewPool test |
| 18 | MASTERED included when enabled | JVM ReviewPool test |

Notes:

- Pure logic is intentionally covered with JVM tests where possible.
- Room persistence/CRUD uses instrumentation tests.
- Final end-to-end interaction tests require Android SDK + emulator, which are not available in the current execution environment.
