package com.benimgunlerim.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.data.BenimGunlerimRepository
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.notifications.DailySummaryScheduler
import com.benimgunlerim.notifications.MorningPlannerScheduler
import com.benimgunlerim.notifications.ReminderPolicy
import com.benimgunlerim.domain.normalizedTimeOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val repository: BenimGunlerimRepository,
    private val dailySummaryScheduler: DailySummaryScheduler,
    private val morningPlannerScheduler: MorningPlannerScheduler,
    private val reminderPolicy: ReminderPolicy,
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences(),
    )

    fun setNotificationMode(mode: String) {
        viewModelScope.launch {
            preferencesRepository.setNotificationMode(mode)
            syncReminderPolicyCache()
            if (mode == "off") {
                dailySummaryScheduler.cancel()
            } else {
                scheduleDailySummary(preferences.value.dailySummaryTime)
            }
        }
    }

    fun setDailySummaryTime(time: String) {
        val normalized = time.normalizedTimeOrNull() ?: return
        viewModelScope.launch {
            preferencesRepository.setDailySummaryTime(normalized)
            if (preferences.value.notificationMode != "off") {
                scheduleDailySummary(normalized)
            }
        }
    }

    fun setAnalyticsEnabled(enabled: Boolean) {
        viewModelScope.launch { preferencesRepository.setAnalyticsEnabled(enabled) }
    }

    fun setThemeMode(mode: String) {
        viewModelScope.launch { preferencesRepository.setThemeMode(mode) }
    }

    fun clearLocalData() {
        viewModelScope.launch {
            repository.clearAllLocalData()
            preferencesRepository.resetOnboarding()
        }
    }

    private fun scheduleDailySummary(time: String) {
        val parsed = time.normalizedTimeOrNull()?.let { LocalTime.parse(it) } ?: LocalTime.of(21, 0)
        dailySummaryScheduler.schedule(parsed)
    }

    fun setMorningPlannerEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setMorningPlannerEnabled(enabled)
            if (enabled) {
                scheduleMorningPlanner(preferences.value.morningPlannerTime)
            } else {
                morningPlannerScheduler.cancel()
            }
        }
    }

    fun setMorningPlannerTime(time: String) {
        val normalized = time.normalizedTimeOrNull() ?: return
        viewModelScope.launch {
            preferencesRepository.setMorningPlannerTime(normalized)
            if (preferences.value.morningPlannerEnabled) {
                scheduleMorningPlanner(normalized)
            }
        }
    }

    fun setQuietHoursEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferencesRepository.setQuietHoursEnabled(enabled)
            syncReminderPolicyCache()
        }
    }

    fun setQuietHoursStart(time: String) {
        val normalized = time.normalizedTimeOrNull() ?: return
        viewModelScope.launch {
            preferencesRepository.setQuietHoursStart(normalized)
            syncReminderPolicyCache()
        }
    }

    fun setQuietHoursEnd(time: String) {
        val normalized = time.normalizedTimeOrNull() ?: return
        viewModelScope.launch {
            preferencesRepository.setQuietHoursEnd(normalized)
            syncReminderPolicyCache()
        }
    }

    private suspend fun syncReminderPolicyCache() {
        val prefs = preferencesRepository.preferences.first()
        reminderPolicy.updateCache(
            notificationMode = prefs.notificationMode,
            quietHoursEnabled = prefs.quietHoursEnabled,
            quietHoursStart = prefs.quietHoursStart,
            quietHoursEnd = prefs.quietHoursEnd,
        )
    }

    private fun scheduleMorningPlanner(time: String) {
        val parsed = time.normalizedTimeOrNull()?.let { LocalTime.parse(it) } ?: LocalTime.of(8, 0)
        morningPlannerScheduler.schedule(parsed)
    }
}

