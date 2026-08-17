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
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.domain.model.CompletionStatus
import com.benimgunlerim.domain.model.RoutineTargetType
import com.benimgunlerim.domain.model.TaskCompletionState
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import kotlin.text.Charsets
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
    companion object {
        private const val MAX_IMPORT_BYTES = 5 * 1024 * 1024
        private const val MAX_TASKS = 10_000
        private const val MAX_SUB_TASKS = 25_000
        private const val MAX_ROUTINES = 2_000
        private const val MAX_COMPLETION_LOGS = 50_000
        private const val MAX_DAILY_STATES = 3_650
        private const val MAX_ACHIEVEMENTS = 1_000
        private const val MAX_ID_LENGTH = 200
        private const val MAX_SHORT_TEXT_LENGTH = 200
        private const val MAX_MEDIUM_TEXT_LENGTH = 1_000
        private const val MAX_LONG_TEXT_LENGTH = 5_000
    }

    suspend fun importFromJson(json: String): DataImportResult? = runCatching {
        val payload = parseAndValidate(json)

        // Phase 1: DB restore — atomic within Room transaction.
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

        // Phase 2: DataStore preferences — separate persistence layer, cannot join Room transaction.
        // If this throws, runCatching reports the failure and returns null to the caller.
        // Known limitation: DB is restored but preferences are not on partial failure; this is
        // unavoidable without full two-phase-commit infrastructure.
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
            completionState = obj.optString("completionState", TaskCompletionState.PENDING.value),
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
            targetType = obj.optString("targetType", RoutineTargetType.CHECK.value),
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
        celebrationEffectsEnabled = optBoolean("celebrationEffectsEnabled", true),
        lightDayModeDate = optString("lightDayModeDate", ""),
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
        require(json.toByteArray(Charsets.UTF_8).size <= MAX_IMPORT_BYTES) {
            "Import payload exceeds 5 MB limit"
        }

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
            achievements = achievements,
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
        achievements: List<AchievementEntity>,
        preferences: UserPreferences?,
    ) {
        val taskIds = tasks.map { it.id }.toSet()
        val subTaskIds = subTasks.map { it.id }.toSet()
        val routineIds = routines.map { it.id }.toSet()
        val logIds = logs.map { it.id }.toSet()
        val dailyStateDates = states.map { it.date }.toSet()
        val achievementIds = achievements.map { it.id }.toSet()

        require(tasks.size <= MAX_TASKS) { "Task import exceeds limit: ${tasks.size}" }
        require(subTasks.size <= MAX_SUB_TASKS) { "Subtask import exceeds limit: ${subTasks.size}" }
        require(routines.size <= MAX_ROUTINES) { "Routine import exceeds limit: ${routines.size}" }
        require(logs.size <= MAX_COMPLETION_LOGS) { "Completion log import exceeds limit: ${logs.size}" }
        require(states.size <= MAX_DAILY_STATES) { "Daily state import exceeds limit: ${states.size}" }
        require(achievements.size <= MAX_ACHIEVEMENTS) { "Achievement import exceeds limit: ${achievements.size}" }

        require(taskIds.size == tasks.size) { "Duplicate task ids are not allowed" }
        require(subTaskIds.size == subTasks.size) { "Duplicate subTask ids are not allowed" }
        require(routineIds.size == routines.size) { "Duplicate routine ids are not allowed" }
        require(logIds.size == logs.size) { "Duplicate completionLog ids are not allowed" }
        require(dailyStateDates.size == states.size) { "Duplicate dailyState dates are not allowed" }
        require(achievementIds.size == achievements.size) { "Duplicate achievement ids are not allowed" }

        tasks.forEach { task ->
            task.id.requireMaxLength("task.id", MAX_ID_LENGTH)
            task.title.requireMaxLength("task.title", MAX_SHORT_TEXT_LENGTH)
            task.note.requireMaxLength("task.note", MAX_LONG_TEXT_LENGTH)
            task.category.requireMaxLength("task.category", MAX_SHORT_TEXT_LENGTH)
            task.color.requireMaxLength("task.color", MAX_SHORT_TEXT_LENGTH)
            task.sourceTemplateId.requireMaxLength("task.sourceTemplateId", MAX_ID_LENGTH)
            require(isIsoDate(task.plannedDate)) { "Invalid task plannedDate: ${task.plannedDate}" }
            task.startTime?.let { require(isHmTime(it)) { "Invalid task startTime: $it" } }
            task.endTime?.let { require(isHmTime(it)) { "Invalid task endTime: $it" } }
            task.reminderTime?.let { require(isHmTime(it)) { "Invalid task reminderTime: $it" } }
            require(task.completionState in setOf(TaskCompletionState.PENDING.value, TaskCompletionState.COMPLETED.value)) {
                "Invalid task completionState: ${task.completionState}"
            }
        }

        subTasks.forEach { subTask ->
            subTask.id.requireMaxLength("subTask.id", MAX_ID_LENGTH)
            subTask.taskId.requireMaxLength("subTask.taskId", MAX_ID_LENGTH)
            subTask.title.requireMaxLength("subTask.title", MAX_SHORT_TEXT_LENGTH)
            require(subTask.taskId in taskIds) { "Dangling subTask.taskId: ${subTask.taskId}" }
        }

        routines.forEach { routine ->
            routine.id.requireMaxLength("routine.id", MAX_ID_LENGTH)
            routine.name.requireMaxLength("routine.name", MAX_SHORT_TEXT_LENGTH)
            routine.description.requireMaxLength("routine.description", MAX_LONG_TEXT_LENGTH)
            routine.color.requireMaxLength("routine.color", MAX_SHORT_TEXT_LENGTH)
            routine.targetUnit.requireMaxLength("routine.targetUnit", MAX_SHORT_TEXT_LENGTH)
            routine.category.requireMaxLength("routine.category", MAX_SHORT_TEXT_LENGTH)
            routine.preferredTime?.let { require(isHmTime(it)) { "Invalid routine preferredTime: $it" } }
            require(
                routine.targetType in setOf(RoutineTargetType.CHECK.value, RoutineTargetType.GOAL.value),
            ) { "Invalid routine targetType: ${routine.targetType}" }
            val dayTokens = routine.targetDays.split(",").map { it.trim() }.filter { it.isNotEmpty() }
            require(dayTokens.isNotEmpty()) { "Routine targetDays must not be empty" }
            dayTokens.forEach { day ->
                require(runCatching { DayOfWeek.valueOf(day) }.isSuccess) { "Invalid routine targetDays token: $day" }
            }
        }

        logs.forEach { log ->
            log.id.requireMaxLength("completionLog.id", MAX_ID_LENGTH)
            log.entityId.requireMaxLength("completionLog.entityId", MAX_ID_LENGTH)
            log.note.requireMaxLength("completionLog.note", MAX_LONG_TEXT_LENGTH)
            log.skipReason.requireMaxLength("completionLog.skipReason", MAX_MEDIUM_TEXT_LENGTH)
            require(isIsoDate(log.date)) { "Invalid completionLog date: ${log.date}" }
            require(
                log.entityType in setOf(CompletionEntityType.TASK.value, CompletionEntityType.ROUTINE.value),
            ) { "Invalid completionLog entityType: ${log.entityType}" }
            require(
                log.status in setOf(CompletionStatus.COMPLETED.value, CompletionStatus.PARTIAL.value, CompletionStatus.SKIPPED.value),
            ) { "Invalid completionLog status: ${log.status}" }
            when (log.entityType) {
                CompletionEntityType.TASK.value ->
                    require(log.entityId in taskIds) { "Dangling completionLog task entityId: ${log.entityId}" }
                CompletionEntityType.ROUTINE.value ->
                    require(log.entityId in routineIds) { "Dangling completionLog routine entityId: ${log.entityId}" }
            }
        }

        states.forEach { state ->
            state.mood.requireMaxLength("dailyState.mood", MAX_SHORT_TEXT_LENGTH)
            state.note.requireMaxLength("dailyState.note", MAX_LONG_TEXT_LENGTH)
            state.reflection.requireMaxLength("dailyState.reflection", MAX_LONG_TEXT_LENGTH)
            state.bestMoment.requireMaxLength("dailyState.bestMoment", MAX_LONG_TEXT_LENGTH)
            state.challenge.requireMaxLength("dailyState.challenge", MAX_LONG_TEXT_LENGTH)
            state.tomorrowIntention.requireMaxLength("dailyState.tomorrowIntention", MAX_LONG_TEXT_LENGTH)
            require(isIsoDate(state.date)) { "Invalid dailyState date: ${state.date}" }
        }

        achievements.forEach { achievement ->
            achievement.id.requireMaxLength("achievement.id", MAX_ID_LENGTH)
        }

        preferences?.let { prefs ->
            prefs.selectedGoalProfile.requireMaxLength("preferences.selectedGoalProfile", MAX_SHORT_TEXT_LENGTH)
            prefs.notificationMode.requireMaxLength("preferences.notificationMode", MAX_SHORT_TEXT_LENGTH)
            prefs.themeMode.requireMaxLength("preferences.themeMode", MAX_SHORT_TEXT_LENGTH)
            prefs.companionType.requireMaxLength("preferences.companionType", MAX_SHORT_TEXT_LENGTH)
            prefs.companionName.requireMaxLength("preferences.companionName", MAX_SHORT_TEXT_LENGTH)
            prefs.ownedItems.requireMaxLength("preferences.ownedItems", MAX_LONG_TEXT_LENGTH)
            prefs.rewardedEvents.requireMaxLength("preferences.rewardedEvents", MAX_LONG_TEXT_LENGTH)
            require(isHmTime(prefs.dailySummaryTime)) { "Invalid preferences dailySummaryTime: ${prefs.dailySummaryTime}" }
            require(isHmTime(prefs.morningPlannerTime)) { "Invalid preferences morningPlannerTime: ${prefs.morningPlannerTime}" }
            require(isHmTime(prefs.quietHoursStart)) { "Invalid preferences quietHoursStart: ${prefs.quietHoursStart}" }
            require(isHmTime(prefs.quietHoursEnd)) { "Invalid preferences quietHoursEnd: ${prefs.quietHoursEnd}" }
        }
    }

    private fun String?.requireMaxLength(fieldName: String, maxLength: Int) {
        require(this == null || length <= maxLength) {
            "$fieldName exceeds max length of $maxLength"
        }
    }

    private fun isIsoDate(value: String): Boolean =
        runCatching { LocalDate.parse(value) }.isSuccess

    private fun isHmTime(value: String): Boolean =
        runCatching { LocalTime.parse(value) }.isSuccess
}
