package com.gdet.testapp.mvi.complete.data

import android.content.Context
import com.gdet.testapp.mvi.complete.utils.MviLogger
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * 用户数据仓库
 *
 * Repository在MVI架构中的作用：
 * 1. 统一数据访问接口
 * 2. 协调本地数据库和远程数据
 * 3. 提供响应式数据流
 * 4. 处理数据同步逻辑
 *
 * Repository模式的优势：
 * - 数据源抽象：隐藏数据来源的复杂性
 * - 缓存管理：使用Room数据库进行本地缓存
 * - 离线支持：提供离线数据访问能力
 * - 测试友好：便于单元测试和Mock
 *
 * 更新说明：
 * - 使用Room数据库替代内存缓存
 * - 添加详细的日志记录
 * - 支持数据库初始化和数据同步
 */
class UserRepository(
    context: Context,
    private val apiService: UserApiService = MockUserApiService()
) {

    companion object {
        private const val TAG = "UserRepository"
    }

    // Room数据库实例
    private val database = AppDatabase.getDatabase(context)
    private val userDao = database.userDao()

    init {
        MviLogger.logRepository(TAG, "Repository初始化", "使用Room数据库")
    }
    
    /**
     * 获取用户列表流（从数据库）
     *
     * @return 用户列表的Flow
     */
    fun getCachedUsersFlow(): Flow<List<User>> {
        MviLogger.logRepository(TAG, "获取用户列表Flow")
        return userDao.getAllUsersFlow()
    }

    /**
     * 根据过滤条件获取用户列表流
     *
     * @param filter 过滤条件
     * @return 过滤后的用户列表Flow
     */
    fun getFilteredUsersFlow(filter: UserFilter): Flow<List<User>> {
        MviLogger.logRepository(TAG, "获取过滤用户列表Flow", "过滤条件: ${filter.hasActiveFilters()}")
        return userDao.getAllUsersFlow().map { users ->
            users.filter { user -> filter.matches(user) }
        }
    }
    
    /**
     * 从网络加载用户列表并同步到数据库
     *
     * @param page 页码
     * @param pageSize 每页大小
     * @param refresh 是否刷新（清空数据库）
     * @return API响应
     */
    suspend fun loadUsers(
        page: Int = 1,
        pageSize: Int = 20,
        refresh: Boolean = false
    ): ApiResponse<List<User>> {
        MviLogger.logRepository(TAG, "加载用户列表", "页码: $page, 每页: $pageSize, 刷新: $refresh")

        // 如果是刷新操作，清空数据库
        if (refresh) {
            MviLogger.logDatabase(TAG, "清空数据库", "刷新操作")
            userDao.deleteAllUsers()
        }

        return when (val response = apiService.getUsers(page, pageSize)) {
            is ApiResponse.Success -> {
                val newUsers = response.data
                MviLogger.logNetwork(TAG, "网络请求成功", "获取到 ${newUsers.size} 个用户")

                try {
                    if (page == 1) {
                        // 第一页：如果不是刷新操作，先清空数据库
                        if (!refresh) {
                            MviLogger.logDatabase(TAG, "清空数据库", "加载第一页")
                            userDao.deleteAllUsers()
                        }
                        // 插入新数据
                        val insertedIds = userDao.insertUsers(newUsers)
                        MviLogger.logDatabase(TAG, "插入用户数据", "插入了 ${insertedIds.size} 个用户")
                    } else {
                        // 后续页：直接追加数据
                        val insertedIds = userDao.insertUsers(newUsers)
                        MviLogger.logDatabase(TAG, "追加用户数据", "追加了 ${insertedIds.size} 个用户")
                    }

                    response
                } catch (e: Exception) {
                    MviLogger.logError(TAG, "保存用户数据到数据库失败", e)
                    // 即使数据库操作失败，也返回网络数据
                    response
                }
            }
            is ApiResponse.Error -> {
                MviLogger.logError(TAG, "网络请求失败", response.exception)

                // 网络错误时，尝试从数据库加载
                if (page == 1) {
                    try {
                        val cachedUsers = userDao.getAllUsers()
                        if (cachedUsers.isNotEmpty()) {
                            MviLogger.logDatabase(TAG, "从数据库加载数据", "获取到 ${cachedUsers.size} 个缓存用户")
                            ApiResponse.Success(cachedUsers)
                        } else {
                            MviLogger.logDatabase(TAG, "数据库为空", "返回网络错误")
                            response
                        }
                    } catch (e: Exception) {
                        MviLogger.logError(TAG, "从数据库加载数据失败", e)
                        response
                    }
                } else {
                    response
                }
            }
            is ApiResponse.Loading -> {
                MviLogger.logNetwork(TAG, "网络请求中")
                response
            }
        }
    }
    
    /**
     * 根据ID获取用户详情
     *
     * @param userId 用户ID
     * @return API响应
     */
    suspend fun getUserById(userId: Long): ApiResponse<User> {
        MviLogger.logRepository(TAG, "获取用户详情", "用户ID: $userId")

        try {
            // 先从数据库中查找
            val cachedUser = userDao.getUserById(userId)
            if (cachedUser != null) {
                MviLogger.logDatabase(TAG, "从数据库获取用户", "用户: ${cachedUser.name}")
                return ApiResponse.Success(cachedUser)
            }
        } catch (e: Exception) {
            MviLogger.logError(TAG, "从数据库获取用户失败", e)
        }

        // 数据库中没有，从网络获取
        return when (val response = apiService.getUserById(userId)) {
            is ApiResponse.Success -> {
                val user = response.data
                MviLogger.logNetwork(TAG, "网络获取用户成功", "用户: ${user.name}")

                try {
                    // 保存到数据库
                    userDao.insertUser(user)
                    MviLogger.logDatabase(TAG, "保存用户到数据库", "用户: ${user.name}")
                } catch (e: Exception) {
                    MviLogger.logError(TAG, "保存用户到数据库失败", e)
                }

                response
            }
            is ApiResponse.Error -> {
                MviLogger.logError(TAG, "网络获取用户失败", response.exception)
                response
            }
            else -> response
        }
    }
    
    /**
     * 创建新用户
     *
     * @param user 用户信息
     * @return API响应
     */
    suspend fun createUser(user: User): ApiResponse<User> {
        MviLogger.logRepository(TAG, "创建新用户", "用户: ${user.name}, 邮箱: ${user.email}")

        // 先检查邮箱是否已存在
        try {
            val emailExists = userDao.isEmailExists(user.email)
            if (emailExists) {
                MviLogger.logValidation(TAG, "邮箱", false, "邮箱已存在: ${user.email}")
                return ApiResponse.Error(Exception("邮箱已存在"))
            }
        } catch (e: Exception) {
            MviLogger.logError(TAG, "检查邮箱是否存在失败", e)
        }

        return when (val response = apiService.createUser(user)) {
            is ApiResponse.Success -> {
                val newUser = response.data
                MviLogger.logNetwork(TAG, "网络创建用户成功", "用户: ${newUser.name}")

                try {
                    // 保存到数据库
                    val insertedId = userDao.insertUser(newUser)
                    MviLogger.logDatabase(TAG, "保存新用户到数据库", "用户ID: $insertedId")
                } catch (e: Exception) {
                    MviLogger.logError(TAG, "保存新用户到数据库失败", e)
                }

                response
            }
            is ApiResponse.Error -> {
                MviLogger.logError(TAG, "网络创建用户失败", response.exception)
                response
            }
            else -> response
        }
    }
    
    /**
     * 更新用户信息
     *
     * @param user 用户信息
     * @return API响应
     */
    suspend fun updateUser(user: User): ApiResponse<User> {
        MviLogger.logRepository(TAG, "更新用户信息", "用户ID: ${user.id}, 用户: ${user.name}")

        return when (val response = apiService.updateUser(user)) {
            is ApiResponse.Success -> {
                val updatedUser = response.data
                MviLogger.logNetwork(TAG, "网络更新用户成功", "用户: ${updatedUser.name}")

                try {
                    // 更新数据库
                    val affectedRows = userDao.updateUser(updatedUser)
                    MviLogger.logDatabase(TAG, "更新数据库用户", "影响行数: $affectedRows")
                } catch (e: Exception) {
                    MviLogger.logError(TAG, "更新数据库用户失败", e)
                }

                response
            }
            is ApiResponse.Error -> {
                MviLogger.logError(TAG, "网络更新用户失败", response.exception)
                response
            }
            else -> response
        }
    }
    
    /**
     * 删除用户
     *
     * @param userId 用户ID
     * @return API响应
     */
    suspend fun deleteUser(userId: Long): ApiResponse<Unit> {
        MviLogger.logRepository(TAG, "删除用户", "用户ID: $userId")

        return when (val response = apiService.deleteUser(userId)) {
            is ApiResponse.Success -> {
                MviLogger.logNetwork(TAG, "网络删除用户成功", "用户ID: $userId")

                try {
                    // 从数据库中删除
                    val affectedRows = userDao.deleteUserById(userId)
                    MviLogger.logDatabase(TAG, "从数据库删除用户", "影响行数: $affectedRows")
                } catch (e: Exception) {
                    MviLogger.logError(TAG, "从数据库删除用户失败", e)
                }

                response
            }
            is ApiResponse.Error -> {
                MviLogger.logError(TAG, "网络删除用户失败", response.exception)
                response
            }
            else -> response
        }
    }

    /**
     * 搜索用户
     *
     * @param query 搜索关键词
     * @return API响应
     */
    suspend fun searchUsers(query: String): ApiResponse<List<User>> {
        MviLogger.logRepository(TAG, "搜索用户", "关键词: $query")

        // 优先从数据库搜索
        try {
            val localResults = userDao.searchUsers(query)
            if (localResults.isNotEmpty()) {
                MviLogger.logDatabase(TAG, "数据库搜索成功", "找到 ${localResults.size} 个用户")
                return ApiResponse.Success(localResults)
            }
        } catch (e: Exception) {
            MviLogger.logError(TAG, "数据库搜索失败", e)
        }

        // 数据库没有结果，尝试网络搜索
        return when (val response = apiService.searchUsers(query)) {
            is ApiResponse.Success -> {
                MviLogger.logNetwork(TAG, "网络搜索成功", "找到 ${response.data.size} 个用户")
                response
            }
            is ApiResponse.Error -> {
                MviLogger.logError(TAG, "网络搜索失败", response.exception)
                response
            }
            else -> response
        }
    }

    /**
     * 清空所有缓存
     */
    suspend fun clearCache() {
        MviLogger.logRepository(TAG, "清空所有缓存")

        try {
            val deletedRows = userDao.deleteAllUsers()
            MviLogger.logDatabase(TAG, "清空数据库", "删除了 $deletedRows 行数据")
        } catch (e: Exception) {
            MviLogger.logError(TAG, "清空数据库失败", e)
        }
    }

    /**
     * 获取缓存的用户数量
     */
    suspend fun getCachedUserCount(): Int {
        return try {
            val count = userDao.getUserCount()
            MviLogger.logDatabase(TAG, "获取用户数量", "数量: $count")
            count
        } catch (e: Exception) {
            MviLogger.logError(TAG, "获取用户数量失败", e)
            0
        }
    }
}
