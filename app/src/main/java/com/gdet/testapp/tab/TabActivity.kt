package com.gdet.testapp.tab

import android.graphics.Color
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.drake.brv.utils.linear
import com.drake.brv.utils.setup
import com.gdet.testapp.R
import com.gdet.testapp.databinding.ActivityTabBinding
import com.gdet.testapp.databinding.ItemTabBinding

/**
 *
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2024-12-07
 * 描述：
 *
 */
class TabActivity : AppCompatActivity(){
    private lateinit var binding:ActivityTabBinding
    private val fragments = mutableListOf<Fragment>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityTabBinding.inflate(layoutInflater)
        setContentView(binding.root)

//        initData()
//        initTabList()
    }

//    private fun initData() {
//        // 初始化Fragment
//        fragments.apply {
//            add(HomeFragment())
//            add(MessageFragment())
//            add(SettingsFragment())
//        }
//    }
//
//    private fun initTabList() {
//        // 准备Tab数据
//        val tabItems = listOf(
//            TabItem(1, "首页", R.drawable.ic_home, R.drawable.ic_home_selected, true),
//            TabItem(2, "消息", R.drawable.ic_message, R.drawable.ic_message_selected),
//            TabItem(3, "设置", R.drawable.ic_settings, R.drawable.ic_settings_selected)
//        )
//
//        // 设置RecyclerView
//        binding.rvTabs.linear() // BRV提供的便捷方法设置布局管理器
//        binding.rvTabs.setup {
//            addType<TabItem>(R.layout.item_tab)
//
//            // 绑定数据到视图
//            onBind {
//                val item = getModel<TabItem>()
//                val itemBinding = ItemTabBinding.bind(itemView)
//
//                // 设置选中状态样式
//                itemBinding.apply {
//                    ivIcon.setImageResource(if (item.isSelected) item.selectedIcon else item.icon)
//                    tvTitle.setTextColor(if (item.isSelected) Color.BLACK else Color.GRAY)
//                    root.setBackgroundColor(if (item.isSelected) Color.WHITE else Color.TRANSPARENT)
//                }
//            }
//
//            // 处理点击事件
//            onClick(R.layout.item_tab) {
//                val oldPosition = tabItems.indexOfFirst { it.isSelected }
//                val newPosition = modelPosition
//
//                if (oldPosition != newPosition) {
//                    // 更新选中状态
//                    tabItems[oldPosition].isSelected = false
//                    tabItems[newPosition].isSelected = true
//
//                    // 局部刷新，只更新变化的项
//                    notifyItemChanged(oldPosition, "updateSelection")
//                    notifyItemChanged(newPosition, "updateSelection")
//
//                    // 切换Fragment
//                    switchFragment(newPosition)
//                }
//            }
//
//            // 优化局部刷新
//            onPayload { position, _, payloads ->
//                if (payloads.contains("updateSelection")) {
//                    val item = getModel<TabItem>()
//                    val itemBinding = ItemTabBinding.bind(itemView)
//
//                    itemBinding.apply {
//                        ivIcon.setImageResource(if (item.isSelected) item.selectedIcon else item.icon)
//                        tvTitle.setTextColor(if (item.isSelected) Color.BLACK else Color.GRAY)
//                        root.setBackgroundColor(if (item.isSelected) Color.WHITE else Color.TRANSPARENT)
//                    }
//                }
//            }
//        }.models = tabItems
//
//        // 默认显示第一个Fragment
//        switchFragment(0)
//    }
//
//    private fun switchFragment(position: Int) {
//        supportFragmentManager.beginTransaction()
//            .replace(R.id.fragment_container, fragments[position])
//            .commit()
//    }

}

data class TabItem(
    val id: Int,
    val title: String,
    val icon: Int,
    val selectedIcon: Int,
    var isSelected: Boolean = false
)