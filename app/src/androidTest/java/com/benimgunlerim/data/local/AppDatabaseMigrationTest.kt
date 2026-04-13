package com.benimgunlerim.data.local

import android.content.Context
import androidx.room.Room
import androidx.sqlite.db.SupportSQLiteDatabase
import androidx.sqlite.db.SupportSQLiteOpenHelper
import androidx.sqlite.db.framework.FrameworkSQLiteOpenHelperFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    private val context: Context = ApplicationProvider.getApplicationContext()
    private val databaseName = "migration-6-7-test.db"

    @After
    fun tearDown() {
        context.deleteDatabase(databaseName)
    }

    @Test
    fun migration6To7_preservesDataAndAddsNewColumns() {
        createVersion6Database()

        val database = Room.databaseBuilder(context, AppDatabase::class.java, databaseName)
            .addMigrations(MIGRATION_6_7)
            .build()

        val db = database.openHelper.writableDatabase
        assertColumnExists(db, "tasks", "priority")
        assertColumnExists(db, "tasks", "reminderTime")
        assertColumnExists(db, "subtasks", "taskId")

        db.query("SELECT title, priority, isArchived FROM tasks WHERE id = 'task-1'").use { cursor ->
            assertTrue(cursor.moveToFirst())
            assertEquals("Legacy task", cursor.getString(0))
            assertEquals(2, cursor.getInt(1))
            assertEquals(0, cursor.getInt(2))
        }

        database.close()
    }

    private fun createVersion6Database() {
        context.deleteDatabase(databaseName)
        val helper = FrameworkSQLiteOpenHelperFactory().create(
            SupportSQLiteOpenHelper.Configuration.builder(context)
                .name(databaseName)
                .callback(object : SupportSQLiteOpenHelper.Callback(6) {
                    override fun onCreate(db: SupportSQLiteDatabase) {
                        createV6Schema(db)
                        insertLegacyRows(db)
                    }

                    override fun onUpgrade(db: SupportSQLiteDatabase, oldVersion: Int, newVersion: Int) = Unit
                })
                .build(),
        )
        helper.writableDatabase.close()
        helper.close()
    }

    private fun createV6Schema(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `tasks` (
                `id` TEXT NOT NULL,
                `title` TEXT NOT NULL,
                `note` TEXT,
                `plannedDate` TEXT NOT NULL,
                `startTime` TEXT,
                `endTime` TEXT,
                `category` TEXT,
                `color` TEXT,
                `completionState` TEXT NOT NULL,
                `completedAt` INTEGER,
                `sourceTemplateId` TEXT,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `routines` (
                `id` TEXT NOT NULL,
                `name` TEXT NOT NULL,
                `description` TEXT,
                `targetDays` TEXT NOT NULL,
                `preferredTime` TEXT,
                `color` TEXT,
                `isArchived` INTEGER NOT NULL,
                `createdAt` INTEGER NOT NULL,
                `updatedAt` INTEGER NOT NULL,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `completion_logs` (
                `id` TEXT NOT NULL,
                `entityType` TEXT NOT NULL,
                `entityId` TEXT NOT NULL,
                `date` TEXT NOT NULL,
                `completedAt` INTEGER,
                `status` TEXT NOT NULL,
                `note` TEXT,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `daily_states` (
                `date` TEXT NOT NULL,
                `mood` TEXT,
                `energyLevel` INTEGER,
                `completionRate` REAL NOT NULL,
                `note` TEXT,
                `reflection` TEXT,
                `dailyScore` INTEGER NOT NULL,
                PRIMARY KEY(`date`)
            )
            """.trimIndent(),
        )
        db.execSQL(
            """
            CREATE TABLE IF NOT EXISTS `achievements` (
                `id` TEXT NOT NULL,
                `unlockedAt` INTEGER,
                PRIMARY KEY(`id`)
            )
            """.trimIndent(),
        )
    }

    private fun insertLegacyRows(db: SupportSQLiteDatabase) {
        db.execSQL(
            """
            INSERT INTO `tasks` (
                `id`, `title`, `note`, `plannedDate`, `startTime`, `endTime`, `category`, `color`,
                `completionState`, `completedAt`, `sourceTemplateId`, `createdAt`, `updatedAt`
            ) VALUES (
                'task-1', 'Legacy task', NULL, '2026-04-13', NULL, NULL, NULL, NULL,
                'pending', NULL, NULL, 1, 1
            )
            """.trimIndent(),
        )
    }

    private fun assertColumnExists(db: SupportSQLiteDatabase, tableName: String, columnName: String) {
        db.query("PRAGMA table_info(`$tableName`)").use { cursor ->
            var found = false
            while (cursor.moveToNext()) {
                if (cursor.getString(cursor.getColumnIndexOrThrow("name")) == columnName) {
                    found = true
                    break
                }
            }
            assertTrue("Expected $tableName.$columnName to exist", found)
        }
    }
}
