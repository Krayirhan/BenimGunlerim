package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.FixedDateTimeProvider
import com.benimgunlerim.notifications.TaskReminderSchedulerContract
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class UpdateTaskUseCaseTest {

    private val taskRepository: TaskRepository = mockk()
    private val taskReminderScheduler: TaskReminderSchedulerContract = mockk(relaxed = true)
    private val fixedDate = LocalDate.of(2025, 1, 15)
    private val dateTimeProvider = FixedDateTimeProvider(
        fixedDate = fixedDate,
        fixedTime = LocalTime.of(10, 0),
        fixedMillis = 1_000L,
    )

    private val useCase = UpdateTaskUseCase(taskRepository, taskReminderScheduler, dateTimeProvider)

    private fun existingTask(reminderTime: String? = null) = TaskEntity(
        id = "t1",
        title = "Eski başlık",
        note = null,
        plannedDate = fixedDate.toString(),
        startTime = null,
        endTime = null,
        category = null,
        color = null,
        completionState = "PENDING",
        completedAt = null,
        sourceTemplateId = null,
        createdAt = 1_000L,
        updatedAt = 1_000L,
        reminderTime = reminderTime,
    )

    @Before
    fun setUp() {
        coEvery {
            taskRepository.updateFull(any(), any(), any(), any(), any(), any(), any(), any())
        } returns existingTask()
    }

    @Test
    fun invoke_callsUpdateFull_withCorrectParams() = runTest {
        val task = existingTask()

        useCase(task, "Yeni başlık", null, fixedDate, null, null, 2, null)

        coVerify {
            taskRepository.updateFull(task, "Yeni başlık", null, fixedDate, null, null, 2, null)
        }
    }

    @Test
    fun invoke_alwaysCancelsOldAlarm() = runTest {
        val task = existingTask()

        useCase(task, "Yeni başlık", null, fixedDate, null, null, 2, null)

        coVerify { taskReminderScheduler.cancel("t1") }
    }

    @Test
    fun invoke_schedulesNewReminder_whenReminderTimeSet() = runTest {
        val task = existingTask()
        val updated = existingTask(reminderTime = "09:00")
        coEvery {
            taskRepository.updateFull(any(), any(), any(), any(), any(), any(), any(), any())
        } returns updated

        useCase(task, "Yeni başlık", null, fixedDate, null, null, 2, "09:00")

        coVerify {
            taskReminderScheduler.schedule(
                taskId = "t1",
                taskTitle = "Eski başlık",
                date = fixedDate,
                time = LocalTime.of(9, 0),
            )
        }
    }

    @Test
    fun invoke_noSchedule_whenReminderTimeNull() = runTest {
        val task = existingTask()

        useCase(task, "Yeni başlık", null, fixedDate, null, null, 2, null)

        coVerify(exactly = 0) { taskReminderScheduler.schedule(any(), any(), any(), any()) }
    }

    @Test
    fun invoke_returnsUpdatedTask() = runTest {
        val task = existingTask()
        val expected = existingTask()

        val result = useCase(task, "Yeni başlık", null, fixedDate, null, null, 2, null)

        assertEquals(expected, result)
    }
}
