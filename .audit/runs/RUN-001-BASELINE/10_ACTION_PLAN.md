# Action Plan

## ACT-TEST-001

Status: PROPOSED. Priority: QUALITY. Source finding: TEST-001. Effort: M.

Goal: restore a green KtLint verification gate. Scope: style/import cleanup in the reported test and Android-test source files only; no behavior changes. Out of scope: production refactors or broad formatter configuration changes. Acceptance: `:app:ktlintCheck` passes, followed by `:app:testDebugUnitTest`. Regression risk: low, but test compilation/execution must be retained.

## ACT-DEP-001

Status: PROPOSED. Priority: RELEASE. Source finding: DEP-001. Effort: M.

Goal: bring AGP/target SDK support into an explicitly supported state. Scope: planned toolchain compatibility update or a documented support decision. Out of scope: opportunistic library mass upgrades. Acceptance: no unsupported compile-SDK warning and all build/test/lint/release gates pass. Regression risks: AGP/Kotlin/KSP/Hilt compatibility.
