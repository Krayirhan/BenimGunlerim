# Test Stratejisi

Bu doküman production öncesi kalite kapısını risk odaklı hale getirmek için güncel test stratejisini tanımlar.

## Kısa Vadeli Hedefler

- JaCoCo gate tek bir genel oran yerine ayrı `LINE` ve `BRANCH` kurallarıyla çalışmalıdır.
- Mevcut enforce edilen baseline `LINE >= 0.60` ve `BRANCH >= 0.40` seviyesindedir.
- Sonraki hedef `LINE >= 0.70` ve `BRANCH >= 0.50` eşiğine çıkmaktır.
- Import, migration, repository side-effect ve ViewModel state testleri release blocker kabul edilir.
- Her başarısız CI koşusunda unit test, coverage, lint, connected test ve logcat artifact'leri saklanır.

## PR Gate

- `testDebugUnitTest`
- `jacocoDebugUnitTestCoverageVerification`
- `lintDebug`
- `assembleDebug`
- `dependency-review`
- `secret-scan`

## Release Gate

- `verifyReleaseSigning`
- `testDebugUnitTest`
- `jacocoDebugUnitTestCoverageVerification`
- `lintRelease`
- `assembleRelease`
- `bundleRelease`
- `connectedDebugAndroidTest`

## Nightly veya Manual Deep Gate

- migration matrix
- large-data import/export tests
- startup/perf regression checks
- notification matrix
- release smoke with logcat review

## Risk Odakli Test Alanlari

- Import: invalid version, invalid date/time, duplicate id, dangling references, size limit, rollback expectation
- Repository: transaction consistency, reminder cancel/schedule side effects, clear local data, cascade behavior
- ViewModel: initial/loading/empty/success/error/action transitions
- Notifications: quiet hours, permission denied, boot restore, timezone/date edges
- Migration: her yayinlanmis schema versiyonundan current schema'ya gecis

## Flaky Test Politikasi

- Retry varsayilan cozum degildir.
- Flaky test goruldugunde logcat, test raporu ve ilgili screenshot artifact'i incelenir.
- Kök neden bulunmadan release gate'ten muafiyet verilmez.