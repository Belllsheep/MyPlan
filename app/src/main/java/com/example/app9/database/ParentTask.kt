//大任务表
package com.example.app9.database

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "parent_tasks")
data class ParentTask(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val description: String,
    val deadline: Long,
    val importance: Short,
    val isReminderEnabled: Boolean,
    val haveDone: Boolean = false
)