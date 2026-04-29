package com.benimgunlerim.data

import com.benimgunlerim.data.local.DailyStateDao
import com.benimgunlerim.data.local.entity.DailyStateEntity
import com.benimgunlerim.domain.DateTimeProvider
import java.time.LocalDate
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow

/**
 * Pure data-access layer for daily state records.
 */
@Singleton
class DailyStateRepository @Inject constructor(
    private val dailyStateDao: DailyStateDao,
    private val dateTimeProvider: DateTimeProvider,
) {
    fun observeByDate(date: LocalDate): Flow<DailyStateEntity?> =
        dailyStateDao.observeByDate(date.toString())

    fun observeToday(): Flow<DailyStateEntity?> =
        dailyStateDao.observeByDate(dateTimeProvider.today().toString())

    fun observeRecent(limit: Int = 7): Flow<List<DailyStateEntity>> =
        dailyStateDao.observeRecent(limit)

    suspend fun getByDate(date: LocalDate): DailyStateEntity? =
        dailyStateDao.getByDate(date.toString())

    suspend fun upsert(state: DailyStateEntity) = dailyStateDao.upsert(state)

    suspend fun saveSummary(
        date: LocalDate,
        mood: String,
        note: String,
        completionRate: Float,
        energyLevel: Int? = null,
        bestMoment: String? = null,
        challenge: String? = null,
        tomorrowIntention: String? = null,
        carriedTaskCount: Int = 0,
    ) {
        val dateStr = date.toString()
        val existing = dailyStateDao.getByDate(dateStr)
        dailyStateDao.upsert(
            DailyStateEntity(
                date = dateStr,
                mood = mood,
                energyLevel = energyLevel,
                completionRate = completionRate,
                note = note.ifBlank { null },
                reflection = note.ifBlank { null },
                dailyScore = (completionRate * 100).toInt(),
                bestMoment = bestMoment?.ifBlank { null },
                challenge = challenge?.ifBlank { null },
                tomorrowIntention = tomorrowIntention?.ifBlank { null },
                closedAt = existing?.closedAt ?: dateTimeProvider.currentTimeMillis(),
                carriedTaskCount = carriedTaskCount,
            ),
        )
    }

    suspend fun autoCloseIfMissed(date: LocalDate) {
        val dateStr = date.toString()
        val existing = dailyStateDao.getByDate(dateStr)
        if (existing?.closedAt != null) return
        dailyStateDao.upsert(
            DailyStateEntity(
                date = dateStr,
                mood = "normal",
                energyLevel = 3,
                completionRate = 0f,
                note = null,
                reflection = null,
                dailyScore = 0,
                closedAt = dateTimeProvider.currentTimeMillis(),
            ),
        )
    }

    suspend fun deleteAll() = dailyStateDao.deleteAll()
}
