package com.benimgunlerim.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.benimgunlerim.data.local.entity.DailyStateEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface DailyStateDao {
    @Query("SELECT * FROM daily_states WHERE date = :date")
    fun observeByDate(date: String): Flow<DailyStateEntity?>

    @Query("SELECT * FROM daily_states WHERE date = :date")
    suspend fun getByDate(date: String): DailyStateEntity?

    @Query("SELECT * FROM daily_states ORDER BY date DESC LIMIT :limit")
    fun observeRecent(limit: Int): Flow<List<DailyStateEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(state: DailyStateEntity)

    @Query("DELETE FROM daily_states")
    suspend fun deleteAll()
}
