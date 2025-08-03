package com.gdet.testapp.mvi.complete

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.gdet.testapp.databinding.ItemUserBinding
import com.gdet.testapp.mvi.complete.data.User

/**
 * 用户列表适配器
 * 
 * 使用ListAdapter和DiffUtil来高效处理列表更新
 * 支持多选模式和各种用户操作
 */
class UserListAdapter(
    private val onUserClick: (User) -> Unit,
    private val onUserLongClick: (User) -> Unit,
    private val onEditClick: (User) -> Unit,
    private val onDeleteClick: (User) -> Unit
) : ListAdapter<User, UserListAdapter.UserViewHolder>(UserDiffCallback()) {
    
    private var selectedUsers: Set<Long> = emptySet()
    private var isMultiSelectMode: Boolean = false
    
    /**
     * 提交新的列表数据
     */
    fun submitList(
        users: List<User>,
        selectedUserIds: Set<Long>,
        multiSelectMode: Boolean
    ) {
        selectedUsers = selectedUserIds
        isMultiSelectMode = multiSelectMode
        submitList(users)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val binding = ItemUserBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return UserViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
    
    /**
     * ViewHolder
     */
    inner class UserViewHolder(
        private val binding: ItemUserBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        fun bind(user: User) {
            // 绑定用户数据
            binding.tvUserName.text = user.getDisplayName()
            binding.tvUserEmail.text = user.email
            
            // 年龄信息
            if (user.age != null) {
                binding.tvUserAge.text = "${user.age}岁"
                binding.tvUserAge.visibility = View.VISIBLE
            } else {
                binding.tvUserAge.visibility = View.GONE
            }
            
            // 城市信息
            if (user.city != null) {
                binding.tvUserCity.text = user.city
                binding.tvUserCity.visibility = View.VISIBLE
            } else {
                binding.tvUserCity.visibility = View.GONE
            }
            
            // 在线状态
            binding.tvOnlineStatus.text = user.getStatusText()
            binding.tvOnlineStatus.setTextColor(
                if (user.isOnline) {
                    binding.root.context.getColor(android.R.color.holo_green_dark)
                } else {
                    binding.root.context.getColor(android.R.color.darker_gray)
                }
            )
            
            binding.viewOnlineStatus.backgroundTintList = 
                android.content.res.ColorStateList.valueOf(
                    if (user.isOnline) {
                        binding.root.context.getColor(android.R.color.holo_green_light)
                    } else {
                        binding.root.context.getColor(android.R.color.darker_gray)
                    }
                )
            
            // 头像 - 这里使用默认图标，实际项目中可以使用图片加载库
            binding.ivAvatar.setImageResource(com.gdet.testapp.R.drawable.ic_user_placeholder)
            
            // 多选模式
            binding.checkboxSelect.visibility = if (isMultiSelectMode) View.VISIBLE else View.GONE
            binding.checkboxSelect.isChecked = selectedUsers.contains(user.id)
            
            // 选中状态覆盖层
            binding.viewSelectedOverlay.visibility = 
                if (isMultiSelectMode && selectedUsers.contains(user.id)) View.VISIBLE else View.GONE
            
            // 操作按钮
            binding.layoutActions.visibility = if (isMultiSelectMode) View.GONE else View.VISIBLE
            
            // 设置点击监听器
            binding.root.setOnClickListener {
                onUserClick(user)
            }
            
            binding.root.setOnLongClickListener {
                onUserLongClick(user)
                true
            }
            
            binding.checkboxSelect.setOnClickListener {
                onUserClick(user) // 复用点击逻辑
            }
            
            binding.btnEdit.setOnClickListener {
                onEditClick(user)
            }
            
            binding.btnDelete.setOnClickListener {
                onDeleteClick(user)
            }
            
            // 加载状态 - 这里可以根据需要显示单个用户的操作状态
            binding.progressBar.visibility = View.GONE
        }
    }
    
    /**
     * DiffUtil回调，用于高效更新列表
     */
    class UserDiffCallback : DiffUtil.ItemCallback<User>() {
        override fun areItemsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem.id == newItem.id
        }
        
        override fun areContentsTheSame(oldItem: User, newItem: User): Boolean {
            return oldItem == newItem
        }
    }
}
