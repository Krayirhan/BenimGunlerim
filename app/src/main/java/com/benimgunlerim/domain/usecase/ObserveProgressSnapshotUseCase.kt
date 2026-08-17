package com.benimgunlerim.domain.usecase

import com.benimgunlerim.data.CompletionLogRepository
import com.benimgunlerim.data.DailyStateRepository
import com.benimgunlerim.data.RoutineRepository
import com.benimgunlerim.data.TaskRepository
import com.benimgunlerim.data.UserPreferences
import com.benimgunlerim.data.UserPreferencesRepository
import com.benimgunlerim.data.currentStreak
import com.benimgunlerim.data.isScheduledFor
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.AchievementDef
import com.benimgunlerim.domain.AchievementTracker
import com.benimgunlerim.domain.ALL_ACHIEVEMENTS
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.ProgressCalculator
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.domain.model.CompletionStatus
import com.benimgunlerim.domain.model.TaskCompletionState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine

data class ProgressDayData(
    val date: String,
    val dailyScore: Int,
    val completionRate: Float,
    val mood: String? = null,
    val energyLevel: Int? = null,
    val note: String? = null,
)

data class ProgressSnapshot(
    val last30Days: List<ProgressDayData>,
    val currentStreak: Int,
    val bestStreak: Int,
    val averageScore: Int,
    val weeklyScore: Int,
    val moodTrend: List<Pair<String, String?>>,
    val energyTrend: List<Pair<String, Int?>>,
    val routineHitRate: Float,
    val taskHitRate: Float,
    val gameState: UserPreferences,
    val unlockedAchievements: List<AchievementDef>,
    val totalAchievements: Int = ALL_ACHIEVEMENTS.size,
)

