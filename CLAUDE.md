# BenimGünlerim — Agent Kılavuzu

**Her agent bu dosyayı önce okur. Buradaki kurallar DESIGN.md'deki tasarım sözleşmesiyle birlikte bağlayıcıdır.**

---

## Proje Nedir

BenimGünlerim: Android (Jetpack Compose, Material3) günlük yaşam takip uygulaması.
Dil: Türkçe. Tek kullanıcı. Gamification (XP, level, streak, başarım).

---

## Mimari Durumu — Aktif Yeniden Yapılanma

Proje **7 katmanlı modüler mimari**ye geçiş sürecinde.

### Faz Durumu

| Faz | Ne | Durum |
|-----|-----|-------|
| **A** | Token sistemi + tema | ✅ TAMAMLANDI (açık mod) — karanlık mod token'ları kodda mevcut ama özellik bilinçli olarak kapalı, bkz. not aşağıda |
| **B** | Core atomlar + gamification atomlar | ✅ TAMAMLANDI |
| **C** | Moleküller + layout katmanı | ✅ TAMAMLANDI |
| **D** | Organizmalar (paylaşımlı bloklar) | ✅ TAMAMLANDI |
| **E** | Ekranların baştan yazılması + Common.kt silme | 🟡 KISMEN TAMAMLANDI |

> **Karanlık mod — bilinçli olarak kapalı (2026-08-18 itibarıyla sabitlendi).** `Theme.kt`'deki `BenimGunlerimTheme` her zaman `LightColors` kullanır; `themeMode` parametresi kasıtlı olarak yok sayılır (`@Suppress("UNUSED_PARAMETER")`). Bu bir eksik değil, ürün kararıdır — uygulama tutarlı ve tek bir açık temada çalışır. `UserPreferencesRepository.themeMode` alanı (DataStore + export/import) hâlâ "system/light/dark" değerini saklıyor ama hiçbir yerde render'a etki etmiyor; bu alanı kaldırmak ayrı, daha geniş kapsamlı bir karar olarak bilinçli şekilde ertelendi. Yeni kod yazarken karanlık mod için ayrı bir renk şeması/dal eklemeyin — `LightColors` tek kaynak.
>
> Projenin 7 katmanlı mimariye geçişinin iskeleti (Faz A-D) ve `Common.kt`'nin silinmesi tamamlandı (30 Temmuz 2026). Faz E'nin "Demir Kural #5" (ekran dosyaları ≤ 200 satır) maddesi büyük ölçüde sağlandı (17 Ağustos 2026 sprinti): `TodayScreen.kt` (536→133), `TaskDetailSheet.kt` (414→199) ve `CloseDaySheet.kt` (326→137) organizma/alt-bileşen dosyalarına bölündü; `TodayViewModel.kt` (743→397) `TodayTaskActions`/`TodayRoutineActions`/`TodayDayCloseActions` collaborator sınıflarına delege edildi. Tek istisna: `TodayViewModel.kt` hâlâ 200 satırın üzerinde — 22 bağımlılıklı tek bir Hilt sınıfı olduğu için (constructor + 3 collaborator kurulumu + StateFlow birleştirme + delegasyon metotları), daha fazla küçültmek use case'leri bir facade nesnesine toplamak gibi daha büyük bir mimari değişiklik gerektirir; bu bilinçli olarak ertelendi. Yeni bir ekran/organizma yazarken referans almadan önce güncel satır sayısını kontrol edin.

---

## 7 Katmanlı Mimari

```
Katman 0 — ui/theme/DesignTokens.kt       ← HAM SAYILAR (dp, ms)
Katman 1 — ui/theme/Theme.kt              ← MaterialTheme, renk, tipografi, shape
Katman 2 — ui/components/core/            ← domain'siz atomlar (Button, Chip, Badge...)
Katman 3 — ui/components/gamification/    ← XpBadge, StreakBadge, LevelBadge, GoldBadge
Katman 4 — ui/components/molecules/       ← 2+ ekranda kullanılan ortak bloklar
Katman 5 — ui/components/organisms/       ← TaskRow, RoutineRow, WeekPicker, AddTaskSheet...
Katman 6 — ui/screens/[ekran]/            ← sadece kompozisyon, ViewModel ayrı
```

