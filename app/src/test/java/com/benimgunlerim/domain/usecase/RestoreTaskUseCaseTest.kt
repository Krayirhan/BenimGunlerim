package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.DatabaseTransactionRunner
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.coVerifyOrder
import io.mockk.mockk
import java.time.LocalDate
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

class RestoreTaskUseCaseTest {

    private val taskRepository: TaskRepository = mockk()
    private val transactionRunner: DatabaseTransactionRunner = mockk()

    private val useCase = RestoreTaskUseCase(taskRepository, transactionRunner)

    private val task = TaskEntity(
        id = "t1",
        title = "Geri gelen görev",
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
        SubTaskEntity(id = "s1", taskId = "t1", title = "A", createdAt = 1_000L),
        SubTaskEntity(id = "s2", taskId = "t1", title = "B", createdAt = 1_000L),
        SubTaskEntity(id = "s3", taskId = "t1", title = "C", createdAt = 1_000L),
    )

    @Before
    fun setUp() {
        coEvery { taskRepository.restore(any()) } returns Unit
        coEvery { taskRepository.restoreSubTasks(any()) } returns Unit
        coEvery { transactionRunner.runInTransaction<Unit>(any()) } coAnswers {
            val block: suspend () -> Unit = firstArg()
            block()
        }
    }

    @Test
    fun invoke_restoresTask() = runTest {
        useCase(task, subTasks)

        coVerify { taskRepository.restore(task) }
    }

    @Test
    fun invoke_restoresAllThreeSubTasks() = runTest {
        useCase(task, subTasks)

        coVerify { taskRepository.restoreSubTasks(subTasks) }
    }

    @Test
    fun invoke_withNoSubTasks_stillRestoresTask() = runTest {
        useCase(task)

        coVerify { taskRepository.restore(task) }
        coVerify { taskRepository.restoreSubTasks(emptyList()) }
    }

    @Test
    fun invoke_restoresTaskBeforeSubTasks_toSatisfyForeignKey() = runTest {
        useCase(task, subTasks)

        coVerifyOrder {
            taskRepository.restore(task)
            taskRepository.restoreSubTasks(subTasks)
        }
    }

    @Test
    fun invoke_runsInsideTransaction() = runTest {
        useCase(task, subTasks)

        coVerify { transactionRunner.runInTransaction<Unit>(any()) }
    }
}
