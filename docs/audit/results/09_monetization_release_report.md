# Audit Raporu — Monetizasyon & Release Hazırlığı

> ⚠️ **2026-08-17 güncellemesi:** Privacy Policy, export/import UI ve analytics toggle P0 maddeleri kapandı; OSS lisans listesi senkronize edildi. Billing/IAP hâlâ bilinçli olarak yok. Ayrıca **Firebase Crashlytics tamamen kaldırıldı** — bu raporun "Crashlytics'i prod'da aktifleştir" önerisi (release checklist madde 6.5-6.6 dahil) artık geçersiz; `docs/release/release-checklist.md` bu değişikliği yansıtacak şekilde ayrıca güncellendi. Güncel durum için bkz. [`INCREMENTAL_REAUDIT_2026-08-17.md`](INCREMENTAL_REAUDIT_2026-08-17.md). Bu doküman tarihsel kayıt olarak değiştirilmemiştir.

## Genel Puan
4 / 10

## Kısa Karar
Release *mekanizması* (signing pipeline, versioning disiplini, quality gate'ler, staged rollout planı) olgun ve profesyonel seviyede belgelenmiş; ama *monetizasyon* tarafı sıfırdan başlıyor — kod tabanında Google Play Billing kütüphanesi, entitlement modeli veya herhangi bir gerçek-para IAP altyapısı yok. Ayrıca 06_security_privacy_report.md'de tespit edilen P0 sorunlar (Privacy Policy yok, export/import UI'a bağlı değil, analytics toggle görünmüyor) bu turda da doğrulandı — kod hâlâ aynı ölü referansları taşıyor. Sonuç: ücretsiz bir beta/kapalı test için altyapı yakın ama "beklet" kararı veriyorum — hem P0 gizlilik maddeleri hem de sıfırdan kurulacak bir monetizasyon planı olmadan ne production'a ne de ücretli modele geçilmeli.

## En Güçlü 5 Taraf
1. Release süreci disiplinli şekilde belgelenmiş: `docs/release/release-checklist.md` 10 bölümde signing, versioning, quality gate, cihaz smoke test, backup/gizlilik kontrolü, Play Store staged rollout, post-release izleme, performans kapısı ve supply-chain kontrollerini kapsıyor — çoğu erken aşama uygulamada bulunmayan bir olgunluk seviyesi.
2. Release signing yapılandırması güvenli tasarlanmış: `app/build.gradle.kts:21-41,57-66` keystore bilgisini `keystore.properties` (repo dışı, `.gitignore:` `keystore.properties` satırıyla korunuyor, `git ls-files` sonucu repoda izlenmiyor) veya ortam değişkenlerinden okuyor; `verifyReleaseSigning` task'ı (satır 102-113) eksik konfigürasyonda build'i durduruyor.
3. R8/ProGuard minification prod için gerçek anlamda aktif: `isMinifyEnabled = true`, `isShrinkResources = true` (`app/build.gradle.kts:74-75`) ve `app/proguard-rules.pro` (92 satır) Hilt, Room, WorkManager, Compose, Parcelable/Serializable, domain model'lere kadar kapsamlı keep-rule seti içeriyor — release APK/AAB boyutu ve crash riski açısından iyi hazırlanmış.
4. Crash reporting mimarisi hem gizlilik hem operasyonel açıdan bilinçli: Crashlytics yalnızca `google-services.json` varsa aktifleşiyor (`app/build.gradle.kts:16-19`) ve context allow-list'i (`ErrorReporterContextPolicy.kt`) PII sızıntısını engelliyor; şu an repoda `google-services.json` yok, yani prod crash reporting bilinçli olarak henüz devreye alınmamış (erken aşama için makul).
5. Mevcut oyunlaştırma ekonomisi (XP/Level/Gold/Shop) tamamen sanal para üzerine kurulu ve gerçek paraya hiç dokunmuyor: `ShopViewModel.kt:28-45` (`ALL_SHOP_ITEMS`) sadece kozmetik öğeler (çerçeve, kutlama efekti, renk aksanı, rapor modu) satıyor, `prefsRepository.purchaseItem` sadece yerel `gold` sayacını düşürüyor — bu, ileride gerçek bir premium katman eklenirken "pay-to-win" riski taşımayan temiz bir taban sağlıyor.

## En Kritik 10 Sorun
| Öncelik | Sorun | Etki | Kanıt/Dosya | Öneri |
|---|---|---|---|---|
| P0 | Uygulamada Google Play Billing (veya başka bir IAP) kütüphanesi hiç yok | Ücretli/premium modele geçiş şu an teknik olarak mümkün değil; "hazır mı?" sorusunun cevabı hayır | `gradle/libs.versions.toml` ve `app/build.gradle.kts:223-264` içinde `billingclient` grep'i sıfır sonuç verdi; `dependencies` bloğunda hiçbir satın alma SDK'sı yok | Premium karar netleşince `com.android.billingclient:billing-ktx` eklenip entitlement/receipt-validation katmanı (yerel + sunucu doğrulama olmadan yalnızca istemci tarafı entitlement, sahtecilik riski taşır) tasarlanmalı |
| P0 | Privacy Policy hâlâ yok, ölü referans hâlâ kodda | Play Store Data Safety formu doldurulamaz; store inceleme reddi riski | `SettingsViewModel.kt:54` — `"...Gizlilik politikasını inceleyin."` metni; `PRIVACY*` glob'u ve `privacy policy` grep'i `app/src/main` genelinde sıfır ekran/doküman sonucu döndürdü | Barındırılan bir Privacy Policy sayfası yazılıp Ayarlar'a bağlanmadan Play Console'a hiçbir track'e (internal dahil, Data Safety formu tüm track'lerde zorunlu) yükleme yapılmamalı |
| P0 | Kodlanmış export/import özelliği hâlâ Settings UI'a bağlı değil | Kullanıcı veri taşınabilirliği (KVKK/GDPR beklentisi) ve "önce yedekle sonra sil" akışından mahrum; monetizasyon açısından da "backup/export" tam bir premium aday olabilecekken şu an ücretsizde bile çalışmıyor | `DataExportService.kt` (200 satır), `DataImportService.kt` (429 satır), `SettingsViewModel.kt:98-148` (`exportData`, `exportDataToFile`, `importDataFromFileContent`, `importData`) tanımlı; `SettingsScreen.kt` (341 satır, tam okundu) içinde "export"/"import" için hiçbir satır/buton yok | Önce ücretsiz temel export'u UI'a bağla; ileride "otomatik zamanlı yedekleme" veya "bulut senkron export" gibi bir üst katman premium aday olabilir |
| P0 | "Anonim kullanım ölçümü" toggle'ı tanımlı ama render edilmiyor | Kullanıcı analytics'i kapatamıyor; Data Safety formunda "kullanıcı kontrolü var mı" sorusuna hayır cevabı verilmek zorunda kalınır | `strings.xml:422,425` (`settings_privacy_title`, `settings_privacy_pii_note`) var; `SettingsScreen.kt` satır 114-148 "Gizlilik" `SectionBlock`'unda bu switch hiç yok, sadece Onboarding/OSS lisansları/versiyon/veri-yerel satırları render ediliyor | Switch'i Gizlilik bölümüne ekle |
| P1 | OSS lisans ekranı statik ve eksik, gerçek bağımlılıklarla senkron değil | Play Store politikası ve bazı lisansların atıf zorunluluğu tam karşılanmıyor; ayrıca DataStore, WorkManager, Firebase Crashlytics, Konfetti gibi gerçek runtime bağımlılıkları listede yok | `OssLicensesScreen.kt:36-73` (`LIBRARIES` sabit listesi, 6 kütüphane) vs `app/build.gradle.kts:239-264` (datastore, work, firebase-crashlytics, konfetti-compose, navigation-compose, hilt-navigation-compose bağımlılıkları listede yok) | `oss-licenses-plugin` veya benzeri otomatik üretim aracına geçilmeli |
| P1 | Store listing / görsel varlık hazırlığı için kanıt yok | Play Console'a yüklenecek ekran görüntüsü, feature graphic, kısa/uzun açıklama, kategorilendirme için repo içinde organize bir kaynak (fastlane/metadata, docs/store) bulunamadı | `find . -iname "*fastlane*"` sıfır sonuç; `find . -maxdepth 3 -iname "*store*"` sadece keystore dosyalarını döndürdü; repo kökündeki `screen_*.png` (10+ dosya, ör. `screen_final_verified7.png`) QA/geliştirme amaçlı ad-hoc ekran görüntüleri, küratörlü store screenshot seti değil | Play Store listing için ayrı bir `docs/store/` veya `fastlane/metadata/android` klasörü açılıp gerçek cihaz screenshot'ları, feature graphic (1024x500) ve TR mağaza metni hazırlanmalı |
| P1 | `versionName = "0.1.0"`, `versionCode = 1` — proje henüz 1.0 öncesi | Bu tek başına engel değil ama "ilk beta için yayınlanabilir mi" sorusuna genel yayın değil, sınırlı/kapalı test cevabı doğru | `app/build.gradle.kts:51-52` | İlk internal/closed testing track'i için uygun; production'a geçişte semantik versiyon disiplinine (checklist zaten var) uyulmalı |
| P1 | Crash reporting prod'da fiilen kapalı (`google-services.json` yok) | Release checklist'te "Firebase Crashlytics içinde test crash event'inin düştüğünü doğrula" adımı (`release-checklist.md:91-92`) şu an sağlanamaz; ilk yayında crash görünürlüğü sıfır olur | `app/build.gradle.kts:16-19` (`if (file("google-services.json").exists())`), Glob sonucu dosya repoda yok | Play Console'a ilk yükleme öncesi Firebase projesi oluşturulup `google-services.json` eklenmeli, aksi halde ilk sürümde crash-free rate izlenemez |
| P2 | Analytics SDK'sı yalnızca yerel/Crashlytics seviyesinde; ayrı bir ürün analitik aracı (event funnel, retention) yok | Hangi ekranın/özelliğin kullanıldığını, premium dönüşüm huninin nasıl davranacağını ölçecek altyapı henüz yok | `app/build.gradle.kts` bağımlılıklarında Firebase Analytics/Amplitude/Mixpanel vb. yok, yalnızca `firebase-crashlytics` var | Monetizasyon kararı öncesi en azından temel bir ürün analitiği (opt-in, KVKK uyumlu) planlanmalı |
| P2 | `keystore.properties` içinde gerçek keystore şifresi düz metin olarak yerel dosyada duruyor (repoya girmiyor ama içerik hassas) | Düşük risk (dosya git-ignored ve izlenmiyor, `git ls-files` boş döndü) ama geliştirici makinesinde düz metin şifre saklama pratiği | `keystore.properties:1-4` (`storePassword=BenimGunlerim!2026`, `keyPassword=BenimGunlerim!2026`) | CI tarafında GitHub Secrets kullanılıyor olması (`external-release-setup.md`) iyi; yerel dosya için de bir password manager/env var pratiğine geçiş düşünülebilir |

## Dosya Bazlı Bulgular

### `app/build.gradle.kts`
- Bulgu: `applicationId = "com.benimgunlerim"`, `minSdk = 26`, `targetSdk = compileSdk = 35`, `versionCode = 1`, `versionName = "0.1.0"` (satır 44-52). Release build type `isMinifyEnabled = true`, `isShrinkResources = true`, `isProfileable = true` (satır 73-76). Hiçbir Billing/Ads kütüphanesi yok.
- Risk: Teknik release hazırlığı iyi durumda; monetizasyon altyapısı sıfır.
- Öneri: Premium karar verilmeden önce Billing kütüphanesi eklenmemeli — önce ücretsiz sürümü yayına hazırlamak önceliklendirilmeli.

### `app/src/main/java/com/benimgunlerim/ui/settings/SettingsScreen.kt`
- Bulgu: 341 satır, tamamı okundu. "Gizlilik" `SectionBlock`'u (satır 114-148) yalnızca onboarding linki, OSS lisansları dialog'u, versiyon bilgisi ve "Veri: Yerel" satırını gösteriyor. Export/import butonu, analytics toggle'ı, Privacy Policy linki yok.
- Risk: Backend'de tam bitmiş üç özellik (export, import, analytics toggle) kullanıcıya hiç ulaşmıyor — hem gizlilik hem "veri taşınabilirliği" beklentisi karşılanmıyor.
- Öneri: `SettingsViewModel`'in zaten sunduğu `exportDataToFile`, `importDataFromFileContent`, `setAnalyticsEnabled` (var olduğu 06 raporunda belirtilmiş, bu turda `SettingsViewModel.kt` importlarında `DataExportService`/`DataImportService` doğrulandı) fonksiyonlarını UI'a bağla.

### `app/src/main/java/com/benimgunlerim/ui/settings/OssLicensesScreen.kt`
- Bulgu: 151 satır. `LIBRARIES` sabit listesi (satır 36-73) 6 kütüphane içeriyor: Compose, Konfetti, Material Icons, Coroutines/Flow, Room, Hilt.
- Risk: `app/build.gradle.kts:239-264`'teki gerçek bağımlılıklarla (DataStore Preferences, WorkManager, Navigation Compose, Hilt Navigation Compose, Firebase Crashlytics/BOM) karşılaştırıldığında liste eksik; lisans yükümlülüğü tam karşılanmıyor olabilir.
- Öneri: Otomatik lisans üretim eklentisine geçilmeli veya liste her release'de manuel senkronize edilmeli.

### `app/src/main/java/com/benimgunlerim/ui/shop/ShopViewModel.kt`
- Bulgu: 130 satır. `ALL_SHOP_ITEMS` (satır 28-45) 12 kozmetik ürün tanımlıyor (rozet çerçeveleri, kutlama efektleri, renk aksanları, rapor modları), fiyatlar 60-150 sanal altın arası. `purchaseItem` ve `claimDailyReward` tamamen `UserPreferencesRepository` üzerinden yerel `gold` sayacıyla çalışıyor, hiçbir ağ/ödeme çağrısı yok.
- Risk: Şu an gerçek parayla hiçbir bağlantısı yok — bu iyi bir taban ama "gerçek para → gold" köprüsü kurulacaksa (örn. "gold paketi satın al") entitlement/receipt doğrulama katmanı sıfırdan tasarlanmalı.
- Öneri: Sanal ekonomi + kozmetik satış modeli korunabilir; gerçek para geçişinde Play Billing + sunucu tarafı (veya en azından signature) doğrulama şart, aksi halde sahte satın alma riski oluşur.

### `docs/release/release-checklist.md` ve `docs/production/external-release-setup.md`
- Bulgu: 130 ve 67 satır, tam okundu. Signing, versioning, quality gate, cihaz smoke test, backup/gizlilik kontrolü, Play Store akışı, post-release izleme, performans kapısı, supply-chain kontrolleri ve GitHub Secrets/branch protection/tag protection adımları detaylı belgelenmiş.
- Risk: Doküman kalitesi yüksek ama madde 5 ("Backup ve Gizlilik Kontrolü") ve madde 6.5 ("Data Safety formu privacy policy ile uyumlu") şu anki kod durumuyla (Privacy Policy yok) karşılanamıyor — checklist kendi kendine engel koyuyor, bu aslında olumlu bir tutarlılık işareti.
- Öneri: Checklist'i uygulamaya geçirmeden (yani Privacy Policy + export/import UI + analytics toggle olmadan) hiçbir track'e yükleme yapılmamalı; checklist zaten bunu doğru şekilde zorunlu kılıyor.

### `keystore.properties` / `app/proguard-rules.pro`
- Bulgu: Keystore bilgisi yerelde düz metin (`keystore.properties`, git-ignored, izlenmiyor); ProGuard kuralları 92 satır, kapsamlı keep-rule seti (Hilt, Room, WorkManager, Compose, domain modeller).
- Risk: Düşük — dosya repoya girmiyor. ProGuard seti minification'ın crash üretmeden çalışmasını destekliyor.
- Öneri: Ek işlem gerekmiyor; CI tarafında zaten GitHub Secrets kullanılıyor.

## Kullanıcı Deneyimi Etkisi
Mevcut kullanıcı hiçbir yerde para ile karşılaşmıyor — tüm oyunlaştırma (XP, seviye, altın, dükkan) tamamen ücretsiz ve kozmetik. Bu, ilk sürüm için kullanıcı güvenini koruyan iyi bir konumlandırma. Ancak aynı kullanıcı Ayarlar'da "gizlilik politikasını incele" cümlesini görüp tıklayacağı hiçbir yer bulamıyor, verisini dışa aktaramıyor ve analytics'i kapatamıyor — bu, "şeffaf ve kontrol bende" hissi vermesi gereken bir gizlilik-odaklı üründe (günlük/duygu takibi yapan bir app'te özellikle kritik) güven kırıcı bir boşluk. Brain Dump gibi hassas kişisel içerik üreten özelliklerin varlığı bu boşluğu daha da önemli kılıyor.

## Teknik Borç Etkisi
Export/import ve analytics-toggle "iş bitti ama teslim edilmedi" tipik entegrasyon borcu — ViewModel/Service katmanı tam, UI katmanı sıfır. OSS lisans listesinin build.gradle.kts ile manuel senkronizasyonu her yeni bağımlılıkta unutulmaya açık bir borç. Monetizasyon tarafında teknik borç değil, sıfırdan inşa gereken bir alan var: Billing entegrasyonu, entitlement modeli, receipt/purchase state yönetimi, restore-purchase akışı hiç yazılmamış — bu "borç" değil "henüz başlanmamış iş", ama planlama için bunun net şekilde ayrılması gerekiyor.

## Release / Monetizasyon Riski
Play Store Data Safety formu barındırılan bir Privacy Policy URL'si olmadan eksiksiz doldurulamaz; bu formun eksik/hatalı doldurulması store incelemesinde reddedilme veya sonradan askıya alınma riski taşır — ve bu risk yalnızca ücretli değil, tamamen ücretsiz bir sürüm için de geçerlidir. Monetizasyon tarafında risk daha temel: kod tabanında hiçbir IAP/Billing kütüphanesi yok, yani "ücretli/premium modele hazır mı" sorusunun cevabı net biçimde hayır. `versionName = "0.1.0"` ile proje kendi release checklist'ine göre henüz production değil, internal/closed testing aşamasına uygun. Sanal ekonomi (gold/shop) modelinin gerçek paraya bağlanması durumunda sunucu tarafı doğrulama olmadan istemci-taraflı entitlement sahtekarlığa açık olur — bu, ilerideki bir monetizasyon fazının mimari riski olarak şimdiden not edilmeli.

## Önceliklendirilmiş Yapılacaklar
### P0 — Yayın öncesi şart
- Barındırılan bir Privacy Policy sayfası yaz, `SettingsViewModel.kt:54`'teki ölü metni gerçek bir linke/ekrana bağla.
- Export/import UI'ını `SettingsScreen.kt`'ye ekleyip mevcut `SettingsViewModel` fonksiyonlarına bağla.
- "Anonim kullanım ölçümü" analytics toggle'ını Gizlilik bölümüne ekle.
- İlk Play Console yüklemesi öncesi bu üç madde tamamlanmadan hiçbir track'e (internal dahil) çıkma — Data Safety formu bunlarsız eksik kalır.

### P1 — Kısa vadede gerekli
- OSS lisans listesini gerçek bağımlılıklarla senkronize et veya otomatik araca geçir.
- `docs/store/` veya `fastlane/metadata/android` altında küratörlü store screenshot seti, feature graphic ve TR mağaza metni hazırla.
- Prod Firebase projesi oluşturup `google-services.json` ekleyerek Crashlytics'i aktifleştir (release checklist madde 6.5-6.6 zaten bunu şart koşuyor).
- Monetizasyon kararı netleşmeden önce hangi ürün analitiği (opt-in, KVKK uyumlu) kullanılacağını planla.

### P2 — Polish / ileri iyileştirme
- Gerçek para → gold köprüsü kurulacaksa Play Billing + entitlement/receipt doğrulama mimarisini ayrı bir teknik tasarım dokümanında planla.
- `keystore.properties` için yerel makinede düz metin şifre yerine bir secret-yönetim pratiğine geçmeyi değerlendir.
- Sanal ekonomi ürün kataloğunu (`ShopViewModel.kt:28-45`) premium/ücretsiz ayrımı netleşince gözden geçir.

## 1 Haftalık Düzeltme Planı
- Gün 1-2: Privacy Policy metnini yaz (veri türleri, saklama yeri, bulut yedeği, Crashlytics durumu, silme hakkı) ve Ayarlar'a bağla.
- Gün 3: Export/import butonlarını `SettingsScreen.kt`'ye ekle.
- Gün 4: Analytics toggle'ını Gizlilik bölümüne ekle.
- Gün 5: OSS lisans listesini gerçek bağımlılıklarla senkronize et; `google-services.json` için Firebase projesi kurulumunu başlat.

## 2 Haftalık Düzeltme Planı
- Hafta 1: Yukarıdaki P0 maddelerinin tamamı + Play Store Data Safety formu taslağının Privacy Policy ile birebir eşleştirilmesi.
- Hafta 2: Store listing varlıklarının (screenshot, feature graphic, TR açıklama metni) hazırlanması, Crashlytics'in prod'da doğrulanması, ve — yalnızca ürün kararı verilirse — Billing entegrasyonu için ayrı bir teknik tasarım dokümanının yazılması (bu turda kod yazılmadı, sadece plan önerilir).

## Final Karar
Şu anki haliyle uygulama **ücretsiz bir kapalı/internal test sürümü** için release mekanizması açısından hazıra yakın (signing, versioning, quality gate, staged rollout süreci belgelenmiş ve mantıklı), ancak **genel yayına (production) çıkmadan önce P0 gizlilik maddeleri** (Privacy Policy, export/import UI, analytics toggle) kapatılmalı — bunlar kod yazımı değil, var olan backend'i UI'a bağlama ve bir doküman yazma işi olduğundan 1 haftalık planla kapatılabilir. **Ücretli/premium modele geçiş için ise proje "beklet" aşamasında**: Billing kütüphanesi, entitlement modeli ve premium/ücretsiz özellik ayrımı sıfırdan tasarlanmalı; bu turda önerilen ücretsiz-kalması-gereken listedeki temel görev/rutin takibi, basit onboarding, temel nefes/reset zaten mevcut ve ücretsiz durumda — bu iyi bir başlangıç noktası, ama premium tarafında hiçbir teknik altyapı yok.

## Monetizasyon Hazırlık Matrisi
| Alan | Hazır mı? | Risk | Öneri |
|---|---|---|---|
| Play Store readiness (signing/versioning) | Kısmen — signing pipeline ve checklist hazır, `versionName=0.1.0` henüz 1.0 öncesi | Düşük-Orta | İlk sürümü internal/closed testing olarak çıkar, production'a checklist'e göre ilerle |
| Ürün konumlandırma | Kısmen — özellik seti (görev/rutin/XP/sakinleşme) net ama Play Store metni/değer önerisi dokümante edilmemiş | Orta | Store listing metni ve konumlandırma dokümanı yazılmalı |
| Freemium/premium sınırları | Hazır değil — kodda hiçbir premium/ücretsiz ayrımı yok, tüm özellikler açık | Orta | Bu audit'in önerdiği ücretsiz/premium listesi ürün kararına dönüştürülmeli |
| Subscription/IAP hazırlığı | Hazır değil — `app/build.gradle.kts` içinde Billing kütüphanesi yok | Yüksek | Karar netleşince `billing-ktx` + entitlement mimarisi sıfırdan kurulmalı |
| Premium özellik adayları | Kısmen değerlendirilebilir — Progress ekranında (`BarChart`, `MetricCard`) temel istatistik zaten var, "gelişmiş" versiyonu ayrıştırılabilir | Düşük | Gelişmiş istatistik/rapor modu (zaten Shop'ta "Detaylı Rapor" kozmetik ürünü var, `ShopViewModel.kt:44`) premium'a taşınabilir aday |
| Kullanıcıyı rahatsız etmeyen monetizasyon | Kısmen — mevcut Shop modeli (sanal altın, kozmetik ürün) zaten rahatsız etmiyor modelde | Düşük | Bu modeli koru; zorlayıcı reklam veya paywall-interrupt eklenmemeli |
| Privacy Policy | Hazır değil — doküman/ekran yok, kodda ölü referans var | Yüksek | P0 — hemen yazılıp bağlanmalı |
| OSS licenses screen | Kısmen — ekran var ama liste eksik/güncel değil | Düşük-Orta | Liste gerçek bağımlılıklarla senkronize edilmeli |
| Crash reporting | Kısmen — mimari hazır (PII-safe), ama `google-services.json` yok, prod'da inaktif | Orta | Yayın öncesi Firebase projesi kurulup aktifleştirilmeli |
| Analytics | Hazır değil — toggle kodda var, UI'da yok; ayrı bir ürün analitik SDK'sı da yok | Orta | Toggle'ı UI'a bağla; ürün analitiği ihtiyacı ayrıca planlanmalı |
| Release signing/versioning | Hazır — koşullu signing config, `verifyReleaseSigning` task'ı, checklist mevcut | Düşük | Ek işlem gerekmiyor |
| App size | Değerlendirilemedi — bu turda APK/AAB build edilmedi, gerçek boyut ölçülmedi | Bilinmiyor | Release APK/AAB build edilip boyut ölçülmeli, "bu alanda kanıt bulamadım" |
| Store screenshots/copy | Hazır değil — küratörlü store asset klasörü yok, root'taki `screen_*.png` dosyaları ad-hoc QA görüntüleri | Orta | `docs/store/` veya `fastlane/metadata` altında gerçek store varlıkları hazırlanmalı |
| Beta release stratejisi | Hazır — `release-checklist.md` madde 6 internal testing → staged rollout akışını net tanımlıyor | Düşük | Ek işlem gerekmiyor |
