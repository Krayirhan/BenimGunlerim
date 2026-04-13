package com.benimgunlerim.data

import com.benimgunlerim.analytics.ErrorReporter
import com.benimgunlerim.data.local.CompletionLogDao
import com.benimgunlerim.data.local.DailyStateDao
import com.benimgunlerim.data.local.RoutineDao
import com.benimgunlerim.data.local.SubTaskDao
import com.benimgunlerim.data.local.TaskDao
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.notifications.RoutineReminderScheduler
import com.benimgunlerim.notifications.TaskReminderScheduler
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.TextStyle
import java.util.Locale
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

@Singleton
class BenimGunlerimRepository @Inject constructor(
    private val taskDao: TaskDao,
    private val routineDao: RoutineDao,
    private val completionLogDao: CompletionLogDao,
    private val dailyStateDao: DailyStateDao,
    private val subTaskDao: SubTaskDao,
    private val routineReminderScheduler: RoutineReminderScheduler,
    private val taskReminderScheduler: TaskReminderScheduler,
    private val errorReporter: ErrorReporter,
) {
    fun observeTasks(date: LocalDate = LocalDate.now()): Flow<List<TaskEntity>> =
        taskDao.observeByDate(date.toString())

    fun observeOverdueTasks(today: LocalDate = LocalDate.now()): Flow<List<TaskEntity>> =
        taskDao.observeOverdue(today.toString())

    fun observeTasksForRange(from: LocalDate, to: LocalDate): Flow<List<TaskEntity>> =
        taskDao.observeRange(from.toString(), to.toString())

    fun observeActiveRoutines(): Flow<List<RoutineEntity>> = routineDao.observeActive()

    fun observeCompletionLogs(date: LocalDate = LocalDate.now()): Flow<List<CompletionLogEntity>> =
        completionLogDao.observeByDate(date.toString())

    fun observeAllCompletionLogs(): Flow<List<CompletionLogEntity>> = completionLogDao.observeAll()

    fun observeCompletionLogsBetween(from: LocalDate, to: LocalDate): Flow<List<CompletionLogEntity>> =
        completionLogDao.observeBetween(from.toString(), to.toString())

    fun observeRecentDailyStates(limit: Int = 7): Flow<List<DailyStateEntity>> =
        dailyStateDao.observeRecent(limit)

    fun observeTodayState(): Flow<DailyStateEntity?> =
        dailyStateDao.observeByDate(LocalDate.now().toString())

    suspend fun addTask(
        title: String,
        date: LocalDate = LocalDate.now(),
        note: String? = null,
        startTime: String? = null,
        category: String? = null,
        priority: Int = 2,
        reminderTime: String? = null,
    ) {
        val now = System.currentTimeMillis()
        val newTask = TaskEntity(
                id = UUID.randomUUID().toString(),
                title = title.trim(),
                note = note?.takeIf { it.isNotBlank() }?.trim(),
                plannedDate = date.toString(),
                startTime = startTime?.takeIf { it.isNotBlank() },
                endTime = null,
                category = category?.takeIf { it.isNotBlank() },
                color = null,
                completionState = "pending",
                completedAt = null,
                sourceTemplateId = null,
                createdAt = now,
                updatedAt = now,
                priority = priority.coerceIn(1, 3),
                reminderTime = reminderTime?.takeIf { it.isNotBlank() },
            )
        taskDao.insert(newTask)
        if (reminderTime != null) {
            runCatching {
                taskReminderScheduler.schedule(newTask.id, newTask.title, date, LocalTime.parse(reminderTime))
            }.onFailure { e ->
                errorReporter.recordNonFatal(e, mapOf("action" to "reminder_schedule", "taskId" to newTask.id))
            }
        }
    }

    suspend fun toggleTask(task: TaskEntity) {
        val now = System.currentTimeMillis()
        val isCompleted = task.completionState == "completed"
        val nextState = if (isCompleted) "pending" else "completed"
        taskDao.update(
            task.copy(
                completionState = nextState,
                completedAt = if (isCompleted) null else now,
                updatedAt = now,
            ),
        )
        if (isCompleted) {
            completionLogDao.deleteForDate("task", task.id, task.plannedDate)
        } else {
            taskReminderScheduler.cancel(task.id)
            completionLogDao.upsert(
                CompletionLogEntity(
                    id = "task-${task.id}-${task.plannedDate}",
                    entityType = "task",
                    entityId = task.id,
                    date = task.plannedDate,
                    completedAt = now,
                    status = "completed",
                    note = null,
                ),
            )
        }
    }

    suspend fun updateTaskTitle(task: TaskEntity, title: String) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        taskDao.update(task.copy(title = cleanTitle, updatedAt = System.currentTimeMillis()))
    }

    suspend fun updateTask(
        task: TaskEntity,
        title: String,
        note: String?,
        plannedDate: LocalDate,
        startTime: String?,
        category: String?,
        priority: Int,
        reminderTime: String?,
    ) {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return
        val updatedTask = task.copy(
            title = cleanTitle,
            note = note?.takeIf { it.isNotBlank() }?.trim(),
            plannedDate = plannedDate.toString(),
            startTime = startTime?.takeIf { it.isNotBlank() },
            category = category?.takeIf { it.isNotBlank() },
            priority = priority.coerceIn(1, 3),
            reminderTime = reminderTime?.takeIf { it.isNotBlank() },
            updatedAt = System.currentTimeMillis(),
        )
        taskDao.update(updatedTask)
        if (task.plannedDate != updatedTask.plannedDate) {
            completionLogDao.deleteForDate("task", task.id, task.plannedDate)
        }
        taskReminderScheduler.cancel(task.id)
        if (updatedTask.reminderTime != null) {
            runCatching {
                val date = LocalDate.parse(updatedTask.plannedDate)
                taskReminderScheduler.schedule(updatedTask.id, updatedTask.title, date, LocalTime.parse(updatedTask.reminderTime))
            }.onFailure { e ->
                errorReporter.recordNonFatal(e, mapOf("action" to "reminder_reschedule", "taskId" to updatedTask.id))
            }
        }
    }

    suspend fun moveTaskToDate(task: TaskEntity, date: LocalDate) {
        val updatedTask = task.copy(
            plannedDate = date.toString(),
            completionState = "pending",
            completedAt = null,
            updatedAt = System.currentTimeMillis(),
        )
        taskDao.update(updatedTask)
        completionLogDao.deleteForDate("task", task.id, task.plannedDate)
    }

    suspend fun deleteTask(task: TaskEntity) {
        taskReminderScheduler.cancel(task.id)
        taskDao.deleteById(task.id)
        completionLogDao.deleteForEntity("task", task.id)
    }

    suspend fun restoreTask(task: TaskEntity) {
        taskDao.insert(task.copy(completionState = "pending", completedAt = null))
    }

    suspend fun setTaskPending(taskId: String) {
        taskDao.setCompletionStateById(taskId, "pending", null)
    }

    // ── SubTask ──────────────────────────────────────────────────────────────

    fun observeSubTasks(taskId: String): Flow<List<SubTaskEntity>> =
        subTaskDao.observeByTaskId(taskId)

    suspend fun addSubTask(taskId: String, title: String) {
        if (title.isBlank()) return
        subTaskDao.insert(
            SubTaskEntity(
                id = UUID.randomUUID().toString(),
                taskId = taskId,
                title = title.trim(),
                isCompleted = false,
                sortOrder = 0,
                createdAt = System.currentTimeMillis(),
            ),
        )
    }

    suspend fun toggleSubTask(subTask: SubTaskEntity) {
        subTaskDao.update(subTask.copy(isCompleted = !subTask.isCompleted))
    }

    suspend fun deleteSubTask(subTask: SubTaskEntity) {
        subTaskDao.deleteById(subTask.id)
    }

    suspend fun addRoutine(
        name: String,
        targetDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
        preferredTime: String? = null,
        targetType: String = "check",
        targetValue: Int? = null,
        targetUnit: String? = null,
    ) {
        val now = System.currentTimeMillis()
        val routine = RoutineEntity(
            id = UUID.randomUUID().toString(),
            name = name.trim(),
            description = null,
            targetDays = targetDays.joinToString(",") { it.name },
            preferredTime = preferredTime?.takeIf { it.isNotBlank() },
            color = null,
            isArchived = false,
            createdAt = now,
            updatedAt = now,
            targetType = targetType,
            targetValue = targetValue?.takeIf { it > 0 },
            targetUnit = targetUnit?.takeIf { it.isNotBlank() },
        )
        routineDao.insert(routine)
        routineReminderScheduler.schedule(routine)
    }

    suspend fun updateRoutine(
        routine: RoutineEntity,
        name: String,
        targetDays: Set<DayOfWeek>,
        preferredTime: String?,
        targetType: String = routine.targetType,
        targetValue: Int? = routine.targetValue,
        targetUnit: String? = routine.targetUnit,
    ) {
        val cleanName = name.trim()
        if (cleanName.isBlank() || targetDays.isEmpty()) return
        val updated = routine.copy(
            name = cleanName,
            targetDays = targetDays.joinToString(",") { it.name },
            preferredTime = preferredTime?.takeIf { it.isNotBlank() },
            targetType = targetType,
            targetValue = targetValue?.takeIf { it > 0 },
            targetUnit = targetUnit?.takeIf { it.isNotBlank() },
            updatedAt = System.currentTimeMillis(),
        )
        routineDao.update(updated)
        routineReminderScheduler.cancel(routine)
        routineReminderScheduler.schedule(updated)
    }

    suspend fun archiveRoutine(routine: RoutineEntity) {
        routineDao.update(routine.copy(isArchived = true, updatedAt = System.currentTimeMillis()))
        routineReminderScheduler.cancel(routine)
    }

    suspend fun toggleRoutine(routine: RoutineEntity, date: LocalDate = LocalDate.now(), completedToday: Boolean) {
        if (completedToday) {
            completionLogDao.deleteForDate("routine", routine.id, date.toString())
        } else {
            completionLogDao.upsert(
                CompletionLogEntity(
                    id = "routine-${routine.id}-$date",
                    entityType = "routine",
                    entityId = routine.id,
                    date = date.toString(),
                    completedAt = System.currentTimeMillis(),
                    status = "completed",
                    note = null,
                ),
            )
        }
    }

    suspend fun setRoutineProgress(routine: RoutineEntity, value: Float, date: LocalDate = LocalDate.now()) {
        val cleanValue = value.coerceAtLeast(0f)
        val target = routine.targetValue?.toFloat()?.takeIf { it > 0f } ?: 1f
        if (cleanValue <= 0f) {
            completionLogDao.deleteForDate("routine", routine.id, date.toString())
            return
        }
        val completed = cleanValue >= target
        completionLogDao.upsert(
            CompletionLogEntity(
                id = "routine-${routine.id}-$date",
                entityType = "routine",
                entityId = routine.id,
                date = date.toString(),
                completedAt = if (completed) System.currentTimeMillis() else null,
                status = if (completed) "completed" else "partial",
                note = null,
                value = cleanValue,
                targetValue = target,
            ),
        )
    }

    suspend fun skipRoutine(routine: RoutineEntity, date: LocalDate = LocalDate.now()) {
        completionLogDao.upsert(
            CompletionLogEntity(
                id = "routine-${routine.id}-$date",
                entityType = "routine",
                entityId = routine.id,
                date = date.toString(),
                completedAt = null,
                status = "skipped",
                note = null,
                value = 0f,
                targetValue = routine.targetValue?.toFloat(),
            ),
        )
    }

    suspend fun saveDailySummary(
        mood: String,
        note: String,
        completionRate: Float,
        energyLevel: Int? = null,
        bestMoment: String? = null,
        challenge: String? = null,
        tomorrowIntention: String? = null,
        carriedTaskCount: Int = 0,
    ) {
        val today = LocalDate.now().toString()
        val existing = dailyStateDao.getByDate(today)
        dailyStateDao.upsert(
            DailyStateEntity(
                date = today,
                mood = mood,
                energyLevel = energyLevel,
                completionRate = completionRate,
                note = note.ifBlank { null },
                reflection = note.ifBlank { null },
                dailyScore = (completionRate * 100).toInt(),
                bestMoment = bestMoment?.ifBlank { null },
                challenge = challenge?.ifBlank { null },
                tomorrowIntention = tomorrowIntention?.ifBlank { null },
                closedAt = existing?.closedAt ?: System.currentTimeMillis(),
                carriedTaskCount = carriedTaskCount,
            ),
        )
    }

    suspend fun carryPendingTasksToTomorrow(): Int {
        val today = LocalDate.now()
        val tomorrow = today.plusDays(1)
        val overdue = taskDao.observeOverdue(today.toString())
        var count = 0
        // we need a suspend query for this
        val tasks = taskDao.getPendingBefore(today.toString())
        tasks.forEach { task ->
            taskDao.update(
                task.copy(
                    plannedDate = tomorrow.toString(),
                    postponedFromDate = task.postponedFromDate ?: task.plannedDate,
                    completionState = "pending",
                    completedAt = null,
                    updatedAt = System.currentTimeMillis(),
                ),
            )
            completionLogDao.deleteForDate("task", task.id, task.plannedDate)
            count++
        }
        return count
    }

    suspend fun clearAllLocalData() {
        completionLogDao.deleteAll()
        taskDao.deleteAll()
        subTaskDao.deleteAll()
        routineDao.deleteAll()
        dailyStateDao.deleteAll()
    }

    suspend fun deleteSeededTemplateData() {
        taskDao.deleteTemplateTasks()
        routineDao.deleteByNames(
            listOf(
                "1 bardak su iç",
                "5 dk yürüyüş yap",
                "5 dk nefes egzersizi",
                "2 dk esneme",
                "Su iç",
                "1 bardak su iÃ§",
                "5 dk yÃ¼rÃ¼yÃ¼ÅŸ yap",
                "5 dk nefes egzersizi",
                "2 dk esneme",
                "Su iÃ§",
            ),
        )
    }

    suspend fun seedTemplateIfEmpty(needId: String, intensityId: String) {
        deleteSeededTemplateData()
        return
        if (taskDao.count() > 0 || routineDao.count() > 0) return

        val today = LocalDate.now()
        val now = System.currentTimeMillis()

        // ── Tasks based on need ──────────────────────────────────────────
        val tasks = mutableListOf<String>()
        val routines = mutableListOf<String>()

        when (needId) {
            "duzen" -> {
                tasks.add("Günün planını yap")
                tasks.add("Ana görevini tamamla")
                tasks.add("Günü kapat ve yarını hazırla")
            }
            "duzenli" -> {
                tasks.add("Bugünkü 3 önceliğini belirle")
                tasks.add("Listedeki en önemli işi bitir")
                tasks.add("Yarının listesini hazırla")
            }
            "saglik" -> {
                routines.add("1 bardak su iç")
                routines.add("5 dk yürüyüş yap")
                routines.add("5 dk nefes egzersizi")
            }
            "odak" -> {
                tasks.add("Bugünün tek odak konusunu seç")
                tasks.add("25 dk kesintisiz çalış")
                tasks.add("Ne öğrendiğini not et")
            }
            "basit" -> {
                tasks.add("Bugün ne yapacağını yaz")
                tasks.add("Günü kapat")
            }
        }

        // Extra items for higher intensity
        if (intensityId != "hafif") {
            routines.add("2 dk esneme")
            when (needId) {
                "saglik" -> tasks.add("Sağlıklı bir öğün planla")
                "odak" -> tasks.add("İkinci odak bloğu (25 dk)")
                else -> tasks.add("Küçük bir mola ver")
            }
        }
        if (intensityId == "yogun") {
            routines.add("Su iç")
            tasks.add("İkinci görevi tamamla")
            tasks.add("10 dk okuma")
        }

        // ── Insert tasks ─────────────────────────────────────────────────
        val taskEntities = tasks.mapIndexed { index, title ->
            TaskEntity(
                id = UUID.randomUUID().toString(),
                title = title,
                note = null,
                plannedDate = today.toString(),
                startTime = null,
                endTime = null,
                category = needId,
                color = null,
                completionState = "pending",
                completedAt = null,
                sourceTemplateId = needId,
                createdAt = now + index,
                updatedAt = now + index,
            )
        }
        if (taskEntities.isNotEmpty()) taskDao.insertAll(taskEntities)

        // ── Insert routines ──────────────────────────────────────────────
        val routineEntities = routines.mapIndexed { index, name ->
            RoutineEntity(
                id = UUID.randomUUID().toString(),
                name = name,
                description = null,
                targetDays = DayOfWeek.entries.joinToString(",") { it.name },
                preferredTime = null,
                color = null,
                isArchived = false,
                createdAt = now + index,
                updatedAt = now + index,
            )
        }
        if (routineEntities.isNotEmpty()) routineDao.insertAll(routineEntities)
    }
}

fun RoutineEntity.isScheduledFor(dayOfWeek: DayOfWeek): Boolean =
    targetDays.split(",").any { it == dayOfWeek.name }

fun RoutineEntity.targetDaySet(): Set<DayOfWeek> =
    targetDays.split(",").mapNotNull { runCatching { DayOfWeek.valueOf(it) }.getOrNull() }.toSet()

fun DayOfWeek.shortTr(): String =
    getDisplayName(TextStyle.SHORT, Locale.forLanguageTag("tr-TR")).replaceFirstChar { it.uppercase() }

fun List<CompletionLogEntity>.currentStreak(today: LocalDate = LocalDate.now()): Int {
    val completedDates = filter { it.status == "completed" }.mapNotNull {
        runCatching { LocalDate.parse(it.date) }.getOrNull()
    }.toSet()
    var streak = 0
    var cursor = today
    while (cursor in completedDates) {
        streak += 1
        cursor = cursor.minusDays(1)
    }
    return streak
}

fun List<CompletionLogEntity>.currentStreakForEntity(
    entityType: String,
    entityId: String,
    today: LocalDate = LocalDate.now(),
): Int =
    filter { it.entityType == entityType && it.entityId == entityId }.currentStreak(today)
