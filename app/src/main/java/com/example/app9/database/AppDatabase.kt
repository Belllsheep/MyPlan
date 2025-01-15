package com.example.app9.database

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context

@Database(
    entities = [
        OneTimeTask::class, // 一次性任务表
        RecurringTask::class, // 重复任务表
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    // 一次性任务 DAO
    abstract fun oneTimeTaskDao(): OneTimeTaskDao

    // 重复任务 DAO
    abstract fun recurringTaskDao(): RecurringTaskDao


    // 单例模式，确保整个程序只有一个数据库对象
    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database" // 数据库名称保持不变
                ).fallbackToDestructiveMigration() // 数据库版本升级时，删除旧表并创建新表
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}