package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.DatabaseTransactionRunner
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.FixedDateTimeProvider
import com.benimgunlerim.domain.model.TaskCompletionState
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class CarryPendingTasksUseCaseTest {

    private val taskRepository: TaskRepository = mockk()
    private val completionLogRepository: CompletionLogRepository = mockk(relaxed = true)
    private val transactionRunner: DatabaseTransactionRunner = mockk()
    private val fixedDate = LocalDate.of(2025, 1, 15)
    private val dateTimeProvider = FixedDateTimeProvider(
        fixedDate = fixedDate,
        fixedTime = LocalTime.of(8, 0),
        fixedMillis = 1_000L,
    )

    private val useCase = CarryPendingTasksUseCase(
        taskRepository,
        completionLogRepository,
        dateTimeProvider,
        transactionRunner,
    )

    @Before
    fun setUp() {
        coEvery { transactionRunner.runInTransaction<Unit>(any()) } coAnswers {
            val block: suspend () -> Unit = firstArg()
            block()
        }
        coEvery { taskRepository.update(any()) } returns Unit
    }

    private fun pendingTask(id: String, plannedDate: String) = TaskEntity(
        id = id,
        title = "Görev $id",
        note = null,
        plannedDate = plannedDate,
        startTime = null,
        endTime = null,
        category = null,
        color = null,
        completionState = TaskCompletionState.PENDING.value,
        completedAt = null,
        sourceTemplateId = null,
        createdAt = 1_000L,
        updatedAt = 1_000L,
    )

    @Test
    fun invoke_emptyPending_returnsZero() = runTest {
        coEvery { taskRepository.getPendingBefore(fixedDate) } returns emptyList()

        val result = useCase()

        assertEquals(0, result)
    }

    @Test
    fun invoke_pendingTasks_movedToTomorrow() = runTest {
        val task = pendingTask("t1", fixedDate.minusDays(1).toString())
        coEvery { taskRepository.getPendingBefore(fixedDate) } returns listOf(task)
        val tomorrow = fixedDate.plusDays(1).toString()

        useCase()

        coVerify {
            taskRepository.update(
                match { it.plannedDate == tomorrow && it.id == "t1" },
            )
        }
    }

    @Test
    fun invoke_setsPostponedFromDate_whenNotAlreadySet() = runTest {
        val originalDate = fixedDate.minusDays(1).toString()
        val task = pendingTask("t2", originalDate)
        coEvery { taskRepository.getPendingBefore(fixedDate) } returns listOf(task)

        useCase()

        coVerify {
            taskRepository.update(
                match { it.postponedFromDate == originalDate && it.id == "t2" },
            )
        }
    }

    @Test
    fun invoke_returnsCountOfMovedTasks() = runTest {
        val tasks = listOf(
            pendingTask("t1", fixedDate.minusDays(2).toString()),
            pendingTask("t2", fixedDate.minusDays(1).toString()),
        )
        coEvery { taskRepository.getPendingBefore(fixedDate) } returns tasks

        val result = useCase()

        assertEquals(2, result)
    }
}
