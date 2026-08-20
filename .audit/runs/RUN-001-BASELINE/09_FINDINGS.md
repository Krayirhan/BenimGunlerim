# Findings

| ID | Severity | Domain | Status | Short issue | User impact |
|---|---|---|---|---|---|
| TEST-001 | P2 | Testing & verification | OPEN | Configured KtLint gate fails across test sources. | CI/release verification is not green. |
| DEP-001 | P2 | Dependency health | OPEN | AGP 8.7.3 is outside its stated compileSdk 36 tested range. | Future Android/toolchain compatibility risk. |

## TEST-001 — KtLint verification gate fails

Evidence: E-09. Confidence: HIGH. Affected components: `app/src/test` and `app/src/androidTest`. KtLint reports import ordering, unused imports, multiline formatting and related style violations across numerous test files. This does not demonstrate an app-flow defect, but a configured quality gate cannot currently pass; it weakens trustworthy automated release verification.

Acceptance: `:app:ktlintCheck` exits 0 without automatic formatting outside approved scope. Verification: run the command in CI/local clean checkout. First/last verified: RUN-001-BASELINE.

## DEP-001 — Android Gradle Plugin / compile SDK support mismatch

Evidence: E-10. Confidence: HIGH. Affected files: `gradle/libs.versions.toml`, build configuration. Android Gradle Plugin 8.7.3 reports testing only through compileSdk 35 while the app compiles/targets 36. Both debug and release builds pass, so this is a compatibility-risk finding rather than a build blocker.

Acceptance: use a toolchain version validated for target SDK 36, or document a tested, supported compatibility decision. Verification: clean debug/release build, lint and test gates without unsupported-compile-SDK warning. First/last verified: RUN-001-BASELINE.
