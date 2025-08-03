package com.gdet.testapp.mvi.complete

import com.gdet.testapp.mvi.complete.data.ApiResponse
import com.gdet.testapp.mvi.complete.data.User
import com.gdet.testapp.mvi.complete.data.UserFilter
import com.gdet.testapp.mvi.complete.data.UserSortType

/**
 * 用户列表的Action（动作）
 * 
 * Action是Intent经过处理后产生的具体动作
 * 它描述了应该执行什么操作来改变应用状态
 * 
 * Action与Intent的区别：
 * - Intent：用户的意图（"我想要搜索用户"）
 * - Action：具体的动作（"开始搜索"、"搜索成功"、"搜索失败"）
 * 
 * 在复杂的MVI应用中，一个Intent可能产生多个Action
 * 例如：LoadUsers Intent可能产生 StartLoading -> LoadSuccess/LoadError
 */
sealed class UserListAction {
    
    // ========== 加载状态相关Action ==========
    
    /**
     * 开始加载用户列表
     * 显示加载指示器
     */
    object StartLoading : UserListAction()
    
    /**
     * 开始刷新用户列表
     * 显示刷新指示器
     */
    object StartRefreshing : UserListAction()
    
    /**
     * 开始加载更多用户
     * 显示底部加载指示器
     */
    object StartLoadingMore : UserListAction()
    
    /**
     * 用户列表加载成功
     * 
     * @param users 用户列表
     * @param isRefresh 是否为刷新操作
     * @param hasMore 是否还有更多数据
     */
    data class LoadUsersSuccess(
        val users: List<User>,
        val isRefresh: Boolean = false,
        val hasMore: Boolean = true
    ) : UserListAction()
    
    /**
     * 用户列表加载失败
     * 
     * @param error 错误信息
     * @param isRefresh 是否为刷新操作
     */
    data class LoadUsersError(
        val error: String,
        val isRefresh: Boolean = false
    ) : UserListAction()
    
    /**
     * 停止加载
     * 隐藏所有加载指示器
     */
    object StopLoading : UserListAction()
    
    // ========== 搜索相关Action ==========
    
    /**
     * 开始搜索
     * 
     * @param query 搜索关键词
     */
    data class StartSearch(val query: String) : UserListAction()
    
    /**
     * 搜索成功
     * 
     * @param results 搜索结果
     * @param query 搜索关键词
     */
    data class SearchSuccess(
        val results: List<User>,
        val query: String
    ) : UserListAction()
    
    /**
     * 搜索失败
     * 
     * @param error 错误信息
     * @param query 搜索关键词
     */
    data class SearchError(
        val error: String,
        val query: String
    ) : UserListAction()
    
    /**
     * 清除搜索结果
     */
    object ClearSearchResults : UserListAction()
    
    // ========== 过滤和排序相关Action ==========
    
    /**
     * 应用过滤条件
     * 
     * @param filter 过滤条件
     */
    data class ApplyUserFilter(val filter: UserFilter) : UserListAction()
    
    /**
     * 清除过滤条件
     */
    object ClearUserFilter : UserListAction()
    
    /**
     * 应用排序
     * 
     * @param sortType 排序类型
     * @param ascending 是否升序
     */
    data class ApplySorting(
        val sortType: UserSortType,
        val ascending: Boolean
    ) : UserListAction()
    
    // ========== 用户选择相关Action ==========
    
    /**
     * 设置选中的用户
     * 
     * @param user 选中的用户
     */
    data class SetSelectedUser(val user: User?) : UserListAction()
    
    /**
     * 切换用户选择状态（多选）
     * 
     * @param user 用户
     * @param selected 是否选中
     */
    data class ToggleUserSelected(
        val user: User,
        val selected: Boolean
    ) : UserListAction()
    
    /**
     * 设置所有用户的选择状态
     * 
     * @param selected 是否选中
     */
    data class SetAllUsersSelected(val selected: Boolean) : UserListAction()
    
    /**
     * 清除所有选择
     */
    object ClearAllSelections : UserListAction()
    
    // ========== CRUD操作相关Action ==========
    
    /**
     * 开始创建用户
     */
    object StartCreatingUser : UserListAction()
    
    /**
     * 用户创建成功
     * 
     * @param user 新创建的用户
     */
    data class CreateUserSuccess(val user: User) : UserListAction()
    
    /**
     * 用户创建失败
     * 
     * @param error 错误信息
     */
    data class CreateUserError(val error: String) : UserListAction()
    
    /**
     * 开始更新用户
     * 
     * @param userId 用户ID
     */
    data class StartUpdatingUser(val userId: Long) : UserListAction()
    
    /**
     * 用户更新成功
     * 
     * @param user 更新后的用户
     */
    data class UpdateUserSuccess(val user: User) : UserListAction()
    
    /**
     * 用户更新失败
     * 
     * @param error 错误信息
     * @param userId 用户ID
     */
    data class UpdateUserError(
        val error: String,
        val userId: Long
    ) : UserListAction()
    
    /**
     * 开始删除用户
     * 
     * @param userIds 要删除的用户ID列表
     */
    data class StartDeletingUsers(val userIds: List<Long>) : UserListAction()
    
    /**
     * 用户删除成功
     * 
     * @param userIds 已删除的用户ID列表
     */
    data class DeleteUsersSuccess(val userIds: List<Long>) : UserListAction()
    
    /**
     * 用户删除失败
     * 
     * @param error 错误信息
     * @param userIds 删除失败的用户ID列表
     */
    data class DeleteUsersError(
        val error: String,
        val userIds: List<Long>
    ) : UserListAction()
    
    // ========== UI状态相关Action ==========
    
    /**
     * 设置视图模式
     * 
     * @param isGridMode 是否为网格模式
     */
    data class SetViewMode(val isGridMode: Boolean) : UserListAction()
    
    /**
     * 设置多选模式
     * 
     * @param enabled 是否启用多选模式
     */
    data class SetMultiSelectMode(val enabled: Boolean) : UserListAction()
    
    /**
     * 显示用户详情
     * 
     * @param user 要显示详情的用户
     */
    data class ShowUserDetail(val user: User) : UserListAction()
    
    /**
     * 隐藏用户详情
     */
    object HideUserDetail : UserListAction()
    
    /**
     * 显示错误消息
     * 
     * @param message 错误消息
     */
    data class ShowErrorMessage(val message: String) : UserListAction()
    
    /**
     * 隐藏错误消息
     */
    object HideErrorMessage : UserListAction()
    
    /**
     * 显示成功消息
     * 
     * @param message 成功消息
     */
    data class ShowSuccessMessage(val message: String) : UserListAction()
    
    /**
     * 隐藏成功消息
     */
    object HideSuccessMessage : UserListAction()
    
    /**
     * 清除缓存成功
     */
    object ClearCacheSuccess : UserListAction()
}
