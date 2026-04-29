package com.benimgunlerim.notifications

import com.benimgunlerim.data.local.TaskDao
import com.benimgunlerim.data.local.RoutineDao
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.di.ApplicationScope
import com.benimgunlerim.di.IoDispatcher
import com.benimgunlerim.domain.DateTimeProvider
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

/** Thin interface so SettingsViewModel can be tested without the full bootstrapper. */
fun interface ReminderRestorer {
    fun rescheduleReminders()
}


@Singleton
class ReminderBootstrapper @Inject constructor(
    private val taskDao: TaskDao,
    private val routineDao: RoutineDao,
    private val taskReminderScheduler: TaskReminderScheduler,
    private val routineReminderScheduler: RoutineReminderScheduler,
    private val dailySummaryScheduler: DailySummaryScheduler,
    private val userPreferencesRepository: UserPreferencesRepository,
    private val dateTimeProvider: DateTimeProvider,
    @ApplicationScope private val applicationScope: CoroutineScope,
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher,
) : ReminderRestorer {
    override fun rescheduleReminders() {
        applicationScope.launch(ioDispatcher) {
            val today = dateTimeProvider.today()
            taskDao.getPendingRemindersFrom(today.toString()).forEach { task ->
                val date = runCatching { LocalDate.parse(task.plannedDate) }.getOrNull()
                val time = task.reminderTime?.let { runCatching { LocalTime.parse(it) }.getOrNull() }
                if (date != null && time != null) {
                    taskReminderScheduler.schedule(task.id, task.title, date, time)
                }
            }

            routineDao.getActiveWithReminder().forEach { routine ->
                routineReminderScheduler.schedule(routine)
            }
            val preferences = userPreferencesRepository.preferences.first()
            if (preferences.notificationMode != "off") {
                val time = runCatching { LocalTime.parse(preferences.dailySummaryTime) }
                    .getOrDefault(LocalTime.of(21, 0))
                dailySummaryScheduler.schedule(time)
            }
        }
    }

    fun rescheduleRoutineReminders() {
        rescheduleReminders()
    }
}
