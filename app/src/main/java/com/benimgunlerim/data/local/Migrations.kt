package com.benimgunlerim.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room veritabanı migration geçmişi.
 *
 * KURAL: Her schema değişikliğinde:
 * 1. AppDatabase.version artırılır.
 * 2. Burada yeni bir MIGRATION_ nesnesi yazılır.
 * 3. AppModule.provideDatabase() içine .addMigrations(MIGRATION_X_Y) eklenir.
 * 4. app/schemas/ klasöründeki JSON commit edilir.
 * 5. fallbackToDestructiveMigration() KULLANILMAZ.
 */

/**
 * v6 → v7: subtasks tablosu eklendi.
 *
 * TaskEntity'e subtask desteği eklenmesiyle birlikte tasks tablosuna bağlı
 * alt görevleri tutan `subtasks` tablosu ve `taskId` üzerinde index oluşturuldu.
 */
val MIGRATION_6_7 = object : Migration(6, 7) {
    override fun migrate(db: SupportSQLiteDatabase) {
        // subtasks tablosu
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `subtasks` (
                `id`          TEXT NOT NULL,
                `taskId`      TEXT NOT NULL,
                `title`       TEXT NOT NULL,
                `isCompleted` INTEGER NOT NULL DEFAULT 0,
                `sortOrder`   INTEGER NOT NULL DEFAULT 0,
                `createdAt`   INTEGER NOT NULL,
                PRIMARY KEY(`id`),
                FOREIGN KEY(`taskId`) REFERENCES `tasks`(`id`)
                    ON UPDATE NO ACTION ON DELETE CASCADE
            )
            """.trimIndent()
        )
        db.execSQL(
            "CREATE INDEX IF NOT EXISTS `index_subtasks_taskId` ON `subtasks` (`taskId`)"
        )

        // tasks tablosuna yeni sütunlar (v6'da yoktu)
        runCatching { db.execSQL("ALTER TABLE `tasks` ADD COLUMN `priority` INTEGER NOT NULL DEFAULT 2") }
        runCatching { db.execSQL("ALTER TABLE `tasks` ADD COLUMN `reminderTime` TEXT") }
        runCatching { db.execSQL("ALTER TABLE `tasks` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0") }
        runCatching { db.execSQL("ALTER TABLE `tasks` ADD COLUMN `isArchived` INTEGER NOT NULL DEFAULT 0") }
        runCatching { db.execSQL("ALTER TABLE `tasks` ADD COLUMN `postponedFromDate` TEXT") }

        // routines tablosuna yeni sütunlar
        runCatching { db.execSQL("ALTER TABLE `routines` ADD COLUMN `targetType` TEXT NOT NULL DEFAULT 'check'") }
        runCatching { db.execSQL("ALTER TABLE `routines` ADD COLUMN `targetValue` INTEGER") }
        runCatching { db.execSQL("ALTER TABLE `routines` ADD COLUMN `targetUnit` TEXT") }
        runCatching { db.execSQL("ALTER TABLE `routines` ADD COLUMN `category` TEXT") }
        runCatching { db.execSQL("ALTER TABLE `routines` ADD COLUMN `reminderEnabled` INTEGER NOT NULL DEFAULT 0") }
        runCatching { db.execSQL("ALTER TABLE `routines` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0") }
        runCatching { db.execSQL("ALTER TABLE `routines` ADD COLUMN `bestStreak` INTEGER NOT NULL DEFAULT 0") }

        // completion_logs tablosuna yeni sütunlar
        runCatching { db.execSQL("ALTER TABLE `completion_logs` ADD COLUMN `value` REAL") }
        runCatching { db.execSQL("ALTER TABLE `completion_logs` ADD COLUMN `targetValue` REAL") }
        runCatching { db.execSQL("ALTER TABLE `completion_logs` ADD COLUMN `skipReason` TEXT") }

        // daily_states tablosuna yeni sütunlar
        runCatching { db.execSQL("ALTER TABLE `daily_states` ADD COLUMN `bestMoment` TEXT") }
        runCatching { db.execSQL("ALTER TABLE `daily_states` ADD COLUMN `challenge` TEXT") }
        runCatching { db.execSQL("ALTER TABLE `daily_states` ADD COLUMN `tomorrowIntention` TEXT") }
        runCatching { db.execSQL("ALTER TABLE `daily_states` ADD COLUMN `closedAt` INTEGER") }
        runCatching { db.execSQL("ALTER TABLE `daily_states` ADD COLUMN `carriedTaskCount` INTEGER NOT NULL DEFAULT 0") }
    }
}
