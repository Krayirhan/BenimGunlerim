# Audit Raporu — Backend / Sync Hazırlığı

> ⚠️ **2026-08-17 güncellemesi:** Export/import UI'a bağlandı. Ayrıca bu raporun atıfta bulunduğu Firebase Crashlytics **tamamen kaldırıldı** — `INTERNET` izninin artık hiçbir teknik gerekçesi kalmadı (bkz. incremental rapordaki "Yeni Bulunan Regresyonlar"). Güncel durum için bkz. [`INCREMENTAL_REAUDIT_2026-08-17.md`](INCREMENTAL_REAUDIT_2026-08-17.md). Bu doküman tarihsel kayıt olarak değiştirilmemiştir.

## Genel Puan
7 / 10

## Kısa Karar
Uygulamanın şu anda hiçbir backend/network katmanı yok ve bu, mevcut ürün aşaması (tek kullanıcı, tek cihaz, ücretsiz sanal ekonomi) için doğru ve bilinçli bir mimari tercih — ceza puanı gerektirmez. Data modeli (String/UUID birincil anahtarlar, `createdAt`/`updatedAt` epoch millis, ISO-8601 `LocalDate` string tarihler) ileride bir sync katmanı eklenmesine hazır bir taban sunuyor; ancak `userId`/`deviceId` alanı hiçbir tabloda yok, bazı tablolarda (`CompletionLogEntity`, `SubTaskEntity`, `AchievementEntity`, `DailyStateEntity`) `updatedAt` eksik ve bu, "son yazan kazanır" tipi bir conflict-resolution stratejisini bugünden zorlaştırıyor. Export/import kod tarafı olgun ama Settings ekranına hâlâ bağlı değil — bu, sync'ten önce çözülmesi gereken en ucuz ve en yüksek getirili adım. Premium/entitlement için `UserPreferencesRepository`'de hazır bir alan yok ama DataStore modeli buna kolayca genişletilebilir. Karar: **local-first mimariyi koru, backend eklemeyi şimdi erteleme; ama export/import'u UI'a bağla ve entity'lere `updatedAt`/`deletedAt` ekleme kararını netleştir.**

## En Güçlü 5 Taraf
1. Tüm birincil anahtarlar `String` (`@PrimaryKey val id: String`) ve UUID tabanlı üretiliyor — `app/src/main/java/com/benimgunlerim/data/TaskRepository.kt` ve `RoutineRepository.kt` içinde `UUID` kullanımı doğrulandı (`grep -rn UUID` üç dosyada eşleşti: `TaskRepository.kt`, `RoutineRepository.kt`, `notifications/NotificationIds.kt`). Auto-increment `Int` ID kullanılmamış olması, ileride sunucu tarafı bir ID çakışmasına düşmeden client-side ID üretimine devam edebilmeyi sağlıyor — sync mimarilerinde (özellikle offline-first / CRDT benzeri yaklaşımlarda) bu, sonradan eklemesi en pahalı olan tasarım kararlarından biridir ve burada baştan doğru yapılmış.
2. Tarih/zaman modeli tutarlı ve taşınabilir: `TaskEntity.plannedDate`, `CompletionLogEntity.date`, `DailyStateEntity.date` hepsi ISO-8601 `LocalDate` string (`yyyy-MM-dd`), `createdAt`/`updatedAt`/`completedAt` alanları ise `Long` (epoch millis) — `app/src/main/java/com/benimgunlerim/data/local/entity/TaskEntity.kt:26-27`, `RoutineEntity.kt:21-22`. Bu format hem JSON serileştirmede hem sunucu tarafı bir REST/GraphQL şemasında dönüşümsüz kullanılabilir; timezone karmaşası riski düşük.
3. Export altyapısı zaten JSON tabanlı, versiyonlanmış ve DAO seviyesinde tam kapsamlı: `DataExportService.kt:40` (`EXPORT_VERSION = 1`) ve `exportToJson()` (satır 44-59) tasks/subTasks/routines/completionLogs/dailyStates/achievements/preferences'ın tamamını tek JSON'a yazıyor. Bu, gelecekte bir "sunucuya yükle" akışının payload şemasını neredeyse hazır veriyor — sıfırdan API sözleşmesi tasarlamak gerekmeyecek.
4. Android Auto Backup bilinçli şekilde yapılandırılmış ve dokümante edilmiş: `app/src/main/res/xml/data_extraction_rules.xml` hem `cloud-backup` hem `device-transfer` bloklarında `datastore/` ve `benim_gunlerim.db`'yi dahil ediyor, dosyanın kendi yorumunda "Keep the privacy policy and release checklist aligned with this behavior" notu var — bu, gerçek bir backend olmadan da kullanıcının Google hesabı üzerinden cihaz değiştirdiğinde verisini kaybetmemesini sağlayan, sıfır ek kod gerektiren düşük maliyetli bir yedekleme katmanı ve bilinçli bir ürün kararı olarak işaretlenmiş.
5. Sanal ekonomi (XP/gold/shop) ve reward sistemi tamamen yerel ve idempotent: `RewardGrantService.grantOnce` (`app/src/main/java/com/benimgunlerim/domain/service/RewardGrantService.kt:51-73`) her ödülü `eventKey` ile tekilleştiriyor. Bu desen ileride sync eklenirse (aynı ödülün iki cihazdan çift işlenmesi riski) doğrudan yeniden kullanılabilir bir "idempotency key" deseni — sync'e geçişte sıfırdan tasarlanması gereken en zor problemlerden birinin temeli zaten atılmış.

