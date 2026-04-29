package com.benimgunlerim.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.benimgunlerim.data.local.entity.TaskEntity
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
class TaskDaoTest {

    private lateinit var db: AppDatabase
    private lateinit var dao: TaskDao

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        dao = db.taskDao()
    }

    @After
    fun tearDown() = db.close()

    // ── Insert / observe ─────────────────────────────────────────────────────

    @Test
    fun insert_and_observeByDate_returnsInsertedTask() = runTest {
        dao.insert(task("t1", date = "2025-01-15"))

        val result = dao.observeByDate("2025-01-15").first()

        assertEquals(1, result.size)
        assertEquals("Test Task", result[0].title)
    }

    @Test
    fun observeByDate_differentDate_returnsEmpty() = runTest {
        dao.insert(task("t1", date = "2025-01-15"))

        val result = dao.observeByDate("2025-01-16").first()

        assertTrue(result.isEmpty())
    }

    @Test
    fun insertAll_insertsMultiple() = runTest {
        dao.insertAll(listOf(task("t1", "2025-01-15"), task("t2", "2025-01-15")))

        val result = dao.observeByDate("2025-01-15").first()

        assertEquals(2, result.size)
    }

    // ── Update / completion state ─────────────────────────────────────────────

    @Test
    fun update_changesFields() = runTest {
        dao.insert(task("t1", date = "2025-01-15"))

        dao.update(task("t1", date = "2025-01-15").copy(title = "Updated Title"))

        val result = dao.observeByDate("2025-01-15").first()
        assertEquals("Updated Title", result[0].title)
    }

    @Test
    fun setCompletionStateById_updatesState() = runTest {
        dao.insert(task("t1", date = "2025-01-15"))

        dao.setCompletionStateById("t1", "completed", completedAt = 1_000L)

        val result = dao.observeByDate("2025-01-15").first()
        assertEquals("completed", result[0].completionState)
        assertEquals(1_000L, result[0].completedAt)
    }

    @Test
    fun setCompletionStateById_clearing_setsNullCompletedAt() = runTest {
        dao.insert(task("t1", "2025-01-15").copy(completionState = "completed", completedAt = 1_000L))

        dao.setCompletionStateById("t1", "pending", completedAt = null)

        val result = dao.observeByDate("2025-01-15").first()
        assertEquals("pending", result[0].completionState)
        assertNull(result[0].completedAt)
    }

    // ── Overdue ───────────────────────────────────────────────────────────────

    @Test
    fun observeOverdue_returnsPendingBeforeToday() = runTest {
        dao.insert(task("t1", date = "2025-01-13").copy(completionState = "pending"))
        dao.insert(task("t2", date = "2025-01-14").copy(completionState = "pending"))
        dao.insert(task("t3", date = "2025-01-15").copy(completionState = "pending")) // today — excluded

        val result = dao.observeOverdue("2025-01-15").first()

        assertEquals(2, result.size)
        assertTrue(result.all { it.plannedDate < "2025-01-15" })
    }

    @Test
    fun observeOverdue_excludesCompletedTasks() = runTest {
        dao.insert(task("t1", date = "2025-01-14").copy(completionState = "completed"))
        dao.insert(task("t2", date = "2025-01-14").copy(completionState = "pending"))

        val result = dao.observeOverdue("2025-01-15").first()

        assertEquals(1, result.size)
        assertEquals("t2", result[0].id)
    }

    @Test
    fun observeOverdue_excludesArchivedTasks() = runTest {
        dao.insert(task("t1", date = "2025-01-14").copy(completionState = "pending", isArchived = true))

        val result = dao.observeOverdue("2025-01-15").first()

        assertTrue(result.isEmpty())
    }

    // ── Delete ────────────────────────────────────────────────────────────────

    @Test
    fun deleteById_removesTask() = runTest {
        dao.insert(task("t1", "2025-01-15"))

        dao.deleteById("t1")

        val result = dao.observeByDate("2025-01-15").first()
        assertTrue(result.isEmpty())
    }

    @Test
    fun deleteAll_removesEverything() = runTest {
        dao.insertAll(listOf(task("t1", "2025-01-15"), task("t2", "2025-01-16")))

        dao.deleteAll()

        assertEquals(0, dao.count())
    }

    // ── getPendingBefore ──────────────────────────────────────────────────────

    @Test
    fun getPendingBefore_returnsOnlyPendingBeforeDate() = runTest {
        dao.insert(task("t1", "2025-01-14").copy(completionState = "pending"))
        dao.insert(task("t2", "2025-01-15").copy(completionState = "pending")) // on today, excluded
        dao.insert(task("t3", "2025-01-14").copy(completionState = "completed")) // completed, excluded

        val result = dao.getPendingBefore("2025-01-15")

        assertEquals(1, result.size)
        assertEquals("t1", result[0].id)
    }

    // ── Count ─────────────────────────────────────────────────────────────────

    @Test
    fun count_returnsCorrectNumber() = runTest {
        assertEquals(0, dao.count())
        dao.insertAll(listOf(task("t1", "2025-01-15"), task("t2", "2025-01-15")))
        assertEquals(2, dao.count())
    }

    // ── observeRange ─────────────────────────────────────────────────────────

    @Test
    fun observeRange_returnsTasksInRange() = runTest {
        dao.insertAll(listOf(
            task("t1", "2025-01-13"),
            task("t2", "2025-01-14"),
            task("t3", "2025-01-15"),
            task("t4", "2025-01-16"),
        ))

        val result = dao.observeRange("2025-01-14", "2025-01-15").first()

        assertEquals(2, result.size)
        val ids = result.map { it.id }.toSet()
        assertTrue("t2" in ids && "t3" in ids)
    }

    // ── Priority ordering ─────────────────────────────────────────────────────

    @Test
    fun observeByDate_orderedByPriorityDesc() = runTest {
        dao.insertAll(listOf(
            task("low",   "2025-01-15").copy(priority = 1, createdAt = 1L),
            task("high",  "2025-01-15").copy(priority = 3, createdAt = 2L),
            task("norm",  "2025-01-15").copy(priority = 2, createdAt = 3L),
        ))

        val result = dao.observeByDate("2025-01-15").first()

        // All have no startTime → sorted by priority DESC
        assertEquals("high", result[0].id)
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private fun task(id: String, date: String) = TaskEntity(
        id = id,
        title = "Test Task",
        note = null,
        plannedDate = date,
        startTime = null,
        endTime = null,
        category = null,
        color = null,
        completionState = "pending",
        completedAt = null,
        sourceTemplateId = null,
        createdAt = 1_000L,
        updatedAt = 1_000L,
        priority = 2,
        reminderTime = null,
    )
}
