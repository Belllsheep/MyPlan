package com.example.app9.ShowPlans

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.app9.R
import com.example.app9.database.OneTimeTask
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class OneTimeTaskAdapter(
    private var tasks: List<OneTimeTask>, // 任务列表
    private val onTaskClick: (OneTimeTask) -> Unit, // 点击任务回调
    private val onTaskDone: (OneTimeTask) -> Unit // 完成任务回调
) : RecyclerView.Adapter<OneTimeTaskAdapter.TaskViewHolder>() {

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.task_title)
        private val descriptionTextView: TextView = itemView.findViewById(R.id.task_description)
        private val deadlineTextView: TextView = itemView.findViewById(R.id.task_deadline)
        private val importanceTextView: TextView = itemView.findViewById(R.id.task_importance)
        private val doneButton: Button = itemView.findViewById(R.id.task_done_button)

        fun bind(task: OneTimeTask) {
            titleTextView.text = task.title
            descriptionTextView.text = task.description
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault())
            val dateString = dateFormat.format(Date(task.deadline))
            deadlineTextView.text = dateString

            // 设置重要性星星
            importanceTextView.text = getImportanceStars(task.importance.toInt())

            // 绑定 Done 按钮的点击事件
            doneButton.setOnClickListener {
                Log.d("OneTimeTaskAdapter", "Done button clicked for task: ${task.title}")
                onTaskDone(task) // 调用 onTaskDone 回调
            }

            // 绑定任务项的点击事件
            itemView.setOnClickListener {
                onTaskClick(task)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_one_time_task, parent, false)
        return TaskViewHolder(view)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(tasks[position])
    }

    override fun getItemCount(): Int = tasks.size

    // 更新任务列表
    fun submitList(newTasks: List<OneTimeTask>) {
        this.tasks = newTasks
        notifyDataSetChanged() // 通知适配器数据已更改
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