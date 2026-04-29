package com.benimgunlerim.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.benimgunlerim.analytics.AnalyticsEvent
import com.benimgunlerim.analytics.AnalyticsTracker
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.model.RoutineTargetType
import com.benimgunlerim.domain.usecase.AddRoutineUseCase
import com.benimgunlerim.domain.usecase.AddTaskUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import java.time.DayOfWeek
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val preferencesRepository: UserPreferencesRepository,
    private val addRoutineUseCase: AddRoutineUseCase,
    private val addTaskUseCase: AddTaskUseCase,
    private val dateTimeProvider: DateTimeProvider,
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
                addRoutineUseCase(
                    name = name,
                    targetDays = DayOfWeek.entries.toSet(),
                    preferredTime = null,
                    targetType = RoutineTargetType.CHECK.value,
                    targetValue = null,
                    targetUnit = null,
                )
            }
            // Add approved task for today (if any)
            if (!approvedTaskTitle.isNullOrBlank()) {
                addTaskUseCase(
                    title = approvedTaskTitle,
                    date = dateTimeProvider.today(),
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

