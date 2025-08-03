package com.gdet.testapp.mvi.simple

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

/**
 * MVI架构中的ViewModel
 * 
 * ViewModel在MVI中的职责：
 * 1. 接收来自UI的Intent
 * 2. 将Intent转换为Action
 * 3. 使用Reducer处理Action并更新State
 * 4. 向UI暴露State
 * 
 * 使用Kotlin Coroutines和Flow来实现响应式编程
 */
class CounterViewModel : ViewModel() {
    
    // 私有的可变状态流，只有ViewModel内部可以修改
    private val _state = MutableStateFlow(CounterState())
    
    // 公开的只读状态流，供UI观察
    val state: StateFlow<CounterState> = _state.asStateFlow()
    
    // Intent通道，用于接收来自UI的Intent
    private val intentChannel = Channel<CounterIntent>(Channel.UNLIMITED)
    
    init {
        // 在ViewModel初始化时开始处理Intent
        handleIntents()
    }
    
    /**
     * 接收来自UI的Intent
     * 这是UI与ViewModel交互的唯一入口
     * 
     * @param intent 用户意图
     */
    fun handleIntent(intent: CounterIntent) {
        viewModelScope.launch {
            intentChannel.send(intent)
        }
    }
    
    /**
     * 处理Intent的核心逻辑
     * 将Intent转换为Action，并使用Reducer更新状态
     */
    private fun handleIntents() {
        viewModelScope.launch {
            intentChannel.consumeAsFlow().collect { intent ->
                // 将Intent转换为Action
                val action = mapIntentToAction(intent)
                
                // 使用Reducer计算新状态
                val currentState = _state.value
                val newState = CounterReducer.reduce(currentState, action)
                
                // 更新状态
                _state.value = newState
                
                // 记录状态变化（用于调试）
                logStateChange(intent, currentState, newState)
            }
        }
    }
    
    /**
     * 将Intent映射为Action
     * 在简单场景中是一对一映射
     * 在复杂场景中可能需要更复杂的逻辑
     * 
     * @param intent 用户意图
     * @return 对应的动作
     */
    private fun mapIntentToAction(intent: CounterIntent): CounterAction {
        return when (intent) {
            is CounterIntent.Increment -> CounterAction.IncrementCounter
            is CounterIntent.Decrement -> CounterAction.DecrementCounter
            is CounterIntent.Reset -> CounterAction.ResetCounter
            is CounterIntent.SetValue -> CounterAction.SetCounterValue(intent.value)
        }
    }
    
    /**
     * 记录状态变化（用于调试和日志）
     * 在生产环境中可以移除或使用专门的日志库
     */
    private fun logStateChange(
        intent: CounterIntent,
        oldState: CounterState,
        newState: CounterState
    ) {
        println("MVI State Change:")
        println("  Intent: ${intent::class.simpleName}")
        println("  Old State: count=${oldState.count}, error=${oldState.errorMessage}")
        println("  New State: count=${newState.count}, error=${newState.errorMessage}")
        println("  History size: ${newState.history.size}")
    }
    
    /**
     * 获取当前状态的便利方法
     * 主要用于测试
     */
    fun getCurrentState(): CounterState = _state.value
    
    /**
     * 清理资源
     */
    override fun onCleared() {
        super.onCleared()
        intentChannel.close()
    }
}
