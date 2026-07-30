# Graph Report - BenimGunlerim  (2026-07-30)

## Corpus Check
- 233 files · ~114,376 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2364 nodes · 3876 edges · 191 communities (104 shown, 87 thin omitted)
- Extraction: 94% EXTRACTED · 6% INFERRED · 0% AMBIGUOUS · INFERRED: 216 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `3aa5b533`
- Run `git rev-parse HEAD` and compare to check if the graph is stale.
- Run `graphify update .` after code changes (no API cost).

## Community Hubs (Navigation)
- TodayScreen.kt
- SettingsViewModel
- PlanViewModel
- TodayViewModel
- OnboardingScreen
- Common.kt
- ErrorReporterTest
- GameEngine
- AccessibilityTest
- UserPreferencesRepository
- DeleteTaskUseCaseTest
- BenimGünlerim — Tasarım Standardı
- ProgressScreen.kt
- SubTaskEntity
- AchievementEntity
- GameEngineTest
- BenimGünlerim — Ürün Felsefesi, Amaçları ve Kullanıcı Akışları
- DailyStateEntity
- TaskEntity
- DataImportService
- PlanViewModelTest
- quietHoursActive
- RoutinesScreen.kt
- ShopViewModelTest
- JSONObject
- SystemFeedbackManager
- OnboardingViewModelTest
- RoutinesViewModelTest
- CompletionLogEntity
- RoutineEntity
- ProgressViewModel
- CloseDayUseCaseTest
- ToggleTaskUseCaseTest
- RoutineDetailViewModelTest
- TodayViewModelTest
- TaskDaoTest
- FakeAchievementDao
- AchievementEvaluationServiceTest
- AppModule
- TaskRepositoryImpl
- DataExportServiceTest
- StubTaskDao
- Plan Sayfası Uçtan Uca Derin İnceleme (2026-04-30)
- ToggleRoutineUseCaseTest
- SettingsViewModelTest
- TaskDao
- FakeCompletionLogDao
- Uçtan Uca Proje İncelemesi — 2026-04-30
- Plan Sayfası Sprint Planı (2026-04-30)
- NotificationHelper.kt
- 10. Sprint 5 — Header, Günü Kapat ve Dünü Tamamla Polish
- CompletionLogDao
- FakeCompletionLogDao
- OperationResultTest
- RewardGrantServiceTest
- FakePrefs
- today_ui_agent_sprint_sistemi.md
- DailyStateDaoTest
- CompletionLogDaoTest
- UserPreferences
- DateTimeProvider
- TickerProvider
- AchievementTracker
- FakeSubTaskDao
- 9. Sprint 4 — Routine Interaction Redesign
- ReportingUncaughtExceptionHandler
- CarryPendingTasksUseCaseTest
- RoutineDao
- RoutineRepository
- UserPreferencesAccess
- Dosya Bazlı Problem Listesi
- 8. Sprint 3 — Card & Button System
- RoutineReminderScheduler
- FakeTaskDao
- NotificationIdsTest
- FakeTaskDao
- BenimGunlerim Production Readiness Denetimi
- Release Checklist - BenimGünlerim
- 11. Sprint 6 — Accessibility ve Hitbox Pass
- 7. Sprint 2 — Semantic Color System
- DailyStateRepository
- AddTaskUseCaseTest
- AppDestination
- DailyScoreCalculatorTest
- FakeAchievementDao
- FakeAchievementDao
- FakeAchievementDao
- FakeAchievementDao
- 6. Birimleri 9/10'a Tasima Plani
- Teknik Görevler
- Teknik Görevler
- Teknik Görevler
- Gizlilik Politikası Taslağı
- Sprint 0
- 6. Sprint 1 — Lokalizasyon ve Metin Kalitesi
- AppDatabaseMigrationTest
- .add
- PlanUiEffect
- TimeValidation
- CompletionLogExtensionsTest
- NotificationMatrixSmokeTest
- ObserveTodaySnapshotUseCaseTest
- BenimGunlerim Production Ready 9/10 Sprint Plani
- Teknik Görevler
- Kalite Kapıları
- README.md
- TaskCompletionState
- RandomProvider
- FixedIndexRandomProvider
- AppTokens
- CloseDayUseCase
- Token Hızlı Başvuru
- Sprint 13 — Room Migration & Schema Güvenliği
- Teknik Görevler
- Teknik Görevler
- Production Readiness Planı
- 4. Sprint Durumu
- ProgressCalculatorTest
- FakeDailyStateDao
- Dependency ve Platform Sağlığı Politikası
- Performans Politikası
- Test Stratejisi
- 10.2 Günü Kapat Kartı Polish
- DataExportService
- NotificationIds
- TestTags
- AchievementDefTest
- Dış Release Kurulum Kontrolü
- BenimGünlerim — Güncel Proje Durumu
- BenimGünlerim
- CompletionStatus
- OperationResult
- DailySummaryReceiver
- MorningPlannerReceiver
- RoutineReminderReceiver
- SnoozeReceiver
- TaskReminderReceiver
- TimeInputValidatorTest
- Gizlilik ve Yedekleme Politikası
- GitHub Branch/Tag Protection ve Secrets Checklist
- DailyScoreCalculator
- CompletionEntityType
- RoutineTargetType
- ObserveProgressSnapshotUseCaseTest
- ErrorReporterContextPolicy
- RoutineExtensions.kt
- ObservePlanSnapshotUseCase
- UpdateRoutineProgressUseCase
- CategoryPalette
- GrantResultTest
- TimeTextTest
- AppDestinationTest
- StartupBenchmark
- gradlew
- CompletionLogExtensions.kt
- migrate
- ProgressCalculator
- CarryPendingTasksUseCase
- DeleteSubTaskUseCase
- DeleteTaskUseCase
- MoveTaskToDateUseCase
- ObserveSubTasksUseCase
- RestoreTaskUseCase
- SaveMissedDaySummaryUseCase
- UpdateTaskTitleUseCase
- UpdateTaskUseCase
- ToggleTaskCompletionUseCaseTest
- ScrollJankBenchmark
- NotificationConstants.kt
- TodayColorTokens.kt
- DeleteTaskUseCaseTest

