package com.gdet.testapp.mvi.complete.data

import androidx.room.*
import kotlinx.coroutines.flow.Flow

/**
 * 用户数据访问对象 (Data Access Object)
 * 
 * 定义了与用户表相关的所有数据库操作
 * 使用Room注解自动生成实现代码
 * 
 * 在MVI架构中，DAO提供了数据层的抽象，
 * Repository通过DAO与数据库交互
 */
@Dao
interface UserDao {
    
    /**
     * 获取所有用户的Flow
     * Flow提供响应式数据流，当数据库数据变化时自动通知观察者
     * 
     * @return 用户列表的Flow
     */
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    fun getAllUsersFlow(): Flow<List<User>>
    
    /**
     * 获取所有用户（一次性查询）
     * 
     * @return 用户列表
     */
    @Query("SELECT * FROM users ORDER BY createdAt DESC")
    suspend fun getAllUsers(): List<User>
    
    /**
     * 根据ID获取用户
     * 
     * @param userId 用户ID
     * @return 用户对象，如果不存在则为null
     */
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Long): User?
    
    /**
     * 根据关键词搜索用户
     * 支持按用户名和邮箱搜索
     * 
     * @param query 搜索关键词
     * @return 匹配的用户列表
     */
    @Query("""
        SELECT * FROM users 
        WHERE name LIKE '%' || :query || '%' 
        OR email LIKE '%' || :query || '%'
        ORDER BY createdAt DESC
    """)
    suspend fun searchUsers(query: String): List<User>
    
    /**
     * 根据城市过滤用户
     * 
     * @param city 城市名称
     * @return 该城市的用户列表
     */
    @Query("SELECT * FROM users WHERE city = :city ORDER BY createdAt DESC")
    suspend fun getUsersByCity(city: String): List<User>
    
    /**
     * 获取在线用户
     * 
     * @return 在线用户列表
     */
    @Query("SELECT * FROM users WHERE isOnline = 1 ORDER BY createdAt DESC")
    suspend fun getOnlineUsers(): List<User>
    
    /**
     * 分页获取用户
     * 
     * @param limit 每页数量
     * @param offset 偏移量
     * @return 用户列表
     */
    @Query("SELECT * FROM users ORDER BY createdAt DESC LIMIT :limit OFFSET :offset")
    suspend fun getUsersPaged(limit: Int, offset: Int): List<User>
    
    /**
     * 插入单个用户
     * 
     * @param user 要插入的用户
     * @return 插入后的用户ID
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUser(user: User): Long
    
    /**
     * 批量插入用户
     * 
     * @param users 要插入的用户列表
     * @return 插入后的用户ID列表
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUsers(users: List<User>): List<Long>
    
    /**
     * 更新用户信息
     * 
     * @param user 要更新的用户
     * @return 受影响的行数
     */
    @Update
    suspend fun updateUser(user: User): Int
    
    /**
     * 删除用户
     * 
     * @param user 要删除的用户
     * @return 受影响的行数
     */
    @Delete
    suspend fun deleteUser(user: User): Int
    
    /**
     * 根据ID删除用户
     * 
     * @param userId 用户ID
     * @return 受影响的行数
     */
    @Query("DELETE FROM users WHERE id = :userId")
    suspend fun deleteUserById(userId: Long): Int
    
    /**
     * 批量删除用户
     * 
     * @param userIds 要删除的用户ID列表
     * @return 受影响的行数
     */
    @Query("DELETE FROM users WHERE id IN (:userIds)")
    suspend fun deleteUsersByIds(userIds: List<Long>): Int
    
    /**
     * 清空所有用户数据
     * 
     * @return 受影响的行数
     */
    @Query("DELETE FROM users")
    suspend fun deleteAllUsers(): Int
    
    /**
     * 获取用户总数
     * 
     * @return 用户总数
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun getUserCount(): Int
    
    /**
     * 检查邮箱是否已存在
     * 
     * @param email 邮箱地址
     * @return 是否存在
     */
    @Query("SELECT COUNT(*) > 0 FROM users WHERE email = :email")
    suspend fun isEmailExists(email: String): Boolean
    
    /**
     * 更新用户在线状态
     * 
     * @param userId 用户ID
     * @param isOnline 在线状态
     * @return 受影响的行数
     */
    @Query("UPDATE users SET isOnline = :isOnline, updatedAt = :updatedAt WHERE id = :userId")
    suspend fun updateUserOnlineStatus(userId: Long, isOnline: Boolean, updatedAt: Long): Int
}
