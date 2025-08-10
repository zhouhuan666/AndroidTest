package com.gdet.testapp.hilt.di.qualifiers

import javax.inject.Qualifier

/**
 * Hilt Qualifier 注解示例
 * Qualifier用于区分同一类型的不同实现
 * 当有多个相同类型的依赖时，使用Qualifier来指定具体要注入哪一个
 */

/**
 * 真实API服务的Qualifier
 * 用于标识生产环境使用的API服务
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class RealApi

/**
 * 模拟API服务的Qualifier  
 * 用于标识测试环境使用的Mock API服务
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MockApi

/**
 * 本地数据源的Qualifier
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class LocalDataSource

/**
 * 远程数据源的Qualifier
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class RemoteDataSource

/**
 * 缓存数据源的Qualifier
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class CacheDataSource

/**
 * IO线程池的Qualifier
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class IoDispatcher

/**
 * Main线程池的Qualifier
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class MainDispatcher

/**
 * Default线程池的Qualifier
 */
@Qualifier
@Retention(AnnotationRetention.RUNTIME)
annotation class DefaultDispatcher