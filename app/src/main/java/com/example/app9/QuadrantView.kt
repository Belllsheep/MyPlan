package com.example.app9.ShowPlans

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import androidx.appcompat.app.AlertDialog
import com.example.app9.Task
import kotlin.math.abs
import kotlin.random.Random

class QuadrantView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    // 用于绘制坐标轴的画笔（固定黑色）
    private val axisPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 2f
        textSize = 24f
    }

    // 用于绘制任务点的画笔（颜色随机）
    private val taskPaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 2f
        textSize = 24f
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 24f
        textAlign = Paint.Align.CENTER
    }

    private var tasks: List<Task> = emptyList()

    // 内边距，用于缩小任务点的绘制区域
    private val padding = 100f

    // 向左偏移量
    private val offsetX = 50f // 向左偏移 50 像素

    // 用于存储已经绘制的任务点的位置
    private val drawnPoints = mutableListOf<PointF>()

    // 定义颜色数组（红、橙、黄、绿、青、蓝、紫）
    private val colors = intArrayOf(
        Color.RED,
        Color.rgb(255, 165, 0), // 橙色
        Color.GREEN,
        Color.CYAN,
        Color.BLUE,
        Color.rgb(128, 0, 128) // 紫色
    )

    fun setTasks(tasks: List<Task>) {
        this.tasks = tasks
        invalidate() // 触发重新绘制
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()

        // 计算实际绘制区域（减去内边距）
        val drawWidth = width - 2 * padding
        val drawHeight = height - 2 * padding

        // 绘制坐标系（使用 axisPaint，固定黑色）
        // 垂直中线向左偏移
        canvas.drawLine(width / 2 - offsetX, padding, width / 2 - offsetX, height - padding, axisPaint)
        // 水平中线向左偏移
        canvas.drawLine(padding - offsetX, height / 2, width - padding - offsetX, height / 2, axisPaint)

        // 绘制坐标轴标签（使用 axisPaint，固定黑色）
        // x 轴标签（重要程度）
        canvas.drawText("重要", width - padding - offsetX - 30f, height / 2 - 20f, textPaint) // 右侧标明“重要”
        canvas.drawText("不重要", padding - offsetX + 30f, height / 2 - 20f, textPaint) // 左侧标明“不重要”

        // y 轴标签（紧急程度）
        canvas.drawText("紧急", width / 2 - offsetX + 50f, padding + 30f, textPaint) // 顶部标明“紧急”
        canvas.drawText("不紧急", width / 2 - offsetX + 50f, height - padding - 10f, textPaint) // 底部标明“不紧急”

        // 绘制箭头（使用 axisPaint，固定黑色）
        // x 轴箭头（右侧）
        drawArrow(canvas, width - padding - offsetX, height / 2, width - padding - offsetX, height / 2, true)
        // y 轴箭头（顶部）
        drawArrow(canvas, width / 2 - offsetX, padding, width / 2 - offsetX, padding, false)

        // 清空已绘制的任务点
        drawnPoints.clear()

        // 绘制任务点
        tasks.forEach { task ->
            val importance = when (task) {
                is Task.OneTimeTask -> task.task.importance
                is Task.RecurringTask -> task.task.importance
            }

            val deadline = when (task) {
                is Task.OneTimeTask -> task.task.deadline
                is Task.RecurringTask -> task.task.nextTriggerTime
            }

            // 计算任务点的坐标（向左偏移）
            val x = padding - offsetX + ((importance / 10f)) * drawWidth
            val y = padding + (1 - calculateUrgency(deadline) * calculateUrgency(deadline)) * drawHeight

            // 检查是否与已绘制的任务点位置相近
            val isOverlapping = drawnPoints.any { point ->
                abs(point.x - x) < 50f && abs(point.y - y) < 50f
            }

            // 如果重叠，则稍微调整位置
            val adjustedX = if (isOverlapping) x + 50f else x
            val adjustedY = if (isOverlapping) y + 50f else y

            // 随机选择一个颜色
            val randomColor = colors[Random.nextInt(colors.size)]
            taskPaint.color = randomColor

            // 绘制任务点（使用 taskPaint，颜色随机）
            val title = when (task) {
                is Task.OneTimeTask -> task.task.title
                is Task.RecurringTask -> task.task.title
            }
            canvas.drawText(title, adjustedX + 15f, adjustedY, taskPaint)

            // 绘制连线（使用 taskPaint，颜色随机）
            if (isOverlapping) {
                canvas.drawLine(x, y, adjustedX, adjustedY, taskPaint)
            }

            // 将当前任务点的位置添加到已绘制列表中
            drawnPoints.add(PointF(adjustedX, adjustedY))
        }
    }

    /**
     * 绘制箭头
     */
    private fun drawArrow(canvas: Canvas, startX: Float, startY: Float, endX: Float, endY: Float, isXAxis: Boolean) {
        val arrowSize = 20f // 箭头大小

        if (isXAxis) {
            // 绘制 x 轴箭头（右侧）
            canvas.drawLine(endX - arrowSize, endY - arrowSize, endX, endY, axisPaint) // 上斜线
            canvas.drawLine(endX - arrowSize, endY + arrowSize, endX, endY, axisPaint) // 下斜线
        } else {
            // 绘制 y 轴箭头（顶部）
            canvas.drawLine(endX - arrowSize, endY + arrowSize, endX, endY, axisPaint) // 左斜线
            canvas.drawLine(endX + arrowSize, endY + arrowSize, endX, endY, axisPaint) // 右斜线
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 检查是否点击了任务点
                checkTaskClick(event.x, event.y)
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    /**
     * 检查是否点击了任务点
     */
    private fun checkTaskClick(touchX: Float, touchY: Float) {
        // 遍历所有任务点，检查点击位置是否在任务点的范围内
        tasks.forEach { task ->
            val importance = when (task) {
                is Task.OneTimeTask -> task.task.importance
                is Task.RecurringTask -> task.task.importance
            }

            val deadline = when (task) {
                is Task.OneTimeTask -> task.task.deadline
                is Task.RecurringTask -> task.task.nextTriggerTime
            }

            // 计算任务点的坐标（向左偏移）
            val x = padding - offsetX + ((importance / 10f)) * (width - 2 * padding)
            val y = padding + (1 - calculateUrgency(deadline) * calculateUrgency(deadline)) * (height - 2 * padding)

            // 检查点击位置是否在任务点的范围内（增大点击范围）
            if (abs(touchX - x) < 100f && abs(touchY - y) < 100f) {
                // 显示任务详细信息
                showTaskDetails(task)
                return
            }
        }
    }

    /**
     * 计算紧急程度
     */
    private fun calculateUrgency(deadline: Long): Float {
        val currentTime = System.currentTimeMillis()
        val timeDiff = deadline - currentTime

        val oneHour = 60 * 60 * 1000L // 1小时
        val oneDay = 24 * oneHour // 1天
        val oneMonth = 30 * oneDay // 1个月

        return when {
            timeDiff <= oneHour -> 1f // 最紧急
            timeDiff <= oneDay -> 0.5f // 中等紧急
            timeDiff <= oneMonth -> 0f // 不紧急
            else -> 0f // 超过一个月也视为不紧急
        }
    }

    /**
     * 显示任务详细信息
     */
    private fun showTaskDetails(task: Task) {
        val title = when (task) {
            is Task.OneTimeTask -> task.task.title
            is Task.RecurringTask -> task.task.title
        }
        val description = when (task) {
            is Task.OneTimeTask -> task.task.description
            is Task.RecurringTask -> task.task.description
        }
        val deadline = when (task) {
            is Task.OneTimeTask -> "截止时间: ${DateUtils.formatDate(task.task.deadline)}"
            is Task.RecurringTask -> "下一次触发时间: ${DateUtils.formatDate(task.task.nextTriggerTime)}"
        }

        // 弹出对话框显示任务详细信息
        AlertDialog.Builder(context)
            .setTitle(title)
            .setMessage("$description\n$deadline")
            .setPositiveButton("关闭", null)
            .show()
    }
}