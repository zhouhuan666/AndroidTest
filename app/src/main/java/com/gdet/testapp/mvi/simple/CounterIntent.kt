package com.gdet.testapp.mvi.simple

/**
 * MVI架构中的Intent（意图）
 * 
 * Intent代表用户的操作意图，是用户与UI交互产生的事件
 * 在MVI中，所有的用户操作都会被转换为Intent对象
 * 
 * 特点：
 * 1. 不可变（immutable）- 使用sealed class确保类型安全
 * 2. 表达用户意图 - 每个Intent都代表一个具体的用户操作
 * 3. 单向数据流的起点 - Intent -> Action -> State -> View
 */
sealed class CounterIntent {
    
    /**
     * 增加计数器的意图
     * 当用户点击"+"按钮时触发
     */
    object Increment : CounterIntent()
    
    /**
     * 减少计数器的意图
     * 当用户点击"-"按钮时触发
     */
    object Decrement : CounterIntent()
    
    /**
     * 重置计数器的意图
     * 当用户点击"重置"按钮时触发
     */
    object Reset : CounterIntent()
    
    /**
     * 设置特定值的意图
     * 当用户输入特定数值时触发
     * @param value 要设置的数值
     */
    data class SetValue(val value: Int) : CounterIntent()
}
