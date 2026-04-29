package com.benimgunlerim.data

import com.benimgunlerim.data.local.CompletionLogDao
import com.benimgunlerim.data.local.SubTaskDao
import com.benimgunlerim.data.local.TaskDao
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.SubTaskEntity
import com.benimgunlerim.data.local.entity.TaskEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.domain.model.CompletionStatus
import com.benimgunlerim.domain.model.TaskCompletionState
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for task and sub-task persistence operations.
 *
 * Use-cases depend on this interface so they can be tested without coupling to
 * the concrete Room-backed implementation.
 */
interface TaskRepository {
    fun observeByDate(date: LocalDate): Flow<List<TaskEntity>>
    fun observeOverdue(today: LocalDate): Flow<List<TaskEntity>>
    fun observeRange(from: LocalDate, to: LocalDate): Flow<List<TaskEntity>>
    fun observeSubTasks(taskId: String): Flow<List<SubTaskEntity>>
    suspend fun insert(task: TaskEntity)
    suspend fun addTask(
        title: String,
        date: LocalDate,
        note: String? = null,
        startTime: String? = null,
        category: String? = null,
        priority: Int = 2,
        reminderTime: String? = null,
    ): TaskEntity
    suspend fun update(task: TaskEntity)
    suspend fun setCompletionState(task: TaskEntity, state: TaskCompletionState): TaskEntity
    suspend fun setCompletionStateById(taskId: String, state: TaskCompletionState)
    suspend fun updateTitle(task: TaskEntity, title: String)
    suspend fun updateFull(
        task: TaskEntity,
        title: String,
        note: String?,
        plannedDate: LocalDate,
        startTime: String?,
        category: String?,
        priority: Int,
        reminderTime: String?,
    ): TaskEntity
    suspend fun moveToDate(task: TaskEntity, date: LocalDate): TaskEntity
    suspend fun delete(task: TaskEntity)
    suspend fun deleteAll()
    suspend fun restore(task: TaskEntity)
    suspend fun writeCompletionLog(task: TaskEntity)
    suspend fun deleteCompletionLog(task: TaskEntity)
    suspend fun getPendingBefore(date: LocalDate): List<TaskEntity>
    suspend fun addSubTask(taskId: String, title: String)
    suspend fun toggleSubTask(subTask: SubTaskEntity)
    suspend fun deleteSubTask(subTask: SubTaskEntity)
    suspend fun deleteAllSubTasks()
    suspend fun count(): Int
    suspend fun deleteTemplateTasks()
}

/**
 * Pure Room-backed data-access layer for tasks and subtasks.
 * No reward granting, no notification scheduling — those belong in use-cases.
 */
