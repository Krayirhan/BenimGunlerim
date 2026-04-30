# Benim Günlerim — Today/Bugün UI İyileştirme Sprint Planı

## 0. Dokümanın Amacı

Bu doküman, **Benim Günlerim** uygulamasındaki **Today/Bugün ekranının UI tarafını** daha sade, sürdürülebilir, test edilebilir ve product-ready hale getirmek için hazırlanmış detaylı sprint planıdır.

Bugün ekranı şu anda ürün fikrini güçlü şekilde taşıyor: günlük özet, görevler, rutinler, geciken işler, tamamlananlar, hızlı ekleme, missed day yönetimi ve gün kapatma akışı mevcut. Ancak UI kodu ve kullanıcı deneyimi tarafında bazı sorunlar var:

- Ekran yoğun ve kalabalıklaşmaya açık.
- `TodayScreen.kt` çok fazla sorumluluk taşıyor.
- `TodaySheets.kt` büyük ve karmaşık.
- Görev ekleme sheet'i hızlı kullanım için biraz ağır.
- Görev/rutin satırları fazla bilgi gösteriyor.
- Tema/renk sistemi tam merkezi değil.
- Erişilebilirlik ve test tag sistemi daha sistemli olmalı.
- Gün kapatma akışı ürün olarak güçlü ama component bazında bölünmeli.

Bu planın hedefi, Today UI puanını yaklaşık **7.4 / 10** seviyesinden **8.7+ / 10** seviyesine çıkarmaktır.

---

# 1. Mevcut Durum Özeti

## 1.1 Today ekranının güçlü tarafları

Bugün ekranı ürün olarak doğru yere konumlanmış. Kullanıcı güne bu ekranla başlıyor, gün içindeki görev/rutin akışını buradan yönetiyor ve günü kapatma deneyimini yine buradan tamamlıyor.

Güçlü taraflar:

- Görev ve rutinler aynı ekranda yönetiliyor.
- Geciken işler gizlenmiyor, görünür tutuluyor.
- Gecikenleri bugüne veya yarına taşıma aksiyonları var.
- Tamamlanan işler ayrı gösteriliyor.
- Boş ekran onboarding’i var.
- Gün kapatma akışı sadece iş tamamlama değil; ruh hali, enerji, iyi an, zorlayan şey ve yarın niyeti gibi davranışsal verileri de topluyor.
- Reward, XP, streak, achievement ve confetti gibi motivasyon katmanları mevcut.
- Snapshot error banner ve retry gibi hata yönetimi düşünülmüş.
- Undo snackbar mantığı var.
- Bazı accessibility detayları düşünülmüş.

## 1.2 Today ekranının ana sorunları

| Sorun | Etki | Öncelik |
|---|---|---:|
| `TodayScreen.kt` çok büyük | Bakım zorlaşır | Çok yüksek |
| `TodaySheets.kt` çok karmaşık | Form/sheet geliştirme zorlaşır | Çok yüksek |
| UI fazla kalabalık | Kullanıcı nereden başlayacağını şaşırabilir | Çok yüksek |
| Add task sheet ağır | Hızlı görev ekleme hissi zayıflar | Yüksek |
| Task row fazla bilgi taşıyor | Mobilde hızlı tarama zorlaşır | Yüksek |
| Routine row fazla sorumluluk taşıyor | Component test/previews zorlaşır | Orta-yüksek |
| Renkler merkezi değil | Dark mode/tema yönetimi zorlaşır | Orta-yüksek |
| Dynamic testTag eksik | UI testleri zorlaşır | Orta |
| TalkBack/onClickLabel eksikleri olabilir | Erişilebilirlik zayıflar | Orta |
| CloseDaySheet tek parça | Geliştirme ve test zorlaşır | Orta-yüksek |

---

# 2. Nihai Hedef

Bu sprintler tamamlandığında Today ekranı şu seviyeye gelmeli:

| Alan | Mevcut | Hedef |
|---|---:|---:|
| Genel UI puanı | 7.4 / 10 | 8.7+ / 10 |
| Kod okunabilirliği | Orta | Yüksek |
| Component ayrımı | Zayıf-orta | Güçlü |
| Görev ekleme deneyimi | Form ağırlıklı | Hızlı + gelişmiş |
| Liste yoğunluğu | Yüksek | Kontrollü / odaklı |
| Gün kapatma deneyimi | Güçlü ama tek parça | Güçlü ve modüler |
| Tema sistemi | Kısmen dağınık | Merkezi |
| Test edilebilirlik | Orta | Yüksek |
| Accessibility | Başlangıç var | Product-ready |

---

# 3. Genel Sprint Yapısı

Varsayım:

- **1 sprint = 1 hafta**
- Tek kişi çalışıyorsa toplam süre: yaklaşık **6 hafta + hazırlık**
- Ekip çalışıyorsa paralel ticketlarla **3-4 hafta** içinde tamamlanabilir.

## Sprint listesi

| Sprint | Başlık | Süre | Ana hedef |
|---|---|---:|---|
| Sprint 0 | Hazırlık ve güvenlik ağı | 2-3 gün | Refactor öncesi davranışları korumak |
| Sprint 1 | TodayScreen refactor | 1 hafta | Büyük dosyayı parçalamak |
| Sprint 2 | Sheet/form sadeleştirme | 1 hafta | Hızlı görev ekleme deneyimini iyileştirmek |
| Sprint 3 | Liste yoğunluğu ve odak modu | 1 hafta | Ekranı daha okunabilir yapmak |
| Sprint 4 | Gün kapatma refactor ve polish | 1 hafta | Close day akışını modüler ve güçlü yapmak |
| Sprint 5 | Tema/renk sistemi | 3-5 gün | Renkleri merkezi sisteme taşımak |
| Sprint 6 | Accessibility ve UI test | 1 hafta | Product-ready kalite kontrolü yapmak |

---

# 4. Sprint 0 — Hazırlık ve Güvenlik Ağı

## 4.1 Amaç

Kodları parçalamadan önce mevcut davranışı belgelemek, preview/test altyapısı hazırlamak ve refactor sırasında kırılmaları yakalayacak güvenlik ağı kurmak.

Bu sprint doğrudan kullanıcıya görünen bir özellik üretmez ama sonraki tüm sprintlerin sağlıklı ilerlemesini sağlar.

## 4.2 Yapılacak işler

### 4.2.1 Mevcut kullanıcı akışlarını belgelemek

Aşağıdaki davranışlar tek tek kontrol edilmeli:

