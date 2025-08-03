package com.gdet.testapp.mvi.complete

import com.gdet.testapp.mvi.complete.data.User
import com.gdet.testapp.mvi.complete.data.UserFilter
import com.gdet.testapp.mvi.complete.data.UserSortType

/**
 * 用户列表的Intent（用户意图）
 * 
 * Intent代表用户在用户列表界面的所有可能操作
 * 每个Intent都对应一个具体的用户交互行为
 * 
 * 在复杂的MVI应用中，Intent通常包含：
 * 1. 数据加载意图（初始加载、刷新、分页）
 * 2. 用户交互意图（点击、搜索、过滤）
 * 3. 状态变更意图（选择、编辑、删除）
 */
sealed class UserListIntent {
    
    // ========== 数据加载相关Intent ==========
    
    /**
     * 初始化加载用户列表
     * 应用启动或界面首次显示时触发
     */
    object LoadInitialUsers : UserListIntent()
    
    /**
     * 刷新用户列表
     * 用户下拉刷新时触发
     */
    object RefreshUsers : UserListIntent()
    
    /**
     * 加载更多用户（分页加载）
     * 用户滚动到列表底部时触发
     */
    object LoadMoreUsers : UserListIntent()
    
    /**
     * 重试加载
     * 当加载失败时，用户点击重试按钮触发
     */
    object RetryLoad : UserListIntent()
    
    // ========== 搜索和过滤相关Intent ==========
    
    /**
     * 搜索用户
     * 用户在搜索框输入关键词时触发
     * 
     * @param query 搜索关键词
     */
    data class SearchUsers(val query: String) : UserListIntent()
    
    /**
     * 清除搜索
     * 用户清空搜索框或点击清除按钮时触发
     */
    object ClearSearch : UserListIntent()
    
    /**
     * 应用过滤条件
     * 用户设置过滤条件时触发
     * 
     * @param filter 过滤条件
     */
    data class ApplyFilter(val filter: UserFilter) : UserListIntent()
    
    /**
     * 清除所有过滤条件
     * 用户点击清除过滤按钮时触发
     */
    object ClearFilters : UserListIntent()
    
    /**
     * 切换排序方式
     * 用户点击排序按钮时触发
     * 
     * @param sortType 排序类型
     * @param ascending 是否升序
     */
    data class ChangeSorting(
        val sortType: UserSortType,
        val ascending: Boolean = true
    ) : UserListIntent()
    
    // ========== 用户操作相关Intent ==========
    
    /**
     * 选择用户
     * 用户点击列表项时触发
     * 
     * @param user 被选择的用户
     */
    data class SelectUser(val user: User) : UserListIntent()
    
    /**
     * 取消选择用户
     * 用户取消选择时触发
     */
    object UnselectUser : UserListIntent()
    
    /**
     * 切换用户选择状态（多选模式）
     * 用户在多选模式下点击用户时触发
     * 
     * @param user 要切换选择状态的用户
     */
    data class ToggleUserSelection(val user: User) : UserListIntent()
    
    /**
     * 全选/取消全选
     * 用户点击全选按钮时触发
     * 
     * @param selectAll 是否全选
     */
    data class SelectAllUsers(val selectAll: Boolean) : UserListIntent()
    
    // ========== CRUD操作相关Intent ==========
    
    /**
     * 创建新用户
     * 用户点击添加按钮并填写信息后触发
     * 
     * @param user 新用户信息
     */
    data class CreateUser(val user: User) : UserListIntent()
    
    /**
     * 更新用户信息
     * 用户编辑用户信息并保存时触发
     * 
     * @param user 更新后的用户信息
     */
    data class UpdateUser(val user: User) : UserListIntent()
    
    /**
     * 删除单个用户
     * 用户点击删除按钮时触发
     * 
     * @param userId 要删除的用户ID
     */
    data class DeleteUser(val userId: Long) : UserListIntent()
    
    /**
     * 批量删除用户
     * 用户选择多个用户并点击批量删除时触发
     * 
     * @param userIds 要删除的用户ID列表
     */
    data class DeleteUsers(val userIds: List<Long>) : UserListIntent()
    
    // ========== UI状态相关Intent ==========
    
    /**
     * 切换列表显示模式
     * 用户切换网格/列表视图时触发
     * 
     * @param isGridMode 是否为网格模式
     */
    data class ToggleViewMode(val isGridMode: Boolean) : UserListIntent()
    
    /**
     * 切换多选模式
     * 用户长按列表项或点击多选按钮时触发
     * 
     * @param enabled 是否启用多选模式
     */
    data class ToggleMultiSelectMode(val enabled: Boolean) : UserListIntent()
    
    /**
     * 显示用户详情
     * 用户点击查看详情时触发
     * 
     * @param userId 用户ID
     */
    data class ShowUserDetails(val userId: Long) : UserListIntent()
    
    /**
     * 隐藏用户详情
     * 用户关闭详情页面时触发
     */
    object HideUserDetails : UserListIntent()
    
    /**
     * 显示错误信息
     * 系统内部错误时触发
     * 
     * @param error 错误信息
     */
    data class ShowError(val error: String) : UserListIntent()
    
    /**
     * 隐藏错误信息
     * 用户关闭错误提示时触发
     */
    object HideError : UserListIntent()
    
    /**
     * 清除缓存
     * 用户点击清除缓存按钮时触发
     */
    object ClearCache : UserListIntent()
}
