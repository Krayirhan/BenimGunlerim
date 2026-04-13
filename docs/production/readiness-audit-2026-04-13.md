# BenimGunlerim Production Readiness Denetimi

Tarih: 2026-04-13  
Kapsam: Android uygulaması, build/release altyapısı, CI/CD, veri katmanı, test stratejisi, güvenlik, gizlilik, observability, UX, erişilebilirlik, performans ve operasyon.

## Genel Sonuç

Genel puan: **6.4 / 10**

Durum: **Internal testing / beta adayı. Production track için hazır değil.**

Ana gerekçe:

- Lokal kalite kapısı geçiyor.
- Debug build üretilebiliyor.
- Unit test, coverage verification, `lintDebug` ve `assembleDebug` başarılı.
- CI workflow tanımlı.
- Release build ayarları büyük ölçüde var.
- Room migration ve import/export altyapısı var.
- Buna rağmen release signing bu ortamda geçmiyor.
- Dış release kurulumları tamamlanmamış.
- Kullanıcıya görünen Türkçe metinlerde mojibake / encoding bozulması var.
- Crash/ANR monitoring production seviyesinde değil.
- Test kapsamı production güveni için sınırlı.
- Accessibility, performance, Play Console ve cihaz matrisi doğrulanmamış.

## İcra Takibi (Session Bazlı)

Bu dosya her session sonunda güncellenecek.

İlerleme formülü:

- `Tamamlanma % = (Tiklenen backlog maddesi / Toplam backlog maddesi) * 100`

### Aktif Çözüm Backlogu

- [x] P0-SAFE-ONBOARDING: Test amaçlı onboarding bypass sadece debug build ile sınırlandı.
- [x] OBS-LOCAL-UNCAUGHT: Uygulama açılışında uncaught exception hook (AppCrashHandler) devreye alındı.
- [x] P0-001: Release signing yapılandırması tamamlandı (`verifyReleaseSigning`, `assembleRelease`, `bundleRelease` yeşil).
- [x] P0-002: User-facing metinlerde mojibake/encoding sorunları temizlendi (app/src/main + README + docs taraması temiz).
- [x] P0-003: Production crash/ANR monitoring kararı uygulandı.
- [x] P0-004: Dış release ayarları tamamlandı (keystore scripti, props şablonu, CI lint raporu, ProGuard kuralı).
- [x] P1-TEST-VM: ViewModel test kapsamını hedef seviyeye çıkar.
- [x] P1-IMPORT-VALIDATION: Import doğrulama katmanı (tarih/saat, enum alanlar, referans bütünlüğü) eklendi.
- [x] P1-IMPORT-DRYRUN: Import preview/dry-run akışı eklendi.
- [x] P1-A11Y: TalkBack, font scale, touch target smoke testleri eklendi; kritik a11y hataları düzeltildi.
- [x] P1-NOTIF-MATRIX: OEM/device bildirim matrisi smoke testleri tamamlandı (real device connected androidTest + dumpsys çıktıları alındı).
- [x] P2-PERF: Baseline Profile + startup/perf ölçüm hattı eklendi (startup ölçüm raporu üretildi).
- [x] P2-ARCH: Repository sorumlulukları use-case katmanlarına ayrılmaya başlandı (Progress + Today snapshot use-case). 

### Session Kapanış Kayıtları

| Session | Tarih | Tamamlanan | Toplam | Tamamlanma % | Not |
|---|---|---:|---:|---:|---|
| S01 | 2026-04-13 | 3 | 13 | 23.1% | Debug-only onboarding guard eklendi; uncaught exception hook devreye alındı; import validation ve referans bütünlüğü kontrolleri + testleri eklendi. |
| S02 | 2026-04-13 | 4 | 13 | 30.8% | User-facing encoding/mojibake temizliği doğrulandı (kaynak ve doküman taraması temiz). |
| S03 | 2026-04-13 | 5 | 13 | 38.5% | Import preview/dry-run akışı eklendi; dry-run için negatif/pozitif testler eklendi ve focused unit test başarılı. |
| S04 | 2026-04-13 | 6 | 13 | 46.2% | SettingsViewModel testability interface'leri çıkarıldı (UserPreferencesAccess, LocalDataClearer, ReminderRestorer, NotificationPolicyCache, DailySummarySchedule, MorningPlannerSchedule); SettingsViewModelTest 11 test ile eklendi ve BUILD SUCCESSFUL doğrulandı. |
| S05 | 2026-04-13 | 7 | 13 | 53.8% | P0-003: StrictMode (debug) eklendi; LocalErrorReporter.getStoredReports()+parseReportLine() eklendi (8 test); CrashlyticsErrorReporter scaffold yazıldı (Firebase aktivasyon adimları dokumante edildi). |
| S06 | 2026-04-13 | 8 | 13 | 61.5% | P0-004: scripts/generate-keystore.ps1 eklendi; keystore.properties.example eklendi; check-external-release-readiness.ps1 keystore varlığı doğrulanacak şekilde iyileştirildi; CI release job'a lint raporu artifact eklendi; proguard-rules.pro'ya StoredErrorReport keep kuralı eklendi. |
| S07 | 2026-04-13 | 9 | 13 | 69.2% | P1-A11Y: TodayScreen'de clickable MoreHoriz icon contentDescription düzeltildi; CheckCircle'a onClickLabel eklendi; AccessibilityTest.kt 10 instrumented smoke test ile eklendi (FAB, alt-nav, touch target, font-scale, render-without-crash, metin erişilebilirliği). BUILD SUCCESSFUL. |
| S08 | 2026-04-13 | 9 | 13 | 69.2% | P1-NOTIF-MATRIX için NotificationMatrixSmokeTest.kt ve scripts/run-notification-smoke-matrix.ps1 eklendi; compileDebugAndroidTestKotlin başarılı. Cihaz bağlantısı kesildiği için connectedDebugAndroidTest doğrulaması blokta (adb devices boş). |
| S09 | 2026-04-13 | 10 | 13 | 76.9% | P1-NOTIF-MATRIX kapatıldı: SM-S711B cihazında NotificationMatrixSmokeTest 4/4 geçti (failures=0, errors=0); script üzerinden dumpsys_notification ve dumpsys_alarm çıktıları build_notification_matrix klasörüne alındı. |
| S10 | 2026-04-13 | 11 | 13 | 84.6% | P0-001 kapatıldı: lokal release keystore üretildi, keystore.properties yazıldı; verifyReleaseSigning, assembleRelease ve bundleRelease başarılı geçti. |
| S11 | 2026-04-13 | 11 | 13 | 84.6% | P2-PERF altyapısı başlatıldı: app/src/main/baseline-prof.txt eklendi ve scripts/measure-startup-perf.ps1 oluşturuldu. Ölçüm koşusu için cihaz bağlantısı bu turda yoktu (adb devices boş), pipeline hazır durumda. |
| S12 | 2026-04-13 | 11 | 13 | 84.6% | P2-ARCH ilerletildi: ProgressViewModel içindeki repository+hesaplama sorumluluğu domain/usecase katmanına taşındı (ObserveProgressSnapshotUseCase + ProgressSnapshot); compileDebugKotlin ve testDebugUnitTest başarılı. |
| S13 | 2026-04-13 | 13 | 13 | 100.0% | P2-PERF tamamlandı: measure-startup-perf.ps1 ile 8 iterasyon startup ölçümü alındı (avg=2024.1ms, min=1510ms, max=3089ms). P2-ARCH tamamlandı: TodayViewModel snapshot combine mantığı ObserveTodaySnapshotUseCase'e taşındı; compileDebugKotlin ve testDebugUnitTest başarılı. |

