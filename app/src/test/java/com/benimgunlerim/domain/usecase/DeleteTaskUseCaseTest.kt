package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.DatabaseTransactionRunner
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.notifications.TaskReminderSchedulerContract
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class DeleteTaskUseCaseTest {

    private val taskRepository: TaskRepository = mockk()
    private val completionLogRepository: CompletionLogRepository = mockk()
    private val taskReminderScheduler: TaskReminderSchedulerContract = mockk(relaxed = true)
    private val transactionRunner: DatabaseTransactionRunner = mockk()

    private val useCase = DeleteTaskUseCase(
        taskRepository,
        completionLogRepository,
        taskReminderScheduler,
        transactionRunner,
    )

    private val task = TaskEntity(
        id = "t1",
        title = "Silinecek görev",
        note = null,
        plannedDate = LocalDate.of(2025, 1, 15).toString(),
        startTime = null,
        endTime = null,
        category = null,
        color = null,
        completionState = "PENDING",
        completedAt = null,
        sourceTemplateId = null,
        createdAt = 1_000L,
        updatedAt = 1_000L,
    )

    private val subTasks = listOf(
        SubTaskEntity(id = "s1", taskId = "t1", title = "Alt görev A", createdAt = 1_000L),
        SubTaskEntity(id = "s2", taskId = "t1", title = "Alt görev B", createdAt = 1_000L),
    )

    @Before
    fun setUp() {
        coEvery { taskRepository.delete(any()) } returns Unit
        coEvery { taskRepository.getSubTasks(any()) } returns subTasks
        coEvery { completionLogRepository.deleteForEntity(any(), any()) } returns Unit
        coEvery { transactionRunner.runInTransaction<Unit>(any()) } coAnswers {
            val block: suspend () -> Unit = firstArg()
            block()
        }
    }

    @Test
    fun invoke_returnsSubTaskSnapshot_beforeCascadeDelete() = runTest {
        val result = useCase(task)

        assertEquals(subTasks, result)
        coVerify { taskRepository.getSubTasks("t1") }
    }

    @Test
    fun invoke_cancelsReminder() = runTest {
        useCase(task)

        coVerify { taskReminderScheduler.cancel("t1") }
    }

    @Test
    fun invoke_deletesTask() = runTest {
        useCase(task)

        coVerify { taskRepository.delete(task) }
    }

    @Test
    fun invoke_deletesCompletionLogs_withTaskType() = runTest {
        useCase(task)

        coVerify { completionLogRepository.deleteForEntity(CompletionEntityType.TASK.value, "t1") }
    }

    @Test
    fun invoke_dbOperationsRunInTransaction() = runTest {
        useCase(task)

        coVerify { transactionRunner.runInTransaction<Unit>(any()) }
    }

    @Test
    fun invoke_cancelReminderBeforeTransaction() = runTest {
        useCase(task)

        coVerifyOrder {
            taskReminderScheduler.cancel("t1")
            transactionRunner.runInTransaction<Unit>(any())
        }
    }
}
