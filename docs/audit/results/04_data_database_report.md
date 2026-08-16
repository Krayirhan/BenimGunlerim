# Audit Raporu — Data / Database

## Genel Puan
7 / 10

## Kısa Karar
Data katmanı, tek geliştiricili bir projeye göre olağanüstü olgun: Room şeması normalize, tarihler tutarlı biçimde ISO-8601 `LocalDate` string olarak saklanıyor, streak hesabı doğru, reward sistemi `eventKey` tabanlı idempotent, export/import validasyonu ciddi düzeyde sıkı. Ancak yayın öncesi mutlaka kapatılması gereken somut açıklar var: migration geçmişi eksik (yalnızca 6→7 kayıtlı, `fallbackToDestructiveMigration()` yok), Brain Dump toplu ekleme transaction dışı, ve görev silme/geri alma akışında alt görevler (subtask) sessizce kalıcı olarak kayboluyor. Mimari refactor gerektirmiyor; hedefli düzeltme + test yeterli. Karar: **refactor değil, düzeltme yapıp beta'ya devam**.

## En Güçlü 5 Taraf
1. Tarih/zaman modeli tutarlı: `TaskEntity.plannedDate`, `CompletionLogEntity.date`, `DailyStateEntity.date` hepsi `LocalDate.toString()` (yyyy-MM-dd) ile saklanıyor; tüm okuma/yazma `DateTimeProvider` soyutlaması üzerinden geçiyor (`app/src/main/java/com/benimgunlerim/domain/DateTimeProvider.kt:12-23`), timezone/sistem saati testte sabitlenebiliyor.
2. Tamamlanma kayıtları ayrı bir `completion_logs` tablosunda, entity üstünde kırılgan boolean alanlar yok — `CompletionLogEntity` (`app/src/main/java/com/benimgunlerim/data/local/entity/CompletionLogEntity.kt:15-26`) `(entityType, entityId, date)` üzerinde `unique` index taşıyor, bu da çift kayıt riskini DB seviyesinde engelliyor.
3. Streak hesabı doğru ve basit: `List<CompletionLogEntity>.currentStreak(today)` (`app/src/main/java/com/benimgunlerim/data/CompletionLogExtensions.kt:7-31`) bugünü veya dünü referans alıp geriye doğru ardışık günleri sayıyor; hem günlük hem rutin bazlı streak (`currentStreakForEntity`) aynı fonksiyonu kullanıyor.
4. Ödül/XP sistemi çift verilmeye karşı korumalı: `RewardGrantService.grantOnce` (`app/src/main/java/com/benimgunlerim/domain/service/RewardGrantService.kt:51-73`) her olay için benzersiz `eventKey` (`"task:$id:$date"`, `"dayClose:$date"` vb.) kullanıyor ve `UserPreferencesRepository.grantRewardOnce` (satır 215-236) bu anahtarı `rewardedEvents` listesinde tutup tekrar tetiklenmeyi engelliyor; ayrıca 90 günlük TTL + 500 kayıt hard-cap ile (satır 238-251) DataStore'un sınırsız büyümesi önlenmiş.
5. Export/Import katmanı üretim kalitesinde: `DataImportService.validateImportData` (`app/src/main/java/com/benimgunlerim/data/DataImportService.kt:298-416`) boyut limiti (5 MB), kayıt sayısı üst sınırları, tekil ID kontrolü, dangling foreign-key kontrolü (subtask→task, log→task/routine), ISO tarih/saat format doğrulaması ve enum whitelist kontrolü yapıyor; import işlemi `DatabaseTransactionRunner.runInTransaction` içinde atomik.

