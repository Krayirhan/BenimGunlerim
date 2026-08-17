# Audit Raporu — Testing & QA

> ⚠️ **2026-08-17 güncellemesi:** Bu raporun 4 P0 maddesi de kapandı: test paketi derleniyor ve geçiyor (50 test sınıfı, 0 hata), `BrainDumpParserTest`/`LightDayModeTest`/`UserPreferencesRepositoryTest` eklendi. CI artık gerçekten yeşil. Güncel durum için bkz. [`INCREMENTAL_REAUDIT_2026-08-17.md`](INCREMENTAL_REAUDIT_2026-08-17.md). Bu doküman tarihsel kayıt olarak değiştirilmemiştir.

## Genel Puan
5 / 10

## Kısa Karar
Test altyapısı iddialı ve bazı katmanlarda (GameEngine, CloseDayUseCase, CompletionLogExtensions, Room DAO'ları) gerçekten iyi: 368 unit test + 62 instrumented test, JaCoCo eşiği, detekt, lint, connected test ve hatta macrobenchmark içeren olgun bir CI pipeline'ı var. Ama şu an `./gradlew testDebugUnitTest` çalıştırıldığında proje **derlenmiyor** — bu tek başına yayın/beta kararını "beklet"e çeker. Bunun ötesinde, 08 numaralı audit talimatının özellikle işaret ettiği dört alanın (Brain Dump satır ayrıştırma, Hafif Gün Modu sıfırlama, Onboarding öneri mapping'i, UserPreferencesRepository) **hiçbirinde tek bir test yok**, üstelik bu mantığın bir kısmı test edilebilir bir fonksiyona bile çıkarılmamış (Composable içinde private lambda/fun olarak gömülü). Karar: refactor + P0 listesi tamamlanmadan yayın yapılmamalı; mevcut haliyle "beklet".

## En Güçlü 5 Taraf
1. `GameEngineTest.kt` (270 satır, 30+ test) XP/level/happiness/companion-mood mantığını threshold sınırlarına kadar (99 vs 100 vs 300 XP) titizlikle test ediyor.
2. `CloseDayUseCaseTest.kt` gün kapama akışını mood etiketleri, perfect-day ödülü, level-up tespiti, özel tarih ve "zaten kapatılmış gün" senaryolarıyla iyi kapsıyor.
3. `CompletionLogExtensionsTest.kt` streak hesaplamasını gerçek edge-case'lerle test ediyor: bugünün eksik olması, geçersiz tarih formatı, `skipped` durumundaki günlerin sayılmaması.
4. `app/src/androidTest/java/com/benimgunlerim/data/local/DaoTests.kt` ve `TaskDaoTest.kt` Room DAO'larını unique-constraint, tarih aralığı sorguları ve silme senaryolarıyla instrumented test olarak kapsıyor; `AppDatabaseMigrationTest.kt` migration'ları da test ediyor.
5. `.github/workflows/android.yml` olgun bir CI: unit test + JaCoCo coverage-verification + detekt + lint + `assembleRelease`/`bundleRelease` + connected (emulator) UI testleri + zamanlanmış macrobenchmark job'u — çoğu Android projesinde bulunmayan bir seviye.

## En Kritik 10 Sorun
| Öncelik | Sorun | Etki | Kanıt/Dosya | Öneri |
|---|---|---|---|---|
| P0 | Unit test derlemesi şu an başarısız | CI kırmızı, `./gradlew test` hiçbir sonuç üretmiyor, mevcut branch release-quality gate'i geçemez | `app/src/test/.../TodayViewModelTest.kt:270-276,538-544` yanlış constructor argümanları; `SettingsViewModelTest.kt:63` `FakePrefs` yeni `setLightDayMode` metodunu implemente etmiyor; `ObserveProgressSnapshotUseCaseTest.kt:34` eksik `taskRepository`/`routineRepository` parametreleri | Faz A+B refactor'ünün değiştirdiği constructor imzalarını test dosyalarına yansıt; `FakePrefs`'e `setLightDayMode` ekle |
| P0 | Brain Dump satır ayrıştırma mantığı test edilemez ve test edilmemiş durumda | Boş satır, tek satır, `-`/`•` önekli satır, tekrar eden satır, 3000 karakter sınırı gibi durumlar hiç doğrulanmıyor; regresyon riski yüksek | `app/src/main/java/com/benimgunlerim/ui/components/calm/BrainDumpDialog.kt:171-180` — parse mantığı Composable içinde inline lambda, saf fonksiyon olarak dışa çıkarılmamış | Parse mantığını `BrainDumpParser.parseLines(text: String): List<String>` gibi saf bir fonksiyona taşı; `BrainDumpParserTest` yaz (boş, tek satır, çok satır, uzun satır, `-`/`•` önek, duplicate) |
| P0 | Hafif Gün Modu günlük sıfırlama mantığı hiç test edilmemiş | Gün değişiminde `lightDayModeDate` karşılaştırması yanlış davranırsa kullanıcı yanlışlıkla "hafif gün" modunda kalabilir/kaybedebilir; timezone/gece yarısı edge-case'i doğrulanmamış | `app/src/main/java/com/benimgunlerim/ui/today/TodayViewModel.kt:247` `isLightDay = snapshot.gameState.lightDayModeDate == dateTimeProvider.today().toString()`; `grep`'te `lightDayMode` için test/androidTest içinde sıfır eşleşme | `LightDayModeDateTest` yaz: dün ayarlanmış mod bugün `false` dönmeli, bugün ayarlanmış `true` dönmeli, `setLightDayMode(false, ...)` tarihi boşaltmalı |
| P0 | UserPreferencesRepository için hiçbir test yok (unit veya instrumented) | XP/gold/happiness/streak/reward-once/prune gibi kritik state DataStore'a yazılıyor; `pruneRewardedEvents` (90 gün retention, 500 kayıt cap) hiç doğrulanmamış — veri bozulması/kaybı sessizce geçebilir | `app/src/main/java/com/benimgunlerim/data/UserPreferencesRepository.kt` (tüm dosya), özellikle `grantRewardOnce` (215-236) ve `pruneRewardedEvents` (243-251) | Robolectric + `DataStore` in-memory test kurulumu ile `UserPreferencesRepositoryTest` ekle: `grantRewardOnce` idempotency, `pruneRewardedEvents` sınırları, `purchaseItem` yetersiz altın senaryosu |
| P1 | Onboarding öneri mapping'i (`suggestedRoutines`, `suggestedTaskTitle`) test edilemez konumda | 5 ihtiyaç × 3 yoğunluk kombinasyonunun doğru rutinleri önerdiği hiç doğrulanmıyor; `OnboardingViewModelTest` sadece mock çağrılarını doğruluyor, mapping'in kendisini değil | `app/src/main/java/com/benimgunlerim/ui/onboarding/OnboardingScreen.kt:104-144` — `private fun` olarak Composable dosyasında gömülü | Mapping'i `OnboardingRecommendations.kt` içine public/internal saf fonksiyon olarak taşı; `OnboardingRecommendationTest` ile 5 need × 3 intensity kombinasyonunu tablo-testle doğrula |
| P1 | ResetDialog (1 Dakikalık Reset) süre/animasyon akışı için hiç test yok | Sayaç/animasyon mantığında regresyon sessizce geçebilir (ör. süre bitmeden kapanma, geri sayımın yanlış başlaması) | `app/src/main/java/com/benimgunlerim/ui/components/calm/ResetDialog.kt`; test/androidTest ağacında `ResetDialog` için sıfır eşleşme | En azından zamanlayıcı mantığını `TickerProvider` üzerinden test eden bir ViewModel/state-holder testi ekle; UI akışı için androidTest |
| P1 | JaCoCo coverage eşiği düşük ve yalnızca hat/branch bazlı, kritik-dosya bazlı değil | %42 satır / %22 dal eşiği, yukarıdaki gibi tamamen test edilmemiş kritik dosyaların (BrainDumpDialog, ResetDialog, UserPreferencesRepository) genel ortalamayı düşürmeden "geçmesine" izin veriyor | `app/build.gradle.kts:204,211` `minimum = "0.42"`, `minimum = "0.22"` | Kritik domain dosyaları için modül/paket bazlı ayrı eşik tanımla (ör. `domain/`, `data/` paketleri için %70+) |
| P1 | Compose UI (androidTest) testleri sığ — sadece tag görünürlüğü, kritik iş akışları yok | `TodayScreenTest.kt` büyük ölçüde `assertIsDisplayed` düzeyinde; Brain Dump akışı, Hafif Gün Modu toggle'ı, onboarding seçim akışı hiç instrumented test edilmiyor | `app/src/androidTest/java/com/benimgunlerim/TodayScreenTest.kt` (99 satır, 8 test, çoğu görünürlük kontrolü) | Brain Dump: metin gir → görev seç → ekle akışını uçtan uca test et; Hafif Gün Modu toggle sonrası UI state değişimini doğrula |
| P2 | `RoutineDetailViewModel.calculateBestStreak` private ve doğrudan test edilmiyor | Sadece `uiState.currentStreak` dolaylı olarak test ediliyor; "en iyi seri" hesaplaması (ayrı algoritma) doğrudan doğrulanmıyor | `app/src/main/java/com/benimgunlerim/ui/routines/RoutineDetailViewModel.kt:123` | `calculateBestStreak`'i `internal` yap veya `CompletionLogExtensions`'a taşıyıp doğrudan test et |
| P2 | Process death / state restore ve manuel QA checklist için audit sırasında kanıt bulunamadı | `SavedStateHandle` kullanımı ya da restore testi repo genelinde görülmedi; `docs/` altında manuel QA checklist dosyasına rastlanmadı | Kod tabanı genelinde arama (bu rapor kapsamında) | Süreç dokümantasyonu: release öncesi manuel smoke-test checklist'i (`docs/qa/manual_checklist.md`) oluştur; process-death senaryosu için en azından `TodayViewModel` `SavedStateHandle` kullanımı değerlendirilsin |

## Dosya Bazlı Bulgular

### `app/src/main/java/com/benimgunlerim/ui/components/calm/BrainDumpDialog.kt`
- Bulgu: Satır ayrıştırma (`textInput.lines().map{...}.filter{...}.distinct()`) doğrudan `Button.onClick` lambda'sı içinde, satır 171-180. Saf bir fonksiyon değil, Compose state'ine bağlı.
- Risk: Parse kuralları (3000 karakter sınırı, `-`/`•` önek temizleme, boş satır filtreleme, duplicate temizleme) değişirse hiçbir test kırılmaz; regresyon production'a kadar fark edilmeyebilir.
- Öneri: `object BrainDumpParser { fun parseLines(raw: String): List<String> }` şeklinde çıkar, Composable'dan çağır, `BrainDumpParserTest` ekle.

### `app/src/main/java/com/benimgunlerim/ui/today/TodayViewModel.kt`
- Bulgu: `isLightDay` hesaplaması (satır 247) ve `toggleLightDayMode` (satır 270-273) hiçbir unit testte referans edilmiyor; `TodayViewModelTest.kt` şu an zaten derlenmiyor (bkz. P0 sorunu).
- Risk: Gün değişiminde `lightDayModeDate` karşılaştırması `LocalDate.toString()` formatına dayanıyor; `dateTimeProvider.today()` implementasyonu değişirse (örn. timezone) sessizce bozulabilir.
- Öneri: `LightDayModeDateTest` ile `FixedDateTimeProvider` kullanarak gün sınırı senaryolarını (dün/bugün/yarın) doğrula.

### `app/src/main/java/com/benimgunlerim/data/UserPreferencesRepository.kt`
- Bulgu: 385 satırlık dosyada XP, gold, happiness, streak-ilişkili sayaçlar, `grantRewardOnce`, `pruneRewardedEvents`, `purchaseItem` gibi para/oyunlaştırma açısından kritik metodların hiçbiri test edilmiyor.
- Risk: `pruneRewardedEvents` yanlış çalışırsa ya ödüller tekrar tekrar verilebilir (gold/XP enflasyonu) ya da DataStore sınırsız büyüyebilir.
- Öneri: Robolectric tabanlı `UserPreferencesRepositoryTest` — gerçek DataStore ile in-memory/temp dizin kullanarak yaz.

### `app/src/main/java/com/benimgunlerim/ui/onboarding/OnboardingScreen.kt`
- Bulgu: `suggestedRoutines` (104-135) ve `suggestedTaskTitle` (137-144) `private` top-level fonksiyonlar; `OnboardingViewModelTest.kt` bunları hiç çağırmıyor, sadece `OnboardingViewModel.completeOnboarding` mock akışını doğruluyor.
- Risk: 5 ihtiyaç × 3 yoğunluk = 15 kombinasyonun hiçbiri doğrulanmıyor; yanlış rutin önerisi kullanıcı deneyimini doğrudan bozar ama testler bunu asla yakalamaz.
- Öneri: Mapping fonksiyonlarını `internal`/public yap, ayrı dosyaya taşı, tablo-güdümlü test yaz.

### `app/src/test/java/com/benimgunlerim/ui/today/TodayViewModelTest.kt`
- Bulgu: 557 satırlık kapsamlı bir test dosyası (toggleTask, undoTaskToggle, reward/level-up event'leri iyi kapsanmış) ama şu an constructor imzası uyuşmazlığı nedeniyle derlenmiyor (satır 270-276, 538-544).
- Risk: Test dosyası aslında iyi tasarlanmış; ama mevcut haliyle CI'da "yeşil" görünmüyor, bu da güven kaybı ve gerçek regresyonların gözden kaçması riski yaratıyor.
- Öneri: `TodayViewModel` constructor parametre sırası/isimleriyle test setup'ını senkronize et — muhtemelen sadece parametre eşleme hatası, mantıksal bir yeniden yazım gerekmiyor.

### `app/src/test/java/com/benimgunlerim/ui/settings/SettingsViewModelTest.kt`
- Bulgu: `FakePrefs` (satır 63) `UserPreferencesAccess` arayüzüne yeni eklenen `setLightDayMode` metodunu implemente etmiyor.
- Risk: Arayüz her genişletildiğinde bu tür fake sınıfların manuel güncellenmesi unutulabiliyor; derleme hatası en azından erken yakalıyor ama şu an yakalanmış hata çözülmeden bırakılmış.
- Öneri: `FakePrefs`'e `override suspend fun setLightDayMode(...)` ekle.

## Kullanıcı Deneyimi Etkisi
Test kapsamındaki boşluklar doğrudan kullanıcının hissedeceği üç noktaya çıkıyor: (1) Brain Dump'ta satır ayrıştırma bozulursa kullanıcı yazdığı görevlerin bir kısmını kaybedebilir ya da tekrarlanan/boş görevler oluşabilir — sessiz veri kaybı riski. (2) Hafif Gün Modu günü değiştirmezse kullanıcı ertesi gün de "hafif gün" hedeflerinde kalır, bu da gamification dengesini (XP/streak beklentisi) bozar. (3) Onboarding'de yanlış rutin önerisi, ilk izlenimi doğrudan etkiler — yeni kullanıcı deneyiminde en kritik andır ve şu an hiçbir otomatik doğrulama yok.

## Teknik Borç Etkisi
Mevcut mimari geçişi (Faz A-E) sırasında test dosyaları kod değişikliklerinin gerisinde kalmış; bu, "refactor sırasında testler kırılırsa hemen fark edilir" güvencesini şu an sağlamıyor çünkü zaten kırık durumdalar. Kritik iş mantığının (BrainDump parse, onboarding mapping) UI katmanına (Composable/private fun) gömülü olması, "domain katmanı test edilebilir olmalı" ilkesini ihlal ediyor ve gelecekteki her refactor'de bu mantığın tekrar keşfedilip taşınmasını gerektirecek — şimdi taşınmazsa borç büyüyerek devam eder.

## Release / Monetizasyon Riski
Uygulama şu anda parasal işlem (satın alma, IAP) içermiyor gibi görünüyor (gold/XP oyun-içi ekonomi, gerçek para değil), bu yönüyle doğrudan finansal risk düşük. Ancak `UserPreferencesRepository.purchaseItem` ve `grantRewardOnce` test edilmeden yayına çıkarsa gold/XP ekonomisinde tutarsızlık (ör. ödülün birden fazla verilmesi) kullanıcı güvenini zedeleyebilir ve gelecekte gerçek IAP eklenirse bu borç doğrudan finansal riske dönüşür. Daha acil risk: mevcut haliyle CI kırmızı olduğu için bu branch/PR release-quality gate'inden geçemez — bu, "release-monetizasyon riski" değil doğrudan "release engelleyici" bir durumdur.

## Önceliklendirilmiş Yapılacaklar

### P0 — Yayın öncesi şart
- `TodayViewModelTest.kt`, `SettingsViewModelTest.kt`, `ObserveProgressSnapshotUseCaseTest.kt` derleme hatalarını çöz — `./gradlew testDebugUnitTest` yeşile dönmeli.
- `BrainDumpParser` saf fonksiyonunu çıkar ve `BrainDumpParserTest` yaz (boş/tek/çoklu satır, `-`/`•` önek, duplicate, 3000 karakter sınırı).
- `LightDayModeDateTest` yaz — gün sınırı geçişini `FixedDateTimeProvider` ile doğrula.
- `UserPreferencesRepositoryTest` yaz — en azından `grantRewardOnce` idempotency ve `pruneRewardedEvents` sınırları.

### P1 — Kısa vadede gerekli
- Onboarding öneri mapping'ini test edilebilir hale getir ve `OnboardingRecommendationTest` yaz.
- `ResetDialog` için zamanlayıcı/state testi ekle.
- JaCoCo eşiğini paket bazlı ayır; kritik domain paketleri için daha yüksek eşik koy.
- Brain Dump ve Hafif Gün Modu için en az birer instrumented (Compose UI) test ekle.

### P2 — Polish / ileri iyileştirme
- `calculateBestStreak`'i test edilebilir/paylaşılan bir yere taşı.
- Manuel QA checklist dokümanı oluştur (`docs/qa/manual_checklist.md`).
- Process-death / state-restore senaryosu için en azından bir instrumented test ekle.

## 1 Haftalık Düzeltme Planı
- Gün 1-2: Derleme hatalarını çöz, CI'ı yeşile döndür (P0 madde 1).
- Gün 3: `BrainDumpParser` çıkarımı + testleri.
- Gün 4: `LightDayModeDateTest`.
- Gün 5: `UserPreferencesRepositoryTest` (Robolectric kurulumu dahil).

## 2 Haftalık Düzeltme Planı
- Hafta 1: Yukarıdaki P0 maddelerinin tamamı + CI'ın stabil yeşil kalması.
- Hafta 2: Onboarding mapping testi, ResetDialog testi, JaCoCo paket-bazlı eşik ayarı, Brain Dump/Hafif Gün Modu için instrumented testler, manuel QA checklist dokümanı.

## Final Karar
Beklet. Test altyapısının iskeleti (CI pipeline, DAO testleri, GameEngine/CloseDayUseCase kapsamı) production kalitesine yakın, ancak şu anki haliyle proje derlenmiyor ve 08 numaralı audit talimatının özellikle vurguladığı dört kritik akış (Brain Dump parse, Hafif Gün Modu sıfırlama, Onboarding mapping, UserPreferencesRepository) sıfır test kapsamına sahip. P0 listesi (tahmini 1 hafta) tamamlanmadan beta/yayın kararı verilmemeli.

## Test Coverage Riskleri
| Akış | Test Var mı? | Risk | Önerilen Test |
|---|---|---|---|
| Brain Dump satır ayrıştırma | Hayır | Sessiz veri kaybı/duplicate görev riski; mantık Composable içine gömülü | `BrainDumpParserTest` (boş/tek/çok satır, önek temizleme, duplicate, 3000 karakter sınırı) |
| Hafif Gün Modu günlük sıfırlama | Hayır | Gün değişince mod yanlışlıkla açık/kapalı kalabilir | `LightDayModeDateTest` (`FixedDateTimeProvider` ile dün/bugün/yarın senaryoları) |
| Görev tamamlama + undo | Kısmen var | `TodayViewModelTest.toggleTask_*` ve `undoTaskToggle` testleri mevcut ama dosya şu an derlenmiyor | Derleme hatasını çöz, mevcut testleri çalışır hale getir |
| Rutin streak hesabı (`currentStreak`) | Var, iyi kapsam | `CompletionLogExtensionsTest` edge-case'leri (eksik gün, geçersiz tarih, skipped) kapsıyor | Ek yok; sürdürülebilir |
| Rutin "en iyi seri" (`calculateBestStreak`) | Dolaylı/zayıf | Private fonksiyon, doğrudan test edilmiyor | `calculateBestStreak`'i paylaşılan/erişilebilir hale getirip doğrudan test et |
| Onboarding öneri mapping'i | Hayır | Yanlış ihtiyaç→rutin eşlemesi ilk kullanıcı deneyimini bozar | `OnboardingRecommendationTest` (5 need × 3 intensity tablo testi) |
| XP/Level hesaplama | Var, çok iyi | `GameEngineTest` threshold sınırlarına kadar kapsıyor | Ek yok; sürdürülebilir |
| Gün sonu kapatma (day close) özeti | Var, iyi kapsam | `CloseDayUseCaseTest` mood/perfect-day/level-up/custom-date senaryolarını kapsıyor | Ek yok; sürdürülebilir |
| UserPreferencesRepository | Hayır | XP/gold/streak durumu DataStore'a yazılıyor, hiç doğrulanmıyor; ödül tekrarı/veri büyümesi riski | `UserPreferencesRepositoryTest` (Robolectric, `grantRewardOnce`, `pruneRewardedEvents`, `purchaseItem`) |
| ResetDialog (1 Dakikalık Reset) süre/animasyon | Hayır | Sayaç/temizleme mantığında regresyon sessiz kalır | Zamanlayıcı state testi + androidTest |
| Room/DAO katmanı | Var, iyi kapsam | `DaoTests.kt`, `TaskDaoTest.kt`, `AppDatabaseMigrationTest.kt` unique constraint, aralık sorguları, migration'ları kapsıyor | Ek yok; sürdürülebilir |
| Date/time timezone edge case | Kısmen | `CompletionLogExtensionsTest` geçersiz tarih formatını kapsıyor ama timezone/DST senaryosu yok | `DateTimeProvider` implementasyonu için timezone-aware test |
| Compose UI (kritik akışlar) | Zayıf | Mevcut testler çoğunlukla tag görünürlüğü; Brain Dump/Hafif Gün Modu/Onboarding akışları instrumented test edilmiyor | Uçtan uca instrumented testler ekle |
| Manuel QA checklist | Kanıt bulunamadı | Release öncesi elle kontrol süreci dokümante değil | `docs/qa/manual_checklist.md` oluştur |
| `assembleDebug` dışında release build/test | Var (CI'da) | `android.yml` `release-quality` job'unda `verifyReleaseSigning`, `assembleRelease`, `bundleRelease` çalıştırılıyor; ama şu an unit test adımı derlenmediği için bu job da başarısız olur | Derleme hatalarını çöz (P0), release job'unun gerçekten yeşil geçtiğini doğrula |
