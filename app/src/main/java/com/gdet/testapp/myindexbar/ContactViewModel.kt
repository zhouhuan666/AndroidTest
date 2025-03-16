package com.gdet.testapp.myindexbar

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 *
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2025-03-16
 * 描述：
 *
 */
class ContactViewModel : ViewModel() {

    private val repository = ContactRepository()

    // 使用 StateFlow 代替 LiveData
    private val _contacts = MutableStateFlow<List<ContactData>>(emptyList())
    val contacts = _contacts.asStateFlow()

    //分组后的联系人数据
    private val _groupedContacts = MutableStateFlow<Map<String, List<ContactData>>>(emptyMap())
    val groupedContacts = _groupedContacts.asStateFlow()

    // 可用的索引
    private val _availableIndexes = MutableLiveData<Set<String>>()
    val availableIndexes: LiveData<Set<String>> = _availableIndexes

    // 当前选中的联系人
    private val _selectedContact = MutableLiveData<ContactData?>()
    val selectedContact: LiveData<ContactData?> = _selectedContact

    // 加载联系人数据
    fun loadContacts() {
        viewModelScope.launch {
            repository.getContacts().collect { contactList ->
                _contacts.value = contactList
                processContacts(contactList)
            }
        }
    }

    // 处理联系人数据，进行分组
    private fun processContacts(contactList: List<ContactData>) {
        val grouped = contactList.groupBy { contact ->
            getSortKey(contact.displayName)
        }

        // 确保按照A-Z#的顺序排列，将#组放在最后
        val sortedGroups = grouped.entries.sortedWith(compareBy {
            if (it.key == "#") "Z1" else it.key
        }).associate { it.key to sortContactsInGroup(it.value) }

        _groupedContacts.value = sortedGroups
        _availableIndexes.value = sortedGroups.keys
    }

    // 获取排序键
    private fun getSortKey(name: String): String {
        if (name.isEmpty()) return "#"

        val firstChar = name[0]
        return when {
            firstChar.toString().matches(Regex("[\\u4e00-\\u9fa5]")) -> {
                // 中文字符，使用多音字处理工具
                ChineseCharacterUtil.getFirstLetter(firstChar)
            }
            firstChar.toString().matches(Regex("[a-zA-Z]")) -> {
                // 英文字符
                firstChar.uppercase()
            }
            else -> "#"
        }
    }

    // 组内排序
    private fun sortContactsInGroup(contacts: List<ContactData>): List<ContactData> {
        return contacts.sortedWith(
            compareBy<ContactData> { contact ->
                val firstChar = contact.displayName.firstOrNull() ?: ' '
                when {
                    firstChar.isDigit() -> 1
                    firstChar.toString().matches(Regex("[\\u4e00-\\u9fa5]")) -> 2
                    firstChar.isLetter() -> 3
                    else -> 0 // 特殊字符
                }
            }.thenBy { it.displayName }
        )
    }

    // 选择联系人
    fun selectContact(contact: ContactData) {
        _selectedContact.value = contact
    }

    // 清除选中的联系人
    fun clearSelectedContact() {
        _selectedContact.value = null
    }

    // 获取联系人在列表中的位置
    fun getPositionForIndex(index: String): Int? {
        val groups = _groupedContacts.value ?: return null

        var position = 0
        for ((groupIndex, contactsInGroup) in groups.entries) {
            if (groupIndex == index) {
                return position
            }
            // 每个组有一个标题和多个联系人
            position += 1 + contactsInGroup.size
        }

        return null
    }
}