package com.benimgunlerim.data

import com.benimgunlerim.analytics.ErrorReporter
import com.benimgunlerim.data.local.AchievementDao
import com.benimgunlerim.data.local.CompletionLogDao
import com.benimgunlerim.data.local.DailyStateDao
import com.benimgunlerim.data.local.RoutineDao
import com.benimgunlerim.data.local.SubTaskDao
import com.benimgunlerim.data.local.TaskDao
import com.benimgunlerim.data.local.entity.AchievementEntity
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.first
import org.json.JSONArray
import org.json.JSONObject

/**
 * Produces a portable JSON snapshot of user data.
 */
@Singleton
open class DataExportService @Inject constructor(
    private val taskDao: TaskDao,
    private val routineDao: RoutineDao,
    private val subTaskDao: SubTaskDao,
    private val completionLogDao: CompletionLogDao,
    private val dailyStateDao: DailyStateDao,
    private val achievementDao: AchievementDao,
    private val preferencesRepository: UserPreferencesSource,
    private val errorReporter: ErrorReporter,
) {
    companion object {
        const val EXPORT_VERSION = 1
        private val ISO_FMT = DateTimeFormatter.ISO_OFFSET_DATE_TIME.withZone(ZoneId.systemDefault())
    }

    open suspend fun exportToJson(): String? = runCatching {
        val prefs = preferencesRepository.preferences.first()
        JSONObject().apply {
            put("version", EXPORT_VERSION)
            put("exportedAt", ISO_FMT.format(Instant.now()))
            put("tasks", taskDao.getAll().toJsonArray())
            put("subTasks", subTaskDao.getAll().toJsonArray())
            put("routines", routineDao.getAll().toJsonArray())
            put("completionLogs", completionLogDao.getAll().toJsonArray())
            put("dailyStates", dailyStateDao.getAll().toJsonArray())
            put("achievements", achievementDao.getAll().toJsonArray())
            put("preferences", prefs.toJson())
        }.toString(2)
    }.onFailure { e ->
        errorReporter.recordNonFatal(e, mapOf("action" to "data_export"))
    }.getOrNull()

    @JvmName("tasksToJsonArray")
    private fun List<TaskEntity>.toJsonArray(): JSONArray = JSONArray().also { arr ->
        forEach { t ->
            arr.put(JSONObject().apply {
                put("id", t.id)
                put("title", t.title)
                putOpt("note", t.note)
                put("plannedDate", t.plannedDate)
                putOpt("startTime", t.startTime)
                putOpt("endTime", t.endTime)
                putOpt("category", t.category)
                putOpt("color", t.color)
                put("completionState", t.completionState)
                putOpt("completedAt", t.completedAt)
                putOpt("sourceTemplateId", t.sourceTemplateId)
                put("createdAt", t.createdAt)
                put("updatedAt", t.updatedAt)
                put("priority", t.priority)
                putOpt("reminderTime", t.reminderTime)
                put("sortOrder", t.sortOrder)
                put("isArchived", t.isArchived)
                putOpt("postponedFromDate", t.postponedFromDate)
            })
        }
    }

    @JvmName("subTasksToJsonArray")
    private fun List<SubTaskEntity>.toJsonArray(): JSONArray = JSONArray().also { arr ->
        forEach { s ->
            arr.put(JSONObject().apply {
                put("id", s.id)
                put("taskId", s.taskId)
                put("title", s.title)
                put("isCompleted", s.isCompleted)
                put("sortOrder", s.sortOrder)
                put("createdAt", s.createdAt)
            })
        }
    }

    @JvmName("routinesToJsonArray")
    private fun List<RoutineEntity>.toJsonArray(): JSONArray = JSONArray().also { arr ->
        forEach { r ->
            arr.put(JSONObject().apply {
                put("id", r.id)
                put("name", r.name)
                putOpt("description", r.description)
                put("targetDays", r.targetDays)
                putOpt("preferredTime", r.preferredTime)
                putOpt("color", r.color)
                put("isArchived", r.isArchived)
                put("createdAt", r.createdAt)
                put("updatedAt", r.updatedAt)
                put("targetType", r.targetType)
                putOpt("targetValue", r.targetValue)
                putOpt("targetUnit", r.targetUnit)
                putOpt("category", r.category)
                put("reminderEnabled", r.reminderEnabled)
                put("sortOrder", r.sortOrder)
                put("bestStreak", r.bestStreak)
            })
        }
    }

    @JvmName("logsToJsonArray")
    private fun List<CompletionLogEntity>.toJsonArray(): JSONArray = JSONArray().also { arr ->
        forEach { l ->
            arr.put(JSONObject().apply {
                put("id", l.id)
                put("entityType", l.entityType)
                put("entityId", l.entityId)
                put("date", l.date)
                putOpt("completedAt", l.completedAt)
                put("status", l.status)
                putOpt("note", l.note)
                putOpt("value", l.value)
                putOpt("targetValue", l.targetValue)
                putOpt("skipReason", l.skipReason)
            })
        }
    }

    @JvmName("statesToJsonArray")
    private fun List<DailyStateEntity>.toJsonArray(): JSONArray = JSONArray().also { arr ->
        forEach { s ->
            arr.put(JSONObject().apply {
                put("date", s.date)
                putOpt("mood", s.mood)
                putOpt("energyLevel", s.energyLevel)
                put("completionRate", s.completionRate)
                putOpt("note", s.note)
                putOpt("reflection", s.reflection)
                put("dailyScore", s.dailyScore)
                putOpt("bestMoment", s.bestMoment)
                putOpt("challenge", s.challenge)
                putOpt("tomorrowIntention", s.tomorrowIntention)
                putOpt("closedAt", s.closedAt)
                put("carriedTaskCount", s.carriedTaskCount)
            })
        }
    }

    @JvmName("achievementsToJsonArray")
    private fun List<AchievementEntity>.toJsonArray(): JSONArray = JSONArray().also { arr ->
        forEach { a ->
            arr.put(JSONObject().apply {
                put("id", a.id)
                putOpt("unlockedAt", a.unlockedAt)
            })
        }
    }

    private fun UserPreferences.toJson(): JSONObject = JSONObject().apply {
        put("onboardingCompleted", onboardingCompleted)
        putOpt("selectedGoalProfile", selectedGoalProfile)
        put("notificationMode", notificationMode)
        put("dailySummaryTime", dailySummaryTime)
        put("analyticsEnabled", analyticsEnabled)
        put("themeMode", themeMode)
        put("totalXp", totalXp)
        put("gold", gold)
        put("happiness", happiness)
        put("companionType", companionType)
        put("companionName", companionName)
        put("lastDailyRewardDate", lastDailyRewardDate)
        put("totalTasksCompleted", totalTasksCompleted)
        put("totalRoutinesCompleted", totalRoutinesCompleted)
        put("totalPerfectDays", totalPerfectDays)
        put("totalDaysClosed", totalDaysClosed)
        put("happyMoodCount", happyMoodCount)
        put("ownedItems", ownedItems)
        put("rewardedEvents", rewardedEvents)
        put("morningPlannerEnabled", morningPlannerEnabled)
        put("morningPlannerTime", morningPlannerTime)
        put("quietHoursEnabled", quietHoursEnabled)
        put("quietHoursStart", quietHoursStart)
        put("quietHoursEnd", quietHoursEnd)
        put("celebrationEffectsEnabled", celebrationEffectsEnabled)
    }
}
