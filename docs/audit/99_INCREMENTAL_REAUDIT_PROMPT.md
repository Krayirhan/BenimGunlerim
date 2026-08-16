# 99 — Incremental Re-Audit Prompt

Bu dosya tam proje audit'i yapmak için değildir.

Bu dosya, daha önce tamamlanmış audit raporlarından sonra yapılan değişiklikleri kontrollü şekilde yeniden incelemek için kullanılır.

Amaç:

- Eski auditleri yeniden üretmemek
- Tüm repoyu baştan taramamak
- Sadece değişen dosyaları ve doğrudan ilişkili dosyaları incelemek
- Önceden açık olan P0/P1/P2 maddelerinin kapanıp kapanmadığını doğrulamak
- Yeni regresyon oluşup oluşmadığını bulmak
- Token kullanımını sert şekilde sınırlamak

---

## 0. Çalışma Modu

Bu çalışma **INCREMENTAL RE-AUDIT** modundadır.

Bu bir full audit değildir.

Ajan aşağıdaki kurallara uymak zorundadır:

- Tüm projeyi yeniden audit etme.
- Önceki audit raporlarını yeniden yazma.
- Product/UX, Frontend, State, Data, Backend, Security, Performance, Testing, Monetization auditlerini baştan üretme.
- Repo genelinde serbest dosya taraması yapma.
- Kod değiştirme. Sadece analiz yap.
- Eski raporlardaki tüm bulguları tekrar özetleme.
- Sadece değişen dosyalar üzerinden delta raporu üret.

Yasak klasörler:

```text
build/
.gradle/
generated/
screenshots/
assets/
app/build/
docs/audit/results/old/
```

`docs/audit/results/` altındaki eski raporlar yalnızca baseline olarak okunabilir. Hepsi baştan okunamaz.

---

## 1. Baseline Dosyaları

Önce yalnızca şu dosyayı oku:

```text
docs/audit/results/10_final_scorecard.md
```

Bu dosya baseline kabul edilir.

Gerekirse, yalnızca değişiklik alanıyla ilgili eski raporu oku:

```text
docs/audit/results/01_product_ux_report.md
docs/audit/results/02_frontend_compose_report.md
docs/audit/results/03_state_viewmodel_report.md
docs/audit/results/04_data_database_report.md
docs/audit/results/05_backend_sync_report.md
docs/audit/results/06_security_privacy_report.md
docs/audit/results/07_performance_report.md
docs/audit/results/08_testing_qa_report.md
docs/audit/results/09_monetization_release_report.md
```

Kural:

- Tüm eski raporları okuma.
- En fazla 2 eski rapor oku.
- Çoğu durumda `10_final_scorecard.md` yeterlidir.

---

## 2. Değişiklik Kaynağı

Önce değişen dosyaları belirle.

Tercih edilen komutlar:

```bash
git status --short
git diff --name-only
git diff --name-only main...HEAD
```

Kullanıcı belirli bir commit/branch aralığı verdiyse onu kullan:

```bash
git diff --name-only <base>...<head>
```

Değişen dosya listesi olmadan repo taraması yapma.

Eğer branch/commit aralığı belli değilse kullanıcıdan şunu iste:

```text
Hangi aralığı yeniden kontrol edeyim?
Örnek:
- main...current-branch
- HEAD~5...HEAD
- son commit
- belirli PR branch'i
```

---

## 3. Token ve Dosya Sınırı

Bu re-audit için sınırlar:

```text
Detaylı okunacak kaynak dosya: en fazla 15
Yüzeysel bakılacak kaynak dosya: en fazla 30
Eski audit raporu: en fazla 2
Toplam çıktı: kısa ve yoğun
```

Hedef token kullanımı:

```text
Küçük değişiklik: 10k - 30k
Orta değişiklik: 30k - 80k
Büyük ama sınırlı değişiklik: 80k - 150k
150k üstü: dur ve kullanıcıdan kapsam daraltmasını iste
```

150k token üstüne çıkacağını düşünüyorsan analizi başlatma. Şunu yaz:

```text
Bu re-audit beklenen token bütçesini aşacak.
Lütfen kapsamı daralt:
1. Sadece P0 doğrulama
2. Sadece Testing
3. Sadece Privacy/Settings
4. Sadece Data/Database
5. Sadece Today/Compose
6. Sadece Performance
```

Eğer değişen dosya sayısı 40'tan fazlaysa dur ve şunu yaz:

```text
Bu değişiklik seti incremental re-audit için fazla büyük.
Lütfen daha küçük commit/PR parçalarına böl.
Önerilen parçalama:
1. Testing fixes
2. Privacy/settings fixes
3. Data/database fixes
4. Today/Compose refactor
5. Performance fixes
6. Release/store fixes
```

