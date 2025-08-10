package com.gdet.testapp.hilt.domain.repository

import android.util.Log
import com.gdet.testapp.hilt.data.api.ApiService
import com.gdet.testapp.hilt.data.local.DatabaseService
import com.gdet.testapp.hilt.data.models.User
import com.gdet.testapp.hilt.data.models.UserPreference
import com.gdet.testapp.hilt.di.qualifiers.IoDispatcher
import com.gdet.testapp.hilt.di.qualifiers.RealApi
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 用户Repository接口
 * 定义了用户数据操作的抽象接口
 */
interface UserRepository {
    suspend fun getUsers(): Result<List<User>>
    suspend fun getUserById(id: Int): Result<User?>
    suspend fun updateUser(user: User): Result<Boolean>
    suspend fun saveUserPreference(preference: UserPreference)
    suspend fun getUserPreference(userId: Int): UserPreference?
    fun getUsersFlow(): Flow<List<User>>
}

/**
 * 用户Repository实现
 * 演示Repository模式在Hilt中的使用
 * 
 * 这个类展示了：
 * 1. 多个依赖的注入（ApiService, DatabaseService, CoroutineDispatcher）
 * 2. Qualifier的使用（@RealApi, @IoDispatcher）
 * 3. Constructor Injection
 * 4. @Singleton作用域
 */
@Singleton
class UserRepositoryImpl @Inject constructor(
    @RealApi private val apiService: ApiService,              // 注入真实API服务
    private val databaseService: DatabaseService,              // 注入数据库服务
    @IoDispatcher private val ioDispatcher: CoroutineDispatcher // 注入IO调度器
) : UserRepository {
    
    companion object {
        private const val TAG = "HiltUserRepository"
    }
    
    init {
        Log.d(TAG, "UserRepositoryImpl 被创建")
        Log.d(TAG, "注入的依赖:")
        Log.d(TAG, "  - ApiService: ${apiService.javaClass.simpleName}")
        Log.d(TAG, "  - DatabaseService: ${databaseService.javaClass.simpleName}")
        Log.d(TAG, "  - IoDispatcher: ${ioDispatcher}")
    }
    
    /**
     * 获取用户列表
     * 演示协程调度器的使用和错误处理
     */
    override suspend fun getUsers(): Result<List<User>> {
        return withContext(ioDispatcher) {
            try {
                Log.d(TAG, "开始获取用户列表 - 使用IO调度器")
                val users = apiService.getUsers()
                Log.d(TAG, "成功获取用户列表，共${users.size}个用户")
                Result.success(users)
            } catch (e: Exception) {
                Log.e(TAG, "获取用户列表失败", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * 根据ID获取用户
     */
    override suspend fun getUserById(id: Int): Result<User?> {
        return withContext(ioDispatcher) {
            try {
                Log.d(TAG, "根据ID获取用户: $id")
                val user = apiService.getUserById(id)
                if (user != null) {
                    Log.d(TAG, "找到用户: $user")
                } else {
                    Log.d(TAG, "未找到ID为${id}的用户")
                }
                Result.success(user)
            } catch (e: Exception) {
                Log.e(TAG, "根据ID获取用户失败", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * 更新用户信息
     */
    override suspend fun updateUser(user: User): Result<Boolean> {
        return withContext(ioDispatcher) {
            try {
                Log.d(TAG, "更新用户信息: $user")
                val success = apiService.updateUser(user)
                if (success) {
                    Log.d(TAG, "用户信息更新成功")
                } else {
                    Log.d(TAG, "用户信息更新失败")
                }
                Result.success(success)
            } catch (e: Exception) {
                Log.e(TAG, "更新用户信息异常", e)
                Result.failure(e)
            }
        }
    }
    
    /**
     * 保存用户偏好设置
     * 演示数据库服务的使用
     */
    override suspend fun saveUserPreference(preference: UserPreference) {
        withContext(ioDispatcher) {
            Log.d(TAG, "保存用户偏好设置: $preference")
            databaseService.saveUserPreference(preference)
            Log.d(TAG, "用户偏好设置保存完成")
        }
    }
    
    /**
     * 获取用户偏好设置
     */
    override suspend fun getUserPreference(userId: Int): UserPreference? {
        return withContext(ioDispatcher) {
            Log.d(TAG, "获取用户偏好设置，用户ID: $userId")
            val preference = databaseService.getUserPreference(userId)
            Log.d(TAG, "用户偏好设置: $preference")
            preference
        }
    }
    
    /**
     * 获取用户列表的Flow
     * 演示如何在Repository中使用Flow
     */
    override fun getUsersFlow(): Flow<List<User>> {
        Log.d(TAG, "创建用户列表Flow")
        return flow {
            try {
                Log.d(TAG, "Flow - 开始获取用户列表")
                val users = apiService.getUsers()
                Log.d(TAG, "Flow - 发射用户列表，共${users.size}个用户")
                emit(users)
            } catch (e: Exception) {
                Log.e(TAG, "Flow - 获取用户列表失败", e)
                emit(emptyList())
            }
        }.flowOn(ioDispatcher) // 指定在IO线程执行
    }
}