## God Nodes (most connected - your core abstractions)
1. `TaskEntity` - 97 edges
2. `AchievementEntity` - 60 edges
3. `RoutineEntity` - 60 edges
4. `CompletionLogEntity` - 56 edges
5. `TodayViewModelTest` - 49 edges
6. `UserPreferencesRepository` - 47 edges
7. `UserPreferences` - 42 edges
8. `TodayViewModel` - 40 edges
9. `DailyStateEntity` - 39 edges
10. `GameEngineTest` - 39 edges

## Surprising Connections (you probably didn't know these)
- `BenimGunlerimApp()` --calls--> `AchievementsScreen()`  [INFERRED]
  app/src/main/java/com/benimgunlerim/ui/AppNavigation.kt → app/src/main/java/com/benimgunlerim/ui/achievements/AchievementsScreen.kt
- `BenimGunlerimApp()` --calls--> `PlanScreen()`  [INFERRED]
  app/src/main/java/com/benimgunlerim/ui/AppNavigation.kt → app/src/main/java/com/benimgunlerim/ui/plan/PlanScreen.kt
- `BenimGunlerimApp()` --calls--> `ProgressScreen()`  [INFERRED]
  app/src/main/java/com/benimgunlerim/ui/AppNavigation.kt → app/src/main/java/com/benimgunlerim/ui/progress/ProgressScreen.kt
- `BenimGunlerimApp()` --calls--> `RoutinesScreen()`  [INFERRED]
  app/src/main/java/com/benimgunlerim/ui/AppNavigation.kt → app/src/main/java/com/benimgunlerim/ui/routines/RoutinesScreen.kt
- `BenimGunlerimApp()` --calls--> `SettingsScreen()`  [INFERRED]
  app/src/main/java/com/benimgunlerim/ui/AppNavigation.kt → app/src/main/java/com/benimgunlerim/ui/settings/SettingsScreen.kt

