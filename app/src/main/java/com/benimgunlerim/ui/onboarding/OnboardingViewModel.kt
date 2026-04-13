package com.benimgunlerim.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.analytics.AnalyticsEvent
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.data.BenimGunlerimRepository
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.LocalDate
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val repository: BenimGunlerimRepository,
    private val analyticsTracker: AnalyticsTracker,
) : ViewModel() {
    val preferences: StateFlow<UserPreferences> = preferencesRepository.preferences.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = UserPreferences(),
    )

    fun completeOnboarding(
        needId: String,
        intensityId: String,
        approvedRoutineNames: List<String>,
        approvedTaskTitle: String?,
    ) {
        viewModelScope.launch {
            // Add only the routines the user explicitly approved — no auto-mock
            approvedRoutineNames.forEach { name ->
                repository.addRoutine(
                    name = name,
                    targetDays = setOf(
                        java.time.DayOfWeek.MONDAY, java.time.DayOfWeek.TUESDAY,
                        java.time.DayOfWeek.WEDNESDAY, java.time.DayOfWeek.THURSDAY,
                        java.time.DayOfWeek.FRIDAY, java.time.DayOfWeek.SATURDAY,
                        java.time.DayOfWeek.SUNDAY,
                    ),
                    preferredTime = null,
                    targetType = "check",
                    targetValue = null,
                    targetUnit = null,
                )
            }
            // Add approved task for today (if any)
            if (!approvedTaskTitle.isNullOrBlank()) {
                repository.addTask(
                    title = approvedTaskTitle,
                    date = LocalDate.now(),
                    note = null,
                    startTime = null,
                    category = null,
                    priority = 2,
                    reminderTime = null,
                )
            }
            preferencesRepository.completeOnboarding(needId)
            preferencesRepository.addXp(10)
            analyticsTracker.track(
                AnalyticsEvent(
                    name = "onboarding_completed",
                    properties = mapOf(
                        "need" to needId,
                        "intensity" to intensityId,
                        "routines_added" to approvedRoutineNames.size.toString(),
                        "task_added" to (!approvedTaskTitle.isNullOrBlank()).toString(),
                    ),
                ),
            )
        }
    }
}

