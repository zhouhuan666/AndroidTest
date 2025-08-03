package com.gdet.testapp.mvi.complete

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.gdet.testapp.mvi.complete.data.UserRepository
import com.gdet.testapp.mvi.complete.utils.MviLogger

/**
 * UserListViewModel的工厂类
 * 
 * 用于创建需要Context参数的ViewModel实例
 * 在MVI架构中，ViewModelFactory负责依赖注入和对象创建
 */
class UserListViewModelFactory(
    private val context: Context
) : ViewModelProvider.Factory {
    
    companion object {
        private const val TAG = "ViewModelFactory"
    }
    
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        MviLogger.logLifecycle(TAG, "创建ViewModel: ${modelClass.simpleName}")
        
        return when {
            modelClass.isAssignableFrom(UserListViewModel::class.java) -> {
                // 创建Repository实例
                val repository = UserRepository(context.applicationContext)
                MviLogger.logRepository(TAG, "创建Repository", "使用ApplicationContext")
                
                // 创建ViewModel实例
                UserListViewModel(repository) as T
            }
            else -> {
                MviLogger.logError(TAG, "未知的ViewModel类型: ${modelClass.simpleName}")
                throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
            }
        }
    }
}