---

## 4. Alan Eşleme Kuralları

Değişen dosyalara göre yalnızca ilgili alanı kontrol et.

### 4.1 Testing / QA

Şu dosyalar değiştiyse sadece Testing / QA re-check yap:

```text
src/test/
src/androidTest/
.github/workflows/
build.gradle.kts
gradle/libs.versions.toml
```

Kontrol et:

- `./gradlew testDebugUnitTest` derleniyor mu?
- Kırık testler düzeldi mi?
- Yeni testler gerçekten kritik akışları kapsıyor mu?
- `BrainDumpParserTest` eklendi mi?
- `LightDayModeDateTest` eklendi mi?
- `UserPreferencesRepositoryTest` eklendi mi?
- `OnboardingRecommendationTest` eklendi mi?
- Testler sadece compile olsun diye mi yazılmış, yoksa gerçek edge case kapsıyor mu?

Okunabilecek eski rapor:

```text
docs/audit/results/08_testing_qa_report.md
```

---

### 4.2 Security / Privacy / Release

Şu dosyalar değiştiyse Security / Privacy ve gerekirse Monetization / Release re-check yap:

```text
SettingsScreen.kt
SettingsViewModel.kt
PrivacyPolicy*
DataExportService.kt
DataImportService.kt
AndroidManifest.xml
data_extraction_rules.xml
full_backup_content.xml
OssLicensesScreen.kt
google-services.json
```

Kontrol et:

- Privacy Policy gerçekten var mı?
- Ayarlar'dan erişiliyor mu?
- Export/import UI'a bağlandı mı?
- Analytics toggle görünür ve çalışır mı?
- Brain Dump gizlilik notu var mı?
- Android backup davranışı policy'de anlatılıyor mu?
- OSS lisans listesi güncel mi?
- Crashlytics aktifse policy bunu söylüyor mu?
- Data Safety formu ile policy çelişir mi?

Okunabilecek eski raporlar:

```text
docs/audit/results/06_security_privacy_report.md
docs/audit/results/09_monetization_release_report.md
```

---

### 4.3 Data / Database

Şu dosyalar değiştiyse sadece Data / Database re-check yap:

```text
data/local/
data/*Repository.kt
domain/usecase/*Task*
domain/usecase/*Routine*
DataExportService.kt
DataImportService.kt
Migrations.kt
AppDatabase.kt
```

Kontrol et:

- Migration kararı net mi?
- Eksik migration yazıldı mı veya "temiz başlangıç" dokümante edildi mi?
- Subtask restore kaybı kapandı mı?
- Brain Dump toplu ekleme transaction/batch oldu mu?
- `ToggleRoutineUseCase` transaction desenine geçti mi?
- Export/import `lightDayModeDate` taşıyor mu?
- Yeni veri kaybı riski oluştu mu?

Okunabilecek eski rapor:

```text
docs/audit/results/04_data_database_report.md
```

---

### 4.4 State / ViewModel

Şu dosyalar değiştiyse sadece State / ViewModel re-check yap:

```text
TodayViewModel.kt
TodayScreen.kt
TodaySheets.kt
AddTaskSheet.kt
RewardDisplayService.kt
AchievementTracker.kt
ShopViewModel.kt
SettingsViewModel.kt
```

Kontrol et:

- `CloseDaySheet` state'i `rememberSaveable` veya ViewModel/SavedStateHandle'a taşındı mı?
- TodayScreen dialog/sheet state'leri process death'e dayanıklı mı?
- Celebration event pipeline Today ekranından bağımsız hale geldi mi?
- One-shot event modeli tutarlı mı?
- Toggle race condition için in-flight guard/mutex var mı?
- Yeni duplicate state oluştu mu?

Okunabilecek eski rapor:

```text
docs/audit/results/03_state_viewmodel_report.md
```

---

### 4.5 Frontend / Compose / Product UX

Şu dosyalar değiştiyse Frontend / Compose ve gerekirse Product / UX re-check yap:

```text
ui/today/
ui/components/
ui/navigation/
ui/onboarding/
ui/routines/
ui/settings/
strings.xml
Theme.kt
DesignTokens.kt
```

Kontrol et:

- `TodayScreen` / `TodaySheets` / `OnboardingScreen` bölündü mü?
- Hardcoded string/dp/Color azaldı mı?
- Rutin hedef/sayaç UI eklendi mi?
- Kutlama efektleri ayarı gerçekten çalışıyor mu?
- AppTopBar / bottom nav tutarlılığı sağlandı mı?
- Ölü kod silindi mi?
- Yeni UI regresyonu var mı?
- Büyük liste için `LazyColumn + key` kullanıldı mı?
- `@Preview` kapsamı arttı mı?

