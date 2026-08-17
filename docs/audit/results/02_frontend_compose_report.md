# Audit Raporu — Frontend / Jetpack Compose

> ⚠️ **2026-08-17 güncellemesi:** `TodayScreen.kt`/`TodayViewModel.kt`/`TodaySheets.kt` artık bölünmüş durumda, hardcoded dp/renk/string için CI'a bağlı bir Detekt kuralı eklendi, `TaskRow`/`RoutineRow` için `@Preview` seti eklendi. Güncel durum için bkz. [`INCREMENTAL_REAUDIT_2026-08-17.md`](INCREMENTAL_REAUDIT_2026-08-17.md). Bu doküman tarihsel kayıt olarak değiştirilmemiştir.

## Genel Puan
6.5 / 10

## Kısa Karar
Mimari geçiş (Faz A-D) gerçek ve iyi kalitede ilerlemiş: `ui/theme`, `ui/components/core`, `ui/components/gamification`, `ui/components/molecules`, `ui/components/organisms` katmanları gerçekten var, iskeletler tutarlı ve `Common.kt` + eski `AppNavigation.kt` fiilen silinmiş. Ancak Faz E ("ekranları baştan yaz") tamamlanmamış: `TodayScreen.kt` (767 satır) ve `TodayViewModel.kt` (740 satır) hâlâ 200 satır sınırının 3,5 katı büyüklükte, FAB menüsü ve iki bağlamsal banner ekran dosyasının içine gömülü kalmış, hardcoded Türkçe string sayısı (142 eşleşme) ve dp literal sayısı (478 eşleşme) hâlâ çok yüksek, ve `@Preview` kapsaması pratikte sıfıra yakın (79 dosyada tek 1 örnek). Bu haliyle refactor + polish turu şart; ürün çalışıyor ve component mimarisi sağlam bir temel oluşturmuş olsa da "Faz E tamamlandı" iddiası mevcut koda göre erken. Yayına çıkmadan önce en azından TodayScreen/TodayViewModel bölünmeli ve hardcoded string/dp temizliği yapılmalı.

## En Güçlü 5 Taraf
1. Katman ayrımı gerçek: `ui/components/core` (`AppButton.kt`, `AppSurface.kt`, `AppBadge.kt`, `AppChip.kt`, `AppDivider.kt`, `AppIcon.kt`), `ui/components/gamification`, `ui/components/molecules` (`SectionBlock.kt`, `EmptyState.kt`, `AlertBanner.kt`, `BarChart.kt`, `ColorRailCard.kt`) ve `ui/components/organisms` (`TaskRow.kt`, `RoutineRow.kt`, `WeekPicker.kt`, `AddTaskSheet.kt`, `HeaderProgressCard.kt`, `CloseDayCard.kt`, `LevelHeroCard.kt`) klasörleri fiilen dolu ve isimlendirme CLAUDE.md'deki hedef hiyerarşiyle birebir örtüşüyor.
2. `Common.kt` ve eski `ui/AppNavigation.kt` gerçekten silinmiş (git status: `D app/.../ui/components/Common.kt`, `D app/.../ui/AppNavigation.kt`), yeni `ui/navigation/AppNavigation.kt` içine taşınmış — "her şey" dosyası artık yok.
3. `AppTopBar` (`ui/components/layout/AppTopBar.kt`) gerçekten tek, global, parametrik bir bileşen: `BenimGunlerimApp` içinde tek yerden çağrılıyor, ekranlara kopyalanmamış.
4. `ResetDialog.kt` ve `BrainDumpDialog.kt` state'i tamamen kendi içinde tutan, lambda ile dışarı ileten (onDismiss/onEnableLightDay/onPickTask, onDismiss/onAddTasks), gerçekten tek dosyada kapsüllenmiş ve tekrar kullanılabilir sheet'ler; TodayScreen'den çağrılıyor ama başka bir ekrana taşınabilir.
5. Alt navigasyon (`ui/navigation/AppNavigation.kt` içindeki `NavigationBar`) `stringResource(destination.labelRes)` ile etiket ve `contentDescription` kullanıyor, `TestTags` ile test kancaları hazır (`BottomNavToday` vb.) — bu, projede en azından bir alanda erişilebilirlik ve testability standardının doğru uygulandığını gösteriyor.