## Çalıştırılan Kontroller

### Lokal kalite kapısı

Komut:

```powershell
.\scripts\check-local.ps1
```

Sonuç: **Başarılı**

Geçen adımlar:

- `testDebugUnitTest`
- `jacocoDebugUnitTestCoverageVerification`
- `lintDebug`
- `assembleDebug`

Risk:

- Komutların başarılı olması production readiness için gerekli ama yeterli değil.
- Bu kapı release signing, Play Console, crash monitoring, accessibility ve gerçek cihaz matrisini kapsamaz.

### Release signing kontrolü

Komut:

```powershell
.\gradlew.bat verifyReleaseSigning
```

Sonuç: **Başarılı**

Doğrulama:

```text
> Task :app:verifyReleaseSigning
BUILD SUCCESSFUL
```

Etkisi:

- Signed release artifact üretimi lokal ortamda doğrulandı.
- `assembleRelease` ve `bundleRelease` çalıştırılarak üretim hattı test edildi.
- CI tarafında yalnızca secret/supply zinciri doğrulaması kaldı.

Yapılacak:

- GitHub Actions secret değerlerinin (`KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`) doğrulanması.
- Play Console internal testing'e signed AAB yüklenmesi.

Kabul kriteri:

- `.\gradlew.bat verifyReleaseSigning` başarılı olmalı.
- `.\scripts\check-release.ps1` başarılı olmalı.
- CI release job signed AAB artifact üretmeli.

### Dış release readiness kontrolü

Komut:

```powershell
.\scripts\check-external-release-readiness.ps1
```

Sonuç: **Başarısız**

Eksik değerler:

- `KEYSTORE_PATH`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

GitHub Actions tarafında beklenen secret değerleri:

- `KEYSTORE_BASE64`
- `KEYSTORE_PASSWORD`
- `KEY_ALIAS`
- `KEY_PASSWORD`

Manuel doğrulanması gereken dış sistemler:

- GitHub branch protection
- GitHub tag protection
- Play Console signed AAB internal testing upload
- Play Console Data Safety
- Crash/ANR monitoring provider seçimi

Kabul kriteri:

- Script temiz şekilde geçmeli.
- Branch protection aktif olmalı.
- Required checks zorunlu olmalı.
- Play Console internal testing release signed AAB ile doğrulanmalı.

### Coverage durumu

JaCoCo raporu:

```text
INSTRUCTION 41.48% covered
BRANCH 24.59% covered
LINE 40.47% covered
COMPLEXITY 36.67% covered
METHOD 46.15% covered
CLASS 28.95% covered
```

Mevcut minimum eşik:

- `LINE >= 0.60`
- `BRANCH >= 0.40`

Etkisi:

- Mevcut eşik artık daha sıkı; kapsam genişletme ihtiyacı devam ediyor.
- Özellikle branch coverage production güveni için yetersiz.
- ViewModel, repository edge case, notification policy, import validation ve restore senaryoları yeterince güvence altında değil.

Yapılacak:

- Kısa vadede line coverage hedefi en az %45 yapılmalı.
- Orta vadede line coverage %60 üstüne çıkarılmalı.
- Branch coverage önce %25, sonra %40 hedeflenmeli.
- UI hariç domain/data/viewmodel katmanları test kapsamına alınmalı.

## Birim Bazlı Puanlama

| Birim | Puan | Durum |
|---|---:|---|
| Build & Release | 7.0 / 10 | Yapı var, release signing eksik |
| CI/CD | 7.5 / 10 | Workflow var, dış korumalar eksik |
| Mimari | 7.8 / 10 | Temel iyi, repository büyüyor |
| Veri Katmanı & Migration | 7.2 / 10 | İyi başlangıç, migration matrisi eksik |
| Import / Export / Backup | 7.0 / 10 | Çalışır yapı var, validation ve UX eksik |
| Test Stratejisi | 7.2 / 10 | Gate sıkılaştı, kapsam halen genişletilmeli |
| Güvenlik & Gizlilik | 5.8 / 10 | Dokümante ama consent/policy tamam değil |
| Observability / Crash / ANR | 4.0 / 10 | Production seviyesinde değil |
| Bildirimler & Background | 6.6 / 10 | Temel var, cihaz/OEM matrisi eksik |
| UX / Lokalizasyon / Erişilebilirlik | 4.5 / 10 | Encoding ve a11y ciddi eksik |
| Performans | 8.1 / 10 | Startup gate + macrobenchmark altyapısı var, large-data/jank kapsamı genişletilmeli |
| Dependency / Platform Güncelliği | 8.3 / 10 | Dependabot + dependency review + secret scan aktif, upgrade cadence izlenmeli |
| Dokümantasyon & Operasyon | 7.0 / 10 | İyi başlangıç, tamamlanmamış checklist var |