## En Kritik 10 Sorun
| Öncelik | Sorun | Etki | Kanıt/Dosya | Öneri |
|---|---|---|---|---|
| P0 | Export/import kodu tam bitmiş ama Settings ekranına hâlâ bağlı değil | Backend olmadığı için export/import şu an tek "yedekleme/cihaz değiştirme" garantisi; UI'a bağlı olmadığı sürece kullanıcı `benim_gunlerim.db` dosyasını Android Auto Backup dışında hiçbir şekilde taşıyamaz/yedekleyemez, "verim güvende mi" güven sorusuna cevap veremiyoruz | `app/src/main/java/com/benimgunlerim/ui/settings/SettingsScreen.kt` (bu turda tam okundu, dosyada `export`/`import` string'i hiç geçmiyor — `grep -ni "export\|import"` sıfır sonuç); `SettingsViewModel.kt` içinde `exportDataToFile`/`importDataFromFileContent` zaten mevcut (09 raporunda doğrulanmış) | Export/import butonlarını Ayarlar'a bağla; bu, sync'e göre çok daha ucuz ve backend gerektirmeyen bir "kullanıcı güveni" yatırımı |
| P0 | Hiçbir tabloda `userId`/`accountId`/`deviceId` alanı yok | İleride hesap eklenirse (çoklu kullanıcı aynı cihaz ya da sunucu tarafı veri ayrımı), mevcut şemaya bu alanı eklemek Room migration + tüm DAO sorgularının `WHERE userId = ?` ile güncellenmesini gerektirir — küçük ama şemaya dokunan, tüm sorgu katmanını etkileyen bir değişiklik | `app/src/main/java/com/benimgunlerim/data/local/entity/TaskEntity.kt`, `RoutineEntity.kt`, `CompletionLogEntity.kt`, `DailyStateEntity.kt`, `AchievementEntity.kt`, `SubTaskEntity.kt` — altı entity de grep edildi, hiçbirinde `userId` alanı yok | Hesap/sync kararı netleşmeden şema değişikliği gerekmiyor (bilinçli erteleme kabul edilebilir), ama karar anında migration'ın kapsamı (6 tablo + DAO sorguları) baştan planlanmalı |
| P1 | `CompletionLogEntity`, `SubTaskEntity`, `AchievementEntity`, `DailyStateEntity`'de `updatedAt` alanı yok | Sync eklenirse "son değişiklik ne zaman oldu" bilgisi olmadan last-write-wins tipi basit bir conflict resolution bile uygulanamaz; `TaskEntity`/`RoutineEntity`'de bu alan var ama diğer dört tabloda yok — tutarsız kapsam | `app/src/main/java/com/benimgunlerim/data/local/entity/CompletionLogEntity.kt:15-26`, `SubTaskEntity.kt:20-27`, `AchievementEntity.kt:7-10`, `DailyStateEntity.kt:7-20` (hiçbirinde `updatedAt` yok) | Sync kararı verilmeden bu alanları eklemek zorunlu değil, ama eklenecekse Room migration'ı (zaten P0 olarak 04 raporunda eksik bulunan migration zincirine ek yük) gerektireceği not edilmeli |
| P1 | Premium/entitlement için `UserPreferencesRepository`'de hiçbir alan/arayüz yok | `data class UserPreferences` (satır 18-52) satır satır incelendi — `premium`, `entitlement`, `subscriptionState`, `purchaseToken` gibi hiçbir alan yok; 09 raporundaki "Billing kütüphanesi yok" bulgusuyla tutarlı, entitlement'ın nerede tutulacağı sorusunun cevabı da bugün için "hiçbir yerde" | `app/src/main/java/com/benimgunlerim/data/UserPreferencesRepository.kt:18-52` | Entitlement state'i DataStore'a (`isPremium: Boolean`, `entitlementSource: String`, `purchaseVerifiedAt: Long`) eklemek düşük maliyetli; ama sunucu tarafı doğrulama olmadan istemci-taraflı bu alan sahteciliğe açık olur (09 raporunda da işaretli) |
| P1 | `RoutineEntity.bestStreak` gibi hiç yazılmayan alanlar zaten var (04 raporunda P2); benzer şekilde şemada "sync-ready" görünüp aslında kullanılmayan alan riski taşıyor | Şemaya erken "gelecek için" alan eklemek, gerçek ihtiyaç netleşmeden yapılırsa hem ölü kod hem yanlış güven yaratır — bu rapor açısından not: sync alanları da (varsa) aynı hataya düşmemeli, önce ihtiyaç netleşmeli | `app/src/main/java/com/benimgunlerim/data/local/entity/RoutineEntity.kt:29` (çapraz referans: 04 raporu, satır 28) | Sync şeması tasarlanırken "şimdiden ekle" değil "karar anında ekle" disiplini sürdürülmeli |
| P1 | Room migration geçmişi eksik (04 raporunda P0 olarak işaretli) — bu, backend/sync şeması tasarımını da riske atıyor | Sync için şema değişikliği (userId, updatedAt, deletedAt eklemek) yeni bir migration gerektirecek; migration disiplini bugün zaten kırık olduğundan (`Migrations.kt`'de yalnızca `MIGRATION_6_7`), sync şeması eklenirken de aynı riskin tekrarlanma ihtimali yüksek | `app/src/main/java/com/benimgunlerim/data/local/Migrations.kt:17-23` (çapraz referans: 04 raporu, satır 19) | Sync şeması tasarlanmadan önce migration disiplinini düzeltmek ön koşul olmalı — aksi halde sync migration'ı da eksik/riskli eklenir |
| P2 | Hard-delete stratejisi (soft-delete/`deletedAt` yok) sync ile doğrudan çelişecek | Sync senaryosunda bir cihazda silinen kaydın diğer cihaza "silindi" olarak yayılabilmesi için tombstone/`deletedAt` alanı gerekir; şu anki `taskDao.deleteById` gibi hard-delete çağrıları (04 raporunda da not edilmiş) bu mekanizmaya sahip değil | `app/src/main/java/com/benimgunlerim/data/TaskRepository.kt:205-208`, `RoutineRepository.kt:153-155` (çapraz referans: 04 raporu, satır 24) | Sync kararı netleşirse silme akışlarını soft-delete + tombstone senkronizasyonuna çevirmek gerekecek; bugün için zorunlu değil |
| P2 | `INTERNET` izni manifestte tanımlı ama uygulamanın kendi network çağrısı yok | `AndroidManifest.xml:2`'de `android.permission.INTERNET` var; `app/build.gradle.kts` bağımlılıklarında (satır 223-264) Retrofit/Ktor/OkHttp yok, tek network-bağımlı kütüphane koşullu eklenen Firebase Crashlytics (`app/build.gradle.kts:16-19`, yalnızca `google-services.json` varsa aktif ve repoda bu dosya yok) — izin muhtemelen Crashlytics/Firebase SDK'sının transitive gereksinimi | `app/src/main/AndroidManifest.xml:2`, `app/build.gradle.kts:16-19,249-251` | Kritik değil, ama Play Store Data Safety formu doldurulurken "bu izin neden var" sorusuna Crashlytics/Firebase referansıyla cevap verilebilmeli; kod tabanında gerçek bir API çağrısı olmadığı teyit edildi |
| P2 | Export payload'ı sync/conflict açısından "tam senkron" değil, tek yönlü "tam yedek/geri yükleme" formatında | `DataExportService.exportToJson()` (satır 44-59) delta/incremental değil, DB'nin tamamını her seferinde dump ediyor; bu backup için doğru ama iki cihaz arası merge senaryosu için (aynı anda iki cihazda değişiklik) uygun değil | `app/src/main/java/com/benimgunlerim/data/DataExportService.kt:44-59` | Bugün için sorun değil (export/import zaten "yedekten geri yükle" amaçlı); gerçek sync eklenirse bu format yerine delta-tabanlı bir protokol gerekecek, export formatı yeniden kullanılmamalı |
| P2 | `DataImportService` import doğrulaması güçlü ama "hangi cihazdan geldiği" bilgisini taşımıyor | Çoklu cihaz senaryosunda "bu import hangi cihazın son durumu" ayrımı yapılamıyor — tek cihaz/tek kullanıcı modelinde sorun değil ama gelecekte "iki cihazdan import" senaryosu için eksik | 04 raporunda doğrulanan `DataImportService.validateImportData` (satır 298-416) — dosya bu turda export tarafı üzerinden çapraz kontrol edildi, import payload'ında `deviceId` alanı yok | Bugün için zorunlu değil; sync kararı netleşirse export/import formatına `sourceDeviceId` eklenmesi düşünülebilir |

## Dosya Bazlı Bulgular

### `app/build.gradle.kts`
- Bulgu: `dependencies` bloğu (satır 223-264) tam okundu — Retrofit, Ktor, OkHttp, Supabase, Firebase Auth/Firestore gibi hiçbir network/backend kütüphanesi yok. Tek network-adjacent bağımlılık koşullu eklenen `firebase-crashlytics` (satır 249-251), o da yalnızca `google-services.json` varsa aktif oluyor (satır 16-19) ve bu dosya repoda yok.
- Risk: Yok — bu bir bulgu değil, doğrulama. Backend'in gerçekten var olmadığı teyit edildi.
- Öneri: Sync kararı verilene kadar bu haliyle bırakılmalı; erken backend eklemek şu anki ürün aşamasında gereksiz mimari şişirme olurdu.

### `app/src/main/AndroidManifest.xml`
- Bulgu: `INTERNET` izni tanımlı (satır 2) ama kod tabanında hiçbir HTTP çağrısı yok; izin muhtemelen Crashlytics/Firebase SDK transitive manifest merge'inden geliyor.
- Risk: Düşük — Play Store incelemesinde "bu izin ne için" sorusuna makul bir cevap var (crash reporting), ama bu net dokümante edilmemiş.
- Öneri: Privacy Policy/Data Safety formunda bu iznin Crashlytics'e bağlı olduğu açıkça belirtilmeli.

### `app/src/main/java/com/benimgunlerim/data/UserPreferencesRepository.kt`
- Bulgu: `data class UserPreferences` (satır 18-52) tamamen okundu; oyunlaştırma durumu (`totalXp`, `gold`, `happiness`, `ownedItems`), bildirim tercihleri, tema, analytics toggle içeriyor — ama premium/entitlement/subscription alanı yok.
- Risk: İleride IAP eklenirse entitlement state'inin nereye yazılacağı belirsiz; DataStore genel olarak buna uygun (basit key-value, zaten `gold`/`ownedItems` gibi ekonomik state burada tutuluyor) ama bugün hiçbir iskelet yok.
- Öneri: Entitlement kararı netleştiğinde bu dosyaya `isPremium`, `entitlementSource`, `purchaseVerifiedAt`, `lastEntitlementSyncAt` gibi alanlar eklenebilir; DataStore'un mevcut `Keys` object deseni (satır 84 sonrası) buna kolayca genişletilebilir yapıda.

### `app/src/main/res/xml/data_extraction_rules.xml`
- Bulgu: Hem `cloud-backup` hem `device-transfer` blokları `datastore/` ve `benim_gunlerim.db`'yi kapsıyor; dosyanın kendi yorum bloğu bunun bilinçli bir ürün kararı olduğunu belirtiyor.
- Risk: Düşük — bu aslında bir güçlü yön; ancak Android Auto Backup'ın kendi sınırları var (yalnızca aynı Google hesabına bağlı Android cihazlar arası, iOS/web'e taşınmaz, kullanıcı Google hesabı yedeklemesini kapatmışsa çalışmaz).
- Öneri: Bu mekanizmanın kapsamı (yalnızca Android-to-Android, Google hesabı bağımlı) Privacy Policy'de netleştirilmeli; "tam bir yedekleme çözümü" olarak pazarlanmamalı, export/import'un tamamlayıcısı olarak konumlandırılmalı.

