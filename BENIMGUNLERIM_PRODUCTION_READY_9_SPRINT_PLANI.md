# BenimGunlerim Production Ready 9/10 Sprint Plani

Bu dokuman BenimGunlerim projesini mevcut MVP seviyesinden, teknik olarak guvenilir, test edilebilir, veri kaybi riski dusuk, Play Store'a hazir ve uzun vadede bakimi surdurulebilir **9/10 production-ready** seviyesine tasimak icin hazirlanmistir.

Bu plan, urun ozellik sprintlerinden farkli olarak muhendislik altyapisini merkeze alir:

- Build, lint, test ve release kapilari
- Room migration ve veri guvenligi
- Bildirim/alarm guvenilirligi
- UI test edilebilirligi
- Security ve privacy kararlari
- Crash/error observability
- CI/CD ve release sureci
- Kod hijyeni, modul sinirlari ve teknik borc

## 1. Mevcut Teknik Durum

Son teknik degerlendirmede gorulen durum:

| Alan | Mevcut Puan | 9/10 Icin Durum |
|---|---:|---|
| Genel production readiness | 5.8 | Kritik kalite kapilari tamamlanmali |
| Mimari yapi | 7.0 | Iyi temel, sinirlar sertlestirilmeli |
| Veri katmani / Room | 4.5 | Migration ve schema yonetimi zorunlu |
| Repository / domain mantigi | 5.5 | Test ve state davranislari guclendirilmeli |
| UI / Compose | 6.5 | Test tag, accessibility, state stability gerekli |
| Bildirim sistemi | 4.0 | Permission, exact alarm, boot ve policy eksik |
| Test altyapisi | 4.0 | UI testleri kirik, migration testi yok |
| Release / paketleme | 4.5 | Minify, signing, versioning ve CI eksik |
| Security / privacy | 5.0 | Backup, logging, data policy netlesmeli |
| Observability | 3.5 | Crash/error/event altyapisi yok |
| DevOps / CI | 3.5 | Otomatik kalite kapilari yok |

Production hedefi:

| Alan | Hedef Puan |
|---|---:|
| Genel production readiness | 9.0 |
| Mimari yapi | 9.0 |
| Veri katmani / Room | 9.0 |
| Repository / domain mantigi | 9.0 |
| UI / Compose | 9.0 |
| Bildirim sistemi | 9.0 |
| Test altyapisi | 9.0 |
| Release / paketleme | 9.0 |
| Security / privacy | 9.0 |
| Observability | 9.0 |
| DevOps / CI | 9.0 |

## 2. Production Ready Tanimi

Bu proje ancak asagidaki kosullar saglandiginda production-ready kabul edilir:

- `testDebugUnitTest` yesil.
- `lintDebug` ve `lintRelease` yesil.
- `assembleRelease` yesil.
- En az kritik UI akislari cihaz/emulator uzerinde yesil.
- Room destructive migration kapali.
- Tum aktif DB surumleri icin migration stratejisi var.
- Release build minify/shrink/resource shrink ile test edilmis.
- Runtime permission ve alarm davranislari Android 13/14/15 icin guvenli.
- Boot/restart sonrasinda bildirimler dogru yeniden kurulur.
- Kullanici verisi backup/privacy kararlari dokumante edilmis.
- Crash ve non-fatal hata gorulebilir.
- CI pipeline merge oncesi kalite kapisi olarak calisir.
- Kullaniciya gorunen metinlerde encoding bozuklugu yok.

## 3. Ana Kalite Kapilari

Her sprint sonunda asagidaki komutlar hedeflenir:

```powershell
.\gradlew.bat testDebugUnitTest
.\gradlew.bat lintDebug
.\gradlew.bat lintRelease
.\gradlew.bat assembleRelease
.\gradlew.bat connectedDebugAndroidTest
```

`connectedDebugAndroidTest` her lokal ortamda zorunlu olmayabilir; ancak CI veya release adayi ortaminda zorunlu kapidir.

## 4. Sprint Haritasi

Bu plan 10 ana sprintten olusur. Siralama bilincli olarak once teknik guvenlik, sonra test, sonra release sertlestirme seklindedir.

