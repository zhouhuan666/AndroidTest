package com.gdet.testapp.mvi.complete.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.random.Random

/**
 * 应用数据库
 * 
 * 使用Room数据库框架管理本地数据存储
 * 在MVI架构中，数据库作为数据层的重要组成部分，
 * 提供持久化存储和离线数据访问能力
 */
@Database(
    entities = [User::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    
    /**
     * 获取用户DAO
     */
    abstract fun userDao(): UserDao
    
    companion object {
        private const val TAG = "AppDatabase"
        private const val DATABASE_NAME = "mvi_app_database"
        
        @Volatile
        private var INSTANCE: AppDatabase? = null
        
        /**
         * 获取数据库实例（单例模式）
         * 
         * @param context 应用上下文
         * @return 数据库实例
         */
        fun getDatabase(context: Context): AppDatabase {
            Log.d(TAG, "获取数据库实例")
            
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    DATABASE_NAME
                )
                .addCallback(DatabaseCallback())
                .build()
                
                Log.i(TAG, "数据库实例创建完成")
                INSTANCE = instance
                instance
            }
        }
        
        /**
         * 数据库回调
         * 用于在数据库创建时执行初始化操作
         */
        private class DatabaseCallback : RoomDatabase.Callback() {
            
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                Log.i(TAG, "数据库首次创建，开始初始化数据")
                
                // 在数据库创建后初始化数据
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        initializeDatabase(database.userDao())
                    }
                }
            }
            
            override fun onOpen(db: SupportSQLiteDatabase) {
                super.onOpen(db)
                Log.d(TAG, "数据库已打开")
                
                // 每次打开数据库时检查是否需要初始化数据
                INSTANCE?.let { database ->
                    CoroutineScope(Dispatchers.IO).launch {
                        checkAndInitializeData(database.userDao())
                    }
                }
            }
        }
        
        /**
         * 初始化数据库数据
         * 如果数据库为空，则插入35个初始用户数据
         */
        private suspend fun initializeDatabase(userDao: UserDao) {
            try {
                Log.i(TAG, "开始初始化数据库数据")
                
                val userCount = userDao.getUserCount()
                Log.d(TAG, "当前数据库中用户数量: $userCount")
                
                if (userCount == 0) {
                    Log.i(TAG, "数据库为空，开始插入初始数据")
                    
                    val initialUsers = generateInitialUsers()
                    val insertedIds = userDao.insertUsers(initialUsers)
                    
                    Log.i(TAG, "初始数据插入完成，插入了 ${insertedIds.size} 个用户")
                    Log.d(TAG, "插入的用户ID: $insertedIds")
                } else {
                    Log.d(TAG, "数据库已有数据，跳过初始化")
                }
            } catch (e: Exception) {
                Log.e(TAG, "初始化数据库数据失败", e)
            }
        }
        
        /**
         * 检查并初始化数据
         * 每次打开数据库时调用，确保有基础数据
         */
        private suspend fun checkAndInitializeData(userDao: UserDao) {
            try {
                val userCount = userDao.getUserCount()
                Log.d(TAG, "检查数据库，当前用户数量: $userCount")
                
                if (userCount == 0) {
                    Log.w(TAG, "数据库为空，重新初始化数据")
                    initializeDatabase(userDao)
                }
            } catch (e: Exception) {
                Log.e(TAG, "检查数据库数据失败", e)
            }
        }
        
        /**
         * 生成35个初始用户数据
         * 
         * @return 初始用户列表
         */
        private fun generateInitialUsers(): List<User> {
            Log.d(TAG, "开始生成35个初始用户数据")
            
            val cities = listOf("北京", "上海", "广州", "深圳", "杭州", "成都", "武汉", "西安", "南京", "重庆")
            val domains = listOf("gmail.com", "163.com", "qq.com", "sina.com", "outlook.com")
            val currentTime = System.currentTimeMillis()
            
            val users = mutableListOf<User>()
            
            repeat(35) { index ->
                val userId = index + 1
                val user = User(
                    id = 0, // Room会自动生成ID
                    name = "用户${String.format("%02d", userId)}",
                    email = "user${String.format("%02d", userId)}@${domains.random()}",
                    avatarUrl = "https://picsum.photos/200/200?random=$userId",
                    age = Random.nextInt(18, 65),
                    city = cities.random(),
                    isOnline = Random.nextBoolean(),
                    createdAt = currentTime - Random.nextLong(0, 365L * 24 * 60 * 60 * 1000), // 随机过去一年内的时间
                    updatedAt = currentTime
                )
                users.add(user)
                
                if ((index + 1) % 10 == 0) {
                    Log.d(TAG, "已生成 ${index + 1} 个用户数据")
                }
            }
            
            Log.i(TAG, "初始用户数据生成完成，共 ${users.size} 个用户")
            return users
        }
        
        /**
         * 清理数据库实例（用于测试）
         */
        fun clearInstance() {
            Log.d(TAG, "清理数据库实例")
            INSTANCE = null
        }
    }
}