### `app/src/main/java/com/benimgunlerim/data/local/entity/*.kt` (6 entity dosyası)
- Bulgu: Tüm ID'ler `String` (UUID), tarihler ISO-8601 string veya epoch `Long`; ancak `userId`/`deviceId` hiçbir yerde yok, `updatedAt` yalnızca `TaskEntity` ve `RoutineEntity`'de var.
- Risk: Sync eklenmeden bugün risk yok; sync eklenirse bu 6 dosyanın tamamına dokunan bir migration gerekecek.
- Öneri: Sync kararı netleşmeden şema değişikliği önerilmiyor — ama karar anında "6 entity + tüm DAO sorguları" kapsamının büyük olacağı baştan bilinmeli.

## Kullanıcı Deneyimi Etkisi
- Kullanıcı bugün tek cihazlı ve hesapsız kullanıyor; bu basit ve sürtünmesiz bir onboarding deneyimi sağlıyor (hesap oluşturma zorunluluğu yok) — local-first yaklaşımın en somut kullanıcı faydası bu.
- Ancak kullanıcı telefonunu değiştirdiğinde ya da uygulamayı silip yeniden kurduğunda, export/import UI'a bağlı olmadığı için verisini taşımanın tek yolu Android Auto Backup'a (aynı Google hesabı, aynı platform) bağımlı kalıyor — bu, kullanıcıya hiç anlatılmayan örtük bir güvenlik ağı; kullanıcı "yedeğim var mı" diye sorduğunda Ayarlar'da hiçbir cevap bulamıyor.
- Sakinleşme/Brain Dump gibi kişisel içerik üreten özellikler düşünüldüğünde, "verim nerede, nasıl taşınır" sorusuna Ayarlar ekranından cevap verilememesi, gizlilik bilinci yüksek bir kullanıcı kitlesinde güven kaybına yol açabilir.