Okunabilecek eski raporlar:

```text
docs/audit/results/01_product_ux_report.md
docs/audit/results/02_frontend_compose_report.md
```

---

### 4.6 Performance

Şu dosyalar değiştiyse sadece Performance re-check yap:

```text
CompletionLogDao.kt
ObserveTodaySnapshotUseCase.kt
TodayListContainers.kt
BrainDumpDialog.kt
TodayScreen.kt
benchmark/
```

Kontrol et:

- `completion_logs.observeAll()` sınırsız tarama kapandı mı?
- Liste render'ı `LazyColumn + key` oldu mu?
- Brain Dump seçim listesi `LazyColumn` oldu mu?
- Brain Dump toplu ekleme transaction/batch oldu mu?
- WarmStartup / ScrollJank benchmark hâlâ `@Ignore` mı?
- Yeni infinite animation / recomposition riski oluştu mu?

Okunabilecek eski rapor:

```text
docs/audit/results/07_performance_report.md
```

---

### 4.7 Backend / Sync

Şu dosyalar değiştiyse sadece Backend / Sync readiness re-check yap:

```text
DataExportService.kt
DataImportService.kt
data/local/entity/
UserPreferencesRepository.kt
AndroidManifest.xml
build.gradle.kts
```

Kontrol et:

- Export/import UI'a bağlandı mı?
- Sync kararı değişti mi?
- Yeni backend/network bağımlılığı eklendi mi?
- `userId`, `deviceId`, `updatedAt`, `deletedAt` gibi alanlar eklendiyse migration planı var mı?
- Entitlement/premium state alanı eklendiyse güvenli mi?
- Local-first mimari hâlâ bilinçli ve tutarlı mı?

Okunabilecek eski rapor:

```text
docs/audit/results/05_backend_sync_report.md
```

---

### 4.8 Monetization / Release

Şu dosyalar değiştiyse sadece Monetization / Release re-check yap:

```text
app/build.gradle.kts
gradle/libs.versions.toml
ShopViewModel.kt
SettingsScreen.kt
PrivacyPolicy*
docs/release/
docs/production/
docs/store/
fastlane/
google-services.json
```

Kontrol et:

- Billing/IAP eklendiyse entitlement modeli var mı?
- Premium/free ayrımı net mi?
- Privacy Policy ve Data Safety uyumlu mu?
- Store listing assetleri hazır mı?
- Release signing/checklist bozuldu mu?
- Crashlytics prod'da doğrulanmış mı?
- OSS lisansları güncel mi?

Okunabilecek eski rapor:

```text
docs/audit/results/09_monetization_release_report.md
```

---

## 5. İnceleme Yöntemi

Adım adım ilerle:

1. `docs/audit/results/10_final_scorecard.md` içindeki P0/P1 listesini oku.
2. `git diff --name-only` ile değişen dosyaları bul.
3. Değişen dosyaları audit alanlarına eşle.
4. Sadece ilgili eski raporu oku.
5. Sadece değişen dosyaları detaylı incele.
6. Gerekiyorsa doğrudan bağlı en fazla 5 dosyayı aç.
7. Eski P0/P1 maddelerinden hangileri kapanmış, hangileri açık kalmış yaz.
8. Yeni P0/P1/P2 regresyon varsa belirt.
9. Genel proje audit'i üretme; sadece delta raporu üret.

---

## 6. Çıktı Formatı

Raporu şu formatta yaz:

```md
# Incremental Re-Audit Report

## İncelenen Değişiklik Aralığı

- Branch / commit aralığı:
- Değişen dosya sayısı:
- Detaylı incelenen dosya sayısı:
- Referans alınan eski raporlar:

## Kapsam Kararı

Bu re-audit şu alanları kapsadı:

- [ ] Product / UX
- [ ] Frontend / Compose
- [ ] State / ViewModel
- [ ] Data / Database
- [ ] Backend / Sync
- [ ] Security / Privacy
- [ ] Performance
- [ ] Testing / QA
- [ ] Monetization / Release

Kapsam dışında bırakılan alanlar:
- ...

## Kapanan Eski Riskler

| Eski Risk | Öncelik | Durum | Kanıt |
|---|---:|---|---|
| ... | P0 | Kapandı | Dosya/satır/commit |

## Açık Kalan Eski Riskler

| Eski Risk | Öncelik | Durum | Neden Kapanmadı |
|---|---:|---|---|
| ... | P0 | Açık | ... |

## Yeni Bulunan Regresyonlar

| Yeni Risk | Öncelik | Etki | Kanıt | Öneri |
|---|---:|---|---|---|
| ... | P1 | ... | ... | ... |

## Güncel Yayın Kararı

- Production:
- Internal / Closed Beta:
- Monetization:
- Gerekçe:

## Güncel P0 Listesi

1.
2.
3.

## Sonraki En Mantıklı PR

PR adı:
Amaç:
Dokunulacak dosyalar:
Beklenen testler:
Risk:

## Token / Kapsam Notu

Bu re-audit full proje audit'i değildir.
Sadece değişen dosyalar ve ilgili eski riskler incelenmiştir.
```