| Sprint | Tema | Hedef Puan Etkisi |
|---|---|---|
| 0 | Repo ve kalite tabani | DevOps 3.5 -> 6.0 |
| 1 | Lint ve encoding temizligi | Kod hijyeni 5.5 -> 7.0 |
| 2 | Room migration ve veri guvenligi | Veri 4.5 -> 8.0 |
| 3 | Bildirim ve alarm production davranisi | Bildirim 4.0 -> 8.0 |
| 4 | Test mimarisi ve unit kapsam | Test 4.0 -> 7.0 |
| 5 | UI test ve accessibility altyapisi | UI/Test 6.5/7.0 -> 8.0 |
| 6 | Release build, signing, shrink | Release 4.5 -> 8.5 |
| 7 | Observability, crash ve diagnostics | Observability 3.5 -> 8.0 |
| 8 | Security, privacy, backup, data export | Security 5.0 -> 8.5 |
| 9 | Stabilizasyon ve release candidate | Genel 8.0 -> 9.0 |

## Sprint 0 - Repo, Branch, CI ve Kalite Tabanini Kur

### Hedef

Projeyi tek kisilik lokal calisma seviyesinden, tekrarlanabilir build ve kalite kapilari olan muhendislik reposuna tasimak.

### Kapsam

- Git repository baslatma veya mevcut repo standardizasyonu.
- `.gitignore` dogrulama.
- Build artefact temizligi.
- CI pipeline tasarimi.
- PR/check standardi.
- Lokal kalite scriptleri.
- Dokumantasyon dosya duzeni.

### Teknik Tasarim

Onerilen repo yapisi:

```text
.
|-- app/
|-- gradle/
|-- docs/
|   |-- production/
|   |-- testing/
|   |-- release/
|-- scripts/
|-- README.md
|-- BENIMGUNLERIM_9_PUAN_SPRINT_PLANI.md
|-- BENIMGUNLERIM_PRODUCTION_READY_9_SPRINT_PLANI.md
```

Onerilen kalite scriptleri:

```text
scripts/check-local.ps1
scripts/check-release.ps1
scripts/clean-build-artifacts.ps1
```

`check-local.ps1`:

- `testDebugUnitTest`
- `lintDebug`
- `assembleDebug`

`check-release.ps1`:

- `testDebugUnitTest`
- `lintRelease`
- `assembleRelease`

CI pipeline:

- Pull request icin: unit test + lint + debug assemble.
- Main branch icin: unit test + lint release + release assemble.
- Release tag icin: signed artifact uretimi.

### Yapilacaklar

- Git repo yoksa `git init` ile baslat.
- `app/build`, `.gradle`, `.idea`, `.kotlin`, build log dosyalari ve local artefactlari repodan uzak tut.
- `local.properties` repoya girmeyecek sekilde garanti altina al.
- `docs/production` altina kalite kapilari dokumani ekle.
- CI icin GitHub Actions ya da kullanilan platforma gore pipeline dosyasi hazirla.
- Gradle wrapper checksum ve JDK 17 gereksinimini README'ye yaz.

### Kabul Kriterleri

- Temiz checkout sonrasi build komutu dokumandan takip edilerek calisir.
- CI en az unit test, lint ve assembleDebug calistirir.
- Lokal build artefactlari kaynak listesine karismaz.
- README production kalite komutlarini icerir.

### Puan Etkisi

- DevOps: 3.5 -> 6.0
- Release: 4.5 -> 5.5
- Maintainability: 5.5 -> 6.0

## Sprint 1 - Lint, Encoding ve Kod Hijyeni

### Hedef

Production kapisini daha ilk asamada kiran lint hatalarini sifirlamak ve kullaniciya gorunen encoding problemlerini temizlemek.

### Kritik Mevcut Sorunlar

- `lintDebug` 4 hata veriyor.
- Notification publish cagirilari `MissingPermission` hatasi uretiyor.
- Bildirim metinlerinde mojibake var: `GÃ¼n`, `hatÄ±rlatma`, `planlayÄ±cÄ±`.
- Bazi yorum/metinlerde encoding bozulmasi var.

### Teknik Tasarim

Notification publish icin merkezi wrapper:

```kotlin
internal fun Context.safeNotify(
    notificationId: Int,
    notification: Notification,
): Boolean
```

Davranis:

- Android 13+ icin `POST_NOTIFICATIONS` kontrol edilir.
- Permission yoksa false doner.
- `SecurityException` yakalanir.
- Lint icin izin kontrati tek noktada belgelenir.

Encoding stratejisi:

- Kullaniciya gorunen butun stringler `strings.xml` icine tasinir.
- Kotlin dosyalarinda sadece dinamik string birlestirme kalir.
- Terminal encoding kaynakli tekrar bozulmayi onlemek icin dosyalar UTF-8 olarak tutulur.

### Yapilacaklar

