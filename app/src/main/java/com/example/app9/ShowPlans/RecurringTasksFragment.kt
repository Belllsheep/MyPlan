package com.example.app9.ShowPlans

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.app9.R
import com.example.app9.database.AppDatabase
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class RecurringTasksFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: RecurringTaskAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_recurring_tasks, container, false)

        // 初始化 RecyclerView
        recyclerView = view.findViewById(R.id.recurringTasksRecyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        // 加载任务数据
        lifecycleScope.launch {
            val db = AppDatabase.getDatabase(requireContext())
            db.recurringTaskDao().getAllTasks().collect { tasks ->
                adapter = RecurringTaskAdapter(tasks)
                recyclerView.adapter = adapter
            }
        }

        return view
    }
}