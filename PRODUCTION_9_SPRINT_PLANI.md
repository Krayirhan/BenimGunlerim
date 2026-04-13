# BenimGünlerim — Production 9/10 Sprint Planı

Bu plan, teknik değerlendirmede tespit edilen tüm kritik ve yüksek öncelikli sorunları gidererek uygulamayı
**Play Store'a gönderilebilir, sağlam ve güvenilir bir 9/10 seviyesine** taşımak için tasarlanmıştır.

**Başlangıç durumu: 6.7 / 10**  
**Hedef: 9.0 / 10**

---

## Blocker Özeti (Ne Eksik?)

| # | Sorun | Önem | Sprint |
|---|-------|------|--------|
| B1 | `fallbackToDestructiveMigration` — veri kaybı | 🔴 | 13 |
| B2 | `exportSchema = false` — migration güvenli değil | 🔴 | 13 |
| B3 | `isMinifyEnabled = false` — release şifreli değil | 🔴 | 14 |
| B4 | ProGuard kuralları boş | 🔴 | 14 |
| B5 | Signing config yok — yayınlanamaz | 🔴 | 14 |
| B6 | `SCHEDULE_EXACT_ALARM` izni eksik — API31+ crash | 🔴 | 15 |
| B7 | `runBlocking` BroadcastReceiver'da — ANR | 🔴 | 15 |
| B8 | `RECEIVE_BOOT_COMPLETED` + BootReceiver yok | 🟠 | 15 |
| B9 | `observeAllCompletionLogs()` tüm tabloyu RAM'e çekiyor | 🟠 | 16 |
| B10 | `android:allowBackup="true"` — ADB ile DB çekilebilir | 🟠 | 17 |
| B11 | `rewardedEvents` string şişme — DataStore patlar | 🟠 | 18 |
| B12 | Test coverage < %10 — hiçbir ViewModel test yok | 🟠 | 19-20 |
| B13 | AlarmManager repeating — pil optimizasyonunda iptal | 🟠 | 21 |
| B14 | UseCase katmanı yok — TodayVM 350+ satır | 🟡 | 22 |
| B15 | LazyColumn item key eksik — gereksiz recomposition | 🟡 | 23 |
| B16 | Koyu mod yok — accessibility | 🟡 | 23 |
| B17 | Room TypeConverter eksik — LocalDate String | 🟡 | 18 |
| B18 | Baseline Profile yok — cold start yavaş | 🟡 | 24 |
| B19 | `.aab` / R8 full mode yok — Play Store hazırlık | 🔴 | 14 |
| B20 | Hilt test runner yok — androidTest Hilt inject edemez | 🟠 | 19 |

---

## Sprint 13 — Room Migration & Schema Güvenliği

### Hedef
Kullanıcı verisi uygulama güncellemesinde silinmesin.
Room şema geçmişi takip edilsin, gelecekteki migration'lar güvenle yazılabilsin.

### Neden Kritik?
`fallbackToDestructiveMigration()` ibaresi şu an her versiyon artışında tüm veritabanını silip yeniden oluşturuyor.
Play Store'da yayınlanmış bir uygulamada bu, kullanıcıların tüm görev ve rutin geçmişini kaybetmesi demektir.

### Teknik Görevler

#### 13.1 — `exportSchema = true` + Şema Klasörü

`app/build.gradle.kts` içine:
```kotlin
android {
    // ...
    defaultConfig {
        // Şema dosyaları src/main/assets/schemas/ altında tutulur
        javaCompileOptions {
            annotationProcessorOptions {
                arguments += mapOf("room.schemaLocation" to "$projectDir/schemas")
            }
        }
    }
}
```

`AppDatabase.kt` içinde:
```kotlin
@Database(
    entities = [...],
    version = 7,
    exportSchema = true,   // false → true
)
```

KSP için `app/build.gradle.kts` içine:
```kotlin
ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
    arg("room.incremental", "true")
    arg("room.expandProjection", "true")
}
```

**Sonuç:** `app/schemas/com.benimgunlerim.data.local.AppDatabase/7.json` oluşur, commit edilir.

#### 13.2 — Migration 6 → 7 Yazılması

v6'dan v7'ye geçişte `subtasks` tablosu eklendi. Migration SQL:

`data/local/Migrations.kt` dosyası oluştur:
```kotlin
package com.benimgunlerim.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `subtasks` (
                `id` TEXT NOT NULL,
                `taskId` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `isCompleted` INTEGER NOT NULL DEFAULT 0,
                `sortOrder` INTEGER NOT NULL DEFAULT 0,
                `createdAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`) ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_subtasks_taskId` ON `subtasks` (`taskId`)")
    }
}
```

#### 13.3 — `AppModule.kt` Güncelleme

```kotlin
fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
    Room.databaseBuilder(context, AppDatabase::class.java, "benim_gunlerim.db")
        .addMigrations(MIGRATION_6_7)   // fallbackToDestructiveMigration() KALDIRILDI
        .build()
