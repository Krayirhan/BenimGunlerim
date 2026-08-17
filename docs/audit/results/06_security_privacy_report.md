# Audit Raporu — Güvenlik & Gizlilik

> ⚠️ **2026-08-17 güncellemesi:** Bu raporun 3 P0 maddesi de kapandı: Privacy Policy eklendi, export/import UI'a bağlandı, analytics toggle render ediliyor. Ayrıca **Firebase Crashlytics tamamen kaldırıldı** — raporun Crashlytics consent/aktivasyon önerileri artık geçersiz, hata kayıtları tamamen cihaz-içi. Güncel durum için bkz. [`INCREMENTAL_REAUDIT_2026-08-17.md`](INCREMENTAL_REAUDIT_2026-08-17.md). Bu doküman tarihsel kayıt olarak değiştirilmemiştir.

## Genel Puan
6 / 10

## Kısa Karar
Teknik temel (yerel-only veri, sanitize edilmiş crash reporting, opt-in analytics alt yapısı, güvenli sakinleşme dili) beklenenin üzerinde sağlam. Ancak kullanıcıya dönük gizlilik yüzeyi eksik: Privacy Policy dokümanı/ekranı hiç yok, tamamen inşa edilmiş veri dışa/içe aktarma (export/import) özelliği Ayarlar ekranına hiç bağlanmamış, "Anonim kullanım ölçümü" toggle'ı UI'da görünmüyor (kullanıcı analytics'i kapatamıyor), ve Ayarlar ekranındaki "Gizlilik politikasını inceleyin" referansı var olmayan bir dokümana işaret ediyor. Bu haliyle Play Store'a "beta" olarak çıkılabilir ama gizlilik/veri şeffaflığı P0 maddeleri kapatılmadan genel yayına (production) çıkılmamalı.

## En Güçlü 5 Taraf
1. Tüm kullanıcı verisi (görevler, rutinler, Brain Dump'tan türeyen görev başlıkları, günlük özetler, XP/level/başarım) yalnızca cihaz-içi Room veritabanında (`benim_gunlerim.db`) ve DataStore'da tutuluyor; kod tabanında herhangi bir network/HTTP çağrısı (Retrofit/OkHttp/Ktor) yok — `app/src/main/java` içinde network SDK'sı bulunamadı.
2. Crash reporting mimarisi PII sızıntısına karşı bilinçli tasarlanmış: `ErrorReporterContextPolicy.kt` (satır 6-16) sabit bir allow-list (`action, app_version, build_type, db_version, notification_permission, screen, theme, thread, type`) ile filtreliyor; görev başlığı, not, Brain Dump metni gibi hiçbir serbest metin bu kanaldan geçemiyor. `ErrorReporter.kt` (satır 11-15) dokümantasyonunda da açıkça "Never include PII (task titles, user names, etc.)" uyarısı var.
3. Debug build'lerde Crashlytics tamamen devre dışı — `AppModule.kt` satır 105-108: `if (BuildConfig.DEBUG) local else crashlytics`; ayrıca repoda `google-services.json` bulunmadığından (`Glob` sonucu: "No files found") release build'de bile Firebase projesi bağlı değil, `CrashlyticsErrorReporter.kt` satır 29'daki `FirebaseApp.getApps(context).isEmpty()` kontrolü sayesinde crash'ler sessizce hiçbir yere gitmiyor.
4. Sakinleşme/Reset metinleri (`1 Dakikalık Reset`, `Hafif Gün Modu`) tıbbi/terapötik iddia taşımıyor; tam tersine audit prompt'unun önerdiği "güvenli dil" kalıplarıyla neredeyse birebir örtüşüyor: `strings.xml:644` "Kısa bir duraklama iyi geldi mi?", `strings.xml:649` "Bugün biraz yoğun mu görünüyor?", `strings.xml:651` "Bugünü hafiflettik. İstersen kısa bir nefesle devam edebilirsin." Hiçbir yerde "kaygını geçirir/tedavi eder/panik atağını durdurur" taraması eşleşme bulmadı.
5. Bildirim izni (POST_NOTIFICATIONS) uygulama açılışında değil, onboarding akışının sonunda bağlamsal olarak isteniyor (`OnboardingScreen.kt` satır 191-197, 564-577) ve kullanıcıya "Şimdilik Geç" (`onboarding_notif_later`) seçeneği sunuluyor — zorlayıcı değil, Android best-practice'e uygun.

## En Kritik 10 Sorun
| Öncelik | Sorun | Etki | Kanıt/Dosya | Öneri |
|---|---|---|---|---|
| P0 | Privacy Policy dokümanı/ekranı yok ama Ayarlar kodunda referans veriliyor | Play Store Data Safety formu ve store listing zorunlu kılar; mevcut haliyle yayın engeli | `SettingsViewModel.kt:54` — `"...Gizlilik politikasını inceleyin."` metni hiçbir linke/ekrana bağlı değil; `strings.xml` içinde "privacy policy/gizlilik politikası" için ayrı bir string veya URL yok | Play Store'a girmeden önce barındırılan bir Privacy Policy sayfası yazılıp Ayarlar'a link/WebView olarak eklenmeli |
| P0 | Tamamen kodlanmış veri export/import özelliği UI'a hiç bağlanmamış | Kullanıcı "verimi indirebilir miyim" sorusuna hiçbir arayüzden cevap alamıyor; GDPR/KVKK "veri taşınabilirliği" beklentisi karşılanmıyor | `SettingsViewModel.kt:98-148` (`exportData`, `exportDataToFile`, `requestImportFromFile`, `importData`, `SettingsUiEffect`) tanımlı; `SettingsScreen.kt` içinde `export`/`import` string'i için grep sıfır sonuç döndü | Ayarlar ekranına "Verilerimi dışa aktar / içe aktar" satırları eklenip mevcut `uiEffects` (SaveExportJson/RequestImportJson) dinlenmeli |
| P0 | "Anonim kullanım ölçümü" toggle'ı tanımlı ama ekranda render edilmiyor | `analyticsEnabled` varsayılan olarak `true` (`UserPreferencesRepository.kt:23,119`) ve kullanıcı bunu kapatacak hiçbir UI kontrolüne erişemiyor | `strings.xml:422-425` (`settings_privacy_title`, `settings_privacy_pii_note`) ve `SettingsViewModel.kt:78-80` (`setAnalyticsEnabled`) mevcut ama `SettingsScreen.kt` "Gizlilik" `SectionBlock`'unda (satır 114-148) bu switch hiç yok — sadece Onboarding, OSS lisansları, versiyon ve "veri local" bilgisi var | `SettingsToggleRow` ile analytics switch'i "Gizlilik" bölümüne eklenmeli |
| P1 | `allowBackup=true` ile Room DB + DataStore tamamen Android bulut yedeğine dahil, kullanıcıya bu konuda açık onay/bilgi verilmiyor | Brain Dump'tan türeyen kişisel görev metinleri kullanıcının Google hesabına (device-to-device transfer dahil) yedekleniyor; kullanıcı bunu bilmiyor | `AndroidManifest.xml:9-11`, `data_extraction_rules.xml:6-12` (yorum satırında bilinçli ürün kararı olarak belirtilmiş: "user-created local productivity data can be included in Android cloud backup") | Bu davranış Privacy Policy'de açıkça belirtilmeli; opsiyonel olarak Ayarlar'da "Bulut yedeğine dahil et" toggle'ı düşünülmeli |
| P1 | OSS lisans listesi statik/manuel ve eksik olabilir; gerçek bağımlılıklarla senkron değil garanti edilmiyor | Hilt, Room, Compose, Konfetti listeleniyor ama AndroidX DataStore, WorkManager, Firebase Crashlytics gibi gerçek runtime bağımlılıkları listede yok | `OssLicensesScreen.kt:36-73` (`LIBRARIES` sabit listesi) vs `app/build.gradle.kts:239-251` (`datastore.preferences`, `work.runtime.ktx`, `firebase.crashlytics` bağımlılıkları listede yok) | Google'ın `oss-licenses-plugin`'i veya otomatik üretilen lisans listesi kullanılmalı |
| P1 | Brain Dump serbest metni hiç sanitize/uyarı olmadan doğrudan görev başlığı olarak DB'ye yazılıyor | Kullanıcı ruh sağlığı, ilişki, sağlık gibi hassas içerik yazabilir (`braindump_hint`) ve bu metin süresiz saklanıyor; export/silme özelliği kullanıcıya ulaşmadığından (bkz. P0) veri üzerinde kontrol hissi zayıf | `BrainDumpDialog.kt:69,268` (`onAddTasks: (List<String>) -> Unit`, `textInput` sınırı sadece 3000 karakter) — herhangi bir içerik filtresi/uyarı yok | Metnin yalnızca cihazda tutulduğu ve dilediği zaman silinebileceği bilgisi diyalogda/onboarding'de kısa bir notla belirtilmeli |
| P1 | Crash reporting prod'da aktif hale getirildiğinde (google-services.json eklenince) kullanıcıya bilgilendirme/consent akışı yok | Crashlytics varsayılan olarak cihaz/OS/IP gibi meta verileri toplar; şu an kod içinde bunun kullanıcıya bildirildiği bir onay ekranı yok | `AppModule.kt:105-108`, `CrashlyticsErrorReporter.kt` — koşulsuz olarak `else crashlytics` seçiliyor, kullanıcı onayı sorgulanmıyor | Privacy Policy'de crash reporting açıkça belirtilmeli; ileri aşamada ilk açılışta kısa bir "tanılama verisi" bilgilendirmesi eklenebilir |
| P2 | `LocalErrorReporter` cihaz üstünde sınırsız süreyle (uygulama ömrü boyunca) son 20 hata kaydını SharedPreferences'ta tutuyor, kullanıcıya bu kayıtları görüntüleme/silme arayüzü yok | Düşük risk (veri cihazda kalıyor, PII filtreli) ama şeffaflık eksik; "Tüm Verileri Sil" akışı bu `local_error_reports` SharedPreferences dosyasını temizliyor mu belirsiz | `LocalErrorReporter.kt:11-13,43-62` (`PREFS_NAME = "local_error_reports"`) — `LocalDataClearer` sınıfının bu prefs dosyasını temizleyip temizlemediği doğrulanmadı (kod okunmadı) | `LocalDataClearer.clearAllLocalData()` içeriği doğrulanıp bu SharedPreferences de temizlik kapsamına alınmalı |
| P2 | Açık kaynak lisans ekranı yalnızca statik metin; tıklanabilir link/URL yok | Kullanıcı lisans metninin tam sürümüne ulaşamıyor | `OssLicensesScreen.kt:29-34` (`OpenSourceLibrary` veri sınıfında `url` alanı yok) | Her kütüphane için lisans metni URL'si eklenmeli |
| P2 | `TimeChangeReceiver` ve `BootReceiver` `exported="true"` (sistem broadcast'leri için gerekli olsa da) `android:permission` ile korunmuyor | Düşük risk — sadece sistem broadcast action'larına (`BOOT_COMPLETED`, `TIMEZONE_CHANGED` vb.) tepki veriyor, dışarıdan tetiklenebilir intent payload'ı işlemiyor gibi görünüyor (receiver içerikleri bu turda derinlemesine incelenmedi) | `AndroidManifest.xml:42-58` | Receiver implementasyonlarının gelen `Intent.action`'ı sıkı doğruladığından emin olunmalı (bu turda kod içeriği doğrulanmadı) |

## Dosya Bazlı Bulgular

### `app/src/main/AndroidManifest.xml`
- Bulgu: `INTERNET` izni tanımlı (satır 2) ama kod tabanında hiçbir network çağrısı kanıtı yok (Firebase Crashlytics SDK'sının kendi network erişimi için gerekli olabilir). `allowBackup="true"` + `dataExtractionRules`/`fullBackupContent` ile Room DB ve DataStore tam bulut yedeğine dahil.
- Risk: Kullanıcı, kişisel görev/Brain Dump verisinin Google hesabına yedeklendiğini bilmeden bu davranışa maruz kalıyor.
- Öneri: Privacy Policy'de açıkça belirt; INTERNET izninin sadece Crashlytics için olduğunu store listing'de not düş.

### `app/src/main/java/com/benimgunlerim/analytics/ErrorReporterContextPolicy.kt`
- Bulgu: Allow-list tabanlı sanitizasyon (satır 6-16) sağlam ve merkezi; hem `CrashlyticsErrorReporter` hem `LocalErrorReporter` bunu kullanıyor.
- Risk: Düşük — allow-list'e yanlışlıkla PII içeren bir anahtar (örn. `"note"`, `"title"`) eklenirse koruma kırılır; bunu önleyen bir test yok.
- Öneri: `ErrorReporterContextPolicyTest` gibi bir birim testiyle allow-list'in yanlışlıkla genişletilmesine karşı regresyon koruması eklenebilir.

### `app/src/main/java/com/benimgunlerim/ui/settings/SettingsScreen.kt`
- Bulgu: "Gizlilik" `SectionBlock`'u (satır 114-148) yalnızca Onboarding, OSS lisansları, sürüm bilgisi ve "veri local" satırlarını gösteriyor; analytics toggle'ı, export/import satırları ve Privacy Policy linki yok.
- Risk: Kullanıcı gizlilik kontrolüne (analytics kapatma, veri indirme) arayüzden hiç erişemiyor — bu, arka planda inşa edilmiş özelliklerin tamamen "ölü kod" olarak kalmasına yol açıyor.
- Öneri: `SettingsViewModel`'in zaten sunduğu `setAnalyticsEnabled`, `exportDataToFile`, `requestImportFromFile`, `uiEffects` akışlarını bu ekrana bağla.

### `app/src/main/java/com/benimgunlerim/ui/components/calm/BrainDumpDialog.kt`
- Bulgu: Serbest metin girişi (`textInput`, satır 73, 3000 karakter sınırı) doğrudan satır bazında ayrıştırılıp (`onAddTasks`) görev başlığına dönüştürülüyor; hiçbir içerik uyarısı/disclaimer yok.
- Risk: Kullanıcı hassas kişisel bilgi (sağlık, ilişki, iş) yazabilir; bu bilgi süresiz yerel DB'de kalır ve (yukarıdaki P1 bulgusu nedeniyle) cihaz bulut yedeğine dahil olur.
- Öneri: Diyalog altına küçük bir "Bu notlar sadece cihazında saklanır" notu eklenebilir; export/silme özellikleri UI'a bağlandığında bu his güçlenir.

### `app/src/main/res/xml/data_extraction_rules.xml` ve `full_backup_content.xml`
- Bulgu: Yorum satırı (satır 2-13) bilinçli bir ürün kararını belgeliyor — Room DB ve DataStore bulut yedeğine dahil, sadece `reminder_policy_cache` hariç tutulmuş.
- Risk: Karar dokümante edilmiş olsa da bu bilgi kullanıcıya hiçbir kanalda (Privacy Policy, Ayarlar) yansıtılmıyor.
- Öneri: Privacy Policy yazılırken bu dosyadaki karar birebir referans alınmalı.

### `app/src/main/res/values/strings.xml`
- Bulgu: Sakinleşme/Reset metinleri (satır 638-680) tıbbi iddia içermiyor; ölçülü ve güvenli dil kullanılmış (`reset_completed_title`, `reset_contextual_body` vb.).
- Risk: Yok — bu bir güçlü yön.
- Öneri: Gelecekte yeni sakinleşme metni eklenirken bu ton (öneri/duraklama dili, "tedavi/geçirir" değil) korunmalı; bir string-review checklist'i CI'a eklenebilir.

### `app/src/main/java/com/benimgunlerim/ui/settings/OssLicensesScreen.kt`
- Bulgu: 6 kütüphane manuel olarak listelenmiş (satır 36-73); DataStore, WorkManager, Firebase Crashlytics gibi gerçek bağımlılıklar eksik.
- Risk: Play Store politikası ve lisans şartları (bazı lisanslar atıf zorunluluğu taşır) tam karşılanmıyor olabilir.
- Öneri: Otomatik lisans üretim aracına geçilmeli.

## Kullanıcı Deneyimi Etkisi
Mevcut kullanıcı, verisinin nerede tutulduğuna dair Ayarlar'da yalnızca "Veri: Yerel" (`settings_data_local`) gibi kısa bir bilgiyle karşılaşıyor; ne bir Privacy Policy'ye ne de export/import'a erişebiliyor. Bu, özellikle Brain Dump gibi kişisel içerik yazan kullanıcılarda "verim nereye gidiyor, silebilir miyim, yedekleyebilir miyim" sorularına cevapsız kalıyor — güven inşası açısından zayıf bir nokta. "Tüm Verileri Sil" akışı (`SettingsScreen.kt:150-183`) var ve çalışır durumda, bu olumlu; ama tek yönlü (geri dönüşü olmayan silme) ve öncesinde export önerilmiyor.

## Teknik Borç Etkisi
Export/import ve analytics-toggle özellikleri backend/ViewModel katmanında tam bitmiş durumda ama UI katmanına hiç bağlanmamış — bu, "iş bitti ama teslim edilmedi" tipik bir entegrasyon borcu. `OssLicensesScreen` statik listesinin gerçek bağımlılıklarla senkron tutulması manuel bir süreç ve build.gradle.kts değiştikçe kolayca eskiyecek. `ErrorReporterContextPolicy` allow-list'i regresyon testi olmadan genişletilebilir durumda; bu ileride sessizce bir PII sızıntısına dönüşebilir.

## Release / Monetizasyon Riski
Play Store Data Safety formu (zorunlu) uygulamanın hangi veri kategorilerini topladığını, sildirilebilir olup olmadığını ve şifreleme durumunu beyan etmeyi gerektirir; şu an barındırılan bir Privacy Policy URL'si olmadan bu form eksiksiz doldurulamaz ve store inceleme sürecinde reddedilme riski var. Firebase Crashlytics bağımlılığı `google-services.json` eklendiği an aktifleşecek şekilde koda gömülü; bu adım atıldığında Privacy Policy'nin crash/tanılama verisi toplamayı da kapsaması gerekecek. Mevcut haliyle (google-services.json yok) crash reporting üretimde çalışmıyor — bu da prod'da hata görünürlüğünü sıfırlıyor, ayrı bir operasyonel risk.

## Önceliklendirilmiş Yapılacaklar
### P0 — Yayın öncesi şart
- Barındırılan bir Privacy Policy sayfası yazıp Ayarlar'a bağla; `SettingsViewModel.kt:54`'teki referansı gerçek bir linke dönüştür.
- Export/import UI'ını `SettingsScreen.kt`'ye ekleyip mevcut `SettingsViewModel` fonksiyonlarına (`exportDataToFile`, `requestImportFromFile`, `uiEffects`) bağla.
- "Anonim kullanım ölçümü" analytics toggle'ını Gizlilik bölümüne ekle; kullanıcı `analyticsEnabled`'ı kapatabilsin.

### P1 — Kısa vadede gerekli
- `allowBackup`/bulut yedeği davranışını Privacy Policy'de ve mümkünse Ayarlar'da açıkça belirt.
- Brain Dump diyaloguna kısa bir "sadece cihazında saklanır" notu ekle.
- `google-services.json` eklenip Crashlytics prod'da aktifleştirilmeden önce kullanıcı bilgilendirmesini Privacy Policy'ye işle.
- `LocalDataClearer.clearAllLocalData()` içeriğini doğrulayıp `local_error_reports` SharedPreferences'ının da temizlik kapsamında olduğunu garanti et.

### P2 — Polish / ileri iyileştirme
- OSS lisans listesini otomatik üretim aracına (`oss-licenses-plugin` vb.) geçir.
- `ErrorReporterContextPolicy` allow-list'i için regresyon testi ekle.
- `TimeChangeReceiver`/`BootReceiver` intent-handling kodunu ayrı bir security-review turunda doğrula.

## 1 Haftalık Düzeltme Planı
- Gün 1-2: Privacy Policy metnini yaz (veri türleri, saklama yeri, bulut yedeği, Crashlytics durumu, silme hakkı) ve statik bir sayfa/WebView olarak Ayarlar'a ekle.
- Gün 3: Export/import butonlarını `SettingsScreen.kt`'ye ekle, `uiEffects` (SAF file picker) akışını `SettingsScreen`'de dinle.
- Gün 4: Analytics toggle'ını Gizlilik bölümüne ekle, `settings_privacy_pii_note` string'ini kullan.
- Gün 5: Brain Dump diyaloguna gizlilik notu ekle; `LocalDataClearer` kapsamını doğrula.

## 2 Haftalık Düzeltme Planı
- Hafta 1: Yukarıdaki P0 maddelerinin tamamı + Play Store Data Safety formunun taslağının Privacy Policy ile birebir eşleştirilmesi.
- Hafta 2: P1 maddeleri (bulut yedeği şeffaflığı, Crashlytics consent notu, SharedPreferences temizlik doğrulaması) + OSS lisans listesinin otomatikleştirilmesi + allow-list regresyon testi.

## Final Karar
Beta/kapalı test için mevcut haliyle ilerlenebilir çünkü teknik altyapı (yerel-only veri, PII-filtreli crash reporting, güvenli sakinleşme dili) sağlam. Ancak genel yayın (Play Store production) öncesi P0 maddeleri — Privacy Policy, export/import UI bağlantısı, analytics toggle'ının görünür olması — kapatılmadan mağazaya çıkılmamalı; bunlar kod yazımı değil, var olan backend'i UI'a bağlama ve bir doküman yazma işi olduğundan 1 haftalık planla kapatılabilir kapsamda.

## Privacy Risk Matrix
| Veri Türü | Nerede Tutuluyor | Hassasiyet | Risk | Öneri |
|---|---|---:|---|---|
| Brain Dump serbest metni (görev başlığına dönüşen) | Room DB (`benim_gunlerim.db`, `TaskEntity`), cihaz-içi + Android bulut yedeği | Yüksek (kişisel/duygusal içerik olabilir) | Orta — 3. taraf paylaşımı yok, ama kullanıcıya bulut yedeği ve silme/export imkanı hakkında bilgi verilmiyor | Privacy Policy'de belirt; export/silme UI'ını bağla |
| Görev/rutin başlıkları ve zamanlamaları | Room DB, cihaz-içi + bulut yedeği | Orta | Düşük-Orta — aynı yedekleme şeffaflık sorunu | Aynı |
| Ruh hali / enerji check-in (`mood`, `energy`) | Room DB (`DailyStateEntity`), cihaz-içi + bulut yedeği | Yüksek (dolaylı sağlık/duygu durumu verisi) | Orta — Play Store "sensitive info" kategorisine girebilir, Data Safety formunda ayrıca beyan gerektirir | Data Safety formunda "sağlık ve fitness" veya "kişisel bilgiler" kategorisi altında beyan et |
| XP / Level / Başarım verisi | Room DB + DataStore, cihaz-içi + bulut yedeği | Düşük | Düşük | Ek işlem gerekmiyor |
| Kullanıcı tercihleri (tema, bildirim saatleri, analytics açık/kapalı) | DataStore, cihaz-içi + bulut yedeği | Düşük | Düşük | Ek işlem gerekmiyor |
| Crash/hata kayıtları (sanitize edilmiş) | Cihaz-içi SharedPreferences (`local_error_reports`, debug) / Firebase Crashlytics (release, şu an inaktif — `google-services.json` yok) | Düşük (allow-list ile PII filtrelenmiş) | Düşük — ama Crashlytics aktifleştiğinde cihaz/OS/IP gibi meta veri 3. tarafa (Google) gidecek | Crashlytics aktifleştirilmeden önce Privacy Policy'ye ekle |
| Analytics eventleri | Yalnızca Logcat (debug), hiçbir yere gönderilmiyor (`LocalAnalyticsTracker.kt`) | Düşük | Düşük — ama kullanıcı bunu Ayarlar'dan kapatamıyor (UI eksik) | Toggle'ı UI'a bağla |
| Uygulama içi hata mesajları / context anahtarları | Allow-list ile sınırlı (`action, screen, theme, type` vb.) | Düşük | Düşük | Allow-list'e yeni anahtar eklerken review zorunlu kılınmalı |