- `NotificationHelper.kt` icinde merkezi `safeNotify` olustur.
- Dort `NotificationManagerCompat.notify` cagrisini wrapper'a tasima.
- Bildirim kanal adlarini `strings.xml` icine alma.
- Bildirim baslik/metinlerini `strings.xml` icine alma.
- `rg "Ã|Ä|Å|Â|â|�"` taramasini sifira yaklastirma.
- `lintDebug` hatalarini sifirlama.
- Lint uyarilarini onceliklendirme:
  - MissingPermission: zorunlu.
  - ObsoleteSdkInt: temizlenmeli.
  - UseTomlInstead: Gradle hijyeni icin temizlenmeli.
  - ComposableNaming: duzeltilmeli.

### Testler

- `lintDebug`
- `testDebugUnitTest`
- Notification permission unit testi veya Robolectric yoksa wrapper icin saf fonksiyon testleri.
- Manuel test:
  - Permission verilmisken notification gorunur.
  - Permission reddedilmisken crash olmaz.

### Kabul Kriterleri

- `lintDebug` yesil.
- Kullaniciya gorunen temel bildirim metinlerinde encoding bozuklugu yok.
- Notification publish izinsiz durumda crash uretmez.
- `strings.xml` uygulamanin ana metinlerini tasimaya baslar.

### Puan Etkisi

- Kod hijyeni: 5.5 -> 7.0
- Bildirim: 4.0 -> 5.5
- Release: 4.5 -> 5.5

## Sprint 2 - Room Migration ve Veri Guvenligi

### Hedef

Offline-first uygulamanin en kritik riski olan veri kaybini ortadan kaldirmak.

### Kritik Mevcut Sorunlar

- `AppDatabase` version 7.
- `exportSchema = false`.
- `fallbackToDestructiveMigration()` aktif.
- Migration testleri yok.

### Teknik Tasarim

Yeni DB politikasi:

```kotlin
@Database(
    entities = [...],
    version = 8,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase()
```

`Room.databaseBuilder`:

```kotlin
Room.databaseBuilder(context, AppDatabase::class.java, "benim_gunlerim.db")
    .addMigrations(MIGRATION_7_8)
    .build()
```

Schema dosya konumu:

```text
app/schemas/com.benimgunlerim.data.local.AppDatabase/
```

Migration stratejisi:

- Mevcut kullanicilar icin veri kaybetmeyen migration.
- Eski surumler bilinmiyorsa minimum olarak v7 -> v8 forward migration.
- Gecmis release olmadiysa production baslangic surumu net dokumante edilir.
- Bundan sonra her schema degisikligi PR'da schema diff ile gelir.

### Yapilacaklar

- `exportSchema = true` yap.
- KSP/Room schema export ayarini Gradle'a ekle.
- `fallbackToDestructiveMigration()` kaldir.
- `Migrations.kt` olustur.
- `MigrationTest` ekle.
- Entity default degerlerini migration ile uyumlu hale getir.
- `CompletionLogEntity`, `TaskEntity`, `RoutineEntity`, `DailyStateEntity`, `AchievementEntity`, `SubTaskEntity` icin indeks ihtiyacini degerlendir.
- Sorgular icin indeksler:
  - `tasks(plannedDate, completionState)`
  - `tasks(plannedDate, isArchived)`
  - `completion_logs(date)`
  - `completion_logs(entityType, entityId, date)`
  - `routines(isArchived, preferredTime)`

### Testler

- Room migration instrumentation testi.
- DAO unit/integration testleri.
- Veri kaybi senaryosu:
  - v7 DB yarat.
  - task/routine/log ekle.
  - v8'e migrate et.
  - verilerin aynen kaldigini dogrula.

### Kabul Kriterleri

- Destructive migration yok.
- Schema export var.
- En az bir migration testi yesil.
- Release build migration ile calisir.
- Veri katmani dokumani var.

### Puan Etkisi

- Veri katmani: 4.5 -> 8.0
- Security/privacy: 5.0 -> 6.5
- Production readiness: 5.8 -> 6.8

## Sprint 3 - Bildirim ve Alarm Sistemi 3.0

### Hedef

Bildirimleri Android production kosullarinda guvenilir, izinlere saygili ve reboot sonrasi devam edebilir hale getirmek.

### Kritik Mevcut Sorunlar

- Exact alarm kullanim politikasi net degil.
- `SCHEDULE_EXACT_ALARM` karari verilmemis.
- Boot sonrası reschedule receiver yok.
- App acilmadan rutin reminder'lar yeniden kurulmayabilir.
- Quiet hours senkron `runBlocking` ile okunuyor.

### Teknik Tasarim

Bildirim katmanlari:

```text
NotificationSettingsRepository
ReminderPolicy
ReminderScheduler
ReminderBootstrapReceiver
NotificationPublisher
```

Alarm politikasi:

- Gorev hatirlatmalari kullanici tarafindan belirli saat icin ayarlandigi icin exact alarm adayi.
- Exact alarm izni alinmayacaksa `setAndAllowWhileIdle` veya WorkManager fallback kullanilmali.
- Rutin hatirlatmalari dakik hassasiyette degilse inexact repeating kullanabilir.
- Daily summary ve morning planner WorkManager periodic/one-time chain ile daha guvenli hale getirilebilir.

Boot politikasi:

- Manifest'e `RECEIVE_BOOT_COMPLETED`.
- `BootCompletedReceiver`.
- Receiver sadece schedule metadata'sini okuyup alarm/work yeniden kurar.

Quiet hours:

- Notification aninda DataStore `runBlocking` yerine cache veya repository snapshot kullan.
- Receiver icinde gerekiyorsa IO coroutine ile karar ver, sonra publish et.

### Yapilacaklar

- `NotificationPublisher` merkezi servis olustur.
- `ReminderPolicy` ile notification mode, quiet hours, permission ve exact alarm kararlarini tek noktaya al.
- `BootCompletedReceiver` ekle.
- Manifest izinlerini netlestir:
  - `POST_NOTIFICATIONS`
  - `VIBRATE`
  - `RECEIVE_BOOT_COMPLETED`
  - `SCHEDULE_EXACT_ALARM` sadece gerekliyse.
- `TaskReminderScheduler` exact alarm fallback davranisi ekle.
- `RoutineReminderScheduler` tekrar eden alarm davranisini test et.
- Reminder cancel/update senaryolarini kapsa.
- Snooze davranisini test et.
- Bildirim action deep link hedeflerini dogrula.

### Testler

- Unit test:
  - quiet hours overnight range
  - notification mode off/light
  - invalid time parse
  - exact alarm fallback
- Instrumentation/manual:
  - Permission denied
  - Permission granted
  - Task reminder schedule/cancel
  - Routine reminder update
  - Snooze
  - Reboot sonrası reschedule

### Kabul Kriterleri

- Notification lint hatasi yok.
- Permission reddedilince crash yok.
- Boot sonrası schedule geri gelir.
- Quiet hours dogru calisir.
- Hatirlatma ayari kapaliyken notification gelmez.

### Puan Etkisi

- Bildirim: 4.0 -> 8.0
- Test: 4.0 -> 5.5
- Security/privacy: 6.5 -> 7.0

## Sprint 4 - Test Mimarisi ve Unit Kapsam

### Hedef

Testleri sadece mevcut davranisi yakalayan kucuk dosyalar olmaktan cikarip, domain ve veri davranislarini production regression guvencesine donusturmek.

### Mevcut Durum

- 4 unit test dosyasi var.
- Domain testleri geciyor.
- Repository testleri gercek repository entegrasyonunu kapsamiyor.
- Notification, migration, DataStore, ViewModel edge case testleri eksik.

### Teknik Tasarim

Test paketleri:

```text
app/src/test/java/com/benimgunlerim/domain/
app/src/test/java/com/benimgunlerim/data/
app/src/test/java/com/benimgunlerim/notifications/
app/src/test/java/com/benimgunlerim/ui/
app/src/androidTest/java/com/benimgunlerim/data/
app/src/androidTest/java/com/benimgunlerim/ui/
```

Gerekli test kutuphaneleri:

- `kotlinx-coroutines-test`
- `androidx.arch.core:core-testing`
- Room testing
- Turbine veya flow test helper
- Robolectric opsiyonel

### Yapilacaklar

- Test dependency'lerini version catalog'a al.
- Coroutine dispatcher injection ekle.
- Repository icin fake DAO veya in-memory Room test stratejisi kur.
- ViewModel testleri icin fake repository ve fake preferences ekle.
- `GameEngine` icin odul, level, exploit prevention testleri genislet.
- `ProgressCalculator` icin zero item, partial routine, skipped routine testleri ekle.
- `TimeText` ve date helper testlerini timezone edge case ile genislet.
- `UserPreferencesRepository` icin reward idempotency testleri ekle.
- `AchievementTracker` testleri ekle.

### Kabul Kriterleri

- Unit test sayisi anlamli sekilde artar.
- Reward exploit ve day-close duplicate XP testleri vardir.
- Repository critical path testleri vardir.
- Testler flaky olmadan tekrar calisir.

### Puan Etkisi

- Test altyapisi: 4.0 -> 7.0
- Domain mantigi: 5.5 -> 7.5
- Maintainability: 6.0 -> 7.5

## Sprint 5 - UI Test, Accessibility ve Compose Test Edilebilirligi

### Hedef