---

## Token Hızlı Başvuru

### Spacing (AppTokens.Spacing)
```
xxs = 4dp   xs = 8dp   sm = 12dp   md = 16dp   lg = 20dp   xl = 24dp   xxl = 32dp
screenHorizontal = 20dp   cardInner = 16dp   cardInnerHero = 20dp   sectionGap = 24dp
```

### Radius (AppTokens.Radius)
```
xs = 4dp   sm = 8dp   md = 12dp   lg = 16dp   xl = 20dp   xxl = 28dp   pill = 99dp
```

### Motion (AppTokens.Motion)
```
fast = 150ms   normal = 300ms   slow = 500ms   pulse = 800ms
```

### IconSize (AppTokens.IconSize)
```
xs = 16dp   sm = 20dp   md = 24dp   lg = 32dp   xl = 48dp
```

---

## Faz Detayları

### Faz B — Core Atomlar + Gamification Atomlar

**Konum:** `ui/components/core/` ve `ui/components/gamification/`  
**Kural:** Bu dosyalar başka bir projeye kopyalanabilmeli. Domain bilgisi (görev, rutin, XP) sadece `gamification/`'da.

#### `ui/components/core/AppSurface.kt`
```kotlin
@Composable
fun AppSurface(
    modifier: Modifier = Modifier,
    radius: Dp = AppTokens.Radius.md,
    color: Color = MaterialTheme.colorScheme.surface,
    borderColor: Color = MaterialTheme.colorScheme.outlineVariant,
    elevation: Dp = AppTokens.Elevation.flat,
    content: @Composable () -> Unit,
)
```
Tüm ekranlarda kart yüzeyi için kullanılır. Common.kt'deki `SurfaceCard` bu bileşenin yerine geçer.

#### `ui/components/core/AppButton.kt`
```kotlin
enum class AppButtonVariant { Primary, Secondary, Ghost, Danger }

@Composable
fun AppButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    variant: AppButtonVariant = AppButtonVariant.Primary,
    enabled: Boolean = true,
    leadingIcon: ImageVector? = null,
)
```

#### `ui/components/core/AppChip.kt`
```kotlin
@Composable
fun AppFilterChip(
    label: String,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    leadingIcon: ImageVector? = null,
)

@Composable
fun AppChoiceChip(
    label: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
)
```

#### `ui/components/core/AppBadge.kt`
```kotlin
enum class AppBadgeVariant { Success, Warning, Error, Info, Neutral }

@Composable
fun AppBadge(
    text: String,
    modifier: Modifier = Modifier,
    variant: AppBadgeVariant = AppBadgeVariant.Neutral,
    leadingIcon: ImageVector? = null,
)
```

#### `ui/components/core/AppDivider.kt`
```kotlin
@Composable
fun AppDivider(modifier: Modifier = Modifier)

@Composable
fun AppVerticalDivider(modifier: Modifier = Modifier)
```

#### `ui/components/core/AppIcon.kt`
```kotlin
@Composable
fun AppIcon(
    icon: ImageVector,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    size: Dp = AppTokens.IconSize.md,
    tint: Color = LocalContentColor.current,
)
```

#### `ui/components/gamification/XpBadge.kt`
Common.kt'deki `XpBadge`'i buraya taşı. XP değeri + altın rengi pill badge.

#### `ui/components/gamification/StreakBadge.kt`
Common.kt'deki `StreakBadge`'i buraya taşı. Gün sayısı + ateş emoji.

#### `ui/components/gamification/LevelBadge.kt`
Common.kt'deki `LevelBadge`'i buraya taşı.

