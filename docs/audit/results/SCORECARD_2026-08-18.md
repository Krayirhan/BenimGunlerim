# Güncel Puan Tablosu — 2026-08-18

> Bu bir **yeniden puanlama** raporudur, baştan yapılan bir full audit değildir (bkz. `docs/audit/99_INCREMENTAL_REAUDIT_PROMPT.md` §12: "doğru soru eski riskleri kapatıp kapatmadığını doğrulamaktır"). Baz alınan puan tablosu `CURRENT_AUDIT_2026-08-17.md`'dir. Puanlar, aynı gün içinde yedi ayrı doğrulama turunda (1: cihaz kanıtı, 2: scaffold fix + detekt temizliği, 3: themeMode ölü kod temizliği, 4: Product/UX açık madde doğrulaması, 5: Product/UX P1 kapanışları, 6: P2 kapanışları + gerçek cihazda keşfedilen landscape bug'ı, 7: proje geneli hardcoded string temizliği) toplanan **gerçek kanıtlara** göre güncellenmiştir. Puanlama standardı `00_MASTER_AUDIT_PROMPT.md`'deki 0-10 ölçeğidir.

## Genel Puan

**7,5 / 10** (gün başı: 7,1 → cihaz doğrulaması: 7,2 → scaffold+detekt fix: 7,3 → themeMode temizliği: 7,4 → Product/UX P1 kapanışları: 7,4 → P2 kapanışları + landscape fix: 7,5 → hardcoded string temizliği: **7,5**, ham ortalama: 7,48 → 7,52)

## Puan Değişim Tablosu

| Alan | 2026-08-17 | Tur 1 | Tur 2 | Tur 3 | Tur 5 | Tur 6 | Tur 7 | Toplam Δ | Değişim Gerekçesi |
|---|---:|---:|---:|---:|---:|---:|---:|---:|---|
| **Product / UX** | 7,1 | 7,1 | 7,3 | 7,3 | 7,9 | 8,0 | 8,0 | +0,9 | Değişmedi bu turda |
| **Frontend / Compose** | 7,2 | 7,2 | 7,9 | 8,0 | 8,1 | 8,3 | **8,6** | **+1,4** | (Tur 7) Demir Kural #3 (hardcoded string yasağı) proje genelinde büyük ölçüde kapandı — ~157 string 3 katmanda (UI/ViewModel/domain) temizlendi. `PrivacyPolicyScreen`, `ShopViewModel` kataloğu, `AchievementTracker` (37 başarım), `GameEngine` (12 seviye unvanı) dahil. 10 değil 8,6 çünkü `OnboardingScreen.kt` gibi büyük dosyalarda hardcoded dp/renk (string değil) hâlâ var — kapsam dışı bırakıldı. |
| **State / ViewModel** | 7,4 | 7,4 | 7,4 | 7,5 | 7,5 | 7,5 | **7,6** | **+0,2** | (Tur 7) `ShopViewModel.purchaseMessage` sealed `ShopMessage` tipine kavuştu (önceden hardcoded String, ayrıca "urune" yazım hatası vardı — düzeltildi); `SettingsViewModel.backupInfo` ölü alan olduğu tespit edilip kaldırıldı; `GameEvent.MiniBanner`/`LevelUp`/`AchievementUnlocked` artık resource-id taşıyor, yerel Context bağımlılığı olmadan doğru katmanda çözülüyor. |
| Data / Database | 8,0 | 8,0 | 8,0 | 8,0 | 8,0 | 8,0 | 8,0 | 0,0 | Değişmedi |
| Backend / Sync readiness | 7,0 | 7,0 | 7,0 | 7,0 | 7,0 | 7,0 | 7,0 | 0,0 | Değişmedi |
| Security / Privacy | 7,5 | 7,5 | 7,5 | 7,5 | 7,5 | 7,5 | 7,5 | 0,0 | Değişmedi |
| Performance | 6,2 | 7,8 | 7,8 | 7,8 | 7,8 | 7,8 | 7,8 | +1,6 | Değişmedi |
| Testing / QA | 7,4 | 7,7 | 7,7 | 7,7 | 7,7 | 7,7 | 7,7 | +0,3 | Tur 7 sonrası tekrar koşuldu; bir flaky "No compose hierarchies found" hatası (emülatör yorgunluğu, kod regresyonu değil) tekrar koşulup 74/74 doğrulandı |
| Monetization / Release | 5,5 | 5,5 | 5,5 | 5,5 | 5,5 | 5,5 | 5,5 | 0,0 | Değişmedi |

