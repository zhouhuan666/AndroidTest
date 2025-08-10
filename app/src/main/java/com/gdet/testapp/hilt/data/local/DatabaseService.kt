package com.gdet.testapp.hilt.data.local

import android.content.Context
import android.util.Log
import com.gdet.testapp.hilt.data.models.UserPreference
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 数据库服务接口
 */
interface DatabaseService {
    fun saveUserPreference(preference: UserPreference)
    fun getUserPreference(userId: Int): UserPreference?
    fun deleteUserPreference(userId: Int): Boolean
}

/**
 * 数据库服务实现
 * 演示Context注入和@ApplicationContext qualifier的使用
 */
@Singleton
class LocalDatabaseService @Inject constructor(
    @ApplicationContext private val context: Context
) : DatabaseService {
    
    companion object {
        private const val TAG = "HiltDatabaseService"
        private const val PREFS_NAME = "user_preferences"
    }
    
    private val sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    init {
        Log.d(TAG, "LocalDatabaseService 被创建，Context: ${context.javaClass.simpleName}")
        Log.d(TAG, "SharedPreferences 初始化完成")
    }
    
    override fun saveUserPreference(preference: UserPreference) {
        Log.d(TAG, "保存用户偏好设置: $preference")
        
        with(sharedPreferences.edit()) {
            putString("theme_${preference.userId}", preference.theme)
            putString("language_${preference.userId}", preference.language)
            putBoolean("notifications_${preference.userId}", preference.notifications)
            apply()
        }
        
        Log.d(TAG, "用户偏好设置保存成功")
    }
    
    override fun getUserPreference(userId: Int): UserPreference? {
        Log.d(TAG, "获取用户偏好设置，用户ID: $userId")
        
        val theme = sharedPreferences.getString("theme_$userId", null)
        val language = sharedPreferences.getString("language_$userId", null)
        
        return if (theme != null && language != null) {
            val notifications = sharedPreferences.getBoolean("notifications_$userId", true)
            val preference = UserPreference(userId, theme, language, notifications)
            Log.d(TAG, "找到用户偏好设置: $preference")
            preference
        } else {
            Log.d(TAG, "未找到用户偏好设置，返回默认值")
            UserPreference(userId, "Light", "zh-CN", true)
        }
    }
    
    override fun deleteUserPreference(userId: Int): Boolean {
        Log.d(TAG, "删除用户偏好设置，用户ID: $userId")
        
        return try {
            with(sharedPreferences.edit()) {
                remove("theme_$userId")
                remove("language_$userId")
                remove("notifications_$userId")
                apply()
            }
            Log.d(TAG, "用户偏好设置删除成功")
            true
        } catch (e: Exception) {
            Log.e(TAG, "删除用户偏好设置失败", e)
            false
        }
    }
}