# Graph Report - BenimGunlerim  (2026-07-30)

## Corpus Check
- 263 files · ~102,562 words
- Verdict: corpus is large enough that graph structure adds value.

## Summary
- 2335 nodes · 3626 edges · 188 communities (109 shown, 79 thin omitted)
- Extraction: 93% EXTRACTED · 7% INFERRED · 0% AMBIGUOUS · INFERRED: 253 edges (avg confidence: 0.8)
- Token cost: 0 input · 0 output

## Graph Freshness
- Built from commit: `c7fa8efd`
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
- TodayUiEffect
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
- NotificationMatrixSmokeTest
- TimeChangeReceiver
- AppDatabaseMigrationTest
- .add
- TimeValidation
- CompletionLogExtensionsTest
- NotificationMatrixSmokeTest
- BenimGunlerim Production Ready 9/10 Sprint Plani
- Kalite Kapıları
- README.md
- TaskCompletionState
- RandomProvider
- FixedIndexRandomProvider
- AppTokens
- Token Hızlı Başvuru
- Sprint 13 — Room Migration & Schema Güvenliği
- ColorRailCard
- Teknik Görevler
- Production Readiness Planı
- 4. Sprint Durumu
- ProgressBar
- EmptyState
- ProgressCalculatorTest
- SectionBlock
- AddSubTaskUseCase
- StatPill
- UpdateRoutineUseCase
- TodayActions.kt
- Dependency ve Platform Sağlığı Politikası
- Performans Politikası
- Test Stratejisi
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
- DeleteTaskUseCase
- ObserveSubTasksUseCase
- RestoreTaskUseCase
- SaveMissedDaySummaryUseCase
- UpdateTaskTitleUseCase
- UpdateTaskUseCase
- ToggleTaskCompletionUseCaseTest
- ScrollJankBenchmark
- NotificationConstants.kt
- TodayColorTokens.kt

## God Nodes (most connected - your core abstractions)
1. `TaskEntity` - 97 edges
2. `AchievementEntity` - 60 edges
3. `RoutineEntity` - 60 edges
4. `CompletionLogEntity` - 54 edges
5. `TodayViewModelTest` - 49 edges
6. `UserPreferencesRepository` - 47 edges
7. `UserPreferences` - 40 edges
8. `DailyStateEntity` - 39 edges
9. `TodayViewModel` - 39 edges
10. `GameEngineTest` - 39 edges

## Surprising Connections (you probably didn't know these)
- `AchievementsScreen()` --calls--> `AchievementRow()`  [INFERRED]
  app/src/main/java/com/benimgunlerim/ui/achievements/AchievementsScreen.kt → app/src/main/java/com/benimgunlerim/ui/components/organisms/AchievementRow.kt
- `BenimGunlerimApp()` --calls--> `AchievementsScreen()`  [INFERRED]
  app/src/main/java/com/benimgunlerim/ui/navigation/AppNavigation.kt → app/src/main/java/com/benimgunlerim/ui/achievements/AchievementsScreen.kt
- `AchievementRow()` --calls--> `AppBadge()`  [INFERRED]
  app/src/main/java/com/benimgunlerim/ui/components/organisms/AchievementRow.kt → app/src/main/java/com/benimgunlerim/ui/components/core/AppBadge.kt
- `ShopItemCard()` --calls--> `AppBadge()`  [INFERRED]
  app/src/main/java/com/benimgunlerim/ui/components/organisms/ShopItemCard.kt → app/src/main/java/com/benimgunlerim/ui/components/core/AppBadge.kt
- `TaskRow()` --calls--> `AppBadge()`  [INFERRED]
  app/src/main/java/com/benimgunlerim/ui/components/organisms/TaskRow.kt → app/src/main/java/com/benimgunlerim/ui/components/core/AppBadge.kt

## Import Cycles
- None detected.

## Communities (188 total, 79 thin omitted)

