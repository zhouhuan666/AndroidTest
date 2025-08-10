package com.gdet.testapp.hilt.di.modules

import android.util.Log
import com.gdet.testapp.hilt.data.local.DatabaseService
import com.gdet.testapp.hilt.data.local.LocalDatabaseService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * 数据库模块
 * 演示如何在Module中配置数据库相关的依赖
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DatabaseModule {
    
    companion object {
        private const val TAG = "HiltDatabaseModule"
    }
    
    /**
     * 绑定数据库服务实现
     * LocalDatabaseService会自动注入Context（通过@ApplicationContext）
     */
    @Binds
    @Singleton
    abstract fun bindDatabaseService(localDatabaseService: LocalDatabaseService): DatabaseService
}