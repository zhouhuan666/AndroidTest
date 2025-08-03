package com.gdet.testapp.mvi.complete

import com.gdet.testapp.mvi.complete.data.User
import com.gdet.testapp.mvi.complete.data.UserSortType

/**
 * 用户列表的Reducer（状态缩减器）
 * 
 * Reducer是MVI架构中的核心组件，负责根据当前状态和动作计算新状态
 * 在复杂应用中，Reducer通常会很大，可以考虑拆分为多个小的Reducer
 * 
 * Reducer的设计原则：
 * 1. 纯函数：相同输入总是产生相同输出，无副作用
 * 2. 不可变：不修改原状态，总是返回新状态
 * 3. 可预测：状态变化逻辑清晰明确
 * 4. 可测试：易于编写单元测试
 * 5. 可组合：可以将多个小Reducer组合成大Reducer
 */
object UserListReducer {
    
    /**
     * 根据当前状态和动作计算新状态
     * 
     * @param currentState 当前状态
     * @param action 要执行的动作
     * @return 新的状态
     */
    fun reduce(currentState: UserListState, action: UserListAction): UserListState {
        return when (action) {
            // ========== 加载状态相关 ==========
            is UserListAction.StartLoading -> {
                currentState.copy(
                    isLoading = true,
                    errorMessage = null
                )
            }
            
            is UserListAction.StartRefreshing -> {
                currentState.copy(
                    isRefreshing = true,
                    errorMessage = null
                )
            }
            
            is UserListAction.StartLoadingMore -> {
                currentState.copy(
                    isLoadingMore = true,
                    errorMessage = null
                )
            }
            
            is UserListAction.LoadUsersSuccess -> {
                val newUsers = if (action.isRefresh) {
                    action.users
                } else {
                    currentState.users + action.users
                }
                
                val newPage = if (action.isRefresh) 1 else currentState.currentPage + 1
                
                currentState.copy(
                    users = newUsers,
                    displayUsers = applyFiltersAndSorting(newUsers, currentState),
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    hasMoreData = action.hasMore,
                    currentPage = newPage,
                    errorMessage = null
                )
            }
            
            is UserListAction.LoadUsersError -> {
                currentState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    errorMessage = action.error
                )
            }
            
            is UserListAction.StopLoading -> {
                currentState.copy(
                    isLoading = false,
                    isRefreshing = false,
                    isLoadingMore = false,
                    isSearching = false
                )
            }
            
            // ========== 搜索相关 ==========
            is UserListAction.StartSearch -> {
                currentState.copy(
                    isSearching = true,
                    searchQuery = action.query,
                    isSearchMode = action.query.isNotBlank(),
                    errorMessage = null
                )
            }
            
            is UserListAction.SearchSuccess -> {
                currentState.copy(
                    searchResults = action.results,
                    displayUsers = if (currentState.isSearchMode) action.results else currentState.displayUsers,
                    isSearching = false,
                    errorMessage = null
                )
            }
            
            is UserListAction.SearchError -> {
                currentState.copy(
                    isSearching = false,
                    errorMessage = action.error
                )
            }
            
            is UserListAction.ClearSearchResults -> {
                currentState.copy(
                    searchQuery = "",
                    searchResults = emptyList(),
                    isSearchMode = false,
                    displayUsers = applyFiltersAndSorting(currentState.users, currentState),
                    isSearching = false
                )
            }
            
            // ========== 过滤和排序相关 ==========
            is UserListAction.ApplyUserFilter -> {
                val newState = currentState.copy(filter = action.filter)
                newState.copy(
                    displayUsers = applyFiltersAndSorting(newState.users, newState)
                )
            }
            
            is UserListAction.ClearUserFilter -> {
                val newState = currentState.copy(filter = currentState.filter.copy())
                newState.copy(
                    displayUsers = applyFiltersAndSorting(newState.users, newState)
                )
            }
            
            is UserListAction.ApplySorting -> {
                val newState = currentState.copy(
                    sortType = action.sortType,
                    sortAscending = action.ascending
                )
                newState.copy(
                    displayUsers = applyFiltersAndSorting(newState.users, newState)
                )
            }
            
            // ========== 用户选择相关 ==========
            is UserListAction.SetSelectedUser -> {
                currentState.copy(selectedUser = action.user)
            }
            
            is UserListAction.ToggleUserSelected -> {
                val newSelectedUsers = if (action.selected) {
                    currentState.selectedUsers + action.user.id
                } else {
                    currentState.selectedUsers - action.user.id
                }
                currentState.copy(selectedUsers = newSelectedUsers)
            }
            
            is UserListAction.SetAllUsersSelected -> {
                val newSelectedUsers = if (action.selected) {
                    currentState.displayUsers.map { it.id }.toSet()
                } else {
                    emptySet()
                }
                currentState.copy(selectedUsers = newSelectedUsers)
            }
            
            is UserListAction.ClearAllSelections -> {
                currentState.copy(
                    selectedUser = null,
                    selectedUsers = emptySet()
                )
            }
            
            // ========== CRUD操作相关 ==========
            is UserListAction.StartCreatingUser -> {
                currentState.copy(
                    operationType = OperationType.CREATING,
                    errorMessage = null
                )
            }
            
            is UserListAction.CreateUserSuccess -> {
                val newUsers = listOf(action.user) + currentState.users
                currentState.copy(
                    users = newUsers,
                    displayUsers = applyFiltersAndSorting(newUsers, currentState),
                    operationType = OperationType.NONE,
                    successMessage = "用户创建成功",
                    showSuccessMessage = true
                )
            }
            
            is UserListAction.CreateUserError -> {
                currentState.copy(
                    operationType = OperationType.NONE,
                    errorMessage = action.error,
                    showErrorDialog = true
                )
            }
            
            is UserListAction.StartUpdatingUser -> {
                currentState.copy(
                    operationType = OperationType.UPDATING,
                    operatingUserIds = setOf(action.userId),
                    errorMessage = null
                )
            }
            
            is UserListAction.UpdateUserSuccess -> {
                val newUsers = currentState.users.map { user ->
                    if (user.id == action.user.id) action.user else user
                }
                currentState.copy(
                    users = newUsers,
                    displayUsers = applyFiltersAndSorting(newUsers, currentState),
                    operationType = OperationType.NONE,
                    operatingUserIds = emptySet(),
                    successMessage = "用户更新成功",
                    showSuccessMessage = true
                )
            }
            
            is UserListAction.UpdateUserError -> {
                currentState.copy(
                    operationType = OperationType.NONE,
                    operatingUserIds = emptySet(),
                    errorMessage = action.error,
                    showErrorDialog = true
                )
            }
            
            is UserListAction.StartDeletingUsers -> {
                currentState.copy(
                    operationType = if (action.userIds.size > 1) OperationType.BATCH_DELETING else OperationType.DELETING,
                    operatingUserIds = action.userIds.toSet(),
                    errorMessage = null
                )
            }
            
            is UserListAction.DeleteUsersSuccess -> {
                val newUsers = currentState.users.filterNot { user ->
                    action.userIds.contains(user.id)
                }
                val newSelectedUsers = currentState.selectedUsers.filterNot { userId ->
                    action.userIds.contains(userId)
                }.toSet()
                
                currentState.copy(
                    users = newUsers,
                    displayUsers = applyFiltersAndSorting(newUsers, currentState),
                    selectedUsers = newSelectedUsers,
                    operationType = OperationType.NONE,
                    operatingUserIds = emptySet(),
                    successMessage = "删除成功",
                    showSuccessMessage = true
                )
            }
            
            is UserListAction.DeleteUsersError -> {
                currentState.copy(
                    operationType = OperationType.NONE,
                    operatingUserIds = emptySet(),
                    errorMessage = action.error,
                    showErrorDialog = true
                )
            }
            
            // ========== UI状态相关 ==========
            is UserListAction.SetViewMode -> {
                currentState.copy(isGridMode = action.isGridMode)
            }
            
            is UserListAction.SetMultiSelectMode -> {
                currentState.copy(
                    isMultiSelectMode = action.enabled,
                    selectedUsers = if (!action.enabled) emptySet() else currentState.selectedUsers
                )
            }
            
            is UserListAction.ShowUserDetail -> {
                currentState.copy(
                    userDetail = action.user,
                    showUserDetail = true
                )
            }
            
            is UserListAction.HideUserDetail -> {
                currentState.copy(
                    userDetail = null,
                    showUserDetail = false
                )
            }
            
            // ========== 消息相关 ==========
            is UserListAction.ShowErrorMessage -> {
                currentState.copy(
                    errorMessage = action.message,
                    showErrorDialog = true
                )
            }
            
            is UserListAction.HideErrorMessage -> {
                currentState.copy(
                    errorMessage = null,
                    showErrorDialog = false
                )
            }
            
            is UserListAction.ShowSuccessMessage -> {
                currentState.copy(
                    successMessage = action.message,
                    showSuccessMessage = true
                )
            }
            
            is UserListAction.HideSuccessMessage -> {
                currentState.copy(
                    successMessage = null,
                    showSuccessMessage = false
                )
            }
            
            is UserListAction.ClearCacheSuccess -> {
                currentState.copy(
                    users = emptyList(),
                    displayUsers = emptyList(),
                    searchResults = emptyList(),
                    selectedUsers = emptySet(),
                    currentPage = 1,
                    hasMoreData = true,
                    successMessage = "缓存清除成功",
                    showSuccessMessage = true
                )
            }
        }
    }
    
    /**
     * 应用过滤和排序条件
     */
    private fun applyFiltersAndSorting(
        users: List<User>,
        state: UserListState
    ): List<User> {
        var result = users
        
        // 应用过滤条件
        if (state.filter.hasActiveFilters()) {
            result = result.filter { user -> state.filter.matches(user) }
        }
        
        // 应用排序
        result = when (state.sortType) {
            UserSortType.NAME -> {
                if (state.sortAscending) {
                    result.sortedBy { it.name }
                } else {
                    result.sortedByDescending { it.name }
                }
            }
            UserSortType.EMAIL -> {
                if (state.sortAscending) {
                    result.sortedBy { it.email }
                } else {
                    result.sortedByDescending { it.email }
                }
            }
            UserSortType.CREATED_TIME -> {
                if (state.sortAscending) {
                    result.sortedBy { it.createdAt }
                } else {
                    result.sortedByDescending { it.createdAt }
                }
            }
            UserSortType.ONLINE_STATUS -> {
                if (state.sortAscending) {
                    result.sortedBy { !it.isOnline } // false在前，true在后
                } else {
                    result.sortedBy { it.isOnline } // true在前，false在后
                }
            }
        }
        
        return result
    }
}
