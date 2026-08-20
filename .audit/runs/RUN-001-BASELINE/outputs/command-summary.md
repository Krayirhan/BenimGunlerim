# RUN-001 Command Output Summary

Complete raw Gradle streams:

- `gradle-quality-gates.log` — `:app:assembleDebug :app:testDebugUnitTest :app:detekt :app:ktlintCheck :app:lintDebug :app:jacocoDebugUnitTestCoverageVerification`; exits non-zero because KtLint fails.
- `gradle-lint.log` — `:app:lintDebug`; report confirms 0 errors and 434 warnings.
- `gradle-release-build.log` — `:app:assembleRelease`; BUILD SUCCESSFUL.

Read-only evidence commands executed: source/config inventory; safe dependency/tool discovery; Graphify query; manifest/backup inspection; Room/DataStore/use-case/coroutine/accessibility searches; test XML and JaCoCo report aggregation; device discovery; ignored/untracked secret-config metadata checks. No environment dump, credential file content, raw secret, network vulnerability scan, or source modification was performed.