```text
[ ] Boş Today ekranı açılıyor mu?
[ ] Empty state CTA butonları görünüyor mu?
[ ] FAB tıklanınca AddTaskSheet açılıyor mu?
[ ] Görev başlığı girilmeden kaydetme engelleniyor mu?
[ ] Geçersiz saat girilince hata mesajı çıkıyor mu?
[ ] Saat boş bırakılınca görev ekleme davranışı istenen gibi mi?
[ ] Reminder sadece geçerli saat varsa aktif oluyor mu?
[ ] Görev başarıyla ekleniyor mu?
[ ] Görev tamamlanınca undo snackbar çıkıyor mu?
[ ] Undo ile görev tekrar pending hale geliyor mu?
[ ] Görev silinince undo snackbar çıkıyor mu?
[ ] Undo ile silinen görev geri geliyor mu?
[ ] Completed görev silinirken ekstra onay çıkıyor mu?
[ ] Görev detay sheet’i açılıyor mu?
[ ] Görev tarih/saat/kategori/not/öncelik güncelleniyor mu?
[ ] Subtask ekleniyor mu?
[ ] Subtask tamamlanıyor mu?
[ ] Subtask siliniyor mu?
[ ] Geciken görev bugüne taşınıyor mu?
[ ] Tüm gecikenler bugüne taşınıyor mu?
[ ] Tüm gecikenler yarına taşınıyor mu?
[ ] Rutin tamamlanıyor mu?
[ ] Hedefli rutin artır/azalt çalışıyor mu?
[ ] Rutin detail navigation çalışıyor mu?
[ ] Gün kapatma sheet’i açılıyor mu?
[ ] Gün kapatma step geçişleri çalışıyor mu?
[ ] Gün kapatma kaydediliyor mu?
[ ] Gecikenleri taşı seçeneği çalışıyor mu?
[ ] Missed day banner görünüyor mu?
[ ] Missed day review sheet açılıyor mu?
[ ] Missed day skip/auto save çalışıyor mu?
[ ] Snapshot error banner görünürse retry çalışıyor mu?
[ ] Reward/confetti/level up overlay tetikleniyor mu?
```

### 4.2.2 Test tag standardı oluşturmak

Yeni dosya önerisi:

```text
ui/TestTags.kt
```

veya Today özelinde:

```text
ui/today/TodayTestTags.kt
```

Önerilen yapı:

```kotlin
object TodayTestTags {
    const val Root = "today_root"
    const val Header = "today_header"
    const val SummaryText = "today_summary_text"

    const val EmptyState = "today_empty_state"
    const val AddFab = "today_add_fab"

    const val SnapshotErrorBanner = "today_snapshot_error_banner"

    const val OverdueSection = "today_overdue_section"
    const val TasksSection = "today_tasks_section"
    const val RoutinesSection = "today_routines_section"
    const val CompletedSection = "today_completed_section"
    const val CloseDayCard = "today_close_day_card"
    const val MissedDayBanner = "today_missed_day_banner"

    const val AddTaskSheet = "today_add_task_sheet"
    const val TaskDetailSheet = "today_task_detail_sheet"
    const val CloseDaySheet = "today_close_day_sheet"

    fun taskRow(id: String) = "today_task_$id"
    fun routineRow(id: String) = "today_routine_$id"
    fun overdueRow(id: String) = "today_overdue_$id"
    fun completedTaskRow(id: String) = "today_completed_task_$id"
    fun completedRoutineRow(id: String) = "today_completed_routine_$id"
}
```

### 4.2.3 Preview data oluşturmak

Yeni dosya önerisi:

```text
ui/today/preview/TodayPreviewData.kt
```

Örnek içerik:

```kotlin
object TodayPreviewData {
    val today = LocalDate.of(2026, 4, 30)

    val pendingTask = TodayTaskUi(
        id = "task_1",
        title = "İngilizce paragraf çalış",
        note = "YDS için 20 soru çöz",
        plannedDate = "2026-04-30",
        startTime = "19:00",
        category = "Ders",
        color = null,
        priority = 1,
        completionState = "pending",
        reminderTime = "19:00"
    )

    val completedTask = TodayTaskUi(
        id = "task_2",
        title = "Market alışverişi",
        note = null,
        plannedDate = "2026-04-30",
        startTime = "16:00",
        category = "Market",
        color = null,
        priority = 2,
        completionState = "completed",
        reminderTime = null
    )

    val overdueTask = TodayTaskUi(
        id = "task_3",
        title = "Fatura öde",
        note = "Elektrik faturası",
        plannedDate = "2026-04-28",
        startTime = null,
        category = "Finans",
        color = null,
        priority = 1,
        completionState = "pending",
        reminderTime = null
    )

    val checkRoutine = TodayRoutineUi(
        id = "routine_1",
        name = "30 dk yürüyüş",
        preferredTime = "18:00",
        color = null,
        targetType = "check",
        targetValue = null,
        targetUnit = null,
        currentStreak = 5,
        bestStreak = 12
    )

    val targetRoutine = TodayRoutineUi(
        id = "routine_2",
        name = "Su iç",
        preferredTime = null,
        color = null,
        targetType = "amount",
        targetValue = 2000,
        targetUnit = "ml",
        currentStreak = 3,
        bestStreak = 9
    )
}
```

### 4.2.4 Minimum preview listesi

Aşağıdaki preview’lar hazırlanmalı:

```text
[ ] EmptyTodayPreview
[ ] LoadingTodayPreview
[ ] ErrorTodayPreview
[ ] FullTodayPreview
[ ] OverdueHeavyTodayPreview
[ ] CompletedHeavyTodayPreview
[ ] ClosedDayPreview
[ ] AddTaskSheetPreview
[ ] TaskDetailSheetPreview
[ ] CloseDaySheetPreview
```

## 4.3 Kabul kriterleri

```text
[ ] Mevcut davranış checklist’i çıkarıldı.
[ ] Preview data hazırlandı.
[ ] En az 5 temel preview oluşturuldu.
[ ] Test tag standardı belirlendi.
[ ] Refactor öncesi uygulama build alıyor.
```

## 4.4 Beklenen etki

Bu sprint UI puanını doğrudan artırmaz ama tüm refactor sürecinin güvenli ilerlemesini sağlar.

---

# 5. Sprint 1 — TodayScreen Refactor

## 5.1 Amaç

`TodayScreen.kt` dosyasının sorumluluklarını azaltmak ve component yapısını temizlemek.

Şu an `TodayScreen.kt` içinde birçok farklı görev aynı dosyada duruyor:

- ViewModel state toplama
- Snackbar/effect yönetimi
- Modal sheet state yönetimi
- Reward/confetti/achievement overlay yönetimi
- FAB yönetimi
- LazyColumn layout
- Header card
- Today list
- Task row
- Routine row
- Overdue row
- Completed section
- Close day card
- Missed day banner
- Utility fonksiyonları
- Comparator fonksiyonları
- Renk parse fonksiyonu

Bu yapı büyüdükçe ekrana yeni özellik eklemek riskli hale gelir.

## 5.2 Hedef dosya yapısı

```text
ui/today/
  TodayRoute.kt
  TodayScreen.kt
  TodayActions.kt

  components/
    TodayHeaderCard.kt
    TodayContent.kt
    TodayList.kt
    TodaySectionCard.kt
    TodayTaskRow.kt
    TodayRoutineRow.kt
    TodayOverdueSection.kt
    TodayCompletedSection.kt
    TodayEmptyState.kt
    TodayCloseDayCard.kt
    TodayMissedDayBanner.kt
    TodaySnapshotErrorBanner.kt
    TodayCommonComponents.kt

  sheets/
    AddTaskSheet.kt
    TaskDetailSheet.kt
    CloseDaySheet.kt

  model/
    TodayUiModels.kt
    TodayUiState.kt
    TodayUiEffect.kt

  util/
    TodayColors.kt
    TodayComparators.kt
    TodayColorParser.kt
    CategoryPalette.kt
```

