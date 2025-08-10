package com.gdet.testapp.hilt.di.modules

import android.util.Log
import com.gdet.testapp.hilt.data.api.ApiService
import com.gdet.testapp.hilt.data.api.MockApiService
import com.gdet.testapp.hilt.data.api.RealApiService
import com.gdet.testapp.hilt.di.qualifiers.MockApi
import com.gdet.testapp.hilt.di.qualifiers.RealApi
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Singleton

/**
 * 网络模块 - 演示Hilt Module的使用
 * 
 * @Module: 告诉Hilt这是一个模块，用于提供依赖项
 * @InstallIn: 指定模块的生命周期和作用域
 * SingletonComponent: 表示这些依赖项在应用程序生命周期内是单例
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class NetworkModule {
    
    companion object {
        private const val TAG = "HiltNetworkModule"
        private const val BASE_URL = "https://api.example.com/"
        
        /**
         * 提供OkHttpClient实例
         * @Provides: 告诉Hilt如何创建这个类型的实例
         * @Singleton: 确保在整个应用生命周期中只有一个实例
         */
        @Provides
        @Singleton
        fun provideOkHttpClient(): OkHttpClient {
            Log.d(TAG, "创建OkHttpClient实例 - @Provides方法")
            
            return OkHttpClient.Builder()
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .writeTimeout(30, TimeUnit.SECONDS)
                .build()
        }
        
        /**
         * 提供Retrofit实例
         * Hilt会自动注入OkHttpClient参数
         */
        @Provides
        @Singleton
        fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
            Log.d(TAG, "创建Retrofit实例 - 依赖OkHttpClient")
            
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(okHttpClient)
                .addConverterFactory(GsonConverterFactory.create())
                .build()
        }
        
        /**
         * 提供应用程序版本信息
         * 演示如何提供简单类型的依赖
         */
        @Provides
        @Singleton
        fun provideAppVersion(): String {
            Log.d(TAG, "提供应用版本信息")
            return "1.0.0"
        }
    }
    
    /**
     * 绑定RealApiService到ApiService接口
     * @Binds: 用于绑定接口和实现类，比@Provides更高效
     * @RealApi: 使用Qualifier区分不同的实现
     */
    @Binds
    @RealApi
    abstract fun bindRealApiService(realApiService: RealApiService): ApiService
    
    /**
     * 绑定MockApiService到ApiService接口
     * 演示同一接口的不同实现
     */
    @Binds
    @MockApi
    abstract fun bindMockApiService(mockApiService: MockApiService): ApiService
}