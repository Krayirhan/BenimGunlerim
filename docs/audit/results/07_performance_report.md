# Audit Raporu — Performans

> ⚠️ **2026-08-17 güncellemesi:** `completion_logs.observeAll()` sınırsız tarama `observeBetween` ile tarih-sınırlı hale getirildi, Brain Dump toplu ekleme transaction'a alındı. "LazyColumn yok" bulgusu geçersiz — ekranlar zaten dış `LazyColumn` içinde çalışıyor (bkz. incremental rapor notu). Güncel durum için bkz. [`INCREMENTAL_REAUDIT_2026-08-17.md`](INCREMENTAL_REAUDIT_2026-08-17.md). Bu doküman tarihsel kayıt olarak değiştirilmemiştir.

## Genel Puan
6 / 10

## Kısa Karar
Uygulama günlük kullanım hacminde (birkaç görev/rutin) akıcı çalışacak şekilde yazılmış; alt yapıda gerçek bir "kasma" belirtisi yok ve DataStore/Room tarafında bilinçli iyi pratikler var (indeksler, rewardedEvents budama, `WhileSubscribed(5_000)`, macrobenchmark modülü, baseline profile). Ancak Today ekranının state-okuma alanı aşırı geniş, liste bileşenleri hiçbir yerde `LazyColumn` kullanmıyor, "tüm zamanların" completion-log tablosu sınırsız şekilde her state güncellemesinde tam taranıyor ve Brain Dump toplu görev ekleme tek transaction'da değil. Bunlar kullanıcı sayısı/veri hacmi arttıkça (uzun süreli kullanıcı, çok görevli gün) somut kasma riski taşır. Beta'ya çıkılabilir ama P0/P1 maddeleri release öncesi kapatılmalı; şu an "yayına hazır performans" iddiası kanıtla desteklenmiyor çünkü ScrollJank ve WarmStartup benchmark testleri `@Ignore` ile devre dışı.

## En Güçlü 5 Taraf
1. Release build R8/minify/shrinkResources açık, kapsamlı `proguard-rules.pro` mevcut (`app/build.gradle.kts:74-83`, `app/proguard-rules.pro`).
2. Ayrı bir `:benchmark` Gradle modülü var — `StartupBenchmark.kt` ve `ScrollJankBenchmark.kt` gerçek altyapı ile yazılmış, macrobenchmark bağımlılığı doğru kurulmuş.
3. `baseline-prof.txt` mevcut ve release derlemesine (`merged_art_profile`, `r8_art_profile`) doğru şekilde işleniyor.
4. `UserPreferencesRepository.pruneRewardedEvents()` DataStore'un sınırsız büyümesini engellemek için 90 günlük/500 kayıtlık bir üst sınır uyguluyor (`UserPreferencesRepository.kt:238-251`) — bilinçli bir performans/veri şişme önlemi.
5. `TodayViewModel.uiState` `stateIn(..., SharingStarted.WhileSubscribed(5_000), ...)` kullanıyor (`TodayViewModel.kt:268`), yani ekran arka plana alındığında Flow toplama 5 saniye sonra düşüyor — gereksiz arka plan koleksiyonunu önlüyor.

