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
class ContactAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_CONTACT = 1
    }

    private var groupedContacts: Map<String, List<ContactData>> = emptyMap()
    private var flattenedItems: List<Item> = emptyList()
    private var onContactClickListener: ((ContactData) -> Unit)? = null

    // 表示列表中的项目类型
    sealed class Item {
        data class Header(val letter: String) : Item()
        data class Contact(val contact: ContactData) : Item()
    }

    fun setOnContactClickListener(listener: (ContactData) -> Unit) {
        onContactClickListener = listener
    }

    fun submitList(groupedContacts: Map<String, List<ContactData>>) {
        this.groupedContacts = groupedContacts

        // 将分组数据扁平化为列表项
        val items = mutableListOf<Item>()
        for ((index, contacts) in groupedContacts) {
            items.add(Item.Header(index))
            items.addAll(contacts.map { Item.Contact(it) })
        }

        this.flattenedItems = items
        notifyDataSetChanged()
    }

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
        when (val item = flattenedItems[position]) {
            is Item.Header -> (holder as HeaderViewHolder).bind(item.letter)
            is Item.Contact -> (holder as ContactViewHolder).bind(item.contact)
        }
    }

    override fun getItemCount(): Int = flattenedItems.size

    override fun getItemViewType(position: Int): Int {
        return when (flattenedItems[position]) {
            is Item.Header -> TYPE_HEADER
            is Item.Contact -> TYPE_CONTACT
        }
    }

    fun getPositionForIndex(index: String): Int {
        return flattenedItems.indexOfFirst { it is Item.Header && it.letter == index }
    }

    fun getAvailableIndexes(): Set<String> {
        return groupedContacts.keys
    }

    fun getItemAtPosition(position: Int): Item? {
        return if (position >= 0 && position < flattenedItems.size) {
            flattenedItems[position]
        } else null
    }

    inner class HeaderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val headerText: TextView = itemView.findViewById(R.id.tvHeader)

        fun bind(header: String) {
            headerText.text = header
        }
    }

    inner class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val nameText: TextView = itemView.findViewById(R.id.tvName)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    val item = flattenedItems[position]
                    if (item is Item.Contact) {
                        onContactClickListener?.invoke(item.contact)
                    }
                }
            }
        }

        fun bind(contact: ContactData) {
            nameText.text = contact.displayName
        }
    }
}