## 5.3 Ticket 1.1 — TodayRoute oluştur

### Amaç

ViewModel bağımlılığını ve state collect işlemini UI layout’tan ayırmak.

### Mevcut problem

`TodayScreen` doğrudan ViewModel alıyor ve state/effect/sheet yönetimi aynı yerde yapılıyor. Bu durum preview almayı ve component test etmeyi zorlaştırır.

### Önerilen yapı

```kotlin
@Composable
fun TodayRoute(
    viewModel: TodayViewModel = hiltViewModel(),
    onNavigateToRoutines: () -> Unit = {},
    onNavigateToPlan: () -> Unit = {},
    onOpenRoutineDetail: (String) -> Unit = {},
) {
    val state by viewModel.uiState.collectAsState()
    val today = viewModel.today()

    TodayScreen(
        state = state,
        today = today,
        actions = TodayActions.from(viewModel),
        gameEvents = viewModel.gameEvents,
        uiEffects = viewModel.uiEffects,
        onNavigateToRoutines = onNavigateToRoutines,
        onNavigateToPlan = onNavigateToPlan,
        onOpenRoutineDetail = onOpenRoutineDetail,
    )
}
```

Alternatif olarak `TodayActions.from(viewModel)` extension yerine route içinde açıkça map edilebilir.

### Kabul kriterleri

```text
[ ] TodayRoute oluşturuldu.
[ ] hiltViewModel sadece route seviyesinde kullanılıyor.
[ ] TodayScreen state ve action parametreleriyle çalışıyor.
[ ] TodayScreen preview alınabilir hale geldi.
```

## 5.4 Ticket 1.2 — TodayActions oluştur

### Amaç

Callback sayısını azaltmak ve alt componentlere daha temiz action geçirmek.

### Önerilen yapı

```kotlin
data class TodayActions(
    val onAddTask: (
        title: String,
        note: String?,
        date: LocalDate,
        startTime: String?,
        category: String?,
        priority: Int,
        reminderTime: String?
    ) -> Unit,

    val onToggleTask: (taskId: String) -> Unit,
    val onDeleteTask: (taskId: String) -> Unit,
    val onRestoreDeletedTask: (taskId: String) -> Unit,
    val onUndoTaskToggle: (taskId: String) -> Unit,

    val onUpdateTask: (
        taskId: String,
        title: String,
        note: String?,
        date: LocalDate,
        startTime: String?,
        category: String?,
        priority: Int,
        reminderTime: String?
    ) -> Unit,

    val onMoveTaskToTomorrow: (taskId: String) -> Unit,
    val onMoveTaskToDate: (taskId: String, date: LocalDate) -> Unit,
    val onMoveAllOverdueTo: (date: LocalDate) -> Unit,

    val onToggleRoutine: (routineId: String, completedToday: Boolean) -> Unit,
    val onUpdateRoutineProgress: (routineId: String, value: Float, wasCompleted: Boolean) -> Unit,

    val onAddSubTask: (taskId: String, title: String) -> Unit,
    val onToggleSubTask: (SubTaskEntity) -> Unit,
    val onDeleteSubTask: (SubTaskEntity) -> Unit,

    val onCloseDay: (
        note: String,
        mood: Int,
        energy: Int,
        bestMoment: String,
        challenge: String,
        tomorrowIntention: String,
        carryOverdueToTomorrow: Boolean
    ) -> Unit,

    val onSaveMissedDay: (
        date: LocalDate,
        note: String,
        mood: Int,
        energy: Int,
        bestMoment: String,
        challenge: String,
        tomorrowIntention: String
    ) -> Unit,

    val onAutoSaveMissedDay: (LocalDate) -> Unit,
    val onRetrySnapshot: () -> Unit,
)
```

Daha temiz bir alternatif:

```kotlin
data class TodayActions(
    val task: TodayTaskActions,
    val routine: TodayRoutineActions,
    val dayClose: TodayDayCloseActions,
    val snapshot: TodaySnapshotActions,
)
```

Bu alternatif daha profesyoneldir.

### Kabul kriterleri

```text
[ ] Callback dağınıklığı azaltıldı.
[ ] TodayList’e 10+ ayrı callback yerine action grubu geçiliyor.
[ ] Alt componentlerin API yüzeyi sadeleşti.
```

## 5.5 Ticket 1.3 — Componentleri dosyalara ayır

### Taşınacak componentler

| Component | Yeni dosya |
|---|---|
| `TodayHeaderCard` | `components/TodayHeaderCard.kt` |
| `TodayList` | `components/TodayList.kt` |
| `TaskRow` | `components/TodayTaskRow.kt` |
| `SwipeableTaskRow` | `components/TodayTaskRow.kt` |
| `RoutineRow` | `components/TodayRoutineRow.kt` |
| `OverdueTaskRow` | `components/TodayOverdueSection.kt` |
| `EmptyTodayOnboarding` | `components/TodayEmptyState.kt` |
| `CloseDayCard` | `components/TodayCloseDayCard.kt` |
| `MissedDayBanner` | `components/TodayMissedDayBanner.kt` |
| `SnapshotErrorBanner` | `components/TodaySnapshotErrorBanner.kt` |
| `Pill` | `components/TodayCommonComponents.kt` |
| `MetaTag` | `components/TodayCommonComponents.kt` |
| `CheckCircle` | `components/TodayCommonComponents.kt` |
| `ItemRow` | `components/TodayCommonComponents.kt` |
| `parseColorOrNull` | `util/TodayColorParser.kt` |
| `todayTaskComparator` | `util/TodayComparators.kt` |
| `overdueTaskComparator` | `util/TodayComparators.kt` |

## 5.6 Ticket 1.4 — TodayContent oluştur

`TodayScreen` içinde LazyColumn doğrudan durmak yerine `TodayContent` componentine alınmalı.

```kotlin
@Composable
fun TodayContent(
    state: TodayUiState,
    today: LocalDate,
    dayIsClosed: Boolean,
    actions: TodayActions,
    onAddTaskClick: () -> Unit,
    onOpenTask: (String) -> Unit,
    onCloseDayClick: () -> Unit,
    onMissedDayReview: () -> Unit,
    onNavigateToRoutines: () -> Unit,
    onNavigateToPlan: () -> Unit,
    onOpenRoutineDetail: (String) -> Unit,
    contentPadding: PaddingValues,
) {
    LazyColumn(...) {
        ...
    }
}
```

## 5.7 Sprint 1 kabul kriterleri

```text
[ ] TodayScreen.kt 250-350 satır aralığına indi.
[ ] TodayRoute oluşturuldu.
[ ] TodayActions veya action grupları oluşturuldu.
[ ] Header/List/Row/Card componentleri ayrı dosyalara taşındı.
[ ] Utility fonksiyonları ayrı dosyalara taşındı.
[ ] Davranış değişmedi.
[ ] Build başarılı.
[ ] En az 5 preview çalışıyor.
```

## 5.8 Sprint 1 beklenen puan etkisi

```text
7.4 / 10 → 7.9 / 10
```

---

# 6. Sprint 2 — Sheet/Form Deneyimini Sadeleştirme

## 6.1 Amaç

Görev ekleme ve görev detay sheet’lerini daha hızlı, daha okunabilir ve daha sürdürülebilir hale getirmek.

