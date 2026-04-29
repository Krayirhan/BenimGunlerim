# Kalite Kapıları

Bu doküman BenimGünlerim için zorunlu production kalite kapılarını tanımlar. Release adayı, aşağıdaki kapıların tamamı yeşil olmadan Play Store'a yüklenmez.

## Lokal Geliştirme Kapısı

Her PR öncesinde hızlı lokal kontrol:

```powershell
.\scripts\check-local.ps1
```

Bu script şu adımları çalıştırır:

- `testDebugUnitTest`
- `jacocoDebugUnitTestCoverageVerification`
- `lintDebug`
- `detekt`
- `assembleDebug`

## Release Kapısı

Release öncesi tam kontrol:

```powershell
.\scripts\check-release.ps1
```

Bu script şu adımları çalıştırır:

- `verifyReleaseSigning`
- `testDebugUnitTest`
- `jacocoDebugUnitTestCoverageVerification`
- `detekt`
- `lintRelease`
- `assembleRelease`
- `bundleRelease`

`verifyReleaseSigning` başarısızsa üretilen APK/AAB production artifact sayılmaz.

## Cihaz Test Kapısı

Release adayında fiziksel cihaz veya emülatör üzerinde:

```powershell
.\gradlew.bat connectedDebugAndroidTest
```

GitHub Actions içinde `connected-ui-tests` job'u aynı komutu emülatörde çalıştırır. Lokal release öncesinde ayrıca fiziksel cihaz smoke testi yapılır.

## CI Kapıları

| Tetikleyici | Zorunlu komutlar |
|---|---|
| Pull request | `testDebugUnitTest` + `jacocoDebugUnitTestCoverageVerification` + `detekt` + `lintDebug` + `assembleDebug` |
| `main` push | `verifyReleaseSigning` + `testDebugUnitTest` + `jacocoDebugUnitTestCoverageVerification` + `detekt` + `lintRelease` + `assembleRelease` + `bundleRelease` |
| `v*` release tag | `verifyReleaseSigning` + `testDebugUnitTest` + `jacocoDebugUnitTestCoverageVerification` + `detekt` + `lintRelease` + `assembleRelease` + `bundleRelease` + signed AAB artifact |
| Manual workflow | Release kapısı ile aynı |

`connected-ui-tests` job'u PR, `main`, `v*` tag ve manual workflow çalıştırmalarında `connectedDebugAndroidTest` kapısını yürütür.

## CI Secret Gereksinimleri

GitHub Actions release job için şu secret'lar tanımlı olmalıdır:

- `KEYSTORE_BASE64`: release keystore dosyasının base64 karşılığı
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Bu secret'lar eksikse `verifyReleaseSigning` fail eder ve release durur.

## Ortam Gereksinimleri

- JDK 17
- Android SDK 35
- Gradle Wrapper: `gradlew` / `gradlew.bat`
- Sistem Gradle kullanılmaz.

## Release Artifact Kuralı

Production için tek kabul edilen artifact:

- `app/build/outputs/bundle/release/app-release.aab`

`app-release-unsigned.apk` veya signing doğrulaması geçmemiş herhangi bir çıktı release artifact değildir.

## Coverage Kuralı

Unit test coverage kapısı JaCoCo ile çalışır:

```powershell
.\gradlew.bat jacocoDebugUnitTestCoverageVerification
```

Başlangıç eşiği `0.20` olarak belirlenmiştir. UI, generated Room/Hilt sınıfları ve Android giriş noktaları coverage hesabından hariç tutulur. Yeni domain/data testleri eklendikçe eşik kademeli olarak yükseltilmelidir.
Güncel eşik: `LINE >= 0.42` ve `BRANCH >= 0.22`.

## Dış Release Kontrolü

Repo dışı ayarları kontrol etmek için:

```powershell
.\scripts\check-external-release-readiness.ps1
```

Bu script lokal release signing değişkenlerini kontrol eder ve GitHub/Play Console tarafında manuel doğrulanması gereken adımları listeler.