### Community 0 - "TodayScreen.kt"
Cohesion: 0.16
Nodes (16): bouncyClick(), ConfettiIntensity, Full, LevelUp, Medium, Mini, ConfettiOverlay(), ConfettiParticle (+8 more)

### Community 1 - "SettingsViewModel"
Cohesion: 0.07
Nodes (14): DataCleared, ExportFailed, ExportSaved, ExportWriteFailed, ImportParseFailed, ImportReadFailed, ImportSuccess, SettingsEvent (+6 more)

### Community 2 - "PlanViewModel"
Cohesion: 0.12
Nodes (14): AchievementsScreen(), Composable, Modifier, ScreenScaffold(), BarChart(), BarChartEntry, Dp, Modifier (+6 more)

### Community 3 - "TodayViewModel"
Cohesion: 0.18
Nodes (4): StateFlow, ViewModel, OverdueTasksMoved, TodayViewModel

### Community 4 - "OnboardingScreen"
Cohesion: 0.10
Nodes (6): StateFlow, ViewModel, RoutinesViewModel, RoutineCardUi, RoutineDetailRoutineUi, RoutinesViewModelTest

### Community 5 - "Common.kt"
Cohesion: 0.10
Nodes (16): AppButton(), AppButtonVariant, Danger, Ghost, Primary, Secondary, ImageVector, Modifier (+8 more)

### Community 6 - "ErrorReporterTest"
Cohesion: 0.07
Nodes (8): CrashlyticsErrorReporter, ErrorReporter, LocalErrorReporter, StoredErrorReport, ErrorReporterTest, RecordingReporter, LocalErrorReporterTest, FirebaseCrashlytics

### Community 8 - "AccessibilityTest"
Cohesion: 0.05
Nodes (14): AccessibilityTest, ActivityScenarioRule, AndroidComposeTestRule, ActivityScenarioRule, AndroidComposeTestRule, RoutinesScreenTest, ActivityScenarioRule, AndroidComposeTestRule (+6 more)

### Community 9 - "UserPreferencesRepository"
Cohesion: 0.07
Nodes (5): Keys, Flow, UserPreferencesRepository, UserPreferencesSource, UserPreferencesWriter

### Community 10 - "DeleteTaskUseCaseTest"
Cohesion: 0.27
Nodes (3): DailySummarySchedule, DailySummaryScheduler, PendingIntent

### Community 11 - "BenimGünlerim — Tasarım Standardı"
Cohesion: 0.04
Nodes (45): 10. Durumlar ve Geri Bildirim, 11. Erişilebilirlik Standardı, 12. İçerik Tonu, 13. Tasarım Karar Kontrol Listesi, 14. İlgili Belgeler, 15. Global UI Kuralları, 16. Tasarım Teslim Kontrolü, 1. Tasarım Vaadi (+37 more)

### Community 13 - "SubTaskEntity"
Cohesion: 0.07
Nodes (6): SubTaskEntity, Flow, SubTaskDao, ToggleSubTaskUseCase, FakeSubTaskDao, FakeSubTaskDao

### Community 14 - "AchievementEntity"
Cohesion: 0.15
Nodes (3): AchievementEntity, StubAchievementDao, FakeAchievementDao

### Community 16 - "BenimGünlerim — Ürün Felsefesi, Amaçları ve Kullanıcı Akışları"
Cohesion: 0.07
Nodes (30): 10. Başarı Kriterleri, 11. Geliştirme Kararları İçin Kısa Filtre, 12. Ürün Yönü, 1. Bu Belgenin Amacı, 2. Ürünün Kısa Tanımı, 3. Temel Ürün Amacı, 4.1 Küçük adımlar önemlidir, 4.2 Kullanıcı suçlanmaz (+22 more)

