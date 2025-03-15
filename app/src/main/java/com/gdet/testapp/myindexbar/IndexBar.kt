package com.gdet.testapp.myindexbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View


/**
 *
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2025-03-15
 * 描述：
 *
 */
class IndexBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val indexList = listOf(
        "A", "B", "C", "D", "E", "F", "G", "H", "I", "J", "K", "L", "M",
        "N", "O", "P", "Q", "R", "S", "T", "U", "V", "W", "X", "Y", "Z", "#"
    )

    private val disabledIndexSet = mutableSetOf<String>()
    private var currentIndex = -1

    private val textPaint = Paint().apply {
        isAntiAlias = true
        textSize = 24f
        color = Color.GRAY
    }

    private val highlightTextPaint = Paint().apply {
        isAntiAlias = true
        textSize = 24f
        color = Color.BLUE
        isFakeBoldText = true
    }

    // 索引选中回调
    var onIndexSelectedListener: ((String) -> Unit)? = null

    // 索引触摸事件回调（用于显示放大镜）
    var onIndexTouchedListener: ((String) -> Unit)? = null

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val height = height
        val itemHeight = height / indexList.size

        // 绘制索引字母
        for (i in indexList.indices) {
            val index = indexList[i]
            val isDisabled = disabledIndexSet.contains(index)
            val isHighlighted = i == currentIndex

            val paint = when {
                isDisabled -> textPaint.apply { alpha = 80 }
                isHighlighted -> highlightTextPaint
                else -> textPaint.apply { alpha = 255 }
            }

            val x = width / 2f
            val y = itemHeight * i + itemHeight / 2f + paint.textSize / 3
            canvas.drawText(index, x, y, paint)
        }
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val y = event.y
                val itemHeight = height / indexList.size
                var newIndex = (y / itemHeight).toInt()

                // 确保索引在有效范围内
                newIndex = newIndex.coerceIn(0, indexList.size - 1)

                // 如果是禁用的索引，找到下一个可用的索引
                if (disabledIndexSet.contains(indexList[newIndex])) {
                    // 尝试找到下一个可用索引
                    var foundIndex = -1
                    for (i in indexList.indices) {
                        if (!disabledIndexSet.contains(indexList[i])) {
                            foundIndex = i
                            if (i > newIndex) break
                        }
                    }
                    if (foundIndex != -1) newIndex = foundIndex
                    else return true // 如果没有可用索引，不处理
                }

                if (newIndex != currentIndex) {
                    currentIndex = newIndex

                    // 触发索引选中回调
                    onIndexSelectedListener?.invoke(indexList[currentIndex])

                    // 触发索引触摸回调（用于显示放大镜）
                    onIndexTouchedListener?.invoke(indexList[currentIndex])

                    invalidate()
                }
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setDisabledIndexes(indexes: Set<String>) {
        disabledIndexSet.clear()
        disabledIndexSet.addAll(indexes)
        invalidate()
    }

    fun setCurrentIndex(index: String) {
        val newIndex = indexList.indexOf(index)
        if (newIndex != -1 && newIndex != currentIndex) {
            currentIndex = newIndex
            invalidate()
        }
    }

    fun getCurrentIndex(): String? {
        return if (currentIndex >= 0 && currentIndex < indexList.size) {
            indexList[currentIndex]
        } else null
    }
}