Kritik kullanici akislari cihaz/emulator uzerinde guvenilir test edilsin; UI ambiguous matcher problemleri ve accessibility eksikleri temizlensin.

### Kritik Mevcut Sorunlar

- `connectedDebugAndroidTest` 4 testten 2 fail.
- Sebep: ayni text icin birden fazla node.
- UI testleri semantic tag kullanmiyor.

### Teknik Tasarim

Test tag standardi:

```text
screen.today.root
screen.today.bottom_nav.today
screen.today.add_task.button
screen.today.task_item.{id}
screen.routines.root
screen.routines.add_routine.button
screen.settings.notifications.toggle
```

Compose helper:

```kotlin
object TestTags {
    const val TodayRoot = "screen.today.root"
    const val BottomNavToday = "bottom_nav.today"
}
```

Accessibility standardi:

- Icon-only button'larda contentDescription zorunlu.
- Checkbox state semantic olarak dogru.
- Sheet/dialog title semantic olarak okunabilir.
- Minimum touch target korunur.

### Yapilacaklar

- Bottom nav item'lara test tag ekle.
- Kritik ekran root'larina test tag ekle.
- UI testlerde text matcher yerine tag matcher kullan.
- Today, Routines, Settings, Onboarding, Day Close akislari icin test yaz.
- Empty state testleri ekle.
- Large font / font scale manuel test checklist ekle.
- Landscape veya dar ekran smoke test ekle.

### Kritik UI Akis Testleri

- Onboarding tamamla, Today ekranina gel.
- Today'de task ekle.
- Task tamamla, XP event dogrula.
- Routine ekle, tamamla.
- Day close kaydet.
- Settings'te notification mode degistir.
- Routines detay sayfasina gir/cik.

### Kabul Kriterleri

- `connectedDebugAndroidTest` yesil.
- UI testler ambiguous text matcher'a bagli degil.
- Ana aksiyonlar accessibility acisindan okunabilir.
- En az 8 kritik UI test yesil.

### Puan Etkisi

- UI: 6.5 -> 8.0
- Test: 7.0 -> 8.0

## Sprint 6 - Release Build, Signing ve Paketleme

### Hedef

APK uretmekten production release artifact uretmeye gecmek.

### Mevcut Sorunlar

- `isMinifyEnabled = false`.
- `proguard-rules.pro` bos.
- Signing stratejisi yok.
- Versioning manuel.
- Release checklist yok.

### Teknik Tasarim

Gradle build types:

```kotlin
buildTypes {
    debug { ... }
    release {
        isMinifyEnabled = true
        isShrinkResources = true
        proguardFiles(...)
    }
}
```

Signing:

- Local release signing secret repoya girmez.
- CI secret store kullanilir.
- `keystore.properties` `.gitignore` icinde kalir.

Versioning:

- `versionCode` monotonic.
- `versionName` semantic.
- Release notes `docs/release/`.

Artifact:

- Internal testing icin APK.
- Play Store icin AAB.

### Yapilacaklar

- Minify ve resource shrink'i release icin aktif et.
- Hilt/Room/Compose icin gerekli ProGuard davranisini test et.
- Release signing config dokumante et.
- `bundleRelease` kapisini ekle.
- Version bump sureci yaz.
- Play internal testing checklist ekle.
- Launcher icon monochrome uyarisini coz.
- Dependency catalog hijyenini tamamla.

### Testler

- `assembleRelease`
- `bundleRelease`
- `lintRelease`
- Release APK smoke test
- Minified build acilis testi
- Notification, DB, navigation smoke test

### Kabul Kriterleri

- Release artifact minify aktifken calisir.
- Signing bilgileri repoya girmez.
- Release checklist dokumante edilir.
- Play internal test'e yuklenebilir AAB uretilir.

### Puan Etkisi

- Release: 4.5 -> 8.5
- Security: 7.0 -> 7.8
- DevOps: 6.0 -> 7.5

## Sprint 7 - Observability, Crash ve Diagnostics

### Hedef

Production'da neyin bozuldugunu gorebilmek. Sessiz hata ve kullanici sikayetinden once teknik sinyal almak.

### Mevcut Sorunlar

- Analytics sadece `Log.d`.
- Crash reporting yok.
- Non-fatal hata raporlama yok.
- Build/version/device bilgisi eventlerde yok.

### Teknik Tasarim

Arayuzler:

```kotlin
interface AnalyticsTracker {
    fun track(event: AnalyticsEvent)
}

interface ErrorReporter {
    fun recordNonFatal(error: Throwable, context: Map<String, String> = emptyMap())
    fun setUserProperty(key: String, value: String)
}
```

Provider:

