package com.example.app9

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app9.database.AppDatabase
import com.example.app9.database.RecurringTaskDao
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar
import com.example.app9.ShowPlans.QuadrantView
import com.example.app9.ShowPlans.TaskAdapter
import com.example.app9.database.OneTimeTaskDao

class HomeFragment : Fragment() {

    private lateinit var todayTasksRecyclerView: RecyclerView
    private lateinit var quadrantView: QuadrantView

    private lateinit var database: AppDatabase
    private lateinit var oneTimeTaskDao: OneTimeTaskDao
    private lateinit var recurringTaskDao: RecurringTaskDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        database = AppDatabase.getDatabase(requireContext())
        oneTimeTaskDao = database.oneTimeTaskDao()
        recurringTaskDao = database.recurringTaskDao()

        todayTasksRecyclerView = view.findViewById(R.id.todayTasksRecyclerView)
        quadrantView = view.findViewById(R.id.quadrantView)

        todayTasksRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        loadTasks()

        return view
    }

    private fun loadTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            val oneTimeTasks = oneTimeTaskDao.getUnfinishedTasks().collect { oneTimeTasks ->
                val recurringTasks = recurringTaskDao.getAllTasks().collect { recurringTasks ->
                    withContext(Dispatchers.Main) {
                        // 将 OneTimeTask 和 RecurringTask 转换为 Task.OneTimeTask 和 Task.RecurringTask
                        val taskOneTimeTasks = oneTimeTasks.map { Task.OneTimeTask(it) }
                        val taskRecurringTasks = recurringTasks.map { Task.RecurringTask(it) }

                        // 合并任务
                        val allTasks = taskOneTimeTasks + taskRecurringTasks

                        showTodayTasks(allTasks)
                        quadrantView.setTasks(allTasks)
                    }
                }
            }
        }
    }

    private fun combineTasks(oneTimeTasks: List<Task.OneTimeTask>, recurringTasks: List<Task.RecurringTask>): List<Task> {
        return oneTimeTasks + recurringTasks
    }

    private fun showTodayTasks(tasks: List<Task>) {
        val today = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val todayTasks = tasks.filter { task ->
            when (task) {
                is Task.OneTimeTask -> task.task.deadline <= today + 24 * 60 * 60 * 1000
                is Task.RecurringTask -> task.task.nextTriggerTime <= today + 24 * 60 * 60 * 1000
            }
        }

        todayTasksRecyclerView.adapter = TaskAdapter(todayTasks,
            onTaskClick = { task -> /* Handle task click */ },
            onTaskDone = { task ->
                CoroutineScope(Dispatchers.IO).launch {
                    when (task) {
                        is Task.OneTimeTask -> oneTimeTaskDao.updateDoneStatus(task.task.id, true)
                        is Task.RecurringTask -> recurringTaskDao.updateDoneStatus(task.task.id, true)
                    }
                    loadTasks() // 重新加载任务列表
                }
            }
        )
    }
}

sealed class Task {
    data class OneTimeTask(val task: com.example.app9.database.OneTimeTask) : Task()
    data class RecurringTask(val task: com.example.app9.database.RecurringTask) : Task()
}