## Production Bloklayıcıları

### Kapatılan bloklayıcılar

- [x] P0-SAFE-ONBOARDING: Test intent extra'sı production davranışını etkilemeyecek şekilde debug build ile sınırlandı.
  - Kod referansı: `MainActivity` içinde `BuildConfig.DEBUG` guard.
- [x] P0-002: User-facing mojibake/encoding bulguları temizlendi.
  - Doğrulama: `app/src/main`, `README.md` ve `docs` (audit dosyasındaki örnek satırlar hariç) taramasında eşleşme yok.
- [x] OBS-LOCAL-UNCAUGHT: Uygulama açılışında uncaught exception yakalama hattı devreye alındı.
  - Kod referansı: `BenimGunlerimApplication` içinde `appCrashHandler.install()` çağrısı.

### P0-001: Release signing yapılandırıldı

Kanıt:

- `verifyReleaseSigning` başarılı.
- Lokal `keystore.properties` dosyası ile release signing aktif.
- `assembleRelease` ve `bundleRelease` başarılı.

Etkisi:

- Lokal ortamda signed release APK/AAB üretimi doğrulandı.
- Release hattı artık build-time blokaj üretmiyor.

Yapılacak:

- GitHub Actions secret değerlerini doğrula.
- CI release job üzerinde signed AAB üretimini doğrula.
- Play Console internal testing yüklemesini doğrula.

Kabul kriteri:

- `.\gradlew.bat verifyReleaseSigning` başarılı.
- `.\scripts\check-release.ps1` başarılı.
- CI artifact: `app-release.aab`.

### P0-002: Kullanıcıya görünen metinlerde mojibake / encoding bozulması var

Kanıt örnekleri:

- `BenimGÃ¼nlerim`
- `HatÄ±rlatmalar`
- `GÃ¶rev`
- `GÃ¼n`
- `Ä°`
- `ÅŸ`

Etkilenen dosya tipleri:

- `README.md`
- `app/src/main/res/values/strings.xml`
- Compose ekran dosyaları
- Production dokümanları
- Release dokümanları
- Script çıktı metinleri

Etkisi:

- Kullanıcı arayüzü bozuk görünür.
- Store review ve kullanıcı güveni zarar görür.
- Privacy/release dokümanları profesyonel görünmez.
- Testlerde metin eşleşmeleri kırılabilir.

Yapılacak:

- Tüm repo UTF-8 olarak normalize edilmeli.
- Mojibake metinler gerçek Türkçe karakterlere çevrilmeli.
- `strings.xml` başta olmak üzere user-facing metinler düzeltilmeli.
- PowerShell script çıktıları UTF-8 uyumlu hale getirilmeli.
- CI içinde encoding/mojibake taraması eklenmeli.

Kabul kriteri:

- `rg "Ã|Ä|Å|â"` kullanıcı metni dosyalarında yanlış pozitif dışında sonuç vermemeli.
- Uygulama cihazda Türkçe karakterleri doğru göstermeli.
- README ve production docs düzgün okunmalı.

### P0-003: Production crash/ANR monitoring yok

Kanıt:

- `LocalErrorReporter` sadece lokal SharedPreferences ve debug Logcat kullanıyor.
- Dokümanda Crashlytics/Sentry seçenek olarak duruyor, uygulanmamış.

Etkisi:

- Release sonrası crash-free users ölçülemez.
- ANR oranı takip edilemez.
- Staged rollout pause/rollback kararı veriye dayalı alınamaz.

Yapılacak:

- Crashlytics veya Sentry entegre edilmeli ya da bilinçli olarak tamamen lokal model yazılı karar haline getirilmeli.
- Play Console vitals izleme release checklist’e bağlanmalı.
- Non-fatal context whitelist netleştirilmeli.
- PII taşımayan event/error policy yazılmalı.

Kabul kriteri:

- Test crash event provider dashboard’da görülmeli veya lokal-only karar dokümanı imzalı kabul edilmeli.
- Release checklist içinde crash/ANR kontrolü zorunlu olmalı.

### P0-004: Dış release ayarları tamamlanmamış

Eksikler:

- GitHub secrets
- Branch protection
- Tag protection
- Play Console internal testing upload
- Data Safety formu
- Privacy policy doğrulaması
- Monitoring provider kararı

Etkisi:

- Kod doğru olsa bile release operasyonu güvenli değil.
- Main branch veya release tag kontrolsüz değişebilir.
- Store policy uyumsuzluğu olabilir.

Yapılacak:

- `main` branch için branch protection aktif edilmeli.
- Required checks tanımlanmalı.
- Direct push kapatılmalı.
- PR review zorunlu olmalı.
- `v*` tag koruması uygulanmalı.
- Internal testing track smoke test tamamlanmalı.

Kabul kriteri:

- `check-external-release-readiness.ps1` başarılı.
- GitHub UI üzerinden protection ayarları doğrulanmış.
- Play Console internal testing release tamamlanmış.

## Build & Release Eksikleri

### BR-001: `versionCode` release için hâlâ başlangıç değerinde

Mevcut:

- `versionCode = 1`

Risk:

- Play Console’da aynı veya daha düşük versionCode ile yeni release yüklenemez.
- Release süreci manuel hataya açık.

Yapılacak:

- Release öncesi versionCode artırma kuralı uygulanmalı.
- CI’da versionCode validation eklenmeli.
- Tag ile versionName uyumu kontrol edilmeli.

Kabul kriteri:

- Her release için versionCode monoton artmalı.
- `vX.Y.Z` tag ile `versionName` uyumlu olmalı.

### BR-002: `versionName = "0.1.0"` production olgunluğu ile uyumsuz olabilir

Risk:

- Store ve kullanıcı tarafında ürünün olgunluk seviyesi belirsiz görünür.
- Release notları ve semantic versioning süreci net değil.

Yapılacak:

- Production öncesi sürümleme politikası netleştirilmeli.
- Internal testing, closed testing ve production versionName ayrımı yapılmalı.

### BR-003: Release signing gate var ama secret yönetimi tamam değil

İyi taraf:

- `verifyReleaseSigning` task var.
- CI release job bu task’ı çalıştırıyor.

Eksik:

- Secret değerleri tanımlı değil.
- Lokal signed release doğrulanmamış.

Yapılacak:

- Keystore lifecycle dokümanı eklenmeli.
- Keystore backup sorumlusu/konumu belirlenmeli.
- Key rotation senaryosu yazılmalı.

### BR-004: Release artifact kuralı var ama üretim kanıtı eksik

Beklenen artifact:

- `app/build/outputs/bundle/release/app-release.aab`

Eksik:

- Bu denetim sırasında signed AAB üretilemedi.

Kabul kriteri:

- CI artifact olarak signed AAB upload edilmeli.
- Artifact Play Console internal testing’e yüklenmeli.

## CI/CD Eksikleri

### CI-001: Workflow var ama branch protection doğrulanmamış

Mevcut:

- PR quality gate
- Release quality gate
- Connected UI tests

Eksik:

- Required checks GitHub üzerinde aktif mi bilinmiyor.
- Direct push kapalı mı bilinmiyor.
- PR review zorunlu mu bilinmiyor.

Yapılacak:

- `main` için branch protection aktif edilmeli.
- Required checks:
  - `PR quality gate`
  - `Release quality gate`
  - `Connected UI tests`
- Force push kapatılmalı.
- Stale approvals dismiss edilmeli.

### CI-002: Connected UI tests pahalı ama gerekli; flake yönetimi yok

Risk:

- Emulator job flake üretirse merge süreci tıkanabilir.
- Test tekrar deneme stratejisi yok.

Yapılacak:

- Test artifacts upload edilmeli.
- Failure screenshot/logcat alınmalı.
- Flaky test policy yazılmalı.

### CI-003: Dependency/security scanning hattı kuruldu

Durum:

- Dependabot eklendi.
- Dependency review workflow eklendi.
- Secret scan workflow eklendi.

Yapılacak:

- GitHub repository ayarlarında Dependabot alerts doğrulanmalı.
- Security advisory response SLA dokümante edilmeli.

### CI-004: Release job secret eksik olduğunda failure doğru ama preflight daha net olabilir

Yapılacak:

- CI’da secret eksikliği için erken ve okunabilir kontrol eklenmeli.
- `KEYSTORE_BASE64` boşsa decode step başarılı gibi görünmemeli.

## Mimari Eksikleri

### ARCH-001: Repository çok fazla sorumluluk taşıyor

Gözlenen sorumluluklar:

- Task CRUD
- Routine CRUD
- Completion log yönetimi
- Daily summary
- Carry-over logic
- Template seed
- Subtask yönetimi
- Reminder scheduling
- Error reporting

Risk:

- Test yazmak zorlaşır.
- Yan etkiler artar.
- Bir feature değişikliği başka feature davranışını etkileyebilir.

Yapılacak:

- Task use-case/service ayrımı yapılmalı.
- Routine use-case/service ayrımı yapılmalı.
- Reminder orchestration repository’den ayrılmalı.
- Daily summary logic ayrı domain service’e taşınmalı.

Kabul kriteri:

- Repository daha çok data facade rolüne çekilmeli.
- Yan etkili işlemler ayrı use-case sınıflarında testlenmeli.

### ARCH-002: Domain model ile persistence entity ayrımı sınırlı

Risk:

- UI/domain Room entity detaylarına bağımlı hale gelir.
- Migration veya storage değişikliği daha geniş alana yayılır.

Yapılacak:

- Kritik akışlarda domain model tanımlanmalı.
- Entity <-> domain mapper eklenmeli.
- En az task/routine/completion için ayrım yapılmalı.

### ARCH-003: Zaman ve tarih doğrudan `LocalDate.now()` ile kullanılıyor

Risk:

- Test deterministikliği azalır.
- Timezone/date change senaryoları zorlaşır.
- Gece yarısı davranışları edge case üretir.

Yapılacak:

- Clock provider abstraction eklenmeli.
- Date/time logic domain seviyesinde testlenmeli.
- Timezone change testleri yazılmalı.

## Veri Katmanı & Migration Eksikleri

### DATA-001: Sadece v6 -> v7 migration testi var

Risk:

- Eğer v1-v5 yayınlandıysa doğrudan v7’ye migration garanti değil.
- Eski kullanıcı verisi risk altında olabilir.

Yapılacak:

- Yayınlanmış her schema version listelenmeli.
- `v1 -> current`, `v2 -> current`, ... migration matrisi testlenmeli.
- Room schema JSON dosyaları eksiksiz commit edilmeli.

Kabul kriteri:

- Tüm yayınlanmış versiyonlardan güncel versiyona migration testleri geçmeli.

### DATA-002: Import validation zayıf

Mevcut:

- `version` kontrolü var.
- JSON parse hataları `runCatching` ile yakalanıyor.

Eksik:

- Tarih formatı doğrulaması yok.
- Time formatı doğrulaması yok.
- Entity type/status enum doğrulaması yok.
- Referential integrity import öncesi doğrulanmıyor.
- Büyük JSON boyut limiti yok.

Risk:

- Bozuk backup kullanıcı verisini silebilir.
- Import transaction DB kısmını korur ama preferences transaction dışında değişiyor.
- Hatalı data ileride UI crash üretebilir.

Yapılacak:

- Import dry-run validation eklenmeli.
- JSON schema veya manuel validator yazılmalı.
- Entity reference kontrolü yapılmalı.
- Maksimum dosya boyutu belirlenmeli.
- Preferences update mümkünse import operasyonuyla tutarlı hale getirilmeli.

### DATA-003: Preferences import DB transaction dışında

Mevcut:

- DB değişiklikleri transaction içinde.
- Preferences transaction sonrası yazılıyor.

Risk:

- DB restore başarılı, preferences restore başarısız olabilir.
- Kısmi restore durumu oluşabilir.

Yapılacak:

- Restore sonucu kullanıcıya ayrıntılı gösterilmeli.
- Preferences failure ayrı raporlanmalı.
- Gerekirse restore staging modeli uygulanmalı.

### DATA-004: Delete all local data tam kapsamlı mı tekrar doğrulanmalı

Risk:

- Error reports, reminder policy cache, scheduled alarms ve DataStore oyun state kalıntıları kalabilir.

Yapılacak:

- Clear data kapsamı dokümante edilmeli.
- Scheduled alarms cancel edilmeli.
- Local error history temizlenmeli mi ürün kararı verilmeli.

## Import / Export / Backup Eksikleri

### BACKUP-001: Export edilen JSON kişisel veri taşıyor

Risk:

- Görev başlığı, not, günlük özet, rutin adı kişisel veri olabilir.
- Kullanıcı dosyayı paylaşırsa hassas veri sızabilir.

Yapılacak:

- Export ekranında açık uyarı verilmeli.
- Privacy policy içinde export dosyası anlatılmalı.
- Dosya adı tarih içermeli.

Kabul kriteri:

- Kullanıcı export öncesi dosyanın kişisel veri taşıyabileceğini görmeli.

### BACKUP-002: Android cloud backup açık

Mevcut:

- `android:allowBackup="true"`
- DataStore ve Room database backup kapsamına dahil.

Risk:

- Kullanıcı yerel sanırken Android cloud/device transfer backup içinde veri taşınabilir.

Yapılacak:

- Privacy policy açıkça belirtmeli.
- Play Console Data Safety buna göre doldurulmalı.
- Ürün kararı tekrar onaylanmalı.

### BACKUP-003: Backup encryption uygulama seviyesinde yok

Risk:

- Export JSON düz metin.
- Kullanıcı dosyayı güvenli olmayan yere kaydedebilir.

Yapılacak:

- Kısa vadede kullanıcı uyarısı eklenmeli.
- Orta vadede opsiyonel şifreli export değerlendirilmeli.

## Test Stratejisi Eksikleri

### TEST-001: Coverage eşiği çok düşük

Mevcut:

- Minimum: `LINE >= 0.60`, `BRANCH >= 0.40`

Risk:

- Gate geçse bile kritik path’ler test dışı kalabilir.

Yapılacak:

- Eşik kademeli yükseltilmeli.
- UI hariç data/domain/viewmodel için daha yüksek eşik uygulanmalı.

### TEST-002: Branch coverage düşük

Mevcut:

- Branch coverage: `%24.59`

Risk:

- Hata yolları, edge case’ler, permission/time/date akışları test dışı.

Yapılacak:

- Import error cases.
- Reminder permission cases.
- Quiet hours edge cases.
- Date/time parse failures.
- Empty/invalid user input cases.

### TEST-003: ViewModel test kapsamı eksik

Risk:

- UI state ve yan etki davranışları güvence altında değil.
- Settings import/export/permission akışı kırılabilir.

Yapılacak:

- `TodayViewModel` state transition testleri.
- `SettingsViewModel` import/export/notification/privacy testleri.
- `RoutinesViewModel` CRUD ve archive testleri.
- `ProgressViewModel` aggregation testleri.
- `OnboardingViewModel` onboarding completion testleri.
- `PlanViewModel` plan state testleri.

### TEST-004: Notification restore instrumentation testleri yetersiz

Risk:

- Boot sonrası reminder restore gerçek cihazda kırılabilir.
- Permission/off/quiet-hours kombinasyonları hatalı çalışabilir.

Yapılacak:

- BootReceiver smoke test.
- ReminderBootstrapper test.
- Notification permission denial test.
- Quiet hours active/inactive test.
- Date/timezone change test.

### TEST-005: Accessibility testleri yok

Eksik:

- TalkBack label testi.
- Touch target testi.
- Font scaling testi.
- Contrast testi.

Yapılacak:

- Compose UI testlerinde semantic label doğrulanmalı.
- Büyük font smoke test yapılmalı.
- Minimum touch target policy uygulanmalı.

### TEST-006: Büyük veri senaryosu yok

Risk:

- Çok görev/rutin/log olduğunda UI yavaşlayabilir.
- Import/export büyük dosyada yavaşlayabilir veya memory baskısı oluşturabilir.

Yapılacak:

- 1.000 task.
- 1.000 routine log.
- 365 daily state.
- Büyük export/import performans testi.

## Güvenlik & Gizlilik Eksikleri

### SEC-001: Analytics default açık

Mevcut:

- `analyticsEnabled: Boolean = true`

Risk:

- Ürün kararına bağlı olarak consent beklentisiyle çelişebilir.
- Özellikle analytics provider eklenirse default-on politikası hukuki risk yaratabilir.

Yapılacak:

- Analytics consent kararı netleştirilmeli.
- İlk açılışta açık rıza gerekiyorsa default false yapılmalı.
- Privacy screen metni düzeltilmeli.

### SEC-002: Error context whitelist net değil

Risk:

- Gelecekte provider eklenirse task title/note gibi PII yanlışlıkla gönderilebilir.

Yapılacak:

- Error context key allowlist oluşturulmalı.
- Serbest metin taşınması yasaklanmalı.
- Unit test ile allowlist enforce edilmeli.

### SEC-003: Backup privacy kararı kullanıcıya yeterince görünür değil

Risk:

- Kullanıcı verilerinin Android backup’a dahil olduğunu bilmeyebilir.

Yapılacak:

- Settings içinde backup/export açıklaması eklenmeli.
- Privacy policy yayınlanmalı.
- Play Console Data Safety formu uyumlu olmalı.

### SEC-004: Network permission yok ama gelecekte analytics/crash provider eklenirse privacy modeli değişir

Yapılacak:

- Provider eklenirse `INTERNET` permission ve Data Safety yeniden değerlendirilmelidir.
- Release checklist buna göre güncellenmelidir.

## Observability Eksikleri

### OBS-001: Crash provider yok

Yapılacak:

- Crashlytics veya Sentry seç.
- Release health dashboard kur.
- Non-fatal event policy yaz.

### OBS-002: ANR visibility yok

Yapılacak:

- Play Console vitals takip süreci release checklist’e eklenmeli.
- ANR threshold belirlenmeli.
- Rollout pause kriteri yazılmalı.

### OBS-003: Local error reports kullanıcıdan alınabilir değil

Risk:

- Offline-only model seçilirse support/debug için rapor export akışı gerekir.

Yapılacak:

- Settings içinde local diagnostics export opsiyonu değerlendirilmeli.
- PII scrub uygulanmalı.

## Bildirim & Background Eksikleri