```

#### 13.4 — Gelecek Migration'lar İçin Kural

`docs/MIGRATION_RULES.md` dosyası (tek referans noktası):
- Her `AppDatabase.version` artışında `Migrations.kt` içine migration eklenir.
- Şema JSON'u commit edilir.
- `fallbackToDestructiveMigration()` bir daha kullanılmaz.

### Kabul Kriterleri
- [ ] `app/schemas/*/7.json` dosyası mevcut ve commit edilmiş
- [ ] `AppModule` içinde `fallbackToDestructiveMigration()` yok
- [ ] `MIGRATION_6_7` testi geçiyor (`MigrationTest`)
- [ ] `compileDebugKotlin` başarılı

---

## Sprint 14 — Release Build, ProGuard & Signing

### Hedef
APK/AAB Play Store'a gönderilebilir hale gelsin.
ProGuard ile kod küçültme ve karıştırma aktif olsun.
Signing config hazır olsun.

### Neden Kritik?
- `isMinifyEnabled = false` ile release APK %40-50 şişik.
- Sınıf isimleri tersine mühendisliğe açık.
- Signing config olmadan yayınlanamaz.
- `.apk` yerine `.aab` Play Store zorunluluğu (2021'den beri).

### Teknik Görevler

#### 14.1 — `app/build.gradle.kts` Release Config

```kotlin
android {
    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
    }
}
```

#### 14.2 — `app/proguard-rules.pro` (Eksiksiz)

```proguard
# ── Room Entity'leri ──────────────────────────────────────────────────────────
-keep class com.benimgunlerim.data.local.entity.** { *; }

# ── Hilt / Dagger ────────────────────────────────────────────────────────────
-dontwarn dagger.hilt.**
-keep class dagger.hilt.** { *; }
-keep class * extends dagger.hilt.android.HiltAndroidApp { *; }
-keep @dagger.hilt.android.lifecycle.HiltViewModel class * { *; }
-keep @dagger.hilt.InstallIn class * { *; }

# ── Kotlin Serialization / Data Classes ──────────────────────────────────────
-keepclassmembers class * {
    @com.google.gson.annotations.SerializedName <fields>;
}
-keep class kotlin.Metadata { *; }
-keepclassmembers class ** {
    ** component1();
    ** component2();
    ** copy(...);
}

# ── Coroutines ────────────────────────────────────────────────────────────────
-keepnames class kotlinx.coroutines.internal.MainDispatcherFactory {}
-keepnames class kotlinx.coroutines.CoroutineExceptionHandler {}
-keepclassmembernames class kotlinx.** {
    volatile <fields>;
}

# ── DataStore ─────────────────────────────────────────────────────────────────
-keepclassmembers class * extends com.google.protobuf.GeneratedMessageLite { *; }

# ── Compose ───────────────────────────────────────────────────────────────────
-keep class androidx.compose.** { *; }
-keepclassmembers class * {
    @androidx.compose.runtime.Composable *;
}

# ── Navigation Compose ────────────────────────────────────────────────────────
-keepnames class androidx.navigation.** { *; }

# ── Notification / BroadcastReceiver ─────────────────────────────────────────
-keep class com.benimgunlerim.notifications.** { *; }
-keep class com.benimgunlerim.MainActivity { *; }

# ── R8 Stack Trace Okunabilirliği ────────────────────────────────────────────
-keepattributes SourceFile,LineNumberTable
-renamesourcefileattribute SourceFile

# ── Enum'lar ──────────────────────────────────────────────────────────────────
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
```

#### 14.3 — Signing Config (`app/build.gradle.kts`)

```kotlin
android {
    signingConfigs {
        create("release") {
            // Değerler local.properties'ten veya CI ortam değişkenlerinden okunur
            storeFile = file(System.getenv("KEYSTORE_PATH") ?: project.findProperty("KEYSTORE_PATH").toString())
            storePassword = System.getenv("KEYSTORE_PASSWORD") ?: project.findProperty("KEYSTORE_PASSWORD").toString()
            keyAlias = System.getenv("KEY_ALIAS") ?: project.findProperty("KEY_ALIAS").toString()
            keyPassword = System.getenv("KEY_PASSWORD") ?: project.findProperty("KEY_PASSWORD").toString()
        }
    }
    buildTypes {
        release {
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = true
            isShrinkResources = true
            // ...
        }
    }
}
```

`local.properties` (git'e commit edilmez):
```properties
KEYSTORE_PATH=../keystore/benimgunlerim-release.jks
KEYSTORE_PASSWORD=...
KEY_ALIAS=benimgunlerim
KEY_PASSWORD=...
```

#### 14.4 — R8 Full Mode

`gradle.properties`:
```properties
android.enableR8.fullMode=true
```

#### 14.5 — App Bundle Yapılandırması

`app/build.gradle.kts`:
```kotlin
android {
    bundle {
        language { enableSplit = true }
        density { enableSplit = true }
        abi { enableSplit = true }
    }
}
```

Release için `./gradlew bundleRelease`.

#### 14.6 — Keystore Oluşturma Komutu

```bash
keytool -genkey -v \
  -keystore benimgunlerim-release.jks \
  -alias benimgunlerim \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

### Kabul Kriterleri
- [ ] `./gradlew bundleRelease` başarılı
- [ ] `.aab` dosyası oluşuyor
- [ ] `apkanalyzer` ile APK boyutu debug'dan %30+ küçük
- [ ] ProGuard mapping dosyası `build/outputs/mapping/release/` içinde
- [ ] `local.properties` `.gitignore`'da

---

## Sprint 15 — Bildirim Sistemi Hardening

### Hedef
- API 31+ cihazlarda crash olan `setExactAndAllowWhileIdle` düzeltilsin.
- ANR riski taşıyan `runBlocking` BroadcastReceiver'dan temizlensin.
- Cihaz yeniden başlatıldığında alarmlar otomatik yeniden kurulsun.

### Teknik Görevler

#### 15.1 — `SCHEDULE_EXACT_ALARM` İzni

`AndroidManifest.xml`:
```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />
<uses-permission android:name="android.permission.SCHEDULE_EXACT_ALARM"
    android:maxSdkVersion="32" />
<!-- API 33+ için kullanıcıdan istenecek -->
<uses-permission android:name="android.permission.USE_EXACT_ALARM" />
```

`TaskReminderScheduler.kt` içinde exact alarm izni kontrolü:
```kotlin
fun schedule(taskId: String, taskTitle: String, date: LocalDate, time: LocalTime) {
    val triggerAt = date.atTime(time).atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()
    if (triggerAt <= System.currentTimeMillis()) return
    val pendingIntent = taskPendingIntent(taskId, taskTitle)
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        if (alarmManager.canScheduleExactAlarms()) {
            alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        } else {
            // Kullanıcı izni vermemişse inexact fallback
            alarmManager.set(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
        }
    } else {
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAt, pendingIntent)
    }
}
```

#### 15.2 — `isInQuietHours()` ANR Düzeltmesi

`NotificationHelper.kt` içindeki `runBlocking` kaldırılır.
Yerine **GoAsync pattern** + DataStore:

```kotlin
// NotificationHelper.kt — runBlocking tamamen kaldırılır

// ── Quiet Hours async yardımcısı ──────────────────────────────────────────────
// Quiet hours DataStore'dan her receiver'da direkt okunmak yerine
// SharedPreferences üzerinden cache'lenir (BroadcastReceiver için güvenli)

internal fun Context.isInQuietHoursSync(): Boolean {
    // SharedPreferences okuma — main thread'de güvenli, I/O yok
    val sp = getSharedPreferences("quiet_hours_cache", Context.MODE_PRIVATE)
    val enabled = sp.getBoolean("enabled", false)
    if (!enabled) return false
    val startStr = sp.getString("start", "22:00") ?: "22:00"
    val endStr = sp.getString("end", "07:00") ?: "07:00"
    return try {
        val fmt = DateTimeFormatter.ofPattern("HH:mm")
        val now = LocalTime.now()
        val start = LocalTime.parse(startStr, fmt)
        val end = LocalTime.parse(endStr, fmt)
        if (start.isBefore(end)) now in start..end else now >= start || now <= end
    } catch (_: Exception) { false }
}
```

`SettingsViewModel.kt` içinde quiet hours değiştiğinde cache güncellenir:
```kotlin
fun setQuietHoursEnabled(enabled: Boolean) {
    viewModelScope.launch {
        preferencesRepository.setQuietHoursEnabled(enabled)
        updateQuietHoursCache()
    }
}

private fun updateQuietHoursCache() {
    val prefs = preferences.value
    // applicationContext.getSharedPreferences("quiet_hours_cache", Context.MODE_PRIVATE).edit {
    //     putBoolean("enabled", prefs.quietHoursEnabled)
    //     putString("start", prefs.quietHoursStart)
    //     putString("end", prefs.quietHoursEnd)
    // }
}
```

`NotificationHelper.kt` içindeki tüm `isInQuietHours()` çağrıları
`isInQuietHoursSync()` olarak güncellenir.

#### 15.3 — BootReceiver + Manifest Kaydı

`notifications/BootReceiver.kt` oluştur:
```kotlin
package com.benimgunlerim.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.RoutineDao
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var routineDao: RoutineDao
    @Inject lateinit var routineReminderScheduler: RoutineReminderScheduler
    @Inject lateinit var dailySummaryScheduler: DailySummaryScheduler
    @Inject lateinit var morningPlannerScheduler: MorningPlannerScheduler
    @Inject lateinit var userPreferencesRepository: UserPreferencesRepository

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED &&
            intent.action != "android.intent.action.QUICKBOOT_POWERON"
        ) return

        val pending = goAsync()
        CoroutineScope(Dispatchers.IO).launch {
            try {
                routineDao.getActiveWithReminder().forEach { routine ->
                    routineReminderScheduler.schedule(routine)
                }
                val prefs = userPreferencesRepository.preferences.first()
                if (prefs.notificationMode != "off") {
                    val time = runCatching {
                        java.time.LocalTime.parse(prefs.dailySummaryTime)
                    }.getOrDefault(java.time.LocalTime.of(21, 0))
                    dailySummaryScheduler.schedule(time)
                }
                if (prefs.morningPlannerEnabled) {
                    val mTime = runCatching {
                        java.time.LocalTime.parse(prefs.morningPlannerTime)
                    }.getOrDefault(java.time.LocalTime.of(8, 0))
                    morningPlannerScheduler.schedule(mTime)
                }
            } finally {
                pending.finish()
            }
        }
    }
}
```

`AndroidManifest.xml` güncellemesi:
```xml
<uses-permission android:name="android.permission.RECEIVE_BOOT_COMPLETED" />

<!-- application içine -->
<receiver
    android:name=".notifications.BootReceiver"
    android:exported="true">
    <intent-filter>
        <action android:name="android.intent.action.BOOT_COMPLETED" />
        <action android:name="android.intent.action.QUICKBOOT_POWERON" />
    </intent-filter>
</receiver>
```

#### 15.4 — Bildirim İkonu Düzeltmesi

`ic_launcher_foreground` bildirim ikonu olarak kullanılamaz (renkli icon).
Yeni `drawable/ic_notification.xml` oluştur — beyaz, transparan arka plan:
```xml
<!-- 24x24dp beyaz vektör, basit checkmark veya nokta -->
<vector xmlns:android="http://schemas.android.com/apk/res/android"
    android:width="24dp"
    android:height="24dp"
    android:viewportWidth="24"
    android:viewportHeight="24">
    <path android:fillColor="@android:color/white"
        android:pathData="M9,16.17L4.83,12l-1.42,1.41L9,19 21,7l-1.41,-1.41L9,16.17z"/>
</vector>
```

`NotificationHelper.kt` içinde:
```kotlin
.setSmallIcon(R.drawable.ic_notification)  // ic_launcher_foreground yerine
```

### Kabul Kriterleri
- [ ] API 31 emülatöründe `setExactAndAllowWhileIdle` crash yok
- [ ] `isInQuietHoursSync()` BroadcastReceiver içinde `runBlocking` yok
- [ ] Emülatör reboot'undan sonra routine alarmları yeniden kuruluyor
- [ ] `RECEIVE_BOOT_COMPLETED` manifest'te
- [ ] Bildirim ikonu beyaz ve transparan

---

## Sprint 16 — Bellek & Sorgu Performansı

### Hedef
Tüm completion log'larını RAM'e çeken sorgu düzeltilsin.
LazyColumn item key'leri eklenerek gereksiz recomposition engellenmesi.
Flow'larda `distinctUntilChanged` eklenmesi.

### Teknik Görevler

#### 16.1 — CompletionLogDao Yeni Sorgu

`CompletionLogDao.kt` içine:
```kotlin
@Query("""
    SELECT * FROM completion_logs 
    WHERE entityType = 'routine' AND entityId = :routineId 
    ORDER BY date DESC 
    LIMIT :limit
""")
fun observeForRoutine(routineId: String, limit: Int = 90): Flow<List<CompletionLogEntity>>
```

#### 16.2 — RoutineDetailViewModel Sorgu Optimizasyonu

`RoutineDetailViewModel.kt` içinde:
```kotlin
// ❌ ÖNCE — tüm tabloyu RAM'e çekiyor
repository.observeAllCompletionLogs()

// ✅ SONRA — sadece bu routine'in logları
completionLogDao.observeForRoutine(routineId, limit = 90)
```

ViewModel içinde combine değişir:
```kotlin
val uiState: StateFlow<RoutineDetailUiState> = combine(
    repository.observeRoutineById(routineId),   // tek entity
    completionLogDao.observeForRoutine(routineId, 90),
) { routine, routineLogs ->
    // ...
}
```

`BenimGunlerimRepository.kt` içine `observeRoutineById` eklenir:
```kotlin
fun observeRoutineById(id: String): Flow<RoutineEntity?> =
    routineDao.observeById(id)
```

`RoutineDao.kt` içine:
```kotlin
@Query("SELECT * FROM routines WHERE id = :id AND isArchived = 0 LIMIT 1")
fun observeById(id: String): Flow<RoutineEntity?>
```

#### 16.3 — `distinctUntilChanged` Eklemesi

`BenimGunlerimRepository.kt` içindeki kritik flow'lar:
```kotlin
fun observeTasks(date: LocalDate): Flow<List<TaskEntity>> =
    taskDao.observeByDate(date.toString()).distinctUntilChanged()

fun observeActiveRoutines(): Flow<List<RoutineEntity>> =
    routineDao.observeActive().distinctUntilChanged()

fun observeCompletionLogs(date: LocalDate): Flow<List<CompletionLogEntity>> =
    completionLogDao.observeByDate(date.toString()).distinctUntilChanged()
```

#### 16.4 — LazyColumn Item Key'leri

`TodayScreen.kt`:
```kotlin
// Görev listesi
items(state.tasks, key = { it.id }) { task ->
    SwipeableTaskRow(task = task, ...)
}

// Rutin listesi
items(state.routines, key = { it.id }) { routine ->
    RoutineRowCard(routine = routine, ...)
}
```

`RoutinesScreen.kt`:
```kotlin
itemsIndexed(routines, key = { _, item -> item.routine.id }) { _, item ->
    RoutineItemCard(...)
}
```

#### 16.5 — ProgressScreen Sorgu Limitleri

`ProgressViewModel.kt` içinde gereksiz `observeAllCompletionLogs()` varsa:
```kotlin
// Son 30 günün verisi yeterli
repository.observeCompletionLogsBetween(
    from = LocalDate.now().minusDays(30),
    to = LocalDate.now()
)
```

### Kabul Kriterleri
- [ ] `RoutineDetailViewModel` artık `observeAllCompletionLogs()` çağırmıyor
- [ ] Profiler'da `LazyColumn` scroll sırasında composition sayısı azaldı
- [ ] Tüm Liste composable'larında `key` lambda tanımlı
- [ ] Ana flow'larda `distinctUntilChanged` mevcut

---

## Sprint 17 — Güvenlik & Veri Gizliliği

### Hedef
ADB backup ile uygulama verisinin dışarı çıkması engellenmesi.
Kritik kullanıcı gizlilik ayarlarının korunması.

### Teknik Görevler

#### 17.1 — ADB Backup Kapatma

`AndroidManifest.xml`:
```xml
<application
    android:allowBackup="false"
    android:dataExtractionRules="@xml/backup_rules"
    android:fullBackupContent="@xml/backup_rules"
    ...>
```

`res/xml/backup_rules.xml` oluştur:
```xml
<?xml version="1.0" encoding="utf-8"?>
<full-backup-content>
    <!-- Room veritabanı hariç tutulur -->
    <exclude domain="database" path="benim_gunlerim.db" />
    <exclude domain="database" path="benim_gunlerim.db-shm" />
    <exclude domain="database" path="benim_gunlerim.db-wal" />
    <!-- DataStore hariç tutulur (şifresiz kystore) -->
    <exclude domain="file" path="datastore/user_preferences.preferences_pb" />
</full-backup-content>
```

Android 12+ için `res/xml/data_extraction_rules.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<data-extraction-rules>
    <cloud-backup>
        <exclude domain="database" path="." />
        <exclude domain="file" path="datastore/" />
    </cloud-backup>
    <device-transfer>
        <exclude domain="database" path="." />
    </device-transfer>
</data-extraction-rules>
```

#### 17.2 — Network Security Config

Uygulama ağ trafiği olmasa da best practice olarak:

`res/xml/network_security_config.xml`:
```xml
<?xml version="1.0" encoding="utf-8"?>
<network-security-config>
    <base-config cleartextTrafficPermitted="false">
        <trust-anchors>
            <certificates src="system" />
        </trust-anchors>
    </base-config>
</network-security-config>
```

`AndroidManifest.xml`:
```xml
<application
    android:networkSecurityConfig="@xml/network_security_config"
    ...>
```

#### 17.3 — `android:exported` Kontrol

Tüm `Activity` bileşenlerinin explicit `android:exported` değerlerine sahip olduğu doğrulanır:
```xml
<activity
    android:name=".MainActivity"
    android:exported="true"          <!-- explicitly set -->
    android:windowSoftInputMode="adjustResize">
```

#### 17.4 — Intent Extra Güvenliği

`SnoozeReceiver` ve diğer receiver'larda gelen intent doğrulama:
```kotlin
override fun onReceive(context: Context, intent: Intent) {
    // Action kontrolü
    if (intent.action != NotificationConstants.ACTION_SNOOZE) return
    // Extra null safety
    val type = intent.getStringExtra(NotificationConstants.EXTRA_SNOOZE_TYPE)
        ?.takeIf { it in setOf("routine", "task", "daily", "morning") } ?: return
    val id = intent.getStringExtra(NotificationConstants.EXTRA_SNOOZE_ID)
        ?.takeIf { it.isNotBlank() } ?: return
    // ...
}
```

### Kabul Kriterleri
- [ ] `adb backup -apk com.benimgunlerim` boş dosya döner
- [ ] `network_security_config.xml` cleartext trafiği engelliyor
- [ ] Tüm receiver'larda `android:exported="false"`
- [ ] Intent extra'ları whitelist ile doğrulanıyor

---

## Sprint 18 — Veri Modeli Temizliği & `rewardedEvents` Refactor

### Hedef
DataStore'daki şişen `rewardedEvents` string'i Room'a taşı.
Room TypeConverter ekle. ColumnInfo annotation ekle.

### Teknik Görevler

#### 18.1 — `RewardEventEntity` Oluşturma

`data/local/entity/RewardEventEntity.kt`:
```kotlin
package com.benimgunlerim.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "reward_events",
    indices = [Index(value = ["event_key"], unique = true)],
)
data class RewardEventEntity(
    @PrimaryKey
    @ColumnInfo(name = "event_key")
    val eventKey: String,

    @ColumnInfo(name = "xp")
    val xp: Int = 0,

    @ColumnInfo(name = "gold")
    val gold: Int = 0,

    @ColumnInfo(name = "created_at")
    val createdAt: Long = System.currentTimeMillis(),
)
```

#### 18.2 — `RewardEventDao` Oluşturma

`data/local/RewardEventDao.kt`:
```kotlin
package com.benimgunlerim.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benimgunlerim.data.local.entity.RewardEventEntity

@Dao
interface RewardEventDao {
    @Query("SELECT COUNT(*) FROM reward_events WHERE event_key = :key")
    suspend fun exists(key: String): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(event: RewardEventEntity): Long

    // Günlük temizlik — 90 günden eski event'ler silinir
    @Query("DELETE FROM reward_events WHERE created_at < :threshold")
    suspend fun deleteOlderThan(threshold: Long)

    @Query("DELETE FROM reward_events")
    suspend fun deleteAll()
}
```

#### 18.3 — AppDatabase Güncellemesi

```kotlin
@Database(
    entities = [
        TaskEntity::class,
        RoutineEntity::class,
        CompletionLogEntity::class,
        DailyStateEntity::class,
        AchievementEntity::class,
        SubTaskEntity::class,
        RewardEventEntity::class,   // YENİ
    ],
    version = 8,  // 7 → 8
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    // ...
    abstract fun rewardEventDao(): RewardEventDao
}
```

#### 18.4 — Migration 7 → 8

`Migrations.kt` içine:
```kotlin
val MIGRATION_7_8 = object : Migration(7, 8) {
    override fun migrate(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `reward_events` (
                `event_key` TEXT NOT NULL,
                `xp` INTEGER NOT NULL DEFAULT 0,
                `gold` INTEGER NOT NULL DEFAULT 0,
                `created_at` INTEGER NOT NULL,
                PRIMARY KEY(`event_key`)
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE UNIQUE INDEX IF NOT EXISTS `index_reward_events_event_key` ON `reward_events` (`event_key`)"
        )
    }
}
```

#### 18.5 — `grantRewardOnce` Refactor

`UserPreferencesRepository.kt` içindeki `grantRewardOnce` kaldırılır.

Yeni implementasyon `BenimGunlerimRepository.kt` içinde:
```kotlin
suspend fun grantRewardOnce(
    eventKey: String,
    xp: Int,
    gold: Int,
    happinessDelta: Int,
): Boolean {
    val inserted = rewardEventDao.insertIfAbsent(
        RewardEventEntity(eventKey = eventKey, xp = xp, gold = gold)
    )
    if (inserted == -1L) return false  // zaten var, grant yok
    prefsRepository.addXpAndGold(xp, gold, happinessDelta)
    return true
}
```

`UserPreferencesRepository.kt` içindeki `rewardedEvents` key ve field kaldırılır.

#### 18.6 — Room TypeConverter (LocalDate)

`data/local/Converters.kt`:
```kotlin
package com.benimgunlerim.data.local

import androidx.room.TypeConverter
import java.time.LocalDate

class Converters {
    @TypeConverter
    fun fromLocalDate(date: LocalDate?): String? = date?.toString()

    @TypeConverter
    fun toLocalDate(value: String?): LocalDate? =
        value?.let { runCatching { LocalDate.parse(it) }.getOrNull() }
}
```

`AppDatabase.kt`:
```kotlin
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() { ... }
```

> **Not:** Mevcut entity'ler `String` olarak tutmaya devam edebilir; bu converter yeni alanlar
> ve sorgular için hazırlık sağlar.

#### 18.7 — `@ColumnInfo` Annotation Eklemesi

Kritik entity'lere özellikle yeni alanlara `@ColumnInfo` eklenir.
`RewardEventEntity` örneği yukarıda. `SubTaskEntity`:
```kotlin
data class SubTaskEntity(
    @PrimaryKey
    @ColumnInfo(name = "id")
    val id: String,

    @ColumnInfo(name = "task_id", index = true)
    val taskId: String,

    @ColumnInfo(name = "title")
    val title: String,
    // ...
)
```

### Kabul Kriterleri
- [ ] `rewardedEvents` DataStore key tamamen kaldırıldı
- [ ] `RewardEventEntity` Room'da, tüm XP grant'ları buradan geçiyor
- [ ] `grantRewardOnce` aynı eventKey için iki kez `true` dönmüyor (birim testi)
- [ ] `MIGRATION_7_8` testi geçiyor
- [ ] `app/schemas/*/8.json` commit edildi

---

## Sprint 19 — Test Altyapısı Kurulumu

### Hedef
Hilt test runner doğru yapılandırılsın.
Room in-memory test veritabanı kurulsun.
Tüm kritik domain logic test altına alınsın.

### Teknik Görevler

#### 19.1 — Build Bağımlılıkları

`app/build.gradle.kts`:
```kotlin
dependencies {
    // Unit Test
    testImplementation(libs.junit)
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.1")
    testImplementation("app.cash.turbine:turbine:1.1.0")      // Flow test
    testImplementation("io.mockk:mockk:1.13.12")               // Mock

    // Android Test
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation("androidx.test.ext:junit:1.2.1")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.6.1")
    androidTestImplementation("androidx.compose.ui:ui-test-junit4")
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.52")
    kspAndroidTest("com.google.dagger:hilt-compiler:2.52")
    androidTestImplementation("androidx.room:room-testing:2.6.1")
    debugImplementation("androidx.compose.ui:ui-test-manifest")
}
```

`libs.versions.toml` içine:
```toml
[versions]
coroutinesTest = "1.8.1"
turbine = "1.1.0"
mockk = "1.13.12"

