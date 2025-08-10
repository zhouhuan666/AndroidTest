package com.gdet.testapp.hilt.presentation.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.gdet.testapp.hilt.data.models.User
import com.gdet.testapp.hilt.data.models.UserPreference
import com.gdet.testapp.hilt.domain.repository.UserRepository
import com.gdet.testapp.hilt.di.qualifiers.MainDispatcher
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject

/**
 * 用户ViewModel
 * 演示Hilt在ViewModel中的使用
 * 
 * 关键点：
 * 1. @HiltViewModel 注解：告诉Hilt这是一个需要依赖注入的ViewModel
 * 2. @Inject 构造函数：Hilt会自动注入所需的依赖
 * 3. ViewModel中的依赖：Repository、调度器等
 * 4. StateFlow的使用：响应式UI状态管理
 */
@HiltViewModel
class UserViewModel @Inject constructor(
    private val userRepository: UserRepository,                    // 注入Repository
    @MainDispatcher private val mainDispatcher: CoroutineDispatcher // 注入Main调度器
) : ViewModel() {
    
    companion object {
        private const val TAG = "HiltUserViewModel"
    }
    
    // UI状态管理
    private val _uiState = MutableStateFlow(UserUiState())
    val uiState: StateFlow<UserUiState> = _uiState.asStateFlow()
    
    private val _users = MutableStateFlow<List<User>>(emptyList())
    val users: StateFlow<List<User>> = _users.asStateFlow()
    
    private val _selectedUser = MutableStateFlow<User?>(null)
    val selectedUser: StateFlow<User?> = _selectedUser.asStateFlow()
    
    private val _userPreference = MutableStateFlow<UserPreference?>(null)
    val userPreference: StateFlow<UserPreference?> = _userPreference.asStateFlow()
    
    init {
        Log.d(TAG, "UserViewModel 被创建")
        Log.d(TAG, "注入的依赖:")
        Log.d(TAG, "  - UserRepository: ${userRepository.javaClass.simpleName}")
        Log.d(TAG, "  - MainDispatcher: $mainDispatcher")
        
        // 初始化时加载用户列表
        loadUsers()
    }
    
    /**
     * 加载用户列表
     * 演示在ViewModel中使用Repository和协程
     */
    fun loadUsers() {
        Log.d(TAG, "开始加载用户列表")
        _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        
        viewModelScope.launch {
            try {
                // 使用Repository获取数据
                val result = userRepository.getUsers()
                
                // 切换到Main线程更新UI
                withContext(mainDispatcher) {
                    if (result.isSuccess) {
                        val userList = result.getOrNull() ?: emptyList()
                        Log.d(TAG, "用户列表加载成功，共${userList.size}个用户")
                        _users.value = userList
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = null
                        )
                        
                        // 记录每个用户信息
                        userList.forEach { user ->
                            Log.d(TAG, "用户: $user")
                        }
                    } else {
                        val error = result.exceptionOrNull()?.message ?: "未知错误"
                        Log.e(TAG, "用户列表加载失败: $error")
                        _uiState.value = _uiState.value.copy(
                            isLoading = false,
                            error = error
                        )
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载用户列表异常", e)
                withContext(mainDispatcher) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "未知异常"
                    )
                }
            }
        }
    }
    
    /**
     * 根据ID选择用户
     */
    fun selectUser(userId: Int) {
        Log.d(TAG, "选择用户，ID: $userId")
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val result = userRepository.getUserById(userId)
                
                withContext(mainDispatcher) {
                    if (result.isSuccess) {
                        val user = result.getOrNull()
                        if (user != null) {
                            Log.d(TAG, "选择用户成功: $user")
                            _selectedUser.value = user
                            // 同时加载用户偏好设置
                            loadUserPreference(userId)
                        } else {
                            Log.d(TAG, "未找到ID为${userId}的用户")
                            _selectedUser.value = null
                        }
                    } else {
                        Log.e(TAG, "选择用户失败: ${result.exceptionOrNull()?.message}")
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "选择用户异常", e)
                withContext(mainDispatcher) {
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            }
        }
    }
    
    /**
     * 更新用户信息
     */
    fun updateUser(user: User) {
        Log.d(TAG, "更新用户: $user")
        _uiState.value = _uiState.value.copy(isLoading = true)
        
        viewModelScope.launch {
            try {
                val result = userRepository.updateUser(user)
                
                withContext(mainDispatcher) {
                    if (result.isSuccess && result.getOrNull() == true) {
                        Log.d(TAG, "用户更新成功")
                        _selectedUser.value = user
                        // 刷新用户列表
                        loadUsers()
                    } else {
                        Log.e(TAG, "用户更新失败")
                        _uiState.value = _uiState.value.copy(
                            error = "用户更新失败"
                        )
                    }
                    _uiState.value = _uiState.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                Log.e(TAG, "更新用户异常", e)
                withContext(mainDispatcher) {
                    _uiState.value = _uiState.value.copy(
                        isLoading = false,
                        error = e.message ?: "更新失败"
                    )
                }
            }
        }
    }
    
    /**
     * 加载用户偏好设置
     */
    private fun loadUserPreference(userId: Int) {
        Log.d(TAG, "加载用户偏好设置，用户ID: $userId")
        
        viewModelScope.launch {
            try {
                val preference = userRepository.getUserPreference(userId)
                
                withContext(mainDispatcher) {
                    _userPreference.value = preference
                    Log.d(TAG, "用户偏好设置加载完成: $preference")
                }
            } catch (e: Exception) {
                Log.e(TAG, "加载用户偏好设置异常", e)
            }
        }
    }
    
    /**
     * 保存用户偏好设置
     */
    fun saveUserPreference(preference: UserPreference) {
        Log.d(TAG, "保存用户偏好设置: $preference")
        
        viewModelScope.launch {
            try {
                userRepository.saveUserPreference(preference)
                
                withContext(mainDispatcher) {
                    _userPreference.value = preference
                    Log.d(TAG, "用户偏好设置保存成功")
                }
            } catch (e: Exception) {
                Log.e(TAG, "保存用户偏好设置异常", e)
                withContext(mainDispatcher) {
                    _uiState.value = _uiState.value.copy(
                        error = "保存偏好设置失败: ${e.message}"
                    )
                }
            }
        }
    }
    
    /**
     * 清除错误信息
     */
    fun clearError() {
        Log.d(TAG, "清除错误信息")
        _uiState.value = _uiState.value.copy(error = null)
    }
    
    /**
     * ViewModel销毁时的清理工作
     */
    override fun onCleared() {
        super.onCleared()
        Log.d(TAG, "UserViewModel 被销毁 - onCleared()")
    }
}

/**
 * UI状态数据类
 * 封装ViewModel的UI状态
 */
data class UserUiState(
    val isLoading: Boolean = false,
    val error: String? = null
)