## 6.2 Mevcut sorun

`AddTaskSheet` çok güçlü ama hızlı görev ekleme için ağır. Şu anda kullanıcı görev eklerken birçok alanla karşılaşıyor:

- Görev adı
- Saat
- Öncelik
- Gelişmiş seçenekler
- Not
- Kategori
- Hatırlatıcı
- Tarih presetleri
- Özel tarih seçimi
- Time picker
- Date picker

Bu yapı özellik açısından iyi ama Today ekranında kullanıcı çoğu zaman hızlı bir iş eklemek ister.

## 6.3 Hedef deneyim

İlk açılışta sheet sade olmalı:

```text
Görev adı
Saat
Kaydet
Gelişmiş seçenekler
```

Gelişmiş seçenekler açıldığında:

```text
Öncelik
Kategori
Not
Hatırlatıcı
Tarih
```

## 6.4 Ticket 2.1 — AddTaskSheetState oluştur

### Önerilen state

```kotlin
data class AddTaskSheetState(
    val title: String = "",
    val note: String = "",
    val time: String = "",
    val category: String = "",
    val priority: Int = 2,
    val plannedDate: LocalDate,
    val reminderEnabled: Boolean = true,
    val advancedExpanded: Boolean = false,
) {
    val hasValidTitle: Boolean
        get() = title.isNotBlank()

    val hasValidTime: Boolean
        get() = TimeInputValidator.isValid(time)

    val canSave: Boolean
        get() = hasValidTitle && hasValidTime

    val reminderCanBeEnabled: Boolean
        get() = time.isNotBlank() && hasValidTime

    val effectiveReminderEnabled: Boolean
        get() = reminderEnabled && reminderCanBeEnabled
}
```

### Event yapısı

```kotlin
sealed interface AddTaskSheetEvent {
    data class TitleChanged(val value: String) : AddTaskSheetEvent
    data class TimeChanged(val value: String) : AddTaskSheetEvent
    data class NoteChanged(val value: String) : AddTaskSheetEvent
    data class CategoryChanged(val value: String) : AddTaskSheetEvent
    data class PriorityChanged(val value: Int) : AddTaskSheetEvent
    data class PlannedDateChanged(val value: LocalDate) : AddTaskSheetEvent
    data class ReminderChanged(val enabled: Boolean) : AddTaskSheetEvent
    data object ToggleAdvanced : AddTaskSheetEvent
    data object SaveClicked : AddTaskSheetEvent
}
```

## 6.5 Ticket 2.2 — AddTaskSheet sadeleştirme

### Yeni component yapısı

```text
sheets/addtask/
  AddTaskSheet.kt
  AddTaskMainFields.kt
  AddTaskAdvancedFields.kt
  AddTaskPrioritySelector.kt
  AddTaskDateSelector.kt
  AddTaskReminderRow.kt
  AddTaskTimePickerDialog.kt
  AddTaskDatePickerDialog.kt
```

### Ana sheet örnek akış

```kotlin
@Composable
fun AddTaskSheet(
    state: AddTaskSheetState,
    onEvent: (AddTaskSheetEvent) -> Unit,
    onSave: () -> Unit,
) {
    Column(...) {
        Text("Görev ekle")

        AddTaskMainFields(
            title = state.title,
            time = state.time,
            onTitleChange = { onEvent(AddTaskSheetEvent.TitleChanged(it)) },
            onTimeChange = { onEvent(AddTaskSheetEvent.TimeChanged(it)) },
        )

        TextButton(
            onClick = { onEvent(AddTaskSheetEvent.ToggleAdvanced) }
        ) {
            Text(if (state.advancedExpanded) "Gelişmiş seçenekleri gizle" else "Gelişmiş seçenekler")
        }

        if (state.advancedExpanded) {
            AddTaskAdvancedFields(...)
        }

        Button(
            onClick = onSave,
            enabled = state.canSave,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Kaydet")
        }
    }
}
```

## 6.6 Ticket 2.3 — Hızlı input parse sistemi

İlk aşamada basit saat ayrıştırma yeterli.

### Desteklenecek formatlar

```text
18:00 yürüyüş yap
20.30 İngilizce çalış
09:15 toplantı
```

### Parse modeli

```kotlin
data class QuickTaskParseResult(
    val title: String,
    val time: String?,
)
```

### Basit parser

```kotlin
object QuickTaskParser {
    private val timePattern = Regex("""\b([01]?\d|2[0-3])[:.]([0-5]\d)\b""")

    fun parse(raw: String): QuickTaskParseResult {
        val match = timePattern.find(raw)
        val time = match?.let {
            val hour = it.groupValues[1].padStart(2, '0')
            val minute = it.groupValues[2]
            "$hour:$minute"
        }
        val title = if (match != null) {
            raw.replace(match.value, "").trim()
        } else {
            raw.trim()
        }
        return QuickTaskParseResult(title = title, time = time)
    }
}
```

Bu parser UI tarafında zorunlu değil ama kullanıcı deneyimini ciddi artırır.

## 6.7 Ticket 2.4 — TaskDetailSheet parçalama

### Mevcut sorun

TaskDetailSheet içinde şu sorumluluklar aynı yerde:

- Başlık/not düzenleme
- Tarih/saat/kategori düzenleme
- Reminder switch
- Priority selector
- Subtask listesi
- Subtask ekleme
- Subtask silme
- Görevi yarına taşıma
- Görev silme
- Time picker
- Date picker
- Delete confirm dialog

### Yeni yapı

```text
sheets/taskdetail/
  TaskDetailSheet.kt
  TaskDetailContentSection.kt
  TaskDetailPlanSection.kt
  TaskDetailOptionsSection.kt
  TaskDetailSubTaskSection.kt
  TaskDetailDangerZone.kt
  TaskDetailDialogs.kt
```

### Bölüm mantığı

```text
TaskDetailContentSection
- Başlık
- Not

TaskDetailPlanSection
- Tarih
- Saat
- Kategori

TaskDetailOptionsSection
- Reminder
- Öncelik

TaskDetailSubTaskSection
- Subtask listesi
- Subtask ekleme
- Subtask toggle/delete

TaskDetailDangerZone
- Yarına taşı
- Sil
```

## 6.8 Sprint 2 kabul kriterleri

```text
[ ] AddTaskSheetState oluşturuldu.
[ ] AddTaskSheetEvent oluşturuldu.
[ ] AddTaskSheet ilk açılışta sade görünüyor.
[ ] Not/kategori/tarih/reminder gelişmiş alanda duruyor.
[ ] Öncelik ya gelişmişte ya da daha minimal chip olarak duruyor.
[ ] TaskDetailSheet alt section componentlerine bölündü.
[ ] Saat validasyonu korundu.
[ ] Reminder logic bozulmadı.
[ ] Keyboard açıkken kaydet butonu erişilebilir.
```

## 6.9 Sprint 2 beklenen puan etkisi

```text
7.9 / 10 → 8.1 / 10
```

---

# 7. Sprint 3 — Liste Yoğunluğunu Azaltma ve Odak Modu

## 7.1 Amaç

Today ekranını yoğun günlerde daha okunabilir hale getirmek.

## 7.2 Mevcut sorun