## Import Cycles
- None detected.

## Communities (191 total, 87 thin omitted)

### Community 0 - "TodayScreen.kt"
Cohesion: 0.05
Nodes (66): AchievementUnlockOverlay(), bouncyClick(), ConfettiIntensity, Full, LevelUp, Medium, Mini, ConfettiOverlay() (+58 more)

### Community 1 - "SettingsViewModel"
Cohesion: 0.05
Nodes (41): DataCleared, ExportFailed, ExportSaved, ExportWriteFailed, ImportParseFailed, ImportReadFailed, ImportSuccess, SettingsEvent (+33 more)

### Community 2 - "PlanViewModel"
Cohesion: 0.20
Nodes (21): orZero(), PlanAddTaskSheet(), PlanDateNavigationRow(), PlanEditTaskSheet(), PlanPrioritySelector(), PlanScreen(), PlanSnapshotErrorBanner(), toLocalDate() (+13 more)

### Community 3 - "TodayViewModel"
Cohesion: 0.15
Nodes (4): StateFlow, ViewModel, TodayUiState, TodayViewModel

### Community 4 - "OnboardingScreen"
Cohesion: 0.17
Nodes (21): AddRoutineSheet(), BaseCard(), DaySelector(), EmptyRoutinesCard(), formatRoutineDays(), InfoPill(), androidx, Brush (+13 more)

### Community 5 - "Common.kt"
Cohesion: 0.05
Nodes (53): AchievementRow(), AchievementsScreen(), Color, Modifier, SummaryPill(), AchievementsUiState, AchievementsViewModel, AchievementWithStatus (+45 more)

### Community 6 - "ErrorReporterTest"
Cohesion: 0.07
Nodes (8): CrashlyticsErrorReporter, ErrorReporter, LocalErrorReporter, StoredErrorReport, ErrorReporterTest, RecordingReporter, LocalErrorReporterTest, FirebaseCrashlytics

### Community 8 - "AccessibilityTest"
Cohesion: 0.04
Nodes (16): AccessibilityTest, ActivityScenarioRule, AndroidComposeTestRule, ActivityScenarioRule, AndroidComposeTestRule, RoutinesScreenTest, ActivityScenarioRule, AndroidComposeTestRule (+8 more)

### Community 9 - "UserPreferencesRepository"
Cohesion: 0.07
Nodes (5): Keys, Flow, UserPreferencesRepository, UserPreferencesSource, UserPreferencesWriter

### Community 10 - "DeleteTaskUseCaseTest"
Cohesion: 0.24
Nodes (3): DailySummarySchedule, DailySummaryScheduler, PendingIntent

### Community 11 - "BenimGünlerim — Tasarım Standardı"
Cohesion: 0.04
Nodes (45): 10. Durumlar ve Geri Bildirim, 11. Erişilebilirlik Standardı, 12. İçerik Tonu, 13. Tasarım Karar Kontrol Listesi, 14. İlgili Belgeler, 15. Global UI Kuralları, 16. Tasarım Teslim Kontrolü, 1. Tasarım Vaadi (+37 more)

### Community 12 - "ProgressScreen.kt"
Cohesion: 0.15
Nodes (32): ProgressDayUi, AchievementItem(), AchievementsCard(), BalanceBar(), BalanceStat(), ConsistencyCard(), DayRow(), EmptyPanel() (+24 more)

### Community 13 - "SubTaskEntity"
Cohesion: 0.07
Nodes (7): SubTaskEntity, Flow, SubTaskDao, DeleteSubTaskUseCase, ToggleSubTaskUseCase, StubSubTaskDao, FakeSubTaskDao

### Community 16 - "BenimGünlerim — Ürün Felsefesi, Amaçları ve Kullanıcı Akışları"
Cohesion: 0.07
Nodes (30): 10. Başarı Kriterleri, 11. Geliştirme Kararları İçin Kısa Filtre, 12. Ürün Yönü, 1. Bu Belgenin Amacı, 2. Ürünün Kısa Tanımı, 3. Temel Ürün Amacı, 4.1 Küçük adımlar önemlidir, 4.2 Kullanıcı suçlanmaz (+22 more)

