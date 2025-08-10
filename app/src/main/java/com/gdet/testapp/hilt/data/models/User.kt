package com.gdet.testapp.hilt.data.models

/**
 * 用户数据模型
 * 演示Hilt中如何处理数据类
 */
data class User(
    val id: Int,
    val name: String,
    val email: String,
    val age: Int
) {
    override fun toString(): String {
        return "User(id=$id, name='$name', email='$email', age=$age)"
    }
}

/**
 * 用户配置数据模型
 */
data class UserPreference(
    val userId: Int,
    val theme: String,
    val language: String,
    val notifications: Boolean
) {
    override fun toString(): String {
        return "UserPreference(userId=$userId, theme='$theme', language='$language', notifications=$notifications)"
    }
}