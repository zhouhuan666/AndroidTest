package com.gdet.testapp.mvi.complete

import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gdet.testapp.R
import com.gdet.testapp.databinding.ActivityUserListBinding
import com.gdet.testapp.mvi.complete.data.User
import com.gdet.testapp.mvi.complete.data.UserFilter
import com.gdet.testapp.mvi.complete.data.MockUserApiService
import com.gdet.testapp.mvi.complete.utils.MviLogger
import kotlinx.coroutines.launch

/**
 * 用户列表Activity - 完整的MVI架构示例
 * 
 * 这个Activity展示了MVI架构在复杂界面中的应用：
 * 1. 复杂的状态管理（加载、搜索、过滤、多选等）
 * 2. 异步操作处理（网络请求、数据库操作）
 * 3. 用户交互处理（搜索、过滤、CRUD操作）
 * 4. 错误处理和用户反馈
 * 
 * MVI架构的优势在复杂界面中更加明显：
 * - 状态集中管理，避免状态不一致
 * - 单向数据流，便于调试和维护
 * - 可预测的状态变化
 * - 易于测试和扩展
 */
class UserListActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "UserListActivity"
    }

    // ViewBinding
    private lateinit var binding: ActivityUserListBinding

    // ViewModel
    private val viewModel: UserListViewModel by viewModels {
        UserListViewModelFactory(this)
    }
    
    // RecyclerView适配器
    private lateinit var userAdapter: UserListAdapter
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        MviLogger.logLifecycle(TAG, "onCreate")

        // 初始化ViewBinding
        binding = ActivityUserListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置Toolbar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.title = "用户列表 - MVI示例"

        // 初始化UI
        setupRecyclerView()
        setupClickListeners()
        setupSearchView()

        // 观察状态变化
        observeState()

        // 初始加载数据
        MviLogger.logUserAction(TAG, "初始加载数据")
        viewModel.handleIntent(UserListIntent.LoadInitialUsers)
    }
    
    /**
     * 设置RecyclerView
     */
    private fun setupRecyclerView() {
        userAdapter = UserListAdapter(
            onUserClick = { user ->
                // 用户点击事件
                if (getCurrentState().isMultiSelectMode) {
                    viewModel.handleIntent(UserListIntent.ToggleUserSelection(user))
                } else {
                    viewModel.handleIntent(UserListIntent.SelectUser(user))
                    viewModel.handleIntent(UserListIntent.ShowUserDetails(user.id))
                }
            },
            onUserLongClick = { user ->
                // 长按进入多选模式
                if (!getCurrentState().isMultiSelectMode) {
                    viewModel.handleIntent(UserListIntent.ToggleMultiSelectMode(true))
                    viewModel.handleIntent(UserListIntent.ToggleUserSelection(user))
                }
            },
            onEditClick = { user ->
                // 编辑用户
                editUser(user)
            },
            onDeleteClick = { user ->
                // 删除用户
                confirmDeleteUser(user)
            }
        )
        
        binding.recyclerViewUsers.apply {
            layoutManager = LinearLayoutManager(this@UserListActivity)
            adapter = userAdapter
            
            // 添加滚动监听，实现分页加载
            addOnScrollListener(object : androidx.recyclerview.widget.RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: androidx.recyclerview.widget.RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    
                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val visibleItemCount = layoutManager.childCount
                    val totalItemCount = layoutManager.itemCount
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()
                    
                    // 当滚动到底部时加载更多
                    if (visibleItemCount + firstVisibleItemPosition >= totalItemCount - 3) {
                        val state = getCurrentState()
                        if (state.canLoadMore()) {
                            viewModel.handleIntent(UserListIntent.LoadMoreUsers)
                        }
                    }
                }
            })
        }
    }
    
    /**
     * 设置点击监听器
     */
    private fun setupClickListeners() {
        // 下拉刷新
        binding.swipeRefreshLayout.setOnRefreshListener {
            viewModel.handleIntent(UserListIntent.RefreshUsers)
        }
        
        // 过滤按钮
        binding.btnFilter.setOnClickListener {
            showFilterDialog()
        }
        
        // 排序按钮
        binding.btnSort.setOnClickListener {
            showSortDialog()
        }
        
        // 视图模式切换
        binding.btnViewMode.setOnClickListener {
            val currentState = getCurrentState()
            viewModel.handleIntent(UserListIntent.ToggleViewMode(!currentState.isGridMode))
        }
        
        // 多选操作
        binding.btnSelectAll.setOnClickListener {
            val currentState = getCurrentState()
            viewModel.handleIntent(UserListIntent.SelectAllUsers(!currentState.isAllSelected()))
        }
        
        binding.btnDeleteSelected.setOnClickListener {
            val selectedIds = getCurrentState().selectedUsers.toList()
            if (selectedIds.isNotEmpty()) {
                confirmDeleteUsers(selectedIds)
            }
        }
        
        binding.btnCancelMultiSelect.setOnClickListener {
            viewModel.handleIntent(UserListIntent.ToggleMultiSelectMode(false))
        }
        
        // 添加用户
        binding.fabAddUser.setOnClickListener {
            addNewUser()
        }
        
        // 重试按钮
        binding.btnRetry.setOnClickListener {
            viewModel.handleIntent(UserListIntent.RetryLoad)
        }
    }
    
    /**
     * 设置搜索框
     */
    private fun setupSearchView() {
        binding.searchView.setOnQueryTextListener(object : androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.handleIntent(UserListIntent.SearchUsers(it))
                }
                return true
            }
            
            override fun onQueryTextChange(newText: String?): Boolean {
                newText?.let {
                    if (it.isEmpty()) {
                        viewModel.handleIntent(UserListIntent.ClearSearch)
                    } else if (it.length >= 2) { // 至少2个字符才开始搜索
                        viewModel.handleIntent(UserListIntent.SearchUsers(it))
                    }
                }
                return true
            }
        })
    }
    
    /**
     * 观察状态变化
     */
    private fun observeState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                renderState(state)
            }
        }
    }
    
    /**
     * 根据状态渲染UI
     */
    private fun renderState(state: UserListState) {
        // 更新用户列表
        userAdapter.submitList(state.displayUsers, state.selectedUsers, state.isMultiSelectMode)
        
        // 更新加载状态
        binding.swipeRefreshLayout.isRefreshing = state.isRefreshing
        binding.layoutLoading.visibility = if (state.isLoading) View.VISIBLE else View.GONE
        binding.layoutLoadMore.visibility = if (state.isLoadingMore) View.VISIBLE else View.GONE
        
        // 更新状态文本
        binding.tvStatus.text = state.getStatusText()
        
        // 更新多选状态
        binding.layoutMultiSelectActions.visibility = if (state.isMultiSelectMode) View.VISIBLE else View.GONE
        binding.tvMultiSelectStatus.apply {
            if (state.isMultiSelectMode) {
                text = state.getMultiSelectStatusText()
                visibility = View.VISIBLE
            } else {
                visibility = View.GONE
            }
        }
        
        // 更新按钮状态
        binding.btnSelectAll.text = if (state.isAllSelected()) "取消全选" else "全选"
        binding.btnDeleteSelected.isEnabled = state.hasSelectedUsers()
        
        // 更新视图模式图标
        binding.btnViewMode.setImageResource(
            if (state.isGridMode) R.drawable.ic_view_list else R.drawable.ic_view_list
        )
        
        // 更新空状态
        binding.layoutEmpty.visibility = if (state.isEmpty()) View.VISIBLE else View.GONE
        if (state.isEmpty()) {
            binding.tvEmptyMessage.text = when {
                state.isSearchMode -> "未找到相关用户"
                state.hasActiveFilters() -> "没有符合条件的用户"
                else -> "暂无用户数据"
            }
            binding.btnRetry.visibility = if (state.hasError()) View.VISIBLE else View.GONE
        }
        
        // 显示错误信息
        if (state.showErrorDialog && state.errorMessage != null) {
            showErrorDialog(state.errorMessage)
        }
        
        // 显示成功信息
        if (state.showSuccessMessage && state.successMessage != null) {
            Toast.makeText(this, state.successMessage, Toast.LENGTH_SHORT).show()
            viewModel.handleIntent(UserListIntent.HideError) // 清除消息
        }
    }
    
    /**
     * 获取当前状态
     */
    private fun getCurrentState(): UserListState = viewModel.getCurrentState()
    
    /**
     * 显示过滤对话框
     */
    private fun showFilterDialog() {
        // 这里可以实现过滤对话框
        Toast.makeText(this, "过滤功能待实现", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 显示排序对话框
     */
    private fun showSortDialog() {
        // 这里可以实现排序对话框
        Toast.makeText(this, "排序功能待实现", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 添加新用户
     */
    private fun addNewUser() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_user, null)

        val etUserName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUserName)
        val etUserEmail = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUserEmail)
        val etUserAge = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUserAge)
        val etUserCity = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etUserCity)
        val switchOnlineStatus = dialogView.findViewById<com.google.android.material.switchmaterial.SwitchMaterial>(R.id.switchOnlineStatus)
        val btnCancel = dialogView.findViewById<android.widget.Button>(R.id.btnCancel)
        val btnConfirm = dialogView.findViewById<android.widget.Button>(R.id.btnConfirm)

        val dialog = androidx.appcompat.app.AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        btnConfirm.setOnClickListener {
            val name = etUserName.text.toString().trim()
            val email = etUserEmail.text.toString().trim()
            val ageText = etUserAge.text.toString().trim()
            val city = etUserCity.text.toString().trim()
            val isOnline = switchOnlineStatus.isChecked

            // 验证输入
            when {
                name.isEmpty() -> {
                    etUserName.error = "请输入用户名"
                    etUserName.requestFocus()
                }
                email.isEmpty() -> {
                    etUserEmail.error = "请输入邮箱"
                    etUserEmail.requestFocus()
                }
                !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches() -> {
                    etUserEmail.error = "请输入有效的邮箱地址"
                    etUserEmail.requestFocus()
                }
                else -> {
                    // 创建新用户
                    val age = if (ageText.isNotEmpty()) {
                        try {
                            ageText.toInt()
                        } catch (e: NumberFormatException) {
                            null
                        }
                    } else null

                    val newUser = User(
                        id = 0, // ID将由服务器生成
                        name = name,
                        email = email,
                        age = age,
                        city = city.ifEmpty { null },
                        isOnline = isOnline
                    )

                    // 发送创建用户的Intent
                    viewModel.handleIntent(UserListIntent.CreateUser(newUser))
                    dialog.dismiss()
                }
            }
        }

        dialog.show()
    }
    
    /**
     * 编辑用户
     */
    private fun editUser(user: User) {
        // 这里可以实现编辑用户对话框
        Toast.makeText(this, "编辑用户: ${user.name}", Toast.LENGTH_SHORT).show()
    }
    
    /**
     * 确认删除单个用户
     */
    private fun confirmDeleteUser(user: User) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("删除用户")
            .setMessage("确定要删除用户 ${user.name} 吗？")
            .setPositiveButton("删除") { _, _ ->
                viewModel.handleIntent(UserListIntent.DeleteUser(user.id))
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /**
     * 确认删除多个用户
     */
    private fun confirmDeleteUsers(userIds: List<Long>) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("批量删除")
            .setMessage("确定要删除选中的 ${userIds.size} 个用户吗？")
            .setPositiveButton("删除") { _, _ ->
                viewModel.handleIntent(UserListIntent.DeleteUsers(userIds))
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /**
     * 显示错误对话框
     */
    private fun showErrorDialog(message: String) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("错误")
            .setMessage(message)
            .setPositiveButton("确定") { _, _ ->
                viewModel.handleIntent(UserListIntent.HideError)
            }
            .show()
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_user_list, menu)

        // 更新网络状态菜单项的标题
        menu?.findItem(R.id.action_toggle_network)?.let { networkMenuItem ->
            updateNetworkMenuTitle(networkMenuItem)
        }

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_clear_cache -> {
                viewModel.handleIntent(UserListIntent.ClearCache)
                true
            }
            R.id.action_toggle_network -> {
                toggleNetworkStatus(item)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    /**
     * 切换网络状态
     */
    private fun toggleNetworkStatus(menuItem: MenuItem) {
        val currentStatus = MockUserApiService.isNetworkAvailable()
        val newStatus = !currentStatus

        MockUserApiService.setNetworkAvailable(newStatus)
        updateNetworkMenuTitle(menuItem)

        val statusText = if (newStatus) "网络已连接" else "网络已断开"
        val message = "$statusText - ${if (newStatus) "将使用网络数据" else "将使用本地缓存数据"}"

        MviLogger.logUserAction(TAG, "切换网络状态", "新状态: $statusText")
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

        // 如果网络恢复，可以选择刷新数据
        if (newStatus) {
            androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("网络已恢复")
                .setMessage("是否要刷新数据以获取最新内容？")
                .setPositiveButton("刷新") { _, _ ->
                    MviLogger.logUserAction(TAG, "网络恢复后刷新数据")
                    viewModel.handleIntent(UserListIntent.RefreshUsers)
                }
                .setNegativeButton("稍后", null)
                .show()
        }
    }

    /**
     * 更新网络状态菜单项标题
     */
    private fun updateNetworkMenuTitle(menuItem: MenuItem) {
        val isNetworkAvailable = MockUserApiService.isNetworkAvailable()
        val title = if (isNetworkAvailable) "🌐 断开网络" else "📱 连接网络"
        val subtitle = if (isNetworkAvailable) "当前: 在线模式" else "当前: 离线模式"

        menuItem.title = title
        // 可以在这里设置副标题或图标
    }
}
