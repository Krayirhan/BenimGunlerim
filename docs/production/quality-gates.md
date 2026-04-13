# Kalite Kapıları

Bu dokümanda BenimGünlerim projesinin production kalite kapıları açıklanmaktadır.

## Zorunlu Kapılar

Her PR ve release öncesinde aşağıdaki komutların tümü yeşil olmalıdır:

```powershell
.\gradlew.bat testDebugUnitTest   # Unit testler
.\gradlew.bat lintDebug           # Lint (debug)
.\gradlew.bat lintRelease         # Lint (release)
.\gradlew.bat assembleRelease     # Release build
.\gradlew.bat bundleRelease       # AAB (Play Store)
.\gradlew.bat connectedDebugAndroidTest  # UI testler (emülatör gerekli)
```

## Hızlı Komutlar

```powershell
# Lokal geliştirme kapısı (hızlı)
.\scripts\check-local.ps1

# Release öncesi tam kapi
.\scripts\check-release.ps1

# Build temizliği
.\scripts\clean-build-artifacts.ps1
```

## Geliştirme Ortamı

- JDK 17 gereklidir
- Android SDK 35 (targetSdk)
- Gradle Wrapper: `gradlew.bat` kullanın, sistem Gradle değil

## CI Kapıları

| Tetikleyici | Komutlar |
|---|---|
| Pull Request | `testDebugUnitTest` + `lintDebug` + `assembleDebug` |
| main branch merge | `testDebugUnitTest` + `lintRelease` + `assembleRelease` |
| Release tag | `bundleRelease` + imzalı artifact |
