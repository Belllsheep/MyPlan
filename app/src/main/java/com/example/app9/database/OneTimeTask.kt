//一次性任务
package com.example.app9.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "one_time_tasks")
data class OneTimeTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val deadline: Long, // 截止时间
    val nextReminderTime: Long, // 下一次提醒时间
    val importance: Short,
    val isReminderEnabled: Boolean,
    val haveDone: Boolean = false
)