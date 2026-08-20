# Scope and Risk — RUN-001-BASELINE

Aggregate class: R2 — consumer local-persistence app.

| Dimension | Rating | Rationale |
|---|---|---|
| Data loss | MEDIUM | Room/DataStore hold user-created work and history. |
| Privacy | MEDIUM | Personal productivity data is retained and included in Android backup. |
| Auth / authorization | N/A | No account, backend or tenant boundary found. |
| Financial / regulatory | N/A | No payment or regulated workflow found. |
| Network attack surface | LOW | INTERNET permission exists; no API client/server integration identified. |
| Availability | LOW | Local-first app; reminders are platform-dependent. |
| Consistency | MEDIUM | Transactions, daily close and progression affect related local records. |
| External dependency | MEDIUM | Android/Gradle/Compose/Room dependency chain. |
| Device lifecycle | MEDIUM | Notification permissions, boot and time changes are relevant. |
| Release/reputation | MEDIUM | Consumer release with privacy and correctness expectations. |

Excluded as N/A: networking/API behavior, authentication, authorization/tenant isolation, sync/conflict handling, distributed observability, backend infrastructure.