[libraries]
kotlinx-coroutines-test = { module = "org.jetbrains.kotlinx:kotlinx-coroutines-test", version.ref = "coroutinesTest" }
turbine = { module = "app.cash.turbine:turbine", version.ref = "turbine" }
mockk = { module = "io.mockk:mockk", version.ref = "mockk" }
hilt-android-testing = { module = "com.google.dagger:hilt-android-testing", version.ref = "hilt" }
androidx-room-testing = { module = "androidx.room:room-testing", version.ref = "room" }
```

#### 19.2 — Hilt Test Runner

`app/build.gradle.kts`:
```kotlin
android {
    defaultConfig {
        testInstrumentationRunner = "com.google.dagger.hilt.android.testing.HiltTestRunner"
    }
}
```

#### 19.3 — In-Memory Room Test Yardımcısı

`app/src/test/java/com/benimgunlerim/util/TestDatabase.kt`:
```kotlin
package com.benimgunlerim.util

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.benimgunlerim.data.local.AppDatabase

fun buildTestDatabase(): AppDatabase =
    Room.inMemoryDatabaseBuilder(
        ApplicationProvider.getApplicationContext<Context>(),
        AppDatabase::class.java,
    )
    .allowMainThreadQueries()
    .build()
```

#### 19.4 — `grantRewardOnce` Unit Testi

`test/data/RewardEventDaoTest.kt`:
```kotlin
@RunWith(AndroidJUnit4::class)
class RewardEventDaoTest {
    private lateinit var db: AppDatabase
    private lateinit var dao: RewardEventDao