### Community 17 - "DailyStateEntity"
Cohesion: 0.06
Nodes (9): DailyStateDaoTest, DailyStateRepository, Flow, DailyStateDao, Flow, DailyStateEntity, StubDailyStateDao, FakeDailyStateDao (+1 more)

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
Cohesion: 0.07
Nodes (20): DayTasksCard(), Color, Modifier, PlanMetaPill(), PlanOverdueActionButton(), PlanOverdueCard(), PlanSectionShell(), PlanTaskRow() (+12 more)

### Community 28 - "CompletionLogEntity"
Cohesion: 0.15
Nodes (3): CompletionLogRepository, CompletionLogRepositoryImpl, Flow

### Community 29 - "RoutineEntity"
Cohesion: 0.17
Nodes (3): Result, ToggleTaskUseCase, ToggleTaskUseCaseTest

### Community 30 - "ProgressViewModel"
Cohesion: 0.06
Nodes (10): DataImportResult, DataImportService, ImportPayload, JSONArray, T, FakeAchievementDao, FakeLocalDataClearer, FakePreferencesWriter (+2 more)

### Community 31 - "CloseDayUseCaseTest"
Cohesion: 0.10
Nodes (3): CloseDayUseCase, Result, CloseDayUseCaseTest

### Community 33 - "RoutineDetailViewModelTest"
Cohesion: 0.06
Nodes (17): DateTimeProvider, FixedDateTimeProvider, SystemDateTimeProvider, BroadcastReceiver, Context, Intent, RoutineReminderReceiver, DetailPill() (+9 more)

### Community 34 - "TodayViewModelTest"
Cohesion: 0.10
Nodes (4): AddSubTaskUseCase, AutoCloseMissedDayUseCase, MoveTaskToDateUseCase, TodayViewModelTest

### Community 36 - "FakeAchievementDao"
Cohesion: 0.16
Nodes (3): AchievementTrackerTest, FakeAchievementDao, Flow

### Community 42 - "Plan Sayfası Uçtan Uca Derin İnceleme (2026-04-30)"
Cohesion: 0.32
Nodes (6): AppChoiceChip(), AppFilterChip(), ImageVector, Modifier, AddTaskSheet(), Modifier

### Community 43 - "ToggleRoutineUseCaseTest"
Cohesion: 0.05
Nodes (14): AchievementUnlocked, GameEvent, LevelUp, RewardEarned, Flow, RewardDisplayService, AlreadyGranted, Granted (+6 more)

### Community 46 - "FakeCompletionLogDao"
Cohesion: 0.09
Nodes (18): AppIcon(), Color, Dp, ImageVector, Modifier, GoldBadge(), Modifier, InfoCard() (+10 more)

### Community 48 - "Plan Sayfası Sprint Planı (2026-04-30)"
Cohesion: 0.05
Nodes (14): CompletionLogDaoTest, DatabaseTransactionRunner, T, AppDatabase, LocalDataClearer, LocalDataClearerImpl, AppModule, Context (+6 more)

### Community 49 - "NotificationHelper.kt"
Cohesion: 0.11
Nodes (19): AppCrashHandler, ReportingUncaughtExceptionHandler, BenimGunlerimApplication, buildSnoozeAction(), ensureMorningNotificationChannel(), ensureRoutineNotificationChannel(), ensureTaskNotificationChannel(), PendingIntent (+11 more)

### Community 50 - "10. Sprint 5 — Header, Günü Kapat ve Dünü Tamamla Polish"
Cohesion: 0.15
Nodes (13): Faz B — Core Atomlar + Gamification Atomlar, `ui/components/core/AppBadge.kt`, `ui/components/core/AppButton.kt`, `ui/components/core/AppChip.kt`, `ui/components/core/AppDivider.kt`, `ui/components/core/AppIcon.kt`, `ui/components/core/AppSurface.kt`, `ui/components/gamification/CompanionBubble.kt` (+5 more)