@Singleton
class TaskRepositoryImpl @Inject constructor(
    private val taskDao: TaskDao,
    private val subTaskDao: SubTaskDao,
    private val completionLogDao: CompletionLogDao,
    private val dateTimeProvider: DateTimeProvider,
) : TaskRepository {
    // ── Observe ──────────────────────────────────────────────────────────────

    override fun observeByDate(date: LocalDate): Flow<List<TaskEntity>> =
        taskDao.observeByDate(date.toString())

    override fun observeOverdue(today: LocalDate): Flow<List<TaskEntity>> =
        taskDao.observeOverdue(today.toString())

    override fun observeRange(from: LocalDate, to: LocalDate): Flow<List<TaskEntity>> =
        taskDao.observeRange(from.toString(), to.toString())

    override fun observeSubTasks(taskId: String): Flow<List<SubTaskEntity>> =
        subTaskDao.observeByTaskId(taskId)

    // ── Insert ───────────────────────────────────────────────────────────────

    override suspend fun insert(task: TaskEntity) = taskDao.insert(task)

    override suspend fun addTask(
        title: String,
        date: LocalDate,
        note: String?,
        startTime: String?,
        category: String?,
        priority: Int,
        reminderTime: String?,
    ): TaskEntity {
        val now = dateTimeProvider.currentTimeMillis()
        val task = TaskEntity(
            id = UUID.randomUUID().toString(),
            title = title.trim(),
            note = note?.takeIf { it.isNotBlank() }?.trim(),
            plannedDate = date.toString(),
            startTime = startTime?.takeIf { it.isNotBlank() },
            endTime = null,
            category = category?.takeIf { it.isNotBlank() },
            color = null,
            completionState = TaskCompletionState.PENDING.value,
            completedAt = null,
            sourceTemplateId = null,
            createdAt = now,
            updatedAt = now,
            priority = priority.coerceIn(1, 3),
            reminderTime = reminderTime?.takeIf { it.isNotBlank() },
        )
        taskDao.insert(task)
        return task
    }

    // ── Update ───────────────────────────────────────────────────────────────

    override suspend fun update(task: TaskEntity) = taskDao.update(task)

    override suspend fun setCompletionState(
        task: TaskEntity,
        state: TaskCompletionState,
    ): TaskEntity {
        val now = dateTimeProvider.currentTimeMillis()
        val isCompleting = state == TaskCompletionState.COMPLETED
        val updated = task.copy(
            completionState = state.value,
            completedAt = if (isCompleting) now else null,
            updatedAt = now,
        )
        taskDao.update(updated)
        return updated
    }

    override suspend fun setCompletionStateById(taskId: String, state: TaskCompletionState) {
        taskDao.setCompletionStateById(
            id = taskId,
            state = state.value,
            completedAt = if (state == TaskCompletionState.COMPLETED) dateTimeProvider.currentTimeMillis() else null,
        )
    }

    override suspend fun updateTitle(task: TaskEntity, title: String) {
        val clean = title.trim()
        if (clean.isBlank()) return
        taskDao.update(task.copy(title = clean, updatedAt = dateTimeProvider.currentTimeMillis()))
    }

    override suspend fun updateFull(
        task: TaskEntity,
        title: String,
        note: String?,
        plannedDate: LocalDate,
        startTime: String?,
        category: String?,
        priority: Int,
        reminderTime: String?,
    ): TaskEntity {
        val cleanTitle = title.trim()
        if (cleanTitle.isBlank()) return task
        val updated = task.copy(
            title = cleanTitle,
            note = note?.takeIf { it.isNotBlank() }?.trim(),
            plannedDate = plannedDate.toString(),
            startTime = startTime?.takeIf { it.isNotBlank() },
            category = category?.takeIf { it.isNotBlank() },
            priority = priority.coerceIn(1, 3),
            reminderTime = reminderTime?.takeIf { it.isNotBlank() },
            updatedAt = dateTimeProvider.currentTimeMillis(),
        )
        taskDao.update(updated)
        if (task.plannedDate != updated.plannedDate) {
            completionLogDao.deleteForDate(CompletionEntityType.TASK.value, task.id, task.plannedDate)
        }
        return updated
    }

    override suspend fun moveToDate(task: TaskEntity, date: LocalDate): TaskEntity {
        val updated = task.copy(
            plannedDate = date.toString(),
            completionState = TaskCompletionState.PENDING.value,
            completedAt = null,
            updatedAt = dateTimeProvider.currentTimeMillis(),
        )
        taskDao.update(updated)
        completionLogDao.deleteForDate(CompletionEntityType.TASK.value, task.id, task.plannedDate)
        return updated
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    override suspend fun delete(task: TaskEntity) {
        taskDao.deleteById(task.id)
        completionLogDao.deleteForEntity(CompletionEntityType.TASK.value, task.id)
    }

    override suspend fun deleteAll() = taskDao.deleteAll()

    // ── Restore ──────────────────────────────────────────────────────────────

    override suspend fun restore(task: TaskEntity) {
        taskDao.insert(
            task.copy(
                completionState = TaskCompletionState.PENDING.value,
                completedAt = null,
            ),
        )
    }

    // ── Completion log helpers ────────────────────────────────────────────────

    override suspend fun writeCompletionLog(task: TaskEntity) {
        completionLogDao.upsert(
            CompletionLogEntity(
                id = "task-${task.id}-${task.plannedDate}",
                entityType = CompletionEntityType.TASK.value,
                entityId = task.id,
                date = task.plannedDate,
                completedAt = dateTimeProvider.currentTimeMillis(),
                status = CompletionStatus.COMPLETED.value,
                note = null,
            ),
        )
    }

    override suspend fun deleteCompletionLog(task: TaskEntity) {
        completionLogDao.deleteForDate(CompletionEntityType.TASK.value, task.id, task.plannedDate)
    }

    // ── Pending carry-over ───────────────────────────────────────────────────

    override suspend fun getPendingBefore(date: LocalDate): List<TaskEntity> =
        taskDao.getPendingBefore(date.toString())

    // ── SubTask ──────────────────────────────────────────────────────────────

    override suspend fun addSubTask(taskId: String, title: String) {
        if (title.isBlank()) return
        subTaskDao.insert(
            SubTaskEntity(
                id = UUID.randomUUID().toString(),
                taskId = taskId,
                title = title.trim(),
                isCompleted = false,
                sortOrder = 0,
                createdAt = dateTimeProvider.currentTimeMillis(),
            ),
        )
    }

    override suspend fun toggleSubTask(subTask: SubTaskEntity) {
        subTaskDao.update(subTask.copy(isCompleted = !subTask.isCompleted))
    }

    override suspend fun deleteSubTask(subTask: SubTaskEntity) {
        subTaskDao.deleteById(subTask.id)
    }

    override suspend fun deleteAllSubTasks() = subTaskDao.deleteAll()

    // ── Count ────────────────────────────────────────────────────────────────

    override suspend fun count(): Int = taskDao.count()

    override suspend fun deleteTemplateTasks() = taskDao.deleteTemplateTasks()
}
