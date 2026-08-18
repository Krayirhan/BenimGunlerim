# Güncel Puan Tablosu — 2026-08-18

> Bu bir **yeniden puanlama** raporudur, baştan yapılan bir full audit değildir (bkz. `docs/audit/99_INCREMENTAL_REAUDIT_PROMPT.md` §12: "doğru soru eski riskleri kapatıp kapatmadığını doğrulamaktır"). Baz alınan puan tablosu `CURRENT_AUDIT_2026-08-17.md`'dir. Puanlar, aynı gün içinde beş ayrı doğrulama turunda (1: cihaz kanıtı, 2: scaffold fix + detekt temizliği, 3: themeMode ölü kod temizliği, 4: Product/UX açık madde doğrulaması, 5: Product/UX P1 kapanışları) toplanan **gerçek kanıtlara** göre güncellenmiştir. Puanlama standardı `00_MASTER_AUDIT_PROMPT.md`'deki 0-10 ölçeğidir.

## Genel Puan

**7,4 / 10** (gün başı: 7,1 → cihaz doğrulaması: 7,2 → scaffold+detekt fix: 7,3 → themeMode temizliği: 7,4 → Product/UX P1 kapanışları: **7,4**, ham ortalama yükseldi: 7,37 → 7,44)

## Puan Değişim Tablosu

