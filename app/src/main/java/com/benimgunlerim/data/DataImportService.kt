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
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import javax.inject.Inject
import javax.inject.Singleton
import org.json.JSONArray
import org.json.JSONObject

data class DataImportResult(
    val tasks: Int,
    val subTasks: Int,
    val routines: Int,
    val completionLogs: Int,
    val dailyStates: Int,
    val achievements: Int,
)

@Singleton
class DataImportService @Inject constructor(
    private val transactionRunner: DatabaseTransactionRunner,
    private val taskDao: TaskDao,
    private val routineDao: RoutineDao,
    private val subTaskDao: SubTaskDao,
    private val completionLogDao: CompletionLogDao,
    private val dailyStateDao: DailyStateDao,
    private val achievementDao: AchievementDao,
    private val preferencesWriter: UserPreferencesWriter,
    private val errorReporter: ErrorReporter,
) {
    suspend fun importFromJson(json: String): DataImportResult? = runCatching {
        val payload = parseAndValidate(json)

        transactionRunner.runInTransaction {
            completionLogDao.deleteAll()
            subTaskDao.deleteAll()
            taskDao.deleteAll()
            routineDao.deleteAll()
            dailyStateDao.deleteAll()
            achievementDao.deleteAll()

            taskDao.insertAll(payload.tasks)
            routineDao.insertAll(payload.routines)
            subTaskDao.insertAll(payload.subTasks)
            completionLogDao.insertAll(payload.logs)
            dailyStateDao.insertAll(payload.states)
            achievementDao.insertAll(payload.achievements)
        }

        payload.preferences?.let { preferencesWriter.replacePreferences(it) }
        payload.toResult()
    }.onFailure { e ->
        errorReporter.recordNonFatal(e, mapOf("action" to "data_import"))
    }.getOrNull()

    fun previewImport(json: String): DataImportResult? = runCatching {
        parseAndValidate(json).toResult()
    }.onFailure { e ->
        errorReporter.recordNonFatal(e, mapOf("action" to "data_import_preview"))
    }.getOrNull()

    private fun JSONArray.toTasks(): List<TaskEntity> = mapObjects { obj ->
        TaskEntity(
            id = obj.getString("id"),
            title = obj.getString("title"),
            note = obj.nullableString("note"),
            plannedDate = obj.getString("plannedDate"),
            startTime = obj.nullableString("startTime"),
            endTime = obj.nullableString("endTime"),
            category = obj.nullableString("category"),
            color = obj.nullableString("color"),
            completionState = obj.optString("completionState", "pending"),
            completedAt = obj.nullableLong("completedAt"),
            sourceTemplateId = obj.nullableString("sourceTemplateId"),
            createdAt = obj.optLong("createdAt", 0L),
            updatedAt = obj.optLong("updatedAt", 0L),
            priority = obj.optInt("priority", 2),
            reminderTime = obj.nullableString("reminderTime"),
            sortOrder = obj.optInt("sortOrder", 0),
            isArchived = obj.optBoolean("isArchived", false),
            postponedFromDate = obj.nullableString("postponedFromDate"),
        )
    }

    private fun JSONArray.toSubTasks(): List<SubTaskEntity> = mapObjects { obj ->
        SubTaskEntity(
            id = obj.getString("id"),
            taskId = obj.getString("taskId"),
            title = obj.getString("title"),
            isCompleted = obj.optBoolean("isCompleted", false),
            sortOrder = obj.optInt("sortOrder", 0),
            createdAt = obj.optLong("createdAt", 0L),
        )
    }

    private fun JSONArray.toRoutines(): List<RoutineEntity> = mapObjects { obj ->
        RoutineEntity(
            id = obj.getString("id"),
            name = obj.getString("name"),
            description = obj.nullableString("description"),
            targetDays = obj.getString("targetDays"),
            preferredTime = obj.nullableString("preferredTime"),
            color = obj.nullableString("color"),
            isArchived = obj.optBoolean("isArchived", false),
            createdAt = obj.optLong("createdAt", 0L),
            updatedAt = obj.optLong("updatedAt", 0L),
            targetType = obj.optString("targetType", "check"),
            targetValue = obj.nullableInt("targetValue"),
            targetUnit = obj.nullableString("targetUnit"),
            category = obj.nullableString("category"),
            reminderEnabled = obj.optBoolean("reminderEnabled", false),
            sortOrder = obj.optInt("sortOrder", 0),
            bestStreak = obj.optInt("bestStreak", 0),
        )
    }

    private fun JSONArray.toCompletionLogs(): List<CompletionLogEntity> = mapObjects { obj ->
        CompletionLogEntity(
            id = obj.getString("id"),
            entityType = obj.getString("entityType"),
            entityId = obj.getString("entityId"),
            date = obj.getString("date"),
            completedAt = obj.nullableLong("completedAt"),
            status = obj.getString("status"),
            note = obj.nullableString("note"),
            value = obj.nullableFloat("value"),
            targetValue = obj.nullableFloat("targetValue"),
            skipReason = obj.nullableString("skipReason"),
        )
    }

    private fun JSONArray.toDailyStates(): List<DailyStateEntity> = mapObjects { obj ->
        DailyStateEntity(
            date = obj.getString("date"),
            mood = obj.nullableString("mood"),
            energyLevel = obj.nullableInt("energyLevel"),
            completionRate = obj.optDouble("completionRate", 0.0).toFloat(),
            note = obj.nullableString("note"),
            reflection = obj.nullableString("reflection"),
            dailyScore = obj.optInt("dailyScore", 0),
            bestMoment = obj.nullableString("bestMoment"),
            challenge = obj.nullableString("challenge"),
            tomorrowIntention = obj.nullableString("tomorrowIntention"),
            closedAt = obj.nullableLong("closedAt"),
            carriedTaskCount = obj.optInt("carriedTaskCount", 0),
        )
    }

    private fun JSONArray.toAchievements(): List<AchievementEntity> = mapObjects { obj ->
        AchievementEntity(
            id = obj.getString("id"),
            unlockedAt = obj.nullableLong("unlockedAt"),
        )
    }

    private fun JSONObject.toUserPreferences(): UserPreferences = UserPreferences(
        onboardingCompleted = optBoolean("onboardingCompleted", false),
        selectedGoalProfile = nullableString("selectedGoalProfile"),
        notificationMode = optString("notificationMode", "light"),
        dailySummaryTime = optString("dailySummaryTime", "21:00"),
        analyticsEnabled = optBoolean("analyticsEnabled", true),
        themeMode = optString("themeMode", "system"),
        totalXp = optInt("totalXp", 0),
        gold = optInt("gold", 0),
        happiness = optInt("happiness", 0),
        companionType = optString("companionType", "cat"),
        companionName = optString("companionName", "Pati"),
        lastDailyRewardDate = optString("lastDailyRewardDate", ""),
        totalTasksCompleted = optInt("totalTasksCompleted", 0),
        totalRoutinesCompleted = optInt("totalRoutinesCompleted", 0),
        totalPerfectDays = optInt("totalPerfectDays", 0),
        totalDaysClosed = optInt("totalDaysClosed", 0),
        happyMoodCount = optInt("happyMoodCount", 0),
        ownedItems = optString("ownedItems", ""),
        rewardedEvents = optString("rewardedEvents", ""),
        morningPlannerEnabled = optBoolean("morningPlannerEnabled", false),
        morningPlannerTime = optString("morningPlannerTime", "08:00"),
        quietHoursEnabled = optBoolean("quietHoursEnabled", false),
        quietHoursStart = optString("quietHoursStart", "22:00"),
        quietHoursEnd = optString("quietHoursEnd", "07:00"),
    )

    private fun JSONArray?.orEmpty(): JSONArray = this ?: JSONArray()

    private fun <T> JSONArray.mapObjects(mapper: (JSONObject) -> T): List<T> =
        List(length()) { index -> mapper(getJSONObject(index)) }

    private fun JSONObject.nullableString(name: String): String? =
        if (has(name) && !isNull(name)) getString(name) else null

    private fun JSONObject.nullableLong(name: String): Long? =
        if (has(name) && !isNull(name)) getLong(name) else null

    private fun JSONObject.nullableInt(name: String): Int? =
        if (has(name) && !isNull(name)) getInt(name) else null

    private fun JSONObject.nullableFloat(name: String): Float? =
        if (has(name) && !isNull(name)) getDouble(name).toFloat() else null

    private data class ImportPayload(
        val tasks: List<TaskEntity>,
        val subTasks: List<SubTaskEntity>,
        val routines: List<RoutineEntity>,
        val logs: List<CompletionLogEntity>,
        val states: List<DailyStateEntity>,
        val achievements: List<AchievementEntity>,
        val preferences: UserPreferences?,
    ) {
        fun toResult(): DataImportResult = DataImportResult(
            tasks = tasks.size,
            subTasks = subTasks.size,
            routines = routines.size,
            completionLogs = logs.size,
            dailyStates = states.size,
            achievements = achievements.size,
        )
    }

    private fun parseAndValidate(json: String): ImportPayload {
        val root = JSONObject(json)
        val version = root.getInt("version")
        require(version == DataExportService.EXPORT_VERSION) {
            "Unsupported import version: $version"
        }

        val tasks = root.optJSONArray("tasks").orEmpty().toTasks()
        val routines = root.optJSONArray("routines").orEmpty().toRoutines()
        val subTasks = root.optJSONArray("subTasks").orEmpty().toSubTasks()
        val logs = root.optJSONArray("completionLogs").orEmpty().toCompletionLogs()
        val states = root.optJSONArray("dailyStates").orEmpty().toDailyStates()
        val achievements = root.optJSONArray("achievements").orEmpty().toAchievements()
        val preferences = root.optJSONObject("preferences")?.toUserPreferences()

        validateImportData(
            tasks = tasks,
            subTasks = subTasks,
            routines = routines,
            logs = logs,
            states = states,
            preferences = preferences,
        )

        return ImportPayload(
            tasks = tasks,
            subTasks = subTasks,
            routines = routines,
            logs = logs,
            states = states,
            achievements = achievements,
            preferences = preferences,
        )
    }

    private fun validateImportData(
        tasks: List<TaskEntity>,
        subTasks: List<SubTaskEntity>,
        routines: List<RoutineEntity>,
        logs: List<CompletionLogEntity>,
        states: List<DailyStateEntity>,
        preferences: UserPreferences?,
    ) {
        val taskIds = tasks.map { it.id }.toSet()
        val routineIds = routines.map { it.id }.toSet()

        tasks.forEach { task ->
            require(isIsoDate(task.plannedDate)) { "Invalid task plannedDate: ${task.plannedDate}" }
            task.startTime?.let { require(isHmTime(it)) { "Invalid task startTime: $it" } }
            task.endTime?.let { require(isHmTime(it)) { "Invalid task endTime: $it" } }
            task.reminderTime?.let { require(isHmTime(it)) { "Invalid task reminderTime: $it" } }
            require(task.completionState in setOf("pending", "completed")) {
                "Invalid task completionState: ${task.completionState}"
            }
        }

        subTasks.forEach { subTask ->
            require(subTask.taskId in taskIds) { "Dangling subTask.taskId: ${subTask.taskId}" }
        }

        routines.forEach { routine ->
            routine.preferredTime?.let { require(isHmTime(it)) { "Invalid routine preferredTime: $it" } }
            require(routine.targetType in setOf("check", "goal")) { "Invalid routine targetType: ${routine.targetType}" }
            val dayTokens = routine.targetDays.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            require(dayTokens.isNotEmpty()) { "Routine targetDays must not be empty" }
            dayTokens.forEach { day ->
                require(runCatching { DayOfWeek.valueOf(day) }.isSuccess) { "Invalid routine targetDays token: $day" }
            }
        }

        logs.forEach { log ->
            require(isIsoDate(log.date)) { "Invalid completionLog date: ${log.date}" }
            require(log.entityType in setOf("task", "routine")) { "Invalid completionLog entityType: ${log.entityType}" }
            require(log.status in setOf("completed", "partial", "skipped")) { "Invalid completionLog status: ${log.status}" }
            when (log.entityType) {
                "task" -> require(log.entityId in taskIds) { "Dangling completionLog task entityId: ${log.entityId}" }
                "routine" -> require(log.entityId in routineIds) { "Dangling completionLog routine entityId: ${log.entityId}" }
            }
        }

        states.forEach { state ->
            require(isIsoDate(state.date)) { "Invalid dailyState date: ${state.date}" }
        }

        preferences?.let { prefs ->
            require(isHmTime(prefs.dailySummaryTime)) { "Invalid preferences dailySummaryTime: ${prefs.dailySummaryTime}" }
            require(isHmTime(prefs.morningPlannerTime)) { "Invalid preferences morningPlannerTime: ${prefs.morningPlannerTime}" }
            require(isHmTime(prefs.quietHoursStart)) { "Invalid preferences quietHoursStart: ${prefs.quietHoursStart}" }
            require(isHmTime(prefs.quietHoursEnd)) { "Invalid preferences quietHoursEnd: ${prefs.quietHoursEnd}" }
        }
    }

    private fun isIsoDate(value: String): Boolean =
        runCatching { LocalDate.parse(value) }.isSuccess

    private fun isHmTime(value: String): Boolean =
        runCatching { LocalTime.parse(value) }.isSuccess
}
