package com.example.app9.AddPlans

import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.example.app9.R
import com.example.app9.database.AppDatabase
import com.example.app9.database.RecurringTask
import com.google.gson.Gson
import kotlinx.coroutines.launch
import java.util.*

class RepeatPlanFragment : Fragment() {

    private lateinit var titleInput: EditText
    private lateinit var descriptionInput: EditText
    private lateinit var importanceSpinner: Spinner
    private lateinit var frequencySpinner: Spinner
    private lateinit var monthlyDaysContainer: LinearLayout
    private lateinit var daysOfMonthInput: EditText
    private lateinit var addDayOfMonthButton: Button
    private lateinit var daysOfMonthList: TextView
    private lateinit var weeklyDaysContainer: LinearLayout
    private lateinit var daysOfWeekSpinner: Spinner
    private lateinit var addDayOfWeekButton: Button
    private lateinit var daysOfWeekList: TextView
    private lateinit var addTimeButton: Button
    private lateinit var timesList: TextView
    private lateinit var createTaskButton: Button

    private val selectedTimes = mutableListOf<String>()
    private val selectedDaysOfMonth = mutableListOf<Int>()
    private val selectedDaysOfWeek = mutableSetOf<String>() // 使用 Set 自动去重

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_repeat_plan, container, false)

        // 初始化视图
        titleInput = view.findViewById(R.id.titleInput)
        descriptionInput = view.findViewById(R.id.descriptionInput)
        importanceSpinner = view.findViewById(R.id.importanceSpinner)
        frequencySpinner = view.findViewById(R.id.frequencySpinner)
        monthlyDaysContainer = view.findViewById(R.id.monthlyDaysContainer)
        daysOfMonthInput = view.findViewById(R.id.daysOfMonthInput)
        addDayOfMonthButton = view.findViewById(R.id.addDayOfMonthButton)
        daysOfMonthList = view.findViewById(R.id.daysOfMonthList)
        weeklyDaysContainer = view.findViewById(R.id.weeklyDaysContainer)
        daysOfWeekSpinner = view.findViewById(R.id.daysOfWeekSpinner)
        addDayOfWeekButton = view.findViewById(R.id.addDayOfWeekButton)
        daysOfWeekList = view.findViewById(R.id.daysOfWeekList)
        addTimeButton = view.findViewById(R.id.addTimeButton)
        timesList = view.findViewById(R.id.timesList)
        createTaskButton = view.findViewById(R.id.createTaskButton)

        // 设置重要性 Spinner 的选项
        val importanceOptions = (1..10).map { it.toString() }
        val importanceAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, importanceOptions)
        importanceAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        importanceSpinner.adapter = importanceAdapter

        // 设置每周的星期 Spinner 的选项
        val daysOfWeekOptions = listOf("MON", "TUE", "WED", "THU", "FRI", "SAT", "SUN")
        val daysOfWeekAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, daysOfWeekOptions)
        daysOfWeekAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        daysOfWeekSpinner.adapter = daysOfWeekAdapter

        // 设置频率选择监听器
        frequencySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                when (position) {
                    0 -> { // 每天
                        monthlyDaysContainer.visibility = View.GONE
                        weeklyDaysContainer.visibility = View.GONE
                    }
                    1 -> { // 每周
                        monthlyDaysContainer.visibility = View.GONE
                        weeklyDaysContainer.visibility = View.VISIBLE
                    }
                    2 -> { // 每月
                        monthlyDaysContainer.visibility = View.VISIBLE
                        weeklyDaysContainer.visibility = View.GONE
                    }
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        // 添加每月的日期按钮点击事件
        addDayOfMonthButton.setOnClickListener {
            val day = daysOfMonthInput.text.toString().toIntOrNull()
            if (day != null && day in 1..31) {
                selectedDaysOfMonth.add(day)
                updateDaysOfMonthList()
                daysOfMonthInput.text.clear()
            } else {
                Toast.makeText(requireContext(), "请输入有效的日期（1-31）", Toast.LENGTH_SHORT).show()
            }
        }

        // 添加每周的星期按钮点击事件
        addDayOfWeekButton.setOnClickListener {
            val day = daysOfWeekSpinner.selectedItem as String
            if (selectedDaysOfWeek.add(day)) { // 如果添加成功（即没有重复）
                updateDaysOfWeekList()
            } else {
                Toast.makeText(requireContext(), "该星期已存在", Toast.LENGTH_SHORT).show()
            }
        }

        // 添加时间按钮点击事件
        addTimeButton.setOnClickListener {
            showTimePicker()
        }

        // 创建任务按钮点击事件
        createTaskButton.setOnClickListener {
            lifecycleScope.launch {
                createRecurringTask()
            }
        }

        return view
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val time = String.format("%02d:%02d", selectedHour, selectedMinute)
                selectedTimes.add(time)
                updateTimesList()
            },
            hour,
            minute,
            true
        )
        timePickerDialog.show()
    }

    private fun updateTimesList() {
        if (selectedTimes.isNotEmpty()) {
            timesList.visibility = View.VISIBLE
            timesList.text = "提醒时间列表：\n" + selectedTimes.joinToString("\n")
        } else {
            timesList.visibility = View.GONE
        }
    }

    private fun updateDaysOfMonthList() {
        if (selectedDaysOfMonth.isNotEmpty()) {
            daysOfMonthList.visibility = View.VISIBLE
            daysOfMonthList.text = "日期列表：\n" + selectedDaysOfMonth.joinToString("\n") { "$it 号" }
        } else {
            daysOfMonthList.visibility = View.GONE
        }
    }

    private fun updateDaysOfWeekList() {
        if (selectedDaysOfWeek.isNotEmpty()) {
            daysOfWeekList.visibility = View.VISIBLE
            daysOfWeekList.text = "星期列表：\n" + selectedDaysOfWeek.joinToString("\n")
        } else {
            daysOfWeekList.visibility = View.GONE
        }
    }

    private suspend fun createRecurringTask() {
        // 获取用户输入
        val title = titleInput.text.toString()
        val description = descriptionInput.text.toString()
        val importance = importanceSpinner.selectedItem.toString().toShort()
        val frequency = frequencySpinner.selectedItem.toString()
        val times = selectedTimes

        // 构建重复规则 JSON
        val repeatRule = when (frequency) {
            "每天" -> mapOf("frequency" to "DAILY", "times" to times)
            "每周" -> mapOf(
                "frequency" to "WEEKLY",
                "days_of_week" to selectedDaysOfWeek.toList(), // 将 Set 转换为 List
                "times" to times
            )
            "每月" -> mapOf(
                "frequency" to "MONTHLY",
                "days_of_month" to selectedDaysOfMonth,
                "times" to times
            )
            else -> throw IllegalArgumentException("Invalid frequency")
        }

        val repeatRuleJson = Gson().toJson(repeatRule)

        // 计算下一次触发时间
        val nextTriggerTime = calculateNextTriggerTime(repeatRule)

        // 创建任务对象
        val task = RecurringTask(
            title = title,
            description = description,
            importance = importance,
            isReminderEnabled = true,
            repeatRule = repeatRuleJson,
            nextTriggerTime = nextTriggerTime
        )

        // 插入数据库
        val db = AppDatabase.getDatabase(requireContext())
        db.recurringTaskDao().insert(task)
    }

    private fun calculateNextTriggerTime(repeatRule: Map<String, Any>): Long {
        // 根据重复规则计算下一次触发时间
        // 这里可以根据你的需求实现具体的逻辑
        return System.currentTimeMillis() // 示例代码，返回当前时间
    }
}