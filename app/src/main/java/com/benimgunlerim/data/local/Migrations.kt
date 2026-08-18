package com.benimgunlerim.data.local

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

/**
 * Room veritabanı migration geçmişi.
 *
 * ## Politika kararı (Sprint 1 — Stabilizasyon, 2026-08-16)
 *
 * v1-v5 için migration nesnesi yoktur ve bilerek yazılmayacaktır: uygulama
 * versionCode=1 / versionName=0.1.0 ile hâlâ pre-release aşamasındadır — hiçbir
 * Play Store track'ine (internal dahil) yayınlanmamıştır, `app/schemas/` altında
 * yalnızca `7.json` bulunur (v1-5 için şema anlık görüntüsü hiç commit edilmemiş),
 * repoda release tag'i yoktur. Yani "gerçek kullanıcıda v1-5 şemasıyla veri var"
 * senaryosu yoktur; bu geçmişi yapay migration'larla üretmenin katma değeri yok.
 *
 * Karar: v1-v5 "desteklenmeyen pre-release şema" sayılır; v6 gerçek migration
 * başlangıcıdır.
 * `AppModule.provideDatabase()` v1-v5 için `fallbackToDestructiveMigrationFrom`
 * kullanır. v6 için gerçek `MIGRATION_6_7` bulunduğundan v6 fallback listesine
 * eklenmez; aynı başlangıç sürümünü hem migration hem fallback olarak vermek
 * Room tarafından reddedilir.
 *
 * Bu karardan sonraki (v7 → v8 ve devamı, yani ilk gerçek yayından itibaren)
 * HER schema değişikliği migration zorunludur — fallback bir daha genişletilmez.
 *
 * KURAL: Her schema değişikliğinde (v7'den itibaren):
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
        db.execSQL("ALTER TABLE `tasks` ADD COLUMN `priority` INTEGER NOT NULL DEFAULT 2")
        db.execSQL("ALTER TABLE `tasks` ADD COLUMN `reminderTime` TEXT")
        db.execSQL("ALTER TABLE `tasks` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `tasks` ADD COLUMN `isArchived` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `tasks` ADD COLUMN `postponedFromDate` TEXT")

        // routines tablosuna yeni sütunlar
        db.execSQL("ALTER TABLE `routines` ADD COLUMN `targetType` TEXT NOT NULL DEFAULT 'check'")
        db.execSQL("ALTER TABLE `routines` ADD COLUMN `targetValue` INTEGER")
        db.execSQL("ALTER TABLE `routines` ADD COLUMN `targetUnit` TEXT")
        db.execSQL("ALTER TABLE `routines` ADD COLUMN `category` TEXT")
        db.execSQL("ALTER TABLE `routines` ADD COLUMN `reminderEnabled` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `routines` ADD COLUMN `sortOrder` INTEGER NOT NULL DEFAULT 0")
        db.execSQL("ALTER TABLE `routines` ADD COLUMN `bestStreak` INTEGER NOT NULL DEFAULT 0")

        // completion_logs tablosuna yeni sütunlar
        db.execSQL("ALTER TABLE `completion_logs` ADD COLUMN `value` REAL")
        db.execSQL("ALTER TABLE `completion_logs` ADD COLUMN `targetValue` REAL")
        db.execSQL("ALTER TABLE `completion_logs` ADD COLUMN `skipReason` TEXT")

        // daily_states tablosuna yeni sütunlar
        db.execSQL("ALTER TABLE `daily_states` ADD COLUMN `bestMoment` TEXT")
        db.execSQL("ALTER TABLE `daily_states` ADD COLUMN `challenge` TEXT")
        db.execSQL("ALTER TABLE `daily_states` ADD COLUMN `tomorrowIntention` TEXT")
        db.execSQL("ALTER TABLE `daily_states` ADD COLUMN `closedAt` INTEGER")
        db.execSQL("ALTER TABLE `daily_states` ADD COLUMN `carriedTaskCount` INTEGER NOT NULL DEFAULT 0")

        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_plannedDate_completionState` ON `tasks` (`plannedDate`, `completionState`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_tasks_plannedDate_isArchived` ON `tasks` (`plannedDate`, `isArchived`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_routines_isArchived_preferredTime` ON `routines` (`isArchived`, `preferredTime`)")
        db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_completion_logs_entityType_entityId_date` ON `completion_logs` (`entityType`, `entityId`, `date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_completion_logs_date` ON `completion_logs` (`date`)")
        db.execSQL("CREATE INDEX IF NOT EXISTS `index_completion_logs_entityId` ON `completion_logs` (`entityId`)")
    }
}
