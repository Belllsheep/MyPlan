package com.example.app9.ShowPlans

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app9.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import com.example.app9.database.AppDatabase
import com.example.app9.database.OneTimeTaskDao

class FinishedTasksFragment : Fragment() {

    private lateinit var adapter: OneTimeTaskAdapter
    private lateinit var database: AppDatabase
    private lateinit var oneTimeTaskDao: OneTimeTaskDao

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_finished_tasks, container, false)

        database = AppDatabase.getDatabase(requireContext())
        oneTimeTaskDao = database.oneTimeTaskDao()

        // 初始化适配器，传递一个空的任务列表
        adapter = OneTimeTaskAdapter(
            tasks = emptyList(), // 初始化为空列表
            onTaskClick = { task ->
                // 处理任务点击事件
            },
            onTaskDone = { task ->
                // 处理任务完成事件
                CoroutineScope(Dispatchers.IO).launch {
                    // 更新任务状态为未完成
                    oneTimeTaskDao.updateDoneStatus(task.id, false)
                    // 重新加载任务列表
                    loadFinishedTasks()
                }
            }
        )

        val recyclerView = view.findViewById<RecyclerView>(R.id.finishedTasksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        recyclerView.adapter = adapter

        loadFinishedTasks()

        return view
    }

    private fun loadFinishedTasks() {
        CoroutineScope(Dispatchers.IO).launch {
            oneTimeTaskDao.getFinishedTasks().collect { tasks ->
                withContext(Dispatchers.Main) {
                    // 更新适配器的任务列表
                    adapter.submitList(tasks)
                }
            }
        }
    }
}