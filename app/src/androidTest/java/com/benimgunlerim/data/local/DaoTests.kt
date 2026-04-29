package com.benimgunlerim.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.DailyStateEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class CompletionLogDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: CompletionLogDao

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.completionLogDao()
    }

    @After
    fun tearDown() = db.close()

    // ── Insert / observe ─────────────────────────────────────────────────────

    @Test
    fun upsert_and_observeByDate_returnsLog() = runTest {
        dao.upsert(log("log1", "task", "t1", "2025-01-15", "completed"))

        val result = dao.observeByDate("2025-01-15").first()

        assertEquals(1, result.size)
        assertEquals("t1", result[0].entityId)
    }

    @Test
    fun observeByDate_differentDate_returnsEmpty() = runTest {
        dao.upsert(log("log1", "task", "t1", "2025-01-15", "completed"))

        val result = dao.observeByDate("2025-01-16").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun upsert_replacesExistingLog() = runTest {
        dao.upsert(log("log1", "task", "t1", "2025-01-15", "completed"))
        dao.upsert(log("log1", "task", "t1", "2025-01-15", "skipped"))

        val result = dao.observeByDate("2025-01-15").first()

        assertEquals(1, result.size)
        assertEquals("skipped", result[0].status)
    }

    @Test
    fun observeAll_returnsAllLogs() = runTest {
        dao.upsert(log("log1", "task", "t1", "2025-01-15", "completed"))
        dao.upsert(log("log2", "routine", "r1", "2025-01-14", "completed"))

        val result = dao.observeAll().first()

        assertEquals(2, result.size)
    }

    @Test
    fun observeBetween_returnsLogsInRange() = runTest {
        dao.upsert(log("log1", "task", "t1", "2025-01-13", "completed"))
        dao.upsert(log("log2", "task", "t2", "2025-01-14", "completed"))
        dao.upsert(log("log3", "task", "t3", "2025-01-15", "completed"))
        dao.upsert(log("log4", "task", "t4", "2025-01-16", "completed"))

        val result = dao.observeBetween("2025-01-14", "2025-01-15").first()

        assertEquals(2, result.size)
        val ids = result.map { it.id }.toSet()
        assertTrue("log2" in ids && "log3" in ids)
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test
    fun deleteForDate_removesOnlyMatchingLog() = runTest {
        dao.upsert(log("log1", "task", "t1", "2025-01-15", "completed"))
        dao.upsert(log("log2", "task", "t1", "2025-01-16", "completed"))

        dao.deleteForDate("task", "t1", "2025-01-15")

        val all = dao.observeAll().first()
        assertEquals(1, all.size)
        assertEquals("log2", all[0].id)
    }

    @Test
    fun deleteForEntity_removesAllLogsForEntity() = runTest {
        dao.upsert(log("log1", "task", "t1", "2025-01-15", "completed"))
        dao.upsert(log("log2", "task", "t1", "2025-01-16", "completed"))
        dao.upsert(log("log3", "task", "t2", "2025-01-15", "completed"))

        dao.deleteForEntity("task", "t1")

        val all = dao.observeAll().first()
        assertEquals(1, all.size)
        assertEquals("t2", all[0].entityId)
    }

    @Test
    fun deleteAll_removesEverything() = runTest {
        dao.upsert(log("log1", "task", "t1", "2025-01-15", "completed"))
        dao.upsert(log("log2", "routine", "r1", "2025-01-15", "completed"))

        dao.deleteAll()

        assertTrue(dao.observeAll().first().isEmpty())
    }

    @Test
    fun insertAll_insertsMultipleLogs() = runTest {
        val logs = listOf(
            log("log1", "task", "t1", "2025-01-15", "completed"),
            log("log2", "task", "t2", "2025-01-15", "skipped"),
        )
        dao.insertAll(logs)

        val result = dao.observeByDate("2025-01-15").first()
        assertEquals(2, result.size)
    }

    // ── Unique constraint (entityType + entityId + date) ─────────────────────

    @Test
    fun upsert_uniqueConstraint_lastWriteWins() = runTest {
        dao.upsert(log("log-v1", "task", "t1", "2025-01-15", "completed"))
        dao.upsert(log("log-v2", "task", "t1", "2025-01-15", "skipped"))

        val result = dao.observeByDate("2025-01-15").first()

        // Only one row; the second upsert replaces (same entityType+entityId+date)
        assertEquals(1, result.size)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun log(
        id: String,
        entityType: String,
        entityId: String,
        date: String,
        status: String,
    ) = CompletionLogEntity(
        id = id,
        entityType = entityType,
        entityId = entityId,
        date = date,
        completedAt = 1_000L,
        status = status,
        note = null,
    )
}

// ────────────────────────────────────────────────────────────────────────────

@RunWith(AndroidJUnit4::class)
class DailyStateDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: DailyStateDao

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.dailyStateDao()
    }

    @After
    fun tearDown() = db.close()

    // ── Insert / observe ─────────────────────────────────────────────────────

    @Test
    fun upsert_and_getByDate_returnsState() = runTest {
        dao.upsert(state("2025-01-15", "harika"))

        val result = dao.getByDate("2025-01-15")

        assertNotNull(result)
        assertEquals("harika", result!!.mood)
    }

    @Test
    fun getByDate_missingDate_returnsNull() = runTest {
        val result = dao.getByDate("2025-01-15")

        assertNull(result)
    }

    @Test
    fun upsert_updatesExistingEntry() = runTest {
        dao.upsert(state("2025-01-15", "normal"))
        dao.upsert(state("2025-01-15", "harika"))

        val result = dao.getByDate("2025-01-15")

        assertEquals("harika", result!!.mood)
    }

    @Test
    fun observeByDate_emitsUpdates() = runTest {
        dao.upsert(state("2025-01-15", "iyi"))

        val result = dao.observeByDate("2025-01-15").first()

        assertNotNull(result)
        assertEquals("iyi", result!!.mood)
    }

    @Test
    fun observeRecent_returnsLimitedDesc() = runTest {
        listOf("2025-01-15", "2025-01-14", "2025-01-13", "2025-01-12").forEach {
            dao.upsert(state(it, "normal"))
        }

        val result = dao.observeRecent(3).first()

        assertEquals(3, result.size)
        // Most recent first
        assertEquals("2025-01-15", result[0].date)
    }

    @Test
    fun deleteAll_removesAllStates() = runTest {
        dao.upsert(state("2025-01-15", "harika"))
        dao.upsert(state("2025-01-14", "kotu"))

        dao.deleteAll()

        assertTrue(dao.getAll().isEmpty())
    }

    @Test
    fun insertAll_insertsMultiple() = runTest {
        dao.insertAll(listOf(
            state("2025-01-15", "harika"),
            state("2025-01-14", "iyi"),
        ))

        assertEquals(2, dao.getAll().size)
    }

    // ── closedAt field ────────────────────────────────────────────────────────

    @Test
    fun upsert_preservesClosedAt() = runTest {
        dao.upsert(state("2025-01-15", "harika").copy(closedAt = 9_999L))

        val result = dao.getByDate("2025-01-15")

        assertEquals(9_999L, result!!.closedAt)
    }

    @Test
    fun upsert_notClosed_closedAtIsNull() = runTest {
        dao.upsert(state("2025-01-15", "normal"))

        val result = dao.getByDate("2025-01-15")

        assertNull(result!!.closedAt)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun state(date: String, mood: String) = DailyStateEntity(
        date = date,
        mood = mood,
        energyLevel = 3,
        completionRate = 0.8f,
        note = "Test note",
        reflection = null,
        dailyScore = 80,
        closedAt = null,
    )
}
