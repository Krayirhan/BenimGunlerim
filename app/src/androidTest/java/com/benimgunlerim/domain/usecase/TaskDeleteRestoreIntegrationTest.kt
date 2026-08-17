package com.benimgunlerim.domain.usecase

import android.content.Context
import androidx.room.Room
import androidx.room.withTransaction
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.CompletionLogRepositoryImpl
import com.benimgunlerim.data.DatabaseTransactionRunner
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.TaskRepositoryImpl
import com.benimgunlerim.data.local.AppDatabase
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.notifications.TaskReminderSchedulerContract
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Reproduces the reported subtask-loss bug: deleting a task with subtasks
 * cascade-deletes them (Room `ON DELETE CASCADE`), and Undo used to bring
 * the task back without them. Exercises [DeleteTaskUseCase] and
 * [RestoreTaskUseCase] against a real in-memory Room database end to end.
 */
@RunWith(AndroidJUnit4::class)
class TaskDeleteRestoreIntegrationTest {

    private lateinit var db: AppDatabase
    private lateinit var taskRepository: TaskRepository
    private lateinit var completionLogRepository: CompletionLogRepository
    private lateinit var transactionRunner: DatabaseTransactionRunner
    private lateinit var deleteTaskUseCase: DeleteTaskUseCase
    private lateinit var restoreTaskUseCase: RestoreTaskUseCase

    private val noopScheduler = object : TaskReminderSchedulerContract {
        override fun schedule(taskId: String, taskTitle: String, date: LocalDate, time: LocalTime) = Unit
        override fun cancel(taskId: String) = Unit
    }

    private val fixedDateTimeProvider = object : DateTimeProvider {
        override fun today(): LocalDate = LocalDate.of(2025, 1, 15)
        override fun currentTime(): LocalTime = LocalTime.of(12, 0)
        override fun currentTimeMillis(): Long = 1_000L
    }

    @Before
    fun setUp() {
        val context: Context = ApplicationProvider.getApplicationContext()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()

        taskRepository = TaskRepositoryImpl(db.taskDao(), db.subTaskDao(), db.completionLogDao(), fixedDateTimeProvider)
        completionLogRepository = CompletionLogRepositoryImpl(db.completionLogDao())
        transactionRunner = object : DatabaseTransactionRunner {
            override suspend fun <T> runInTransaction(block: suspend () -> T): T = db.withTransaction { block() }
        }
        deleteTaskUseCase = DeleteTaskUseCase(taskRepository, completionLogRepository, noopScheduler, transactionRunner)
        restoreTaskUseCase = RestoreTaskUseCase(taskRepository, transactionRunner)
    }

    @After
    fun tearDown() = db.close()

    private val task = TaskEntity(
        id = "t1",
        title = "Alışveriş listesi",
        note = null,
        plannedDate = "2025-01-15",
        startTime = null,
        endTime = null,
        category = null,
        color = null,
        completionState = "pending",
        completedAt = null,
        sourceTemplateId = null,
        createdAt = 1_000L,
        updatedAt = 1_000L,
    )

    @Test
    fun deleteTask_cascadeDeletesSubTasks() = runTest {
        db.taskDao().insert(task)
        db.subTaskDao().insertAll(
            listOf(
                subTask("s1", "Subtask A"),
                subTask("s2", "Subtask B"),
                subTask("s3", "Subtask C"),
            ),
        )

        deleteTaskUseCase(task)

        assertTrue(db.subTaskDao().getByTaskId("t1").isEmpty())
    }

    @Test
    fun deleteThenUndo_restoresTaskWithAllThreeSubTasks() = runTest {
        db.taskDao().insert(task)
        db.subTaskDao().insertAll(
            listOf(
                subTask("s1", "Subtask A"),
                subTask("s2", "Subtask B"),
                subTask("s3", "Subtask C"),
            ),
        )

        val snapshot = deleteTaskUseCase(task)
        // Task and its subtasks are gone after delete (cascade).
        assertTrue(db.taskDao().observeByDate("2025-01-15").first().isEmpty())
        assertTrue(db.subTaskDao().getByTaskId("t1").isEmpty())

        restoreTaskUseCase(task, snapshot)

        val restoredTasks = db.taskDao().observeByDate("2025-01-15").first()
        assertEquals(1, restoredTasks.size)
        val restoredSubTasks = db.subTaskDao().getByTaskId("t1")
        assertEquals(3, restoredSubTasks.size)
        assertEquals(setOf("Subtask A", "Subtask B", "Subtask C"), restoredSubTasks.map { it.title }.toSet())
    }

    @Test
    fun deleteThenUndo_taskWithoutSubTasks_restoresCleanly() = runTest {
        db.taskDao().insert(task)

        val snapshot = deleteTaskUseCase(task)
        restoreTaskUseCase(task, snapshot)

        val restoredTasks = db.taskDao().observeByDate("2025-01-15").first()
        assertEquals(1, restoredTasks.size)
        assertTrue(db.subTaskDao().getByTaskId("t1").isEmpty())
    }

    private fun subTask(id: String, title: String) = com.benimgunlerim.data.local.entity.SubTaskEntity(
        id = id,
        taskId = "t1",
        title = title,
        isCompleted = false,
        sortOrder = 0,
        createdAt = 1_000L,
    )
}
