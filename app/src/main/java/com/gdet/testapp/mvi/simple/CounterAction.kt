package com.gdet.testapp.mvi.simple

/**
 * MVI架构中的Action（动作）
 * 
 * Action是Intent经过处理后产生的具体动作
 * 它描述了应该执行什么操作来改变应用状态
 * 
 * Intent vs Action的区别：
 * - Intent：用户的意图（"我想要增加计数"）
 * - Action：具体的动作（"将计数增加1"）
 * 
 * 在简单场景中，Intent和Action可能一一对应
 * 在复杂场景中，一个Intent可能产生多个Action（如网络请求、缓存更新等）
 */
sealed class CounterAction {
    
    /**
     * 增加计数的动作
     * 对应CounterIntent.Increment
     */
    object IncrementCounter : CounterAction()
    
    /**
     * 减少计数的动作
     * 对应CounterIntent.Decrement
     */
    object DecrementCounter : CounterAction()
    
    /**
     * 重置计数的动作
     * 对应CounterIntent.Reset
     */
    object ResetCounter : CounterAction()
    
    /**
     * 设置特定值的动作
     * 对应CounterIntent.SetValue
     * @param value 要设置的数值
     */
    data class SetCounterValue(val value: Int) : CounterAction()
}
