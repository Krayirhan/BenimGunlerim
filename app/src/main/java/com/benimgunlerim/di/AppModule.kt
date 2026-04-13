package com.benimgunlerim.di

import android.content.Context
import androidx.room.Room
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.analytics.ErrorReporter
import com.benimgunlerim.analytics.LocalAnalyticsTracker
import com.benimgunlerim.analytics.LocalErrorReporter
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.UserPreferencesSource
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
    fun provideAnalyticsTracker(tracker: LocalAnalyticsTracker): AnalyticsTracker = tracker

    @Provides
    @Singleton
    fun provideErrorReporter(reporter: LocalErrorReporter): ErrorReporter = reporter

    @Provides
    @Singleton
    fun provideUserPreferencesSource(repo: UserPreferencesRepository): UserPreferencesSource = repo

    @Provides
    @Singleton
    @ApplicationScope
    fun provideApplicationScope(): CoroutineScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)
}
