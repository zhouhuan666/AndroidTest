package com.gdet.testapp.mvi.simple

/**
 * MVI架构中的Reducer（状态缩减器）
 * 
 * Reducer是一个纯函数，负责根据当前状态和动作计算新的状态
 * 它是MVI架构中状态管理的核心组件
 * 
 * Reducer的特点：
 * 1. 纯函数 - 相同的输入总是产生相同的输出，无副作用
 * 2. 不可变 - 不修改原状态，总是返回新的状态对象
 * 3. 可预测 - 状态变化逻辑集中在一个地方，易于理解和测试
 * 4. 可组合 - 可以将多个小的reducer组合成大的reducer
 */
object CounterReducer {
    
    /**
     * 根据当前状态和动作计算新状态
     * 
     * @param currentState 当前状态
     * @param action 要执行的动作
     * @return 新的状态
     */
    fun reduce(currentState: CounterState, action: CounterAction): CounterState {
        return when (action) {
            is CounterAction.IncrementCounter -> {
                // 增加计数逻辑
                if (currentState.canIncrement()) {
                    val newCount = currentState.count + 1
                    val newHistory = currentState.history + "增加到 $newCount"
                    currentState.copy(
                        count = newCount,
                        history = newHistory,
                        errorMessage = null // 清除之前的错误
                    )
                } else {
                    // 不能增加时显示错误
                    currentState.copy(
                        errorMessage = "计数不能超过100"
                    )
                }
            }
            
            is CounterAction.DecrementCounter -> {
                // 减少计数逻辑
                if (currentState.canDecrement()) {
                    val newCount = currentState.count - 1
                    val newHistory = currentState.history + "减少到 $newCount"
                    currentState.copy(
                        count = newCount,
                        history = newHistory,
                        errorMessage = null // 清除之前的错误
                    )
                } else {
                    // 不能减少时显示错误
                    currentState.copy(
                        errorMessage = "计数不能小于-100"
                    )
                }
            }
            
            is CounterAction.ResetCounter -> {
                // 重置计数逻辑
                val newHistory = currentState.history + "重置为 0"
                currentState.copy(
                    count = 0,
                    history = newHistory,
                    errorMessage = null,
                    isLoading = false
                )
            }
            
            is CounterAction.SetCounterValue -> {
                // 设置特定值逻辑
                val newValue = action.value
                when {
                    newValue > 100 -> {
                        currentState.copy(
                            errorMessage = "设置值不能超过100"
                        )
                    }
                    newValue < -100 -> {
                        currentState.copy(
                            errorMessage = "设置值不能小于-100"
                        )
                    }
                    else -> {
                        val newHistory = currentState.history + "设置为 $newValue"
                        currentState.copy(
                            count = newValue,
                            history = newHistory,
                            errorMessage = null
                        )
                    }
                }
            }
        }
    }
    
    /**
     * 处理加载状态的reducer
     * 展示如何处理异步操作的状态变化
     */
    fun reduceLoading(currentState: CounterState, isLoading: Boolean): CounterState {
        return currentState.copy(isLoading = isLoading)
    }
    
    /**
     * 处理错误状态的reducer
     * 展示如何统一处理错误状态
     */
    fun reduceError(currentState: CounterState, errorMessage: String?): CounterState {
        return currentState.copy(
            errorMessage = errorMessage,
            isLoading = false
        )
    }
}
