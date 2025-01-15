package com.example.app9.AddPlans

import android.app.DatePickerDialog
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
import com.example.app9.database.OneTimeTask
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.*

class OneTimePlanFragment : Fragment() {

    private lateinit var titleInput: TextInputEditText
    private lateinit var descriptionInput: TextInputEditText
    private lateinit var importanceSpinner: Spinner
    private lateinit var deadlineDateInput: TextInputEditText
    private lateinit var timeInput: TextInputEditText
    private lateinit var reminderCheckbox: CheckBox
    private lateinit var addButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_one_time_plan, container, false)

        // 初始化视图
        titleInput = view.findViewById(R.id.titleInput) // 初始化 titleInput
        descriptionInput = view.findViewById(R.id.descriptionInput) // 初始化 descriptionInput
        importanceSpinner = view.findViewById(R.id.importanceSpinner)
        deadlineDateInput = view.findViewById(R.id.deadlineDateInput)
        timeInput = view.findViewById(R.id.timeInput)
        reminderCheckbox = view.findViewById(R.id.reminderCheckbox)
        addButton = view.findViewById(R.id.addButton)

        // 设置 Spinner 的选项为 1 到 10
        val importanceOptions = (1..10).map { it.toString() }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, importanceOptions)
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        importanceSpinner.adapter = adapter

        // 设置日期选择器
        deadlineDateInput.setOnClickListener {
            showDatePicker()
        }

        // 设置时间选择器
        timeInput.setOnClickListener {
            showTimePicker()
        }

        // 添加按钮点击事件
        addButton.setOnClickListener {
            lifecycleScope.launch {
                createOneTimeTask()
            }
        }

        return view
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = "$selectedDay/${selectedMonth + 1}/$selectedYear"
                deadlineDateInput.setText(selectedDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePickerDialog = TimePickerDialog(
            requireContext(),
            { _, selectedHour, selectedMinute ->
                val selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
                timeInput.setText(selectedTime)
            },
            hour,
            minute,
            true // 24小时制
        )
        timePickerDialog.show()
    }

    private suspend fun createOneTimeTask() {
        try {
            // 获取用户输入
            val title = titleInput.text.toString()
            val description = descriptionInput.text.toString()
            val importance = importanceSpinner.selectedItem.toString().toShort()
            val deadlineDate = deadlineDateInput.text.toString()
            val time = timeInput.text.toString()

            // 将日期和时间转换为时间戳
            val deadlineTimestamp = parseDateAndTime(deadlineDate, time)

            // 创建任务对象
            val task = OneTimeTask(
                title = title,
                description = description,
                deadline = deadlineTimestamp,
                nextReminderTime = deadlineTimestamp, // 假设提醒时间与截止时间相同
                importance = importance,
                isReminderEnabled = reminderCheckbox.isChecked
            )

            // 插入数据库
            val db = AppDatabase.getDatabase(requireContext())
            db.oneTimeTaskDao().insert(task)

            // 提示用户任务添加成功
            Toast.makeText(requireContext(), "任务添加成功", Toast.LENGTH_SHORT).show()

            // 清空输入框
            clearInputs()
        } catch (e: Exception) {
            // 提示用户任务添加失败
            Toast.makeText(requireContext(), "任务添加失败: ${e.message}", Toast.LENGTH_SHORT).show()
        }
    }

    private fun parseDateAndTime(dateStr: String, timeStr: String): Long {
        val format = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        val dateTimeStr = "$dateStr $timeStr"
        return format.parse(dateTimeStr)?.time ?: throw IllegalArgumentException("Invalid date or time")
    }

    private fun clearInputs() {
        titleInput.text?.clear()
        descriptionInput.text?.clear()
        deadlineDateInput.text?.clear()
        timeInput.text?.clear()
        reminderCheckbox.isChecked = false
        importanceSpinner.setSelection(0)
    }
}