package com.benimgunlerim.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.benimgunlerim.data.local.entity.TaskEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE plannedDate = :date ORDER BY startTime IS NULL ASC, startTime ASC, priority DESC, createdAt ASC")
    fun observeByDate(date: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE plannedDate < :today AND completionState = 'pending' AND (isArchived = 0 OR isArchived IS NULL) ORDER BY plannedDate ASC, priority DESC")
    fun observeOverdue(today: String): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks WHERE plannedDate >= :from AND plannedDate <= :to AND (isArchived = 0 OR isArchived IS NULL) ORDER BY plannedDate ASC, startTime IS NULL ASC, startTime ASC, priority DESC")
    fun observeRange(from: String, to: String): Flow<List<TaskEntity>>

    @Query("SELECT COUNT(*) FROM tasks")
    suspend fun count(): Int

    @Query("SELECT * FROM tasks WHERE plannedDate < :before AND completionState = 'pending' AND (isArchived = 0 OR isArchived IS NULL)")
    suspend fun getPendingBefore(before: String): List<TaskEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(task: TaskEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(tasks: List<TaskEntity>)

    @Update
    suspend fun update(task: TaskEntity)

    @Query("DELETE FROM tasks WHERE id = :id")
    suspend fun deleteById(id: String)

    @Query("UPDATE tasks SET completionState = :state, completedAt = :completedAt WHERE id = :id")
    suspend fun setCompletionStateById(id: String, state: String, completedAt: Long?)

    @Query("DELETE FROM tasks WHERE sourceTemplateId IS NOT NULL")
    suspend fun deleteTemplateTasks()

    @Query("DELETE FROM tasks")
    suspend fun deleteAll()
}