**Aritmetik ortalama:** (8,0+8,6+7,6+8,0+7,0+7,5+7,8+7,7+5,5) / 9 = **7,52** → raporlanan genel puan **7,5 / 10**

## Tur 7 — Proje Geneli Hardcoded String Temizliği

Kullanıcının açık onayıyla en geniş kapsam seçildi (UI + ViewModel + Domain, ~157 string). Üç aşamada yapıldı, her aşama sonrası tam doğrulama zinciri koşuldu:

- **Aşama 1 (UI, ~40 string, düşük risk):** `RoutineDayCodes`, `RoutineRow`, `ProgressScreen`, `StreakBadge`, `AchievementRow`, `ShopItemCard`, `PrivacyPolicyScreen` (7 bölüm × başlık/açıklama/madde — bu dosya `@Composable private fun privacySections()` olarak yeniden yapılandırıldı çünkü eskisi bir top-level `val` idi ve `stringResource()` çağıramıyordu), `AddRoutineSheet`'teki "Genel" varsayılanı.
- **Aşama 2 (ViewModel/Actions, ~27 string, orta risk — mimari değişiklik):** `GameEvent.MiniBanner` artık `@StringRes Int` taşıyor, `TodayEventEffects.kt`'nin zaten kullandığı `context.getString()` deseniyle çözülüyor. `ShopViewModel`: 10 ürünlük katalog `nameRes`/`descriptionRes`'e taşındı; `purchaseMessage: String?` → `ShopMessage` sealed class. **Yol boyunca 2 ölü kod/hata bulundu:** `SettingsViewModel.backupInfo` hiçbir yerde render edilmiyordu (silindi); `ShopViewModel`'de `"Bu urune zaten sahipsin."` yazım hatası vardı (düzeltildi: "ürüne"), ayrıca `purchaseMessage`/`clearMessage()`'ın kendisi de UI'da hiç tüketilmiyor (bilinçli olarak silinmedi, doğru mimariyle bırakıldı).
- **Aşama 3 (Domain, ~90 string, en yüksek risk):** `AchievementTracker`'daki 37 başarım tanımının tamamı `@StringRes` alanlara taşındı. `GameEngine`'deki 12 seviye unvanı + `companionMessage()` motivasyon cümle havuzları resource-id tabanlı hale getirildi (`companionMessage()`'ın üretimde hiç çağıranı olmadığı ortaya çıktı — yine silinmedi, ileride bağlanmaya hazır bırakıldı). `GameEvent.LevelUp`/`AchievementUnlocked` artık `titleRes`/`descriptionRes` taşıyor, `TodayModalsHost.kt`'de (zaten Composable) `stringResource()` ile çözülüyor.

**Doğrulama sırasında yakalanan flaky test:** `connectedDebugAndroidTest` bir turda 2 testte "No compose hierarchies found in the app" hatası verdi. Uygulamayı elle başlatıp ekran görüntüsü aldım — çökme yok, tüm string'ler (seviye unvanı "Gelişimci" dahil) doğru render oluyordu. Testleri tekrar koşturdum: 74/74 geçti. Sonuç: emülatörün uzun oturum sonrası yorgunluğu, kod regresyonu değil.

**Kapsam dışı bırakılan (bilinçli):** `CategoryPalette.kt`'deki kategori-renk eşleme anahtarları (kullanıcıya gösterilmiyor, veri anahtarı — Demir Kural #3 kapsamına girmiyor) ve `@Preview` dosyalarındaki örnek veriler (hiç son kullanıcıya gösterilmiyor, endüstri standart pratiği).

## Tur 6 — P2 Kapanışları + Gerçek Cihazda Keşfedilen Landscape Bug'ı

**P2 (polish) maddeleri:**
- `WeekPicker.kt` prev/next hafta ikon butonları 36dp'den `AppTokens.TouchTarget.min` (48dp)'ye çıkarıldı.
- `progress_mood_very_bad` string'i "Çok Kötü" → "Çok kötü" — artık `progress_mood_*`, `today_mood_*`, `today_close_step1_mood_*` üç seti de casing açısından tutarlı.

**Manuel accessibility/responsive kanıtı — bu kez gerçekten toplandı:** Bağlı emülatörde `adb shell settings put system font_scale 1.5` ile büyük font, `user_rotation`/`accelerometer_rotation` ile landscape zorlandı, gerçek ekran görüntüleri alındı.
- **1.5x font ölçeği: sorun yok.** İlk bakışta "İlk Görevi Ekle" butonu bottom nav'ın arkasında kalmış gibi görünse de, scroll edilince tamamen erişilebilir olduğu doğrulandı — yanlış alarmdı, düzeltme gerekmedi.
- **Landscape modda gerçek bug bulundu ve düzeltildi:** Today ekranında FAB (`+` butonu), kısa dikey alanda (~427dp) uyarı bannerının "Değerlendir" aksiyon butonunun üzerine biniyordu. Kök neden: `ScreenScaffold`'daki FAB her zaman sabit 32dp alt-offset ile köşeye yerleşiyor; landscape'te içerik alanı o kadar kısalıyor ki bu sabit konum ilk ekranda üst içerikle çakışıyor. **Düzeltme:** yeni `isCompactHeight()` yardımcı fonksiyonu (`LocalConfiguration.screenHeightDp < 480`) + yeni `AppFab` bileşeni — compact yükseklikte Material'ın kendi önerdiği `SmallFloatingActionButton`'a düşüyor, `ScreenScaffold` da compact yükseklikte alt-offset'i azaltıyor. 3 ekranda (Today, Plan, Routines) uygulandı, aynı emülatörde tekrar test edilip düzeltmenin çalıştığı görsel olarak doğrulandı.

Bu, sohbette önerdiğim "manuel kanıt gerektirir, otomatikleştirilemez" iddiasının tam da neden doğru olduğunu gösteren bir örnek: gerçek cihaz testi, kod okuyarak asla bulunamayacak bir bug ortaya çıkardı.

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
3. (P2, kod değil kritik) `OnboardingScreen.kt` (834 satır) gibi büyük dosyalarda hardcoded dp/renk (string değil — string'ler Tur 7'de temizlendi) hâlâ var, Demir Kural #1/#2 kapsamında.

## Kapsam ve Güvenilirlik Notu

- Değişmeyen 4 alan (Backend, Security, Monetization, ve Data'nın büyük kısmı) bu turlarda **yeniden okunmadı** — `CURRENT_AUDIT_2026-08-17.md` puanları aynen taşındı.
- Product/UX, Frontend/Compose, State/ViewModel, Performance, Testing/QA puanları bu oturumda toplanan **gerçek kanıtla** (kod okuma + compile + detekt + lint + unit test + jacoco + cihazda connectedDebugAndroidTest) güncellendi.
- Her üç tur sonrası da tam doğrulama zinciri koşuldu: `compileDebugKotlin`, `detekt`, `lintDebug`, `testDebugUnitTest`, `jacocoDebugUnitTestCoverageVerification`, `connectedDebugAndroidTest` — hepsi geçti, birikimli regresyon yok.
- 7,4 puanı hâlâ "her şey yeniden değerlendirildi" anlamına gelmez — yalnızca kanıtı toplanan/değişen alanlar puanlandı.
- Tur 4, bu raporun kendi önceki turlarındaki (Product/UX bölümü sohbet yanıtı) iki aşırı-kötümser ifadeyi kod kanıtıyla düzeltti (soft-delete, accessibility). Puan etkilenmedi çünkü düzeltme "yeni kapanan risk" değil, "yanlış tarif edilmiş mevcut durum"dur.
- Tur 5, Tur 4'te "açık" diye doğrulanan 3 gerçek P1'i kapattı — bu tur puanı gerçekten değiştirdi (Product/UX +0,6, Frontend +0,1 bu turda). Tam doğrulama zinciri (compile + detekt + lint + unit test + jacoco + connectedDebugAndroidTest) her adımda tekrar koşuldu; bir gerçek regresyon (taşan sheet içeriği) cihaz testinde yakalandı ve düzeltildi.