### Community 17 - "DailyStateEntity"
Cohesion: 0.06
Nodes (8): DailyStateDaoTest, DailyStateRepository, Flow, DailyStateDao, Flow, DailyStateEntity, StubDailyStateDao, FakeDailyStateDao

### Community 18 - "TaskEntity"
Cohesion: 0.12
Nodes (3): TaskEntity, Flow, TaskRepository

### Community 21 - "quietHoursActive"
Cohesion: 0.13
Nodes (5): NotificationPolicyCache, quietHoursActive(), ReminderPolicy, ReminderPolicyTest, java

### Community 23 - "ShopViewModelTest"
Cohesion: 0.10
Nodes (6): StateFlow, ViewModel, ShopItem, ShopUiState, ShopViewModel, ShopViewModelTest

### Community 24 - "JSONObject"
Cohesion: 0.27
Nodes (3): DataImportServiceTest, FakePreferencesStore, JSONObject

### Community 25 - "SystemFeedbackManager"
Cohesion: 0.12
Nodes (5): FeedbackManager, SystemFeedbackManager, SoundPool, VibrationEffect, Vibrator

### Community 26 - "OnboardingViewModelTest"
Cohesion: 0.09
Nodes (6): AddRoutineUseCase, AddTaskUseCase, StateFlow, ViewModel, OnboardingViewModel, OnboardingViewModelTest

### Community 27 - "RoutinesViewModelTest"
Cohesion: 0.09
Nodes (6): ArchiveRoutineUseCase, UpdateRoutineUseCase, StateFlow, ViewModel, RoutinesViewModel, RoutinesViewModelTest

### Community 28 - "CompletionLogEntity"
Cohesion: 0.15
Nodes (3): CompletionLogRepository, CompletionLogRepositoryImpl, Flow

### Community 29 - "RoutineEntity"
Cohesion: 0.17
Nodes (3): Result, ToggleTaskUseCase, ToggleTaskUseCaseTest

### Community 30 - "ProgressViewModel"
Cohesion: 0.18
Nodes (5): DataImportResult, DataImportService, ImportPayload, JSONArray, T

### Community 33 - "RoutineDetailViewModelTest"
Cohesion: 0.13
Nodes (6): SkipRoutineUseCase, StateFlow, ViewModel, RoutineDetailUiState, RoutineDetailViewModel, RoutineDetailViewModelTest

### Community 36 - "FakeAchievementDao"
Cohesion: 0.16
Nodes (3): AchievementTrackerTest, FakeAchievementDao, Flow

### Community 42 - "Plan Sayfası Uçtan Uca Derin İnceleme (2026-04-30)"
Cohesion: 0.12
Nodes (5): StateFlow, ViewModel, PlanSnapshotResult, PlanUiState, PlanViewModel

### Community 43 - "ToggleRoutineUseCaseTest"
Cohesion: 0.19
Nodes (3): Result, ToggleRoutineUseCase, ToggleRoutineUseCaseTest

### Community 49 - "NotificationHelper.kt"
Cohesion: 0.06
Nodes (27): AppCrashHandler, ReportingUncaughtExceptionHandler, BenimGunlerimApplication, RestoreRemindersUseCase, buildSnoozeAction(), ensureMorningNotificationChannel(), ensureRoutineNotificationChannel(), ensureTaskNotificationChannel() (+19 more)

### Community 50 - "10. Sprint 5 — Header, Günü Kapat ve Dünü Tamamla Polish"
Cohesion: 0.15
Nodes (13): Faz B — Core Atomlar + Gamification Atomlar, `ui/components/core/AppBadge.kt`, `ui/components/core/AppButton.kt`, `ui/components/core/AppChip.kt`, `ui/components/core/AppDivider.kt`, `ui/components/core/AppIcon.kt`, `ui/components/core/AppSurface.kt`, `ui/components/gamification/CompanionBubble.kt` (+5 more)

### Community 51 - "CompletionLogDao"
Cohesion: 0.18
Nodes (3): CompletionLogDao, Flow, CompletionLogEntity

