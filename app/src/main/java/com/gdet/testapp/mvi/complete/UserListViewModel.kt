package com.gdet.testapp.mvi.complete

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdet.testapp.mvi.complete.data.ApiResponse
import com.gdet.testapp.mvi.complete.data.User
import com.gdet.testapp.mvi.complete.data.UserRepository
import com.gdet.testapp.mvi.complete.utils.MviLogger
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch

/**
 * 用户列表的ViewModel
 * 
 * ViewModel在MVI架构中的职责：
 * 1. 接收来自UI的Intent
 * 2. 将Intent转换为Action
 * 3. 协调Repository进行数据操作
 * 4. 使用Reducer处理Action并更新State
 * 5. 向UI暴露State
 * 
 * 复杂ViewModel的设计考虑：
 * - 职责分离：将复杂逻辑拆分到不同方法
 * - 错误处理：统一处理各种错误情况
 * - 生命周期管理：正确处理协程和资源清理
 * - 性能优化：避免不必要的状态更新
 */
class UserListViewModel(
    private val repository: UserRepository
) : ViewModel() {

    companion object {
        private const val TAG = "UserListViewModel"
    }
    
    // 私有的可变状态流
    private val _state = MutableStateFlow(UserListState())
    
    // 公开的只读状态流
    val state: StateFlow<UserListState> = _state.asStateFlow()
    
    // Intent通道
    private val intentChannel = Channel<UserListIntent>(Channel.UNLIMITED)
    
    init {
        MviLogger.logLifecycle(TAG, "ViewModel初始化")
        // 开始处理Intent
        handleIntents()
    }
    
    /**
     * 接收来自UI的Intent
     */
    fun handleIntent(intent: UserListIntent) {
        MviLogger.logIntent(TAG, intent)
        viewModelScope.launch {
            intentChannel.send(intent)
        }
    }
    
    /**
     * 处理Intent的核心逻辑
     */
    private fun handleIntents() {
        viewModelScope.launch {
            intentChannel.consumeAsFlow().collect { intent ->
                when (intent) {
                    // ========== 数据加载相关 ==========
                    is UserListIntent.LoadInitialUsers -> {
                        loadUsers(isRefresh = false, isInitial = true)
                    }
                    
                    is UserListIntent.RefreshUsers -> {
                        loadUsers(isRefresh = true)
                    }
                    
                    is UserListIntent.LoadMoreUsers -> {
                        loadMoreUsers()
                    }
                    
                    is UserListIntent.RetryLoad -> {
                        retryLoad()
                    }
                    
                    // ========== 搜索相关 ==========
                    is UserListIntent.SearchUsers -> {
                        searchUsers(intent.query)
                    }
                    
                    is UserListIntent.ClearSearch -> {
                        clearSearch()
                    }
                    
                    // ========== 过滤和排序相关 ==========
                    is UserListIntent.ApplyFilter -> {
                        applyFilter(intent.filter)
                    }
                    
                    is UserListIntent.ClearFilters -> {
                        clearFilters()
                    }
                    
                    is UserListIntent.ChangeSorting -> {
                        changeSorting(intent.sortType, intent.ascending)
                    }
                    
                    // ========== 用户选择相关 ==========
                    is UserListIntent.SelectUser -> {
                        selectUser(intent.user)
                    }
                    
                    is UserListIntent.UnselectUser -> {
                        unselectUser()
                    }
                    
                    is UserListIntent.ToggleUserSelection -> {
                        toggleUserSelection(intent.user)
                    }
                    
                    is UserListIntent.SelectAllUsers -> {
                        selectAllUsers(intent.selectAll)
                    }
                    
                    // ========== CRUD操作相关 ==========
                    is UserListIntent.CreateUser -> {
                        createUser(intent.user)
                    }
                    
                    is UserListIntent.UpdateUser -> {
                        updateUser(intent.user)
                    }
                    
                    is UserListIntent.DeleteUser -> {
                        deleteUser(intent.userId)
                    }
                    
                    is UserListIntent.DeleteUsers -> {
                        deleteUsers(intent.userIds)
                    }
                    
                    // ========== UI状态相关 ==========
                    is UserListIntent.ToggleViewMode -> {
                        toggleViewMode(intent.isGridMode)
                    }
                    
                    is UserListIntent.ToggleMultiSelectMode -> {
                        toggleMultiSelectMode(intent.enabled)
                    }
                    
                    is UserListIntent.ShowUserDetails -> {
                        showUserDetails(intent.userId)
                    }
                    
                    is UserListIntent.HideUserDetails -> {
                        hideUserDetails()
                    }
                    
                    is UserListIntent.ShowError -> {
                        showError(intent.error)
                    }
                    
                    is UserListIntent.HideError -> {
                        hideError()
                    }
                    
                    is UserListIntent.ClearCache -> {
                        clearCache()
                    }
                }
            }
        }
    }
    

    
    /**
     * 加载用户列表
     */
    private fun loadUsers(isRefresh: Boolean, isInitial: Boolean = false) {
        viewModelScope.launch {
            // 发送开始加载的Action
            val startAction = if (isRefresh) {
                UserListAction.StartRefreshing
            } else {
                UserListAction.StartLoading
            }
            updateState(startAction)
            
            // 调用Repository加载数据
            val page = if (isRefresh) 1 else _state.value.currentPage
            when (val response = repository.loadUsers(page = page, refresh = isRefresh)) {
                is ApiResponse.Success -> {
                    val action = UserListAction.LoadUsersSuccess(
                        users = response.data,
                        isRefresh = isRefresh,
                        hasMore = response.data.size >= 20 // 假设每页20条数据
                    )
                    updateState(action)
                }
                
                is ApiResponse.Error -> {
                    val action = UserListAction.LoadUsersError(
                        error = response.exception.message ?: "加载失败",
                        isRefresh = isRefresh
                    )
                    updateState(action)
                }
                
                is ApiResponse.Loading -> {
                    // 已经在上面处理了
                }
            }
        }
    }
    
    /**
     * 加载更多用户
     */
    private fun loadMoreUsers() {
        val currentState = _state.value
        if (!currentState.canLoadMore()) {
            return
        }
        
        viewModelScope.launch {
            updateState(UserListAction.StartLoadingMore)
            
            val nextPage = currentState.currentPage + 1
            when (val response = repository.loadUsers(page = nextPage)) {
                is ApiResponse.Success -> {
                    val action = UserListAction.LoadUsersSuccess(
                        users = response.data,
                        isRefresh = false,
                        hasMore = response.data.size >= 20
                    )
                    updateState(action)
                }
                
                is ApiResponse.Error -> {
                    val action = UserListAction.LoadUsersError(
                        error = response.exception.message ?: "加载更多失败",
                        isRefresh = false
                    )
                    updateState(action)
                }
                
                is ApiResponse.Loading -> {
                    // 已经处理
                }
            }
        }
    }
    
    /**
     * 重试加载
     */
    private fun retryLoad() {
        val currentState = _state.value
        if (currentState.users.isEmpty()) {
            loadUsers(isRefresh = false, isInitial = true)
        } else {
            loadMoreUsers()
        }
    }
    
    /**
     * 搜索用户
     */
    private fun searchUsers(query: String) {
        viewModelScope.launch {
            updateState(UserListAction.StartSearch(query))
            
            if (query.isBlank()) {
                updateState(UserListAction.ClearSearchResults)
                return@launch
            }
            
            when (val response = repository.searchUsers(query)) {
                is ApiResponse.Success -> {
                    val action = UserListAction.SearchSuccess(
                        results = response.data,
                        query = query
                    )
                    updateState(action)
                }
                
                is ApiResponse.Error -> {
                    val action = UserListAction.SearchError(
                        error = response.exception.message ?: "搜索失败",
                        query = query
                    )
                    updateState(action)
                }
                
                is ApiResponse.Loading -> {
                    // 已经处理
                }
            }
        }
    }
    
    /**
     * 清除搜索
     */
    private fun clearSearch() {
        updateState(UserListAction.ClearSearchResults)
    }
    
    /**
     * 应用过滤条件
     */
    private fun applyFilter(filter: com.gdet.testapp.mvi.complete.data.UserFilter) {
        updateState(UserListAction.ApplyUserFilter(filter))
    }
    
    /**
     * 清除过滤条件
     */
    private fun clearFilters() {
        updateState(UserListAction.ClearUserFilter)
    }
    
    /**
     * 改变排序方式
     */
    private fun changeSorting(sortType: com.gdet.testapp.mvi.complete.data.UserSortType, ascending: Boolean) {
        updateState(UserListAction.ApplySorting(sortType, ascending))
    }
    
    /**
     * 选择用户
     */
    private fun selectUser(user: User) {
        updateState(UserListAction.SetSelectedUser(user))
    }
    
    /**
     * 取消选择用户
     */
    private fun unselectUser() {
        updateState(UserListAction.SetSelectedUser(null))
    }
    
    /**
     * 切换用户选择状态
     */
    private fun toggleUserSelection(user: User) {
        val currentState = _state.value
        val isSelected = currentState.isUserSelected(user.id)
        updateState(UserListAction.ToggleUserSelected(user, !isSelected))
    }
    
    /**
     * 全选/取消全选
     */
    private fun selectAllUsers(selectAll: Boolean) {
        updateState(UserListAction.SetAllUsersSelected(selectAll))
    }

    /**
     * 创建用户
     */
    private fun createUser(user: User) {
        viewModelScope.launch {
            updateState(UserListAction.StartCreatingUser)

            when (val response = repository.createUser(user)) {
                is ApiResponse.Success -> {
                    updateState(UserListAction.CreateUserSuccess(response.data))
                }

                is ApiResponse.Error -> {
                    updateState(UserListAction.CreateUserError(
                        response.exception.message ?: "创建用户失败"
                    ))
                }

                is ApiResponse.Loading -> {
                    // 已经处理
                }
            }
        }
    }

    /**
     * 更新用户
     */
    private fun updateUser(user: User) {
        viewModelScope.launch {
            updateState(UserListAction.StartUpdatingUser(user.id))

            when (val response = repository.updateUser(user)) {
                is ApiResponse.Success -> {
                    updateState(UserListAction.UpdateUserSuccess(response.data))
                }

                is ApiResponse.Error -> {
                    updateState(UserListAction.UpdateUserError(
                        error = response.exception.message ?: "更新用户失败",
                        userId = user.id
                    ))
                }

                is ApiResponse.Loading -> {
                    // 已经处理
                }
            }
        }
    }

    /**
     * 删除单个用户
     */
    private fun deleteUser(userId: Long) {
        deleteUsers(listOf(userId))
    }

    /**
     * 删除多个用户
     */
    private fun deleteUsers(userIds: List<Long>) {
        viewModelScope.launch {
            updateState(UserListAction.StartDeletingUsers(userIds))

            // 逐个删除用户
            val failedIds = mutableListOf<Long>()
            for (userId in userIds) {
                when (val response = repository.deleteUser(userId)) {
                    is ApiResponse.Error -> {
                        failedIds.add(userId)
                    }
                    else -> {
                        // 成功或加载中
                    }
                }
            }

            if (failedIds.isEmpty()) {
                updateState(UserListAction.DeleteUsersSuccess(userIds))
            } else {
                updateState(UserListAction.DeleteUsersError(
                    error = "删除失败：${failedIds.size} 个用户删除失败",
                    userIds = failedIds
                ))
            }
        }
    }

    /**
     * 切换视图模式
     */
    private fun toggleViewMode(isGridMode: Boolean) {
        updateState(UserListAction.SetViewMode(isGridMode))
    }

    /**
     * 切换多选模式
     */
    private fun toggleMultiSelectMode(enabled: Boolean) {
        updateState(UserListAction.SetMultiSelectMode(enabled))
    }

    /**
     * 显示用户详情
     */
    private fun showUserDetails(userId: Long) {
        viewModelScope.launch {
            when (val response = repository.getUserById(userId)) {
                is ApiResponse.Success -> {
                    updateState(UserListAction.ShowUserDetail(response.data))
                }

                is ApiResponse.Error -> {
                    updateState(UserListAction.ShowErrorMessage(
                        response.exception.message ?: "获取用户详情失败"
                    ))
                }

                is ApiResponse.Loading -> {
                    // 可以显示加载状态
                }
            }
        }
    }

    /**
     * 隐藏用户详情
     */
    private fun hideUserDetails() {
        updateState(UserListAction.HideUserDetail)
    }

    /**
     * 显示错误
     */
    private fun showError(error: String) {
        updateState(UserListAction.ShowErrorMessage(error))
    }

    /**
     * 隐藏错误
     */
    private fun hideError() {
        updateState(UserListAction.HideErrorMessage)
    }

    /**
     * 清除缓存
     */
    private fun clearCache() {
        viewModelScope.launch {
            repository.clearCache()
            updateState(UserListAction.ClearCacheSuccess)
        }
    }

    /**
     * 更新状态的统一方法
     */
    private fun updateState(action: UserListAction) {
        MviLogger.logAction(TAG, action)

        val currentState = _state.value
        val newState = UserListReducer.reduce(currentState, action)
        _state.value = newState

        // 记录状态变化
        MviLogger.logStateChange(TAG, currentState, newState)
        logDetailedStateChange(action, currentState, newState)
    }
    
    /**
     * 记录详细的状态变化
     */
    private fun logDetailedStateChange(
        action: UserListAction,
        oldState: UserListState,
        newState: UserListState
    ) {
        val changes = mutableMapOf<String, Pair<Any?, Any?>>()

        if (oldState.users.size != newState.users.size) {
            changes["users.size"] = oldState.users.size to newState.users.size
        }
        if (oldState.isLoading != newState.isLoading) {
            changes["isLoading"] = oldState.isLoading to newState.isLoading
        }
        if (oldState.isRefreshing != newState.isRefreshing) {
            changes["isRefreshing"] = oldState.isRefreshing to newState.isRefreshing
        }
        if (oldState.errorMessage != newState.errorMessage) {
            changes["errorMessage"] = oldState.errorMessage to newState.errorMessage
        }
        if (oldState.searchQuery != newState.searchQuery) {
            changes["searchQuery"] = oldState.searchQuery to newState.searchQuery
        }
        if (oldState.selectedUsers.size != newState.selectedUsers.size) {
            changes["selectedUsers.size"] = oldState.selectedUsers.size to newState.selectedUsers.size
        }

        if (changes.isNotEmpty()) {
            MviLogger.logDetailedStateChange(TAG, changes)
        }
    }
    
    /**
     * 获取当前状态（用于测试）
     */
    fun getCurrentState(): UserListState = _state.value
    
    override fun onCleared() {
        super.onCleared()
        MviLogger.logLifecycle(TAG, "ViewModel清理")
        intentChannel.close()
    }
}
