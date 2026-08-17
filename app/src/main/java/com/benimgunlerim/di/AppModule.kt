package com.benimgunlerim.di

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import com.benimgunlerim.BuildConfig
import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.CompletionLogRepositoryImpl
import com.benimgunlerim.data.DatabaseTransactionRunner
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.TaskRepositoryImpl
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.analytics.CrashlyticsErrorReporter
import com.benimgunlerim.analytics.ErrorReporter
import com.benimgunlerim.analytics.LocalAnalyticsTracker
import com.benimgunlerim.analytics.LocalErrorReporter
import com.benimgunlerim.data.LocalDataClearer
import com.benimgunlerim.data.LocalDataClearerImpl
import com.benimgunlerim.data.UserPreferencesAccess
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.UserPreferencesSource
import com.benimgunlerim.data.UserPreferencesWriter
import com.benimgunlerim.notifications.DailySummarySchedule
import com.benimgunlerim.notifications.DailySummaryScheduler
import com.benimgunlerim.notifications.MorningPlannerSchedule
import com.benimgunlerim.notifications.MorningPlannerScheduler
import com.benimgunlerim.notifications.NotificationPolicyCache
import com.benimgunlerim.notifications.ReminderBootstrapper
import com.benimgunlerim.notifications.ReminderPolicy
import com.benimgunlerim.notifications.ReminderRestorer
import com.benimgunlerim.notifications.RoutineReminderScheduler
import com.benimgunlerim.notifications.RoutineReminderSchedulerContract
import com.benimgunlerim.notifications.TaskReminderScheduler
import com.benimgunlerim.notifications.TaskReminderSchedulerContract
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.FeedbackManager
import com.benimgunlerim.domain.RandomProvider
import com.benimgunlerim.domain.SystemDateTimeProvider
import com.benimgunlerim.domain.SystemFeedbackManager
import com.benimgunlerim.domain.SystemRandomProvider
import com.benimgunlerim.domain.SystemTickerProvider
import com.benimgunlerim.domain.TickerProvider
import com.benimgunlerim.data.local.AchievementDao
import com.benimgunlerim.data.local.AppDatabase
import com.benimgunlerim.data.local.CompletionLogDao
import com.benimgunlerim.data.local.DailyStateDao
import com.benimgunlerim.data.local.RoutineDao
import com.benimgunlerim.data.local.SubTaskDao
import com.benimgunlerim.data.local.TaskDao
import com.benimgunlerim.data.local.MIGRATION_6_7
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

@Module
@InstallIn(SingletonComponent::class)
object AppModule {
    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder(context, AppDatabase::class.java, "benim_gunlerim.db")
            .addMigrations(MIGRATION_6_7)
            // v1-6 pre-release şema olarak "desteklenmiyor" kabul edildi — bkz. Migrations.kt.
            // Uygulama hiç yayınlanmadığı için bu aralıkta gerçek kullanıcı verisi yok;
            // eski bir dev/test cihazında bu şema bulunursa crash yerine sessiz reset olur.
            .fallbackToDestructiveMigrationFrom(1, 2, 3, 4, 5, 6)
            .build()

    @Provides
    fun provideTaskDao(database: AppDatabase): TaskDao = database.taskDao()

    @Provides
    fun provideRoutineDao(database: AppDatabase): RoutineDao = database.routineDao()

    @Provides
    fun provideCompletionLogDao(database: AppDatabase): CompletionLogDao = database.completionLogDao()

    @Provides
    fun provideDailyStateDao(database: AppDatabase): DailyStateDao = database.dailyStateDao()

    @Provides
    fun provideAchievementDao(database: AppDatabase): AchievementDao = database.achievementDao()

    @Provides
    @Singleton
    fun provideSubTaskDao(database: AppDatabase): SubTaskDao = database.subTaskDao()

    @Provides
    @Singleton
    fun provideDatabaseTransactionRunner(database: AppDatabase): DatabaseTransactionRunner =
        object : DatabaseTransactionRunner {
            override suspend fun <T> runInTransaction(block: suspend () -> T): T =
                database.withTransaction { block() }
        }

    @Provides
    @Singleton
    fun provideAnalyticsTracker(tracker: LocalAnalyticsTracker): AnalyticsTracker = tracker

    @Provides
    @Singleton
    fun provideErrorReporter(
        local: LocalErrorReporter,
        crashlytics: CrashlyticsErrorReporter,
    ): ErrorReporter = if (BuildConfig.DEBUG) local else crashlytics

    @Provides
    @Singleton
    fun provideUserPreferencesSource(repo: UserPreferencesRepository): UserPreferencesSource = repo

    @Provides
    @Singleton
    fun provideUserPreferencesWriter(repo: UserPreferencesRepository): UserPreferencesWriter = repo

    @Provides
    @Singleton
    fun provideUserPreferencesAccess(repo: UserPreferencesRepository): UserPreferencesAccess = repo

    @Provides
    @Singleton
    fun provideTaskRepository(impl: TaskRepositoryImpl): TaskRepository = impl

    @Provides
    @Singleton
    fun provideCompletionLogRepository(impl: CompletionLogRepositoryImpl): CompletionLogRepository = impl

    @Provides
    @Singleton
    fun provideLocalDataClearer(impl: LocalDataClearerImpl): LocalDataClearer = impl

    @Provides
    @Singleton
    fun provideDailySummarySchedule(scheduler: DailySummaryScheduler): DailySummarySchedule = scheduler

    @Provides
    @Singleton
    fun provideMorningPlannerSchedule(scheduler: MorningPlannerScheduler): MorningPlannerSchedule = scheduler

    @Provides
    @Singleton
    fun provideReminderRestorer(bootstrapper: ReminderBootstrapper): ReminderRestorer = bootstrapper

    @Provides
    @Singleton
    fun provideNotificationPolicyCache(policy: ReminderPolicy): NotificationPolicyCache = policy

    @Provides
    @Singleton
    fun provideDateTimeProvider(): DateTimeProvider = SystemDateTimeProvider()

    @Provides
    @Singleton
    fun provideRandomProvider(): RandomProvider = SystemRandomProvider()

    @Provides
    @Singleton
    fun provideTaskReminderSchedulerContract(scheduler: TaskReminderScheduler): TaskReminderSchedulerContract = scheduler

    @Provides
    @Singleton
    fun provideRoutineReminderSchedulerContract(scheduler: RoutineReminderScheduler): RoutineReminderSchedulerContract = scheduler

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    @Provides
    @Singleton
    fun provideFeedbackManager(impl: SystemFeedbackManager): FeedbackManager = impl

    @Provides
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher = Dispatchers.IO

    @Provides
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher = Dispatchers.Default

    @Provides
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher = Dispatchers.Main

    @Provides
    @Singleton
    fun provideTickerProvider(): TickerProvider = SystemTickerProvider()
}