### Community 56 - "today_ui_agent_sprint_sistemi.md"
Cohesion: 0.17
Nodes (11): 7 Katmanlı Mimari, BenimGünlerim — Agent Kılavuzu, Demir Kurallar — İstisna Yok, Ekran Başına Hangi Bileşenler, Faz Durumu, Graphify, Hedef Dosya Hiyerarşisi (Faz E sonrası), Mevcut Durum — Geçiş Süreci (+3 more)

### Community 58 - "CompletionLogDaoTest"
Cohesion: 0.17
Nodes (12): Faz C — Moleküller + Layout Katmanı, `ui/components/layout/AppTopBar.kt`, `ui/components/layout/ScreenScaffold.kt`, `ui/components/molecules/AlertBanner.kt`, `ui/components/molecules/BarChart.kt`, `ui/components/molecules/ColorRailCard.kt`, `ui/components/molecules/EmptyState.kt`, `ui/components/molecules/InfoCard.kt` (+4 more)

### Community 60 - "DateTimeProvider"
Cohesion: 0.19
Nodes (3): DateTimeProvider, FixedDateTimeProvider, SystemDateTimeProvider

### Community 61 - "TickerProvider"
Cohesion: 0.22
Nodes (5): ImmediateTickerProvider, Flow, SystemTickerProvider, TickerProvider, TickerProviderTest

### Community 62 - "AchievementTracker"
Cohesion: 0.25
Nodes (3): AchievementDef, AchievementTracker, Flow

### Community 63 - "FakeSubTaskDao"
Cohesion: 0.16
Nodes (10): bestStreak(), hitRate(), Flow, ObserveProgressSnapshotUseCase, ProgressSnapshot, StateFlow, ViewModel, ProgressUiState (+2 more)

### Community 64 - "9. Sprint 4 — Routine Interaction Redesign"
Cohesion: 0.22
Nodes (4): AppModule, Context, CoroutineDispatcher, CoroutineScope

### Community 65 - "ReportingUncaughtExceptionHandler"
Cohesion: 0.25
Nodes (3): PendingIntent, MorningPlannerSchedule, MorningPlannerScheduler

### Community 66 - "CarryPendingTasksUseCaseTest"
Cohesion: 0.25
Nodes (6): ActionFailed, DaySaved, OverdueTasksMoved, TaskDeleted, TaskMovedTomorrow, TodayUiEffect

### Community 67 - "RoutineDao"
Cohesion: 0.09
Nodes (4): RoutineEntity, StubRoutineDao, FakeRoutineDao, FakeRoutineDao

### Community 70 - "Dosya Bazlı Problem Listesi"
Cohesion: 0.18
Nodes (11): Faz D — Organizmalar, `ui/components/organisms/AchievementRow.kt`, `ui/components/organisms/AddRoutineSheet.kt`, `ui/components/organisms/AddTaskSheet.kt`, `ui/components/organisms/CloseDayCard.kt`, `ui/components/organisms/HeaderProgressCard.kt`, `ui/components/organisms/LevelHeroCard.kt`, `ui/components/organisms/RoutineRow.kt` (+3 more)

### Community 71 - "8. Sprint 3 — Card & Button System"
Cohesion: 0.27
Nodes (4): AnalyticsEvent, AnalyticsTracker, LocalAnalyticsTracker, TaskCompletedUndo

### Community 72 - "RoutineReminderScheduler"
Cohesion: 0.23
Nodes (3): PendingIntent, RoutineReminderScheduler, RoutineReminderSchedulerContract

### Community 73 - "FakeTaskDao"
Cohesion: 0.11
Nodes (25): BenimGunlerimApp(), Destination, Plan, Progress, Routines, Settings, Today, ExplainerCard() (+17 more)

### Community 77 - "Release Checklist - BenimGünlerim"
Cohesion: 0.17
Nodes (11): 10. Supply Chain ve Repo Kontrolleri, 1. Signing Kurulumu, 2. Versioning, 3. Zorunlu Kalite Kapısı, 4. Cihaz Smoke Test, 5. Backup ve Gizlilik Kontrolü, 6. Play Store Akışı, 7. Post-Release (+3 more)