Bugün ekranı aynı anda çok fazla bilgi gösterebiliyor:

- Geciken görevler
- Açık görevler
- Açık rutinler
- Tamamlanan görevler
- Tamamlanan rutinler
- Missed day banner
- Close day card
- Header progress
- Summary/guidance text

Bu güçlü ama yoğun günlerde kullanıcıyı yorabilir.

## 7.3 Hedef

Today ekranı üç görünüm moduna sahip olabilir:

```kotlin
enum class TodayViewMode {
    Focus,
    Normal,
    Full
}
```

| Mod | Amaç |
|---|---|
| Focus | Sadece en kritik işleri gösterir |
| Normal | Dengeli günlük görünüm |
| Full | Tüm görev/rutin detaylarını gösterir |

## 7.4 Ticket 3.1 — Focus mode ekle

### Focus mode davranışı

Focus mode’da gösterilecekler:

```text
1. Header card
2. Eğer geciken varsa: geciken özet kartı
3. En önemli 3 açık görev
4. En yakın saatli 2 rutin
5. Gün kapatma kartı
```

### Görev öncelik algoritması

```kotlin
fun List<TodayTaskUi>.focusTasks(): List<TodayTaskUi> =
    filterNot { it.isCompleted }
        .sortedWith(
            compareBy<TodayTaskUi> { it.startTime.isNullOrBlank() }
                .thenBy { it.startTime ?: "99:99" }
                .thenBy { it.priority }
        )
        .take(3)
```

Burada `isCompleted` UI modelde yoksa extension ile hesaplanabilir:

```kotlin
val TodayTaskUi.isCompleted: Boolean
    get() = completionState == TaskCompletionState.COMPLETED.value
```

### Rutin öncelik algoritması

```kotlin
fun List<TodayRoutineUi>.focusRoutines(completedRoutineIds: Set<String>): List<TodayRoutineUi> =
    filterNot { it.id in completedRoutineIds }
        .sortedWith(
            compareBy<TodayRoutineUi> { it.preferredTime.isNullOrBlank() }
                .thenBy { it.preferredTime ?: "99:99" }
        )
        .take(2)
```

## 7.5 Ticket 3.2 — Görünüm seçici ekle

Header altında veya summary bölümünde küçük segment control:

```text
[Odak] [Normal] [Tümü]
```

İlk versiyon local state olabilir:

```kotlin
var viewMode by rememberSaveable { mutableStateOf(TodayViewMode.Normal) }
```

Sonraki versiyonda DataStore’a taşınabilir.

## 7.6 Ticket 3.3 — Task row sadeleştir

### Mevcut problem

Task row’da çok fazla bilgi var:

- Başlık
- Priority chip
- Kategori chip
- Saat chip
- Not preview
- More menu
- Check circle
- Accent rail

### Yeni önerilen yapı

```text
[✓] Görev başlığı                         18:00
    Kategori · Kısa not
```

### Priority gösterimi

Priority chip yerine:

| Priority | Gösterim |
|---|---|
| 1 / Yüksek | Daha belirgin accent rail + küçük ünlem ikonu |
| 2 / Normal | Normal accent rail |
| 3 / Düşük | Daha soft accent rail |

### Yeni TaskRow layout

```text
Row
  CheckCircle
  Column(weight=1)
    Row
      Title
      TimeText
    Optional second line
  MoreButton
```

## 7.7 Ticket 3.4 — Completed section özetle

### Mevcut davranış

Tamamlananlar gösteriliyor, çoksa kısaltılıyor.

### Yeni öneri

Varsayılan görünüm:

```text
Bugün 5 şey tamamlandı       [Göster]
```

Açılmış görünüm:

```text
Tamamlananlar
- Görev 1
- Görev 2
- Rutin 1
[Daha az göster]
```

### Kabul kriteri

```text
[ ] Completed section varsayılan özet görünür.
[ ] Detaylar kullanıcı isteyince açılır.
[ ] 10+ completed item ekranı boğmaz.
```

## 7.8 Ticket 3.5 — Overdue summary card

### Mevcut sorun

Gecikenler çoksa ekranın üstü yoğunlaşabilir.

### Yeni yapı

3 veya daha fazla geciken varsa:

```text
3 geciken işin var
Birikmiş işleri bugüne veya yarına taşıyabilirsin.

[Bugüne al] [Yarına taşı] [Tek tek göster]
```

1-2 geciken varsa direkt satırlar gösterilebilir.

### State

```kotlin
var overdueExpanded by rememberSaveable { mutableStateOf(false) }
```

### Davranış

```kotlin
if (overdueTasks.size >= 3 && !overdueExpanded) {
    OverdueSummaryCard(...)
} else {
    OverdueTaskList(...)
}
```

## 7.9 Sprint 3 kabul kriterleri

```text
[ ] TodayViewMode enum oluşturuldu.
[ ] Focus/Normal/Full görünüm mantığı eklendi.
[ ] Focus mode maksimum 3 görev + 2 rutin + overdue özet gösteriyor.
[ ] Task row sadeleşti.
[ ] Priority chip azaltıldı/kaldırıldı.
[ ] Completed section varsayılan özet hale geldi.
[ ] Overdue çoksa özet kart gösteriliyor.
[ ] Normal mode eski davranışa yakın çalışıyor.
```

## 7.10 Sprint 3 beklenen puan etkisi

```text
8.1 / 10 → 8.4 / 10
```

---

# 8. Sprint 4 — Gün Kapatma Refactor ve Polish

## 8.1 Amaç

Gün kapatma akışını hem component olarak bölmek hem de kullanıcıya daha güçlü bir kapanış deneyimi vermek.

## 8.2 Mevcut güçlü taraf

Gün kapatma akışı ürünün en değerli taraflarından biri. Çünkü kullanıcıdan sadece görev durumu değil, davranışsal ve duygusal veri de topluyor:

- Bugünün özeti
- Ruh hali
- Enerji seviyesi
- En iyi an
- Zorlayan şey
- Kısa not
- Yarın için tek niyet
- Gecikenleri taşıma kararı

Bu uygulamayı sıradan todo uygulamasından ayırır.

## 8.3 Mevcut sorun

`CloseDaySheet` tek component içinde çok fazla state ve UI taşıyor:

```text
step
mood
energy
note
bestMoment
challenge
tomorrowIntention
carryTasks
summary tiles
mood selector
energy selector
reflection fields
suggestion buttons
overdue carry card
navigation buttons
```

Bu yüzden test, preview ve değişiklik yapmak zorlaşır.

## 8.4 Ticket 4.1 — CloseDaySheetState oluştur

```kotlin
data class CloseDaySheetState(
    val step: Int = 0,
    val mood: Int = 3,
    val energy: Int = 3,
    val note: String = "",
    val bestMoment: String = "",
    val challenge: String = "",
    val tomorrowIntention: String = "",
    val carryTasks: Boolean = false,
) {
    val isFirstStep: Boolean get() = step == 0
    val isLastStep: Boolean get() = step == 3
}
```

## 8.5 Ticket 4.2 — CloseDayEvent oluştur

