# Audit Raporu — State / ViewModel

> ⚠️ **2026-08-17 güncellemesi:** `CloseDaySheet`/`TodayScreen` dialog state'leri `rememberSaveable`'a taşındı, kutlama event pipeline'ı `AppEventCoordinator` ile ekran bağımsız hale geldi, `TodayViewModel` alt sınıflara bölündü. Toggle race condition guard'ı hâlâ açık. Güncel durum için bkz. [`INCREMENTAL_REAUDIT_2026-08-17.md`](INCREMENTAL_REAUDIT_2026-08-17.md). Bu doküman tarihsel kayıt olarak değiştirilmemiştir.

## Genel Puan
7 / 10

## Kısa Karar
Mimari genel olarak sağlıklı: `StateFlow` + tek yönlü veri akışı (`combine` → `stateIn`) tüm ekranlarda tutarlı uygulanmış, `PlanViewModel`, `ProgressViewModel`, `AchievementsViewModel`, `RoutineDetailViewModel` gibi ekranlar temiz UiState/UiEffect ayrımına sahip. Ancak `TodayViewModel` + `TodayScreen` ikilisi projenin en kritik ve en riskli parçası: 740 satırlık ViewModel'de 27 bağımlılık, ekranda 11 adet kaydedilmeyen (`rememberSaveable` değil) dialog/sheet state'i, gün kapatma formunun (`CloseDaySheet`) process death'te tamamen kaybolması ve başarım/ödül kutlama olaylarının tek bir ekranın (`Today`) yaşam döngüsüne sıkıca bağlı olması gibi somut, yayın öncesi düzeltilmesi gereken sorunlar var. Refactor + hedefli düzeltme öncesi yayın önerilmez; beta/dahili test için kullanılabilir.

## En Güçlü 5 Taraf
1. Tüm ViewModel'ler `combine(...).stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), İlkDeğer)` desenini tutarlı kullanıyor — konfigürasyon değişikliklerinde yeniden abonelikte gecikme (debounce) ile gereksiz yeniden sorgulama önleniyor (örn. `TodayViewModel.kt:268`, `PlanViewModel.kt:100-107`, `ProgressViewModel.kt:59`).
2. Ödül/idempotency mantığı use-case katmanında sağlam: `ToggleTaskUseCase` ve `ToggleRoutineUseCase`, `rewardGrantService.grantOnce(eventKey = "task:${task.id}:${task.plannedDate}", ...)` ile tarih+id bazlı benzersiz anahtar kullanıyor (`ToggleTaskUseCase.kt:66-72`, `ToggleRoutineUseCase.kt:49-55`) — aynı görev birden fazla kez XP kazandırmıyor.
3. `PlanViewModel` (`app/src/main/java/com/benimgunlerim/ui/plan/PlanViewModel.kt`) CLAUDE.md'nin önerdiği ViewModel/UiState/UiEffect ayrımına en yakın örnek: 276 satır, tek sorumluluk, `PlanUiEffect` sealed class ile one-shot event'ler net (`PlanViewModel.kt:41-46`).
4. Görev tamamla/toggle işlemi Room transaction'ı içinde atomik yapılıyor: `transactionRunner.runInTransaction { taskRepository.setCompletionState(...); taskRepository.writeCompletionLog(...) }` (`ToggleTaskUseCase.kt:43-46, 58-61`) — state ile completion log arasında tutarsızlık riski düşük.
5. Hafif Gün Modu, günlük otomatik sıfırlama için ayrı bir "reset" job'una ihtiyaç duymayan zarif bir tasarıma sahip: `DataStore`'da sadece tarih string'i (`lightDayModeDate`) tutuluyor ve `isLightDayMode = snapshot.gameState.lightDayModeDate == dateTimeProvider.today().toString()` (`TodayViewModel.kt:247`) her gün otomatik olarak "false"a düşüyor — ayrı bir cron/worker gerektirmiyor.