### Community 78 - "11. Sprint 6 — Accessibility ve Hitbox Pass"
Cohesion: 0.31
Nodes (5): completedRoutineIds(), Flow, ObserveTodaySnapshotUseCase, TodaySnapshot, Flow

### Community 82 - "AppDestination"
Cohesion: 0.18
Nodes (9): Achievements, AppDestination, Onboarding, Plan, Progress, RoutineDetailPattern, Routines, Settings (+1 more)

### Community 90 - "Teknik Görevler"
Cohesion: 0.29
Nodes (3): FakeTransactionRunner, T, FakeTransactionRunnerTest

### Community 92 - "Gizlilik Politikası Taslağı"
Cohesion: 0.18
Nodes (10): Analitik, Dışa Aktarma ve Geri Yükleme, Gizlilik Politikası Taslağı, Hata Raporlama, İletişim, İzinler, Toplanan Veriler, Veri Silme (+2 more)

### Community 93 - "Sprint 0"
Cohesion: 0.29
Nodes (4): BootReceiver, BroadcastReceiver, Context, Intent

### Community 95 - "AppDatabaseMigrationTest"
Cohesion: 0.29
Nodes (3): AppDatabaseMigrationTest, Context, SupportSQLiteDatabase

### Community 97 - "PlanUiEffect"
Cohesion: 0.29
Nodes (5): ActionFailed, OverdueTasksMoved, PlanUiEffect, TaskAdded, TaskDeleted

### Community 98 - "TimeValidation"
Cohesion: 0.22
Nodes (6): TimeInputValidator, TimeValidation, Incomplete, InvalidClock, InvalidFormat, Ok

### Community 104 - "Kalite Kapıları"
Cohesion: 0.18
Nodes (11): CI Kapıları, CI Secret Gereksinimleri, Cihaz Test Kapısı, Coverage Kuralı, Dış Release Kontrolü, Kalite Kapıları, Lokal Geliştirme Kapısı, Ortam Gereksinimleri (+3 more)

### Community 106 - "TaskCompletionState"
Cohesion: 0.25
Nodes (5): fromString(), TaskCompletionState, COMPLETED, PENDING, SKIPPED

### Community 107 - "RandomProvider"
Cohesion: 0.31
Nodes (3): T, RandomProvider, SystemRandomProvider

### Community 109 - "AppTokens"
Cohesion: 0.33
Nodes (8): AppTokens, BottomNav, Elevation, IconSize, Dp, Motion, Radius, Spacing

### Community 111 - "Token Hızlı Başvuru"
Cohesion: 0.40
Nodes (5): IconSize (AppTokens.IconSize), Motion (AppTokens.Motion), Radius (AppTokens.Radius), Spacing (AppTokens.Spacing), Token Hızlı Başvuru

### Community 115 - "Production Readiness Planı"
Cohesion: 0.22
Nodes (9): Bildirim ve Background İşleri, Build & Release, CI/CD ve Kalite Kapıları, Güvenlik ve Gizlilik, Performans ve Operasyon, Production Readiness Planı, Test Stratejisi, UX, Lokalizasyon ve Erişilebilirlik (+1 more)

### Community 116 - "4. Sprint Durumu"
Cohesion: 0.22
Nodes (9): 4. Sprint Durumu, Sprint 0 — Temel davranış ve davranış kilidi, Sprint 1 — Lokalizasyon ve metin kalitesi, Sprint 2 — Semantic color system, Sprint 3 — Card ve button system, Sprint 4 — Rutin etkileşimi, Sprint 5 — Header, gün kapatma ve missed day polish, Sprint 6 — Erişilebilirlik ve hitbox (+1 more)

### Community 121 - "FakeDailyStateDao"
Cohesion: 0.46
Nodes (4): AlreadyGranted, Granted, GrantResult, RewardGrantService

