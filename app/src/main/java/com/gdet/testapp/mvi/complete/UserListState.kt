package com.gdet.testapp.mvi.complete

import com.gdet.testapp.mvi.complete.data.User
import com.gdet.testapp.mvi.complete.data.UserFilter
import com.gdet.testapp.mvi.complete.data.UserSortType

/**
 * 用户列表的State（状态）
 * 
 * State代表用户列表界面在某个时刻的完整状态
 * 它包含了UI渲染所需的所有信息
 * 
 * 复杂State的设计原则：
 * 1. 完整性：包含UI所需的所有数据
 * 2. 不可变性：使用data class确保状态不可变
 * 3. 可组合性：将相关状态组合在一起
 * 4. 可测试性：纯数据对象，易于测试
 * 5. 性能考虑：避免不必要的状态变化
 */
data class UserListState(
    // ========== 数据相关状态 ==========
    
    /**
     * 用户列表数据
     */
    val users: List<User> = emptyList(),
    
    /**
     * 搜索结果列表
     * 当用户进行搜索时，显示搜索结果而不是完整列表
     */
    val searchResults: List<User> = emptyList(),
    
    /**
     * 当前显示的用户列表
     * 根据搜索状态和过滤条件计算得出
     */
    val displayUsers: List<User> = emptyList(),
    
    /**
     * 选中的单个用户（单选模式）
     */
    val selectedUser: User? = null,
    
    /**
     * 选中的多个用户（多选模式）
     */
    val selectedUsers: Set<Long> = emptySet(),
    
    /**
     * 用户详情（用于详情页面）
     */
    val userDetail: User? = null,
    
    // ========== 加载状态 ==========
    
    /**
     * 是否正在初始加载
     */
    val isLoading: Boolean = false,
    
    /**
     * 是否正在刷新
     */
    val isRefreshing: Boolean = false,
    
    /**
     * 是否正在加载更多
     */
    val isLoadingMore: Boolean = false,
    
    /**
     * 是否正在搜索
     */
    val isSearching: Boolean = false,
    
    /**
     * 是否还有更多数据可加载
     */
    val hasMoreData: Boolean = true,
    
    /**
     * 当前页码
     */
    val currentPage: Int = 1,
    
    // ========== 搜索和过滤状态 ==========
    
    /**
     * 当前搜索关键词
     */
    val searchQuery: String = "",
    
    /**
     * 是否处于搜索模式
     */
    val isSearchMode: Boolean = false,
    
    /**
     * 当前过滤条件
     */
    val filter: UserFilter = UserFilter(),
    
    /**
     * 当前排序方式
     */
    val sortType: UserSortType = UserSortType.NAME,
    
    /**
     * 是否升序排列
     */
    val sortAscending: Boolean = true,
    
    // ========== UI状态 ==========
    
    /**
     * 是否为网格显示模式
     */
    val isGridMode: Boolean = false,
    
    /**
     * 是否启用多选模式
     */
    val isMultiSelectMode: Boolean = false,
    
    /**
     * 是否显示用户详情
     */
    val showUserDetail: Boolean = false,
    
    // ========== 操作状态 ==========
    
    /**
     * 正在执行的操作类型
     */
    val operationType: OperationType = OperationType.NONE,
    
    /**
     * 正在操作的用户ID列表
     */
    val operatingUserIds: Set<Long> = emptySet(),
    
    // ========== 错误和消息状态 ==========
    
    /**
     * 错误消息
     */
    val errorMessage: String? = null,
    
    /**
     * 成功消息
     */
    val successMessage: String? = null,
    
    /**
     * 是否显示错误对话框
     */
    val showErrorDialog: Boolean = false,
    
    /**
     * 是否显示成功提示
     */
    val showSuccessMessage: Boolean = false
) {
    
    // ========== 计算属性 ==========
    
    /**
     * 是否有任何加载状态
     */
    fun isAnyLoading(): Boolean {
        return isLoading || isRefreshing || isLoadingMore || isSearching
    }
    
    /**
     * 是否有错误
     */
    fun hasError(): Boolean = errorMessage != null
    
    /**
     * 是否有成功消息
     */
    fun hasSuccessMessage(): Boolean = successMessage != null
    
    /**
     * 是否为空状态
     */
    fun isEmpty(): Boolean {
        return displayUsers.isEmpty() && !isAnyLoading()
    }
    
    /**
     * 是否有选中的用户
     */
    fun hasSelectedUsers(): Boolean {
        return selectedUsers.isNotEmpty()
    }
    
    /**
     * 获取选中用户数量
     */
    fun getSelectedUserCount(): Int = selectedUsers.size
    
    /**
     * 是否全选
     */
    fun isAllSelected(): Boolean {
        return displayUsers.isNotEmpty() && 
               selectedUsers.size == displayUsers.size &&
               displayUsers.all { user -> selectedUsers.contains(user.id) }
    }
    
    /**
     * 是否有活动的过滤条件
     */
    fun hasActiveFilters(): Boolean = filter.hasActiveFilters()
    
    /**
     * 获取状态描述文本
     */
    fun getStatusText(): String {
        return when {
            isLoading -> "正在加载..."
            isRefreshing -> "正在刷新..."
            isSearching -> "正在搜索..."
            hasError() -> "加载失败"
            isEmpty() && isSearchMode -> "未找到相关用户"
            isEmpty() -> "暂无用户数据"
            isSearchMode -> "搜索结果：${displayUsers.size} 个用户"
            hasActiveFilters() -> "过滤结果：${displayUsers.size} 个用户"
            else -> "共 ${displayUsers.size} 个用户"
        }
    }
    
    /**
     * 获取多选状态文本
     */
    fun getMultiSelectStatusText(): String {
        return if (isMultiSelectMode) {
            "已选择 ${getSelectedUserCount()} 个用户"
        } else {
            ""
        }
    }
    
    /**
     * 检查用户是否被选中
     */
    fun isUserSelected(userId: Long): Boolean {
        return selectedUsers.contains(userId)
    }
    
    /**
     * 检查是否可以执行操作
     */
    fun canPerformOperation(): Boolean {
        return operationType == OperationType.NONE && !isAnyLoading()
    }
    
    /**
     * 检查是否可以加载更多
     */
    fun canLoadMore(): Boolean {
        return hasMoreData && !isAnyLoading() && !isSearchMode
    }
}

/**
 * 操作类型枚举
 */
enum class OperationType {
    /**
     * 无操作
     */
    NONE,
    
    /**
     * 创建用户
     */
    CREATING,
    
    /**
     * 更新用户
     */
    UPDATING,
    
    /**
     * 删除用户
     */
    DELETING,
    
    /**
     * 批量删除
     */
    BATCH_DELETING
}
