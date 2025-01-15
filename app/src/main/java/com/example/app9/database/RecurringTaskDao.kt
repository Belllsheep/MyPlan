//重复任务访问接口
package com.example.app9.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface RecurringTaskDao {
    @Insert
    suspend fun insert(task: RecurringTask): Long

    @Query("SELECT * FROM recurring_tasks ORDER BY nextTriggerTime ASC")
    fun getAllTasks(): Flow<List<RecurringTask>>

    @Query("UPDATE recurring_tasks SET haveDone = :haveDone WHERE id = :taskId")
    suspend fun updateDoneStatus(taskId: Int, haveDone: Boolean)

    @Query("DELETE FROM recurring_tasks WHERE id = :taskId")
    suspend fun delete(taskId: Int)

    @Query("UPDATE recurring_tasks SET nextTriggerTime = :nextTriggerTime WHERE id = :taskId")
    suspend fun updateNextTriggerTime(taskId: Int, nextTriggerTime: Long)

    // 添加根据任务 ID 获取任务的方法
    @Query("SELECT * FROM recurring_tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTaskById(taskId: Int): RecurringTask?
}