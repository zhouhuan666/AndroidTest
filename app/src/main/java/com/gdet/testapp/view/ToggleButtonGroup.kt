package com.gdet.testapp.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.gdet.testapp.R

/**
 *
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2025-01-19
 * 描述：
 *
 */
class ToggleButtonGroup @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    interface OnButtonCheckedListener {
        fun onButtonChecked(position: Int, text: String)
    }

    private var buttons = mutableListOf<TextView>()
    private var currentCheckedPosition = -1
    private var listener: OnButtonCheckedListener? = null

    init {
        orientation = HORIZONTAL
        gravity = Gravity.CENTER
        background = ContextCompat.getDrawable(context, R.drawable.bg_toggle_group)
    }

    fun setButtons(buttonDataList: List<String>) {
        removeAllViews()
        buttons.clear()

        buttonDataList.forEachIndexed { index, text ->
            val button = createButton(text, index)
            buttons.add(button)
            addView(button)
        }
    }

    fun setCheckedPosition(position: Int) {
        if (position in 0 until buttons.size) {
            updateButtonState(position)
        }
    }

    fun getCheckedPosition(): Int = currentCheckedPosition

    fun setOnButtonCheckedListener(listener: OnButtonCheckedListener) {
        this.listener = listener
    }

    private fun createButton(text: String, position: Int): TextView {
        return TextView(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT).apply {
                weight = 1f
            }
            gravity = Gravity.CENTER
            this.text = text
            textSize = 14f
            setPadding(
                resources.getDimensionPixelSize(R.dimen.toggle_button_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.toggle_button_padding_vertical),
                resources.getDimensionPixelSize(R.dimen.toggle_button_padding_horizontal),
                resources.getDimensionPixelSize(R.dimen.toggle_button_padding_vertical)
            )
            setTextColor(ContextCompat.getColorStateList(context, R.color.toggle_text_color))
            background = ContextCompat.getDrawable(context, R.drawable.bg_toggle_button)

            setOnClickListener {
                updateButtonState(position)
                listener?.onButtonChecked(position, text)
            }
        }
    }

    private fun updateButtonState(position: Int) {
        currentCheckedPosition = position
        buttons.forEachIndexed { index, button ->
            button.isSelected = index == position
        }
    }
}