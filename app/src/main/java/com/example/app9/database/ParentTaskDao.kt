//大任务访问接口
package com.example.app9.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ParentTaskDao {
    @Insert
    suspend fun insert(task: ParentTask): Long

    @Query("SELECT * FROM parent_tasks ORDER BY deadline ASC")
    fun getAllTasks(): Flow<List<ParentTask>>

    @Query("UPDATE parent_tasks SET haveDone = :haveDone WHERE id = :taskId")
    suspend fun updateDoneStatus(taskId: Int, haveDone: Boolean)

    @Query("DELETE FROM parent_tasks WHERE id = :taskId")
    suspend fun delete(taskId: Int)
}