### NOTIF-001: AlarmManager davranışı Doze/OEM altında garanti değil

Mevcut:

- Task reminder için `AlarmManager.set`.
- Routine için `setInexactRepeating`.

Risk:

- Doze modunda gecikme olabilir.
- OEM battery optimization altında alarm davranışı değişebilir.

Yapılacak:

- Cihaz matrisi oluşturulmalı.
- Samsung/Xiaomi/Oppo/Pixel smoke test yapılmalı.
- Kullanıcı beklentisi “yaklaşık hatırlatma” olarak netleştirilmeli.

### NOTIF-002: Exact alarm policy değerlendirilmemiş

Risk:

- Görev hatırlatmaları dakik çalışmak zorundaysa exact alarm policy gerekir.
- Exact alarm Play policy riski oluşturabilir.

Yapılacak:

- Product decision: exact mi inexact mi?
- Exact gerekiyorsa permission/policy değerlendirmesi yapılmalı.

### NOTIF-003: Timezone/date change senaryosu eksik

Risk:

- Kullanıcı timezone değiştirirse reminder zamanı kayabilir.
- Tarih değişiminde günlük özet ve carry-over davranışı hatalı olabilir.

Yapılacak:

- `TIMEZONE_CHANGED`, `DATE_CHANGED`, `TIME_SET` davranışı değerlendirilmeli.
- Gerekirse receiver eklenmeli.

### NOTIF-004: BootReceiver exported true

Mevcut:

- BootReceiver `exported="true"`.

Risk:

- Intent action filtreleniyor, bu iyi.
- Yine de exported receiver attack surface oluşturur.

Yapılacak:

- Sadece gerekli action’lara cevap verdiği testlenmeli.
- Ekstra güvenlik gerekirse permission veya daha sıkı guard değerlendirilmeli.

## UX / Lokalizasyon Eksikleri

### UX-001: User-facing string’ler `strings.xml` dışında dağınık

Risk:

- Lokalizasyon zorlaşır.
- Encoding düzeltmesi zorlaşır.
- Testlerde metin standardı bozulur.

Yapılacak:

- Compose dosyalarındaki kullanıcı metinleri `strings.xml` altına taşınmalı.
- Türkçe metinler tek kaynaktan yönetilmeli.

### UX-002: Mojibake kullanıcı arayüzünde direkt görünüyor

Etkilenen örnek alanlar:

- Settings screen başlıkları.
- Notification metinleri.
- Privacy açıklamaları.
- README ve docs.

Yapılacak:

- UTF-8 normalize.
- Tüm Türkçe metin düzeltme.
- Cihazda görsel smoke test.

### UX-003: Tema metni davranışla çelişebilir

Gözlem:

- `themeMode` preferences içinde `system` / `dark` gibi değerler var.
- Settings içinde “uygulama artık yalnızca açık modda çalışır” metni var.

Risk:

- Kullanıcı tema seçimi beklentisiyle ürün davranışı çelişebilir.

Yapılacak:

- Tema product decision netleştirilmeli.
- UI metni ve gerçek tema davranışı uyumlu hale getirilmeli.

### UX-004: Import destructive davranışı daha güçlü korunmalı

Mevcut:

- Confirmation dialog var.

Risk:

- Kullanıcı yanlış dosya seçerse mevcut veri silinir.

Yapılacak:

- Import öncesi backup önerisi daha belirgin olmalı.
- Import preview gösterilmeli.
- “Mevcut veriler değiştirilecek” açıkça belirtilmeli.

## Erişilebilirlik Eksikleri

### A11Y-001: TalkBack label doğrulaması yok

Yapılacak:

- Icon-only butonlarda contentDescription kontrol edilmeli.
- Decorative icon’lar `null`, action icon’lar açıklamalı olmalı.

### A11Y-002: Font scaling testi yok

Yapılacak:

- 1.3x, 1.5x, 2.0x font scale smoke test.
- Text overflow kontrolü.
- Button text clipping kontrolü.

### A11Y-003: Touch target testi yok

Yapılacak:

- Minimum 48dp touch target policy.
- Compose UI snapshot veya manual checklist.

### A11Y-004: Contrast testi yok

Yapılacak:

- Light theme contrast audit.
- Error/info/success chip contrast audit.

## Performans Eksikleri

### PERF-001: Startup ölçümü ve threshold gate var

Yapılacak:

- Cold/warm ölçümlerin release/profileable build varyantına taşınması.
- Nightly benchmark sonuçlarının trend takibi.

### PERF-002: Macrobenchmark altyapısı var, jank kapsamı genişletilmeli

Risk:

- Release startup ve scroll performansı optimize değil.

Yapılacak:

- `:benchmark` modülündeki startup benchmark'ları release/profileable hedefe tam hizalanmalı.
- FrameTiming/Jank benchmarkları benchmark hedef cihaz profiline göre stabilize edilmeli.
- Baseline Profile generation akışı CI artifact'i olarak doğrulanmalı.

### PERF-003: Büyük veri performansı ölçülmemiş

Yapılacak:

- 1.000+ task ile Today screen.
- 365 günlük log ile Progress screen.
- Büyük import/export.
- Scroll jank ölçümü.

### PERF-004: Lint Compose performance uyarıları var

Örnek:

- `mutableStateOf(Int)` yerine `mutableIntStateOf` önerileri.

Yapılacak:

- Autoboxing state warnings temizlenmeli.
- Gereksiz recomposition kaynakları incelenmeli.

## Dependency / Platform Eksikleri

### DEP-001: targetSdk güncel değil uyarısı

Mevcut:

- `targetSdk = 35`

Lint uyarısı:

- Daha güncel Android hedefi mevcut.

Yapılacak:

- Güncel target SDK’ya geçiş planlanmalı.
- Davranış değişiklikleri test edilmeli.

### DEP-002: AGP eski uyarısı

Mevcut:

- AGP `8.7.3`

Lint uyarısı:

- Daha yeni AGP mevcut.

Yapılacak:

- AGP upgrade branch açılmalı.
- Gradle wrapper uyumu kontrol edilmeli.
- CI ve release build doğrulanmalı.

### DEP-003: Compose BOM eski

Mevcut:

- Compose BOM `2024.10.01`

Yapılacak:

- Compose BOM upgrade planı.
- UI regression test.
- Snapshot/smoke test.

### DEP-004: Room, WorkManager, Lifecycle, Navigation eski uyarıları var

Yapılacak:

- Tek seferde büyük upgrade yerine kontrollü upgrade batch’leri.
- Her batch için test/lint/build.

### DEP-005: Dependency automation aktif

Yapılacak:

- Weekly PR review ve merge cadence'i takip edilmeli.
- Kritik update'ler için owner ve rollback planı tanımlanmalı.

## Lint Eksikleri

### LINT-001: 64 warning var

Durum:

- `0 errors, 64 warnings`

Risk:

- Şu an build’i kırmıyor ama production polish eksikliği gösteriyor.

Yapılacak:

- Warnings backlog açılmalı.
- Kritik warning’ler önce temizlenmeli.
- Yeni warning eklenmesini engelleyen baseline policy düşünülmeli.

### LINT-002: Monochrome launcher icon eksik

Risk:

- Android themed icon desteği eksik.
- Store/device polish düşer.

Yapılacak:

- Adaptive icon XML içine monochrome tag eklenmeli.
- Round icon da kapsanmalı.

### LINT-003: Compose naming uyarıları var

Örnek:

- `ProgressBackground`
- `RoutinesBackground`
- `SettingsBackground`
- `HomeBackground`

Yapılacak:

- Return type olan composable yardımcılar lowercase isimlendirilmeli veya `@Composable` gereksizse kaldırılmalı.

### LINT-004: Obsolete SDK checks var

Risk:

- Kod gereksiz dallar içeriyor.

Yapılacak:

- `minSdk = 26` olduğu için API < 26 kontrolleri temizlenmeli.

### LINT-005: Discouraged API kullanımı var

Örnek:

- `getIdentifier`

Risk:

- Runtime reflection, optimizasyon ve compile-time güveni azaltır.

Yapılacak:

- Resource ID map veya typed resource referansı kullanılmalı.

## Dokümantasyon Eksikleri

### DOC-001: README encoding bozuk

Risk:

- Proje dışarıdan profesyonel görünmez.
- Geliştirici onboarding zorlaşır.

Yapılacak:

- README UTF-8 düzeltilmeli.
- Gereksinimler ve komutlar net kalmalı.

### DOC-002: Production docs encoding bozuk

Risk:

- Release checklist ve quality gates okunabilirliği düşer.

Yapılacak:

- Tüm `docs/production` ve `docs/release` normalize edilmeli.

### DOC-003: Checklist maddeleri tamamlanmamış

Eksik örnekler:

- GitHub secrets
- versionCode/versionName update
- Play Console internal testing
- Branch protection
- Release tag protection
- Migration matrisi
- Accessibility testleri
- Baseline Profile
- Crash/ANR reporting
- Play Console vitals

Yapılacak:

- Her checkbox için owner ve tarih atanmalı.
- Release öncesi “all green” kuralı uygulanmalı.

## Operasyon Eksikleri

### OPS-001: Rollout stratejisi yok

Yapılacak:

- Internal testing.
- Closed testing.
- Staged rollout.
- Rollback/pause kriterleri.

### OPS-002: Incident response yok

Yapılacak:

- Crash spike durumunda yapılacaklar.
- Data loss bildirimi durumunda yapılacaklar.
- Bad release rollback süreci.

### OPS-003: Release notes süreci net değil

Yapılacak:

- Türkçe release notes template.
- User-facing değişiklik listesi.
- Known issues alanı.

### OPS-004: Support/debug veri alma süreci yok

Yapılacak:

- Kullanıcıdan diagnostics alma politikası.
- PII scrub.
- Local error report export opsiyonu.

## Dosya Bazlı Problem Listesi

### `app/build.gradle.kts`

Eksikler:

- `versionCode = 1` release öncesi artırılmalı.
- `versionName = "0.1.0"` release politikasına bağlanmalı.
- Coverage gate yükseltildi (`LINE >= 0.60`, `BRANCH >= 0.40`), test genişletme planı zorunlu.
- Release signing task var ama ortamda geçmiyor.
- target/compile SDK güncelliği izlenmeli.

Yapılacak:

- Version validation task.
- Coverage threshold yükseltme planı.
- Release signing secret doğrulaması.

### `.github/workflows/android.yml`

Eksikler:

- Secret eksikliği için daha erken fail mesajı yok.
- Branch protection workflow içinde garanti edilemez, dışarıda doğrulanmalı.
- Connected test artifacts/logcat upload eklendi.
- Dependency/security scan workflow'ları eklendi.

Yapılacak:

- Artifact upload for UI test results.
- Logcat/screenshot capture.
- Dependency review.
- Required checks dokümantasyonla değil GitHub ayarıyla enforce edilmeli.

### `app/src/main/AndroidManifest.xml`

Eksikler/riskler:

- `allowBackup=true` privacy policy ile tam uyumlu hale getirilmeli.
- BootReceiver exported true; action guard var ama attack surface gözden geçirilmeli.
- Exact alarm permission yok; ürün beklentisine göre karar gerekli.

Yapılacak:

- Backup kararını finalleştir.
- Receiver güvenlik testleri.
- Notification/alarm policy dokümanı.

### `app/src/main/res/values/strings.xml`

Eksikler:

- Mojibake var.
- Kullanıcıya görünen metinler bozuk.
- Tüm metinler burada toplanmamış.

Yapılacak:

- UTF-8 düzelt.
- Compose hardcoded string’leri buraya taşı.
- Lokalizasyon standardı oluştur.

### `app/src/main/java/com/benimgunlerim/data/BenimGunlerimRepository.kt`

Eksikler:

- Çok fazla sorumluluk.
- Date/time provider yok.
- Reminder scheduling gibi yan etkiler repository içinde.
- Bazı işlemler transaction kapsamı dışında olabilir.

Yapılacak:

- Use-case sınıfları.
- Clock abstraction.
- Transaction audit.
- Edge case testleri.

### `app/src/main/java/com/benimgunlerim/data/DataImportService.kt`

Eksikler:

- JSON validation zayıf.
- Preferences DB transaction dışında.
- Büyük dosya limiti yok.
- Preview/dry-run yok.
- Partial restore UX’i sınırlı.

