package com.gdet.testapp.mvi.simple

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gdet.testapp.databinding.ActivityCounterBinding
import kotlinx.coroutines.launch

/**
 * MVI架构中的View层 - CounterActivity（传统View版本）
 *
 * View层在MVI中的职责：
 * 1. 观察State的变化并更新UI
 * 2. 将用户交互转换为Intent发送给ViewModel
 * 3. 保持UI逻辑简单，避免业务逻辑
 *
 * MVI的核心原则：
 * - 单向数据流：Intent -> Action -> State -> View
 * - 状态驱动UI：UI完全由State决定
 * - 不可变状态：State是不可变的，每次变化都产生新的State
 */
class CounterActivity : AppCompatActivity() {

    // 使用ViewBinding简化UI操作
    private lateinit var binding: ActivityCounterBinding

    // 使用by viewModels()委托创建ViewModel
    private val viewModel: CounterViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 初始化ViewBinding
        binding = ActivityCounterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置UI事件监听器
        setupClickListeners()

        // 观察State变化
        observeState()
    }

    /**
     * 设置UI事件监听器
     * 将用户交互转换为Intent
     */
    private fun setupClickListeners() {
        // 增加按钮点击事件
        binding.btnIncrement.setOnClickListener {
            // 发送增加Intent给ViewModel
            viewModel.handleIntent(CounterIntent.Increment)
        }

        // 减少按钮点击事件
        binding.btnDecrement.setOnClickListener {
            // 发送减少Intent给ViewModel
            viewModel.handleIntent(CounterIntent.Decrement)
        }

        // 重置按钮点击事件
        binding.btnReset.setOnClickListener {
            // 发送重置Intent给ViewModel
            viewModel.handleIntent(CounterIntent.Reset)
        }

        // 设置值按钮点击事件
        binding.btnSetValue.setOnClickListener {
            // 显示输入框和应用按钮
            binding.etCustomValue.visibility = View.VISIBLE
            binding.btnApplyValue.visibility = View.VISIBLE
            binding.etCustomValue.requestFocus()
        }

        // 应用自定义值按钮点击事件
        binding.btnApplyValue.setOnClickListener {
            val inputText = binding.etCustomValue.text.toString()
            if (inputText.isNotEmpty()) {
                try {
                    val value = inputText.toInt()
                    // 发送设置值Intent给ViewModel
                    viewModel.handleIntent(CounterIntent.SetValue(value))

                    // 隐藏输入框
                    binding.etCustomValue.visibility = View.GONE
                    binding.btnApplyValue.visibility = View.GONE
                    binding.etCustomValue.text.clear()
                } catch (e: NumberFormatException) {
                    // 输入格式错误时显示提示
                    Toast.makeText(this, "请输入有效的数字", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /**
     * 观察State变化并更新UI
     * 这是MVI架构的核心：UI完全由State驱动
     */
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                // 根据State更新UI
                renderState(state)
            }
        }
    }

    /**
     * 根据State渲染UI
     * 这个方法包含了所有的UI更新逻辑
     *
     * @param state 当前状态
     */
    private fun renderState(state: CounterState) {
        // 更新计数显示
        binding.tvCounterValue.text = state.getDisplayText()

        // 更新状态信息
        if (state.hasError()) {
            binding.tvStatus.text = state.errorMessage
            binding.tvStatus.visibility = View.VISIBLE
            binding.tvStatus.setTextColor(getColor(android.R.color.holo_red_dark))
        } else if (state.isLoading) {
            binding.tvStatus.text = "处理中..."
            binding.tvStatus.visibility = View.VISIBLE
            binding.tvStatus.setTextColor(getColor(android.R.color.darker_gray))
        } else {
            binding.tvStatus.visibility = View.GONE
        }

        // 更新按钮状态
        binding.btnIncrement.isEnabled = state.canIncrement() && !state.isLoading
        binding.btnDecrement.isEnabled = state.canDecrement() && !state.isLoading
        binding.btnReset.isEnabled = state.isOperationEnabled && !state.isLoading
        binding.btnSetValue.isEnabled = state.isOperationEnabled && !state.isLoading

        // 更新历史记录
        updateHistoryDisplay(state.history)

        // 记录状态渲染（用于调试）
        logStateRender(state)
    }

    /**
     * 更新历史记录显示
     *
     * @param history 操作历史列表
     */
    private fun updateHistoryDisplay(history: List<String>) {
        if (history.isEmpty()) {
            binding.tvHistory.text = "暂无操作历史"
        } else {
            // 显示最近的10条记录
            val recentHistory = history.takeLast(10)
            binding.tvHistory.text = recentHistory.joinToString("\n")

            // 自动滚动到底部
            binding.tvHistory.post {
                val scrollView = binding.tvHistory.parent as? android.widget.ScrollView
                scrollView?.fullScroll(View.FOCUS_DOWN)
            }
        }
    }

    /**
     * 记录状态渲染（用于调试）
     */
    private fun logStateRender(state: CounterState) {
        println("UI Render:")
        println("  Count: ${state.count}")
        println("  Loading: ${state.isLoading}")
        println("  Error: ${state.errorMessage}")
        println("  Can Increment: ${state.canIncrement()}")
        println("  Can Decrement: ${state.canDecrement()}")
    }
}