## En Kritik 10 Sorun
| Öncelik | Sorun | Etki | Kanıt/Dosya | Öneri |
|---|---|---|---|---|
| P0 | `TodayScreen.kt` 767 satır — 200 satır kuralının 3,8 katı; FAB menüsü, Hafif Gün Modu banner'ı, bağlamsal reset kartı ve `QuickActionRow` hepsi tek dosyada | Bakım riski yüksek, tek dosyada state+UI karışık, PR review'ları zorlaşıyor | `app/src/main/java/com/benimgunlerim/ui/today/TodayScreen.kt:1-767` | Aşağıdaki "Beklenen öneri tipi" bölümünde verilen parçalara böl |
| P0 | `TodayViewModel.kt` 740 satır, ~35+ public fonksiyon: görev CRUD, alt görev CRUD, rutin CRUD, Hafif Gün Modu, Brain Dump, gün kapama, kaçırılan gün akışı hepsi tek ViewModel'de | Tek sorumluluk ilkesi ihlali; test edilebilirlik düşük, her değişiklik geniş blast radius yaratıyor | `app/src/main/java/com/benimgunlerim/ui/today/TodayViewModel.kt:155-740` | UseCase'lere zaten delege ediliyor (iyi) ama ViewModel'i TaskActions/RoutineActions/DayCloseActions gibi ayrı sınıflara/delegate'lere böl, ya da en azından `Composition`/facade pattern ile küçük ViewModel + ayrı state holder'lara ayır |
| P0 | Hardcoded Türkçe metinler `strings.xml` kuralını ihlal ediyor — aynı dosyada bir satır önce `stringResource` kullanılırken bir satır sonra literal string var | Çeviri/ölçeklenebilirlik imkânsız, tutarsız kod stili, gözden kaçan string'ler QA'da fark edilmiyor | `TodayScreen.kt:148` (`"✓ Görev tamamlandı · +10 XP"`), `:178-186` (`"Bugünün görevleri tamamlandı 🎉"`, `"${event.totalCount} / ${event.totalCount} görev tamamlandı"`, vb.), `:497-537` (`"Görev Ekle"`, `"Bugün için tek seferlik yapılacak iş"`, `"Aktif"`) | Bu string'leri `strings.xml`'e taşı, `stringResource(...)` ile değiştir; parametrik olanlar için `stringResource(R.string.x, arg)` kullan |
| P0 | `AppNavigation.kt` içindeki topbar başlık/altyazı çiftleri tamamen hardcoded, 10 satır aşağıdaki bottom-nav etiketleri ise doğru şekilde `stringResource` kullanıyor — aynı dosya içinde çelişkili standart | Tutarsızlık; çoklu dil desteği veya metin değişikliği riskli hale geliyor | `app/src/main/java/com/benimgunlerim/ui/navigation/AppNavigation.kt:149-156` | `"Benim Günlerim"`, `"Plan"`, `"Günlerini düzenle"` vb. için `strings.xml`'de topbar başlık/altyazı kaynakları tanımla |
| P1 | `AppTopBar.kt` ve `TodayColorTokens.kt` gibi "component/tema" dosyalarında yoğun `Color(0xFF...)` literal kullanımı — Material tema token'ları yerine ekrana özel sabit renk paleti | Karanlık mod tutarlılığı manuel olarak elle sürdürülüyor (iki ayrı `Color(0xFF..)` seti dark/light için), tema merkezi değişince senkron kalmayabilir | `app/src/main/java/com/benimgunlerim/ui/components/layout/AppTopBar.kt:63-69`, `app/src/main/java/com/benimgunlerim/ui/today/theme/TodayColorTokens.kt:33-73` | `MaterialTheme.colorScheme.*` semantic renklerine geçir veya bu paletleri `Theme.kt`'deki merkezi token setine taşı; iki yerde aynı renk mantığının tekrarlanmasını (AppTopBar'daki `isDark` hesaplama mantığı) tek yardımcı fonksiyona indir |
| P1 | `@Preview` kapsamı neredeyse yok: 79 Compose dosyasından sadece `RoutineDetailScreen.kt` içinde 1 tane `@Preview` var | Tasarım/dark-mode/edge-case regresyonları sadece cihazda test edilerek yakalanabiliyor, review hızı düşük | Repo genelinde `grep -rl "@Preview" ui/` → tek sonuç: `app/src/main/java/com/benimgunlerim/ui/routines/RoutineDetailScreen.kt` | En azından `TodayScreen`, `TaskRow`, `RoutineRow`, `AppTopBar`, `ResetDialog`, `BrainDumpDialog`, `EmptyState` için light/dark ve boş/dolu state varyasyonlu `@Preview` ekle |
| P1 | Sabit yükseklikli buton/alan kullanımı ölçüsüz — Demir Kural 7 "sabit yükseklik yasak" içerik kapsayıcıları için ama `BrainDumpDialog` metin alanı `Modifier.height(180.dp)` ile sabitlenmiş; uzun metinlerde taşma riski | Kullanıcı çok satır yazarsa metin alanı büyümüyor, sadece scroll'a güveniliyor; UX'te "kesik" his verebilir | `app/src/main/java/com/benimgunlerim/ui/components/calm/BrainDumpDialog.kt:157-159` | `heightIn(min = 120.dp, max = 240.dp)` gibi esnek bir aralık kullan |
| P1 | `ResetDialog.kt` içinde süre seçenekleri hardcoded Türkçe: `"30 saniye"`, `"1 dakika"` — dosyanın geri kalanı tamamen `stringResource` kullanırken bu iki string atlanmış | Tutarsızlık, string.xml'de eksik çeviri kaynağı | `app/src/main/java/com/benimgunlerim/ui/components/calm/ResetDialog.kt:199` | `stringResource(R.string.reset_duration_30s)` / `..._60s` ekle |
| P1 | `contentDescription = null` 28 yerde kullanılmış; bazıları dekoratif ikon olduğu için doğru olabilir ama etkileşimli/anlamlı ikonlarla karışık — tek tek doğrulama yapılmadı bu turda ("bu alanda tam kapsamlı doğrulama yapılamadı" — bkz. not) | Ekran okuyucu kullanıcılar için bazı ikonlar (ör. `TodayScreen.kt:277,340,729` dekoratif banner ikonları — bunlar kabul edilebilir) ile potansiyel olarak anlamlı ikonlar ayırt edilmemiş | `app/src/main/java/com/benimgunlerim/ui/today/TodayScreen.kt:277,340,729`, genel `grep contentDescription = null` sonuçları | Her `contentDescription = null` kullanımını gözden geçirip yalnızca gerçekten dekoratif (yanındaki Text ile aynı bilgiyi taşıyan) ikonlarda bırak |
| P2 | `AchievementsScreen.kt` küçük ve temiz (64 satır) ama yine de hardcoded Türkçe string içeriyor: `"Başarım Özeti"`, `"Kazanıldı"`, `"Tüm Başarımlar"`, `"Tamamlandı"`, `"Kilitli"`, emoji `"🏆"` | Küçük/iyi yazılmış dosyalarda bile string kuralı sistematik olarak uygulanmamış — bu bir disiplin/lint eksikliği olduğunu gösteriyor | `app/src/main/java/com/benimgunlerim/ui/achievements/AchievementsScreen.kt:37-58` | `strings.xml`'e taşı; ileride bir Detekt/ktlint custom rule ile hardcoded Türkçe karakter içeren `Text("...")` literal'lerini CI'da yakala |

## Component Sağlığı
| Component/Dosya | Durum | Risk | Önerilen Aksiyon |
|---|---|---|---|
| `ui/today/TodayScreen.kt` (767 satır) | Kırmızı | Sürdürülemez boyut, FAB menüsü ve iki banner ekranın içine gömülü | `TodayRoute` zaten ayrı; `TodayScreen`'i aşağıdaki alt bölümlere ayır |
| `ui/today/TodayViewModel.kt` (740 satır) | Kırmızı | Tek ViewModel'de 4+ farklı iş alanı (görev, rutin, gün kapama, brain dump) | Alt state-holder'lara/UseCase gruplarına böl |
| `ui/today/TodaySheets.kt` (914 satır) | Kırmızı | Proje genelinde en büyük dosya; 4 composable içeriyor ama her biri devasa (`CloseDaySheet` vb. muhtemelen) | Her sheet kendi dosyasına (`CloseDaySheet.kt`, `SummaryTile.kt`) taşınmalı |
| `ui/onboarding/OnboardingScreen.kt` (882 satır) | Kırmızı | Tek dosyada NeedCard, IntensityCard, SelectableRoutineRow + ana akış | `organisms/onboarding/` altına ayrı dosyalara böl |
| `ui/components/calm/ResetDialog.kt` (388 satır) | Sarı | Boyut sınırın altında (component dosyası, 200 kuralı ekran için) ama dp literal yoğun, 2 hardcoded string var | dp'leri `AppTokens`'a taşı, kalan 2 string'i `strings.xml`'e al |
| `ui/components/calm/BrainDumpDialog.kt` (312 satır) | Sarı | İyi kapsüllenmiş, tek sorumluluk; dp literal yoğun | dp'leri `AppTokens`'a taşı |
| `ui/components/layout/AppTopBar.kt` (205 satır) | Sarı | Gerçekten global ve tekil component (iyi), ama iç renk paleti hardcoded `Color(0xFF..)` ve `contentDescription` string'leri (`"Profil"`, `"Bildirimler"`) hardcoded | Renkleri temaya, string'leri `strings.xml`'e taşı |
| `ui/navigation/AppNavigation.kt` (318 satır) | Sarı | Bottom nav doğru (stringResource), ama topbar başlık/altyazı hardcoded — dosya içi standart tutarsızlığı | Başlık/altyazı çiftlerini `strings.xml`'e taşı |
| `ui/components/organisms/TaskRow.kt`, `RoutineRow.kt`, `WeekPicker.kt`, `AddTaskSheet.kt` | Yeşil | Boyut makul (130-231 satır), CLAUDE.md'deki hedef API'lere yakın imzalar, state parametre olarak geliyor | Küçük polish: dp literal'lerin `AppTokens`'a taşınması |
| `ui/components/molecules/SectionBlock.kt`, `EmptyState.kt`, `AlertBanner.kt`, `ColorRailCard.kt` | Yeşil | Molekül katmanı CLAUDE.md'deki tasarımla örtüşüyor, boyutlar küçük (58-126 satır) | Yok / düşük öncelik |
| `ui/theme/DesignTokens.kt` (134 satır), `Theme.kt` (262 satır) | Yeşil | Token kaynağı merkezi ve tek dosyada; CLAUDE.md'deki "Katman 0/1" tanımıyla birebir örtüşüyor | Yok |
| `@Preview` kapsamı (proje geneli) | Kırmızı | 79 dosyadan sadece 1'inde `@Preview` var | Kritik organizmalar ve TodayScreen alt parçaları için preview seti ekle |
| `ui/components/Common.kt`, eski `ui/AppNavigation.kt` | Yeşil (silinmiş) | Faz E hedefi olan "Common.kt tamamen silinir" fiilen gerçekleşmiş | Yok |

## Dosya Bazlı Bulgular

### `app/src/main/java/com/benimgunlerim/ui/today/TodayScreen.kt`
- Bulgu: Dosya 767 satır; ekranın kompozisyonu, FAB hızlı eylemler menüsü (satır 476-551), Hafif Gün Modu banner'ı (satır 248-315), bağlamsal reset öneri kartı (satır 316-389) ve `QuickActionRow` private composable'ı (satır 699-767) hepsi aynı dosyada. Ayrıca satır 148, 178-186, 497-537'de hardcoded Türkçe string'ler var — dosyanın geri kalanı `stringResource` kullanırken.
- Risk: CLAUDE.md Demir Kural 5 ("Ekran dosyaları 200 satırı geçemez") doğrudan ihlal ediliyor; string tutarsızlığı Kural 3'ü ihlal ediyor. Yeni bir geliştirici bu dosyaya dokunduğunda hem state hem UI hem navigasyon mantığıyla aynı anda uğraşmak zorunda kalıyor.
- Öneri: TodayScreen.kt şu parçalara ayrılmalı:
  - `TodayScreen` (yalnızca kompozisyon + ScreenScaffold iskeleti, ~120 satır)
  - `TodayContent` (Column içindeki scroll edilebilir gövde)
  - `LightDayBanner` (satır 249-315, `ui/components/organisms/LightDayBanner.kt`)
  - `ContextualResetCard` (satır 316-389, `ui/components/organisms/ContextualResetCard.kt`)
  - `TodayFabActionSheet` (satır 476-551 + `QuickActionRow`, `ui/components/organisms/TodayFabActionSheet.kt`)
  - `TodayGameEventEffects` (satır 133-198'deki `LaunchedEffect` bloklarını saran ayrı bir composable veya ViewModel'e taşınan effect handler)
  - Hardcoded string'ler `strings.xml`'e taşınmalı (`today_task_completed_undo_msg`, `today_all_tasks_completed_title/body/badge`, `today_all_routines_completed_*`, `quick_action_add_task_title/subtitle`, `quick_action_active_badge`)

### `app/src/main/java/com/benimgunlerim/ui/today/TodayViewModel.kt`
- Bulgu: 740 satır, tek `@HiltViewModel` sınıfı içinde görev CRUD (toggle/update/move/delete/restore/undo — satır 327-465), alt görev CRUD (472-486), rutin CRUD + streak (493-598), gün kapama + kaçırılan gün akışı (609-715) ve Brain Dump (277-292) birlikte yönetiliyor.
- Risk: Tek sorumluluk ilkesi ihlali; state tek `uiState` StateFlow'unda toplanıyor, bu da her domain'de bir değişiklik olduğunda tüm ekranın recompose olma riskini artırıyor (aşağıdaki "Teknik Borç Etkisi" bölümüne bakın). Unit test yazmak için her testin devasa bir ViewModel'i mock'lamasını gerektiriyor.
- Öneri: UseCase'lere delege etme zaten var (`observeTodaySnapshotSafe`, `saveDailySummaryWithOptionalCarry` vb.) — bu iyi bir temel. Bir sonraki adım: ViewModel'i `TodayTaskActions`, `TodayRoutineActions`, `TodayDayCloseActions` gibi ayrı sınıflara (constructor injection ile ViewModel'e enjekte edilen delegate'ler) bölmek, ya da en azından `uiState`'i alt state parçalarına ayırıp yalnızca ilgili UI bölümünün recompose olmasını sağlamak.

### `app/src/main/java/com/benimgunlerim/ui/today/TodaySheets.kt`
- Bulgu: Proje genelinde en büyük UI dosyası (914 satır), 4 üst düzey composable (satır 86, 314, 648, 883) içeriyor; `SheetSectionHeader` ve `SummaryTile` gibi küçük yardımcı composable'lar da aynı dosyada.
- Risk: Dosya adı "Sheets" (çoğul) olsa da hepsi tek dosyada — CLAUDE.md'nin "her bileşen kendi dosyasında" kuralına (Demir Kural 8) aykırı.
- Öneri: Her üst düzey composable'ı (muhtemelen `CloseDaySheet`, ikinci bir sheet, vb.) kendi dosyasına ayır; `SheetSectionHeader`/`SummaryTile` gibi paylaşılan parçaları `ui/components/molecules/` altına taşı.

### `app/src/main/java/com/benimgunlerim/ui/components/layout/AppTopBar.kt`
- Bulgu: Gerçekten tek/global bir component (iyi) ama içinde `isDark` hesaplama mantığı (satır 58-60) ve 6 adet `Color(0xFF..)` literal (satır 63-69) var. `contentDescription = "Profil"` (satır 99) ve `"Bildirimler"` (satır 164) hardcoded.
- Risk: Karanlık/aydınlık renk çiftleri merkezi temadan bağımsız olarak elle sürdürülüyor; `Theme.kt`'deki asıl `ColorScheme` değişirse bu dosya senkron kalmayabilir. Erişilebilirlik metinleri çeviri kapsamı dışında kalıyor.
- Öneri: Renkleri `MaterialTheme.colorScheme` semantic rollerine (`surfaceContainerHigh`, `outlineVariant` vb.) veya merkezi `AppTokens`/`Theme.kt` içine taşı; `contentDescription` değerlerini `stringResource(R.string.topbar_profile_cd)` / `topbar_notifications_cd` yap.

### `app/src/main/java/com/benimgunlerim/ui/navigation/AppNavigation.kt`
- Bulgu: Bottom nav etiketleri `stringResource(destination.labelRes)` ile doğru yapılmış (satır 213, 220) ama hemen üstündeki topbar başlık/altyazı `when` bloğu (satır 149-156) tamamen hardcoded Türkçe string çiftleri içeriyor.
- Risk: Aynı dosyada iki farklı standart bir arada — gelecekteki katkıcı için hangi yaklaşımın "doğru" olduğu belirsiz, bu da tutarsızlığın yayılmasına yol açar.
- Öneri: `topBarTitle`/`topBarSubtitle` çiftlerini `strings.xml`'e taşı (`R.string.topbar_today_title`, `R.string.topbar_today_subtitle` vb.).

### `app/src/main/java/com/benimgunlerim/ui/components/calm/ResetDialog.kt`
- Bulgu: 388 satırlık component dosyası, neredeyse tüm metinler `stringResource` ile geliyor (iyi) ama satır 199'daki süre etiketleri (`"30 saniye"`, `"1 dakika"`) hardcoded kalmış. dp literal sayısı yüksek (satır 105, 108, 110, 121... onlarca yerde).
- Risk: String kuralı kısmi uygulanmış; dp literal'ler `AppTokens.Spacing`/`AppTokens.Radius` ile eşleşen değerler olsa da (12.dp, 16.dp, 24.dp gibi) doğrudan token referansı kullanılmıyor, bu da spacing tutarlılığını gelecekte bozma riskini taşıyor (birileri "20.dp" yazıp `AppTokens.Spacing.lg` ile aynı değeri farklı bir yerde farklı yazabilir).
- Öneri: 2 hardcoded string'i `strings.xml`'e taşı; dp literal'leri `AppTokens.Spacing.*` / `AppTokens.Radius.*` ile değiştir.

### `app/src/main/java/com/benimgunlerim/ui/components/calm/BrainDumpDialog.kt`
- Bulgu: 312 satır, tek sorumluluk (metin gir → satırlara ayır → seç → görev olarak ekle), state tamamen local `remember`'larda, dışarıya `onDismiss`/`onAddTasks` lambda'larıyla açılıyor — organism seviyesinde iyi bir örnek. Ancak metin alanı `Modifier.height(180.dp)` ile sabit (satır 159) ve dp literal yoğun.
- Risk: Sabit yükseklik CLAUDE.md Demir Kural 7'yi ihlal ediyor (bu kural içerik kapsayıcıları için `IntrinsicSize.Min` istiyor; burada tam o senaryo değil ama sabit 180dp bir `OutlinedTextField` uzun girdilerde kullanıcı deneyimini kısıtlıyor).
- Öneri: `heightIn(min = 140.dp, max = 260.dp)` gibi esnek bir yükseklik aralığına geçir.

### `app/src/main/java/com/benimgunlerim/ui/today/CategoryPalette.kt`
- Bulgu: Kategori rengi seçimi Türkçe anahtar kelime eşleştirmesiyle yapılıyor (`"spor"`, `"iş"`, `"ev"` vb., satır 32-63) — bu, uygulamanın tek dil (Türkçe) hedefiyle tutarlı ama İngilizce görev başlığı yazan bir kullanıcı için (ör. "gym", "work") tüm eşleşmeler fallback hash rengine düşüyor.
- Risk: Düşük — bu bir tasarım kararı, hata değil; ama Kural 3 açısından bu string'ler UI metni değil domain-mantığı anahtar kelimesi olduğu için ihlal sayılmaz.
- Öneri: Değişiklik gerekmiyor; ileride çok dilli destek planlanıyorsa bu keyword listesi ayrı bir yapılandırma dosyasına taşınmalı.

### `app/src/main/java/com/benimgunlerim/ui/achievements/AchievementsScreen.kt`
- Bulgu: 64 satır, temiz kompozisyon, doğru component kullanımı (`ScreenScaffold`, `SectionBlock`, `StatPill`, `AchievementRow`) ama satır 37, 44-58'de hardcoded Türkçe string'ler ve emoji var.
- Risk: Küçük/örnek-niteliğinde bir dosyada bile string kuralı uygulanmamış olması, bu kuralın disiplinli şekilde uygulanmadığının kanıtı — CI'da otomatik yakalama olmadığı sürece tekrar edecek bir problem.
- Öneri: `strings.xml`'e taşı (`R.string.achievements_summary_title`, `achievements_unlocked_label`, `achievements_all_title`, `achievements_status_done`, `achievements_status_locked`).

## Kullanıcı Deneyimi Etkisi
- Bugün ekranındaki FAB menüsü, Hafif Gün Modu ve Reset akışları kullanıcı gözünden tutarlı ve düşünülmüş (breathing animasyonu, kademeli adım geçişleri, undo destekli snackbar'lar) — bu iyi bir temel deneyim sağlıyor.
- Ancak `@Preview` kapsamının pratikte yok denecek kadar az olması, tasarımcı/geliştirici arasında görsel doğrulamanın yalnızca cihazda derleyip çalıştırarak yapılabildiği anlamına geliyor; bu, karanlık mod ve boş-state gibi varyasyonların gözden kaçma riskini artırıyor.
- Hardcoded string'lerin bir kısmı (`"✓ Görev tamamlandı · +10 XP"` gibi) kullanıcıya doğrudan görünen metinler — bunların strings.xml dışında kalması, gelecekte metin tonunu tutarlı güncellemeyi (ör. "acımasız ama hakkaniyetli" ton kılavuzu değişirse) zorlaştırıyor çünkü tüm metinler tek yerden yönetilemiyor.

## Teknik Borç Etkisi
- `TodayViewModel`'in tek `uiState: StateFlow<TodayUiState>` üzerinden 35+ fonksiyonu yönetmesi, her state güncellemesinde `TodayScreen`'in `collectAsState()` ile tüm state'i okuyup (satır 101) potansiyel olarak gereksiz recomposition tetiklemesi riski taşıyor — bu turda gerçek recomposition profiling yapılmadı, bu nedenle kesin hüküm verilmiyor ("bu alanda profiling kanıtı bulamadım"), ama state'in granülerliği düşük olduğu için risk teorik olarak yüksek.
- 767+740+914 satırlık üç dosya (`TodayScreen`, `TodayViewModel`, `TodaySheets`) Bugün akışının tamamını oluşturuyor ve hepsi aynı anda değişmeye açık — bu, gelecekteki her Bugün ekranı değişikliğinin merge çakışması riskini artırıyor.
- Hardcoded string/dp/renk kullanımı otomatik lint/Detekt kuralıyla engellenmediği sürece (repo'da böyle bir kural bulunamadı) yeni kod da aynı borcu üretmeye devam edecek.

## Release / Monetizasyon Riski
- Bu denetim yalnızca frontend/Compose kapsamındadır; monetizasyon (IAP, reklam, lisans) akışlarına bu turda rastlanmadı — "bu alanda kanıt bulamadım."
- Play Store yayını açısından doğrudan risk: `@Preview` eksikliği ve büyük dosyalar release kalitesini düşürmüyor (derleniyor, çalışıyor) ama QA turlarının manuel test yüküyle sınırlı kalmasına neden oluyor; regresyon riskini artırıyor.
- Hardcoded Türkçe metinler tek-dil hedefiyle uyumlu olduğu için bu turda bir "yayın engelleyici" değil, ama gelecekte çoklu dil eklenirse büyük bir yeniden yazım maliyeti doğuracak.

## Önceliklendirilmiş Yapılacaklar

### P0 — Yayın öncesi şart
- `TodayScreen.kt`'i yukarıda önerilen 6 parçaya böl (özellikle FAB menüsü ve iki banner'ı organism'e taşı).
- `TodayViewModel.kt`'i domain bazlı alt yapılara ayır (en azından task/routine/day-close action gruplarına).
- `TodayScreen.kt`, `AppNavigation.kt` içindeki tüm hardcoded Türkçe string'leri `strings.xml`'e taşı (özellikle satır 148, 178-186, 497-537 ve AppNavigation satır 149-156).

### P1 — Kısa vadede gerekli
- `TodaySheets.kt`'i alt composable'lara böl, her birini ayrı dosyaya taşı.
- `OnboardingScreen.kt` (882 satır) için aynı bölme işlemini uygula (`NeedCard`, `IntensityCard`, `SelectableRoutineRow` zaten ayrı fonksiyonlar — bunları ayrı dosyalara taşımak yeterli).
- `AppTopBar.kt` ve `TodayColorTokens.kt` içindeki hardcoded `Color(0xFF..)` paletlerini merkezi temaya bağla.
- Kritik organizmalar (`TaskRow`, `RoutineRow`, `AppTopBar`, `ResetDialog`, `BrainDumpDialog`, `EmptyState`) için light/dark `@Preview` seti ekle.
- `ResetDialog.kt` içindeki 2 hardcoded süre string'ini taşı.

### P2 — Polish / ileri iyileştirme
- `BrainDumpDialog.kt`'teki sabit `180.dp` metin alanı yüksekliğini esnek aralığa çevir.
- Kalan tüm bileşen dosyalarındaki dp literal'leri `AppTokens.Spacing`/`Radius` referanslarına çevir (478 eşleşmenin büyük kısmı bu iş).
- `contentDescription = null` kullanımlarının tamamını tek tek gözden geçirip yalnızca gerçekten dekoratif ikonlarda bırak.
- Hardcoded Türkçe string/dp/renk kullanımını CI'da yakalayan bir Detekt custom rule ekle.

## 1 Haftalık Düzeltme Planı
- Gün 1-2: `TodayScreen.kt`'i organism'lere böl (`LightDayBanner`, `ContextualResetCard`, `TodayFabActionSheet`); derleme ve mevcut `TodayScreenTest.kt`'nin geçtiğini doğrula.
- Gün 3-4: `TodayViewModel.kt`'teki fonksiyonları domain gruplarına ayır (en az UseCase'lere daha fazla delege ederek dosyayı küçült); `TodayScreenTest.kt` ile regresyon kontrolü.
- Gün 5: `TodayScreen.kt` ve `AppNavigation.kt` içindeki tüm hardcoded string'leri `strings.xml`'e taşı.

## 2 Haftalık Düzeltme Planı
- Hafta 1: Yukarıdaki 1 haftalık plan + `TodaySheets.kt` bölme.
- Hafta 2: `OnboardingScreen.kt` bölme, `AppTopBar`/`TodayColorTokens` renk merkezileştirmesi, kritik component'ler için `@Preview` seti, kalan hardcoded string/dp temizliği ve (mümkünse) Detekt custom rule ekleme.

## Final Karar
Refactor. Mimari temel (Faz A-D) sağlam ve gerçek; ancak Faz E'nin "tamamlandı" olarak işaretlenmesi mevcut kod durumuyla örtüşmüyor — `TodayScreen`/`TodayViewModel`/`TodaySheets`/`OnboardingScreen` hâlâ 200 satır kuralının çok üzerinde ve hardcoded string/dp/renk kullanımı yaygın. Yayına çıkmadan önce en azından P0 maddeleri (Today akışının bölünmesi ve hardcoded string temizliği) tamamlanmalı; bunlar tamamlanmadan "Faz E bitti" iddiası CLAUDE.md'nin kendi Demir Kuralları'yla çelişmeye devam eder.
