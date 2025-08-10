package com.gdet.testapp.hilt.di.modules

import com.gdet.testapp.hilt.domain.repository.UserRepository
import com.gdet.testapp.hilt.domain.repository.UserRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Repository模块
 * 演示如何在Hilt中配置Repository层的依赖
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    
    /**
     * 绑定UserRepository接口到具体实现
     * Hilt会自动处理UserRepositoryImpl的所有依赖注入
     */
    @Binds
    @Singleton
    abstract fun bindUserRepository(userRepositoryImpl: UserRepositoryImpl): UserRepository
}