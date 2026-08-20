# Contextual Rubric v1.0 — LOCKED

Weights total 100. N/A domains are excluded, not penalized.

| Domain | Weight | Applicable subcriteria |
|---|---:|---|
| Core product correctness | 18 | task/routine/day-close/progression correctness; validation; export/import behavior |
| Data integrity & persistence | 17 | Room schema/migrations; transaction boundaries; backup scope; destructive operations |
| Lifecycle & reliability | 12 | Flow/coroutine ownership; process/device lifecycle; notification restoration; error paths |
| UX & accessibility | 10 | Turkish resources; navigation/state feedback; semantic descriptions; touch/layout evidence |
| Architecture & maintainability | 10 | UI/domain/data boundaries; dependency direction; shared design system; change isolation |
| Testing & verification | 12 | core unit tests; DB/migration/UI tests; coverage gate; executable suite health |
| Security & privacy | 9 | secret handling; component exposure; backup/privacy controls; local data handling |
| Performance & resource use | 5 | Compose/resource patterns; baseline profile; benchmark capability |
| Dependency health | 3 | version catalog; build compatibility; supply-chain evidence |
| Release engineering | 4 | release/minification/signing gate; lint/static gates; CI/release evidence |

Severity ceilings: user data loss/privacy exposure may be P0/P1; broken core flow P1; quality/test/tooling gaps normally P2; cosmetic warnings P3/P4. Scores require E-01..E-12.
