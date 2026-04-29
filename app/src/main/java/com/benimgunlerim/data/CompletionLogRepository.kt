package com.benimgunlerim.data

import com.benimgunlerim.data.local.CompletionLogDao
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Abstraction for completion-log persistence.
 */
interface CompletionLogRepository {
    fun observeByDate(date: LocalDate): Flow<List<CompletionLogEntity>>
    fun observeAll(): Flow<List<CompletionLogEntity>>
    fun observeBetween(from: LocalDate, to: LocalDate): Flow<List<CompletionLogEntity>>
    suspend fun upsert(log: CompletionLogEntity)
    suspend fun deleteForDate(entityType: String, entityId: String, date: String)
    suspend fun deleteForEntity(entityType: String, entityId: String)
    suspend fun deleteAll()
}

/**
 * Room-backed implementation of completion-log persistence.
 */
@Singleton
class CompletionLogRepositoryImpl @Inject constructor(
    private val completionLogDao: CompletionLogDao,
) : CompletionLogRepository {
    override fun observeByDate(date: LocalDate): Flow<List<CompletionLogEntity>> =
        completionLogDao.observeByDate(date.toString())

    override fun observeAll(): Flow<List<CompletionLogEntity>> =
        completionLogDao.observeAll()

    override fun observeBetween(from: LocalDate, to: LocalDate): Flow<List<CompletionLogEntity>> =
        completionLogDao.observeBetween(from.toString(), to.toString())

    override suspend fun upsert(log: CompletionLogEntity) = completionLogDao.upsert(log)

    override suspend fun deleteForDate(entityType: String, entityId: String, date: String) =
        completionLogDao.deleteForDate(entityType, entityId, date)

    override suspend fun deleteForEntity(entityType: String, entityId: String) =
        completionLogDao.deleteForEntity(entityType, entityId)

    override suspend fun deleteAll() = completionLogDao.deleteAll()
}