## Teknik Borç Etkisi
- Şema borcu düşük-orta: ID/tarih modelleme kararları doğru atılmış, sync'e geçişte bu ikisi yeniden yazılmaz. Asıl borç, "sync'e hazır ama yarım" alanlar: `updatedAt`'ın yalnızca 2/6 tabloda olması, `userId`'nin hiçbir yerde olmaması — bunlar bugün borç değil (ihtiyaç yok), ama sync kararı verildiği an aynı anda hem şema hem migration disiplini borcu (04 raporundaki P0) birlikte ödenmesi gereken bir noktaya birikmiş durumda.
- Export/import "bitmiş ama teslim edilmemiş" borcu (09 raporuyla örtüşüyor) bu rapor açısından da geçerli: kod hazır, entegrasyon eksik — bu en düşük maliyetli, en yüksek getirili kapanabilir borç kalemi.
- Entitlement/premium için hiçbir iskelet olmaması "borç" değil "henüz başlanmamış iş" — planlama gerektiriyor ama şu an acil değil.

## Release / Monetizasyon Riski
- Backend olmaması bugünkü ücretsiz/local-only sürüm için release riski taşımıyor — Play Store bir uygulamanın backend'siz olmasını cezalandırmaz.
- Asıl risk, export/import'un UI'a bağlı olmaması: kullanıcı veri taşınabilirliği beklentisi (KVKK/GDPR ruhu) karşılanmıyor ve bu, 09 raporunda zaten P0 olarak işaretli — bu rapor da aynı sonuca farklı bir açıdan (sync/backup hazırlığı) varıyor.
- Premium/IAP eklenmesi durumunda mevcut mimari (yerel DataStore + Room, hiçbir sunucu tarafı doğrulama) entitlement'ı yalnızca istemci tarafında tutmak zorunda kalır — bu, "gold satın al" gibi bir gerçek para köprüsü kurulursa sahtecilik riski taşır (09 raporuyla tutarlı). Basit "kozmetik premium tema/rozet" gibi düşük riskli bir premium modelde ise bugünkü mimari (DataStore + Google Play Billing'in kendi purchase token doğrulaması) yeterli olabilir; sunucu tarafı receipt validation olmadan yüksek değerli bir IAP modeline geçilmemeli.