### Community 54 - "RewardGrantServiceTest"
Cohesion: 0.11
Nodes (13): EmptyState(), Composable, Modifier, CloseDayCard(), Modifier, Modifier, TaskRow(), Modifier (+5 more)

### Community 56 - "today_ui_agent_sprint_sistemi.md"
Cohesion: 0.17
Nodes (11): 7 Katmanlı Mimari, BenimGünlerim — Agent Kılavuzu, Demir Kurallar — İstisna Yok, Ekran Başına Hangi Bileşenler, Faz Durumu, Graphify, Hedef Dosya Hiyerarşisi (Faz E sonrası), Mevcut Durum — Geçiş Süreci (+3 more)

### Community 58 - "CompletionLogDaoTest"
Cohesion: 0.17
Nodes (12): Faz C — Moleküller + Layout Katmanı, `ui/components/layout/AppTopBar.kt`, `ui/components/layout/ScreenScaffold.kt`, `ui/components/molecules/AlertBanner.kt`, `ui/components/molecules/BarChart.kt`, `ui/components/molecules/ColorRailCard.kt`, `ui/components/molecules/EmptyState.kt`, `ui/components/molecules/InfoCard.kt` (+4 more)

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
Cohesion: 0.13
Nodes (11): AppTopBar(), Modifier, BenimGunlerimApp(), Destination, Plan, Progress, Routines, Settings (+3 more)

### Community 65 - "TodayUiEffect"
Cohesion: 0.17
Nodes (7): ActionFailed, DaySaved, TaskCompletedUndo, TaskDeleted, TaskMovedTomorrow, TodayUiEffect, TodayUiState

### Community 66 - "CarryPendingTasksUseCaseTest"
Cohesion: 0.30
Nodes (13): androidx, ExplainerCard(), IntensityCard(), IntensityOption, Color, NeedCard(), NeedOption, OnboardingScreen() (+5 more)

### Community 67 - "RoutineDao"
Cohesion: 0.13
Nodes (3): RoutineEntity, FakeRoutineDao, FakeRoutineDao

### Community 70 - "Dosya Bazlı Problem Listesi"
Cohesion: 0.18
Nodes (11): Faz D — Organizmalar, `ui/components/organisms/AchievementRow.kt`, `ui/components/organisms/AddRoutineSheet.kt`, `ui/components/organisms/AddTaskSheet.kt`, `ui/components/organisms/CloseDayCard.kt`, `ui/components/organisms/HeaderProgressCard.kt`, `ui/components/organisms/LevelHeroCard.kt`, `ui/components/organisms/RoutineRow.kt` (+3 more)

### Community 71 - "8. Sprint 3 — Card & Button System"
Cohesion: 0.27
Nodes (3): AnalyticsEvent, AnalyticsTracker, LocalAnalyticsTracker

### Community 72 - "RoutineReminderScheduler"
Cohesion: 0.23
Nodes (3): PendingIntent, RoutineReminderScheduler, RoutineReminderSchedulerContract

### Community 73 - "FakeTaskDao"
Cohesion: 0.14
Nodes (6): AchievementsUiState, AchievementsViewModel, AchievementWithStatus, StateFlow, ViewModel, AchievementsViewModelTest

### Community 75 - "FakeTaskDao"
Cohesion: 0.29
Nodes (3): PendingIntent, MorningPlannerSchedule, MorningPlannerScheduler

### Community 76 - "BenimGunlerim Production Readiness Denetimi"
Cohesion: 0.31
Nodes (5): completedRoutineIds(), Flow, ObserveTodaySnapshotUseCase, TodaySnapshot, Flow

### Community 77 - "Release Checklist - BenimGünlerim"
Cohesion: 0.17
Nodes (11): 10. Supply Chain ve Repo Kontrolleri, 1. Signing Kurulumu, 2. Versioning, 3. Zorunlu Kalite Kapısı, 4. Cihaz Smoke Test, 5. Backup ve Gizlilik Kontrolü, 6. Play Store Akışı, 7. Post-Release (+3 more)

