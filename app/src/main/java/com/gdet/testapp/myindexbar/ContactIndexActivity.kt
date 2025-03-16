package com.gdet.testapp.myindexbar

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gdet.testapp.R
import kotlinx.coroutines.launch

/**
 *
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2025-03-15
 * 描述：
 *
 */
class ContactIndexActivity : AppCompatActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var indexBar: IndexBar
    private lateinit var contactAdapter: ContactAdapter

    private val viewModel: ContactViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactindex)

        recyclerView = findViewById(R.id.recyclerView)
        indexBar = findViewById(R.id.indexBar)

        setupRecyclerView()
        setupIndexBar()
        observeViewModel()

        // 加载联系人数据
        viewModel.loadContacts()
    }

    private fun setupRecyclerView() {
        contactAdapter = ContactAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@ContactIndexActivity)
            adapter = contactAdapter

            // 监听滚动以更新索引条高亮
            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)

                    val layoutManager = recyclerView.layoutManager as LinearLayoutManager
                    val firstVisibleItemPosition = layoutManager.findFirstVisibleItemPosition()

                    // 找到当前可见项对应的索引
                    val currentIndex = findCurrentIndexForPosition(firstVisibleItemPosition)
                    currentIndex?.let {
                        indexBar.setCurrentIndex(it)
                    }
                }
            })
        }

        // 设置联系人点击事件
        contactAdapter.setOnContactClickListener { contact ->
            if (contact.hasPhoneNumber()) {
                // 显示联系人详情对话框
                val dialog = ContactDetailDialog.newInstance(contact)
                dialog.show(supportFragmentManager, "ContactDetailDialog")
            }
        }
    }

    private fun findCurrentIndexForPosition(position: Int): String? {
        if (position < 0) return null

        // 找到当前位置对应的索引
        val item = contactAdapter.getItemAtPosition(position)
        return when (item) {
            is ContactAdapter.Item.Header -> item.letter
            is ContactAdapter.Item.Contact -> {
                // 向上查找最近的标题
                var currentPos = position
                while (currentPos >= 0) {
                    val currentItem = contactAdapter.getItemAtPosition(currentPos)
                    if (currentItem is ContactAdapter.Item.Header) {
                        return currentItem.letter
                    }
                    currentPos--
                }
                null
            }
            null -> null
        }
    }

    private fun setupIndexBar() {
        // 设置索引选中回调（用于滚动列表）
        indexBar.onIndexSelectedListener = { index ->
            val position = contactAdapter.getPositionForIndex(index)
            if (position >= 0) {
                (recyclerView.layoutManager as LinearLayoutManager).scrollToPositionWithOffset(position, 0)
            }
        }
    }

    private fun observeViewModel() {
        // 观察分组后的联系人数据
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.groupedContacts.collect { groupedContacts ->
                    contactAdapter.submitList(groupedContacts)
                }
            }
        }

        // 观察可用索引
        viewModel.availableIndexes.observe(this) { availableIndexes ->
            val allIndexes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#".map { it.toString() }.toSet()
            val unavailableIndexes = allIndexes - availableIndexes
            indexBar.setDisabledIndexes(unavailableIndexes)
        }
    }
}