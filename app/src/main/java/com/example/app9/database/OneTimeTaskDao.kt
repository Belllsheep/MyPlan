package com.example.app9.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface OneTimeTaskDao {
    @Insert
    suspend fun insert(task: OneTimeTask): Long

    @Query("SELECT * FROM one_time_tasks ORDER BY deadline ASC")
    fun getAllTasks(): Flow<List<OneTimeTask>>

    @Query("UPDATE one_time_tasks SET haveDone = :haveDone WHERE id = :taskId")
    suspend fun updateDoneStatus(taskId: Int, haveDone: Boolean)

    @Query("DELETE FROM one_time_tasks WHERE id = :taskId")
    suspend fun delete(taskId: Int)

    @Query("SELECT * FROM one_time_tasks WHERE haveDone = 0 ORDER BY deadline ASC")
    fun getUnfinishedTasks(): Flow<List<OneTimeTask>>

    @Query("SELECT * FROM one_time_tasks WHERE haveDone = 1 ORDER BY deadline ASC")
    fun getFinishedTasks(): Flow<List<OneTimeTask>>
}