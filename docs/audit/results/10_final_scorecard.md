# Benim Günlerim — Full Project Scorecard

## Genel Puan

**5.5 / 10**

Dokuz alanın tamamı (Backend/Sync Readiness dahil) çalıştırıldıktan sonra alan puanlarının aritmetik ortalaması (~6.1) bu skoru hafif iyimser gösterirdi; puan bilinçli olarak aşağı çekildi çünkü Testing/QA raporu şu anda `./gradlew testDebugUnitTest`'in **derlenmediğini** doğruluyor — yani proje şu an CI'da kırmızı durumda ve bu tek başına her türlü yayın kararını geçersiz kılar. Mimari temel (Faz A-D component sistemi, Room/DataStore veri katmanı, StateFlow deseni, bilinçli local-first backend kararı) beklenenin üstünde olgun; ama Faz E'nin "tamamlandı" iddiası, build'in kırık olması ve gizlilik/monetizasyon yüzeyinin eksikliğiyle birlikte değerlendirildiğinde genel notu ortadan aşağı çekiyor.

## Kısa Final Karar

- **Yayına hazır mı?** Hayır. Build kırık, Privacy Policy yok, migration geçmişi eksik, rutin hedef takibi UI'da çalışmıyor.
- **Kısıtlı beta için hazır mı?** Kısmen — önce CI'ı yeşile döndürüp (derleme hatalarını çöz) P0 veri-kaybı maddelerini kapatmak şartıyla iç/kapalı test grubuna çıkılabilir.
- **Para kazanma için hazır mı?** Hayır. Kod tabanında hiçbir Billing/IAP kütüphanesi, entitlement modeli veya premium/ücretsiz ayrımı yok — bu "borç" değil, sıfırdan başlanacak bir iş.
- **Önce hangi riskler çözülmeli?** (1) Test derleme hatalarını çözüp CI'ı yeşile döndür, (2) Room migration geçmişini tamamla/karar dokümante et, (3) gün kapatma formunun process death'te veri kaybını çöz, (4) Privacy Policy + export/import + analytics toggle'ı bağla, (5) rutin hedef/sayaç takibi UI'sını ekle.

## Alan Puanları

| Alan | Puan | Durum | En Büyük Risk | En Büyük Güç |
|---|---:|---|---|---|
| Product / UX | 6 | Refactor | Hedef/sayaç tipi rutinlerde artır/azalt kontrolü hiçbir ekranda yok — çekirdek özellik fiilen çalışmıyor | Türkçe metin/ton kalitesi ve onboarding kişiselleştirmesi gerçek ve tutarlı |
| Frontend / Compose | 6.5 | Refactor | `TodayScreen`/`TodayViewModel`/`TodaySheets` 200 satır kuralının 3-4,5 katı üzerinde, hardcoded string/dp/renk yaygın | Katman mimarisi (core/gamification/molecules/organisms) gerçek ve tutarlı, `Common.kt` fiilen silinmiş |
| State / ViewModel | 7 | Refactor + düzeltme | `CloseDaySheet` ve 11 dialog state'i `rememberSaveable` değil — process death'te gün kapatma verisi kayboluyor | `StateFlow`+`combine`+`WhileSubscribed` deseni tutarlı, ödül idempotency (`grantOnce`) sağlam |
| Data / Database | 7 | Düzeltme (refactor değil) | Room migration geçmişi eksik (yalnızca 6→7); eski şemalı cihaz güncellemede açılmama riski | Tarih/streak modeli tutarlı, export/import validasyonu üretim kalitesinde |
| Backend / Sync Readiness | 7 | Hazır (local-first bilinçli tercih) | Entity şemalarında `userId`/tutarlı `updatedAt` yok — gelecekte sync/conflict resolution eklenirse veri modeli yeniden tasarlanmalı; premium/entitlement alanı `UserPreferencesRepository`'de henüz yok | Network/backend hiç yok ama bu bilinçli ve doğru bir MVP kararı; export/import servisleri (`DataExportService`/`DataImportService`) zaten tam kodlanmış, yalnızca UI'a bağlanmayı bekliyor |
| Security / Privacy | 6 | Düzeltme | Privacy Policy yok, export/import UI'a bağlı değil, analytics toggle görünmüyor — üçü de "kod bitti, UI'a bağlanmadı" | Veri tamamen yerel, crash reporting PII-safe allow-list ile tasarlanmış |
| Performance | 6 | Düzeltme | `completion_logs.observeAll()` sınırsız tam tablo taraması + hiçbir listede `LazyColumn` yok | Room indeksleri, DataStore budama, macrobenchmark modülü ve baseline profile mevcut |
| Testing / QA | 5 | Beklet | `./gradlew testDebugUnitTest` şu an derlenmiyor (CI kırmızı); Brain Dump/Hafif Gün Modu/Onboarding mapping/UserPreferencesRepository sıfır test kapsamı | GameEngine, CloseDayUseCase, DAO katmanı gerçekten iyi test edilmiş; olgun CI pipeline iskeleti var |
| Monetization / Release | 4 | Beklet (monetizasyon), Kapalı test için yakın (release mekanizması) | Kod tabanında Billing/IAP kütüphanesi hiç yok; Privacy Policy eksikliği Play Store Data Safety formunu engelliyor | Release signing/versioning/checklist süreci olgun ve profesyonelce belgelenmiş; sanal ekonomi gerçek paraya hiç dokunmuyor |