    @Before fun createDb() {
        db = buildTestDatabase()
        dao = db.rewardEventDao()
    }

    @After fun closeDb() = db.close()

    @Test fun grantRewardOnce_secondCallReturnsFalse() = runBlocking {
        val entity = RewardEventEntity("task:abc:2026-04-13", xp = 12, gold = 5)
        val first = dao.insertIfAbsent(entity)
        val second = dao.insertIfAbsent(entity)
        assertNotEquals(-1L, first)   // inserted
        assertEquals(-1L, second)    // already exists → IGNORE
    }
}
```

#### 19.5 — `TodayViewModel` Coroutine Testi

`test/ui/today/TodayViewModelTest.kt`:
```kotlin
@OptIn(ExperimentalCoroutinesApi::class)
class TodayViewModelTest {
    @get:Rule val dispatcherRule = MainDispatcherRule()

    private lateinit var repository: BenimGunlerimRepository
    private lateinit var prefsRepository: UserPreferencesRepository
    private lateinit var viewModel: TodayViewModel

    @Before fun setup() {
        repository = mockk(relaxed = true)
        prefsRepository = mockk(relaxed = true)
        every { repository.observeTasks(any()) } returns flowOf(emptyList())
        every { repository.observeActiveRoutines() } returns flowOf(emptyList())
        every { repository.observeCompletionLogs(any()) } returns flowOf(emptyList())
        every { repository.observeTodayState() } returns flowOf(null)
        every { repository.observeOverdueTasks() } returns flowOf(emptyList())
        every { prefsRepository.preferences } returns flowOf(UserPreferences())
        viewModel = TodayViewModel(repository, prefsRepository, mockk(relaxed = true), mockk(relaxed = true), mockk(relaxed = true))
    }