### Community 79 - "7. Sprint 2 — Semantic Color System"
Cohesion: 0.67
Nodes (3): AppDivider(), AppVerticalDivider(), Modifier

### Community 82 - "AppDestination"
Cohesion: 0.17
Nodes (10): Achievements, AppDestination, Onboarding, Plan, Progress, RoutineDetailPattern, Routines, Settings (+2 more)

### Community 88 - "6. Birimleri 9/10'a Tasima Plani"
Cohesion: 0.50
Nodes (3): Dp, Modifier, LevelDots()

### Community 89 - "Teknik Görevler"
Cohesion: 0.33
Nodes (6): AlertBanner(), AlertBannerSeverity, Error, Info, Warning, Modifier

### Community 90 - "Teknik Görevler"
Cohesion: 0.29
Nodes (4): BootReceiver, BroadcastReceiver, Context, Intent

### Community 92 - "Gizlilik Politikası Taslağı"
Cohesion: 0.18
Nodes (10): Analitik, Dışa Aktarma ve Geri Yükleme, Gizlilik Politikası Taslağı, Hata Raporlama, İletişim, İzinler, Toplanan Veriler, Veri Silme (+2 more)

### Community 94 - "TimeChangeReceiver"
Cohesion: 0.33
Nodes (4): BroadcastReceiver, Context, Intent, TimeChangeReceiver

### Community 95 - "AppDatabaseMigrationTest"
Cohesion: 0.29
Nodes (3): AppDatabaseMigrationTest, Context, SupportSQLiteDatabase

### Community 96 - ".add"
Cohesion: 0.14
Nodes (10): AchievementEvaluationService, CloseDaySheet(), Color, Modifier, OutlinedSmallButton(), SheetSectionHeader(), SummaryTile(), TaskDetailSheet() (+2 more)

### Community 98 - "TimeValidation"
Cohesion: 0.22
Nodes (6): TimeInputValidator, TimeValidation, Incomplete, InvalidClock, InvalidFormat, Ok

### Community 102 - "BenimGunlerim Production Ready 9/10 Sprint Plani"
Cohesion: 0.07
Nodes (6): PendingIntent, TaskReminderScheduler, TaskReminderSchedulerContract, AddTaskUseCaseTest, DeleteTaskUseCaseTest, UpdateTaskUseCaseTest

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

### Community 117 - "ProgressBar"
Cohesion: 0.07
Nodes (23): AppSurface(), Color, Dp, Modifier, Modifier, LevelBadge(), Modifier, StreakBadge() (+15 more)

### Community 118 - "EmptyState"
Cohesion: 0.22
Nodes (10): AppBadge(), AppBadgeVariant, Error, Info, Neutral, Success, Warning, AppColorBadge() (+2 more)

### Community 135 - "Dependency ve Platform Sağlığı Politikası"
Cohesion: 0.25
Nodes (7): Cadence, Dependency ve Platform Sağlığı Politikası, Otomasyon, Sürümleme Kuralı, Takip Edilecek Borçlar, targetSdk Politikası, Zorunlu Kontroller

### Community 136 - "Performans Politikası"
Cohesion: 0.25
Nodes (7): Büyük Veri Senaryoları (Manual Deep Gate), CI Deep Gate, Gate Komutları, Performans Politikası, Threshold'lar, Uygulama Başlangıç İlkeleri, Ölçüm Kapsamı

### Community 137 - "Test Stratejisi"
Cohesion: 0.22
Nodes (8): Flaky Test Politikasi, Kısa Vadeli Hedefler, Nightly veya Manual Deep Gate, PR Gate, Release Gate, Risk Odakli Test Alanlari, Test Stratejisi, UI ve Görsel Doğrulama Standardı

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

## Knowledge Gaps
- **268 isolated node(s):** `Keys`, `ValidationError`, `Failure`, `TASK`, `ROUTINE` (+263 more)
  These have ≤1 connection - possible missing edges or undocumented components.