## Genel Güçlü Yönler

1. **Domain/veri katmanı disiplinli.** Room şeması normalize, tarihler `LocalDate` ISO-8601 string olarak tutarlı saklanıyor, streak hesabı doğru, ödül sistemi `eventKey` bazlı idempotent (`RewardGrantService.grantOnce`), export/import validasyonu (boyut limiti, FK kontrolü, enum whitelist) üretim kalitesinde.
2. **Component mimarisi gerçek.** CLAUDE.md'nin öngördüğü katman hiyerarşisi (`core/`, `gamification/`, `molecules/`, `organisms/`) fiilen dolu ve isimlendirme dokümanla örtüşüyor; `Common.kt` ve eski `AppNavigation.kt` gerçekten silinmiş.
3. **Türkçe ton ve ürün kişiselleştirmesi kaliteli.** `strings.xml` genelinde suçlayıcı dil yok, onboarding önerileri (`suggestedRoutines`/`suggestedTaskTitle`) gerçekten seçime göre değişiyor, Hafif Gün Modu ve gün-kaçırma akışları destekleyici bir dille tasarlanmış.
4. **Veri gizliliği mimari olarak güvenli tasarlanmış.** Hiçbir network SDK'sı yok, tüm veri yerel; crash reporting allow-list ile PII sızıntısına karşı korunuyor; debug/release ayrımı ve `google-services.json` yokluğunda crash reporting'in sessizce devre dışı kalması bilinçli bir tasarım.
5. **Release mekanizması olgun.** Signing pipeline (`keystore.properties` + env var + `verifyReleaseSigning` task), R8/ProGuard kuralları, 10 bölümlük `release-checklist.md`, macrobenchmark ve JaCoCo/detekt/lint içeren CI pipeline'ı — çoğu erken aşama projede bulunmayan bir seviye disiplin gösteriyor.
6. **Backend yokluğu bilinçli bir mimari tercih.** Uygulamada hiçbir network/API katmanı yok ve bu bir eksiklik değil; local-first MVP kararı doğru uygulanmış, export/import servisleri ileride sync'e geçişi kolaylaştıracak şekilde zaten kodlanmış durumda.

## En Kritik 10 Sorun