### Community 135 - "Dependency ve Platform Sağlığı Politikası"
Cohesion: 0.25
Nodes (7): Cadence, Dependency ve Platform Sağlığı Politikası, Otomasyon, Sürümleme Kuralı, Takip Edilecek Borçlar, targetSdk Politikası, Zorunlu Kontroller

### Community 136 - "Performans Politikası"
Cohesion: 0.25
Nodes (7): Büyük Veri Senaryoları (Manual Deep Gate), CI Deep Gate, Gate Komutları, Performans Politikası, Threshold'lar, Uygulama Başlangıç İlkeleri, Ölçüm Kapsamı

### Community 137 - "Test Stratejisi"
Cohesion: 0.22
Nodes (8): Flaky Test Politikasi, Kısa Vadeli Hedefler, Nightly veya Manual Deep Gate, PR Gate, Release Gate, Risk Odakli Test Alanlari, Test Stratejisi, UI ve Görsel Doğrulama Standardı

### Community 139 - "10.2 Günü Kapat Kartı Polish"
Cohesion: 0.15
Nodes (3): AchievementEntity, StubAchievementDao, FakeAchievementDao

### Community 152 - "Dış Release Kurulum Kontrolü"
Cohesion: 0.29
Nodes (6): Branch Protection, Dış Release Kurulum Kontrolü, GitHub Secrets, Monitoring, Play Console, Tag Protection

### Community 154 - "BenimGünlerim — Güncel Proje Durumu"
Cohesion: 0.29
Nodes (7): 1. Mevcut Konum, 2. Tamamlanan Ürün Kapsamı, 3. Son Doğrulamalar, 5. Production Öncesi Açıklar, 6. Önerilen Uygulama Sırası, 7. Doküman Kullanım Kuralı, BenimGünlerim — Güncel Proje Durumu

### Community 155 - "BenimGünlerim"
Cohesion: 0.29
Nodes (7): BenimGünlerim, Geliştirme Ortamı, Hızlı Kontrol, Mevcut Ürün Kapsamı, Production Dokümanları, Release Kontrolü, Teknoloji

### Community 157 - "CompletionStatus"
Cohesion: 0.40
Nodes (5): CompletionStatus, COMPLETED, PARTIAL, SKIPPED, fromString()

### Community 158 - "OperationResult"
Cohesion: 0.33
Nodes (5): Failure, T, OperationResult, Success, ValidationError

### Community 159 - "DailySummaryReceiver"
Cohesion: 0.33
Nodes (4): DailySummaryReceiver, BroadcastReceiver, Context, Intent

### Community 160 - "MorningPlannerReceiver"
Cohesion: 0.33
Nodes (4): BroadcastReceiver, Context, Intent, MorningPlannerReceiver

### Community 161 - "RoutineReminderReceiver"
Cohesion: 0.33
Nodes (4): BroadcastReceiver, Context, Intent, RoutineReminderReceiver

### Community 162 - "SnoozeReceiver"
Cohesion: 0.33
Nodes (4): BroadcastReceiver, Context, Intent, SnoozeReceiver

### Community 163 - "TaskReminderReceiver"
Cohesion: 0.33
Nodes (4): BroadcastReceiver, Context, Intent, TaskReminderReceiver

### Community 168 - "Gizlilik ve Yedekleme Politikası"
Cohesion: 0.33
Nodes (5): Android Backup Davranışı, Değişiklik Kararı, Gizlilik ve Yedekleme Politikası, Production Gereksinimleri, Yerel Veri

### Community 172 - "GitHub Branch/Tag Protection ve Secrets Checklist"
Cohesion: 0.33
Nodes (5): Branch Protection, GitHub Branch/Tag Protection ve Secrets Checklist, Otomasyon, Secrets, Tag Protection

### Community 174 - "CompletionEntityType"
Cohesion: 0.50
Nodes (4): CompletionEntityType, ROUTINE, TASK, fromString()

### Community 175 - "RoutineTargetType"
Cohesion: 0.50
Nodes (4): fromString(), RoutineTargetType, CHECK, GOAL

### Community 200 - "gradlew"
Cohesion: 0.83
Nodes (3): gradlew script, die(), warn()

