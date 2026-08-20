# Tool and Graph Analysis

| Capability | Status | Result |
|---|---|---|
| Gradle build/test | AVAILABLE_CONFIGURED | Debug and release builds; JVM tests and JaCoCo executed. |
| Detekt / custom rules | AVAILABLE_CONFIGURED | Executed in combined gate without reported failure. |
| KtLint | AVAILABLE_CONFIGURED | Executed; failed on test-source formatting. |
| Android Lint | AVAILABLE_CONFIGURED | 0 errors, 434 warnings. |
| JaCoCo | AVAILABLE_CONFIGURED | Gate executed; 54.7% lines, 32.7% branches in measured scope. |
| Macrobenchmark | AVAILABLE_CONFIGURED | Module and prior device artifacts exist; not re-run. |
| Graphify | AVAILABLE_CONFIGURED | Existing graph queried; graph itself has pre-existing working-tree modifications. |
| Dedicated vulnerability / secret scanner | UNAVAILABLE | No safe local configured scanner found; metadata-only secret checks used. |

Graph observation (E-07): `TaskEntity`, `RoutineEntity` and `UserPreferences` are high-connection bridges. Source topology validates this as expected central local-domain state, not an architecture defect. `TodayViewModel` is a notable composition node, but it delegates to collaborators and no failure evidence justifies a finding.

Graph limitation: output references documents currently deleted from the working tree, so it is useful for topology only and not treated as current documentation truth.