| Alan | 2026-08-17 | Tur 1 | Tur 2 | Tur 3 | Tur 5 | Toplam Δ | Değişim Gerekçesi |
|---|---:|---:|---:|---:|---:|---:|---|
| **Product / UX** | 7,1 | 7,1 | 7,3 | 7,3 | **7,9** | **+0,8** | (Tur 2) Achievements/Shop geri butonu. (Tur 5) 3 gerçek P1 kapandı: `AddTaskSheet` artık saat/kategori/hatırlatıcı topluyor, `AddRoutineSheet` artık kategori/hatırlatıcı/hedef-tipi topluyor (ve `RoutineEntity.category` alanı DB'de vardı ama repository katmanı hiç kullanmıyordu — o da bağlandı), arşivlenmiş rutinler artık geri yüklenebiliyor. |
| **Frontend / Compose** | 7,2 | 7,2 | 7,9 | 8,0 | **8,1** | **+0,9** | (Tur 3) themeMode/preview temizliği. (Tur 5) `RoutineDetailScreen`'deki son sabit-yükseklik ihlali (`Button.height(50.dp)`) `AppButton`'a taşındı, Demir Kural #7 artık bu dosyada tam sağlanıyor. |
| State / ViewModel | 7,4 | 7,4 | 7,4 | 7,5 | 7,5 | +0,1 | Değişmedi |
| Data / Database | 8,0 | 8,0 | 8,0 | 8,0 | 8,0 | 0,0 | `RoutineDao.observeArchived()` + `RoutineRepository.unarchive()` eklendi ama bu zaten var olan şemaya (isArchived, category) erişim katmanı ekliyor, yeni migration gerekmedi — tavan puanı etkilemedi |
| Backend / Sync readiness | 7,0 | 7,0 | 7,0 | 7,0 | 7,0 | 0,0 | Değişmedi |
| Security / Privacy | 7,5 | 7,5 | 7,5 | 7,5 | 7,5 | 0,0 | Değişmedi |
| Performance | 6,2 | 7,8 | 7,8 | 7,8 | 7,8 | +1,6 | Değişmedi |
| Testing / QA | 7,4 | 7,7 | 7,7 | 7,7 | 7,7 | +0,3 | Tur 5 sonrası tekrar koşuldu (74/74) — bu turda yeni bir instrumentation testi bir regresyonu gerçekten yakaladı (bkz. aşağıda), sonra düzeltilip tekrar 74/74 oldu |
| Monetization / Release | 5,5 | 5,5 | 5,5 | 5,5 | 5,5 | 0,0 | Değişmedi |

**Aritmetik ortalama:** (7,9+8,1+7,5+8,0+7,0+7,5+7,8+7,7+5,5) / 9 = **7,44** → raporlanan genel puan **7,4 / 10** (görünen rakam aynı kaldı, ham ortalama 7,37'den 7,44'e çıktı)

## Tur 4 — Product/UX Açık Madde Doğrulaması (kod kanıtıyla, puan değişmedi)

Bu tur, Product/UX'in açık kalan 4 maddesini tek tek koda bakarak doğruladı. Sonuç: 2'si birebir teyit edildi, 2'sinde önceki karakterizasyonum **fazla kötümserdi** — düzeltiyorum. Bu, yeni bir kod değişikliği değil, **var olan** ama önceki turlarda yanlış tarif ettiğim gerçekliğin düzeltilmesi olduğu için puanı değiştirmiyor (2026-08-17 baseline zaten bu gerçekliği kapsıyordu).

| Madde | Önceki ifadem | Kod kanıtı sonrası düzeltilmiş durum |
|---|---|---|
| `AddTaskSheet`/`AddRoutineSheet` eksik alanlar | Açık | **Teyit edildi, değişmedi.** `AddTaskSheet.kt:39` — `category` state'i hiçbir UI alanına bağlı değil, `startTime`/`reminderTime` `onSave`'e sabit `null` geçiliyor. `AddRoutineSheet.kt:36` — `category` hardcoded `"Genel"`, `targetCount`/`unit` her zaman `null`, yeni rutinde hatırlatıcı saati hiç seçilemiyor. |
| Arşiv geri alma UI'sı yok | Açık | **Teyit edildi, değişmedi.** Proje genelinde `unarchive`/`restore routine`/`observeArchived` araması sıfır sonuç. Arşivlenen rutin tek yönlü kayboluyor. |
| Soft-delete/trash stratejisi yok | "Hiç koruma yok" gibi sundum | **Düzeltildi: kısmen yanlıştı.** `DeleteTaskUseCase` gerçekten hard delete yapıyor (doğru), ama `TodayTaskActions.kt` içinde oturum-içi bir **Undo mekanizması** var — silinen görev+subtask'lar bellekte snapshot'lanıp `RestoreTaskUseCase` ile geri getirilebiliyor (`TaskCompletedUndo` efekti). Kalıcı çöp kutusu yok (doğru), ama kazara silmeye karşı tamamen korumasız da değil. |
| Manuel UX/accessibility kanıtı yok | "Hiç a11y çalışması yok" gibi sundum | **Düzeltildi: kısmen yanlıştı.** `AccessibilityTest.kt` — content description zorunluluğu, ≥48dp touch target, ekran-çökmeden-render smoke testleri içeren 11 testlik otomatik suite var; bugünkü 74/74'ün içinde koşuyor. Eksik olan hâlâ gerçek: TalkBack narrasyonu, gerçek büyük font render'ı, tablet/landscape — bunlar hiç test edilmedi ve otomatikleştirilemez, cihazda manuel doğrulama gerektirir. |

## Tur 5 — Product/UX P1 Kapanışları (gerçek kod değişikliği)

Tur 4'te "açık" olarak teyit edilen 3 maddeden (form alanları, arşiv geri alma) tamamı bu turda gerçekten kapatıldı:

- **`AddTaskSheet.kt`**: Saat alanı (`TaskDetailSheet.kt`'deki `TaskTimePickerDialog` yeniden kullanıldı), kategori alanı, hatırlatıcı switch'i eklendi. `onSave` artık `startTime`/`reminderTime`'ı gerçek değerlerle geçiriyor (önceden sabit `null`).
- **`AddRoutineSheet.kt`**: Kategori alanı, hatırlatıcı saati + time picker, "Hedefli rutin" switch'i + hedef değer/birim alanları eklendi. **Ek bulgu:** `RoutineEntity.category` DB şemasında zaten vardı ama `RoutineRepository.add`/`update`, `AddRoutineUseCase`, `UpdateRoutineUseCase` hiçbiri bunu parametre olarak almıyordu — UI'a eklemekle yetinmedim, tüm zinciri (DAO → Repository → UseCase → ViewModel → Screen) bağladım; aksi halde yeni bir plasebo alan olurdu.
- **Arşiv geri alma**: `RoutineDao.observeArchived()`, `RoutineRepository.unarchive()`, yeni `UnarchiveRoutineUseCase`, `RoutinesViewModel.archivedRoutines`/`unarchiveRoutine()`, `RoutinesScreen`'de "Arşivlenmiş Rutinler" bölümü (yalnızca arşiv doluyken görünür, her satırda geri yükleme butonu).
- **`RoutineDetailScreen.kt`**: `Button().height(50.dp)` → `AppButton(variant=Secondary)`, son sabit-yükseklik ihlali giderildi.

**Bu turda yakalanan gerçek regresyon:** Yeni alanlar eklenince `AddTaskSheet`/`AddRoutineSheet` içeriği ekranı taşırdı, "Kaydet" butonuna dokunulamaz hale geldi. Bunu ben fark etmedim — mevcut bir instrumentation testi (`TodayScreenTest.deleting_task_shows_confirmation_dialog`) cihazda gerçekten başarısız oldu ve sorunu yakaladı. İkisine de `verticalScroll` eklenerek düzeltildi, tekrar 74/74.

**Detekt/lint:** Yeni parametre sayısı ve composable karmaşıklığı için `@Suppress("LongParameterList")` / `@Suppress("LongMethod", "CyclomaticComplexMethod")` eklendi — projenin `TaskDetailSheet.kt`'de zaten kullandığı aynı desen, form-ağırlıklı composable'lar için kabul edilmiş bir istisna.

## Bu Turda (Tur 3) Kapatılan Maddeler

- **Ölü `themeMode` altyapısı kaldırıldı.** `UserPreferencesRepository` (DataStore alanı + key), `SettingsViewModel.setThemeMode` (çağrılmayan public fonksiyon), `Theme.kt`/`MainActivity.kt` (yok sayılan parametre), export/import JSON'u (`DataExportService`/`DataImportService`) — hiçbiri render'a etki etmeden "canlıymış gibi" taşınıyordu.
- **Yanıltıcı preview'lar kaldırıldı.** `RoutineRowPreviews`/`TaskRowPreviews`'daki "Koyu" adlı preview'lar hiç karanlık render olmuyordu; artık yok, kalan preview'lar doğru şekilde adlandırıldı.
- **Geriye dönük uyumluluk test edildi.** `DataImportServiceTest`'te eski export formatındaki `"themeMode"` anahtarı bilerek JSON'da bırakıldı — import'un bilinmeyen alanı zararsızca yok saydığı doğrulandı.
- **`config/detekt/baseline.xml`** preview fonksiyon adı değişikliklerine göre güncellendi (8 eski kayıt → 4 yeni kayıt).

## Önceki Turlarda Kapatılan Maddeler

- **Scaffold/bottom-nav tutarlılığı (eski P0) — kapandı.** `DetailScreenScaffold.kt`, 5 ekrana bağlandı.
- **Achievements/Shop'ta eksik geri butonu — kapandı.**
- **`ShopScreen.kt` hardcoded string ihlali — bu dosyada kapandı**, proje genelinde değil.
- **Detekt kırmızısı (16 ihlal) — kapandı.**
- **CLAUDE.md karanlık mod dokümantasyon çelişkisi — kapandı.** Faz A satırı artık gerçek durumu ("token'lar var, özellik bilinçli kapalı") yansıtıyor.

## Yayın Kararı (güncellenmiş gerekçeyle)

- **Production:** Hayır. Kod-seviyeli tüm bilinen P0'lar kapandı. Kalan engeller kod değil, dış kanıt: Play Console/Data Safety/vitals, accessibility ve OEM notification matrisi.
- **Internal / Closed Beta:** Evet, uygun.
- **Monetization:** Hayır — Billing/IAP bilinçli olarak yok.

## Güncel P0 Listesi

1. Billing/IAP — bilinçli erteleme, ürün kararı bekliyor.
2. Play Console / accessibility / OEM notification dış kanıtı — kod değil, doğrulama eksikliği.
3. (P1, kod değil) Proje genelinde hardcoded string temizliği tamamlanmadı (`OnboardingScreen.kt` 834 satır dahil) — Demir Kural #3 ihlali hâlâ yaygın.

## Kapsam ve Güvenilirlik Notu

- Değişmeyen 4 alan (Backend, Security, Monetization, ve Data'nın büyük kısmı) bu turlarda **yeniden okunmadı** — `CURRENT_AUDIT_2026-08-17.md` puanları aynen taşındı.
- Product/UX, Frontend/Compose, State/ViewModel, Performance, Testing/QA puanları bu oturumda toplanan **gerçek kanıtla** (kod okuma + compile + detekt + lint + unit test + jacoco + cihazda connectedDebugAndroidTest) güncellendi.
- Her üç tur sonrası da tam doğrulama zinciri koşuldu: `compileDebugKotlin`, `detekt`, `lintDebug`, `testDebugUnitTest`, `jacocoDebugUnitTestCoverageVerification`, `connectedDebugAndroidTest` — hepsi geçti, birikimli regresyon yok.
- 7,4 puanı hâlâ "her şey yeniden değerlendirildi" anlamına gelmez — yalnızca kanıtı toplanan/değişen alanlar puanlandı.
- Tur 4, bu raporun kendi önceki turlarındaki (Product/UX bölümü sohbet yanıtı) iki aşırı-kötümser ifadeyi kod kanıtıyla düzeltti (soft-delete, accessibility). Puan etkilenmedi çünkü düzeltme "yeni kapanan risk" değil, "yanlış tarif edilmiş mevcut durum"dur.
- Tur 5, Tur 4'te "açık" diye doğrulanan 3 gerçek P1'i kapattı — bu tur puanı gerçekten değiştirdi (Product/UX +0,6, Frontend +0,1 bu turda). Tam doğrulama zinciri (compile + detekt + lint + unit test + jacoco + connectedDebugAndroidTest) her adımda tekrar koşuldu; bir gerçek regresyon (taşan sheet içeriği) cihaz testinde yakalandı ve düzeltildi.
