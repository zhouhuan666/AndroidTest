package com.gdet.testapp.myindexbar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.widget.FrameLayout
import androidx.core.view.doOnLayout

/**
 *
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2025-03-15
 * 描述：
 *
 */
class ContactIndexLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : FrameLayout(context, attrs, defStyleAttr) {

    private var indexBar: IndexBar? = null
    private var recyclerViewLeft = 0

    private val magnifierPaint = Paint().apply {
        isAntiAlias = true
        color = Color.BLUE
        alpha = 180
    }

    private val magnifierTextPaint = Paint().apply {
        isAntiAlias = true
        textSize = 60f
        color = Color.WHITE
        textAlign = Paint.Align.CENTER
    }

    private var showMagnifier = false
    private var currentIndex: String? = null
    private var magnifierHideRunnable: Runnable? = null

    override fun onFinishInflate() {
        super.onFinishInflate()

        // 查找子视图中的IndexBar
        for (i in 0 until childCount) {
            val child = getChildAt(i)
            if (child is IndexBar) {
                indexBar = child
                break
            }
        }

        // 设置索引选中回调
        indexBar?.onIndexSelectedListener = { index ->
            // 只处理列表滚动，不显示放大镜
        }

        // 设置索引触摸回调（用于显示放大镜）
        indexBar?.onIndexTouchedListener = { index ->
            currentIndex = index
            showMagnifier = true

            // 取消之前的隐藏任务
            magnifierHideRunnable?.let { removeCallbacks(it) }

            // 设置2秒后隐藏放大镜
            magnifierHideRunnable = Runnable {
                showMagnifier = false
                invalidate()
            }
            postDelayed(magnifierHideRunnable, 2000)

            invalidate() // 触发重绘
        }

        // 获取RecyclerView的左边界位置
        doOnLayout {
            for (i in 0 until childCount) {
                val child = getChildAt(i)
                if (child !is IndexBar && child.id != View.NO_ID) {
                    recyclerViewLeft = child.left
                    break
                }
            }
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)

        // 绘制放大镜效果
        if (showMagnifier && currentIndex != null) {
            // 放大镜位置：列表左侧对齐，垂直居中
            val centerX = recyclerViewLeft + 80f // 列表左侧偏移一点
            val centerY = height / 2f // 垂直居中
            val radius = 80f

            // 绘制放大镜背景
            canvas.drawCircle(centerX, centerY, radius, magnifierPaint)

            // 绘制放大镜中的字母
            canvas.drawText(currentIndex!!, centerX, centerY + magnifierTextPaint.textSize / 3, magnifierTextPaint)
        }
    }
}