#### `ui/components/gamification/GoldBadge.kt`
Common.kt'deki `GoldBadge`'i buraya taşı.

#### `ui/components/gamification/CompanionBubble.kt`
Common.kt'deki `CompanionBubble`'ı buraya taşı.

#### `ui/components/gamification/LevelDots.kt`
Common.kt'deki `LevelDots`'ı buraya taşı.

**Faz B bittikten sonra:** Common.kt'den yukarıdaki bileşenlerin orijinalleri silinir. Import'lar güncellenir.

---

### Faz C — Moleküller + Layout Katmanı

**Konum:** `ui/components/molecules/` ve `ui/components/layout/`  
**Kural:** Her molekül en az 2 ekranda kullanılmalı. Tek ekrana özgü karmaşık yapılar organisms'e gider.

#### `ui/components/molecules/SectionBlock.kt`
En kritik molekül — tüm ekranlar kullanır.
```kotlin
@Composable
fun SectionBlock(
    title: String,
    modifier: Modifier = Modifier,
    trailingContent: (@Composable () -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
)
```
Common.kt'deki `SectionHeader` + içerik pattern'ini birleştirir.

#### `ui/components/molecules/MetricCard.kt`
Common.kt'deki `MetricCard`'ı buraya taşı. İkon + etiket + değer.

#### `ui/components/molecules/StatPill.kt`
Common.kt'deki `StatPill`'i buraya taşı.

#### `ui/components/molecules/ProgressBar.kt`
Common.kt'deki `AnimatedProgressBar`'ı buraya taşı. `progress: Float, label: String?` parametreleriyle.

#### `ui/components/molecules/EmptyState.kt`
Common.kt'deki `EmptyStateView`'ı buraya taşı.
```kotlin
@Composable
fun EmptyState(
    emoji: String,
    title: String,
    description: String,
    modifier: Modifier = Modifier,
    action: (@Composable () -> Unit)? = null,
)
```

#### `ui/components/molecules/AlertBanner.kt`
Common.kt'deki `AlertCard` + `WarningCard`'ı tek bileşende birleştir.
```kotlin
enum class AlertBannerSeverity { Info, Warning, Error }

@Composable
fun AlertBanner(
    message: String,
    severity: AlertBannerSeverity = AlertBannerSeverity.Info,
    modifier: Modifier = Modifier,
    action: (() -> Unit)? = null,
    actionLabel: String? = null,
)
```

#### `ui/components/molecules/ColorRailCard.kt`
SettingsCard + RoutineItemCard'ın sol rail pattern'ini evrensel hale getirir.
**KRİTİK:** Sabit yükseklik yasak. `IntrinsicSize.Min` kullan.
```kotlin
@Composable
fun ColorRailCard(
    railColor: Color,
    modifier: Modifier = Modifier,
    railWidth: Dp = 5.dp,
    content: @Composable ColumnScope.() -> Unit,
)
// İçeride: Row(Modifier.fillMaxWidth().height(IntrinsicSize.Min))
//   Box(Modifier.width(railWidth).fillMaxHeight().background(railColor))
//   Column { content() }
```

#### `ui/components/molecules/BarChart.kt`
ProgressScreen'deki inline bar chart'ı genel amaçlı hale getir.
```kotlin
data class BarChartEntry(val label: String, val value: Float, val color: Color)

@Composable
fun BarChart(
    entries: List<BarChartEntry>,
    maxValue: Float,
    modifier: Modifier = Modifier,
    barWidth: Dp = AppTokens.Spacing.xl,
    maxBarHeight: Dp = 80.dp,
)
```

#### `ui/components/molecules/InfoCard.kt`
Common.kt'deki `InfoCard`'ı buraya taşı.

