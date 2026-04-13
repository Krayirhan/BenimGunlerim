# BenimGünlerim

BenimGünlerim, kullanıcının gününü küçük görevler, basit rutinler ve görünür ilerleme hissiyle yönetmesine yardımcı olan offline-first Android uygulamasıdır.

## Teknoloji

- Kotlin
- Jetpack Compose
- Room
- DataStore
- Hilt
- Navigation Compose
- Offline-first yerel veri mimarisi

## Geliştirme Ortamı

Gereksinimler:

- JDK 17
- Android SDK 35
- Gradle Wrapper

Windows örneği:

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot'
$env:ANDROID_HOME='C:\Users\Acer\AppData\Local\Android\Sdk'
.\gradlew.bat assembleDebug
```

## Hızlı Kontrol

```powershell
.\scripts\check-local.ps1
```

## Release Kontrolü

```powershell
.\scripts\check-release.ps1
```

Release için signing bilgileri `keystore.properties` veya CI secret'ları ile sağlanmalıdır. Signing doğrulaması geçmeyen artifact production release sayılmaz.

## Mevcut Ürün Kapsamı

- Onboarding
- Şablon veri oluşturma
- Bugün ekranı
- Görev ekleme, düzenleme, tamamlama ve silme
- Alt görevler
- Rutin ekleme, düzenleme, tamamlama ve arşivleme
- Completion log
- Günlük ilerleme
- Akşam özeti
- İlerlemen ekranı
- Ayarlar
- Bildirimler ve sessiz saatler
- Yerel veri export akışı

## Production Dokümanları

- [Production readiness özeti](docs/production/production-readiness.md)
- [Tarihli audit snapshot](docs/production/readiness-audit-2026-04-13.md)
- [Kalite kapıları](docs/production/quality-gates.md)
- [Release checklist](docs/release/release-checklist.md)

Eski sprint planları ve tarihsel çalışma notları `docs/archive/` altında tutulur.
