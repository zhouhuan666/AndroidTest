package com.gdet.testapp.hilt.presentation

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.gdet.testapp.R
import com.gdet.testapp.hilt.data.api.ApiService
import com.gdet.testapp.hilt.data.local.DatabaseService
import com.gdet.testapp.hilt.data.models.UserPreference
import com.gdet.testapp.hilt.di.qualifiers.MockApi
import com.gdet.testapp.hilt.presentation.viewmodel.UserViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Hilt演示Fragment
 * 
 * 关键点：
 * 1. @AndroidEntryPoint 注解：告诉Hilt这个Fragment需要依赖注入
 * 2. 两种ViewModel获取方式：
 *    - viewModels(): Fragment自己的ViewModel
 *    - activityViewModels(): 与Activity共享的ViewModel
 * 3. @Inject 字段注入：在Fragment中注入依赖
 * 4. Fragment的生命周期与依赖注入
 */
@AndroidEntryPoint
class HiltDemoFragment : Fragment() {
    
    companion object {
        private const val TAG = "HiltDemoFragment"
    }
    
    /**
     * 与Activity共享的ViewModel
     * 使用activityViewModels()可以获取到Activity中相同的ViewModel实例
     * 这样Fragment和Activity可以共享状态
     */
    private val sharedViewModel: UserViewModel by activityViewModels()
    
    /**
     * Fragment自己的ViewModel（如果需要的话）
     * 这会创建一个Fragment作用域的ViewModel实例
     */
    // private val fragmentViewModel: UserViewModel by viewModels()
    
    /**
     * 演示在Fragment中注入不同的依赖
     * 这里注入Mock API服务，演示Qualifier的使用
     */
    @Inject
    @MockApi
    lateinit var mockApiService: ApiService
    
    /**
     * 注入数据库服务
     */
    @Inject
    lateinit var databaseService: DatabaseService
    
    /**
     * 注入应用版本信息
     */
    @Inject
    lateinit var appVersion: String
    
    // UI组件
    private lateinit var btnSavePreference: Button
    private lateinit var tvFragmentInfo: TextView
    
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "Fragment onAttach() - 开始生命周期")
    }
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "Fragment onCreateView()")
        return inflater.inflate(R.layout.fragment_hilt_demo, container, false)
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        Log.d(TAG, "Fragment onViewCreated()")
        Log.d(TAG, "Hilt注入的依赖信息:")
        Log.d(TAG, "  - SharedViewModel: ${sharedViewModel.javaClass.simpleName}")
        Log.d(TAG, "  - MockApiService: ${mockApiService.javaClass.simpleName}")
        Log.d(TAG, "  - DatabaseService: ${databaseService.javaClass.simpleName}")
        Log.d(TAG, "  - AppVersion: $appVersion")
        
        initViews(view)
        setupClickListeners()
        observeSharedViewModel()
        
        // 演示使用注入的Mock API服务
        demonstrateMockApiService()
    }
    
    /**
     * 初始化Views
     */
    private fun initViews(view: View) {
        btnSavePreference = view.findViewById(R.id.btnSavePreference)
        tvFragmentInfo = view.findViewById(R.id.tvFragmentInfo)
        
        // 更新Fragment信息显示
        tvFragmentInfo.text = """
            这是一个使用Hilt的Fragment示例
            
            注入的依赖:
            • MockApiService: ${mockApiService.javaClass.simpleName}
            • DatabaseService: ${databaseService.javaClass.simpleName}
            • AppVersion: $appVersion
            • SharedViewModel: 与Activity共享
            
            点击按钮保存默认偏好设置
        """.trimIndent()
        
        Log.d(TAG, "Fragment Views初始化完成")
    }
    
    /**
     * 设置点击监听器
     */
    private fun setupClickListeners() {
        btnSavePreference.setOnClickListener {
            Log.d(TAG, "用户点击 - 保存偏好设置")
            saveDefaultPreference()
        }
        
        Log.d(TAG, "Fragment点击监听器设置完成")
    }
    
    /**
     * 观察共享的ViewModel
     * 演示Fragment如何与Activity共享ViewModel状态
     */
    private fun observeSharedViewModel() {
        Log.d(TAG, "开始观察共享ViewModel状态")
        
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                sharedViewModel.selectedUser.collect { user ->
                    if (user != null) {
                        Log.d(TAG, "Fragment接收到选中用户变化: ${user.name}")
                        // Fragment可以根据选中的用户更新自己的UI
                        updateUIForSelectedUser(user.name)
                    }
                }
            }
        }
    }
    
    /**
     * 根据选中用户更新UI
     */
    private fun updateUIForSelectedUser(userName: String) {
        val updatedText = """
            这是一个使用Hilt的Fragment示例
            
            当前选中用户: $userName
            
            注入的依赖:
            • MockApiService: ${mockApiService.javaClass.simpleName}
            • DatabaseService: ${databaseService.javaClass.simpleName}
            • AppVersion: $appVersion
            • SharedViewModel: 与Activity共享
            
            点击按钮保存 $userName 的偏好设置
        """.trimIndent()
        
        tvFragmentInfo.text = updatedText
        Log.d(TAG, "Fragment UI已更新，当前用户: $userName")
    }
    
    /**
     * 保存默认偏好设置
     * 演示在Fragment中使用注入的依赖和共享ViewModel
     */
    private fun saveDefaultPreference() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                // 获取当前选中的用户ID，如果没有则使用默认值
                val userId = sharedViewModel.selectedUser.value?.id ?: 1
                
                Log.d(TAG, "为用户$userId 保存默认偏好设置")
                
                // 创建默认偏好设置
                val defaultPreference = UserPreference(
                    userId = userId,
                    theme = "Dark",
                    language = "zh-CN", 
                    notifications = true
                )
                
                // 使用共享ViewModel保存偏好设置
                sharedViewModel.saveUserPreference(defaultPreference)
                
                Log.d(TAG, "偏好设置保存完成: $defaultPreference")
                
                // 也可以直接使用注入的DatabaseService
                // databaseService.saveUserPreference(defaultPreference)
                
            } catch (e: Exception) {
                Log.e(TAG, "保存偏好设置失败", e)
            }
        }
    }
    
    /**
     * 演示使用注入的Mock API服务
     */
    private fun demonstrateMockApiService() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                Log.d(TAG, "演示Mock API服务的使用")
                
                // 使用Mock API获取用户列表
                val mockUsers = mockApiService.getUsers()
                Log.d(TAG, "Mock API返回的用户列表:")
                mockUsers.forEach { user ->
                    Log.d(TAG, "  Mock用户: $user")
                }
                
                // 演示获取特定用户
                val mockUser = mockApiService.getUserById(100)
                Log.d(TAG, "Mock API返回的特定用户: $mockUser")
                
            } catch (e: Exception) {
                Log.e(TAG, "使用Mock API失败", e)
            }
        }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "Fragment onDestroyView() - View被销毁")
    }
    
    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "Fragment onDetach() - 生命周期结束")
    }
}