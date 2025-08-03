package com.gdet.testapp.mvi.simple

/**
 * MVI架构中的State（状态）
 * 
 * State代表应用在某个时刻的完整状态
 * 它是不可变的数据类，包含了UI渲染所需的所有信息
 * 
 * State的特点：
 * 1. 不可变性 - 使用data class确保状态不可变
 * 2. 完整性 - 包含UI渲染所需的所有数据
 * 3. 可预测性 - 相同的State总是产生相同的UI
 * 4. 可测试性 - 纯数据对象，易于测试
 */
data class CounterState(
    /**
     * 当前计数值
     * 这是UI需要显示的主要数据
     */
    val count: Int = 0,
    
    /**
     * 是否正在加载
     * 在复杂场景中可能需要显示加载状态
     * 这里作为示例展示如何在State中包含UI状态
     */
    val isLoading: Boolean = false,
    
    /**
     * 错误信息
     * 当操作失败时显示给用户的错误信息
     * null表示没有错误
     */
    val errorMessage: String? = null,
    
    /**
     * 是否可以执行操作
     * 例如：在某些条件下可能需要禁用按钮
     */
    val isOperationEnabled: Boolean = true,
    
    /**
     * 操作历史记录
     * 展示如何在State中包含更复杂的数据结构
     */
    val history: List<String> = emptyList()
) {
    
    /**
     * 便利方法：检查是否有错误
     */
    fun hasError(): Boolean = errorMessage != null
    
    /**
     * 便利方法：获取显示文本
     * 在State中包含计算属性可以简化UI逻辑
     */
    fun getDisplayText(): String {
        return when {
            isLoading -> "计算中..."
            hasError() -> "错误: $errorMessage"
            else -> count.toString()
        }
    }
    
    /**
     * 便利方法：检查是否可以增加
     * 业务逻辑：计数不能超过100
     */
    fun canIncrement(): Boolean = isOperationEnabled && count < 100
    
    /**
     * 便利方法：检查是否可以减少
     * 业务逻辑：计数不能小于-100
     */
    fun canDecrement(): Boolean = isOperationEnabled && count > -100
}
