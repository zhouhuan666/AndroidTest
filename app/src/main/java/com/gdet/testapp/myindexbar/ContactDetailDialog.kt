package com.gdet.testapp.myindexbar

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.LinearLayoutManager
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
class ContactDetailDialog : DialogFragment() {

    private lateinit var contact: ContactData
    private lateinit var phoneNumberAdapter: PhoneNumberAdapter

    companion object {
        private const val ARG_CONTACT = "contact"

        fun newInstance(contact: ContactData): ContactDetailDialog {
            val fragment = ContactDetailDialog()
            val args = Bundle()
            args.putParcelable(ARG_CONTACT, contact)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.DialogTheme)

        // 使用兼容 API 30+ 的方式获取 Parcelable
        contact = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            arguments?.getParcelable(ARG_CONTACT, ContactData::class.java)
        } else {
            @Suppress("DEPRECATION")
            arguments?.getParcelable(ARG_CONTACT)
        } ?: throw IllegalArgumentException("Contact is required")
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.dialog_contact_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvName = view.findViewById<TextView>(R.id.tvContactName)
        val recyclerView = view.findViewById<RecyclerView>(R.id.rvPhoneNumbers)

        tvName.text = contact.displayName

        // 设置电话号码列表
        phoneNumberAdapter = PhoneNumberAdapter()
        recyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = phoneNumberAdapter
        }

        // 获取所有电话号码
        val phoneNumbers = contact.getAllPhoneNumbers()
        phoneNumberAdapter.submitList(phoneNumbers)

        // 设置电话号码点击事件
        phoneNumberAdapter.setOnPhoneNumberClickListener { phoneNumber ->
            // 拨打电话
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:${phoneNumber.number.replace("/", "")}")
            }
            startActivity(intent)
        }
    }

    override fun onStart() {
        super.onStart()
        dialog?.window?.setLayout(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
    }
}