- Development: local log tracker.
- Production: Firebase Crashlytics veya secilen crash provider.
- Privacy mode: analytics kapaliyken sadece zorunlu crash sinyali veya tamamen kapali davranis netlesir.

Event standardi:

- `task_created`
- `task_completed`
- `routine_created`
- `routine_completed`
- `day_closed`
- `reminder_scheduled`
- `reminder_failed`
- `migration_failed`
- `notification_permission_denied`

### Yapilacaklar

- `ErrorReporter` arayuzu ekle.
- Development implementation ekle.
- Production crash provider karari ver.
- Analytics event naming dokumani yaz.
- Silent `runCatching` bloklarinda en az non-fatal log ekle.
- Bildirim schedule fail, parse fail, migration fail gibi kritik hatalari raporla.
- Kullanici privacy tercihi ile analytics davranisini bagla.

### Testler

- Analytics disabled iken event gonderilmez.
- ErrorReporter fake ile non-fatal cagri test edilir.
- Schedule exception non-fatal olarak yakalanir.

### Kabul Kriterleri

- Crash ve non-fatal icin merkezi arayuz var.
- Kritik background islerde hata yutulmuyor.
- Analytics event sozlugu dokumante.
- Privacy tercihi davranisa yansiyor.

### Puan Etkisi

- Observability: 3.5 -> 8.0
- Maintainability: 7.5 -> 8.0
- Production readiness: 7.5 -> 8.2

## Sprint 8 - Security, Privacy, Backup ve Data Export

### Hedef

Kullanici verisinin gizliligi, yedeklenmesi, silinmesi ve disari aktarilmasi konusunda bilincli production davranisi olusturmak.

### Mevcut Sorunlar

- `android:allowBackup="true"` bilincli policy olmadan acik.
- Veri export/import yok.
- Clear local data var ama UX ve recovery politikasi net degil.
- Analytics privacy dokumani yok.

### Teknik Tasarim

Privacy kararlari:

- Uygulama offline-first.
- Gorev/rutin/gun sonu verileri cihazda tutulur.
- Cloud sync yoksa bu net anlatilir.
- Backup aciksa hangi verilerin yedeklenecegi `dataExtractionRules` ile tanimlanir.
- Backup kapatilacaksa veri kaybi riski kullaniciya anlatilir.

Data export:

```text
export.json
|-- version
|-- exportedAt
|-- tasks
|-- routines
|-- completionLogs
|-- dailyStates
|-- preferences
```

Data delete:

- Local data clear.
- Onboarding reset.
- Export before delete onerisi.

### Yapilacaklar

- Backup policy karari ver:
  - ya `allowBackup=false`
  - ya da `dataExtractionRules` ile kontrollu backup.
- Privacy dokumani yaz.
- Settings'e data export/import altyapisi planla.
- JSON export serializer ekle.
- Import icin versioned parser tasarla.
- Clear local data icin confirmation UX ekle.
- Loglarda kullanici task/routine title basilmadigini dogrula.

### Testler

- Export JSON schema testi.
- Import backward compatibility testi.
- Clear data sonrasi DB ve DataStore state testi.
- Analytics disabled privacy testi.

### Kabul Kriterleri

- Backup karari bilincli ve dokumante.
- Kullanici verisi export edilebilir.
- Clear data geri donulmez oldugu UI'da net.
- PII sayilabilecek task/routine title'lar analytics'e gitmez.

### Puan Etkisi

- Security/privacy: 5.0 -> 8.5
- Veri: 8.0 -> 8.5
- Settings/UX: 6.5 -> 8.0

## Sprint 9 - Stabilizasyon, Performans ve Release Candidate

### Hedef

Tum teknik kapilari yesile alip uygulamayi release candidate seviyesine getirmek.

### Kapsam

- Crash-free smoke test.
- Performance pass.
- Large data test.
- Battery/notification davranisi.
- Accessibility pass.
- Regression checklist.
- Release candidate build.

### Teknik Test Matrisi

| Senaryo | Beklenen |
|---|---|
| Ilk kurulum | Onboarding acilir |
| Onboarding tamamla | Today ekranina gidilir |
| 100 task listele | UI akici kalir |
| 50 routine listele | UI akici kalir |
| Task reminder schedule | Alarm kurulur |
| Permission denied | Crash olmaz |
| DB upgrade | Veri kaybi olmaz |
| App reboot sonrası | Reminder'lar geri kurulur |
| Day close tekrar kaydet | Duplicate XP verilmez |
| Offline kullanim | Tum ana akislar calisir |

### Yapilacaklar

