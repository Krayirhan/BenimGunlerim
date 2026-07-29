# Production Readiness Planı

> Güncel proje ve sprint durumu için önce [`docs/PROJECT_STATUS.md`](../PROJECT_STATUS.md) okunmalıdır. Bu dosya production kalite checklist'idir; tarihsel audit sonuçları bugünkü durum yerine geçmez.

Bu plan production hazırlığını takip etmek için yaşayan kontrol listesidir.

## Build & Release

- [x] Release build için R8 ve resource shrink açık.
- [x] Lokal `verifyReleaseSigning` görevi var.
- [x] CI release job `verifyReleaseSigning` çalıştırır.
- [x] CI signed AAB artifact upload eder.
- [ ] GitHub secret'ları tanımlı: `KEYSTORE_BASE64`, `KEYSTORE_PASSWORD`, `KEY_ALIAS`, `KEY_PASSWORD`.
- [ ] `versionCode`/`versionName` release öncesi güncellenir.
- [ ] Play Console internal testing release'i signed AAB ile doğrulanır.

## CI/CD ve Kalite Kapıları

- [x] PR kalite kapısı: unit test, debug lint, debug build.
- [x] Release kalite kapısı: signing, unit test, release lint, release APK, release AAB.
- [x] JaCoCo unit test coverage kapısı eklendi.
- [x] Connected UI test job'u CI'a eklendi.
- [x] Dış release hazırlığı kontrol scripti eklendi.
- [ ] Branch protection GitHub üzerinde etkinleştirilir.
- [ ] Release tag koruması GitHub üzerinde etkinleştirilir.

## Veri ve Migration

- [x] Room schema export açık.
- [x] Destructive migration kullanılmıyor.
- [x] v6 -> v7 migration testi var.
- [x] Export alt görevler ve başarımlar dahil tüm ana yerel veri tiplerini kapsar.
- [x] JSON import/restore servisi eklendi.
- [x] Import servisi DB değişikliklerini transaction içinde yapar.
- [x] Import/export temel unit testleri eklendi.
- [x] Import/restore kullanıcı arayüzüne bağlandı.
- [x] Export -> import round-trip unit testi eklendi.
- [ ] Tüm yayınlanmış eski sürümlerden güncel sürüme migration matrisi tamamlanır.
- [ ] Export/import tam round-trip instrumentation testi eklenir.

## Bildirim ve Background İşleri

- [x] App açılışında reminder restore çalışır.
- [x] Boot sonrasında task, routine ve daily reminder restore edilir.
- [x] Notification permission merkezi policy ile kontrol edilir.
- [ ] Timezone/date change senaryosu değerlendirilir.
- [ ] OEM battery optimization manuel test matrisi tamamlanır.

## Güvenlik ve Gizlilik

- [x] Backup kapsamı teknik dokümanda açıklandı.
- [x] Release checklist privacy/backup kontrolü içerir.
- [ ] Kullanıcıya açık privacy policy hazırlanır.
- [ ] Analytics consent ürün kararı kesinleştirilir.
- [ ] Error reporting context whitelist'i gözden geçirilir.

## Test Stratejisi

- [x] Unit test kapısı var.
- [x] Lint kapısı var.
- [x] Connected UI test kapısı var.
- [x] Settings ekranı ve yerel veri aksiyonları için UI smoke testi eklendi.
- [x] Coverage raporu ve minimum eşik eklendi.
- [ ] ViewModel test kapsamı genişletilir.
- [ ] Notification restore instrumentation testi genişletilir.
- [ ] Accessibility testleri eklenir.

## UX, Lokalizasyon ve Erişilebilirlik

- [x] README, production docs, release docs ve ana kaynaklarda mojibake taraması temiz.
- [ ] Tüm kullanıcıya görünen metinler `strings.xml` altında doğrulanır.
- [ ] TalkBack label kontrolü yapılır.
- [ ] Font scaling ve touch target kontrolü yapılır.
- [ ] Tablet/landscape smoke test yapılır.

## Performans ve Operasyon

- [ ] Startup süresi ölçülür.
- [ ] Baseline Profile eklenir.
- [ ] Büyük veri senaryosu test edilir.
- [ ] Crash/ANR reporting kararı uygulanır.
- [x] Uncaught exception hook `ErrorReporter` altyapısına bağlandı.
- [ ] Play Console vitals release sonrası izlenir.
