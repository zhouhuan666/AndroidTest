package com.gdet.testapp.hilt.presentation

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gdet.testapp.R
import com.gdet.testapp.hilt.data.models.UserPreference
import com.gdet.testapp.hilt.presentation.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Hilt演示Activity
 * 
 * 关键点：
 * 1. @AndroidEntryPoint 注解：告诉Hilt这个Activity需要依赖注入
 * 2. viewModels() 委托：自动获取Hilt注入的ViewModel
 * 3. @Inject 字段注入：演示如何在Activity中直接注入依赖
 * 4. StateFlow 订阅：响应式UI更新
 */
@AndroidEntryPoint
class HiltDemoActivity : AppCompatActivity() {
    
    companion object {
        private const val TAG = "HiltDemoActivity"
    }
    
    /**
     * 使用Hilt的ViewModel注入
     * by viewModels() 会自动获取Hilt容器中的ViewModel实例
     */
    private val viewModel: UserViewModel by viewModels()
    
    /**
     * 演示字段注入（Field Injection）
     * 虽然构造函数注入是首选，但有时在Android组件中需要使用字段注入
     * 注意：字段注入的依赖必须在onCreate()之后才能使用
     */
    @Inject
    lateinit var appVersion: String
    
    // UI组件
    private lateinit var progressBar: ProgressBar
    private lateinit var tvError: TextView
    private lateinit var btnLoadUsers: Button
    private lateinit var btnSelectUser: Button
    private lateinit var tvUserList: TextView
    private lateinit var tvSelectedUser: TextView
    private lateinit var tvUserPreference: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_hilt_demo)
        
        Log.d(TAG, "HiltDemoActivity onCreate()")
        Log.d(TAG, "Hilt注入的ViewModel: ${viewModel.javaClass.simpleName}")
        Log.d(TAG, "Hilt注入的应用版本: $appVersion")
        
        initViews()
        setupClickListeners()
        observeViewModel()
        
        // 添加Fragment
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HiltDemoFragment())
                .commit()
        }
        
        Log.d(TAG, "Activity初始化完成，开始观察ViewModel状态")
    }
    
    /**
     * 初始化Views
     */
    private fun initViews() {
        progressBar = findViewById(R.id.progressBar)
        tvError = findViewById(R.id.tvError)
        btnLoadUsers = findViewById(R.id.btnLoadUsers)
        btnSelectUser = findViewById(R.id.btnSelectUser)
        tvUserList = findViewById(R.id.tvUserList)
        tvSelectedUser = findViewById(R.id.tvSelectedUser)
        tvUserPreference = findViewById(R.id.tvUserPreference)
        
        Log.d(TAG, "Views初始化完成")
    }
    
    /**
     * 设置点击监听器
     */
    private fun setupClickListeners() {
        btnLoadUsers.setOnClickListener {
            Log.d(TAG, "用户点击 - 加载用户列表")
            viewModel.loadUsers()
        }
        
        btnSelectUser.setOnClickListener {
            Log.d(TAG, "用户点击 - 选择用户1")
            viewModel.selectUser(1)
        }
        
        Log.d(TAG, "点击监听器设置完成")
    }
    
    /**
     * 观察ViewModel状态
     * 演示如何在Activity中响应ViewModel的状态变化
     */
    private fun observeViewModel() {
        Log.d(TAG, "开始观察ViewModel状态变化")
        
        // 观察UI状态
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { uiState ->
                    Log.d(TAG, "UI状态更新: isLoading=${uiState.isLoading}, error=${uiState.error}")
                    
                    // 更新加载状态
                    progressBar.visibility = if (uiState.isLoading) View.VISIBLE else View.GONE
                    
                    // 更新错误信息
                    if (uiState.error != null) {
                        tvError.text = "错误: ${uiState.error}"
                        tvError.visibility = View.VISIBLE
                        Log.e(TAG, "显示错误: ${uiState.error}")
                    } else {
                        tvError.visibility = View.GONE
                    }
                }
            }
        }
        
        // 观察用户列表
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.users.collect { users ->
                    Log.d(TAG, "用户列表更新，共${users.size}个用户")
                    
                    if (users.isNotEmpty()) {
                        val userListText = users.joinToString("\n") { user ->
                            "ID: ${user.id}, 姓名: ${user.name}, 邮箱: ${user.email}, 年龄: ${user.age}"
                        }
                        tvUserList.text = userListText
                        Log.d(TAG, "用户列表显示更新完成")
                    } else {
                        tvUserList.text = "暂无用户数据"
                    }
                }
            }
        }
        
        // 观察选中用户
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.selectedUser.collect { user ->
                    if (user != null) {
                        Log.d(TAG, "选中用户更新: $user")
                        tvSelectedUser.text = """
                            ID: ${user.id}
                            姓名: ${user.name}
                            邮箱: ${user.email}
                            年龄: ${user.age}
                        """.trimIndent()
                    } else {
                        tvSelectedUser.text = "暂无选中用户"
                        Log.d(TAG, "清除选中用户")
                    }
                }
            }
        }
        
        // 观察用户偏好设置
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.userPreference.collect { preference ->
                    if (preference != null) {
                        Log.d(TAG, "用户偏好设置更新: $preference")
                        tvUserPreference.text = """
                            用户ID: ${preference.userId}
                            主题: ${preference.theme}
                            语言: ${preference.language}
                            通知: ${if (preference.notifications) "开启" else "关闭"}
                        """.trimIndent()
                    } else {
                        tvUserPreference.text = "暂无偏好设置"
                        Log.d(TAG, "清除用户偏好设置")
                    }
                }
            }
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "HiltDemoActivity onDestroy() - 生命周期结束")
    }
}