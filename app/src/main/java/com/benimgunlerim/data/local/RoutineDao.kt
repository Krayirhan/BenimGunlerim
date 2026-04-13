package com.benimgunlerim.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.benimgunlerim.data.local.entity.RoutineEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface RoutineDao {
    @Query("SELECT * FROM routines WHERE isArchived = 0 ORDER BY createdAt ASC")
    fun observeActive(): Flow<List<RoutineEntity>>

    @Query("SELECT * FROM routines WHERE isArchived = 0 AND preferredTime IS NOT NULL")
    suspend fun getActiveWithReminder(): List<RoutineEntity>

    @Query("SELECT COUNT(*) FROM routines")
    suspend fun count(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(routine: RoutineEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(routines: List<RoutineEntity>)

    @Update
    suspend fun update(routine: RoutineEntity)

    @Query("SELECT * FROM routines ORDER BY createdAt ASC")
    suspend fun getAll(): List<RoutineEntity>

    @Query("DELETE FROM routines WHERE name IN (:names)")
    suspend fun deleteByNames(names: List<String>)

    @Query("DELETE FROM routines")
    suspend fun deleteAll()
}