### Community 208 - "DeleteSubTaskUseCase"
Cohesion: 0.40
Nodes (4): AchievementUnlocked, GameEvent, LevelUp, RewardEarned

### Community 249 - "DeleteTaskUseCaseTest"
Cohesion: 0.24
Nodes (3): PendingIntent, TaskReminderScheduler, TaskReminderSchedulerContract

## Knowledge Gaps
- **255 isolated node(s):** `Keys`, `ValidationError`, `Failure`, `TASK`, `ROUTINE` (+250 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **87 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `UserPreferences` connect `UserPreferences` to `SettingsViewModel`, `UserPreferencesRepository`, `RoutinesScreen.kt`, `ShopViewModelTest`, `JSONObject`, `OnboardingViewModelTest`, `RoutineEntity`, `ProgressViewModel`, `CloseDayUseCaseTest`, `TodayViewModelTest`, `DataExportServiceTest`, `ToggleRoutineUseCaseTest`, `FakeCompletionLogDao`, `ObserveProgressSnapshotUseCaseTest`, `RewardGrantServiceTest`, `FakeSubTaskDao`, `BenimGunlerim Production Readiness Denetimi`, `11. Sprint 6 — Accessibility ve Hitbox Pass`, `Teknik Görevler`, `CloseDayUseCase`?**
  _High betweenness centrality (0.124) - this node is a cross-community bridge._
- **Why does `TaskEntity` connect `TaskEntity` to `TodayViewModel`, `JSONObject`, `OnboardingViewModelTest`, `RoutineEntity`, `ProgressViewModel`, `TodayViewModelTest`, `TaskDaoTest`, `TaskRepositoryImpl`, `DataExportServiceTest`, `StubTaskDao`, `Plan Sayfası Uçtan Uca Derin İnceleme (2026-04-30)`, `SettingsViewModelTest`, `TaskDao`, `Plan Sayfası Sprint Planı (2026-04-30)`, `FakeCompletionLogDao`, `FakePrefs`, `UserPreferences`, `CarryPendingTasksUseCaseTest`, `8. Sprint 3 — Card & Button System`, `FakeTaskDao`, `BenimGunlerim Production Readiness Denetimi`, `7. Sprint 2 — Semantic Color System`, `DailyStateRepository`, `DeleteTaskUseCase`, `MoveTaskToDateUseCase`, `AddTaskUseCaseTest`, `RestoreTaskUseCase`, `6. Birimleri 9/10'a Tasima Plani`, `UpdateTaskTitleUseCase`, `UpdateTaskUseCase`, `TaskCompletionState`?**
  _High betweenness centrality (0.102) - this node is a cross-community bridge._
- **Why does `TodayViewModel` connect `TodayViewModel` to `TodayScreen.kt`, `CarryPendingTasksUseCaseTest`, `RoutineDao`, `TodayViewModelTest`, `8. Sprint 3 — Card & Button System`, `11. Sprint 6 — Accessibility ve Hitbox Pass`, `DeleteSubTaskUseCase`, `TaskEntity`, `AchievementTracker`?**
  _High betweenness centrality (0.097) - this node is a cross-community bridge._
- **Are the 10 inferred relationships involving `TaskEntity` (e.g. with `.task()` and `.persistedTask()`) actually correct?**
  _`TaskEntity` has 10 INFERRED edges - model-reasoned connections that need verification._
- **Are the 10 inferred relationships involving `RoutineEntity` (e.g. with `.schedulers_scheduleAndCancel_withoutCrash()` and `.routineIsScheduledFor_returnsFalseWhenTokenHasWhitespace()`) actually correct?**
  _`RoutineEntity` has 10 INFERRED edges - model-reasoned connections that need verification._
- **Are the 11 inferred relationships involving `CompletionLogEntity` (e.g. with `.log()` and `.setProgress()`) actually correct?**
  _`CompletionLogEntity` has 11 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `ValidationError`, `Failure` to the rest of the system?**
  _255 weakly-connected nodes found - possible documentation gaps or missing edges._