package com.example.app9.ShowPlans

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app9.R
import com.example.app9.database.RecurringTask
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class RecurringTaskAdapter(private val tasks: List<RecurringTask>) :
    RecyclerView.Adapter<RecurringTaskAdapter.TaskViewHolder>() {

    // ViewHolder 类
    class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val title: TextView = itemView.findViewById(R.id.taskTitle)
        val description: TextView = itemView.findViewById(R.id.taskDescription)
        val importance: TextView = itemView.findViewById(R.id.taskImportance)
        val nextTriggerTime: TextView = itemView.findViewById(R.id.taskNextTriggerTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recurring_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = tasks[position]
        holder.title.text = task.title
        holder.description.text = task.description

        // 将 importance 从 Short 转换为 Int
        holder.importance.text = "重要性: ${getImportanceStars(task.importance.toInt())}"

        // 格式化下一次触发时间
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
        val date = Date(task.nextTriggerTime)
        holder.nextTriggerTime.text = "下一次触发时间：${dateFormat.format(date)}"
    }

    override fun getItemCount(): Int {
        return tasks.size
    }

    // 获取重要性星星的显示文本
    private fun getImportanceStars(importance: Int): CharSequence {
        val fullStar = "★"
        val halfStar = "½"
        val emptyStar = "☆"
        val stars = StringBuilder()

        // 计算完整星星和半星星的数量
        val fullStars = importance / 2
        val hasHalfStar = importance % 2 != 0

        // 添加完整星星
        for (i in 1..fullStars) {
            stars.append(fullStar)
        }

        // 添加半星星
        if (hasHalfStar) {
            stars.append(halfStar)
        }

        // 添加空星星
        val remainingStars = 5 - fullStars - if (hasHalfStar) 1 else 0
        for (i in 1..remainingStars) {
            stars.append(emptyStar)
        }

        return stars.toString()
    }
}