## Backend Readiness
| Alan | Hazır mı? | Risk | Öneri |
|---|---|---|---|
| Backend/API varlığı | Yok (bilinçli) — `app/build.gradle.kts` içinde network kütüphanesi yok, `INTERNET` izni yalnızca Crashlytics transitive gereksinimi | Düşük — mevcut ürün aşaması için doğru karar | Kullanıcı hesabı/çoklu cihaz talebi netleşmeden backend eklenmemeli |
| Local-first mimari bilinci | Evet — Room + DataStore, tüm state cihazda; `data_extraction_rules.xml` bilinçli yorumla dokümante edilmiş | Düşük | Mevcut yaklaşım korunmalı |
| ID/anahtar modeli sync uyumluluğu | Kısmen hazır — tüm ID'ler UUID string (`TaskRepository.kt`, `RoutineRepository.kt`), auto-increment yok | Düşük | Ek işlem gerekmiyor, sync'e hazır bir temel |
| Zaman damgası modeli | Kısmen hazır — `TaskEntity`/`RoutineEntity`'de `updatedAt` var, `CompletionLogEntity`/`SubTaskEntity`/`AchievementEntity`/`DailyStateEntity`'de yok | Orta | Sync kararı netleşirse eksik 4 tabloya `updatedAt` eklenmeli (migration ile) |
| Kullanıcı/hesap scoping | Yok — hiçbir entity'de `userId`/`deviceId` alanı yok | Orta (yalnızca hesap eklenirse yüksek) | Hesap eklenmeden şema değişikliği gerekmiyor; karar anında 6 entity + DAO katmanını kapsayan bir migration planlanmalı |
| Conflict resolution stratejisi | Tanımlı değil (backend yok) | Orta — ileride sync eklenirse kritik | `updatedAt` + basit last-write-wins ile başlanabilir; `RewardGrantService`'teki idempotency-key deseni (eventKey) referans alınabilir |
| Export/Import (backup alternatifi) | Kod hazır, UI'a bağlı değil — `DataExportService.kt`, `DataImportService.kt` tam, `SettingsScreen.kt`'de hiç buton yok | Yüksek (kullanıcı güveni açısından) | P0 — export/import butonlarını Ayarlar'a bağla; bu, sync'ten çok daha ucuz ve acil |
| Android Auto Backup (örtük yedek) | Var ve bilinçli yapılandırılmış — `data_extraction_rules.xml` | Düşük | Kapsamının (yalnızca Android-Android, Google hesabı bağımlı) kullanıcıya/Privacy Policy'de netleştirilmesi önerilir |
| Premium entitlement modeli | Yok — `UserPreferences`'ta alan yok, Billing kütüphanesi yok (09 raporuyla tutarlı) | Orta-Yüksek (yalnızca IAP kararı verilirse) | Entitlement state'i DataStore'a eklenebilir; yüksek değerli IAP için sunucu tarafı doğrulama şart |
| Multi-device kullanım | Desteklenmiyor (backend yok); Android Auto Backup yalnızca "cihaz değiştirme" senaryosunu kısmen kapsıyor, eşzamanlı çoklu cihaz kullanımını kapsamıyor | Düşük (bugünkü tek-cihaz ürün modeli için) | Çoklu cihaz eşzamanlı kullanım talebi gelmeden yatırım yapılmamalı |
| Şema versiyonlama (export formatı) | Var — `DataExportService.EXPORT_VERSION = 1` | Düşük | Format değiştikçe versiyon artırılmaya devam edilmeli; import tarafında versiyon bazlı migration mantığı gerekebilir |
| Room şema migration disiplini | Zayıf (04 raporunda P0) — sync şeması eklenirse bu zayıflık büyür | Yüksek | Sync şeması tasarlanmadan önce migration disiplini düzeltilmeli (04 raporuyla ortak öncelik) |

