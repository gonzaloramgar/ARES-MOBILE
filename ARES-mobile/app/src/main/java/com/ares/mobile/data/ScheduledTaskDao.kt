package com.ares.mobile.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduledTaskDao {
    @Query("SELECT * FROM scheduled_tasks ORDER BY triggerAtMillis ASC")
    fun observeTasks(): Flow<List<ScheduledTaskEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(task: ScheduledTaskEntity): Long

    @Query("DELETE FROM scheduled_tasks WHERE id = :id")
    suspend fun deleteById(id: Long)
}