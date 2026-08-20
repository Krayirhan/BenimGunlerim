# Evidence Index

| ID | Level | Check | Result / limitation |
|---|---|---|---|
| E-01 | E3_STATIC | README, Gradle manifests, source inventory | Android Kotlin/Compose, Room, DataStore, Hilt and test topology confirmed. |
| E-02 | E3_STATIC | AndroidManifest + backup XML | Explicit receivers/export flags and local backup inclusion confirmed. |
| E-03 | E3_STATIC | Repositories/use cases/services/source search | Local-first task/routine/progression/import/export flow confirmed. |
| E-04 | E4_TOOL | `:app:assembleDebug` | PASSED (up-to-date tasks accepted by Gradle). |
| E-05 | E2_TEST | `:app:testDebugUnitTest` + XML reports | 408 tests, 0 failures, 0 errors, 2 skipped. |
| E-06 | E4_TOOL | JaCoCo verification/report | PASSED; measured line 54.7%, branch 32.7%. |
| E-07 | E4_TOOL | `graphify query ...` | Topology inspected; graph freshness limited by pre-existing changes. |
| E-08 | E4_TOOL | `:app:detekt` | PASSED in combined gate; custom rules configured. |
| E-09 | E4_TOOL | `:app:ktlintCheck` | FAILED: widespread test-source formatting violations. |
| E-10 | E4_TOOL | `:app:lintDebug` | 0 errors, 434 warnings; notable AGP/compileSdk compatibility warning. |
| E-11 | E4_TOOL | `:app:assembleRelease` | PASSED; minified release APK produced. |
| E-12 | E3_STATIC | safe secret metadata checks | local signing/local properties ignored and untracked; no raw values accessed. |

Raw command output is retained under `outputs/`. Android instrumentation/device flows, fresh macrobenchmarks, store-console setup and external CVE scanning were NOT EXECUTED.