```kotlin
sealed interface CloseDayEvent {
    data object NextClicked : CloseDayEvent
    data object BackClicked : CloseDayEvent
    data class MoodChanged(val value: Int) : CloseDayEvent
    data class EnergyChanged(val value: Int) : CloseDayEvent
    data class NoteChanged(val value: String) : CloseDayEvent
    data class BestMomentChanged(val value: String) : CloseDayEvent
    data class ChallengeChanged(val value: String) : CloseDayEvent
    data class TomorrowIntentionChanged(val value: String) : CloseDayEvent
    data class CarryTasksChanged(val value: Boolean) : CloseDayEvent
    data object SaveClicked : CloseDayEvent
}
```

## 8.6 Ticket 4.3 — Step componentlerine böl

Yeni dosya yapısı:

```text
sheets/closeday/
  CloseDaySheet.kt
  CloseDayStepIndicator.kt
  CloseDaySummaryStep.kt
  CloseDayMoodEnergyStep.kt
  CloseDayReflectionStep.kt
  CloseDayTomorrowStep.kt
  CloseDayFooterActions.kt
  CloseDayResultPreview.kt
```

### Component sorumlulukları

#### CloseDayStepIndicator

```text
4 adımlı progress bar gösterir.
```

#### CloseDaySummaryStep

```text
- Tamamlanan / toplam
- Başarı yüzdesi
- Geciken sayısı
- Progress bar
```

#### CloseDayMoodEnergyStep

```text
- Mood selector
- Energy selector
```

#### CloseDayReflectionStep

```text
- En iyi an
- Zorlayan şey
- Kısa not
```

#### CloseDayTomorrowStep

```text
- Yarın niyeti
- Öneri butonları
- Gecikenleri taşıma kartı
```

#### CloseDayResultPreview

```text
Kaydetmeden önce özet gösterir.
```

## 8.7 Ticket 4.4 — Result preview ekle

Son adımda veya kaydetmeden hemen önce şu özet gösterilebilir:

```text
Bugünün özeti

5 / 7 tamamlandı
Ruh hali: İyi
Enerji: 4 / 5
Yarın niyeti: 30 dk yürüyüş
2 geciken iş yarına taşınacak
```

Bu kullanıcıya “ne kaydediyorum?” netliği verir.

## 8.8 Ticket 4.5 — CloseDayMode ekle

Normal gün ve missed day aynı sheet’i kullanıyor olabilir. Ama metinler farklı olmalı.

```kotlin
enum class CloseDayMode {
    Today,
    MissedDay
}
```

### Kullanım

```kotlin
CloseDaySheet(
    mode = CloseDayMode.Today,
    ...
)
```

Missed day için metinler:

```text
Dünü tamamla
Kaçırdığın günü kısa bir notla kapatabilirsin.
```

Today için metinler:

```text
Günü kapat
Bugünün kısa değerlendirmesini yap.
```

## 8.9 Ticket 4.6 — Kapanış sonrası feedback

Gün kaydedildiğinde sadece snackbar yerine daha anlamlı feedback verilebilir:

```text
Gün kaydedildi
Bugün %71 tamamladın.
Yarın için niyetin hazır.
```

Bu bir dialog olmak zorunda değil. Küçük bir success card veya snackbar message yeterli olabilir.

## 8.10 Sprint 4 kabul kriterleri

```text
[ ] CloseDaySheetState oluşturuldu.
[ ] CloseDayEvent oluşturuldu.
[ ] CloseDaySheet step componentlerine bölündü.
[ ] Normal day / missed day mode ayrıldı.
[ ] Kaydetmeden önce özet preview gösteriliyor.
[ ] Geciken taşıma kararı net gösteriliyor.
[ ] Close day preview’ları alınabiliyor.
[ ] Gün kaydetme davranışı bozulmadı.
```

## 8.11 Sprint 4 beklenen puan etkisi

```text
8.4 / 10 → 8.6 / 10
```

---

# 9. Sprint 5 — Tema, Renk Sistemi ve Görsel Tutarlılık

## 9.1 Amaç

Today ekranındaki hardcoded renkleri azaltmak ve ekranın light/dark mode, marka kimliği ve uzun vadeli bakım açısından daha sağlıklı hale gelmesini sağlamak.

## 9.2 Mevcut sorun

Today ekranında tema renkleri kullanılıyor ama bazı alanlarda doğrudan hex renkler var:

- Header background
- Background gradient
- Task section background
- Routine section background
- Border renkleri
- Bazı soft accent renkleri

Bu ileride şu sorunlara yol açabilir:

- Dark mode’da kontrast bozulabilir.
- Marka rengi değişirse birçok dosyaya dokunmak gerekir.
- Aynı anlam için farklı renkler kullanılabilir.
- Component tutarlılığı azalır.

## 9.3 Ticket 5.1 — TodayColors oluştur

Yeni dosya:

```text
ui/today/theme/TodayColors.kt
```

Önerilen yapı:

```kotlin
@Immutable
data class TodayColors(
    val backgroundTop: Color,
    val backgroundBottom: Color,

    val headerBackground: Color,
    val headerBorder: Color,
    val headerContent: Color,

    val taskSectionBackground: Color,
    val taskSectionBorder: Color,
    val taskAccent: Color,

    val routineSectionBackground: Color,
    val routineSectionBorder: Color,
    val routineAccent: Color,

    val overdueBackground: Color,
    val overdueBorder: Color,
    val overdueAccent: Color,

    val completedBackground: Color,
    val completedBorder: Color,
    val completedAccent: Color,

    val chipBackground: Color,
    val chipBorder: Color,
)
```

## 9.4 Ticket 5.2 — Light/Dark renk seti

```kotlin
fun lightTodayColors() = TodayColors(
    backgroundTop = Color(0xFFF7F6FF),
    backgroundBottom = Color.White,
    headerBackground = Color(0xFF66AE90),
    headerBorder = Color(0xFF5C9F84),
    headerContent = Color.White,
    taskSectionBackground = Color(0xFFF2F5FF),
    taskSectionBorder = Color(0xFFD0DAF2),
    taskAccent = CandyPrimary,
    routineSectionBackground = Color(0xFFF2FAF4),
    routineSectionBorder = Color(0xFFCEE4D4),
    routineAccent = LevelSky,
    overdueBackground = StreakCoral.copy(alpha = 0.08f),
    overdueBorder = StreakCoral.copy(alpha = 0.22f),
    overdueAccent = StreakCoral,
    completedBackground = CompletedGreen.copy(alpha = 0.08f),
    completedBorder = CompletedGreen.copy(alpha = 0.22f),
    completedAccent = CompletedGreen,
    chipBackground = Color.White.copy(alpha = 0.90f),
    chipBorder = Color.Black.copy(alpha = 0.12f),
)
```

Dark mode ayrı hesaplanmalı. Direkt light renklerin koyulaştırılmış hali her zaman iyi sonuç vermez.

## 9.5 Ticket 5.3 — CompositionLocal veya MaterialTheme extension

```kotlin
val LocalTodayColors = staticCompositionLocalOf { lightTodayColors() }

val MaterialTheme.todayColors: TodayColors
    @Composable
    get() = LocalTodayColors.current
```

Theme içinde provide:

```kotlin
CompositionLocalProvider(
    LocalTodayColors provides if (darkTheme) darkTodayColors() else lightTodayColors()
) {
    MaterialTheme(...) {
        content()
    }
}
```