class ObserveProgressSnapshotUseCase @Inject constructor(
    private val taskRepository: TaskRepository,
    private val routineRepository: RoutineRepository,
    private val dailyStateRepository: DailyStateRepository,
    private val completionLogRepository: CompletionLogRepository,
    private val prefsRepository: UserPreferencesRepository,
    private val achievementTracker: AchievementTracker,
    private val dateTimeProvider: DateTimeProvider,
) {
    operator fun invoke(): Flow<ProgressSnapshot> {
        val today = dateTimeProvider.today()
        val fromDate = today.minusDays(30)

        return combine(
            taskRepository.observeRange(fromDate, today),
            routineRepository.observeActive(),
            dailyStateRepository.observeRecent(limit = 30),
            completionLogRepository.observeBetween(fromDate, today),
            combine(
                prefsRepository.preferences,
                achievementTracker.unlockedAchievements,
            ) { prefs, unlocked -> prefs to unlocked },
        ) { tasks: List<TaskEntity>, routines: List<RoutineEntity>, closedStates: List<DailyStateEntity>, allLogs: List<CompletionLogEntity>, extra: Pair<UserPreferences, List<AchievementDef>> ->
            val (prefs, unlocked) = extra

            val dayDataList = (0..29).map { offset ->
                val date = today.minusDays(offset.toLong())
                val dateStr = date.toString()
                val closedState = closedStates.firstOrNull { it.date == dateStr }

                val dayTasks = tasks.filter { it.plannedDate == dateStr }
                val completedTasks = dayTasks.count { it.completionState == TaskCompletionState.COMPLETED.value }
                val dayRoutines = routines.filter { it.isScheduledFor(date.dayOfWeek) }
                val dayRoutineLogs = allLogs.filter {
                    it.date == dateStr &&
                    it.entityType == CompletionEntityType.ROUTINE.value &&
                    it.status == CompletionStatus.COMPLETED.value
                }
                val completedRoutines = dayRoutines.count { r -> dayRoutineLogs.any { it.entityId == r.id } }

                val liveRate = ProgressCalculator.dailyProgress(
                    totalTasks = dayTasks.size,
                    completedTasks = completedTasks,
                    totalRoutines = dayRoutines.size,
                    completedRoutines = completedRoutines,
                )
                val liveScore = (liveRate * 100).toInt()

                // Use closed summary if available for past days, otherwise use live calculated score
                val finalScore = if (closedState != null && date != today) closedState.dailyScore else liveScore
                val finalRate = if (closedState != null && date != today) closedState.completionRate else liveRate

                ProgressDayData(
                    date = dateStr,
                    dailyScore = finalScore,
                    completionRate = finalRate,
                    mood = closedState?.mood,
                    energyLevel = closedState?.energyLevel,
                    note = closedState?.note,
                )
            }

            val weekDays = dayDataList.take(7)

            // Routine hit rate calculation only within each routine's active lifespan
            var scheduledRoutineSlots = 0
            var completedRoutineCount = 0

            routines.forEach { routine ->
                val createdDate = runCatching {
                    Instant.ofEpochMilli(routine.createdAt).atZone(ZoneId.systemDefault()).toLocalDate()
                }.getOrNull() ?: today

                val startDate = if (createdDate.isAfter(fromDate)) createdDate else fromDate

                var cursor = startDate
                while (!cursor.isAfter(today)) {
                    if (routine.isScheduledFor(cursor.dayOfWeek)) {
                        scheduledRoutineSlots++
                        val dateStr = cursor.toString()
                        val isCompleted = allLogs.any {
                            it.date == dateStr &&
                            it.entityType == CompletionEntityType.ROUTINE.value &&
                            it.entityId == routine.id &&
                            it.status == CompletionStatus.COMPLETED.value
                        }
                        if (isCompleted) {
                            completedRoutineCount++
                        }
                    }
                    cursor = cursor.plusDays(1)
                }
            }

            val routineHitRate = if (scheduledRoutineSlots > 0) {
                (completedRoutineCount.toFloat() / scheduledRoutineSlots.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }

            val tasksIn30Days = tasks.filter { task ->
                val date = runCatching { LocalDate.parse(task.plannedDate) }.getOrNull()
                date != null && !date.isBefore(fromDate) && !date.isAfter(today)
            }
            val completedTasksCount = tasksIn30Days.count { it.completionState == TaskCompletionState.COMPLETED.value }
            val taskHitRate = if (tasksIn30Days.isNotEmpty()) {
                (completedTasksCount.toFloat() / tasksIn30Days.size.toFloat()).coerceIn(0f, 1f)
            } else {
                0f
            }

            val currentStreak = allLogs.currentStreak(today)
            val bestStreak = maxOf(dayDataList.bestStreak(), currentStreak)

            ProgressSnapshot(
                last30Days = dayDataList,
                currentStreak = currentStreak,
                bestStreak = bestStreak,
                averageScore = if (dayDataList.isEmpty()) 0 else dayDataList.map { it.dailyScore }.average().toInt(),
                weeklyScore = if (weekDays.isEmpty()) 0 else weekDays.map { it.dailyScore }.average().toInt(),
                moodTrend = dayDataList.take(14).map { it.date to it.mood },
                energyTrend = dayDataList.take(14).map { it.date to it.energyLevel },
                routineHitRate = routineHitRate,
                taskHitRate = taskHitRate,
                gameState = prefs,
                unlockedAchievements = unlocked,
            )
        }
    }
}

private fun List<ProgressDayData>.bestStreak(): Int {
    if (isEmpty()) return 0
    val sorted = sortedBy { it.date }
    var best = 0
    var current = 0
    var prevDate: LocalDate? = null
    for (day in sorted) {
        val date = runCatching { LocalDate.parse(day.date) }.getOrNull() ?: continue
        val isSuccessful = day.dailyScore >= 50 || day.completionRate >= 0.5f
        if (isSuccessful) {
            current = if (prevDate != null && date == prevDate!!.plusDays(1)) current + 1 else 1
            if (current > best) best = current
            prevDate = date
        } else {
            current = 0
            prevDate = null
        }
    }
    return best
}