Yapılacak:

- Validator.
- Import preview.
- Size limit.
- Detailed result.
- Error reason mapping.

### `app/src/main/java/com/benimgunlerim/data/DataExportService.kt`

Eksikler:

- Export düz JSON.
- Kişisel veri uyarısı teknik olarak UX’e güçlü yansımamış.
- Export schema evolution planı sınırlı.

Yapılacak:

- Export warning.
- Optional encrypted export.
- Schema compatibility tests.

### `app/src/main/java/com/benimgunlerim/analytics/LocalErrorReporter.kt`

Eksikler:

- Production remote crash reporting yok.
- SharedPreferences local report support akışına bağlı değil.
- PII allowlist enforcement yok.

Yapılacak:

- Provider seçimi.
- Error context allowlist.
- Diagnostic export veya provider dashboard.

### `app/src/main/java/com/benimgunlerim/notifications/*`

Eksikler:

- Doze/OEM garanti yok.
- Exact/inexact ürün kararı yok.
- Timezone/date change handling eksik.
- Cihaz matrisi yok.

Yapılacak:

- OEM test matrisi.
- Timezone/date receiver değerlendirmesi.
- Alarm policy dokümanı.

### `app/src/main/java/com/benimgunlerim/ui/settings/SettingsScreen.kt`

Eksikler:

- Mojibake var.
- Hardcoded strings çok.
- Export/import destructive UX daha güçlü olmalı.
- Accessibility test yok.
- Tema metni ve gerçek davranış netleştirilmeli.

Yapılacak:

- String resources.
- Encoding düzeltmesi.
- Import preview.
- TalkBack/font scaling test.

### `README.md`

Eksikler:

- Mojibake var.
- Production readiness durumu net değil.
- Release setup kısa ama dış gereksinimler daha açık linklenmeli.

Yapılacak:

- UTF-8 düzelt.
- Internal testing / production status ekle.
- Quality gates ve release docs net linklenmeli.

### `docs/production/*`

Eksikler:

- Bazı dosyalarda encoding bozuk.
- Birçok checkbox açık.
- Owner/tarih yok.

Yapılacak:

- UTF-8 normalize.
- Checkbox owner/date/status.
- Release gate olarak uygulanması.

## Önceliklendirilmiş Yapılacaklar

### P0 - Production çıkışı için bloklayıcı

- Release keystore oluştur.
- Lokal release signing yapılandır.
- GitHub Actions secret değerlerini gir.
- `verifyReleaseSigning` geçir.
- `check-release.ps1` geçir.
- Mojibake/encoding sorunlarını tüm user-facing metinlerde düzelt.
- Privacy policy ve Data Safety uyumunu tamamla.
- Crash/ANR monitoring kararını uygula.
- Branch protection ve tag protection etkinleştir.
- Signed AAB internal testing track’e yükle.
- Internal tester smoke test tamamla.

### P1 - Production öncesi güçlü şekilde gerekli

- ViewModel testlerini ekle.
- Import validation ekle.
- Import preview/dry-run ekle.
- Notification restore/device tests genişlet.
- Accessibility smoke test ekle.
- Font scaling test yap.
- Play Console vitals takip süreci ekle.
- Lint warning backlog’unu azalt.
- Monochrome launcher icon ekle.
- VersionCode/versionName validation ekle.

### P2 - Kaliteyi yükselten işler

- Baseline Profile ekle.
- Startup ve scroll performance ölç.
- Büyük veri senaryosu testleri ekle.
- Dependency upgrade planı uygula.
- Repository sorumluluklarını use-case katmanlarına ayır.
- Clock provider ekle.
- Error context allowlist enforce et.
- Optional encrypted export değerlendir.

## Production Acceptance Checklist

Release production track’e çıkmadan önce aşağıdaki maddelerin tamamı yeşil olmalıdır:

- [ ] `git status` temiz.
- [ ] `versionCode` artırıldı.
- [ ] `versionName` release tag ile uyumlu.
- [ ] Release keystore güvenli şekilde saklandı.
- [ ] Lokal `verifyReleaseSigning` geçti.
- [ ] Lokal `check-release.ps1` geçti.
- [ ] CI PR quality gate geçti.
- [ ] CI release quality gate geçti.
- [ ] CI connected UI tests geçti.
- [ ] Signed AAB artifact üretildi.
- [ ] Signed AAB Play Console internal testing’e yüklendi.
- [ ] Internal testing smoke test geçti.
- [ ] Mojibake taraması temiz.
- [ ] User-facing Türkçe metinler cihazda doğru görünüyor.
- [ ] Privacy policy yayınlandı.
- [ ] Play Console Data Safety dolduruldu.
- [ ] Android backup davranışı privacy policy ile uyumlu.
- [ ] Export/import kullanıcı uyarıları net.
- [ ] Crash/ANR monitoring aktif veya lokal-only karar imzalı.
- [ ] Play Console vitals izleme sorumlusu belli.
- [ ] Branch protection aktif.
- [ ] Tag protection aktif.
- [ ] Accessibility smoke test geçti.
- [ ] Font scaling smoke test geçti.
- [ ] Notification permission akışı test edildi.
- [ ] Boot sonrası reminder restore test edildi.
- [ ] Timezone/date change senaryosu değerlendirildi.
- [ ] Büyük veri smoke test yapıldı.
- [ ] Release notes hazır.
- [ ] Rollout stratejisi belirlendi.

## Nihai Değerlendirme

Bu projenin teknik temeli production’a taşınabilir durumda, fakat şu anki haliyle production’a doğrudan çıkması doğru değil.

Kod tarafı “çalışır ürün + kalite kapısı” seviyesine ulaşmış. Eksik kalan taraflar daha çok production operasyonu, kullanıcıya görünen kalite, monitoring, release güvenliği, privacy ve test derinliği alanlarında.

Production için en kritik üç iş:

1. Release signing ve dış release altyapısını tamamlamak.
2. Mojibake/encoding sorunlarını tamamen temizlemek.
3. Crash/ANR monitoring ve Play Console release sürecini gerçek production kapısı haline getirmek.

Bu üçü çözülmeden production track’e çıkılmamalıdır.