## 9.6 Ticket 5.4 — Hardcoded renkleri temizle

Örnek dönüşüm:

```kotlin
.background(Color(0xFF66AE90))
```

yerine:

```kotlin
.background(MaterialTheme.todayColors.headerBackground)
```

Gradient:

```kotlin
Brush.verticalGradient(
    listOf(
        MaterialTheme.todayColors.backgroundTop,
        MaterialTheme.todayColors.backgroundBottom,
    )
)
```

## 9.7 Ticket 5.5 — CategoryPalette düzeni

`CategoryPalette` korunmalı. Ancak iki şeye dikkat edilmeli:

1. Kategori renkleri TodayColors ile çakışmamalı.
2. Dark mode’da kategori renkleri çok düşük kontrast üretmemeli.

Öneri:

```kotlin
fun categoryAccentFor(seed: String, isDark: Boolean): Color
```

veya renkler theme üzerinden normalize edilmeli.

## 9.8 Kontrast checklist

```text
[ ] Header içinde beyaz yazı okunabilir.
[ ] Progress bar görünür.
[ ] Task section ve background ayrımı net.
[ ] Routine section ve background ayrımı net.
[ ] Overdue coral text yeterince okunabilir.
[ ] Completed item opacity çok düşmüyor.
[ ] Disabled state okunabilir.
[ ] Chip border/text kontrastı yeterli.
[ ] Dark mode’da surface/background ayrımı yeterli.
```

## 9.9 Sprint 5 kabul kriterleri

```text
[ ] TodayColors oluşturuldu.
[ ] Light renk seti oluşturuldu.
[ ] Dark renk seti oluşturuldu.
[ ] Today UI içindeki direkt hex renkler minimuma indirildi.
[ ] Header/section/card/chip renkleri merkezi hale geldi.
[ ] Dark mode manuel kontrol edildi.
[ ] Büyük font + dark mode birlikte kontrol edildi.
```

## 9.10 Sprint 5 beklenen puan etkisi

```text
8.6 / 10 → 8.75 / 10
```

---

# 10. Sprint 6 — Accessibility, UI Test ve Product-Ready Kontrol

## 10.1 Amaç

Today ekranını sadece güzel değil, erişilebilir, test edilebilir ve release-ready hale getirmek.

## 10.2 Mevcut durum

Kodda bazı iyi başlangıçlar var:

- `contentDescription`
- `semantics`
- `heading`
- `Role.Checkbox`
- `minimumInteractiveComponentSize`
- `testTag`
- Sistem animasyonları kapalıysa motion azaltma

Ama bunlar tüm componentlere sistematik uygulanmalı.

## 10.3 Ticket 6.1 — Dynamic testTag sistemi

### Eklenecek test tagler

```kotlin
Modifier.testTag(TodayTestTags.taskRow(task.id))
Modifier.testTag(TodayTestTags.routineRow(routine.id))
Modifier.testTag(TodayTestTags.overdueRow(task.id))
Modifier.testTag(TodayTestTags.CompletedSection)
Modifier.testTag(TodayTestTags.CloseDayCard)
```

### Section test tagleri

```text
[ ] Header
[ ] Summary
[ ] Overdue section
[ ] Tasks section
[ ] Routines section
[ ] Completed section
[ ] Close day card
[ ] Missed day banner
[ ] FAB
[ ] Add task sheet
[ ] Task detail sheet
[ ] Close day sheet
```

## 10.4 Ticket 6.2 — TalkBack label iyileştirme

Clickable alanlarda sadece `clickable {}` yerine label/role verilmeli.

Örnek:

```kotlin
Modifier.clickable(
    role = Role.Button,
    onClickLabel = stringResource(R.string.today_open_task_detail),
    onClick = { onOpen(task) }
)
```

Check circle için:

```kotlin
Modifier.semantics {
    role = Role.Checkbox
    contentDescription = if (done) "Görevi tamamlanmadı yap" else "Görevi tamamla"
}
```

## 10.5 Ticket 6.3 — Swipe delete alternatifi

Swipe silme iyi ama herkes için erişilebilir değil. Menüde açık alternatif olmalı.

Task overflow menüsü:

```text
Detayı aç
Yarına taşı
Sil
```

Completed task için:

```text
Detayı aç
Tamamlanmadı yap
Sil
```

## 10.6 Ticket 6.4 — Compose UI testleri

Minimum test dosyası:

```text
TodayScreenTest.kt
AddTaskSheetTest.kt
CloseDaySheetTest.kt
```

### Test 1 — Empty state

```text
Given TodayUiState empty
When TodayScreen renders
Then empty state visible
And add task CTA visible
```

### Test 2 — FAB opens sheet

```text
Given TodayScreen rendered
When user clicks FAB
Then AddTaskSheet visible
```

### Test 3 — Invalid time disables save

```text
Given AddTaskSheet open
When user enters title and invalid time
Then save button disabled or error visible
```

### Test 4 — Valid task save

```text
Given AddTaskSheet open
When user enters title and valid time
And clicks save
Then onSave called
```

### Test 5 — Task toggle

```text
Given task row visible
When user clicks check circle
Then onToggleTask called with task id
```

### Test 6 — Overdue bulk action

```text
Given overdue tasks visible
When user clicks "Bugüne al"
Then onMoveAllOverdueToday called
```

### Test 7 — Focus mode

```text
Given many tasks and routines
When user selects Focus mode
Then only limited focus items visible
```

### Test 8 — Close day step navigation

```text
Given CloseDaySheet open
When user clicks next
Then step 2 content visible
```

### Test 9 — Close day save

```text
Given CloseDaySheet final step
When user fills intention and saves
Then onSave called with mood/energy/note fields
```

### Test 10 — Snapshot retry

```text
Given snapshotLoadError true
When retry button clicked
Then onRetrySnapshot called
```

## 10.7 Ticket 6.5 — Preview matrix genişletme

Final preview listesi:

```text
[ ] EmptyTodayPreview
[ ] LoadingTodayPreview
[ ] SnapshotErrorPreview
[ ] TodayNormalPreview
[ ] TodayFocusPreview
[ ] TodayFullPreview
[ ] OverdueHeavyPreview
[ ] CompletedHeavyPreview
[ ] ClosedDayPreview
[ ] MissedDayPreview
[ ] AddTaskSheetCollapsedPreview
[ ] AddTaskSheetAdvancedPreview
[ ] TaskDetailSheetPreview
[ ] CloseDaySummaryStepPreview
[ ] CloseDayMoodStepPreview
[ ] CloseDayReflectionStepPreview
[ ] CloseDayTomorrowStepPreview
[ ] DarkModeTodayPreview
[ ] LargeFontTodayPreview
```

## 10.8 Ticket 6.6 — UI regression checklist

Her release öncesi manuel kontrol:

```text
[ ] 360dp küçük Android ekran
[ ] 411dp standart Android ekran
[ ] Büyük font
[ ] Dark mode
[ ] Light mode
[ ] Sistem animasyonları kapalı
[ ] Keyboard açık
[ ] Çok uzun görev adı
[ ] Çok uzun kategori adı
[ ] Çok uzun not preview
[ ] 20+ görev
[ ] 10+ rutin
[ ] 10+ completed item
[ ] 5+ overdue item
[ ] Day closed state
[ ] Missed day state
[ ] Snapshot error state
[ ] Offline/error state
```