## En Kritik 10 Sorun
| Öncelik | Sorun | Etki | Kanıt/Dosya | Öneri |
|---|---|---|---|---|
| P0 | `completion_logs` tablosu `LIMIT`/tarih sınırı olmadan `observeAll()` ile tam okunuyor; her görev/rutin toggle'ında ve her dakika ticker'ında tüm geçmiş yeniden taranıp streak hesaplanıyor | Aylar/yıllar süren kullanımda her Today state güncellemesi O(tüm geçmiş kayıt) işine dönüşür; pil/CPU tüketimi kullanım süresiyle orantısız büyür | `CompletionLogDao.kt:18-19` (`SELECT * FROM completion_logs ORDER BY date DESC`), `ObserveTodaySnapshotUseCase.kt:52,73-80`, `CompletionLogExtensions.kt:7-38` | `observeAll()`'ı son N gün (örn. 60-90 gün) ile sınırla veya streak hesabını SQL tarafında (window/aggregate query) yap; tam tablo tarama yerine tarih aralıklı sorgu kullan |
| P0 | Today ekranında hiçbir liste `LazyColumn` değil; tüm görev/rutin satırları `Column + verticalScroll + forEachIndexed` ile aynı anda composed ediliyor, `key=` yok | 100 görev + 50 rutin senaryosunda (audit'in önerdiği test) tüm satırlar ekrana girmeden composed olur; scroll jank ve gereksiz recomposition riski yüksek | `TodayScreen.kt:222-225` (`Column().verticalScroll(rememberScrollState())`), `TodayListContainers.kt:21-36,68-86` (`forEachIndexed`, key yok) | `TaskListContainer`/`RoutineListContainer` içeriğini `LazyColumn` + `items(tasks, key = { it.id })` ile yeniden yaz |
| P0 | Brain Dump toplu görev ekleme tek transaction'da değil; her satır için ayrı `addTaskUseCase()` çağrısı, dolayısıyla ayrı Room insert + ayrı Flow invalidation | 500 satırlık yapıştırmada N adet ayrı DB yazımı + N adet UI re-emisyonu; ara adımlarda tutarsız ara state görünebilir, performans lineer değil süper-lineer davranabilir | `TodayViewModel.kt:277-290` (`taskTitles.forEach { addTaskUseCase(...) }`), `DatabaseTransactionRunner.kt` (tanımlı ama burada kullanılmıyor), `TaskDao`'da `insertAll` yok | `DatabaseTransactionRunner.runInTransaction { }` içine alıp tek transaction'da toplu ekleme yap; TaskDao'ya `@Insert insertAll(List<TaskEntity>)` ekle |
| P1 | `TodayUiState` tek bir geniş state objesi; `TaskListContainer`/`RoutineListContainer` parametreleri `List<TodayTaskUi>`/`List<TodayRoutineUi>` — Compose derleyicisi `List<T>` arayüzünü kararsız (unstable) kabul eder, bu yüzden dakikalık ticker gibi ilgisiz güncellemelerde bile liste bileşenleri equality kontrolü yapılmadan yeniden composed olur | Her 60 saniyede bir (`minuteTickerFlow`) ve her StateFlow emisyonunda görev/rutin listeleri kullanıcı hiçbir şey yapmasa da yeniden composed olabilir | `TodayViewModel.kt:236-241,268` (`minuteTickerFlow` combine'e dahil), `TodayScreen.kt:101,435-465` (`state.tasks`, `state.routines` doğrudan geçiliyor), `TodayUiModels.kt:5,21` (data class içinde `List`/`Set` alanları yok ama üst state'te `List<TodayTaskUi>`) | `canCloseDay` gibi zamana bağlı alanları ayrı, dar kapsamlı bir `StateFlow`'a taşı; `ImmutableList` (kotlinx.collections.immutable) kullan veya Compose "strong skipping mode" aç |
| P1 | `TodayScreen.kt` 767 satır, `TodayViewModel.kt` 740 satır — proje kendi kuralı olan 200 satır ekran limitini (CLAUDE.md, Demir Kural #5) ~4x aşıyor; tek Composable içinde state'in tamamı okunuyor (`val state by viewModel.uiState.collectAsState()`) | Geniş recomposition kapsamı: state'in herhangi bir alanı değiştiğinde 700+ satırlık Composable ağacının tamamı yeniden değerlendirilir (Compose'un kendi akıllı atlama mekanizması tek tek child'larda çalışsa da giriş noktası aşırı geniş) | `TodayScreen.kt:94-697` | Header/Overdue/TaskSection/RoutineSection/CloseDay bloklarını ayrı composable'lara ve mümkünse organisms'e taşı; her biri state'in yalnızca ihtiyaç duyduğu dar alt kümesini parametre olarak alsın |
| P1 | Nefes/reset animasyonu ekranda `ModalBottomSheet` kapandığında veya `ResetState.BREATHING` dışına çıkıldığında düzgün duruyor (pozitif), fakat `HappinessBar` (`CelebrationSystem.kt:361-367`) içindeki `rememberInfiniteTransition` mutluluk ≥80 olduğunda süresiz döngüde kalıyor ve bu bileşenin nerede/ne sıklıkla ekranda kaldığı (örn. arka planda Shop/Progress ekranında) net değil — off-screen'de duraklatma mekanizması yok | Görünürde değilken bile infinite animasyon composition'da kalırsa gereksiz frame üretimi/pil tüketimi olur | `CelebrationSystem.kt:361-367` (`rememberInfiniteTransition(label = "happyPulse")`, sürekli `RepeatMode.Reverse`) | Bileşenin sadece görünür olduğu composable ağacında var olduğundan emin ol; `LifecycleResumeEffect`/`isVisible` guard'ı ile pause/resume ekle |
| P1 | İki paralel konfeti sistemi var: kütüphane tabanlı `SubtleCelebrationParticles` (Konfetti) ve elle yazılmış Canvas tabanlı `ConfettiOverlay`/`StarBurst` (`CelebrationSystem.kt`) — hangisinin ne zaman kullanıldığı net değil, kod tekrarı + olası çifte partikül render riski | Bakım yükü + potansiyel gereksiz APK/CPU maliyeti (iki ayrı partikül motoru); hangi ekranın hangi sistemi kullandığı incelenmeden karar verilemez | `CelebrationParticles.kt:1-42` (Konfetti kütüphanesi), `CelebrationSystem.kt:82-150,196-227` (elle yazılmış Canvas confetti + StarBurst) | Tek bir konfeti/kutlama sistemine konsolide et; kullanılmayanı kaldır |
| P1 | Benchmark modülü var ama en kritik iki metrik devre dışı: `warmStartup` ve `todayScreenScrollJank` `@Ignore` ile kapatılmış | "Startup time" ve "scroll jank" sorularına şu an ölçülebilir/otomatik bir cevap yok; sadece cold startup ölçülüyor | `StartupBenchmark.kt:32-44` (`@Ignore("Warm startup metric extraction is flaky...")`), `ScrollJankBenchmark.kt:21-39` (`@Ignore("FrameTiming metric needs stable renderthread slices...")`) | İhmal edilen testleri stabilize et (cihaz profili sabitleme, `CompilationMode.Partial(BaselineProfileMode.Require)` dene) veya CI'da manuel/periyodik çalıştırma sürecine bağla |
| P2 | Brain Dump "SELECT_TASKS" adımı, ayrıştırılan tüm satırları `parsedLines.forEach` ile `Column().verticalScroll` içinde composed ediyor; `LazyColumn` yok | Çok satırlı (yakl. 3000 karakter sınırına kadar, kısa satırlarla yüzlerce satır olabilir) yapıştırmalarda seçim ekranı tüm checkbox'ları aynı anda oluşturur | `BrainDumpDialog.kt:149` (3000 karakter sınırı var — olumlu), `BrainDumpDialog.kt:206-262` (`parsedLines.forEach` + `verticalScroll`, key yok) | `LazyColumn` + `items(parsedLines, key = { it })` kullan |
| P2 | `AppDatabase` sürüm 7, migration'lar elle yazılmış SQL (iyi — `fallbackToDestructiveMigration` kullanılmıyor) fakat DAO/Repository katmanında toplu (`insertAll`/`updateAll`) hiçbir batch API yok; her toplu işlem tek tek suspend fonksiyon döngüsüne düşüyor | Gelecekte eklenecek her "toplu" özellik (şablon rutin kurulumu, CSV/QR import vb.) aynı N-transaction sorununu miras alır | `TaskDao`/`RoutineDao` (grep sonucu: `insertAll` yalnızca `CompletionLogDao.kt:31`'de var, Task/Routine DAO'larında yok) | Task/Routine DAO'larına `@Insert suspend fun insertAll(items: List<...>)` ekleyip use-case'leri buna yönlendir |

## Dosya Bazlı Bulgular

### `app/src/main/java/com/benimgunlerim/ui/today/TodayScreen.kt`
- Bulgu: 767 satırlık tek Composable; `state by viewModel.uiState.collectAsState()` en üstte okunuyor (satır 101), ekranın her alt bölümü (banner, header, overdue, task/routine listeleri, close-day kartı, FAB menüsü, 6 farklı dialog/sheet) aynı fonksiyon gövdesinde. Listeler `Column + verticalScroll` (satır 222-225), `LazyColumn` yok.
- Risk: Geniş recomposition kapsamı + büyük görev/rutin sayısında (100+50 senaryosu) tüm satırların anında composed olması; CLAUDE.md'nin kendi 200 satır kuralını 4x aşıyor.
- Öneri: Ekranı `HeaderSection`, `TaskSection`, `RoutineSection`, `CloseDaySection` gibi alt composable'lara böl; liste konteynerlerini `LazyColumn`'a taşı.

### `app/src/main/java/com/benimgunlerim/ui/today/TodayViewModel.kt`
- Bulgu: `uiState` beş kaynağı `combine` ediyor (satır 236-241) — bunlardan biri her 60 saniyede tetiklenen `minuteTickerFlow` (satır 192, 240), bir diğeri `ObserveTodaySnapshotUseCase` üzerinden `completionLogRepository.observeAll()`'a bağlı (dolaylı, `ObserveTodaySnapshotUseCase.kt:52`). `addTasksFromBrainDump` (satır 277-290) her satır için ayrı `addTaskUseCase()` çağırıyor, `DatabaseTransactionRunner` inject edilmemiş/kullanılmamış.
- Risk: Dakikalık gereksiz tam state yeniden hesaplama + Brain Dump'ta N adet ayrı DB yazımı.
- Öneri: `canCloseDay` hesaplamasını ayrı, dar bir Flow'a çıkar; Brain Dump eklemesini tek transaction + batch insert'e çevir.

### `app/src/main/java/com/benimgunlerim/domain/usecase/ObserveTodaySnapshotUseCase.kt`
- Bulgu: `completionLogRepository.observeAll()` (satır 52) sınırsız tüm-zamanlar sorgusu; sonuç `currentStreak()`/`currentStreakForEntity()` ile her rutin için ayrı ayrı filtrelenip taranıyor (satır 73-80, O(rutin sayısı × toplam log sayısı)).
- Risk: Uzun süreli kullanıcıda (yıllar süren completion_logs tablosu) her Today state güncellemesinde büyüyen maliyetli tam tarama.
- Öneri: Streak hesabını SQL tarafında pencere fonksiyonu/aggregate ile yap veya `observeAll()` yerine son N günü kapsayan `observeBetween()` kullan.

### `app/src/main/java/com/benimgunlerim/data/local/CompletionLogDao.kt`
- Bulgu: `observeAll()` (satır 18-19) `SELECT * FROM completion_logs ORDER BY date DESC` — `LIMIT` yok, tarih filtresi yok.
- Risk: Tablo büyüdükçe her emisyon daha ağır hale gelir; index (`index_completion_logs_date`) var ama sorgu zaten tüm satırları çekiyor, index sadece sıralamaya yardımcı olur.
- Öneri: Streak/istatistik ihtiyaçlarına göre tarih aralığı parametreli sorgulara geçilmesi.

### `app/src/main/java/com/benimgunlerim/ui/today/TodayListContainers.kt`
- Bulgu: `TaskListContainer`/`RoutineListContainer` `forEachIndexed` ile satırları oluşturuyor (satır 21-36, 68-86); `LazyColumn`/`key` kullanılmıyor.
- Risk: Büyük listelerde windowing yok; öğe silme/ekleme sırasında konum tabanlı (index tabanlı) recomposition riski.
- Öneri: `LazyColumn` + `items(tasks, key = { it.id })` / `items(routines, key = { it.id })`.

### `app/src/main/java/com/benimgunlerim/ui/components/calm/BrainDumpDialog.kt`
- Bulgu: Metin girişi 3000 karakterle sınırlı (satır 149, olumlu bir koruma). `SELECT_TASKS` adımında `parsedLines.forEach` içinde `Column().verticalScroll` kullanılıyor (satır 206-262), `LazyColumn` yok.
- Risk: Çok satırlı yapıştırmalarda (kısa satırlarla yüzlerce öğe olası) seçim ekranı tüm satırları aynı anda composed eder.
- Öneri: `LazyColumn` + `items(parsedLines, key = { it })`.

### `app/src/main/java/com/benimgunlerim/ui/components/calm/ResetDialog.kt`
- Bulgu: Nefes animasyonu (`rememberInfiniteTransition`, satır 232-241) yalnızca `ResetState.BREATHING` composed edildiğinde çalışıyor; `AnimatedContent` state değiştiğinde eski state dispose oluyor — animasyon doğru şekilde kapsanmış (pozitif bulgu). 1 saniyelik `delay(1000L)` döngüsü (satır 87-98) `LaunchedEffect(currentState)` içinde, dialog kapatıldığında composable dispose olduğu için düzgün duruyor.
- Risk: Düşük — bu dosyada ciddi bir performans sorunu tespit edilmedi.
- Öneri: Mevcut yapı korunabilir; düşük RAM cihazda gerçek FPS ölçümü (Layout Inspector) ile doğrulama önerilir.

### `app/src/main/java/com/benimgunlerim/ui/components/CelebrationSystem.kt`
- Bulgu: Elle yazılmış Canvas tabanlı `ConfettiOverlay`/`StarBurst`/`LevelUpOverlay`/`AchievementUnlockOverlay` mevcut (satır 82-227, 229-302, 477-532); ayrıca ayrı bir dosyada (`CelebrationParticles.kt`) Konfetti kütüphanesi tabanlı `SubtleCelebrationParticles` var. `HappinessBar` içinde mutluluk ≥80 iken süresiz `rememberInfiniteTransition` pulse (satır 361-367).
- Risk: İki paralel kutlama/konfeti implementasyonu bakım karmaşası ve potansiyel çifte kullanım riski taşıyor; hangi ekranların hangisini kullandığı bu denetimde netleştirilemedi ("bu alanda kanıt bulamadım" — hangi ekranların `LevelUpOverlay` vs `LevelUpDialog`+Konfetti kullandığı ayrı bir haritalama gerektirir).
- Öneri: Tek bir kutlama sistemine konsolide edilmesi; kullanılmayan implementasyonun kaldırılması.

### `app/build.gradle.kts`
- Bulgu: `release` build type'ında `isMinifyEnabled = true`, `isShrinkResources = true`, `isProfileable = true`, `proguardFiles` doğru tanımlı (satır 73-84). `versionCode = 1`, `versionName = "0.1.0"` (henüz yayın öncesi aşama). Bağımlılıklar arasında `konfetti-compose`, `firebase-crashlytics`, tam Compose BOM, Room, DataStore, WorkManager, Hilt var — hiçbiri aşırı ağır değil, APK boyutu riski düşük görünüyor (Lottie eklenmemiş).
- Risk: Düşük — release yapılandırması bu düzeyde sağlam. `versionCode`/signing config `keystore.properties`/env değişkenlerine bağlı; CI'da bu secret'ların ayarlı olup olmadığı bu denetimde doğrulanamadı.
- Öneri: Release APK/AAB boyutunu `./gradlew :app:assembleRelease` sonrası gerçek ölçümle doğrula (bu denetimde build çalıştırılmadı, sadece statik inceleme yapıldı).

### `benchmark/src/main/java/com/benimgunlerim/benchmark/StartupBenchmark.kt` ve `ScrollJankBenchmark.kt`
- Bulgu: Modül var ve doğru kurulmuş ama `warmStartup` (StartupBenchmark.kt:32-34) ve `todayScreenScrollJank` (ScrollJankBenchmark.kt:21-23) testleri `@Ignore` ile devre dışı; yalnızca `coldStartup` aktif.
- Risk: "Startup time" ve "scroll jank" için otomatik, tekrarlanabilir bir ölçüm şu an fiilen yok.
- Öneri: Flaky nedenini (cihaz profili, renderthread slice stabilitesi) çözüp testleri tekrar aktif et.

## Kullanıcı Deneyimi Etkisi

Günlük tipik kullanım (birkaç görev + birkaç rutin) için kullanıcı muhtemelen hiçbir performans sorunu hissetmeyecek — state yönetimi mantıksal olarak doğru, gereksiz `runBlocking` ya da main-thread DB çağrısı bulunamadı. Ancak uygulamanın kendi hedef kullanıcı profili (uzun süreli, alışkanlık/rutin takibi yapan kişi) tam olarak "aylarca biriken completion_logs" ve "yoğun gün — çok görev" senaryolarını üretir. Bu iki senaryoda (P0 satır 1 ve 2) kullanıcı: (a) Today ekranını her açtığında/her dakika hafif bir gecikme/pil tüketimi hissedebilir, (b) çok görevli bir günde scroll sırasında jank yaşayabilir, (c) Brain Dump ile büyük bir liste yapıştırdığında "Ekle" butonuna bastıktan sonra UI'ın birkaç saniye "takılı" hissettirmesi riski var (N ayrı transaction + N ayrı recomposition). Bunların hiçbiri "çöküyor" seviyesinde değil ama "hızlı ve akıcı" hissi zamanla aşınabilir.

## Teknik Borç Etkisi

- Tüm liste UI'larının `LazyColumn` yerine `Column+forEach` ile yazılmış olması, gelecekte "sonsuz kaydırma" veya arşiv/geçmiş görünümleri eklenmesini zorlaştıracak bir mimari borç oluşturuyor — bu ilke ekranlar arasında tutarlı bir hatadır (Today, Plan, Routines hepsinde aynı desen), tek noktadan (örn. ortak bir `AppLazyList` molekülü) düzeltilmesi gerekir.
- `TodayScreen.kt`/`TodayViewModel.kt` boyutu, projenin kendi CLAUDE.md kuralına (Demir Kural #5, ekran ≤200 satır) doğrudan aykırı; bu, hem performans hem bakılabilirlik borcu — her yeni özellik bu dosyalara eklendikçe recomposition kapsamı daha da genişleyecek.
- `observeAll()` tam-tablo tarama deseni sadece Today'de değil, ileride Progress/Achievements ekranlarında da benzer şekilde kullanılıyor olabilir (bu denetimde tam olarak doğrulanmadı — "bu alanda kanıt bulamadım", ayrı bir tarama gerekir) — eğer öyleyse aynı borç birden fazla yerde tekrarlanmış olur.
- Batch insert API'lerinin (TaskDao/RoutineDao) hiç bulunmaması, gelecekteki her "toplu işlem" özelliğinin (şablonlar, CSV import, tekrarlayan görev oluşturma) aynı N-transaction hatasını miras almasına yol açar.

## Release / Monetizasyon Riski

- Şu anki `versionCode=1`/`versionName=0.1.0` ile proje henüz ilk yayın öncesi; performans açısından **engelleyici** (blocker) bir çökme/ANR kanıtı bulunmadı, dolayısıyla "yayına çıkamaz" seviyesinde bir risk yok.
- Ancak Play Store incelemesi ve ilk kullanıcı izlenimleri açısından, uzun-vadeli kullanıcıda (haftalar sonra) ortaya çıkacak "uygulama yavaşladı" algısı (completion_logs tam tarama + minute ticker'ın gereksiz geniş recomposition'ı) düşük yıldız/kaldırma riskini artırır — bu tür sorunlar genelde launch sonrası fark edilir ve geriye dönük çözümü daha maliyetlidir.
- Monetizasyon planı bu denetimde görülemedi (Shop ekranı gold/XP ile sanal ürün satıyor gibi görünüyor, gerçek para/IAP entegrasyonu bulunamadı) — performans riski doğrudan bir ödeme akışını etkilemiyor gibi görünse de, "Dükkan" ekranının performans davranışı bu turda ayrıca incelenmedi ("bu alanda kanıt bulamadım").
- Benchmark modülünün iki kritik testinin (`warmStartup`, `scrollJank`) devre dışı olması, "release öncesi performans doğrulandı" iddiasını şu an kanıtla desteklenemez kılıyor — bu bir süreç/güvence riski, doğrudan kullanıcı riski değil.

## Performance Riskleri
| Alan | Risk | Senaryo | Öneri |
|---|---|---|---|
| Completion log okuma | `observeAll()` sınırsız tam tablo taraması + her rutin için ayrı streak filtreleme | Yıllar süren kullanım, her Today state güncellemesi (görev toggle, dakika ticker) | Tarih-sınırlı sorgu veya SQL agregasyonu ile streak hesabı |
| Today liste render | `LazyColumn` yok, `Column+forEach`, `key` yok | 100 görev + 50 rutin senaryosu (audit önerisi) | `LazyColumn` + `items(..., key = { it.id })` |
| Recomposition kapsamı | `List<T>` parametreleri Compose'da unstable; dakikalık ticker tüm state'i yeniliyor | Kullanıcı hiçbir işlem yapmadan her 60 saniyede bir | Dar kapsamlı state ayrımı + `ImmutableList` / strong skipping mode |
| Brain Dump toplu ekleme | N ayrı `addTaskUseCase()` çağrısı, tek transaction yok | 500 satırlık yapıştırma, "Ekle" sonrası | `DatabaseTransactionRunner` + toplu `insertAll` |
| Brain Dump seçim ekranı | `parsedLines.forEach` + `verticalScroll`, `LazyColumn` yok | Yüzlerce kısa satırlı yapıştırma | `LazyColumn` + `key` |
| Nefes/Reset animasyonu | `rememberInfiniteTransition` yalnızca BREATHING state'inde composed (doğru kapsam) | Normal kullanım | Düşük risk; düşük RAM cihazda FPS doğrulaması önerilir |
| HappinessBar pulse | Mutluluk ≥80 iken süresiz infinite animasyon, görünürlük guard'ı yok | Bileşenin arka planda/offscreen kaldığı durumlar | Görünürlük tabanlı pause/resume ekle |
| Konfetti/kutlama | İki paralel implementasyon (Konfetti kütüphanesi + elle yazılmış Canvas) | Level-up/başarım/gün kapama anları | Tek sisteme konsolide et |
| Benchmark güvencesi | `warmStartup` ve `scrollJank` testleri `@Ignore` | Release öncesi performans doğrulama süreci | Flaky nedenini çöz, testleri aktive et |
| APK/dependency yükü | Mevcut bağımlılık seti (Compose, Room, DataStore, Work, Hilt, Firebase Crashlytics, Konfetti) ölçülü; Lottie yok | Gelecekte Lottie/başka ağır animasyon kütüphanesi eklenirse | Ekleme öncesi APK boyut diff'i ve gerekirse dynamic feature/on-demand modül değerlendirilmeli |

## Önceliklendirilmiş Yapılacaklar

### P0 — Yayın öncesi şart
- `completion_logs.observeAll()` tam tablo taramasını tarih-sınırlı sorguya çevir (streak hesabı SQL veya sınırlı pencere ile).
- Today ekranındaki (ve varsa Plan/Routines) tüm görev/rutin listelerini `LazyColumn` + `key = { it.id }` ile yeniden yaz.
- Brain Dump toplu görev eklemeyi tek `DatabaseTransactionRunner` transaction'ı + batch insert'e çevir.

### P1 — Kısa vadede gerekli
- `TodayScreen.kt`/`TodayViewModel.kt`'yi projenin kendi 200 satır kuralına uyacak şekilde alt bileşenlere/parçalara böl; `canCloseDay` gibi zamana bağlı alanları ayrı dar bir state'e taşı.
- İki paralel kutlama/konfeti sistemini tek sisteme konsolide et.
- `warmStartup`/`scrollJank` benchmark testlerini stabilize edip tekrar aktive et.
- Brain Dump seçim ekranını `LazyColumn`'a taşı.

### P2 — Polish / ileri iyileştirme
- `HappinessBar` gibi süresiz infinite animasyonlara görünürlük tabanlı pause/resume ekle.
- Task/Routine DAO'larına `insertAll` gibi batch API'ler ekleyerek gelecekteki toplu işlemler için altyapı hazırla.
- `ImmutableList`/strong skipping mode değerlendirerek Compose stabilite uyarılarını azalt.
- Release APK/AAB boyutunu gerçek bir build ile ölç ve baseline olarak kaydet.

## 1 Haftalık Düzeltme Planı
- Gün 1-2: `observeAll()` → tarih-sınırlı sorgu + streak hesaplama refactor'ü, ilgili unit testlerin güncellenmesi.
- Gün 3: Today ekranındaki `TaskListContainer`/`RoutineListContainer`'ı `LazyColumn`'a taşı, `key` ekle.
- Gün 4: Brain Dump toplu ekleme akışını `DatabaseTransactionRunner` + batch insert ile yeniden yaz.
- Gün 5: Layout Inspector ile Today ekranında gerçek cihazda (veya düşük RAM emülatörde) 100 görev + 50 rutin senaryosunu test et, recomposition count ölç, önceki/sonraki karşılaştır.

## 2 Haftalık Düzeltme Planı
- Hafta 1: Yukarıdaki P0 maddeleri + `TodayScreen.kt`/`TodayViewModel.kt` bölünmesinin ilk aşaması (Header/Task/Routine/CloseDay ayrımı).
- Hafta 2: Konfeti sistemlerinin konsolidasyonu, benchmark testlerinin (`warmStartup`, `scrollJank`) stabilize edilip CI'a bağlanması, Brain Dump seçim ekranının `LazyColumn`'a taşınması, batch DAO API'lerinin eklenmesi ve 500 satırlık Brain Dump senaryosunun gerçek cihazda ölçülmesi.

## Final Karar
Beta seviyesinde performans kabul edilebilir; **release/yayın için P0 maddeleri (completion-log tam tarama, LazyColumn eksikliği, Brain Dump transaction) kapatılmadan "performans doğrulandı" denemez.** Mimari temel (Room indeksleri, DataStore budama, StateFlow paylaşım stratejisi, ayrı benchmark modülü) sağlam olduğu için bu iyileştirmeler büyük bir yeniden yazım gerektirmiyor — 1-2 haftalık hedefli bir sprintle kapatılabilir bir borç seviyesindedir.