## En Kritik 10 Sorun
| Öncelik | Sorun | Etki | Kanıt/Dosya | Öneri |
|---|---|---|---|---|
| P0 | Gün kapatma formunun (`CloseDaySheet`) tüm alanları (`step, mood, energy, note, bestMoment, challenge, tomorrowIntention, carryTasks`) `remember` ile tutuluyor, `rememberSaveable` değil | Process death / configürasyon değişikliğinde kullanıcı günün özetini, en iyi anını, zorluğunu vs. yazarken her şey sessizce siliniyor — günlük ritüelin kalbi | `app/src/main/java/com/benimgunlerim/ui/today/TodaySheets.kt:656-663` | Bu alanları `rememberSaveable` veya bir `CloseDayViewModel`/`SavedStateHandle` destekli state'e taşı |
| P0 | `TodayScreen` içindeki 11 dialog/sheet/event state'i (`showFabMenu, showAddTaskSheet, showResetDialog, showBrainDumpDialog, showCloseSheet, showMissedDaySheet, dismissContextualReset, levelUpEvent, achievementEvent, blockCompletionData, miniBannerData, menuRoutineId, editingRoutine`) hiçbiri `rememberSaveable` değil | Process death sonrası (arka plana atılıp bellekten atılan Activity) tüm açık dialoglar sıfırlanıyor; kullanıcı ekrana döndüğünde hangi sheet'te olduğunu unutup akışın ortasında kayboluyor | `app/src/main/java/com/benimgunlerim/ui/today/TodayScreen.kt:105-119` | Sade `Boolean`/`String?` state'leri `rememberSaveable`'a taşı; karmaşık olanlar (`blockCompletionData: Triple<String,String,String>?`) için `Saver` yaz veya ViewModel'e taşı |
| P0 | Başarım/ödül kutlama olayları (`RewardDisplayService.gameEvents`, `AchievementTracker.newUnlock`) sadece `TodayViewModel.init` içinde toplanıyor ve `replay=0` `MutableSharedFlow` üzerinden akıyor | `ShopViewModel.purchaseItem()` içindeki `achievementTracker.tryUnlock("first_buy")` (`ShopViewModel.kt:116`) gibi Today dışı ekranlardan tetiklenen başarımlar, kullanıcı o an `TodayScreen` içinde değilse (Shop/Progress/Settings'te ise) kalıcı olarak kayboluyor — geri dönüldüğünde de gösterilmiyor çünkü `replay=0` | `TodayViewModel.kt:226-234`, `RewardDisplayService.kt:17-18`, `AchievementTracker.kt:81-82`, `ShopViewModel.kt:116` | Kutlama toplama işini Activity/App-scope bir `CelebrationCoordinator`'a taşı (Application-level `SharedFlow` collector + global overlay), ekran ViewModel yaşam döngüsünden bağımsız hale getir |
| P1 | `toggleTask` çağrısında "wasPending"/"allTasksBonus" hesabı, UI'dan gelen (potansiyel olarak bayat) `TaskEntity` nesnesine ve `uiState.value`'nin o anki anlık görüntüsüne dayanıyor; art arda hızlı tıklamalarda kilitleme/eşzamanlılık koruması yok | Kullanıcı bir görevi çok hızlı iki kez tıklarsa iki coroutine aynı `task.completionState`'i (eski) baz alıp iki farklı toggle işlemi başlatabilir; "ilk görev" / "tüm görevler tamam" mini kutlamaları yanlış tetiklenebilir veya XP muhasebesi tutarsızlaşabilir | `app/src/main/java/com/benimgunlerim/ui/today/TodayViewModel.kt:327-357` | `toggleTask` çağrılarını görev id bazlı bir `Mutex`/in-flight `Set<String>` ile koru; UI'da tıklanan öğeyi anlık olarak devre dışı bırak |
| P1 | `TodayScreen` ve `RoutinesViewModel` içinde entity map'leri (`taskEntitiesById`, `routineEntitiesById`) `combine` bloğunun **yan etkisi** olarak yazılıyor; state resmi `StateFlow` dışında ikinci bir mutable kaynakta duplike ediliyor | Bu, tarif ettiğiniz "aynı state farklı yerde duplicate tutuluyor mu" sorusunun doğrudan cevabı — okunabilirliği düşürüyor, ileride flow yeniden düzenlenirse (örn. `flatMapLatest` sırası değişirse) sessizce bozulabilecek gizli bir bağımlılık yaratıyor | `TodayViewModel.kt:146-147, 243-244`, `RoutinesViewModel.kt:44, 50` | Entity map'i StateFlow'un kendisinden (`uiState.map { ... }` veya ayrı `stateIn`) türet, `combine` lambda'sını yan etkisiz bırak |
| P1 | `TodayViewModel` 27 dependency, 740 satır, tek sınıfta: görev CRUD, alt-görev CRUD, rutin CRUD, gün kapatma, kaçırılan gün akışı, brain dump, hafif gün modu, ödül/başarım orkestrasyonu bir arada | Tek Sorumluluk İlkesi ihlali; test edilebilirlik ve bakım maliyeti yüksek; CLAUDE.md'nin "ekran dosyaları 200 satırı geçemez" kuralı ekranlara uygulanmış ama ViewModel'lere uygulanmamış | `app/src/main/java/com/benimgunlerim/ui/today/TodayViewModel.kt:111-145` (constructor), dosya toplam 740 satır | `TaskActionsViewModel`, `RoutineActionsViewModel`, `DayCloseViewModel` gibi alt-birimlere böl veya en azından iş mantığını use-case'lere devretmeye devam edip ViewModel'i saf koordinatöre indir |
| P1 | `com.benimgunlerim.ui.components.organisms.AddTaskSheet` (yeni Faz D organizma) ile `com.benimgunlerim.ui.today.TodaySheets.kt` içindeki eski dahili `AddTaskSheet`/`CloseDaySheet` aynı anda canlı; `TodayScreen` yeni organizmayı, `PlanScreen` eski dahili sürümü kullanıyor | Aynı form için iki farklı state modeli (biri kendi içinde `remember` tutuyor, diğeri state'i dışarıdan parametre olarak alıyor) — CLAUDE.md Faz D/E "iki ekranda görünen her yapı organisms'te olmalı" kuralına aykırı, gelecekte biri güncellenip diğeri unutulma riski taşıyor | `app/src/main/java/com/benimgunlerim/ui/components/organisms/AddTaskSheet.kt:33-45`, `app/src/main/java/com/benimgunlerim/ui/today/TodaySheets.kt:87-107`, `PlanScreen.kt` (AddTaskSheet çağrısı) | Plan ekranını da organizma `AddTaskSheet`'e taşı, `TodaySheets.kt`'deki dahili kopyayı sil |
| P1 | `organisms/AddTaskSheet.kt` içindeki form state'i (`title, selectedDate, selectedPriority, category`) `remember` ile tutuluyor, `rememberSaveable` değil | Kullanıcı yeni görev eklerken process death olursa yazdığı başlık/kategori/tarih kaybolur | `app/src/main/java/com/benimgunlerim/ui/components/organisms/AddTaskSheet.kt:41-45` | `rememberSaveable` kullan |
| P2 | `TodayUiEffect.TaskMovedTomorrow("task_moved_tomorrow")` ve `TodayUiEffect.DaySaved("day_saved")` gibi effect'lerde mesaj payload'u olarak ham/anlamsız sabit string'ler (`"task_moved_tomorrow"`, `"day_saved"`, `"missed_day_saved"`) taşınıyor, `TodayScreen`'de bu payload hiç kullanılmıyor (sadece `TaskDeleted`/`TaskCompletedUndo`/`ActionFailed` dinleniyor — `else -> Unit`) | Ölü kod / yanıltıcı API: effect sınıfı bir `message: String` alanı taşıyor gibi görünüyor ama gerçek bir string kaynağı (R.string) değil, kullanılmıyor da | `TodayViewModel.kt:100, 104, 417, 648, 689, 708`; tüketimi `TodayScreen.kt:135-164` (`else -> Unit`) | Payload'ı kaldır (`data object DaySaved`) veya gerçekten `R.string` id'si taşıyan bir alana çevirip UI'da göster |
| P2 | `ShopViewModel`, `SettingsViewModel` ve `TodayViewModel` içinde one-shot mesajlar için üç farklı model kullanılıyor: `_purchaseMessage: MutableStateFlow<String?>` (Shop, tüketici `clearMessage()` çağırmazsa tekrar tetiklenebilir), `_dataOperationMessage: MutableStateFlow<SettingsEvent?>` (Settings), `_uiEffects: MutableSharedFlow<...>` (Today/Plan/Settings-uiEffects) | Aynı "one-shot event" problemi üç farklı şekilde çözülmüş; `StateFlow<String?>` tabanlı olanlar (Shop, Settings.dataOperationMessage) recomposition/yeniden abone olma sırasında **yeniden oynatılabilir** (StateFlow her zaman son değeri tutar, `LaunchedEffect` yeniden çalışırsa mesaj tekrar gösterilebilir) — SharedFlow tabanlı model daha güvenli ama tutarsız kullanılıyor | `ShopViewModel.kt:64, 94, 98, 108, 115, 119, 127-129`; `SettingsViewModel.kt:44-45, 94, 102, 113, 131, 141, 146` | Tüm one-shot event'leri `MutableSharedFlow(extraBufferCapacity=…)` desenine standardize et; proje genelinde tek bir `UiEffect`/`OneShotEvent` sözleşmesi tanımla |

## Dosya Bazlı Bulgular

### `app/src/main/java/com/benimgunlerim/ui/today/TodayViewModel.kt`
- Bulgu: 740 satır, 27 constructor bağımlılığı; görev/alt görev/rutin/gün kapatma/kaçırılan gün/brain dump/hafif gün modu hepsi tek sınıfta. `TodayUiState` 17 alan taşıyor (`TodayViewModel.kt:77-97`), `uiState` tek bir dev `combine` (`TodayViewModel.kt:236-268`) ile üretiliyor.
- Risk: Bakım/test maliyeti yüksek; yeni bir domain akışı (örn. yeni bir kutlama türü) eklemek her seferinde bu dosyayı büyütüyor. `taskEntitiesById`/`routineEntitiesById` (satır 146-147) `combine` içinde yan etki olarak dolduruluyor — flow yeniden sıralanırsa veya `WhileSubscribed` timeout'u nedeniyle akış yeniden başlarsa, bu map'lerin "state"i UI state'inden bağımsız kalabilir.
- Öneri: İş mantığını use-case'lere devretmeye devam ederek ViewModel'i ince bir koordinatöre indir; `TaskActions`, `RoutineActions`, `DayClose` alt-gruplarına ayır (ör. ayrı sınıflarda tutup ViewModel'de delege et).

### `app/src/main/java/com/benimgunlerim/ui/today/TodayScreen.kt`
- Bulgu: 11 adet composable-local state (satır 105-119) `remember` ile tutuluyor; hiçbiri `rememberSaveable` değil. `LaunchedEffect(viewModel) { viewModel.gameEvents.collect {...} }` (satır 167-198) sadece bu composable aktifken çalışıyor.
- Risk: Process death sonrası açık dialog/sheet durumu kayboluyor; oyunlaştırma event'leri sadece Today ekranı composed haldeyken tüketiliyor.
- Öneri: Dialog görünürlük state'lerini `rememberSaveable`'a taşı; kompleks olanları (`blockCompletionData`, `levelUpEvent`) ayrı bir "pending celebration" alanına ViewModel/SavedStateHandle üzerinden taşı.

### `app/src/main/java/com/benimgunlerim/ui/today/TodaySheets.kt`
- Bulgu: `CloseDaySheet` (satır 649-663) çok adımlı (step-based) bir formun tüm alanlarını `remember` ile tutuyor. Aynı dosyada, artık `TodayScreen` tarafından kullanılmayan (yalnızca aynı paket içinde tarihsel olarak kalmış) ayrı bir `AddTaskSheet` implementasyonu (satır 87-107) hâlâ mevcut ve `PlanScreen` tarafından kullanılıyor.
- Risk: Gün kapatma formunda veri kaybı (P0, yukarıda); iki paralel `AddTaskSheet`'in bakımı ayrı ayrı yapılmak zorunda.
- Öneri: `CloseDaySheet` state'ini `rememberSaveable`/ViewModel'e taşı; `PlanScreen`'i organizma `AddTaskSheet`'e geçir ve bu dosyadaki eskisini sil.

### `app/src/main/java/com/benimgunlerim/ui/components/organisms/AddTaskSheet.kt`
- Bulgu: `title, selectedDate, selectedPriority, category, isError` (satır 41-45) `remember` ile tutuluyor.
- Risk: Process death'te doldurulmuş form verisi kaybolur (görece küçük risk çünkü form kısa, ama CLAUDE.md'nin "organizmalar state tutmaz, state parametre gelir" kuralına da aykırı — bu organizma hem state tutuyor hem de "state dışarıdan gelmiyor").
- Öneri: `rememberSaveable`'a geçir veya form state'ini tamamen çağırana (TodayScreen/PlanScreen) hoist et — CLAUDE.md Demir Kural 6 ("Organizmalar state tutmaz") ile tutarlı hale getir.

### `app/src/main/java/com/benimgunlerim/domain/service/RewardDisplayService.kt`
- Bulgu: `@Singleton`, `MutableSharedFlow<GameEvent>(extraBufferCapacity = 16)`, `replay = 0`. Tek tüketicisi `TodayViewModel.gameEvents` üzerinden `TodayScreen`.
- Risk: Herhangi bir ekrandan (Shop, Settings, Plan, Routines) tetiklenen ödül/başarım event'i, o anda `TodayScreen` composed değilse kalıcı olarak kaybolur (replay yok). `AchievementTracker.newUnlock` da aynı şekilde sadece `TodayViewModel.init` içinde dinleniyor (`TodayViewModel.kt:229-234`).
- Öneri: Global/Application-scope bir event toplayıcı (örn. Activity seviyesinde bir `CelebrationHost` composable + tek bir uygulama genelinde `SharedFlow` collector) kur; ekran ViewModel yaşam döngüsünden ayır.

### `app/src/main/java/com/benimgunlerim/ui/plan/PlanViewModel.kt`
- Bulgu: `PlanUiState`/`PlanUiEffect` net ayrılmış, `latestTasksById`/`latestOverdueTasks` yine `map` içinde yan etki olarak set ediliyor (satır 89-90) ama ViewModel küçük ve anlaşılır kaldığı için risk düşük.
- Risk: Aynı "yan etkili cache" deseni burada da var ama dosya küçük olduğu için okunabilirlik sorunu daha az.
- Öneri: Uzun vadede `taskEntitiesById`'i `uiState`'ten türetilen ayrı bir `stateIn` akışına çevirmek proje genelinde tutarlılık sağlar.

### `app/src/main/java/com/benimgunlerim/ui/routines/RoutinesViewModel.kt`
- Bulgu: `routineEntitiesById: MutableStateFlow<Map<String, RoutineEntity>>` (satır 44), yalnızca senkron cache amaçlı kullanılıyor, hiçbir yerde `Flow` olarak dışarı sızmıyor; `toggleRoutine()` (satır 131-146) tamamlanma durumunu `routines.value` üzerinden anlık okuyor.
- Risk: `routines.value` henüz stateIn tarafından güncellenmemişse (ilk emisyon öncesi) `toggleRoutine` çağrısı yanlış `isCompletedToday` ile çalışabilir; küçük bir pencere ama olası.
- Öneri: `routineEntitiesById`'i düz `var` yap (StateFlow olması gereksiz complexity katıyor) ya da `combine` çıktısını doğrudan kullan.

### `app/src/main/java/com/benimgunlerim/domain/usecase/ToggleTaskUseCase.kt` / `ToggleRoutineUseCase.kt`
- Bulgu: `grantOnce(eventKey = "task:${task.id}:${task.plannedDate}", ...)` ve `"routine:${routine.id}:$date"` anahtarlarıyla idempotent ödül. `ToggleTaskUseCase`, tamamlama/geri alma sırasında `DatabaseTransactionRunner.runInTransaction` kullanıyor (satır 43-46, 58-61) ama ardından `taskRepository.observeByDate(today).first()` (satır 82) ile "tüm görevler tamam mı" kontrolü transaction dışında, ayrı bir okuma ile yapılıyor.
- Risk: Aynı anda iki görev toggle edilirse (iki ayrı `toggleTask` çağrısı, satır 82'deki `.first()` okuması) art arda çalışan coroutine'ler arasında "tüm görevler tamamlandı" bonusunun iki kez ya da hiç tetiklenmemesi mümkün — küçük ölçekli tek kullanıcılı bir uygulamada düşük olasılık ama kanıtlanabilir bir race window var.
- Öneri: "Tüm görevler tamam" kontrolünü de aynı transaction içine al veya bir görev-bazlı `Mutex` ile seri hale getir.

### `app/src/main/java/com/benimgunlerim/data/UserPreferencesRepository.kt`
- Bulgu: `lightDayModeDate` (satır 51, 110, 140, 348-352) DataStore'da saklanıyor, `setLightDayMode(enabled, dateStr)` kapatıldığında `""` yazıyor, açıldığında bugünün tarihini yazıyor.
- Risk: Kullanıcı Hafif Gün Modu'nu açıp kapatmadan günü geçirirse alan `""`'e hiç dönmez; ertesi gün `lightDayModeDate` hâlâ dünün tarihini taşır ama karşılaştırma günceli yakaladığı için (`TodayViewModel.kt:247`) fonksiyonel bir hata yok — sadece kullanılmayan/stale bir alan DataStore'da kalıcı olarak birikir (temizlenmiyor).
- Öneri: Önemsiz risk; istenirse günlük bir "eski light-day tarihini temizle" adımı eklenebilir, P2 seviyesinde.

## Kullanıcı Deneyimi Etkisi
En kritik UX riski, kullanıcının günlük kapanış ritüelini (mood, en iyi an, zorluk, yarın için niyet) yazarken telefon arka plana atılıp sistem tarafından bellekten temizlenirse tüm bu emeğin sessizce kaybolmasıdır (`TodaySheets.kt:656-663`). Bu, uygulamanın "duygusal günlük" değerini doğrudan zedeler — kullanıcı bir daha aynı özenle yazmayabilir. İkinci önemli etki: başarım/ödül kutlamalarının yalnızca Today ekranındayken görünmesi — kullanıcı Dükkan'da bir ürün alıp "İlk Alışveriş" başarımını kazandığında hiçbir kutlama görmez, bu da oyunlaştırmanın (gamification) motive edici geri bildirim döngüsünü kırar. Üçüncü olarak, 11 adet kaydedilmeyen dialog state'i nedeniyle ekran döndürme/arka plan senaryolarında kullanıcı "nerede kaldığını" kaybedebilir (özellikle rutin düzenleme sheet'i `editingRoutine` açıkken).

## Teknik Borç Etkisi
`TodayViewModel`'in 740 satır ve 27 bağımlılığa büyümüş olması, her yeni özellik eklemede (örn. yeni bir kutlama türü, yeni bir gün-sonu adımı) bu dosyanın daha da büyümesi riskini taşıyor — CLAUDE.md'nin ekranlar için koyduğu "200 satır" kısıtı ViewModel katmanına hiç uygulanmamış. İki paralel `AddTaskSheet` implementasyonu (organisms vs. today-local) ve üç farklı one-shot event modeli (StateFlow<T?>, SharedFlow, effect-with-unused-payload) projenin "tutarlı mimari" hedefinden (Faz A-E, CLAUDE.md) sapmaya başladığının işareti. `combine` bloklarının içinde yan etkili mutable cache doldurma deseni (`TodayViewModel`, `RoutinesViewModel`, `PlanViewModel`) tek bir yerde standart bir yardımcıya çıkarılmazsa, her yeni ViewModel'de tekrar tekrar (ve muhtemelen biraz farklı) yeniden yazılacaktır.

## Release / Monetizasyon Riski
Şu an parayla satın alınabilir bir öğe yok (Dükkan yalnızca oyun-içi altınla çalışıyor, `ShopViewModel.kt` gerçek para akışı içermiyor), bu nedenle doğrudan bir ödeme/finansal risk tespit edilmedi. Ancak gün kapatma verisinin process death'te kaybolması, App Store/Play Store incelemesi veya ilk kullanıcı izlenimlerinde "verilerim kayboluyor" şikayetlerine yol açabilecek türden bir kalite riski taşır ve bu, ücretli/premium bir sürüm planlanıyorsa güven kaybına dönüşebilir.

## Önceliklendirilmiş Yapılacaklar

### P0 — Yayın öncesi şart
- `CloseDaySheet` (`TodaySheets.kt:649-663`) form state'ini `rememberSaveable`'a taşı.
- `TodayScreen.kt:105-119` içindeki dialog/sheet/event state'lerini `rememberSaveable` (veya ViewModel'e taşınmış eşdeğerine) çevir.
- Başarım/ödül kutlama event pipeline'ını (`RewardDisplayService`, `AchievementTracker.newUnlock`) `TodayViewModel`'in yaşam döngüsünden bağımsızlaştır (uygulama-seviyesi bir toplayıcıya taşı).

### P1 — Kısa vadede gerekli
- `toggleTask`/`toggleRoutine` için görev-id bazlı bir kilit (in-flight guard) ekle; UI'da tıklanan öğeyi anlık devre dışı bırak.
- `TodayViewModel`'i alt sorumluluklara böl (görev, rutin, gün kapatma) — 740 satır tek dosyada kalmamalı.
- `PlanScreen`'i organizma `AddTaskSheet`'e geçirip `TodaySheets.kt`'deki eski dahili `AddTaskSheet`'i sil.
- `organisms/AddTaskSheet.kt` form state'ini `rememberSaveable`'a çevir.
- `combine` içindeki yan etkili entity-map doldurma desenini (`TodayViewModel`, `RoutinesViewModel`, `PlanViewModel`) standart bir yaklaşıma (StateFlow'tan türetilmiş, yan etkisiz) çevir.

### P2 — Polish / ileri iyileştirme
- Kullanılmayan effect payload'larını (`"task_moved_tomorrow"`, `"day_saved"` gibi) temizle veya gerçek `R.string` id'lerine çevir.
- Proje genelinde tek bir one-shot event standardı belirle (`MutableSharedFlow` tabanlı) ve `ShopViewModel`/`SettingsViewModel`'i buna geçir.
- `lightDayModeDate`'in stale kalan eski tarih değerlerini temizleyen küçük bir bakım adımı ekle.

## 1 Haftalık Düzeltme Planı
- Gün 1-2: `CloseDaySheet` ve `TodayScreen` dialog state'lerini `rememberSaveable`'a taşı, process-death senaryosunu manuel test et (Developer Options → "Don't keep activities").
- Gün 3-4: Kutlama/başarım event pipeline'ını uygulama-seviyesine taşı (Shop'tan tetiklenen bir başarımın Today dışındayken de görüldüğünü doğrula).
- Gün 5: `toggleTask`/`toggleRoutine` için çift-tıklama koruması ekle, birim testle doğrula.

## 2 Haftalık Düzeltme Planı
- Hafta 1: Yukarıdaki P0 + P1'in ilk üç maddesi.
- Hafta 2: `TodayViewModel`'i alt sorumluluklara bölme, `AddTaskSheet` konsolidasyonu, one-shot event standardizasyonu, P2 temizlikleri.

## Final Karar
Beta/dahili test için uygundur; genel kullanıcıya açık yayın öncesinde en az P0 maddelerinin (özellikle gün kapatma formunun veri kaybı ve kutlama event'lerinin ekran-bağımlılığı) çözülmesi gerekir.

## State Risk Haritası
| State Alanı | Mevcut Durum | Risk | Önerilen Model |
|---|---|---|---|
| `TodayScreen` dialog/sheet görünürlükleri (`showFabMenu`, `showAddTaskSheet`, `showResetDialog`, `showBrainDumpDialog`, `showCloseSheet`, `showMissedDaySheet`, `dismissContextualReset`) | `remember { mutableStateOf(false) }` — `TodayScreen.kt:105-111` | Process death'te sıfırlanır; kullanıcı akışın ortasında kaybolur | `rememberSaveable { mutableStateOf(false) }` |
| `CloseDaySheet` form alanları (`step, mood, energy, note, bestMoment, challenge, tomorrowIntention, carryTasks`) | `remember` — `TodaySheets.kt:656-663` | Yazılan gün kapanış metni process death'te tamamen kaybolur | `rememberSaveable` (String/Int/Boolean için otomatik `Saver` yeterli) |
| `levelUpEvent`, `achievementEvent`, `blockCompletionData`, `miniBannerData` (aktif kutlama modalları) | `remember { mutableStateOf<...?>(null) }` — `TodayScreen.kt:113-116` | Kutlama modalı açıkken process death olursa event kaybolur; ayrıca kaynağı (`RewardDisplayService`) zaten replay=0 olduğu için event bir daha üretilmez | Basit alanlar için `rememberSaveable` + özel `Saver`; asıl kaynak sorunu (event pipeline'ın Today'e bağlı olması) ayrıca çözülmeli |
| `taskEntitiesById`, `routineEntitiesById` (id→entity cache) | Düz `var` / `MutableStateFlow`, `combine` içinde yan etki olarak dolduruluyor — `TodayViewModel.kt:146-147, 243-244`; `RoutinesViewModel.kt:44, 50` | Resmi `StateFlow`'un dışında, senkronizasyonu garanti edilmeyen ikinci bir "gerçek kaynak"; okunabilirlik ve test edilebilirlik düşük | `uiState`'ten türetilen ayrı bir `stateIn` akışı veya ViewModel'in tek bir `combine` çıktısına entegre edilmiş salt-okunur alan |
| `TodayUiState.isLightDayMode` | `snapshot.gameState.lightDayModeDate == dateTimeProvider.today().toString()` ile türetilen salt-okunur alan — `TodayViewModel.kt:247` | Düşük risk; DataStore'daki `lightDayModeDate` günler sonra da temizlenmeden kalabilir (stale ama zararsız) | Mevcut model korunabilir; istenirse periyodik temizlik eklenebilir |
| Görev/rutin toggle sırasında "kaç görev tamamlandı" sayacı (`completedCountBefore`, `totalTasks`) | `viewModelScope.launch` içinde `uiState.value`'den anlık okunuyor, kilitsiz — `TodayViewModel.kt:331-333`, `500-503` | Art arda hızlı tıklamalarda race condition; mini kutlama ("ilk görev", "tüm görevler tamam") yanlış/duplicate tetiklenebilir | Görev/rutin id bazlı `Mutex` veya UI'da anlık disable + debounce |
| One-shot mesajlar (`ShopViewModel._purchaseMessage`, `SettingsViewModel._dataOperationMessage`) | `MutableStateFlow<String?>` / `MutableStateFlow<SettingsEvent?>` — `ShopViewModel.kt:64`, `SettingsViewModel.kt:44-45` | `StateFlow` son değeri her zaman tutar; `LaunchedEffect` yeniden tetiklenirse (örn. recomposition/yeniden abonelik) mesaj yinelenebilir | `MutableSharedFlow(extraBufferCapacity=…)` tabanlı `UiEffect` deseni (projede zaten `TodayUiEffect`/`PlanUiEffect`/`SettingsUiEffect` olarak kısmen var — tutarlı uygula) |
| Onboarding tamamlanma / başlangıç rotası (`AppDestination.Onboarding` vs `Today`) | `BenimGunlerimApp` içinde `userPreferences.onboardingCompleted`'a göre `startDestination` hesaplanıyor — `AppNavigation.kt:112-120` | Düşük risk; DataStore kalıcı olduğu için process death'ten etkilenmez | Mevcut model doğru; ek aksiyon gerekmez |