#### `ui/components/layout/ScreenScaffold.kt`
Tüm ekranların ortak iskeleti.
```kotlin
@Composable
fun ScreenScaffold(
    modifier: Modifier = Modifier,
    floatingActionButton: (@Composable () -> Unit)? = null,
    content: @Composable (PaddingValues) -> Unit,
)
// İçeride: background + contentPadding (start=screenHorizontal, end=screenHorizontal, bottom=140dp)
```

#### `ui/components/layout/AppTopBar.kt`
AppNavigation.kt'deki `AppTopBar`'ı buraya taşı.

---

### Faz D — Organizmalar

**Konum:** `ui/components/organisms/`  
**Kural:** Lambda ile aksiyon yukarı ilet. ViewModel'e bağlanma. State dışarıdan gelir.

#### `ui/components/organisms/TaskRow.kt`
TodayScreen + PlanScreen'deki task satırlarını birleştirir.
```kotlin
@Composable
fun TaskRow(
    title: String,
    isCompleted: Boolean,
    onToggleComplete: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    dueTime: String? = null,
    priority: TaskPriority? = null,
    tags: List<String> = emptyList(),
)
```
SwipeToDismissBox + CheckCircle animasyonu buraya taşınır.

#### `ui/components/organisms/RoutineRow.kt`
Today + Routines ekranlarındaki rutin satırını birleştirir.
```kotlin
@Composable
fun RoutineRow(
    title: String,
    isCompletedToday: Boolean,
    onToggle: () -> Unit,
    weekHistory: List<Boolean>, // 7 günlük geçmiş
    modifier: Modifier = Modifier,
    color: Color = MaterialTheme.colorScheme.primary,
)
```

#### `ui/components/organisms/WeekPicker.kt`
Plan + Routines'teki hafta seçiciyi birleştirir.
```kotlin
@Composable
fun WeekPicker(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier,
    taskCountByDate: Map<LocalDate, Int> = emptyMap(),
)
```

#### `ui/components/organisms/AddTaskSheet.kt`
Today FAB + Plan FAB'ın paylaşımlı görev ekleme formu.

#### `ui/components/organisms/AddRoutineSheet.kt`
Routines FAB'ının rutin ekleme formu.

#### `ui/components/organisms/HeaderProgressCard.kt`
Today ekranının üst kısmı: streak + mood + dairesel progress.

#### `ui/components/organisms/CloseDayCard.kt`
Today ekranının gün kapama kartı (koyu yüzey — `Color(0xFF1B2730)` bu bileşende kalabilir).

#### `ui/components/organisms/LevelHeroCard.kt`
Progress ekranının gradient hero banner'ı.

#### `ui/components/organisms/ShopItemCard.kt`
Dükkan ekranının satın alma kartı.

#### `ui/components/organisms/AchievementRow.kt`
Başarımlar + Progress ekranlarında ortak satır. Unlock/locked durumu parametrik.

---

### Faz E — Ekranları Baştan Yaz

**Sıra:** Today → Plan → Routines → Progress → Achievements → Shop → Settings  
**Kural:** Her ekran ≤ 200 satır. Sıfır DP literal. Sıfır hardcoded renk. Sıfır hardcoded string.  
**Her ekran bittikten sonra:** Common.kt'den o ekrana ait inline bileşenler silinir.  
**Faz E tamamlandığında:** `Common.kt` tamamen silinir, AppNavigation.kt `navigation/` altına taşınır.

---

## Demir Kurallar — İstisna Yok

1. **Ekran/bileşen dosyasında DP literal yasak.** `AppTokens.Spacing.md` kullan.
2. **Ekran/bileşen dosyasında hardcoded renk yasak.** `MaterialTheme.colorScheme.*` kullan.
3. **Kullanıcıya gösterilen tüm metinler `strings.xml`'den gelir.** Hardcoded Türkçe metin yasak.
4. **İki ekranda görünen her yapı `molecules/` veya `organisms/`'te olmalı.**
5. **Ekran dosyaları 200 satırı geçemez.** Geçiyorsa bir bileşen hâlâ organisms'e taşınmamış.
6. **Organizmalar state tutmaz.** State parametre gelir, aksiyon lambda ile yukarı gider.
7. **Sabit yükseklik (`.height(Xdp)`) içerik kapsayıcılarında yasak.** `IntrinsicSize.Min` kullan.
8. **`Common.kt` gibi "her şey" dosyası oluşturma.** Her bileşen kendi dosyasında.

