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
 * 创建日期：2025-03-16
 * 描述：
 *
 */
class PhoneNumberAdapter : RecyclerView.Adapter<PhoneNumberAdapter.PhoneNumberViewHolder>() {

    private var phoneNumbers: List<PhoneNumber> = emptyList()
    private var onPhoneNumberClickListener: ((PhoneNumber) -> Unit)? = null

    fun setOnPhoneNumberClickListener(listener: (PhoneNumber) -> Unit) {
        onPhoneNumberClickListener = listener
    }

    fun submitList(numbers: List<PhoneNumber>) {
        phoneNumbers = numbers
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PhoneNumberViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_phone_number, parent, false)
        return PhoneNumberViewHolder(view)
    }

    override fun onBindViewHolder(holder: PhoneNumberViewHolder, position: Int) {
        holder.bind(phoneNumbers[position])
    }

    override fun getItemCount(): Int = phoneNumbers.size

    inner class PhoneNumberViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val numberText: TextView = itemView.findViewById(R.id.tvPhoneNumber)
        private val typeText: TextView = itemView.findViewById(R.id.tvPhoneType)

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onPhoneNumberClickListener?.invoke(phoneNumbers[position])
                }
            }
        }

        fun bind(phoneNumber: PhoneNumber) {
            numberText.text = phoneNumber.number
            typeText.text = phoneNumber.type.toString()
        }
    }
}