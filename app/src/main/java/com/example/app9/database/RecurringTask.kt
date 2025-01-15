//重复性任务
package com.example.app9.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "recurring_tasks")
data class RecurringTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val importance: Short,
    val isReminderEnabled: Boolean,
    val repeatRule: String, // 重复规则的 JSON 字符串
    val nextTriggerTime: Long, // 下一次触发时间
    val haveDone: Boolean = false
)