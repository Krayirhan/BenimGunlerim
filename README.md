<div align="center">

# 📆 BenimGünlerim

### Offline-first Günlük Görev & Rutin Takibi — Android

*Görev ve rutinlerle görünür ilerleme hissi*

[![Kotlin](https://img.shields.io/badge/Kotlin-Jetpack%20Compose-7F52FF?style=flat-square&logo=kotlin&logoColor=white)](https://kotlinlang.org)
[![Room](https://img.shields.io/badge/Room-Offline--first-3DDC84?style=flat-square&logo=android&logoColor=white)](https://developer.android.com/training/data-storage/room)
[![Hilt](https://img.shields.io/badge/DI-Hilt-4285F4?style=flat-square&logo=dagger&logoColor=white)](https://dagger.dev/hilt/)
[![SDK](https://img.shields.io/badge/Android%20SDK-35-3DDC84?style=flat-square&logo=android&logoColor=white)](build.gradle.kts)

</div>

---

Kullanıcının gününü küçük görevler, basit rutinler ve görünür ilerleme hissiyle yönetmesine yardımcı olan **tamamen offline-first** Android uygulaması. İnternet bağlantısı olmadan da tüm özellikler çalışır.

## ✨ Kapsam

| Alan | Özellikler |
|---|---|
| **Onboarding** | Şablon veri oluşturma ile hızlı başlangıç |
| **Görevler** | Ekleme, düzenleme, tamamlama, silme, alt görevler |
| **Rutinler** | Ekleme, düzenleme, tamamlama, arşivleme |
| **İlerleme** | Completion log, günlük ilerleme, akşam özeti, "İlerlemen" ekranı |
| **Bildirimler** | Hatırlatmalar ve sessiz saatler |
| **Veri** | Yerel export akışı, tamamen offline veri mimarisi |

## 🧰 Teknoloji Yığını

Kotlin · Jetpack Compose · Room · DataStore · Hilt · Navigation Compose

## 🚀 Geliştirme Ortamı

### Gereksinimler

| Araç | Sürüm |
|---|---|
| JDK | 17 |
| Android SDK | 35 |
| Gradle Wrapper | dahil |

### Derleme (Windows)

```powershell
$env:JAVA_HOME='C:\Program Files\Eclipse Adoptium\jdk-17.0.16.8-hotspot'
$env:ANDROID_HOME='C:\Users\Acer\AppData\Local\Android\Sdk'
.\gradlew.bat assembleDebug
```

### Doğrulama scriptleri

```powershell
.\scripts\check-local.ps1     # Hızlı kontrol
.\scripts\check-release.ps1   # Release kontrolü
```

> Release için signing bilgileri `keystore.properties` veya CI secret'ları ile sağlanmalıdır. Signing doğrulaması geçmeyen artifact, production release sayılmaz.

## 📚 Dokümantasyon

| Konu | Doküman |
|---|---|
| Tasarım ve teknik mimari | [DESIGN.md](DESIGN.md) |
| Güncel proje durumu / sprint kaynağı | [docs/PROJECT_STATUS.md](docs/PROJECT_STATUS.md) |
| Ürün felsefesi ve kullanıcı akışları | [docs/product/benimgunlerim-urun-felsefesi-ve-akislari.md](docs/product/benimgunlerim-urun-felsefesi-ve-akislari.md) |
| Production readiness özeti | [docs/production/production-readiness.md](docs/production/production-readiness.md) |
| Kalite kapıları | [docs/production/quality-gates.md](docs/production/quality-gates.md) |
| Performans politikası | [docs/production/performance-policy.md](docs/production/performance-policy.md) |
| Dependency/platform politikası | [docs/production/dependency-platform-policy.md](docs/production/dependency-platform-policy.md) |
| Release checklist | [docs/release/release-checklist.md](docs/release/release-checklist.md) |

Eski sprint planları ve tarihsel çalışma notları `docs/archive/` altında tutulur.

---

<div align="center">

**Stack:** Kotlin · Jetpack Compose · Room · DataStore · Hilt · Navigation Compose

</div>
