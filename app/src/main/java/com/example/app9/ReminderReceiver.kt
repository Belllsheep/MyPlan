package com.example.app9

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.app9.database.AppDatabase
import com.example.app9.database.RecurringTask
import com.example.app9.database.RecurringTaskDao
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.Calendar

class ReminderReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getIntExtra("taskId", -1)
        val title = intent.getStringExtra("title") ?: return
        val description = intent.getStringExtra("description") ?: return

        // 显示通知
        showNotification(context, taskId, title, description)

        // 如果是重复任务，设置下一次提醒
        if (intent.getBooleanExtra("isRecurring", false)) {
            CoroutineScope(Dispatchers.IO).launch {
                val db = AppDatabase.getDatabase(context)
                val recurringTaskDao = db.recurringTaskDao()
                val task = recurringTaskDao.getTaskById(taskId)
                if (task != null) {
                    val nextTriggerTime = calculateNextTriggerTime(task)
                    if (nextTriggerTime != null) {
                        // 更新任务的下一次触发时间
                        recurringTaskDao.updateNextTriggerTime(taskId, nextTriggerTime)
                        // 设置下一次提醒
                        setReminder(context, taskId, title, description, nextTriggerTime, true)
                        Log.d("何欣", "下一次提醒时间: $nextTriggerTime")
                    }
                }
            }
        }
    }

    private fun showNotification(context: Context, taskId: Int, title: String, description: String) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        val channelId = "plan_reminder_channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Plan Reminders",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(description)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setCategory(NotificationCompat.CATEGORY_REMINDER) // 设置通知类别
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC) // 设置可见性
            .setAutoCancel(true)
            .build()

        notificationManager.notify(taskId, notification)
    }

    private fun calculateNextTriggerTime(task: RecurringTask): Long? {
        val repeatRule = Gson().fromJson(task.repeatRule, Map::class.java)
        val frequency = repeatRule["frequency"] as String
        val times = repeatRule["times"] as List<String>
        val calendar = Calendar.getInstance()

        return when (frequency) {
            "DAILY" -> {
                // 找到下一个时间点
                for (time in times) {
                    val (hour, minute) = time.split(":").map { it.toInt() }
                    calendar.set(Calendar.HOUR_OF_DAY, hour)
                    calendar.set(Calendar.MINUTE, minute)
                    calendar.set(Calendar.SECOND, 0)
                    calendar.set(Calendar.MILLISECOND, 0)

                    if (calendar.timeInMillis > System.currentTimeMillis()) {
                        return calendar.timeInMillis
                    }
                }
                // 如果今天的时间点都已过，则设置为明天第一个时间点
                val (hour, minute) = times[0].split(":").map { it.toInt() }
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.add(Calendar.DAY_OF_MONTH, 1)
                calendar.timeInMillis
            }
            "WEEKLY" -> {
                val daysOfWeek = repeatRule["days_of_week"] as List<String>
                // 找到下一个符合条件的时间点
                for (day in daysOfWeek) {
                    val dayOfWeek = when (day) {
                        "MON" -> Calendar.MONDAY
                        "TUE" -> Calendar.TUESDAY
                        "WED" -> Calendar.WEDNESDAY
                        "THU" -> Calendar.THURSDAY
                        "FRI" -> Calendar.FRIDAY
                        "SAT" -> Calendar.SATURDAY
                        "SUN" -> Calendar.SUNDAY
                        else -> throw IllegalArgumentException("Invalid day of week: $day")
                    }
                    calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
                    for (time in times) {
                        val (hour, minute) = time.split(":").map { it.toInt() }
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        if (calendar.timeInMillis > System.currentTimeMillis()) {
                            return calendar.timeInMillis
                        }
                    }
                }
                // 如果本周的时间点都已过，则设置为下周第一个时间点
                val dayOfWeek = when (daysOfWeek[0]) {
                    "MON" -> Calendar.MONDAY
                    "TUE" -> Calendar.TUESDAY
                    "WED" -> Calendar.WEDNESDAY
                    "THU" -> Calendar.THURSDAY
                    "FRI" -> Calendar.FRIDAY
                    "SAT" -> Calendar.SATURDAY
                    "SUN" -> Calendar.SUNDAY
                    else -> throw IllegalArgumentException("Invalid day of week: ${daysOfWeek[0]}")
                }
                calendar.set(Calendar.DAY_OF_WEEK, dayOfWeek)
                val (hour, minute) = times[0].split(":").map { it.toInt() }
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
                calendar.timeInMillis
            }
            "MONTHLY" -> {
                val daysOfMonth = repeatRule["days_of_month"] as List<Int>
                // 找到下一个符合条件的时间点
                for (day in daysOfMonth) {
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    for (time in times) {
                        val (hour, minute) = time.split(":").map { it.toInt() }
                        calendar.set(Calendar.HOUR_OF_DAY, hour)
                        calendar.set(Calendar.MINUTE, minute)
                        calendar.set(Calendar.SECOND, 0)
                        calendar.set(Calendar.MILLISECOND, 0)

                        if (calendar.timeInMillis > System.currentTimeMillis()) {
                            return calendar.timeInMillis
                        }
                    }
                }
                // 如果本月的时间点都已过，则设置为下月第一个时间点
                calendar.set(Calendar.DAY_OF_MONTH, daysOfMonth[0])
                val (hour, minute) = times[0].split(":").map { it.toInt() }
                calendar.set(Calendar.HOUR_OF_DAY, hour)
                calendar.set(Calendar.MINUTE, minute)
                calendar.add(Calendar.MONTH, 1)
                calendar.timeInMillis
            }
            else -> null
        }
    }

    private fun setReminder(context: Context, taskId: Int, title: String, description: String, triggerTime: Long, isRecurring: Boolean) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(context, ReminderReceiver::class.java).apply {
            putExtra("taskId", taskId)
            putExtra("title", title)
            putExtra("description", description)
            putExtra("isRecurring", isRecurring)
        }

        // 检查并取消已经存在的 PendingIntent
        val existingPendingIntent = PendingIntent.getBroadcast(context, taskId, intent, PendingIntent.FLAG_NO_CREATE)
        if (existingPendingIntent != null) {
            alarmManager.cancel(existingPendingIntent)
            existingPendingIntent.cancel() // 显式取消 PendingIntent
        }

        val pendingIntent = PendingIntent.getBroadcast(context, taskId, intent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)

        // 设置提醒时间为触发时间前1分钟
        val reminderTime = triggerTime

        alarmManager.setExact(AlarmManager.RTC_WAKEUP, reminderTime, pendingIntent)
    }
}