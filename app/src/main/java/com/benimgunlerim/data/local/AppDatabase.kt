package com.benimgunlerim.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.benimgunlerim.data.local.entity.AchievementEntity
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.data.local.entity.TaskEntity

@Database(
    entities = [
        TaskEntity::class,
        RoutineEntity::class,
        CompletionLogEntity::class,
        DailyStateEntity::class,
        AchievementEntity::class,
        SubTaskEntity::class,
    ],
    version = 7,
    exportSchema = true,
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun routineDao(): RoutineDao
    abstract fun completionLogDao(): CompletionLogDao
    abstract fun dailyStateDao(): DailyStateDao
    abstract fun achievementDao(): AchievementDao
    abstract fun subTaskDao(): SubTaskDao
}