---

## Hedef Dosya Hiyerarşisi (Faz E sonrası)

```
ui/
  theme/
    DesignTokens.kt   ← token sabitleri
    Theme.kt          ← BenimGunlerimTheme, renkler, tipografi
  components/
    core/             ← AppSurface, AppButton, AppChip, AppBadge, AppDivider, AppIcon
    gamification/     ← XpBadge, StreakBadge, LevelBadge, GoldBadge, CompanionBubble
    molecules/        ← MetricCard, StatPill, ProgressBar, SectionBlock, SectionHeader,
                         InfoCard, EmptyState, AlertBanner, BarChart, ColorRailCard
    organisms/        ← TaskRow, RoutineRow, WeekPicker, AddTaskSheet, AddRoutineSheet,
                         HeaderProgressCard, CloseDayCard, LevelHeroCard,
                         ShopItemCard, AchievementRow
    layout/           ← ScreenScaffold, AppTopBar
  screens/
    today/            ← TodayScreen, TodayViewModel, TodayUiState
    plan/             ← PlanScreen, PlanViewModel, PlanUiState
    routines/         ← RoutinesScreen, RoutinesViewModel, RoutinesUiState, RoutineDetailScreen
    progress/         ← ProgressScreen, ProgressViewModel, ProgressUiState
    achievements/     ← AchievementsScreen
    shop/             ← ShopScreen, ShopViewModel
    settings/         ← SettingsScreen, SettingsViewModel
    onboarding/       ← OnboardingScreen
  navigation/
    AppNavigation.kt  ← NavHost
    AppScaffold.kt    ← tek Scaffold (BottomNav + TopBar)
    Routes.kt         ← sealed object rota tanımları
```

---

## Mevcut Durum — Geçiş Süreci

Faz B-E tamamlanana kadar ekranlar eski yapıda kalmaya devam edebilir. Her fazda:
- Yeni bileşenler doğru konuma yazılır
- Ekranlar o bileşeni kullanacak şekilde güncellenir
- Common.kt'den ilgili parçalar kaldırılır
- Uygulama her fazda derlenebilir olmaya devam eder

---

## Ekran Başına Hangi Bileşenler

| Ekran | Ana bileşenler |
|-------|---------------|
| **Bugün** | HeaderProgressCard, SectionBlock×3, TaskRow, RoutineRow, CloseDayCard, AddTaskSheet |
| **Plan** | WeekPicker, SectionBlock, TaskRow, AddTaskSheet |
| **Rutinler** | SectionBlock, RoutineRow (ColorRailCard içinde), AddRoutineSheet |
| **İlerleme** | LevelHeroCard, MetricCard×4, BarChart, ProgressBar×2, AchievementRow mini |
| **Başarımlar** | StatPill×3, AchievementRow×N |
| **Dükkan** | GoldBadge, InfoCard (günlük hediye), SectionBlock×N, ShopItemCard×N |
| **Ayarlar** | ColorRailCard×4, AppChip, MetricCard×N, AppButton (Danger) |

---

## Tasarım Standardı

Tam tasarım sözleşmesi `DESIGN.md`'de. Yeni veya değişen her ekran önce `DESIGN.md`'yi kontrol eder.

---

## Graphify

```
graphify query "<soru>"          ← mimari soru
graphify path "<A>" "<B>"        ← ilişki araştırma
graphify explain "<kavram>"      ← odaklı açıklama
graphify update .                ← kod değişikliği sonrası güncelle
```