---

## 7. Yasaklı Davranışlar

Aşağıdakiler kesin yasaktır:

- "Projeyi baştan değerlendirdim" demek
- Eski auditleri tekrar üretmek
- Tüm ekranlara yeniden bakmak
- Tüm repository katmanını yeniden taramak
- Görsel/UX audit için Data/Database dosyalarını açmak
- Testing audit için tüm UI dosyalarını açmak
- Performance audit için tüm business logic'i okumak
- Önceki raporları komple özetlemek
- `docs/audit/results` altındaki tüm dosyaları okumak
- Asset, screenshot, generated veya build output okumak

---

## 8. Acil Durdurma Kuralları

### Değişen dosya yoksa

```text
Değişen dosya bulunmadı.
Re-audit yapılmasına gerek yok.
```

### Çok fazla dosya değişmişse

```text
Bu değişiklik seti çok büyük.
Lütfen PR'ı küçük parçalara böl.
```

### Eski final scorecard yoksa

```text
Baseline final scorecard bulunamadı.
Önce docs/audit/results/10_final_scorecard.md dosyası gerekli.
```

### Sadece rapor istenmişse

Kullanıcı sadece "mevcut raporlardan özet çıkar" dediyse repo dosyalarını açma.

Sadece şunu oku:

```text
docs/audit/results/10_final_scorecard.md
```

---

## 9. Kullanılacak Kısa Komut

Ajanı çalıştırırken şu promptu kullan:

```text
docs/audit/99_INCREMENTAL_REAUDIT_PROMPT.md dosyasını uygula.

Tam audit yapma.
Repo genelini tarama.
Eski audit raporlarını tekrar üretme.
Sadece git diff ile değişen dosyaları ve ilgili eski P0/P1 risklerini kontrol et.
Çıktıyı docs/audit/results/INCREMENTAL_REAUDIT_YYYY_MM_DD.md olarak yaz.
```

---

## 10. PR Bazlı Kullanım

Her PR için ideal kullanım:

1. PR sadece tek konu içersin.
2. Agent sadece bu prompt ile çalışsın.
3. Çıktı kısa incremental rapor olsun.
4. P0 kapanmış mı, yeni P0 açılmış mı ona baksın.
5. Full scorecard yalnızca büyük milestone sonunda güncellensin.

Önerilen PR parçaları:

```text
PR-1: Testing compile fixes
PR-2: Privacy + Settings export/import + analytics toggle
PR-3: Data migration + subtask restore
PR-4: CloseDay/Today rememberSaveable
PR-5: Routine target counter UI
PR-6: Performance LazyColumn + completion_logs limit
PR-7: Frontend cleanup / hardcoded strings / previews
```

Milestone örnekleri:

```text
P0_FIX_SPRINT_DONE
BETA_CANDIDATE_1
PLAY_INTERNAL_TEST_READY
PRODUCTION_CANDIDATE_1
```

Sadece bu milestone'larda daha geniş final scorecard update yapılabilir.

---

## 11. Full Re-Audit Ne Zaman Yapılır?

Full audit sadece şu durumlarda yapılır:

- Ana mimari değiştiyse
- Backend/sync eklendiyse
- Billing/IAP eklendiyse
- Room şeması büyük değiştiyse
- Navigation/scaffold sistemi tamamen değiştiyse
- Release candidate çıkmadan hemen önce
- 20+ dosyalı büyük refactor tamamlandıysa

Full re-audit yapılacaksa bile önce kullanıcıdan onay alınmalıdır:

```text
Bu işlem full re-audit gerektiriyor ve yüksek token kullanabilir.
Devam edeyim mi, yoksa alan bazlı incremental audit mi yapalım?
```

Kullanıcı onaylamadan full re-audit başlatma.

---

## 12. En Önemli Kural

Bu dosyanın amacı kaliteyi yeniden ölçmek değil, **değişikliklerin eski riskleri kapatıp kapatmadığını doğrulamaktır**.

Yani doğru soru şudur:

```text
Bu PR eski P0/P1 risklerinden hangilerini kapattı, hangilerini açık bıraktı, yeni risk açtı mı?
```

Yanlış soru şudur:

```text
Bu projeyi baştan değerlendirir misin?
```