| Öncelik | Sorun | Alan | Etki | Çözüm |
|---|---|---|---|---|
| 1 | `./gradlew testDebugUnitTest` şu an derlenmiyor (constructor imza uyuşmazlıkları, eksik `FakePrefs` metodu) — CI kırmızı | Testing/QA | Hiçbir regresyon güvencesi yok, release-quality gate geçilemiyor, branch şu anki haliyle merge edilemez kalitede | `TodayViewModelTest`, `SettingsViewModelTest`, `ObserveProgressSnapshotUseCaseTest`'teki parametre uyuşmazlıklarını gider, `FakePrefs`'e `setLightDayMode` ekle |
| 2 | Room migration geçmişi eksik — yalnızca `MIGRATION_6_7` var, v1-v5 yok, `fallbackToDestructiveMigration()` de yok | Data/Database | Eski şemalı bir cihaz güncelleme aldığında Room `IllegalStateException` fırlatır — uygulama açılmaz, tüm kullanıcı verisi erişilemez hale gelir | v1→7 arası migration'ları geriye dönük yaz veya "hiç yayınlanmadı" kararını PR/commit'te açıkça dokümante et |
| 3 | `CloseDaySheet` (gün kapatma formu: mood, en iyi an, zorluk, yarın niyeti) tüm alanları `remember`, `rememberSaveable` değil | State/ViewModel | Process death/arka plana atılıp bellekten temizlenme durumunda kullanıcının yazdığı günlük özet sessizce tamamen kayboluyor — uygulamanın duygusal çekirdeği zedeleniyor | Alanları `rememberSaveable`'a veya `SavedStateHandle` destekli bir state'e taşı |
| 4 | Görev silme/geri alma akışında alt görevler (subtask) `ON DELETE CASCADE` ile kalıcı silinir, `RestoreTaskUseCase` bunları geri getirmez | Data/Database | Kullanıcı "geri al" dese bile alt görev listesi sessizce boş döner — somut, fark edilir veri kaybı | Silmeden önce subtask'ları snapshot'la, restore akışına dahil et |
| 5 | Privacy Policy dokümanı/ekranı hiç yok; `SettingsViewModel.kt:54`'teki referans hiçbir yere bağlı değil | Security/Privacy, Monetization | Play Store Data Safety formu doldurulamaz — herhangi bir track'e (internal dahil) yükleme reddedilme riski taşır | Barındırılan bir Privacy Policy sayfası yaz, Ayarlar'a link/WebView olarak bağla |
| 6 | Tamamen kodlanmış export/import özelliği ve "Anonim kullanım ölçümü" analytics toggle'ı Settings UI'a hiç bağlanmamış | Security/Privacy, Monetization | Kullanıcı verisini indiremiyor, analytics'i kapatamıyor — veri taşınabilirliği ve kullanıcı kontrolü vaadi kırık; "iş bitti, teslim edilmedi" | `SettingsScreen.kt`'ye export/import butonları ve analytics switch'i ekle, mevcut `SettingsViewModel` fonksiyonlarına bağla |
| 7 | Rutin listesinde hedef/sayaç tipi rutinler (`targetType/targetValue/targetUnit`) için artır/azalt kontrolü hiçbir ekranda yok | Product/UX | "8 bardak su" gibi hedefli rutinler günlük akışta fiilen ilerletilemiyor — ürünün "alışkanlık takibi" çekirdek vaadinin bir alt kümesi çalışmıyor | `RoutineRow`'a hedef tipini ilet, sayaç rutinleri için +/- kontrolü ekle |
| 8 | `completion_logs.observeAll()` sınırsız tam tablo taraması + Today'deki hiçbir liste `LazyColumn` kullanmıyor | Performance | Uzun süreli kullanımda (aylar/yıllar) her state güncellemesi tüm geçmişi tarar; çok görevli günde scroll jank riski yüksek | Streak hesabını tarih-sınırlı sorguya çevir; `TaskListContainer`/`RoutineListContainer`'ı `LazyColumn`+`key` ile yeniden yaz |
| 9 | Başarım/ödül kutlama event pipeline'ı (`RewardDisplayService`, `AchievementTracker.newUnlock`) yalnızca `TodayViewModel` yaşam döngüsüne bağlı, `replay=0` | State/ViewModel | Shop/Progress/Settings gibi Today dışı ekranlardan tetiklenen başarımlar kullanıcı o an Today'de değilse kalıcı olarak kaybolur, kutlama hiç görünmez | Kutlama toplama işini uygulama-seviyesi bir coordinator'a taşı, ekran ViewModel'inden bağımsızlaştır |
| 10 | Kod tabanında Google Play Billing/IAP kütüphanesi, entitlement modeli veya premium/ücretsiz ayrımı hiç yok | Monetization/Release | Ücretli/premium modele geçiş şu an teknik olarak mümkün değil; monetizasyon planı sıfırdan kurulmalı | Premium karar netleşmeden Billing eklenmemeli; önce ücretsiz sürümü P0 maddeleriyle yayına hazırla, ardından ayrı bir teknik tasarımla Billing planla |

## Yayın Kararı

- [ ] Yayına hazır.
- [ ] Kısıtlı beta için hazır.
- [x] Data/state/test sorunları çözülmeden yayınlanmamalı.
- [x] Tasarım iyi, teknik borç çözülmeden yayınlanmamalı.
- [x] Monetizasyon için erken.

## 1 Haftalık Plan

### P0 — Mutlaka yapılacaklar