    @Test fun initialState_isLoading() = runTest {
        viewModel.uiState.test {
            val initial = awaitItem()
            assertTrue(initial.isLoading || initial.tasks.isEmpty())
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test fun addTask_withBlankTitle_doesNotCallRepository() = runTest {
        viewModel.addTask("")
        coVerify(exactly = 0) { repository.addTask(any(), any(), any(), any(), any(), any(), any()) }
    }

    @Test fun toggleTask_alreadyCompleted_doesNotGrantXp() = runTest {
        val completedTask = TaskEntity(
            id = "t1", title = "Test", plannedDate = LocalDate.now().toString(),
            completionState = "completed", createdAt = 0L, updatedAt = 0L,
            note = null, startTime = null, endTime = null, category = null,
            color = null, completedAt = null, sourceTemplateId = null
        )
        viewModel.toggleTask(completedTask)
        coVerify(exactly = 0) { prefsRepository.grantRewardOnce(any(), any(), any(), any()) }
    }
}
```

#### 19.6 — `AchievementTracker` Unit Testi

`test/domain/AchievementTrackerTest.kt`:
```kotlin
class AchievementTrackerTest {
    @Test fun checkTaskCount_threshold10_emitsUnlock() = runTest {
        val tracker = AchievementTracker(mockk(relaxed = true))
        tracker.checkTaskCount(10)
        tracker.newUnlock.test {
            // İlk collect'te unlock bekleniyor
            // Test implementasyona göre ayarlanır
            cancelAndIgnoreRemainingEvents()
        }
    }
}
```

#### 19.7 — Migration Unit Testi

`androidTest/data/MigrationTest.kt`:
```kotlin
@RunWith(AndroidJUnit4::class)
class MigrationTest {
    private val testDb = MigrationTestHelper(
        InstrumentationRegistry.getInstrumentation(),
        AppDatabase::class.java,
    )

    @Test fun migrate6to7() {
        testDb.createDatabase("test-db", 6).apply {
            // v6 şemasında bir routine ekle
            execSQL("INSERT INTO routines (id, name, targetDays, isArchived, createdAt, updatedAt) VALUES ('r1','Test','MONDAY',0,0,0)")
            close()
        }
        testDb.runMigrationsAndValidate("test-db", 7, true, MIGRATION_6_7)
        // subtasks tablosu var mı?
    }

    @Test fun migrate7to8() {
        testDb.createDatabase("test-db", 7).apply { close() }
        val db = testDb.runMigrationsAndValidate("test-db", 8, true, MIGRATION_7_8)
        // reward_events tablosu var mı?
        val cursor = db.query("SELECT name FROM sqlite_master WHERE type='table' AND name='reward_events'")
        assertEquals(1, cursor.count)
        cursor.close()
    }
}
```

### Kabul Kriterleri
- [ ] `./gradlew test` tüm unit testler geçiyor
- [ ] `./gradlew connectedAndroidTest` migration testleri geçiyor
- [ ] Hilt inject androidTest içinde çalışıyor
- [ ] `TodayViewModel` blank title guard testi geçiyor
- [ ] `RewardEventDao` duplicate guard testi geçiyor

---

## Sprint 20 — Kapsamlı Birim Test Coverage

### Hedef
Kritik iş mantığının %60+ coverage'a ulaşması.
XP exploit, streak hesaplama, quiet hours mantığı test altına alınması.

### Test Dosyaları

#### 20.1 — `RoutineDetailViewModel` Streak Testi

`test/ui/routines/RoutineDetailViewModelTest.kt`:
```kotlin
class RoutineDetailViewModelTest {
    @Test fun calculateBestStreak_consecutiveDays_returnsCorrectValue() {
        // 3 gün üst üste log → bestStreak = 3
        val logs = listOf(
            completionLog("2026-04-10"),
            completionLog("2026-04-11"),
            completionLog("2026-04-12"),
        )
        // Private fonksiyonu test etmek için reflection veya internal erişim
        val result = RoutineDetailViewModel::class
            .declaredFunctions.first { it.name == "calculateBestStreak" }
            .call(mockViewModel, logs) as Int
        assertEquals(3, result)
    }

    @Test fun calculateBestStreak_withGap_returnsLongestRun() {
        val logs = listOf(
            completionLog("2026-04-01"),
            completionLog("2026-04-02"),
            // boşluk
            completionLog("2026-04-10"),
            completionLog("2026-04-11"),
            completionLog("2026-04-12"),
            completionLog("2026-04-13"),
        )
        // En uzun = 4
        assertEquals(4, calculateBestStreakPublic(logs))
    }
}
```

#### 20.2 — Quiet Hours Mantık Testi

`test/notifications/QuietHoursTest.kt`:
```kotlin
class QuietHoursTest {
    @Test fun normalRange_insideRange_returnsTrue() {
        // 09:00 start, 12:00 end → 10:30 içinde → true
        assertTrue(isInRange(LocalTime.of(9, 0), LocalTime.of(12, 0), LocalTime.of(10, 30)))
    }

    @Test fun overnightRange_pastMidnight_returnsTrue() {
        // 22:00 start, 07:00 end → 02:00 içinde → true
        assertTrue(isInRange(LocalTime.of(22, 0), LocalTime.of(7, 0), LocalTime.of(2, 0)))
    }

    @Test fun overnightRange_afternoonHour_returnsFalse() {
        // 22:00 start, 07:00 end → 14:00 dışında → false
        assertFalse(isInRange(LocalTime.of(22, 0), LocalTime.of(7, 0), LocalTime.of(14, 0)))
    }

    @Test fun boundary_exactStartTime_returnsTrue() {
        assertTrue(isInRange(LocalTime.of(22, 0), LocalTime.of(7, 0), LocalTime.of(22, 0)))
    }

    private fun isInRange(start: LocalTime, end: LocalTime, now: LocalTime): Boolean =
        if (start.isBefore(end)) now in start..end else now >= start || now <= end
}
```

#### 20.3 — `GameEngine` Tam Kapsam

`test/domain/GameEngineTest.kt` genişletme:
```kotlin
@Test fun xpForTask_priority1_returns8() {
    assertEquals(8, GameEngine.xpForTask(1))
}

@Test fun xpForTask_priority2_returns12() {
    assertEquals(12, GameEngine.xpForTask(2))
}

@Test fun xpForTask_priority3_returns18() {
    assertEquals(18, GameEngine.xpForTask(3))
}

@Test fun xpForTask_outOfRange_clampsToNormal() {
    assertEquals(12, GameEngine.xpForTask(0))
    assertEquals(12, GameEngine.xpForTask(99))
}

@Test fun levelInfo_xp0_returnsLevel1() {
    val info = GameEngine.levelInfo(0)
    assertEquals(1, info.level)
}

@Test fun levelInfo_highXp_returnsMaxLevel() {
    val info = GameEngine.levelInfo(Int.MAX_VALUE)
    assertTrue(info.level >= 30)
}
```

#### 20.4 — Repository Integration Test

`test/data/BenimGunlerimRepositoryTest.kt` genişletme:
```kotlin
@Test fun addTask_blankTitle_doesNotInsert() = runTest {
    repository.addTask("  ", LocalDate.now())
    coVerify(exactly = 0) { taskDao.insert(any()) }
}

@Test fun moveTaskToDate_updatesPlanedDate() = runTest {
    val task = TaskEntity(id = "t1", title = "T", plannedDate = "2026-04-13",
        completionState = "pending", // fill all required fields
        createdAt = 0L, updatedAt = 0L,
        note = null, startTime = null, endTime = null, category = null,
        color = null, completedAt = null, sourceTemplateId = null)
    repository.moveTaskToDate(task, LocalDate.of(2026, 4, 14))
    coVerify { taskDao.update(match { it.plannedDate == "2026-04-14" }) }
}
```

### Kabul Kriterleri
- [ ] Quiet hours her 3 senaryo için test geçiyor
- [ ] `GameEngine` XP tüm priority değerleri test altında
- [ ] Streak hesaplama boşluklu senaryo test edildi
- [ ] `./gradlew test` yeşil

---

## Sprint 21 — WorkManager Geçişi (Rutin Alarmlar)

### Hedef
`setInexactRepeating` yerine WorkManager `PeriodicWorkRequest` kullanımı.
Pil optimizasyonu ve force-stop senaryolarında tetiklemeler sağlıklı kalsın.
Task hatırlatıcısı exact alarm olarak AlarmManager'da kalmaya devam eder
(kullanıcının seçtiği belirli bir saate bağlı olduğu için).

### Teknik Görevler

#### 21.1 — `RoutineCheckWorker` Oluşturma

`notifications/RoutineCheckWorker.kt`:
```kotlin
package com.benimgunlerim.notifications

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.benimgunlerim.data.BenimGunlerimRepository
import com.benimgunlerim.data.UserPreferencesRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import java.time.LocalDate

@HiltWorker
class RoutineCheckWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val repository: BenimGunlerimRepository,
    private val prefsRepository: UserPreferencesRepository,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val prefs = prefsRepository.preferences.first()
        if (prefs.notificationMode == "off") return Result.success()
        if (applicationContext.isInQuietHoursSync()) return Result.success()

        val today = LocalDate.now()
        repository.observeActiveRoutines().first()
            .filter { routine ->
                routine.reminderEnabled &&
                !routine.preferredTime.isNullOrBlank() &&
                today.dayOfWeek.name in routine.targetDays.split(",")
            }
            .forEach { routine ->
                applicationContext.showRoutineReminder(routine.id, routine.name)
            }

        return Result.success()
    }
}
```

#### 21.2 — Hilt WorkManager Entegrasyonu

`build.gradle.kts` bağımlılıkları:
```kotlin
implementation("androidx.hilt:hilt-work:1.2.0")
ksp("androidx.hilt:hilt-compiler:1.2.0")
```

`BenimGunlerimApplication.kt` güncellemesi:
```kotlin
@HiltAndroidApp
class BenimGunlerimApplication : Application(), Configuration.Provider {

