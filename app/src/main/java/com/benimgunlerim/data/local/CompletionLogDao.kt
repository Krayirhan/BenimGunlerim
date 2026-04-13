package com.benimgunlerim.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benimgunlerim.data.local.entity.CompletionLogEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CompletionLogDao {
    @Query("SELECT * FROM completion_logs WHERE date = :date")
    fun observeByDate(date: String): Flow<List<CompletionLogEntity>>

    @Query("SELECT * FROM completion_logs WHERE entityType = :entityType AND entityId = :entityId ORDER BY date DESC")
    fun observeForEntity(entityType: String, entityId: String): Flow<List<CompletionLogEntity>>

    @Query("SELECT * FROM completion_logs ORDER BY date DESC")
    fun observeAll(): Flow<List<CompletionLogEntity>>

    @Query("SELECT * FROM completion_logs WHERE date >= :from AND date <= :to ORDER BY date DESC")
    fun observeBetween(from: String, to: String): Flow<List<CompletionLogEntity>>

    @Query("SELECT * FROM completion_logs ORDER BY date DESC")
    suspend fun getAll(): List<CompletionLogEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(log: CompletionLogEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(logs: List<CompletionLogEntity>)

    @Query("DELETE FROM completion_logs WHERE entityType = :entityType AND entityId = :entityId AND date = :date")
    suspend fun deleteForDate(entityType: String, entityId: String, date: String)

    @Query("DELETE FROM completion_logs WHERE entityType = :entityType AND entityId = :entityId")
    suspend fun deleteForEntity(entityType: String, entityId: String)

    @Query("DELETE FROM completion_logs")
    suspend fun deleteAll()
}
