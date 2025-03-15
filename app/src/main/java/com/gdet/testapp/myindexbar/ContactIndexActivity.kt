package com.gdet.testapp.myindexbar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.gdet.testapp.R

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contactindex)

        recyclerView = findViewById(R.id.recyclerView)
        indexBar = findViewById(R.id.indexBar)

        setupRecyclerView()
        setupIndexBar()
        loadContacts()
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
    }

    private fun findCurrentIndexForPosition(position: Int): String? {
        // 找到当前位置对应的索引
        val availableIndexes = contactAdapter.getAvailableIndexes().toList().sorted()

        for (i in availableIndexes.indices.reversed()) {
            val indexPosition = contactAdapter.getPositionForIndex(availableIndexes[i])
            if (indexPosition <= position) {
                return availableIndexes[i]
            }
        }

        return availableIndexes.firstOrNull()
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

    private fun loadContacts() {
        val contacts = listOf(
            Contact("阿里巴巴", "123456789"),
            Contact("Aron", "987654321"),
            Contact("鲍勃", "555555555"),
            Contact("Bill", "444444444"),
            Contact("曹操", "333333333"),
            Contact("单田芳", "111222333"),  // "单"是多音字，应该归为S组
            Contact("单纯", "333222111"),    // 另一个"单"的例子
            Contact("仇人", "444555666"),    // "仇"是多音字，应该归为Q组
            Contact("曾国藩", "666777888"),  // "曾"是多音字，应该归为Z组
            Contact("1号联系人", "111111111"),
            Contact("@特殊符号", "000000000"),
            Contact("David", "222222222"),
            Contact("丁丁", "999999999"),
            Contact("Emma", "888888888"),
            Contact("方方", "777777777"),
            Contact("高高", "666666666"),
            Contact("黄河", "123123123"),
            Contact("Jack", "456456456"),
            Contact("Kevin", "789789789"),
            Contact("李四", "321321321"),
            Contact("Mary", "654654654"),
            Contact("牛牛", "987987987"),
            Contact("欧阳", "135792468"),
            Contact("朴树", "246813579"),    // "朴"是多音字，应该归为P组
            Contact("钱钱", "112233445"),
            Contact("沈阳", "556677889"),    // "沈"是多音字，应该归为S组
            Contact("Tom", "998877665"),
            Contact("王五", "554433221"),
            Contact("小明", "112233445"),
            Contact("杨过", "556677889"),
            Contact("张三", "998877665")
        )

        contactAdapter.updateContacts(contacts)

        // 更新索引条的可用索引
        val allIndexes = "ABCDEFGHIJKLMNOPQRSTUVWXYZ#".map { it.toString() }.toSet()
        val unavailableIndexes = allIndexes - contactAdapter.getAvailableIndexes()
        indexBar.setDisabledIndexes(unavailableIndexes)
    }
}