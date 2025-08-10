package com.gdet.testapp.hilt.di.modules

import android.util.Log
import com.gdet.testapp.hilt.di.qualifiers.DefaultDispatcher
import com.gdet.testapp.hilt.di.qualifiers.IoDispatcher
import com.gdet.testapp.hilt.di.qualifiers.MainDispatcher
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.Dispatchers
import javax.inject.Singleton

/**
 * 协程模块
 * 演示如何提供协程调度器
 */
@Module
@InstallIn(SingletonComponent::class)
object CoroutineModule {
    
    private const val TAG = "HiltCoroutineModule"
    
    /**
     * 提供IO线程调度器
     * 用于IO密集型操作，如网络请求、数据库操作
     */
    @Provides
    @Singleton
    @IoDispatcher
    fun provideIoDispatcher(): CoroutineDispatcher {
        Log.d(TAG, "提供IO调度器 - Dispatchers.IO")
        return Dispatchers.IO
    }
    
    /**
     * 提供Main线程调度器
     * 用于UI操作
     */
    @Provides
    @Singleton
    @MainDispatcher
    fun provideMainDispatcher(): CoroutineDispatcher {
        Log.d(TAG, "提供Main调度器 - Dispatchers.Main")
        return Dispatchers.Main
    }
    
    /**
     * 提供Default线程调度器
     * 用于CPU密集型操作
     */
    @Provides
    @Singleton
    @DefaultDispatcher
    fun provideDefaultDispatcher(): CoroutineDispatcher {
        Log.d(TAG, "提供Default调度器 - Dispatchers.Default")
        return Dispatchers.Default
    }
}