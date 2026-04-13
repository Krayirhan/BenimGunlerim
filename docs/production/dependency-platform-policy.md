# Dependency ve Platform Sağlığı Politikası

Bu doküman dependency güncelleme ve platform sürdürülebilirlik kurallarını tanımlar.

## Otomasyon

- Dependabot aktif olmalıdır (`.github/dependabot.yml`).
- PR'da dependency review job başarılı olmalıdır.
- Secret scan job başarılı olmalıdır.

## Sürümleme Kuralı

- Kotlin ve KSP lockstep güncellenir.
- Compose BOM güncellemeleri smoke testlerle doğrulanır.
- AGP/Gradle güncellemeleri ayrı branch/PR ile yapılır.

## Cadence

- Haftalık: Gradle ve GitHub Actions dependency PR review.
- Aylık: Compose BOM ve AndroidX grup güncellemesi.
- Çeyreklik: AGP, Gradle Wrapper, Kotlin/KSP review.

## Zorunlu Kontroller

- `testDebugUnitTest`
- `jacocoDebugUnitTestCoverageVerification`
- `lintDebug` veya `lintRelease`
- `assembleDebug` veya `assembleRelease`
- Kritik değişikliklerde `connectedDebugAndroidTest`

## targetSdk Politikası

- Play deadline beklenmeden upgrade branch açılır.
- Notification/background/permission davranışları manuel smoke test edilir.
- Release öncesi en az bir fiziksel cihazda doğrulama yapılır.

## Takip Edilecek Borçlar

- Deprecated API uyarıları backlog'a alınır.
- Güvenlik advisory geldiğinde SLA ile yanıtlanır.
- Büyük versiyon geçişleri (AGP/Kotlin) release notunda işaretlenir.