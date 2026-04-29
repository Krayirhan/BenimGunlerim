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

class AddTaskUseCaseTest {

    private val taskRepository: TaskRepository = mockk()
    private val taskReminderScheduler: TaskReminderSchedulerContract = mockk(relaxed = true)
    private val fixedDate = LocalDate.of(2025, 1, 15)
    private val dateTimeProvider = FixedDateTimeProvider(
        fixedDate = fixedDate,
        fixedTime = LocalTime.of(10, 0),
        fixedMillis = 1_000L,
    )

    private val useCase = AddTaskUseCase(taskRepository, taskReminderScheduler, dateTimeProvider)

    private fun persistedTask(reminderTime: String? = null) = TaskEntity(
        id = "t1",
        title = "Test görev",
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
            taskRepository.addTask(any(), any(), any(), any(), any(), any(), any())
        } returns persistedTask()
    }

    @Test
    fun invoke_callsRepositoryWithCorrectParams() = runTest {
        useCase("Test görev")

        coVerify { taskRepository.addTask("Test görev", fixedDate, null, null, null, 2, null) }
    }

    @Test
    fun invoke_schedulesReminder_whenReminderTimeSet() = runTest {
        val task = persistedTask(reminderTime = "09:00")
        coEvery { taskRepository.addTask(any(), any(), any(), any(), any(), any(), any()) } returns task

        useCase("Test görev", reminderTime = "09:00")

        coVerify {
            taskReminderScheduler.schedule(
                taskId = "t1",
                taskTitle = "Test görev",
                date = fixedDate,
                time = LocalTime.of(9, 0),
            )
        }
    }

    @Test
    fun invoke_noSchedule_whenReminderTimeNull() = runTest {
        useCase("Test görev")

        coVerify(exactly = 0) { taskReminderScheduler.schedule(any(), any(), any(), any()) }
    }

    @Test
    fun invoke_returnsPersistedTask() = runTest {
        val expected = persistedTask()

        val result = useCase("Test görev")

        assertEquals(expected, result)
    }
}
