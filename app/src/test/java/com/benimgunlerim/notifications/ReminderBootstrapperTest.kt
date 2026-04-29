package com.benimgunlerim.notifications

import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.local.RoutineDao
import com.benimgunlerim.data.local.TaskDao
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import java.time.LocalDate
import java.time.LocalTime
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ReminderBootstrapperTest {

    private val testDispatcher = StandardTestDispatcher()
    private val testScope = TestScope(testDispatcher)

    private val taskDao: TaskDao = mockk(relaxed = true)
    private val routineDao: RoutineDao = mockk(relaxed = true)
    private val taskReminderScheduler: TaskReminderScheduler = mockk(relaxed = true)
    private val routineReminderScheduler: RoutineReminderScheduler = mockk(relaxed = true)
    private val dailySummaryScheduler: DailySummaryScheduler = mockk(relaxed = true)
    private val prefsRepository: UserPreferencesRepository = mockk(relaxed = true)

    private fun makeBootstrapper() = ReminderBootstrapper(
        taskDao = taskDao,
        routineDao = routineDao,
        taskReminderScheduler = taskReminderScheduler,
        routineReminderScheduler = routineReminderScheduler,
        dailySummaryScheduler = dailySummaryScheduler,
        userPreferencesRepository = prefsRepository,
        applicationScope = testScope,
        ioDispatcher = testDispatcher,
    )

    @Test
    fun rescheduleReminders_schedulesTaskWithFutureReminder() = testScope.runTest {
        val tomorrow = LocalDate.now().plusDays(1)
        val task = makeTask("t1", "Buy milk", tomorrow.toString(), "09:00")
        coEvery { taskDao.getPendingRemindersFrom(any()) } returns listOf(task)
        coEvery { routineDao.getActiveWithReminder() } returns emptyList()
        coEvery { prefsRepository.preferences } returns flowOf(UserPreferences(notificationMode = "on"))

        makeBootstrapper().rescheduleReminders()
        advanceUntilIdle()

        coVerify {
            taskReminderScheduler.schedule(
                "t1",
                "Buy milk",
                tomorrow,
                LocalTime.of(9, 0),
            )
        }
    }

    @Test
    fun rescheduleReminders_schedulesRoutineReminders() = testScope.runTest {
        val routine = makeRoutine("r1", "Morning Run", "07:00")
        coEvery { taskDao.getPendingRemindersFrom(any()) } returns emptyList()
        coEvery { routineDao.getActiveWithReminder() } returns listOf(routine)
        coEvery { prefsRepository.preferences } returns flowOf(UserPreferences(notificationMode = "on"))

        makeBootstrapper().rescheduleReminders()
        advanceUntilIdle()

        coVerify { routineReminderScheduler.schedule(routine) }
    }

    @Test
    fun rescheduleReminders_schedulesDailySummary_whenNotificationsEnabled() = testScope.runTest {
        coEvery { taskDao.getPendingRemindersFrom(any()) } returns emptyList()
        coEvery { routineDao.getActiveWithReminder() } returns emptyList()
        coEvery { prefsRepository.preferences } returns flowOf(
            UserPreferences(notificationMode = "on", dailySummaryTime = "21:00"),
        )

        makeBootstrapper().rescheduleReminders()
        advanceUntilIdle()

        coVerify { dailySummaryScheduler.schedule(LocalTime.of(21, 0)) }
    }

    @Test
    fun rescheduleReminders_doesNotScheduleDailySummary_whenNotificationsOff() = testScope.runTest {
        coEvery { taskDao.getPendingRemindersFrom(any()) } returns emptyList()
        coEvery { routineDao.getActiveWithReminder() } returns emptyList()
        coEvery { prefsRepository.preferences } returns flowOf(UserPreferences(notificationMode = "off"))

        makeBootstrapper().rescheduleReminders()
        advanceUntilIdle()

        coVerify(exactly = 0) { dailySummaryScheduler.schedule(any()) }
    }

    @Test
    fun rescheduleReminders_skipsTask_withNullReminderTime() = testScope.runTest {
        val task = makeTask("t1", "No reminder", LocalDate.now().plusDays(1).toString(), null)
        coEvery { taskDao.getPendingRemindersFrom(any()) } returns listOf(task)
        coEvery { routineDao.getActiveWithReminder() } returns emptyList()
        coEvery { prefsRepository.preferences } returns flowOf(UserPreferences(notificationMode = "on"))

        makeBootstrapper().rescheduleReminders()
        advanceUntilIdle()

        coVerify(exactly = 0) { taskReminderScheduler.schedule(any(), any(), any(), any()) }
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private fun makeTask(id: String, title: String, plannedDate: String, reminderTime: String?) = TaskEntity(
        id = id,
        title = title,
        note = null,
        plannedDate = plannedDate,
        startTime = null,
        endTime = null,
        category = null,
        color = null,
        completionState = "pending",
        completedAt = null,
        sourceTemplateId = null,
        createdAt = 1L,
        updatedAt = 1L,
        reminderTime = reminderTime,
    )

    private fun makeRoutine(id: String, name: String, preferredTime: String?) = RoutineEntity(
        id = id,
        name = name,
        description = null,
        targetDays = "MONDAY,TUESDAY,WEDNESDAY,THURSDAY,FRIDAY,SATURDAY,SUNDAY",
        preferredTime = preferredTime,
        color = null,
        isArchived = false,
        createdAt = 1L,
        updatedAt = 1L,
        reminderEnabled = true,
    )
}