## Önceliklendirilmiş Yapılacaklar

### P0 — Yayın öncesi şart
- Export/import butonlarını `SettingsScreen.kt`'ye bağla (09 raporuyla ortak madde) — backend olmadan tek gerçek "veri taşınabilirliği" garantisi bu.
- `userId`/sync şeması eklenmeden önce mevcut Room migration disiplinini (04 raporu P0) düzelt — sync şeması bu zemin üzerine kurulacak.

### P1 — Kısa vadede gerekli
- Entitlement/premium kararı netleşmeden önce `UserPreferencesRepository`'ye nereye ekleneceğini planlayan kısa bir teknik not yaz (kod değil, karar dokümanı).
- Android Auto Backup'ın kapsam sınırlarını (yalnızca Android-Android, Google hesabı bağımlı) Privacy Policy taslağına ekle.
- `updatedAt` alanının hangi tablolara ekleneceğine (sync ihtiyacı netleşince) karar ver; bugünden şemaya eklemeye gerek yok.

### P2 — Polish / ileri iyileştirme
- Sync kararı verilirse hard-delete → soft-delete (`deletedAt` + tombstone) geçişini planla.
- Export formatına ileride `sourceDeviceId` eklenmesini değerlendir (yalnızca çoklu cihaz importu senaryosu gerçek ihtiyaç olursa).
- `INTERNET` izninin Crashlytics'e bağlı olduğunu Data Safety formunda ve/veya kod yorumunda netleştir.

