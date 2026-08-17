# Incremental Re-Audit Report — 2026-08-17

> Bu rapor `docs/audit/99_INCREMENTAL_REAUDIT_PROMPT.md` sürecine göre üretilmiştir. Eski raporlar (`01`-`10`) **yeniden yazılmamıştır** — bu, onların üzerine eklenen bir delta/milestone raporudur. Değişikliklerin doğrulaması, kodu yazan ve her adımda derleme + tam unit test paketiyle test eden aynı oturum tarafından yapıldığından, ayrıca bir "keşif" turu gerekmedi.

## İncelenen Değişiklik Aralığı

- Commit aralığı: `ed84a2b` (audit paketinin eklendiği commit) → `e925169` (HEAD)
- Commit sayısı: 5
- Değişen dosya sayısı: 106 (standart incremental re-audit sınırı olan 40'ın üzerinde — bu rapor **milestone-seviyesi** bir güncelleme olarak ele alınmıştır, bkz. `99_INCREMENTAL_REAUDIT_PROMPT.md` §10)
- Referans alınan eski raporlar: `01`–`10`'un tamamı (bu oturumda zaten üretilen değişikliklerin doğrudan sonucu olduğu için ek okuma maliyeti olmadı)

## Kapsam Kararı

Bu re-audit şu alanları kapsadı:

- [x] Product / UX
- [x] Frontend / Compose
- [x] State / ViewModel
- [x] Data / Database
- [x] Backend / Sync
- [x] Security / Privacy
- [x] Performance
- [x] Testing / QA
- [x] Monetization / Release

Kapsam dışında bırakılan alanlar: yok — milestone kapsamı geniş olduğu için tüm alanlar tarandı.

## Kapanan Eski Riskler

| Eski Risk | Öncelik | Kaynak Rapor | Durum | Kanıt |
|---|---:|---|---|---|
| Unit test derlemesi başarısız (constructor imza uyuşmazlıkları, eksik `FakePrefs.setLightDayMode`) | P0 | 08 | Kapandı | `compileDebugUnitTestKotlin`/`testDebugUnitTest` BUILD SUCCESSFUL, 50 test sınıfı / 0 hata |
| Brain Dump satır ayrıştırma test edilemez, saf fonksiyon değil | P0 | 08 | Kapandı | `domain/BrainDumpParser.kt` + `BrainDumpParserTest.kt` |
| Hafif Gün Modu günlük sıfırlama test edilmemiş | P0 | 08 | Kapandı | `domain/LightDayMode.kt` + `LightDayModeTest.kt` |
| `UserPreferencesRepository` için hiç test yok | P0 | 08 | Kapandı | `androidTest/.../UserPreferencesRepositoryTest.kt` |
| Room migration geçmişi eksik (`fallbackToDestructiveMigration` yok, v1-6 planlanmamış) | P0 | 04 | Kapandı | `Migrations.kt` üstündeki politika dokümanı + `fallbackToDestructiveMigrationFrom`; v1-6 bilinçli olarak "pre-release, desteklenmez" kararı yazıldı |
| Görev silme/geri alma akışında alt görevler kalıcı kayboluyor | P0 | 04 | Kapandı | `DeleteTaskUseCase`/`RestoreTaskUseCase` subtask snapshot+restore, `TaskDeleteRestoreIntegrationTest.kt` |
| Brain Dump toplu ekleme transaction'da değil | P1 | 04, 07 | Kapandı | `AddTasksBatchUseCase` + `DatabaseTransactionRunner`, `AddTasksBatchUseCaseTest.kt` |
| Export/Import `lightDayModeDate` alanını taşımıyor | P1 | 04 | Kapandı | `DataExportService`/`DataImportService` + roundtrip testleri |
| Privacy Policy dokümanı/ekranı yok | P0 | 06, 09 | Kapandı | `PrivacyPolicyScreen.kt` + `docs/privacy.html`, Ayarlar'a bağlandı |
| Export/import UI'a bağlı değil | P0 | 05, 06, 09 | Kapandı | `SettingsScreen.kt` export/import butonları + dosya seçici |
| "Anonim kullanım ölçümü" toggle'ı render edilmiyor | P0 | 06, 09 | Kapandı | `SettingsScreen.kt` analytics `Switch` |
| OSS lisans listesi gerçek bağımlılıklarla senkron değil (DataStore, WorkManager, Navigation, Lifecycle eksik) | P1 | 06, 09 | Kapandı | `OssLicensesScreen.kt` 10 kütüphaneye güncellendi |
| `completion_logs.observeAll()` sınırsız tam tablo taraması | P0 | 07 | Kapandı | `ObserveTodaySnapshotUseCase`/`ObserveProgressSnapshotUseCase` artık `observeBetween(fromDate, today)` kullanıyor |
| Today'de `LazyColumn` yok | P0 | 07 | **Kapandı (farklı bir yoldan)** — bkz. not aşağıda | `TodayScreen.kt`/`PlanScreen.kt` artık dış `LazyColumn` içinde `item{}` kullanıyor; iç container'ların `LazyColumn`'a çevrilmesi mimari olarak yanlış olurdu (nested-scrollable crash riski) |
| Rutin listesinde hedef/sayaç tipi rutinler için artır/azalt kontrolü yok | P0 | 01 | Kapandı | `RoutineRow.kt` + `TodayListContainers.kt`'de `targetType == "goal"` için +/- `IconButton` |
| Gün kapatma formu (`CloseDaySheet`) `rememberSaveable` değil | P0 | 03 | Kapandı | Tüm alanlar `rememberSaveable`'a taşındı |
| `TodayScreen`'deki 11 dialog/sheet state'i `rememberSaveable` değil | P0 | 03 | Kapandı | `TodayScreen.kt`'de `remember{mutableStateOf}` → `MutableState` + `rememberSaveable` deseni |
| Başarım/ödül kutlama event pipeline'ı yalnızca `TodayViewModel` yaşam döngüsüne bağlı (`replay=0`) | P0 | 03 | Kapandı | `AppEventCoordinator` (app-level `@Singleton`) + `AppEventCoordinatorTest.kt` |
| Ölü kod: `PlanTaskListComponents.kt`, `CelebrationSystem.kt`, `BenimGunlerimRepository_BACKUP.kt.bak` | P1 | 01, 04 | Kapandı | Üçü de silindi |
| `TodayScreen.kt`/`TodayViewModel.kt`/`TodaySheets.kt` 200 satır kuralının 3-4,5 katı üzerinde | P0 | 01, 02, 03 | Kapandı | `TodayScreen.kt` 536→133; `TodayViewModel.kt` 743→397 (`TodayTaskActions`/`RoutineActions`/`DayCloseActions`'a delege); `TodaySheets.kt` silinip 13 dosyaya bölündü (`TaskDetailSheet.kt` 199, `CloseDaySheet.kt` 137 dahil) |
| Hardcoded dp/renk/string sistematik ihlal (`AppTopBar`, `AddTaskSheet`, `AchievementsScreen`, `OssLicensesScreen`) | P1 | 01, 02 | Kapandı (4 dosya) | Bu dosyalar temizlendi + yeni `detekt-rules` modülü (`NoHardcodedDpRule`, `NoHardcodedColorRule`) CI'a bağlandı |
| `@Preview` kapsamı ~sıfır (79 dosyada 1 örnek) | P1 | 02 | Kısmen kapandı | `TaskRow`/`RoutineRow` için light/dark × boş/dolu 4'er `@Preview` eklendi; diğer organizmalar hâlâ kapsam dışı |
| JaCoCo eşiği domain/data için paket bazlı ayrılmamış | — (1 aylık plan maddesi, final scorecard) | 10 | Kapandı | `app/build.gradle.kts` domain (≥0.55) / data (≥0.50) LINE kuralları eklendi |

## Açık Kalan Eski Riskler

| Eski Risk | Öncelik | Kaynak Rapor | Durum | Neden Kapanmadı |
|---|---:|---|---|---|
| "Kutlama efektleri" ayarı (`celebrationEffectsEnabled`) hiçbir kutlama tetikleyicisinde okunmuyor — plasebo | P0 | 01, 02 | Açık | Bu turda dokunulmadı; `AppEventCoordinator`/`CelebrationModals.kt` hâlâ ayarı kontrol etmiyor |
| Ana ekranlar arası ortak iskelet tutarsız (`RoutineDetailScreen`/`OssLicensesScreen` kendi `Scaffold`'unu çiziyor, Başarımlar/Dükkan bottom nav'da yok) | P0 | 01 | Açık | Dokunulmadı |
| Rutin arşivleme onaysız, geri alınamaz | P1 | 01 | Açık | Dokunulmadı |
| `AddTaskSheet`/`AddRoutineSheet`'te kategori/saat/hatırlatıcı/hedef alanları UI'da yok (callback imzasında var) | P1 | 01 | Açık | Dokunulmadı |
| `toggleTask`/`toggleRoutine` için görev-id bazlı in-flight guard/mutex yok (çift-tıklama race condition) | P1 | 03 | Açık | `TodayTaskActions`/`TodayRoutineActions`'a taşındı ama davranış aynı — guard eklenmedi |
| `ToggleRoutineUseCase` `DatabaseTransactionRunner` kullanmıyor (ToggleTaskUseCase'in aksine) | P1 | 04 | Açık | Dokunulmadı |
| Soft-delete/trash stratejisi yok, tüm silmeler kalıcı | P1 | 03, 04 | Açık | Bilinçli olarak 1 aylık ufka bırakıldı |
| İki paralel one-shot event modeli (`Shop`/`Settings`'te `StateFlow<String?>` vs `Today`/`Plan`'da `SharedFlow`) | P2 | 03 | Açık | Dokunulmadı |
| Kullanılmayan effect payload'ları (`"task_moved_tomorrow"` gibi ham string sabitler) | P2 | 03 | Açık | `TodayTaskActions.kt`/`TodayDayCloseActions.kt`'ye aynen taşındı |
| Billing/IAP kütüphanesi yok | P0 | 09 | **Açık — bilinçli** | İş kararı netleşmeden başlanmaması gerektiği için ertelendi (doğru karar) |
| Kalan büyük dosyalarda (`OnboardingScreen.kt` 834 satır) hardcoded dp/string temizliği tamamlanmadı | P1 | 01, 02 | Açık | Detekt kuralı 90+ ihlali tespit etti, kapsam/risk nedeniyle bu turda ertelenip baseline'a alındı |
| `userId`/`deviceId` alanları hiçbir entity'de yok (gelecekte sync eklenirse) | P0 (bilinçli erteleme) | 05 | Açık — bilinçli | Hesap/sync kararı netleşmeden şema değişikliği erken olur |
| `AppTopBar`/`ResetDialog`/`BrainDumpDialog`/`EmptyState` için `@Preview` yok | P1 | 02 | Açık | Sadece `TaskRow`/`RoutineRow` eklendi |
| `RoutineEntity.bestStreak` hiç güncellenmiyor (şemada var, yazma yolu yok) | P2 | 04 | Açık | Dokunulmadı |

## Yeni Bulunan Regresyonlar

| Yeni Risk | Öncelik | Etki | Kanıt | Öneri |
|---|---:|---|---|---|
| Firebase Crashlytics tamamen kaldırıldı (`e925169`) ama **05/06/07/09 raporlarındaki "Crashlytics'i aktifleştir/google-services.json ekle" önerileri artık geçersiz** | P2 (dokümantasyon) | Yeni katılan biri eski raporları okuyup var olmayan bir entegrasyonu "aktifleştirmeye" çalışabilir | `docs/audit/results/05_backend_sync_report.md`, `06_security_privacy_report.md`, `07_performance_report.md`, `09_monetization_release_report.md` çok sayıda Crashlytics referansı taşıyor; kod tabanında artık `grep -rn "firebase\|crashlytics"` sıfır sonuç | Bu rapor bu geçersizliği burada belgeliyor; eski raporlar bilerek değiştirilmedi (tarihsel kayıt) |
| `AndroidManifest.xml`'deki `INTERNET` izni artık hiçbir amaca hizmet etmiyor | P2 | Play Store Data Safety formunda gereksiz bir izin beyanı; önceki raporlar bu izni açıkça Crashlytics'e bağlıyordu (`05_backend_sync_report.md:26,38`) | `AndroidManifest.xml:2`; kod tabanında hâlâ hiçbir HTTP/network çağrısı yok | İzni kaldırmayı değerlendirin — onay isterseniz ben kaldırabilirim |

## Güncel Yayın Kararı

- **Production**: Hayır. Kutlama ayarının plasebo olması ve uygulama iskeleti tutarsızlığı (Başarımlar/Dükkan bottom nav dışı) kullanıcı tarafından fark edilir kalitede kusurlar.
- **Internal / Closed Beta**: **Evet, artık uygun.** Eski scorecard'ın "önce" şart koştuğu üç şey (test paketi yeşil, Privacy Policy, export/import) kapandı.
- **Monetization**: Hayır — bilinçli olarak, Billing/IAP hâlâ hiç yok.
- **Gerekçe**: 5 commit / 106 dosyalık bu seri, eski final scorecard'ın P0 listesindeki 9 maddeden 7'sini kapattı (yalnızca "kutlama toggle plasebo" ve "Billing yok" açık kaldı, ikincisi zaten bilinçli erteleme). CI artık gerçekten yeşil (test + Detekt + JaCoCo hepsi geçiyor, önceki turda JaCoCo BRANCH eşiği de düzeltildi).

## Güncel P0 Listesi

1. "Kutlama efektleri" ayarını gerçek kutlama tetikleyicilerine bağla (`AppEventCoordinator`/`CelebrationModals.kt`).
2. Ana rotalarda (`RoutineDetailScreen`, `OssLicensesScreen`, Başarımlar, Dükkan) ortak `AppTopBar`/bottom nav sözleşmesini uygula.
3. (Bilinçli, iş kararı bekliyor) Billing/IAP — premium karar netleşmeden başlanmamalı.

## Sonraki En Mantıklı PR

- **PR adı**: `celebration-toggle-and-scaffold-consistency`
- **Amaç**: Kalan iki gerçek P0'ı kapatmak — kutlama ayarını fonksiyonel hale getirmek ve uygulama iskeletini tek sözleşmeye bağlamak
- **Dokunulacak dosyalar**: `AppEventCoordinator.kt`, `CelebrationModals.kt` (ya da ilgili kutlama tetikleyicileri), `RoutineDetailScreen.kt`, `OssLicensesScreen.kt`, Başarımlar/Dükkan rotalarının `AppNavigation.kt`'deki tanımı
- **Beklenen testler**: `celebrationEffectsEnabled=false` iken hiçbir modal/konfeti açılmadığını doğrulayan bir ViewModel/coordinator testi; manuel UI doğrulaması (bottom nav tüm rotalarda görünüyor mu)
- **Risk**: Orta — kutlama tetikleyicileri birden fazla dosyaya yayılmış, tek tek bulunup koşullandırılmalı; scaffold birleştirme navigasyon davranışını değiştirebilir, manuel regresyon testi şart

## Token / Kapsam Notu

Bu rapor, tek bir çalışma oturumunda üretilen ve doğrulanan (her adımda derleme + `testDebugUnitTest` ile test edilen) 5 commit'lik bir seriyi kapsıyor. Standart incremental re-audit'in "40 dosyadan fazlaysa dur" kuralı, buradaki bilgi zaten doğrulanmış olduğu için (yeniden keşif gerekmedi) bilinçli olarak aşılmıştır — bkz. `99_INCREMENTAL_REAUDIT_PROMPT.md` §10, milestone re-audit istisnası. Eski raporlar (`01`-`10`) hiçbiri yeniden yazılmamıştır.
