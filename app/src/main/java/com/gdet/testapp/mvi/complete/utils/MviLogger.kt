package com.gdet.testapp.mvi.complete.utils

import android.util.Log

/**
 * MVI架构专用日志工具类
 * 
 * 提供统一的日志管理，便于调试和监控MVI数据流
 * 支持不同级别的日志输出和格式化
 */
object MviLogger {
    
    private const val TAG_PREFIX = "MVI"
    private var isDebugMode = true // 在生产环境中设置为false
    
    /**
     * 设置调试模式
     */
    fun setDebugMode(enabled: Boolean) {
        isDebugMode = enabled
        Log.i("$TAG_PREFIX-Logger", "调试模式: ${if (enabled) "开启" else "关闭"}")
    }
    
    /**
     * Intent日志
     */
    fun logIntent(tag: String, intent: Any) {
        if (isDebugMode) {
            Log.d("$TAG_PREFIX-Intent", "[$tag] Intent: ${intent::class.simpleName}")
        }
    }
    
    /**
     * Action日志
     */
    fun logAction(tag: String, action: Any) {
        if (isDebugMode) {
            Log.d("$TAG_PREFIX-Action", "[$tag] Action: ${action::class.simpleName}")
        }
    }
    
    /**
     * State变化日志
     */
    fun logStateChange(tag: String, oldState: Any, newState: Any) {
        if (isDebugMode) {
            Log.d("$TAG_PREFIX-State", "[$tag] State变化:")
            Log.d("$TAG_PREFIX-State", "  旧状态: ${oldState.hashCode()}")
            Log.d("$TAG_PREFIX-State", "  新状态: ${newState.hashCode()}")
        }
    }
    
    /**
     * 详细的State变化日志
     */
    fun logDetailedStateChange(tag: String, changes: Map<String, Pair<Any?, Any?>>) {
        if (isDebugMode) {
            Log.d("$TAG_PREFIX-State", "[$tag] 详细状态变化:")
            changes.forEach { (field, change) ->
                Log.d("$TAG_PREFIX-State", "  $field: ${change.first} -> ${change.second}")
            }
        }
    }
    
    /**
     * 数据库操作日志
     */
    fun logDatabase(tag: String, operation: String, details: String = "") {
        if (isDebugMode) {
            Log.d("$TAG_PREFIX-DB", "[$tag] $operation${if (details.isNotEmpty()) ": $details" else ""}")
        }
    }
    
    /**
     * 网络请求日志
     */
    fun logNetwork(tag: String, operation: String, details: String = "") {
        if (isDebugMode) {
            Log.d("$TAG_PREFIX-Network", "[$tag] $operation${if (details.isNotEmpty()) ": $details" else ""}")
        }
    }
    
    /**
     * Repository操作日志
     */
    fun logRepository(tag: String, operation: String, details: String = "") {
        if (isDebugMode) {
            Log.d("$TAG_PREFIX-Repo", "[$tag] $operation${if (details.isNotEmpty()) ": $details" else ""}")
        }
    }
    
    /**
     * UI操作日志
     */
    fun logUI(tag: String, operation: String, details: String = "") {
        if (isDebugMode) {
            Log.d("$TAG_PREFIX-UI", "[$tag] $operation${if (details.isNotEmpty()) ": $details" else ""}")
        }
    }
    
    /**
     * 错误日志
     */
    fun logError(tag: String, message: String, throwable: Throwable? = null) {
        Log.e("$TAG_PREFIX-Error", "[$tag] $message", throwable)
    }
    
    /**
     * 警告日志
     */
    fun logWarning(tag: String, message: String) {
        Log.w("$TAG_PREFIX-Warning", "[$tag] $message")
    }
    
    /**
     * 信息日志
     */
    fun logInfo(tag: String, message: String) {
        Log.i("$TAG_PREFIX-Info", "[$tag] $message")
    }
    
    /**
     * 性能监控日志
     */
    fun logPerformance(tag: String, operation: String, duration: Long) {
        if (isDebugMode) {
            Log.d("$TAG_PREFIX-Perf", "[$tag] $operation 耗时: ${duration}ms")
        }
    }
    
    /**
     * 数据流日志
     */
    fun logDataFlow(tag: String, step: String, data: Any? = null) {
        if (isDebugMode) {
            val dataInfo = data?.let { " - 数据: ${it::class.simpleName}" } ?: ""
            Log.d("$TAG_PREFIX-Flow", "[$tag] $step$dataInfo")
        }
    }
    
    /**
     * 生命周期日志
     */
    fun logLifecycle(tag: String, event: String) {
        if (isDebugMode) {
            Log.d("$TAG_PREFIX-Lifecycle", "[$tag] $event")
        }
    }
    
    /**
     * 用户操作日志
     */
    fun logUserAction(tag: String, action: String, details: String = "") {
        if (isDebugMode) {
            Log.d("$TAG_PREFIX-User", "[$tag] 用户操作: $action${if (details.isNotEmpty()) " - $details" else ""}")
        }
    }
    
    /**
     * 缓存操作日志
     */
    fun logCache(tag: String, operation: String, details: String = "") {
        if (isDebugMode) {
            Log.d("$TAG_PREFIX-Cache", "[$tag] 缓存操作: $operation${if (details.isNotEmpty()) " - $details" else ""}")
        }
    }
    
    /**
     * 验证日志
     */
    fun logValidation(tag: String, field: String, result: Boolean, message: String = "") {
        if (isDebugMode) {
            val status = if (result) "通过" else "失败"
            Log.d("$TAG_PREFIX-Validation", "[$tag] 验证 $field: $status${if (message.isNotEmpty()) " - $message" else ""}")
        }
    }
    
    /**
     * 格式化对象信息
     */
    private fun formatObject(obj: Any?): String {
        return when (obj) {
            null -> "null"
            is String -> "\"$obj\""
            is Number -> obj.toString()
            is Boolean -> obj.toString()
            is List<*> -> "List(${obj.size})"
            is Map<*, *> -> "Map(${obj.size})"
            else -> "${obj::class.simpleName}@${obj.hashCode()}"
        }
    }
}