## 1 Haftalık Düzeltme Planı
- Gün 1-2: Export/import UI entegrasyonu (09 raporundaki P0 ile aynı iş, bu rapor da aynı önceliği veriyor).
- Gün 3-4: Room migration geçmişini netleştir (04 raporu P0) — sync şeması için ön koşul.
- Gün 5: Android Auto Backup kapsamını Privacy Policy taslağına not olarak ekle; entitlement yerleşimi için kısa bir teknik karar dokümanı taslağı yaz (kod yazılmaz).

## 2 Haftalık Düzeltme Planı
- 1. hafta: Yukarıdaki P0/P1 kalemleri.
- 2. hafta: Sync/hesap ihtiyacı gerçekten netleşirse (ürün kararı olarak), `updatedAt` eksik 4 tabloya eklenmesi + `userId` şema tasarımı için ayrı bir teknik tasarım dokümanı yazılması (bu turda kod yazılmadı, yalnızca plan önerilir); IAP kararı netleşirse entitlement alanlarının DataStore'a eklenmesi için ayrı bir teknik tasarım dokümanı.

## Final Karar
Backend'in bugün var olmaması bir eksiklik değil, bu ürün aşaması için doğru bir mimari tercih — puan bu nedenle kırılmadı. Asıl zayıf halka, backend'siz de yapılabilecek en ucuz güven yatırımının (export/import UI entegrasyonu) hâlâ yapılmamış olması ve şemanın sync'e "kısmen" hazır olması (ID/tarih modeli iyi, ama `userId`/tutarlı `updatedAt` yok). Karar: **local-first mimariyi koru, backend eklemeyi erteleme (beklet); ama export/import'u UI'a bağlamayı ve migration disiplinini düzeltmeyi bu sprint içinde tamamla (düzeltme).** Sync/hesap/IAP kararları ürün tarafında netleşmeden şemaya yeni alan eklenmemeli — bu, CLAUDE.md'nin "gerekmeden karmaşıklık ekleme" ruhuyla da uyumlu.
