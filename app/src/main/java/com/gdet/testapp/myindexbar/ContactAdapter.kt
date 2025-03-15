package com.gdet.testapp.myindexbar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
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
class ContactAdapter(private var contacts: List<Contact> = emptyList()) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CONTACT = 1
    }

    private var indexPositionMap = mutableMapOf<String, Int>()
    private var sectionHeaders = mutableListOf<String>()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_HEADER -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_header, parent, false)
                HeaderViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_contact, parent, false)
                ContactViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is HeaderViewHolder -> {
                val headerPosition = sectionHeaders.indexOfFirst {
                    indexPositionMap[it] == position
                }
                if (headerPosition != -1) {
                    holder.bind(sectionHeaders[headerPosition])
                }
            }
            is ContactViewHolder -> {
                // 找到实际的联系人位置（减去之前的header数量）
                var contactIndex = position
                for (header in sectionHeaders) {
                    val headerPos = indexPositionMap[header] ?: continue
                    if (headerPos < position) {
                        contactIndex--
                    }
                }

                if (contactIndex >= 0 && contactIndex < contacts.size) {
                    holder.bind(contacts[contactIndex])
                }
            }
        }
    }

    override fun getItemCount(): Int {
        return contacts.size + sectionHeaders.size
    }

    override fun getItemViewType(position: Int): Int {
        return if (indexPositionMap.values.contains(position)) {
            TYPE_HEADER
        } else {
            TYPE_CONTACT
        }
    }

    fun updateContacts(newContacts: List<Contact>) {
        // 按照排序规则对联系人进行分组和排序
        val groupedContacts = newContacts.groupBy { it.sortKey }

        // 确保按照A-Z#的顺序排列，将#组放在最后
        val sortedGroups = groupedContacts.entries.sortedWith(compareBy {
            if (it.key == "#") "Z1" else it.key // 使#排在Z之后
        })

        // 对每组内的联系人进行排序
        val sortedContacts = mutableListOf<Contact>()
        val availableIndexes = mutableSetOf<String>()

        indexPositionMap.clear()
        sectionHeaders.clear()

        var currentPosition = 0

        for ((index, contactsInGroup) in sortedGroups) {
            if (contactsInGroup.isNotEmpty()) {
                availableIndexes.add(index)
                sectionHeaders.add(index)
                indexPositionMap[index] = currentPosition
                currentPosition++

                // 组内排序：先按首字符类型（特殊符号、数字、中文、英文），再按Unicode
                val sortedGroupContacts = contactsInGroup.sortedWith(
                    compareBy<Contact> { contact ->
                        val firstChar = contact.name.firstOrNull() ?: ' '
                        when {
                            firstChar.isDigit() -> 1
                            firstChar.toString().matches(Regex("[\\u4e00-\\u9fa5]")) -> 2
                            firstChar.isLetter() -> 3
                            else -> 0 // 特殊字符
                        }
                    }.thenBy { it.inGroupSortKey }
                )

                sortedContacts.addAll(sortedGroupContacts)
                currentPosition += sortedGroupContacts.size
            }
        }

        contacts = sortedContacts
        notifyDataSetChanged()
    }

    fun getAvailableIndexes(): Set<String> {
        return sectionHeaders.toSet()
    }

    fun getPositionForIndex(index: String): Int {
        return indexPositionMap[index] ?: 0
    }

    class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerText: TextView = itemView.findViewById(R.id.tvHeader)

        fun bind(header: String) {
            headerText.text = header
        }
    }

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.tvName)
        private val phoneText: TextView = itemView.findViewById(R.id.tvPhone)

        fun bind(contact: Contact) {
            nameText.text = contact.name
            phoneText.text = contact.phoneNumber
        }
    }
}