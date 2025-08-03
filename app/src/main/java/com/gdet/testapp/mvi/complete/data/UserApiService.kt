package com.gdet.testapp.mvi.complete.data

import kotlinx.coroutines.delay
import kotlin.random.Random

/**
 * 用户API服务接口
 * 
 * 在真实项目中，这里会使用Retrofit等网络库
 * 这里使用模拟数据来演示MVI架构中的异步操作处理
 */
interface UserApiService {
    
    /**
     * 获取用户列表
     * 
     * @param page 页码（从1开始）
     * @param pageSize 每页大小
     * @return 用户列表响应
     */
    suspend fun getUsers(page: Int = 1, pageSize: Int = 20): ApiResponse<List<User>>
    
    /**
     * 根据ID获取用户详情
     * 
     * @param userId 用户ID
     * @return 用户详情响应
     */
    suspend fun getUserById(userId: Long): ApiResponse<User>
    
    /**
     * 创建新用户
     * 
     * @param user 用户信息
     * @return 创建结果响应
     */
    suspend fun createUser(user: User): ApiResponse<User>
    
    /**
     * 更新用户信息
     * 
     * @param user 用户信息
     * @return 更新结果响应
     */
    suspend fun updateUser(user: User): ApiResponse<User>
    
    /**
     * 删除用户
     * 
     * @param userId 用户ID
     * @return 删除结果响应
     */
    suspend fun deleteUser(userId: Long): ApiResponse<Unit>
    
    /**
     * 搜索用户
     * 
     * @param query 搜索关键词
     * @return 搜索结果响应
     */
    suspend fun searchUsers(query: String): ApiResponse<List<User>>
}

/**
 * API响应包装类
 * 
 * 统一处理API响应，包含成功和失败状态
 */
sealed class ApiResponse<out T> {
    /**
     * 成功响应
     */
    data class Success<T>(val data: T) : ApiResponse<T>()
    
    /**
     * 错误响应
     */
    data class Error(val exception: Exception) : ApiResponse<Nothing>()
    
    /**
     * 加载中状态
     */
    object Loading : ApiResponse<Nothing>()
}

/**
 * 模拟的用户API服务实现
 *
 * 在真实项目中，这里会是Retrofit的接口实现
 * 这里使用模拟数据来演示MVI架构
 *
 * 更新说明：
 * - 默认模拟网络请求失败
 * - 演示离线模式和本地缓存的使用
 * - 可通过开关控制网络状态
 */
class MockUserApiService : UserApiService {

    companion object {
        // 网络模拟开关：true=网络正常，false=网络失败
        private var isNetworkAvailable = false

        /**
         * 设置网络状态（用于测试不同场景）
         */
        fun setNetworkAvailable(available: Boolean) {
            isNetworkAvailable = available
            println("🌐 网络状态设置为: ${if (available) "可用" else "不可用"}")
        }

        /**
         * 获取当前网络状态
         */
        fun isNetworkAvailable(): Boolean = isNetworkAvailable
    }
    
