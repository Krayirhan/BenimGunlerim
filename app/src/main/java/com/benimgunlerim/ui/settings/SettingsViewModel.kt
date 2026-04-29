package com.benimgunlerim.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.data.DataExportService
import com.benimgunlerim.data.DataImportService
import com.benimgunlerim.data.LocalDataClearer
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesAccess
import com.benimgunlerim.notifications.DailySummarySchedule
import com.benimgunlerim.notifications.MorningPlannerSchedule
import com.benimgunlerim.notifications.NotificationPolicyCache
import com.benimgunlerim.notifications.ReminderRestorer
import com.benimgunlerim.domain.normalizedTimeOrNull
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalTime
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesAccess,
    private val localDataClearer: LocalDataClearer,
    private val dataExportService: DataExportService,
    private val dataImportService: DataImportService,
    private val dailySummaryScheduler: DailySummarySchedule,
    private val morningPlannerScheduler: MorningPlannerSchedule,
    private val reminderBootstrapper: ReminderRestorer,
    private val reminderPolicy: NotificationPolicyCache,
) : ViewModel() {
    private val _dataOperationMessage = MutableStateFlow<SettingsEvent?>(null)
    val dataOperationMessage: StateFlow<SettingsEvent?> = _dataOperationMessage.asStateFlow()

    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences(),
    )
        val backupInfo: String = "Yedekleme: Görevler, rutinler, günlük özetler ve ayarlar Android bulut yedeğine dahil edilir. Gizlilik politikasını inceleyin."

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
            localDataClearer.clearAllLocalData()
            preferencesRepository.resetOnboarding()
            _dataOperationMessage.value = SettingsEvent.DataCleared
        }
    }

    fun exportData(onReady: (String) -> Unit) {
        viewModelScope.launch {
            val json = dataExportService.exportToJson()
            if (json == null) {
                _dataOperationMessage.value = SettingsEvent.ExportFailed
            } else {
                onReady(json)
            }
        }
    }

    fun importData(json: String) {
        viewModelScope.launch {
            val result = dataImportService.importFromJson(json)
            if (result == null) {
                _dataOperationMessage.value = SettingsEvent.ImportParseFailed
                return@launch
            }
            syncReminderPolicyCache()
            reminderBootstrapper.rescheduleReminders()
            _dataOperationMessage.value = SettingsEvent.ImportSuccess(result.tasks, result.routines)
        }
    }

    fun setDataOperationMessage(event: SettingsEvent) {
        _dataOperationMessage.value = event
    }

    fun clearDataOperationMessage() {
        _dataOperationMessage.value = null
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