## En Kritik 10 Sorun
| Öncelik | Sorun | Etki | Kanıt/Dosya | Öneri |
|---|---|---|---|---|
| P0 | Room migration geçmişi eksik: sadece `MIGRATION_6_7` tanımlı, `app/schemas/` altında yalnızca `7.json` var, `fallbackToDestructiveMigration()` de kullanılmıyor | v1-v6 şemasında kurulu bir cihaz güncelleme aldığında Room `IllegalStateException` fırlatır, uygulama migration bulunamadığı için açılmaz / tüm kullanıcı verisi erişilemez hale gelir | `app/src/main/java/com/benimgunlerim/di/AppModule.kt:67-70`, `app/src/main/java/com/benimgunlerim/data/local/Migrations.kt:17-23`, `app/schemas/com.benimgunlerim.data.local.AppDatabase/7.json` (tek dosya) | v1→7 arası her sürüm için migration nesnesi yaz, eksik `schemas/*.json` dosyalarını commit et veya (geliştirme aşamasındaysa ve gerçek kullanıcı yoksa) bunu bilinçli bir karar olarak dokümante et |
| P0 | Görev silme + geri alma (undo) akışında alt görevler kalıcı olarak kayboluyor | `SubTaskEntity` `ON DELETE CASCADE` ile bağlı; `DeleteTaskUseCase` görevi silince alt görevler DB'de kalıcı silinir, `RestoreTaskUseCase`/`TaskRepository.restore()` sadece `TaskEntity`'i yeniden insert eder, subtask'ları geri getirmez → kullanıcı "geri al" dese bile alt görev listesi boş döner | `app/src/main/java/com/benimgunlerim/data/local/entity/SubTaskEntity.kt:10-17`, `app/src/main/java/com/benimgunlerim/data/TaskRepository.kt:205-221`, `app/src/main/java/com/benimgunlerim/domain/usecase/RestoreTaskUseCase.kt:10-14` | Silmeden önce subtask listesini snapshot'la (ViewModel'de `deletedTasksById` yanına `deletedSubTasksById` ekle) ve restore sırasında birlikte geri yaz |
| P1 | Brain Dump toplu görev ekleme transaction içinde değil ve `insertAll` batch API'si kullanılmıyor | `addTasksFromBrainDump` her başlığı ayrı `suspend` çağrısıyla, döngü içinde tek tek ekliyor; N görevden biri ortasında hata verirse (ör. reminder scheduling exception, IO) kısmi ekleme oluşur, kullanıcı "5 görev ekledim" derken 2'si eksik kalabilir | `app/src/main/java/com/benimgunlerim/ui/today/TodayViewModel.kt:277-290` | `DatabaseTransactionRunner.runInTransaction` içine al veya `TaskDao.insertAll` (zaten mevcut: `app/src/main/java/com/benimgunlerim/data/local/TaskDao.kt:34-35`) kullanan toplu bir `addTasksBulk` repository metodu ekle |
| P1 | `ToggleRoutineUseCase`, aynı işi yapan `ToggleTaskUseCase`'in aksine `DatabaseTransactionRunner` kullanmıyor | Rutin tamamlama/log yazma ile tercih (`prefsRepository`) güncellemeleri arasında transaction garantisi yok; süreç ortasında kesilirse log yazılıp XP/gold verilmeden kalabilir veya tersi | `app/src/main/java/com/benimgunlerim/domain/usecase/ToggleRoutineUseCase.kt:21-72` (transactionRunner parametresi yok) vs `ToggleTaskUseCase.kt:30,43-46,58-61` | `ToggleRoutineUseCase`'e de `DatabaseTransactionRunner` enjekte edip log yazma adımını transaction'a al; DataStore adımı zaten ayrı katman olduğu için CloseDayUseCase'deki gibi "bilinen sınırlama" olarak dokümante edilebilir |
| P1 | Export/Import, `UserPreferences.lightDayModeDate` alanını taşımıyor | `DataExportService.toJson()` ve `DataImportService.toUserPreferences()` bu alanı hiç yazmıyor/okumuyor; yedek alınıp geri yüklendiğinde Hafif Gün Modu durumu sessizce sıfırlanır (kritik değil ama veri kaybı) | `app/src/main/java/com/benimgunlerim/data/DataExportService.kt:173-199` (alan yok), `app/src/main/java/com/benimgunlerim/data/DataImportService.kt:194-220` (alan yok) — model tanımı: `app/src/main/java/com/benimgunlerim/data/UserPreferencesRepository.kt:51` | `toJson()`/`toUserPreferences()` fonksiyonlarına `lightDayModeDate` alanını ekle |
| P1 | Silme stratejisi tamamen hard-delete; kalıcı "trash/soft-delete" yok, undo sadece process ömrü boyunca bellekte tutuluyor | `deletedTasksById` bir `MutableMap` (ViewModel state), süreç öldürülürse (arka plana atma sonrası sistem tarafından kill, ekran döndürme sonrası config change hariç) veya kullanıcı ekrandan çıkarsa geri alma imkânı kalmıyor; `RoutineRepository.deleteByNames`, `TaskDao.deleteAll`, `CompletionLogDao.deleteAll` da tümüyle kalıcı | `app/src/main/java/com/benimgunlerim/ui/today/TodayViewModel.kt:441` (`deletedTasksById`), `app/src/main/java/com/benimgunlerim/data/TaskRepository.kt:205-208`, `RoutineRepository.kt:153-155` | Kritik silme işlemleri (rutin, çoklu görev) için DB seviyesinde `deletedAt` soft-delete alanı ve zamanlanmış temizlik (ör. 30 gün) değerlendirilmeli; en azından "Tüm verileri sil" (`LocalDataClearerImpl`) öncesi ikinci onay + son yedek önerisi eklenmeli |
| P2 | Kaynak ağacında ölü/artefakt dosya: `BenimGunlerimRepository_BACKUP.kt.bak` | Derlemeye girmiyor ama `data/` paketinde duruyor; CLAUDE.md'nin "Common.kt gibi 'her şey' dosyası oluşturma" ve genel temizlik ilkesiyle çelişiyor, yeni katılan geliştiriciyi yanıltabilir | `app/src/main/java/com/benimgunlerim/data/BenimGunlerimRepository_BACKUP.kt.bak:1` | Dosyayı sil (git geçmişinde zaten mevcut) |
| P2 | `AchievementDao.insertAll` `OnConflictStrategy.REPLACE` kullanıyor, sadece import senaryosunda çağrılıyor olsa da genel amaçlı bir repository metodu değil, DAO'nun kendisi tek kullanım yerine bağlı bir varsayım taşıyor | Yanlış bir çağrı yeri (ör. ileride "achievement seed" amaçlı kullanılırsa) zaten kilitli (`unlockedAt` dolu) başarımları sıfırlayabilir | `app/src/main/java/com/benimgunlerim/data/local/AchievementDao.kt:27-28` | DAO seviyesinde yorum ekleyerek "yalnızca tam veri geri yüklemede kullanılır" uyarısı bırak veya import'a özel ayrı metot adı ver |
| P2 | `DailyStateRepository.saveSummary` ve `AutoCloseMissedDayUseCase`/`SaveMissedDaySummaryUseCase` içinde `dailyScore = (completionRate * 100).toInt()` hesaplaması iki-üç yerde tekrarlanıyor | Tek noktadan değişmeyen bir iş kuralı; ileride yuvarlama/skor formülü değişirse bir yer unutulabilir | `app/src/main/java/com/benimgunlerim/data/DailyStateRepository.kt:54`, `app/src/main/java/com/benimgunlerim/domain/usecase/AutoCloseMissedDayUseCase.kt:58` | `ProgressCalculator`'a `scoreFromRate(rate): Int` yardımcı fonksiyonu ekleyip tüm çağrı yerlerini ona yönlendir |
| P2 | `RoutineEntity.bestStreak` alanı şemada var ama hiçbir DAO/repository metodu bu alanı güncellemiyor (grep'te `bestStreak` sadece export/import ve entity tanımında geçiyor, güncelleme yok) | Şemada "hazır" görünen bir alan aslında hiç yazılmıyor; UI bu alanı okursa her zaman 0 döner, yanıltıcı veri modeli | `app/src/main/java/com/benimgunlerim/data/local/entity/RoutineEntity.kt:29` | Ya `bestStreak`'i `ProgressSnapshot`'taki canlı hesaplamadan güncelleyen bir yazma yolu ekle, ya da alanı kaldırıp ölü şema genişlemesini önle |

## Dosya Bazlı Bulgular

### `app/src/main/java/com/benimgunlerim/data/local/Migrations.kt`
- Bulgu: Yalnızca `MIGRATION_6_7` tanımlı; dosyanın kendi doküman bloğu ("Her schema değişikliğinde... yeni bir MIGRATION_ nesnesi yazılır") v1-v5 için hiç uygulanmamış.
- Risk: Eski şemadaki bir cihaz güncellendiğinde Room migration bulamayıp crash olur; `fallbackToDestructiveMigration()` de yok, dolayısıyla "en azından veri silinerek devam eder" güvenlik ağı da yok — direkt açılmama riski.
- Öneri: Ya geçmiş migration'ları geriye dönük yaz (eğer gerçek kullanıcıda v1-6 şeması varsa), ya da proje henüz hiç yayınlanmadıysa bunu bilinçli olarak dokümante edip `app/schemas/` altına sadece güncel şemayı commit etmeye devam et — ama bu kararın PR/commit mesajında açık şekilde yazılması gerekir.

### `app/src/main/java/com/benimgunlerim/data/TaskRepository.kt`
- Bulgu: `delete(task)` satır 205-208, `taskDao.deleteById` + `completionLogDao.deleteForEntity` çağırıyor; alt görevler `SubTaskEntity` FK `ON DELETE CASCADE` ile DB motoru tarafından sessizce silinir, repository bunu snapshot'lamaz.
- Risk: `RestoreTaskUseCase` çağrıldığında görev geri gelir ama alt görevler geri gelmez — kullanıcı için sessiz veri kaybı.
- Öneri: `delete()` öncesi `subTaskDao.observeByTaskId`/yeni bir `getByTaskId` ile subtask listesini oku, silme sonrası `restore()` bu listeyi de yeniden yazsın.

### `app/src/main/java/com/benimgunlerim/ui/today/TodayViewModel.kt`
- Bulgu: `addTasksFromBrainDump` (satır 277-290) döngü içinde tek tek `addTaskUseCase` çağırıyor, transaction yok.
- Risk: Kısmi ekleme / tutarsız durum; ayrıca N ayrı DB yazımı performans açısından da `insertAll` toplu yazımına göre daha yavaş.
- Öneri: Toplu ekleme için repository'de transaction'lı bir `addTasksBulk(titles: List<String>, date, priority)` metodu ekle.

### `app/src/main/java/com/benimgunlerim/data/DataExportService.kt` ve `DataImportService.kt`
- Bulgu: `UserPreferences.toJson()`/`toUserPreferences()` `lightDayModeDate` alanını hiç işlemiyor (satır 173-199 / 194-220).
- Risk: Yedekten geri yüklemede Hafif Gün Modu durumu kaybolur — küçük ama tutarsız bir veri kaybı.
- Öneri: İki fonksiyona da alanı ekle; import validasyonuna ISO tarih formatı kontrolü de eklenmeli (`isIsoDate` zaten mevcut).

### `app/src/main/java/com/benimgunlerim/domain/usecase/ToggleRoutineUseCase.kt`
- Bulgu: `ToggleTaskUseCase`'in aksine `DatabaseTransactionRunner` enjekte edilmemiş; log yazma tek başına, prefs güncellemesi ayrı bir DataStore işlemi.
- Risk: Rutin tamamlama sırasında süreç kesilirse (crash/OOM) log yazılmış ama ödül verilmemiş ya da tam tersi bir tutarsız durum oluşabilir; küçük olasılık ama task tarafında zaten önlenmişken rutin tarafında önlenmemiş olması tutarsızlık.
- Öneri: Aynı transaction deseni rutin akışına da uygulanmalı.

## Kullanıcı Deneyimi Etkisi
- Streak, XP, level ve başarım hesapları güvenilir olduğu için kullanıcı "ilerlemem kayboldu" hissi yaşamayacak — bu güçlü bir taraf.
- Görev silme sonrası alt görevlerin sessizce kaybolması (P0) kullanıcı güvenini doğrudan zedeler: "geri al" dediği halde eksik veri görmesi, özellikle çok adımlı görevler kullanan kullanıcılar için can sıkıcı olur.
- Hafif Gün Modu'nun her gün otomatik ve doğru sıfırlanması (`lightDayModeDate == today` karşılaştırması, `TodayViewModel.kt:247`) iyi tasarlanmış; ekstra "gece yarısı sıfırlama" job'una gerek yok, timezone değişse bile cihazın yerel `LocalDate.now()`'ı referans alındığı için tutarlı kalır.
- Migration eksikliği (P0), gerçek kullanıcı tabanına yayıldıktan sonra bir güncellemede "uygulama açılmıyor / verilerim gitti" şikayetlerine yol açabilecek en yıkıcı senaryo.

## Teknik Borç Etkisi
- Genel mimari borç düşük: repository/DAO/usecase ayrımı temiz, entity'ler domain sızıntısı taşımıyor, enum tabanlı string sabitleri (`TaskCompletionState`, `CompletionStatus`, `CompletionEntityType`) DB'deki serbest string kullanımını disipline etmiş.
- Asıl borç, tutarsızlık borcu: bazı akışlar transaction kullanıyor (ToggleTaskUseCase, DeleteTaskUseCase, CarryPendingTasksUseCase, DataImportService, LocalDataClearerImpl) bazıları kullanmıyor (ToggleRoutineUseCase, Brain Dump toplu ekleme). Bu, "kural var ama her yerde uygulanmıyor" tipi bir borç ve yeni geliştirici kod tabanına bakınca hangi deseni izleyeceğini bilemez.
- `RoutineEntity.bestStreak` gibi hiç yazılmayan alanlar şema genişlemesini gereksiz büyütüyor; ileride "bu alan neden hep 0" sorusu zaman kaybettirecek.
- `.bak` dosyası gibi kalıntılar küçük ama projedeki "temiz kod tabanı" hedefiyle çelişiyor.

## Release / Monetizasyon Riski
- Bugüne kadar hiçbir ödeme/abonelik mekanizması bu dosyalarda görülmedi (Data/DB odaklı bu turda backend/ödeme entegrasyonu kanıtı yok — "bu alanda kanıt bulamadım").
- En büyük release riski migration eksikliği: eğer bu şema sürümleri (1-6) gerçekten hiç yayınlanmamışsa risk düşüktür; ama proje geçmişinde `version = 7`'ye kadar gelinmiş olması, önceki sürümlerin bir noktada test cihazlarında/iç dağıtımda kurulu olabileceğini düşündürür. Yayın öncesi bu netleştirilmeli.
- Veri kaybı senaryoları (subtask kaybı, light day export kaybı) kullanıcı güvenini zedeler ama doğrudan gelir riski taşımaz; yine de "premium/backup" gibi bir monetizasyon planı varsa (export/import'un olgunluğu buna işaret ediyor), backup'ın eksiksiz olması ürün vaadinin bir parçası olur — bu nedenle P1 olarak işaretlendi.

## Data Model Riskleri
| Model/Tablo/Ayar | Risk | Etki | Öneri |
|---|---|---|---|
| `AppDatabase` (version=7) + `Migrations.kt` | Sadece v6→v7 migration'ı var, `app/schemas/` altında tek `7.json` mevcut, destructive fallback yok | Eski şemalı cihazda güncelleme sonrası açılış crash'i / veri erişilemezliği | v1-v6 migration'larını geriye dönük ekle veya bilinçli olarak "hiç yayınlanmadı" kararını dokümante et; her yeni sürümde `schemas/*.json` commit disiplinini sürdür |
| `SubTaskEntity` (`ON DELETE CASCADE` → `tasks`) | `TaskRepository.delete()` cascade silmeyi snapshot'lamıyor, `RestoreTaskUseCase` subtask'ları geri getirmiyor | Görev "geri al" sonrası alt görevler kalıcı kayıp | Silme öncesi subtask snapshot alıp restore akışına dahil et |
| `UserPreferences.lightDayModeDate` (DataStore) | Export/Import bu alanı taşımıyor | Yedekten geri yüklemede Hafif Gün Modu durumu sessizce sıfırlanır | `DataExportService`/`DataImportService`'e alanı ekle |
| `RoutineEntity.bestStreak` | Hiçbir yazma yolu yok, her zaman entity default'u (0 veya import'tan gelen değer) kalır | Şemada var olan ama asla güncellenmeyen "ölü alan"; ileride yanlış veri gösterme riski | Alanı canlı streak hesaplamasından güncelleyen bir yazma yolu ekle ya da şemadan kaldır |
| `completion_logs` tablosu — Brain Dump toplu ekleme yolu | Toplu görev ekleme transaction dışı, tek tek insert | Kısmi ekleme durumunda tutarsız görev listesi | `DatabaseTransactionRunner` + `TaskDao.insertAll` kullanan toplu ekleme metodu |
| `routines`/`tasks`/`completion_logs` hard-delete stratejisi | Soft-delete/trash yok; undo sadece bellekte (`deletedTasksById`), process ölünce kaybolur | Yanlışlıkla silinen veri (özellikle "Tüm verileri sil" ve rutin arşivleme dışı silme) kalıcı kaybolabilir | Kritik silme yollarına DB seviyeli `deletedAt` + zamanlanmış temizlik eklenmesi değerlendirilmeli |
| `ToggleRoutineUseCase` yazma akışı | Log yazımı + ödül akışı transaction'sız (task tarafıyla tutarsız) | Nadir ama olası ara-kesilme senaryosunda log/ödül tutarsızlığı | `DatabaseTransactionRunner` ekle, `ToggleTaskUseCase` deseniyle hizala |

## Önceliklendirilmiş Yapılacaklar

### P0 — Yayın öncesi şart
- Room migration geçmişini tamamla (v1→v6 migration'ları ekle veya "hiç yayınlanmadı" kararını netleştirip dokümante et); `fallbackToDestructiveMigration()` kullanmadan güvenli bir yol seç.
- Görev silme/geri alma akışına subtask snapshot + restore desteği ekle.

### P1 — Kısa vadede gerekli
- Brain Dump toplu görev eklemeyi `DatabaseTransactionRunner` + `insertAll` ile batch'e çevir.
- `ToggleRoutineUseCase`'i `ToggleTaskUseCase` ile aynı transaction desenine getir.
- Export/Import'a `lightDayModeDate` alanını ekle.
- Kritik silme akışları için soft-delete/trash stratejisini değerlendir (en azından "Tüm verileri sil" için ikinci onay + otomatik yedek öner).

### P2 — Polish / ileri iyileştirme
- `BenimGunlerimRepository_BACKUP.kt.bak` dosyasını sil.
- `dailyScore` hesaplamasını `ProgressCalculator`'da tek noktaya topla.
- `RoutineEntity.bestStreak` alanını ya gerçekten besle ya da şemadan kaldır.
- `AchievementDao.insertAll`'ın yalnızca import senaryosuna özel olduğunu belgeleyen bir yorum/isimlendirme ekle.

## 1 Haftalık Düzeltme Planı
- Gün 1-2: Migration geçmişini netleştir — ya eksik migration'ları yaz ya da mevcut şema durumu için resmi bir "temiz başlangıç" kararı al ve `Migrations.kt`/`AppModule.kt`'ye yorum olarak işle; `app/schemas/` klasörünü güncel tut.
- Gün 3: `TaskRepository.delete`/`restore` akışına subtask snapshot ekle, `DeleteTaskUseCase`/`RestoreTaskUseCase` testlerini güncelle.
- Gün 4: Brain Dump toplu ekleme için transaction'lı batch metodu yaz, `TodayViewModel.addTasksFromBrainDump`'ı ona yönlendir.
- Gün 5: `ToggleRoutineUseCase`'e `DatabaseTransactionRunner` ekle; export/import'a `lightDayModeDate` alanını ekle.

## 2 Haftalık Düzeltme Planı
- 1. hafta: Yukarıdaki P0/P1 kalemlerinin tamamı + ilgili unit testler (migration testi için Room'un `MigrationTestHelper`'ı kullanılmalı).
- 2. hafta: P2 kalemlerinin temizliği (`.bak` dosyası, `bestStreak` alanı kararı, `dailyScore` merkezi hesaplama); ardından kritik silme akışları için soft-delete/trash yaklaşımının kapsamını (hangi entity'ler, ne kadar saklama süresi) netleştirip bir sonraki sprint'e taşı.

## Final Karar
Beta'ya devam edilebilir; ancak gerçek kullanıcıya açık bir yayın (Play Store production track) öncesi P0 kalemleri (migration geçmişi + subtask geri alma kaybı) mutlaka kapatılmalı. Data katmanının temel mimarisi sağlam olduğundan büyük bir refactor gerekmiyor — bu bir "beklet" değil, hedefli "düzeltme" kararıdır.
