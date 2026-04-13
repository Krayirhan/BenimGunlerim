package com.benimgunlerim.notifications

import com.benimgunlerim.data.local.RoutineDao
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.di.ApplicationScope
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

@Singleton
class ReminderBootstrapper @Inject constructor(
    private val routineDao: RoutineDao,
    private val routineReminderScheduler: RoutineReminderScheduler,
    private val dailySummaryScheduler: DailySummaryScheduler,
    private val userPreferencesRepository: UserPreferencesRepository,
    @ApplicationScope private val applicationScope: CoroutineScope,
) {
    fun rescheduleRoutineReminders() {
        applicationScope.launch(Dispatchers.IO) {
            routineDao.getActiveWithReminder().forEach { routine ->
                routineReminderScheduler.schedule(routine)
            }
            val preferences = userPreferencesRepository.preferences.first()
            if (preferences.notificationMode != "off") {
                val time = runCatching { java.time.LocalTime.parse(preferences.dailySummaryTime) }
                    .getOrDefault(java.time.LocalTime.of(21, 0))
                dailySummaryScheduler.schedule(time)
            }
        }
    }
}