    @Inject lateinit var workerFactory: HiltWorkerFactory

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
    // ...
}
```

#### 21.3 — `RoutineReminderScheduler` WorkManager'a Taşıma

```kotlin
@Singleton
class RoutineReminderScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
) {
    private val workManager = WorkManager.getInstance(context)

    fun scheduleDaily() {
        val request = PeriodicWorkRequestBuilder<RoutineCheckWorker>(
            repeatInterval = 1,
            repeatIntervalTimeUnit = TimeUnit.DAYS,
        )
        .setInitialDelay(calculateInitialDelayToNineAm(), TimeUnit.MILLISECONDS)
        .setConstraints(Constraints.Builder().build())
        .addTag("routine_check")
        .build()

        workManager.enqueueUniquePeriodicWork(
            "routine_check",
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    fun cancel() {
        workManager.cancelUniqueWork("routine_check")
    }

    private fun calculateInitialDelayToNineAm(): Long {
        val now = LocalDateTime.now()
        var target = now.toLocalDate().atTime(9, 0)
        if (!target.isAfter(now)) target = target.plusDays(1)
        return ChronoUnit.MILLIS.between(now, target).coerceAtLeast(0)
    }
}
```

> **Not:** `schedule(routine: RoutineEntity)` imzası kaldırılır.
> `ReminderBootstrapper` artık tek `routineReminderScheduler.scheduleDaily()` çağırır.

#### 21.4 — `ReminderBootstrapper` Simplifikasyonu

```kotlin
@Singleton
class ReminderBootstrapper @Inject constructor(
    private val routineReminderScheduler: RoutineReminderScheduler,
    private val dailySummaryScheduler: DailySummaryScheduler,
    private val morningPlannerScheduler: MorningPlannerScheduler,
    private val userPreferencesRepository: UserPreferencesRepository,
) {
    fun rescheduleAll() {
        CoroutineScope(Dispatchers.IO).launch {
            routineReminderScheduler.scheduleDaily()
            val prefs = userPreferencesRepository.preferences.first()
            if (prefs.notificationMode != "off") {
                dailySummaryScheduler.schedule(LocalTime.parse(prefs.dailySummaryTime))
            }
            if (prefs.morningPlannerEnabled) {
                morningPlannerScheduler.schedule(LocalTime.parse(prefs.morningPlannerTime))
            }
        }
    }
}
```

### Kabul Kriterleri
- [ ] `WorkManager.getInstance().getWorkInfosByTag("routine_check")` ENQUEUED veya RUNNING
- [ ] Uygulama force-stop'tan sonra background alarm korunuyor (WorkManager yönetiyor)
- [ ] `RoutineCheckWorker` quiet hours kontrolüne uyuyor
- [ ] `BootReceiver` içinde `scheduleDaily()` çağrılıyor

---

## Sprint 22 — UseCase Katmanı & ViewModel Refactor

### Hedef
`TodayViewModel` 350+ satırdan ~150 satıra insin.
XP mantığı, achievement kontrolleri UseCase'e taşınsın.
`BenimGunlerimRepository` bölünsün.

### Teknik Görevler

#### 22.1 — `CompleteTaskUseCase` Oluşturma

`domain/CompleteTaskUseCase.kt`:
```kotlin
package com.benimgunlerim.domain

import com.benimgunlerim.analytics.AnalyticsEvent
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.data.BenimGunlerimRepository
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import java.time.LocalDate
import javax.inject.Inject

class CompleteTaskUseCase @Inject constructor(
    private val repository: BenimGunlerimRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val analyticsTracker: AnalyticsTracker,
    private val achievementTracker: AchievementTracker,
    private val feedbackManager: FeedbackManager,
) {
    sealed class Result {
        data class XpGranted(val xp: Int, val gold: Int) : Result()
        object AlreadyCompleted : Result()
        object XpAlreadyClaimed : Result()
    }

    suspend fun execute(task: TaskEntity): Result {
        repository.toggleTask(task)
        if (task.completionState == "completed") return Result.AlreadyCompleted

        feedbackManager.tapMedium()
        analyticsTracker.track(AnalyticsEvent("task_completed"))

        val taskXp = GameEngine.xpForTask(task.priority)
        val granted = repository.grantRewardOnce(
            eventKey = "task:${task.id}:${task.plannedDate}",
            xp = taskXp,
            gold = GameEngine.GOLD_TASK_COMPLETE,
            happinessDelta = GameEngine.HAPPINESS_TASK,
        )

        return if (granted) {
            prefsRepository.incrementTasksCompleted()
            achievementTracker.checkTaskCount(prefsRepository.preferences.first().totalTasksCompleted)
            Result.XpGranted(taskXp, GameEngine.GOLD_TASK_COMPLETE)
        } else {
            Result.XpAlreadyClaimed
        }
    }
}
```

#### 22.2 — `CompleteRoutineUseCase` Oluşturma

Aynı pattern, `domain/CompleteRoutineUseCase.kt`.

#### 22.3 — `TodayViewModel` Refactor

```kotlin
@HiltViewModel
class TodayViewModel @Inject constructor(
    private val repository: BenimGunlerimRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val completeTaskUseCase: CompleteTaskUseCase,
    private val completeRoutineUseCase: CompleteRoutineUseCase,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {

    // toggleTask artık sadece use case çağrısı:
    fun toggleTask(task: TaskEntity) {
        viewModelScope.launch {
            val result = completeTaskUseCase.execute(task)
            when (result) {
                is CompleteTaskUseCase.Result.XpGranted ->
                    _gameEvents.tryEmit(GameEvent.RewardEarned(result.xp, result.gold))
                else -> Unit
            }
        }
    }
}
```

#### 22.4 — Repository Bölme (Opsiyonel — büyük refactor)

Can alıcı blockerlardan biri değil. `BenimGunlerimRepository` içindeki
metodlar mantıksal gruplara ayrılabilir (500 satır sorun yaratıyorsa):

- `TaskRepository`
- `RoutineRepository`
- `DailyStateRepository`

Bu değişiklik diğer sprint'lerin engeli değil, ayrı bir use case olarak planlanabilir.

### Kabul Kriterleri
- [ ] `TodayViewModel` satır sayısı < 200
- [ ] `CompleteTaskUseCase` bağımsız birim testi mevcut
- [ ] `toggleTask` artık doğrudan `prefsRepository.grantRewardOnce` çağırmıyor

---

## Sprint 23 — UI Tamamlama & Erişilebilirlik

### Hedef
Koyu mod desteği eklenmesi.
Tüm interaktif elemanlara `contentDescription`.
Font ölçekleme testleri.

### Teknik Görevler

#### 23.1 — Dinamik Koyu Mod Desteği

`ui/theme/Theme.kt` içinde:
```kotlin
@Composable
fun BenimGunlerimTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) {
        darkColorScheme(
            primary = CandyPrimary,
            background = Color(0xFF121212),
            surface = Color(0xFF1E1E1E),
            onBackground = Color(0xFFE8E8E8),
            onSurface = Color(0xFFE0E0E0),
            surfaceVariant = Color(0xFF2C2C2C),
            outline = Color(0xFF3D3D3D),
        )
    } else {
        lightColorScheme(
            primary = CandyPrimary,
            // mevcut light renkler
        )
    }
    MaterialTheme(colorScheme = colorScheme, content = content)
}
```

`MainActivity.kt`:
```kotlin
BenimGunlerimTheme(darkTheme = preferences.themeMode == "dark" || (preferences.themeMode == "system" && isSystemInDarkTheme())) {
    BenimGunlerimApp()
}
```

#### 23.2 — ContentDescription Taraması

Tüm `IconButton`, `FloatingActionButton`, `Icon` componentleri taranır.
Eksik olanlar eklenir:

```kotlin
// Rutin detay "Arşivle" butonu
IconButton(onClick = { ... }) {
    Icon(Icons.Rounded.Archive, contentDescription = "Rutini arşivle")
}

// Swipe-to-delete arka planı
Box(Modifier.semantics { contentDescription = "Kaydırarak sil" })
```

#### 23.3 — Minimum Touch Target 48dp

Küçük ikonlar `Modifier.size(48.dp)` ile sarılır:
```kotlin
IconButton(
    modifier = Modifier.size(48.dp),
    onClick = { ... }
) {
    Icon(Icons.Rounded.Close, contentDescription = "Kapat", modifier = Modifier.size(24.dp))
}
```

#### 23.4 — Font Scale Testi

`@PreviewFontScale` ile Composable preview'lar eklenir:
```kotlin
@Preview(fontScale = 1.5f, name = "Large Font")
@Preview(fontScale = 2.0f, name = "Extra Large Font")
@Composable
fun TodayScreenLargeFontPreview() {
    BenimGunlerimTheme { TodayScreen() }
}
```

Büyük font'ta taşan text'ler için:
```kotlin
Text(
    text = routine.name,
    maxLines = 2,
    overflow = TextOverflow.Ellipsis,
)
```

#### 23.5 — String Resource'a Taşıma

`res/values/strings.xml` içine hardcoded Türkçe string'ler:
```xml
<string name="notif_routine_title">Küçük bir adım zamanı</string>
<string name="notif_task_title">Görev hatırlatması</string>
<string name="snooze_action">10 dk ertele</string>
<string name="routine_add_fab_desc">Rutin ekle</string>
<string name="task_add_fab_desc">Görev ekle</string>
```

### Kabul Kriterleri
- [ ] Uygulama koyu modda açılıyor, tüm renkler okunabilir
- [ ] Talkback ile ekranlar gezinilebilir
- [ ] Font scale 1.5x ile ana ekranlar kırılmıyor
- [ ] Tüm FAB ve IconButton'larda `contentDescription` mevcut

---

## Sprint 24 — Baseline Profile & Performance Final

### Hedef
Soğuk başlatma süresi optimize edilmesi.
Baseline Profile oluşturulması ve commit edilmesi.

### Teknik Görevler

#### 24.1 — Baseline Profile Bağımlılıkları

`app/build.gradle.kts`:
```kotlin
dependencies {
    implementation("androidx.profileinstaller:profileinstaller:1.4.1")
}
```

`baselineProfiles/build.gradle.kts` modülü oluşturulur:
```kotlin
plugins {
    id("com.android.test")
    id("androidx.baselineprofile")
}

android {
    targetProjectPath = ":app"
}

dependencies {
    implementation("androidx.test.uiautomator:uiautomator:2.3.0")
    implementation("androidx.benchmark:benchmark-macro-junit4:1.3.2")
    implementation("androidx.test.ext:junit:1.2.1")
}
```

#### 24.2 — BaselineProfileGenerator

`baselineProfiles/src/main/.../BaselineProfileGenerator.kt`:
```kotlin
@RunWith(AndroidJUnit4::class)
@BaselineProfileRule
class BaselineProfileGenerator {
    @get:Rule val baselineRule = BaselineProfileRule()

    @Test
    fun startup() = baselineRule.collect(
        packageName = "com.benimgunlerim",
    ) {
        pressHome()
        startActivityAndWait()
        // Bugün ekranını yükle
        device.wait(Until.hasObject(By.text("Bugün")), 3000)
        // Rutinler sekmesine git
        device.findObject(By.text("Rutinler")).click()
        device.waitForIdle()
    }
}
```

#### 24.3 — Uygulama Startup Trace

`MainActivity.kt`:
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    // Splash screen API ile ilk frame hızlandırma
    installSplashScreen()
    super.onCreate(savedInstanceState)
}
```

#### 24.4 — Compose Stability Annotation

Sık değişmeyen büyük data class'lara `@Stable` veya `@Immutable`:
```kotlin
@Immutable
data class TodayUiState(...)

@Immutable
data class RoutineDetailUiState(...)
```

### Kabul Kriterleri
- [ ] `app/src/main/baseline-prof.txt` dosyası mevcut
- [ ] `./gradlew generateBaselineProfile` başarılı
- [ ] Compose Compiler raporu incelendi, unstable parametreler azaltıldı

---

## Sprint 25 — Play Store Final Hazırlık & QA

### Hedef
Uygulama Play Store internal test kanalına gönderilebilecek hale gelsin.
Tüm manuel QA senaryoları geçsin.

### Teknik Görevler

#### 25.1 — `build.gradle.kts` Son Kontroller

```kotlin
defaultConfig {
    applicationId = "com.benimgunlerim"
    minSdk = 26
    targetSdk = 35
    versionCode = 2          // 1 → 2 (ilk release)
    versionName = "1.0.0"    // 0.1.0 → 1.0.0
}
```

#### 25.2 — Compose BOM Güncelleme

`libs.versions.toml`:
```toml
composeBom = "2025.03.00"   # 2024.10.01 → güncel
```

#### 25.3 — Bağımlılık Güvenlik Taraması

```bash
./gradlew dependencyUpdates -Drevision=release
```

Kritik güvenlik yamaları olan bağımlılıklar güncellenir.

#### 25.4 — Manuel QA Checklist

```
[ ] İlk kurulum, onboarding akışı
[ ] Onboarding sonrası boş ekran
[ ] Görev ekleme, düzenleme, silme
[ ] Görev tamamlama → XP toast görünüyor
[ ] Görev aynı gün 2x tamamlama → 2. XP gelmiyor
[ ] SwipeToDismiss → snackbar → "Geri al"
[ ] Rutin ekleme (6 hedef tipi)
[ ] Rutin tamamlama
[ ] Rutin detay sayfası açılıyor
[ ] Günü kapat akışı
[ ] Günü kapat → 2. kapat → XP gelmiyor
[ ] Bildirim izni iste (API 33+)
[ ] Bildirim geldi → "10 dk ertele" çalışıyor
[ ] Sessiz saatler aktif → bildirim gelmiyor
[ ] Cihaz reboot → alarm hala aktif
[ ] Ayarlar → "Veriyi temizle" → onay → uygulama resetleniyor
[ ] Türkçe büyük font (1.5x) → UI kırılmıyor
[ ] Koyu mod açık → tüm ekranlar okunabilir
[ ] Küçük ekran (5") → scroll çalışıyor
[ ] Büyük ekran (6.7") → boşluklar mantıklı
[ ] Play Console: policies, content rating, data safety formu
```

#### 25.5 — Data Safety Form (Play Console)

Toplanılan veriler:
- Kişisel veri: `displayName` (opsiyonel)
- Konum: Hayır
- Finansal bilgi: Hayır
- Sağlık bilgisi: Hayır (mood/enerji yerel kalıyor)
- Veri paylaşımı: Hayır (ağ yok)

#### 25.6 — Crashlytics Hazırlığı (Opsiyonel)

Firebase Crashlytics eklenmesi değerlendirilir:
```kotlin
implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
implementation("com.google.firebase:firebase-crashlytics-ktx")
```

### Kabul Kriterleri
- [ ] `./gradlew bundleRelease` sıfır hata
- [ ] APK Analyzer: release APK boyutu < 12 MB
- [ ] Manuel QA checklist %100 yeşil
- [ ] Play Console internal testing'e yüklendi
- [ ] Lint uyarı sayısı 0 (kritik olanlar)

---

## Sprint Zaman Çizelgesi

| Sprint | Başlık | Öncelik | Süre |
|--------|--------|---------|------|
| **13** | Room Migration | 🔴 Kritik | 1 gün |
| **14** | Release Build & Signing | 🔴 Kritik | 1 gün |
| **15** | Bildirim Hardening | 🔴 Kritik | 2 gün |
| **16** | Bellek & Sorgu Perf. | 🟠 Yüksek | 1 gün |
| **17** | Güvenlik & Privacy | 🟠 Yüksek | 1 gün |
| **18** | Veri Modeli Temizliği | 🟠 Yüksek | 2 gün |
| **19** | Test Altyapısı | 🟠 Yüksek | 2 gün |
| **20** | Test Coverage | 🟠 Yüksek | 2 gün |
| **21** | WorkManager Geçişi | 🟡 Orta | 2 gün |
| **22** | UseCase Katmanı | 🟡 Orta | 2 gün |
| **23** | UI & Erişilebilirlik | 🟡 Orta | 2 gün |
| **24** | Baseline Profile | 🟡 Orta | 1 gün |
| **25** | Play Store Final | 🟡 Orta | 2 gün |
| | **TOPLAM** | | **~23 gün** |

---

## Sprint Sonrası Beklenen Puan Artışı

| Alan | Önce | Sonra |
|------|------|-------|
| Mimari & Tasarım | 7.5 | **8.5** |
| Veri Katmanı | 6.5 | **9.0** |
| DI (Hilt) | 8.0 | **9.0** |
| Bildirim Sistemi | 6.0 | **8.5** |
| Güvenlik | 6.5 | **9.0** |
| Test Altyapısı | 4.5 | **8.0** |
| UI/UX Kalitesi | 8.0 | **9.0** |
| Performans | 6.5 | **8.5** |
| Build & Bağımlılık | 6.0 | **9.5** |
| Domain / İş Mantığı | 7.5 | **9.0** |
| Kod Kalitesi | 7.5 | **8.5** |
| **GENEL** | **6.7** | **9.0** |

---

## Kritik Sıralama — Hangi Sprint Önce?

Sprint'lerin bağımlılık sırasına göre uygulama önceliği:

```
Sprint 14 (Release Build)
    │
    ├── Sprint 13 (Migration) ← başta bunu yap, veriyi kurtar
    │
Sprint 15 (Bildirim Hardening) ← ikinci kritik blok
    │
    ├── Sprint 16 (Performans) ← bağımsız, paralel yapılabilir
    ├── Sprint 17 (Güvenlik)   ← bağımsız, paralel yapılabilir
    │
Sprint 18 (Veri Modeli) ← rewardedEvents blockerı
    │
    ├── Sprint 19 (Test Altyapısı)
    │       └── Sprint 20 (Test Coverage)
    │
Sprint 21 (WorkManager)
    │
    ├── Sprint 22 (UseCase)
    ├── Sprint 23 (UI)
    └── Sprint 24 (Baseline)
            │
        Sprint 25 (Play Store)
```

**Minimum yayın seti (9/10 için zorunlu):**
Sprint 13 → 14 → 15 → 17 → 18 → 19

**Bu 6 sprint tamamlandığında uygulama yayınlanabilir, blocker sıfır.**
