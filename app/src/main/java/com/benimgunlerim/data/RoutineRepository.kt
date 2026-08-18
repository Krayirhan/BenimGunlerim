package com.benimgunlerim.data

import com.benimgunlerim.data.local.CompletionLogDao
import com.benimgunlerim.data.local.RoutineDao
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import com.benimgunlerim.data.local.entity.RoutineEntity
import com.benimgunlerim.domain.DateTimeProvider
import com.benimgunlerim.domain.model.CompletionEntityType
import com.benimgunlerim.domain.model.CompletionStatus
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Pure data-access layer for routines.
 * No reward granting, no notification scheduling — those belong in use-cases.
 */
@Singleton
class RoutineRepository @Inject constructor(
    private val routineDao: RoutineDao,
    private val completionLogDao: CompletionLogDao,
    private val dateTimeProvider: DateTimeProvider,
) {
    // ── Observe ──────────────────────────────────────────────────────────────

    fun observeActive(): Flow<List<RoutineEntity>> = routineDao.observeActive()

    // ── Insert ───────────────────────────────────────────────────────────────

    suspend fun add(
        name: String,
        targetDays: Set<DayOfWeek> = DayOfWeek.entries.toSet(),
        preferredTime: String? = null,
        targetType: String = "check",
        targetValue: Int? = null,
        targetUnit: String? = null,
        category: String? = null,
    ): RoutineEntity {
        val now = dateTimeProvider.currentTimeMillis()
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
            category = category?.takeIf { it.isNotBlank() },
        )
        routineDao.insert(routine)
        return routine
    }

    // ── Update ───────────────────────────────────────────────────────────────

    @Suppress("LongParameterList")
    suspend fun update(
        routine: RoutineEntity,
        name: String,
        targetDays: Set<DayOfWeek>,
        preferredTime: String?,
        targetType: String = routine.targetType,
        targetValue: Int? = routine.targetValue,
        targetUnit: String? = routine.targetUnit,
        category: String? = routine.category,
    ): RoutineEntity {
        val cleanName = name.trim()
        if (cleanName.isBlank() || targetDays.isEmpty()) return routine
        val updated = routine.copy(
            name = cleanName,
            targetDays = targetDays.joinToString(",") { it.name },
            preferredTime = preferredTime?.takeIf { it.isNotBlank() },
            targetType = targetType,
            targetValue = targetValue?.takeIf { it > 0 },
            targetUnit = targetUnit?.takeIf { it.isNotBlank() },
            category = category?.takeIf { it.isNotBlank() },
            updatedAt = dateTimeProvider.currentTimeMillis(),
        )
        routineDao.update(updated)
        return updated
    }

    suspend fun archive(routine: RoutineEntity): RoutineEntity {
        val archived = routine.copy(isArchived = true, updatedAt = dateTimeProvider.currentTimeMillis())
        routineDao.update(archived)
        return archived
    }

    suspend fun unarchive(routine: RoutineEntity): RoutineEntity {
        val restored = routine.copy(isArchived = false, updatedAt = dateTimeProvider.currentTimeMillis())
        routineDao.update(restored)
        return restored
    }

    fun observeArchived(): Flow<List<RoutineEntity>> = routineDao.observeArchived()

    // ── Completion log helpers ────────────────────────────────────────────────

    suspend fun writeCompletionLog(routine: RoutineEntity, date: LocalDate) {
        completionLogDao.upsert(
            CompletionLogEntity(
                id = "routine-${routine.id}-$date",
                entityType = CompletionEntityType.ROUTINE.value,
                entityId = routine.id,
                date = date.toString(),
                completedAt = dateTimeProvider.currentTimeMillis(),
                status = CompletionStatus.COMPLETED.value,
                note = null,
            ),
        )
    }

    suspend fun deleteCompletionLog(routineId: String, date: LocalDate) {
        completionLogDao.deleteForDate(CompletionEntityType.ROUTINE.value, routineId, date.toString())
    }

    suspend fun setProgress(routine: RoutineEntity, value: Float, date: LocalDate) {
        val cleanValue = value.coerceAtLeast(0f)
        val target = routine.targetValue?.toFloat()?.takeIf { it > 0f } ?: 1f
        if (cleanValue <= 0f) {
            completionLogDao.deleteForDate(CompletionEntityType.ROUTINE.value, routine.id, date.toString())
            return
        }
        val completed = cleanValue >= target
        completionLogDao.upsert(
            CompletionLogEntity(
                id = "routine-${routine.id}-$date",
                entityType = CompletionEntityType.ROUTINE.value,
                entityId = routine.id,
                date = date.toString(),
                completedAt = if (completed) dateTimeProvider.currentTimeMillis() else null,
                status = if (completed) CompletionStatus.COMPLETED.value else CompletionStatus.PARTIAL.value,
                note = null,
                value = cleanValue,
                targetValue = target,
            ),
        )
    }

    suspend fun skip(routine: RoutineEntity, date: LocalDate) {
        completionLogDao.upsert(
            CompletionLogEntity(
                id = "routine-${routine.id}-$date",
                entityType = CompletionEntityType.ROUTINE.value,
                entityId = routine.id,
                date = date.toString(),
                completedAt = null,
                status = CompletionStatus.SKIPPED.value,
                note = null,
                value = 0f,
                targetValue = routine.targetValue?.toFloat(),
            ),
        )
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    suspend fun deleteAll() = routineDao.deleteAll()

    suspend fun deleteByNames(names: List<String>) = routineDao.deleteByNames(names)

    // ── Count ────────────────────────────────────────────────────────────────

    suspend fun count(): Int = routineDao.count()
}
