package com.gdet.testapp.hilt.data.api

import android.util.Log
import com.gdet.testapp.hilt.data.models.User
import javax.inject.Inject
import javax.inject.Singleton

/**
 * API服务接口
 * 演示如何在Hilt中定义和使用接口
 */
interface ApiService {
    suspend fun getUsers(): List<User>
    suspend fun getUserById(id: Int): User?
    suspend fun updateUser(user: User): Boolean
}

/**
 * 真实API服务实现
 * 演示Constructor Injection（构造函数注入）
 */
@Singleton
class RealApiService @Inject constructor() : ApiService {
    
    companion object {
        private const val TAG = "HiltRealApiService"
    }
    
    // 模拟用户数据
    private val users = mutableListOf(
        User(1, "张三", "zhangsan@example.com", 25),
        User(2, "李四", "lisi@example.com", 30),
        User(3, "王五", "wangwu@example.com", 28)
    )
    
    init {
        Log.d(TAG, "RealApiService 被创建 - Constructor Injection")
    }
    
    override suspend fun getUsers(): List<User> {
        Log.d(TAG, "获取所有用户列表，共${users.size}个用户")
        // 模拟网络延迟
        kotlinx.coroutines.delay(500)
        return users.toList()
    }
    
    override suspend fun getUserById(id: Int): User? {
        Log.d(TAG, "根据ID获取用户: $id")
        kotlinx.coroutines.delay(200)
        return users.find { it.id == id }
    }
    
    override suspend fun updateUser(user: User): Boolean {
        Log.d(TAG, "更新用户信息: $user")
        kotlinx.coroutines.delay(300)
        val index = users.indexOfFirst { it.id == user.id }
        return if (index != -1) {
            users[index] = user
            Log.d(TAG, "用户更新成功")
            true
        } else {
            Log.d(TAG, "用户更新失败：未找到用户")
            false
        }
    }
}

/**
 * 模拟API服务实现
 * 演示如何为同一个接口提供不同的实现
 */
@Singleton
class MockApiService @Inject constructor() : ApiService {
    
    companion object {
        private const val TAG = "HiltMockApiService"
    }
    
    init {
        Log.d(TAG, "MockApiService 被创建 - Constructor Injection")
    }
    
    override suspend fun getUsers(): List<User> {
        Log.d(TAG, "Mock - 获取用户列表")
        return listOf(
            User(100, "Mock用户1", "mock1@test.com", 20),
            User(101, "Mock用户2", "mock2@test.com", 22)
        )
    }
    
    override suspend fun getUserById(id: Int): User? {
        Log.d(TAG, "Mock - 根据ID获取用户: $id")
        return User(id, "Mock用户$id", "mock$id@test.com", 25)
    }
    
    override suspend fun updateUser(user: User): Boolean {
        Log.d(TAG, "Mock - 更新用户: $user")
        return true
    }
}