    // 模拟的用户数据
    private val mockUsers = mutableListOf<User>().apply {
        repeat(50) { index ->
            add(
                User(
                    id = index.toLong() + 1,
                    name = "用户${index + 1}",
                    email = "user${index + 1}@example.com",
                    avatarUrl = "https://picsum.photos/200/200?random=${index + 1}",
                    age = Random.nextInt(18, 65),
                    city = listOf("北京", "上海", "广州", "深圳", "杭州", "成都").random(),
                    isOnline = Random.nextBoolean(),
                    createdAt = System.currentTimeMillis() - Random.nextLong(0, 365L * 24 * 60 * 60 * 1000),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }
    }
    
    override suspend fun getUsers(page: Int, pageSize: Int): ApiResponse<List<User>> {
        return try {
            // 模拟网络延迟
            delay(Random.nextLong(300, 1000))

            // 检查网络状态 - 默认模拟网络不可用
            if (!isNetworkAvailable) {
                throw Exception("网络不可用 - 模拟离线状态")
            }

            // 如果网络可用，模拟偶尔的网络错误（5%概率）
            if (Random.nextFloat() < 0.05f) {
                throw Exception("网络连接超时")
            }

            val startIndex = (page - 1) * pageSize
            val endIndex = minOf(startIndex + pageSize, mockUsers.size)

            if (startIndex >= mockUsers.size) {
                ApiResponse.Success(emptyList())
            } else {
                val pageData = mockUsers.subList(startIndex, endIndex)
                println("🌐 网络请求成功: 返回 ${pageData.size} 个用户 (页码: $page)")
                ApiResponse.Success(pageData)
            }
        } catch (e: Exception) {
            println("❌ 网络请求失败: ${e.message}")
            ApiResponse.Error(e)
        }
    }
    
    override suspend fun getUserById(userId: Long): ApiResponse<User> {
        return try {
            delay(Random.nextLong(200, 800))

            // 检查网络状态
            if (!isNetworkAvailable) {
                throw Exception("网络不可用 - 无法获取用户详情")
            }

            if (Random.nextFloat() < 0.03f) {
                throw Exception("服务器错误")
            }

            val user = mockUsers.find { it.id == userId }
            if (user != null) {
                println("🌐 网络获取用户成功: ${user.name}")
                ApiResponse.Success(user)
            } else {
                ApiResponse.Error(Exception("用户不存在"))
            }
        } catch (e: Exception) {
            println("❌ 网络获取用户失败: ${e.message}")
            ApiResponse.Error(e)
        }
    }
    
    override suspend fun createUser(user: User): ApiResponse<User> {
        return try {
            delay(Random.nextLong(500, 1500))

            // 检查网络状态
            if (!isNetworkAvailable) {
                throw Exception("网络不可用 - 无法创建用户")
            }

            if (Random.nextFloat() < 0.05f) {
                throw Exception("服务器繁忙，创建用户失败")
            }

            val newUser = user.copy(
                id = mockUsers.maxOfOrNull { it.id }?.plus(1) ?: 1,
                createdAt = System.currentTimeMillis(),
                updatedAt = System.currentTimeMillis()
            )
            mockUsers.add(newUser)

            println("🌐 网络创建用户成功: ${newUser.name}")
            ApiResponse.Success(newUser)
        } catch (e: Exception) {
            println("❌ 网络创建用户失败: ${e.message}")
            ApiResponse.Error(e)
        }
    }
    
    override suspend fun updateUser(user: User): ApiResponse<User> {
        return try {
            delay(Random.nextLong(400, 1200))

            // 检查网络状态
            if (!isNetworkAvailable) {
                throw Exception("网络不可用 - 无法更新用户")
            }

            if (Random.nextFloat() < 0.05f) {
                throw Exception("服务器错误，更新用户失败")
            }

            val index = mockUsers.indexOfFirst { it.id == user.id }
            if (index != -1) {
                val updatedUser = user.copy(updatedAt = System.currentTimeMillis())
                mockUsers[index] = updatedUser
                println("🌐 网络更新用户成功: ${updatedUser.name}")
                ApiResponse.Success(updatedUser)
            } else {
                ApiResponse.Error(Exception("用户不存在"))
            }
        } catch (e: Exception) {
            println("❌ 网络更新用户失败: ${e.message}")
            ApiResponse.Error(e)
        }
    }
    
    override suspend fun deleteUser(userId: Long): ApiResponse<Unit> {
        return try {
            delay(Random.nextLong(300, 1000))

            // 检查网络状态
            if (!isNetworkAvailable) {
                throw Exception("网络不可用 - 无法删除用户")
            }

            if (Random.nextFloat() < 0.03f) {
                throw Exception("服务器错误，删除用户失败")
            }

            val removed = mockUsers.removeIf { it.id == userId }
            if (removed) {
                println("🌐 网络删除用户成功: 用户ID $userId")
                ApiResponse.Success(Unit)
            } else {
                ApiResponse.Error(Exception("用户不存在"))
            }
        } catch (e: Exception) {
            println("❌ 网络删除用户失败: ${e.message}")
            ApiResponse.Error(e)
        }
    }
    
    override suspend fun searchUsers(query: String): ApiResponse<List<User>> {
        return try {
            delay(Random.nextLong(200, 800))

            // 检查网络状态
            if (!isNetworkAvailable) {
                throw Exception("网络不可用 - 无法搜索用户")
            }

            if (Random.nextFloat() < 0.03f) {
                throw Exception("搜索服务暂时不可用")
            }

            val results = mockUsers.filter { user ->
                user.name.contains(query, ignoreCase = true) ||
                user.email.contains(query, ignoreCase = true) ||
                user.city?.contains(query, ignoreCase = true) == true
            }

            println("🌐 网络搜索成功: 找到 ${results.size} 个匹配用户")
            ApiResponse.Success(results)
        } catch (e: Exception) {
            println("❌ 网络搜索失败: ${e.message}")
            ApiResponse.Error(e)
        }
    }
}