- Profiling ile recomposition hotspot kontrolu.
- Lazy list key'lerini dogrula.
- Uzun metin ve font scale testleri.
- Edge-to-edge ve system bar davranisini kontrol et.
- Release candidate branch/tag sureci kur.
- RC checklist dokumani ekle.
- Known issues listesi olustur.

### Kabul Kriterleri

- Unit test yesil.
- Lint debug/release yesil.
- Release assemble/bundle yesil.
- UI smoke test yesil.
- Migration test yesil.
- Notification manual checklist tamam.
- Release notes hazir.
- Genel production puani en az 9.0.

### Puan Etkisi

- Genel production readiness: 8.2 -> 9.0
- Test: 8.0 -> 9.0
- Release: 8.5 -> 9.0
- UI: 8.0 -> 9.0

## 5. 9/10 Definition of Done

Her sprint icin ortak tamamlanma tanimi:

- Kod format ve lint temiz.
- Unit testler guncel.
- Yeni davranis icin test var.
- Public/kritik davranis dokumante.
- Eski kullanici verisini bozan degisiklik yok.
- Release build etkisi dusunulmus.
- Privacy/security etkisi degerlendirilmis.
- UI degisikliklerinde accessibility ve test tag dusunulmus.

Production release icin final DoD:

- `testDebugUnitTest`: pass
- `lintDebug`: pass
- `lintRelease`: pass
- `assembleRelease`: pass
- `bundleRelease`: pass
- `connectedDebugAndroidTest`: pass
- Migration tests: pass
- Manual notification checklist: pass
- Manual data privacy checklist: pass
- Release notes: ready
- Known issues: documented

## 6. Birimleri 9/10'a Tasima Plani

### Mimari - 9/10

Gerekli durum:

- UI, domain, data, notification sinirlari net.
- Repository buyumesi kontrol altinda.
- Scheduler, publisher, policy ayrimi var.
- Dispatcher ve clock injection var.
- Test fake'leri kolay yaziliyor.

Somut isler:

- `ClockProvider` veya `TimeProvider` ekle.
- `DispatcherProvider` ekle.
- Notification policy ayir.
- Data export/import ayri servis.
- Repository fonksiyonlarini feature bazli servislerle bol.

### Veri Katmani - 9/10

Gerekli durum:

- Destructive migration yok.
- Schema export var.
- Migration testleri var.
- Indeksler sorgularla uyumlu.
- Veri export/import versioned.

Somut isler:

- Room migration altyapisi.
- DAO testleri.
- Index annotation'lari.
- Export JSON schema.
- Clear data testleri.

### Domain Mantigi - 9/10

Gerekli durum:

- XP, reward, streak, day close idempotent.
- Task/routine ayrimi net.
- Date/time edge case testli.
- Duplicate reward exploit kapali.

Somut isler:

- `RewardService` ayir.
- `DayCloseService` ayir.
- `StreakCalculator` domain sinifina tasinabilir.
- Tum hesaplamalar unit testli.

### UI - 9/10

Gerekli durum:

- Ana akislarda test tag var.
- Accessibility temel seviyede dogru.
- Empty/loading/error state'ler var.
- Uzun metin ve font scale dayanikli.
- Encoding bozuklugu yok.

Somut isler:

- `TestTags.kt`.
- Ortak empty/error/loading component.
- `strings.xml` merkezi metinler.
- UI smoke suite.

### Bildirim - 9/10

Gerekli durum:

- Permission guvenli.
- Exact alarm policy net.
- Boot sonrasi reschedule var.
- Quiet hours guvenli.
- Snooze testli.

Somut isler:

- `NotificationPublisher`.
- `ReminderPolicy`.
- `BootCompletedReceiver`.
- Schedule/cancel/update testleri.

### Test - 9/10

Gerekli durum:

- Unit, integration, UI ve migration testleri var.
- Testler deterministic.
- CI'da kosuyor.
- Kritik regression akislari kapsaniyor.

Somut isler:

- Coroutine test altyapisi.
- In-memory Room test.
- Compose UI test tag.
- Migration test.
- Release smoke checklist.

### Release - 9/10

Gerekli durum:

- Minified release build calisir.
- AAB uretimi var.
- Signing dokumante.
- Versioning kontrollu.
- Release notes var.

Somut isler:

- `isMinifyEnabled = true`.
- `isShrinkResources = true`.
- `bundleRelease` CI kapisi.
- Signing secret policy.
- Release checklist.

### Security / Privacy - 9/10

Gerekli durum:

- Backup policy bilincli.
- Kullanici verisi export/delete edilebilir.
- Analytics privacy uyumlu.
- Loglarda hassas veri yok.

Somut isler:

- `allowBackup` karari.
- `dataExtractionRules`.
- Privacy dokumani.
- Export/delete UX.

### Observability - 9/10

Gerekli durum:

- Crash ve non-fatal hata gorulebilir.
- Event sozlugu var.
- Analytics opt-out calisir.
- Background hata yutmaz.

Somut isler:

- `ErrorReporter`.
- Crash provider entegrasyonu.
- Non-fatal schedule/migration/report noktalarini ekle.
- Event naming dokumani.

### DevOps - 9/10

Gerekli durum:

- CI zorunlu kalite kapilari.
- Release workflow.
- Dependency update stratejisi.
- Dokumante lokal kurulum.

Somut isler:

- GitHub Actions veya esdegeri.
- PR template.
- Issue template.
- Dependabot/Renovate.
- `docs/release` ve `docs/testing`.

## 7. Risk Listesi

| Risk | Etki | Cozum |
|---|---|---|
| Migration gec yazilirsa veri kaybi | Cok yuksek | Sprint 2 onde |
| Notification policy yanlis secilirse reminder calismaz | Yuksek | Sprint 3 manual matrix |
| Minify sonradan acilirsa release oncesi kirilir | Yuksek | Sprint 6'da erken ac |
| UI testler text'e bagli kalirsa flaky olur | Orta | Test tag standardi |
| Analytics privacy net degilse guven sorunu | Orta | Sprint 8 privacy pass |
| Encoding tekrar bozulursa kalite dusuk gorunur | Orta | strings.xml ve UTF-8 kontrol |
| CI yoksa kalite geriler | Yuksek | Sprint 0 |

## 8. Onerilen Zamanlama

1 haftalik sprint varsayimi:

| Hafta | Sprint |
|---|---|
| 1 | Sprint 0 + Sprint 1 |
| 2 | Sprint 2 |
| 3 | Sprint 3 |
| 4 | Sprint 4 |
| 5 | Sprint 5 |
| 6 | Sprint 6 |
| 7 | Sprint 7 |
| 8 | Sprint 8 |
| 9 | Sprint 9 |

Tek gelistirici icin daha gercekci plan:

| Hafta | Odak |
|---|---|
| 1 | Repo, lint, encoding |
| 2 | Migration |
| 3 | Notification |
| 4 | Unit test |
| 5 | UI test |
| 6 | Release build |
| 7 | Observability |
| 8 | Privacy/export |
| 9-10 | Stabilizasyon |

## 9. Final Release Checklist

### Build

- [ ] `testDebugUnitTest` pass
- [ ] `lintDebug` pass
- [ ] `lintRelease` pass
- [ ] `assembleRelease` pass
- [ ] `bundleRelease` pass
- [ ] Minified release smoke test pass

### Data

- [ ] Destructive migration kapali
- [ ] Schema export acik
- [ ] Migration test pass
- [ ] Export JSON test pass
- [ ] Clear data test pass

### Notifications

- [ ] Permission denied crash yok
- [ ] Permission granted notification calisir
- [ ] Task reminder schedule/cancel calisir
- [ ] Routine reminder schedule/update calisir
- [ ] Snooze calisir
- [ ] Boot sonrasi reschedule calisir
- [ ] Quiet hours calisir

### UI

- [ ] Onboarding smoke pass
- [ ] Today smoke pass
- [ ] Routines smoke pass
- [ ] Plan smoke pass
- [ ] Progress smoke pass
- [ ] Settings smoke pass
- [ ] Font scale smoke pass
- [ ] Dark/system theme smoke pass

### Security / Privacy

- [ ] Backup policy kararli
- [ ] Privacy dokumani hazir
- [ ] Analytics opt-out calisir
- [ ] Loglarda hassas veri yok
- [ ] Data delete UX net

### Release

- [ ] Version code arttirildi
- [ ] Version name guncel
- [ ] Release notes hazir
- [ ] Known issues hazir
- [ ] Signed AAB hazir
- [ ] Internal testing yukleme hazir

## 10. Nihai Kabul

Bu plan tamamlandiginda beklenen sonuc:

| Alan | Hedef |
|---|---:|
| Genel production readiness | 9.0 |
| Mimari | 9.0 |
| Veri | 9.0 |
| Domain | 9.0 |
| UI | 9.0 |
| Bildirim | 9.0 |
| Test | 9.0 |
| Release | 9.0 |
| Security/privacy | 9.0 |
| Observability | 9.0 |
| DevOps | 9.0 |

Bu seviyede proje sadece calisan bir Android uygulamasi degil; veri guvenligi, release sureci, test kapsami, production hata gorunurlugu ve kullanici guveni acisindan da yayinlanabilir bir urun olur.