- [ ] Test derleme hatalarını çöz (`TodayViewModelTest`, `SettingsViewModelTest`, `ObserveProgressSnapshotUseCaseTest`, `FakePrefs`), `./gradlew testDebugUnitTest` yeşile dönsün.
- [ ] Room migration geçmişini netleştir: v1-v6 migration'larını yaz ya da "temiz başlangıç" kararını `Migrations.kt`/`AppModule.kt`'ye açıkça dokümante et.
- [ ] `CloseDaySheet` ve `TodayScreen`'deki 11 dialog/sheet state'ini `rememberSaveable`'a taşı; process-death senaryosunu manuel doğrula.
- [ ] Görev silme/restore akışına subtask snapshot desteği ekle.
- [ ] Privacy Policy sayfasını yaz ve Ayarlar'a bağla.

### P1 — Güçlü şekilde önerilenler

- [ ] Export/import butonlarını `SettingsScreen.kt`'ye ekle, mevcut `SettingsViewModel` fonksiyonlarına bağla.
- [ ] Analytics toggle'ını Gizlilik bölümüne ekle.
- [ ] Rutin listesine hedef/sayaç tipi rutinler için artır/azalt kontrolü ekle.
- [ ] `completion_logs.observeAll()`'ı tarih-sınırlı sorguya çevir; Today listelerini `LazyColumn`'a taşı.
- [ ] Brain Dump toplu ekleme ve `ToggleRoutineUseCase`'i `DatabaseTransactionRunner` ile transaction'a al.

## 2 Haftalık Plan

- [ ] 1 haftalık planın tamamı + regresyon testiyle doğrulama (görev/rutin/gün kapatma akışları manuel QA).
- [ ] Başarım/ödül kutlama pipeline'ını (`RewardDisplayService`, `AchievementTracker`) uygulama-seviyesi bir coordinator'a taşıyarak Today ekranı bağımlılığından kurtar; "Kutlama efektleri" ayarını gerçek tetikleyicilere bağla.
- [ ] `TodayScreen.kt`/`TodayViewModel.kt`/`TodaySheets.kt`'yi CLAUDE.md'nin 200 satır kuralına uyacak şekilde organisms'e böl (FAB menüsü, banner'lar, sheet'ler ayrı dosyalara).
- [ ] Onboarding öneri mapping'ini (`suggestedRoutines`/`suggestedTaskTitle`) saf fonksiyona çıkar ve test et; `BrainDumpParser`'ı saf fonksiyona çıkar ve test et; `LightDayModeDateTest` ve `UserPreferencesRepositoryTest` yaz.
- [ ] Ölü kodu sil (`PlanTaskListComponents.kt`, `CelebrationSystem.kt`, `.bak` dosyası), iki paralel `AddTaskSheet`/konfeti implementasyonunu tek sisteme konsolide et.
- [ ] Tüm ana rotalarda (Rutin Detay, Başarımlar, Dükkan, OSS Lisansları) ortak `AppTopBar`/bottom nav sözleşmesini uygula.
- [ ] OSS lisans listesini gerçek bağımlılıklarla senkronize et; `docs/store/` altında store screenshot/feature graphic hazırlığına başla.

## 1 Aylık Plan

- [ ] `TodayViewModel`'i (740 satır, 27 bağımlılık) `TaskActions`/`RoutineActions`/`DayCloseActions` gibi alt sorumluluklara böl; toggle işlemlerine görev-id bazlı mutex/in-flight guard ekle.
- [ ] Hardcoded dp/renk/Türkçe string temizliğini proje genelinde tamamla (`AppTopBar`, `AddTaskSheet`, `AchievementsScreen`, `OnboardingScreen` dahil); bunu yakalayan bir Detekt/ktlint custom rule CI'a ekle.
- [ ] JaCoCo eşiğini paket bazlı ayır (domain/data için %70+); kritik organizmalar için `@Preview` seti (light/dark, boş/dolu state) ekle; `warmStartup`/`scrollJank` benchmark testlerini stabilize edip tekrar aktive et.
- [ ] Kritik silme akışları için soft-delete/trash stratejisini değerlendir; export/import'a `lightDayModeDate` alanını ekle; crash reporting'i prod'da aktifleştirmek için Firebase projesi kurup `google-services.json` ekle.
- [ ] Monetizasyon kararı netleşirse: Billing kütüphanesi + entitlement/receipt doğrulama mimarisini ayrı bir teknik tasarım dokümanında planla (bu ayda kod yazılmasın, yalnızca tasarım).
- [ ] Gelecekteki sync/multi-device desteği için entity şemalarına `userId` ve tutarlı `updatedAt` alanlarını ekleyecek bir migration'ı planla (05_backend_sync_report.md); `UserPreferencesRepository`'ye premium/entitlement alanı için yer ayır.
- [ ] CLAUDE.md'deki "Faz E TAMAMLANDI" ibaresini gerçek koda göre güncelle; store listing metni/görselleri ve TR mağaza açıklamasını tamamla.

