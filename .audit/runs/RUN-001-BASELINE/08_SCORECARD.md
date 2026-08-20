# Scorecard

| Domain | Weight | Score /10 | Weighted | Confidence | Main evidence |
|---|---:|---:|---:|---|---|
| Core product correctness | 18 | 8.2 | 14.8 | HIGH | E-03, E-05 |
| Data integrity & persistence | 17 | 8.0 | 13.6 | HIGH | E-02, E-03, E-05 |
| Lifecycle & reliability | 12 | 7.4 | 8.9 | MEDIUM | E-02, E-03, E-05 |
| UX & accessibility | 10 | 7.0 | 7.0 | MEDIUM | E-01, E-10; no fresh device review |
| Architecture & maintainability | 10 | 7.8 | 7.8 | HIGH | E-01, E-03, E-07, E-08 |
| Testing & verification | 12 | 7.0 | 8.4 | HIGH | E-05, E-06, E-09 |
| Security & privacy | 9 | 7.8 | 7.0 | MEDIUM | E-02, E-12 |
| Performance & resource use | 5 | 7.0 | 3.5 | MEDIUM | E-01; fresh benchmark not run |
| Dependency health | 3 | 6.0 | 1.8 | HIGH | E-10 |
| Release engineering | 4 | 7.0 | 2.8 | HIGH | E-09, E-10, E-11 |

Overall: **75.6 / 100** (all weighted domains applicable; rounded: **75.6**).

Scores are independent from release verdict. The main deductions are the failing configured format gate, toolchain compatibility warning, and non-executed device/release-external verification—not missing backend infrastructure.
