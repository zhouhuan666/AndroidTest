package com.gdet.testapp.mvi.complete.data

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * 用户数据模型
 *
 * 在MVI架构中，数据模型应该是不可变的
 * 使用data class确保数据的不可变性和结构化
 *
 * 使用Room注解将其标记为数据库实体
 */
@Entity(tableName = "users")
data class User(
    /**
     * 用户唯一标识符
     */
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    
    /**
     * 用户名
     */
    val name: String,
    
    /**
     * 用户邮箱
     */
    val email: String,
    
    /**
     * 用户头像URL
     */
    val avatarUrl: String? = null,
    
    /**
     * 用户年龄
     */
    val age: Int? = null,
    
    /**
     * 用户城市
     */
    val city: String? = null,
    
    /**
     * 用户状态（在线/离线）
     */
    val isOnline: Boolean = false,
    
    /**
     * 创建时间戳
     */
    val createdAt: Long = System.currentTimeMillis(),
    
    /**
     * 最后更新时间戳
     */
    val updatedAt: Long = System.currentTimeMillis()
) {
    
    /**
     * 获取用户显示名称
     * 如果名称为空，则使用邮箱的用户名部分
     */
    fun getDisplayName(): String {
        return if (name.isNotBlank()) {
            name
        } else {
            email.substringBefore("@")
        }
    }
    
    /**
     * 获取用户状态文本
     */
    fun getStatusText(): String {
        return if (isOnline) "在线" else "离线"
    }
    
    /**
     * 检查用户信息是否完整
     */
    fun isProfileComplete(): Boolean {
        return name.isNotBlank() && 
               email.isNotBlank() && 
               age != null && 
               city != null
    }
}

/**
 * 用户列表的排序方式
 */
enum class UserSortType {
    /**
     * 按名称排序
     */
    NAME,
    
    /**
     * 按邮箱排序
     */
    EMAIL,
    
    /**
     * 按创建时间排序
     */
    CREATED_TIME,
    
    /**
     * 按在线状态排序
     */
    ONLINE_STATUS
}

/**
 * 用户过滤条件
 */
data class UserFilter(
    /**
     * 搜索关键词
     */
    val searchQuery: String = "",
    
    /**
     * 是否只显示在线用户
     */
    val onlineOnly: Boolean = false,
    
    /**
     * 城市过滤
     */
    val cityFilter: String? = null,
    
    /**
     * 年龄范围过滤
     */
    val ageRange: IntRange? = null
) {
    
    /**
     * 检查用户是否匹配过滤条件
     */
    fun matches(user: User): Boolean {
        // 搜索关键词匹配
        if (searchQuery.isNotBlank()) {
            val query = searchQuery.lowercase()
            val matchesName = user.name.lowercase().contains(query)
            val matchesEmail = user.email.lowercase().contains(query)
            if (!matchesName && !matchesEmail) {
                return false
            }
        }
        
        // 在线状态过滤
        if (onlineOnly && !user.isOnline) {
            return false
        }
        
        // 城市过滤
        if (cityFilter != null && user.city != cityFilter) {
            return false
        }
        
        // 年龄范围过滤
        if (ageRange != null && user.age != null && user.age !in ageRange) {
            return false
        }
        
        return true
    }
    
    /**
     * 检查是否有活动的过滤条件
     */
    fun hasActiveFilters(): Boolean {
        return searchQuery.isNotBlank() || 
               onlineOnly || 
               cityFilter != null || 
               ageRange != null
    }
}