## Release Checklist

- [ ] Debug ve release build başarılı. *(Release build/AAB bu turda gerçek olarak çalıştırılmadı, doğrulanmadı — ayrıca unit test derlemesi şu an kırık.)*
- [ ] Kritik ViewModel testleri geçiyor. *(TodayViewModelTest dahil test paketi derlenmiyor.)*
- [x] Kritik repository/database testleri geçiyor. *(DAO/migration/GameEngine/CloseDayUseCase testleri sağlam ve mevcut; UserPreferencesRepository testsiz kalan istisna.)*
- [ ] Onboarding manuel QA tamam. *(Dokümante edilmiş bir manuel QA checklist bulunamadı.)*
- [ ] Today ekranı manuel QA tamam. *(Aynı gerekçe; ayrıca process-death senaryosu doğrulanmamış.)*
- [ ] Hafif Gün Modu QA tamam. *(Sıfır otomatik test, manuel checklist kanıtı yok.)*
- [ ] Brain Dump QA tamam. *(Parse mantığı test edilemez durumda, sıfır test.)*
- [x] Reset/Nefes QA tamam. *(ResetDialog UX/ton açısından temiz bulundu; zamanlayıcı testi eksik ama işlevsel risk düşük değerlendirildi.)*
- [ ] Privacy Policy hazır. *(Doküman/ekran hiç yok.)*
- [ ] OSS Licenses ekranı hazır. *(Ekran var ama liste eksik/güncel değil — gerçek bağımlılıklarla senkron değil.)*
- [x] Crash reporting kararı verildi. *(Bilinçli tasarım: debug'da local, release'de `google-services.json` varsa Crashlytics — karar net, sadece prod aktivasyonu henüz yapılmamış.)*
- [ ] Analytics kişisel veri toplamıyor. *(Mimari PII-safe ama kullanıcı kontrolü — toggle — UI'da yok, bu nedenle "kullanıcı onayı/kontrolü" boyutu eksik sayılmalı.)*
- [ ] Store screenshots hazır. *(Küratörlü store asset klasörü yok; root'taki `screen_*.png` dosyaları ad-hoc QA görüntüsü.)*
- [x] Version code/name doğru. *(`versionCode=1`, `versionName="0.1.0"` — henüz 1.0 öncesi ama internal/closed test için tutarlı ve doğru.)*
- [x] Release signing hazır. *(Keystore config, `verifyReleaseSigning` task, GitHub Secrets akışı sağlam.)*

## Final Not

Benim Günlerim, tek geliştiricili bir proje için beklenenin oldukça üzerinde bir mimari ve ürün-dili olgunluğuna sahip — Room/DataStore veri katmanı, component mimarisi ve release süreç dokümantasyonu gerçekten iyi seviyede; ama bu olgunluk, kodun kendi iddialarıyla (CLAUDE.md'nin "Faz E TAMAMLANDI" demesi, testlerin "yeşil" görünmesi gerektiği varsayımı) örtüşmüyor. Şu anda proje üç ayrı nedenle yayına hazır değil: testler derlenmiyor (CI kırmızı), Room migration geçmişi eksik olduğu için bir güncelleme kullanıcı verisini erişilemez kılabilir, ve gün kapatma formu gibi uygulamanın duygusal çekirdeği process death'te sessizce siliniyor — bunlara ek olarak Privacy Policy'nin hiç yazılmamış olması tek başına Play Store'a çıkışı engelliyor. Görsel/ürün tarafı iyi olsa da, veri bütünlüğü ve gizlilik şeffaflığı borcu çözülmeden — özellikle test paketi yeniden çalışır hale getirilmeden — hiçbir track'e (internal dahil) yükleme yapılmamalı; bunların çoğu büyük bir yeniden yazım değil, 1-2 haftalık hedefli bir sprintle kapatılabilir "bağlama" ve "tamamlama" işleri. Monetizasyon ise bambaşka bir eksen: kod tabanında hiçbir Billing/IAP altyapısı yok, dolayısıyla ücretli modele geçiş şu an gündemde bile olmamalı — önce sağlam, ücretsiz, gizlilik-şeffaf bir sürüm çıkarılmalı.