## 10.9 Sprint 6 kabul kriterleri

```text
[ ] Dynamic testTag sistemi tamamlandı.
[ ] Ana clickable alanlarda role/onClickLabel var.
[ ] Swipe delete için görünür alternatif var.
[ ] En az 8 Compose UI test yazıldı.
[ ] Preview matrix tamamlandı.
[ ] Dark mode kontrol edildi.
[ ] Büyük font kontrol edildi.
[ ] Keyboard/sheet davranışı kontrol edildi.
```

## 10.10 Sprint 6 beklenen puan etkisi

```text
8.75 / 10 → 8.9 / 10
```

---

# 11. UI Model İyileştirme Önerisi

## 11.1 Mevcut sorun

`TodayTaskUi` ve `TodayRoutineUi` sade tutulmuş. Bu başlangıç için iyi ama UI componentleri içinde bazı hesaplar tekrar ediliyor:

- Task completed mı?
- Display time ne olmalı?
- Note preview nasıl kesilmeli?
- Priority label ne?
- Category color ne?
- Routine progress percent ne?

Bu hesaplar UI içinde arttıkça componentler şişiyor.

## 11.2 Önerilen zengin UI model

```kotlin
data class TodayTaskUiModel(
    val id: String,
    val title: String,
    val notePreview: String?,
    val plannedDate: String,
    val displayTime: String,
    val categoryLabel: String?,
    val accentColor: Color,
    val priority: TodayPriorityUi,
    val isCompleted: Boolean,
    val hasReminder: Boolean,
    val isOverdue: Boolean,
)
```

Priority modeli:

```kotlin
enum class TodayPriorityUi {
    High,
    Normal,
    Low
}
```

Routine modeli:

```kotlin
data class TodayRoutineUiModel(
    val id: String,
    val name: String,
    val displayTime: String?,
    val accentColor: Color,
    val isCompleted: Boolean,
    val isCheckType: Boolean,
    val targetValue: Int?,
    val currentValue: Float,
    val targetUnit: String?,
    val progressPercent: Float,
    val streakLabel: String,
)
```

## 11.3 Avantaj

| Önce | Sonra |
|---|---|
| Component içinde hesap çok | Component sadece gösterim yapar |
| Test etmek zor | Mapper test edilir |
| Renk/label logic dağınık | Tek yerde toplanır |
| Row componentleri büyür | Row sadeleşir |

## 11.4 Önerilen mapper

```kotlin
class TodayUiModelMapper {
    fun mapTask(entity: TaskEntity, today: LocalDate): TodayTaskUiModel {
        ...
    }

    fun mapRoutine(
        routine: RoutineEntity,
        log: CompletionLogEntity?,
        completedRoutineIds: Set<String>
    ): TodayRoutineUiModel {
        ...
    }
}
```

Bu sprint planına zorunlu olarak eklenmeyebilir ama Sprint 1 veya Sprint 3 sırasında yapılırsa UI daha temiz olur.

---

# 12. Nihai Definition of Done

Tüm sprintler tamamlandığında Today UI için aşağıdaki maddeler karşılanmalı:

```text
[ ] TodayScreen.kt 350 satır altında.
[ ] TodayRoute ayrı.
[ ] TodayActions veya action grupları ayrı.
[ ] Header/List/Row/Card componentleri ayrı dosyalarda.
[ ] TodaySheets.kt parçalanmış.
[ ] AddTaskSheet hızlı görev ekleme odaklı.
[ ] AddTaskSheet gelişmiş alanları ikincil gösteriyor.
[ ] TaskDetailSheet section componentlerine bölünmüş.
[ ] Focus mode var.
[ ] Normal mode eski davranışı koruyor.
[ ] Full mode tüm detayları gösterebiliyor.
[ ] Task row sadeleşmiş.
[ ] Completed section varsayılan özetli.
[ ] Overdue section çok item olduğunda özet kart kullanıyor.
[ ] CloseDaySheet step componentlerine ayrılmış.
[ ] Normal close day ve missed day mode ayrılmış.
[ ] Close day kaydetmeden önce özet preview gösteriyor.
[ ] TodayColors/theme sistemi var.
[ ] Light/dark renk setleri var.
[ ] Hardcoded hex renkler minimuma inmiş.
[ ] Dynamic testTag sistemi var.
[ ] Ana clickable alanlarda role/onClickLabel var.
[ ] Swipe delete için erişilebilir alternatif var.
[ ] En az 8 Compose UI test var.
[ ] Preview matrix tamamlanmış.
[ ] Dark mode kontrol edilmiş.
[ ] Büyük font kontrol edilmiş.
[ ] Keyboard açıkken sheet davranışı test edilmiş.
[ ] Mevcut task/routine/day close davranışları kırılmamış.
```

---

# 13. Önceliklendirilmiş Kısa İş Listesi

Eğer tüm sprintleri tek seferde yapmak istemezsen, en kritik ilk 10 iş şunlar:

| Öncelik | İş | Etki |
|---|---|---:|
| 1 | `TodayScreen.kt` parçala | Çok yüksek |
| 2 | `TodaySheets.kt` parçala | Çok yüksek |
| 3 | `AddTaskSheet` sadeleştir | Çok yüksek |
| 4 | Task row bilgi yoğunluğunu azalt | Yüksek |
| 5 | Focus mode ekle | Yüksek |
| 6 | Completed section özetle | Orta-yüksek |
| 7 | Overdue summary card ekle | Orta-yüksek |
| 8 | CloseDaySheet step componentlerine böl | Orta-yüksek |
| 9 | TodayColors/theme sistemi kur | Orta |
| 10 | Dynamic testTag + accessibility label ekle | Orta |

---

# 14. En Mantıklı Uygulama Sırası

```text
1. Sprint 0 — Checklist, preview, test tag standardı
2. Sprint 1 — TodayScreen parçalama
3. Sprint 2 — Sheet/form sadeleştirme
4. Sprint 3 — Liste sadeleştirme + Focus mode
5. Sprint 4 — Close day refactor/polish
6. Sprint 5 — TodayColors/theme sistemi
7. Sprint 6 — Accessibility + UI test
```

Bu sıra önemlidir. Çünkü önce componentler ayrılmadan yeni UX özellikleri eklenirse dosyalar daha da büyür.

---

# 15. Final Değerlendirme

Today UI tarafı ürün fikri açısından güçlü. Asıl problem fikirde değil, uygulama katmanında:

- Çok fazla şey tek ekranda.
- Çok fazla component tek dosyada.
- Formlar biraz ağır.
- Liste satırları yoğun.
- Renk sistemi merkezi değil.
- Test ve accessibility sistemi daha düzenli olmalı.

Bu sprint planı uygulanırsa Today tarafı şu seviyeye gelir:

```text
Mevcut UI puanı: 7.4 / 10
Sprintler sonrası hedef: 8.7 - 8.9 / 10
```

Bu seviyeye geldiğinde Today ekranı sadece çalışan bir ekran değil; sürdürülebilir, test edilebilir, daha sade, daha kullanıcı dostu ve product-ready bir modül olur.