- **79 thin communities (<3 nodes) omitted from report** — run `graphify query` to explore isolated nodes.

## Suggested Questions
_Questions this graph is uniquely positioned to answer:_

- **Why does `TaskEntity` connect `TaskEntity` to `TodayViewModel`, `PlanViewModelTest`, `JSONObject`, `OnboardingViewModelTest`, `RoutinesViewModelTest`, `RoutineEntity`, `ProgressViewModel`, `TodayViewModelTest`, `TaskDaoTest`, `TaskRepositoryImpl`, `DataExportServiceTest`, `SettingsViewModelTest`, `TaskDao`, `FakeCompletionLogDao`, `FakePrefs`, `UserPreferences`, `DateTimeProvider`, `TodayUiEffect`, `DailyStateRepository`, `DeleteTaskUseCase`, `AddTaskUseCaseTest`, `RestoreTaskUseCase`, `UpdateTaskTitleUseCase`, `UpdateTaskUseCase`, `BenimGunlerim Production Ready 9/10 Sprint Plani`, `TaskCompletionState`?**
  _High betweenness centrality (0.104) - this node is a cross-community bridge._
- **Why does `UserPreferences` connect `UserPreferences` to `SettingsViewModel`, `TodayViewModelTest`, `DataExportServiceTest`, `UserPreferencesRepository`, `ToggleRoutineUseCaseTest`, `BenimGunlerim Production Readiness Denetimi`, `ObserveProgressSnapshotUseCaseTest`, `FakeSubTaskDao`, `FakeAchievementDao`, `ShopViewModelTest`, `JSONObject`, `OnboardingViewModelTest`, `DateTimeProvider`, `RoutineEntity`, `ProgressViewModel`, `CloseDayUseCaseTest`?**
  _High betweenness centrality (0.094) - this node is a cross-community bridge._
- **Why does `TodayViewModelTest` connect `TodayViewModelTest` to `TodayViewModel`, `UserPreferencesRepository`, `SubTaskEntity`, `SystemFeedbackManager`, `OnboardingViewModelTest`, `RoutineEntity`, `CloseDayUseCaseTest`, `RoutineDetailViewModelTest`, `ToggleRoutineUseCaseTest`, `TickerProvider`, `AchievementTracker`, `UpdateRoutineProgressUseCase`, `8. Sprint 3 — Card & Button System`, `BenimGunlerim Production Readiness Denetimi`, `CarryPendingTasksUseCase`, `DeleteTaskUseCase`, `ObserveSubTasksUseCase`, `RestoreTaskUseCase`, `SaveMissedDaySummaryUseCase`, `UpdateTaskTitleUseCase`, `UpdateTaskUseCase`, `ColorRailCard`, `SectionBlock`, `AddSubTaskUseCase`?**
  _High betweenness centrality (0.077) - this node is a cross-community bridge._
- **Are the 10 inferred relationships involving `TaskEntity` (e.g. with `.task()` and `.persistedTask()`) actually correct?**
  _`TaskEntity` has 10 INFERRED edges - model-reasoned connections that need verification._
- **Are the 10 inferred relationships involving `RoutineEntity` (e.g. with `.schedulers_scheduleAndCancel_withoutCrash()` and `.routineIsScheduledFor_returnsFalseWhenTokenHasWhitespace()`) actually correct?**
  _`RoutineEntity` has 10 INFERRED edges - model-reasoned connections that need verification._
- **Are the 11 inferred relationships involving `CompletionLogEntity` (e.g. with `.log()` and `.setProgress()`) actually correct?**
  _`CompletionLogEntity` has 11 INFERRED edges - model-reasoned connections that need verification._
- **What connects `Keys`, `ValidationError`, `Failure` to the rest of the system?**
  _268 weakly-connected nodes found - possible documentation gaps or missing edges._