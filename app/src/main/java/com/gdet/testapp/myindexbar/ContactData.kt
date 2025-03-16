package com.gdet.testapp.myindexbar

import android.os.Parcel
import android.os.Parcelable

/**
 *
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2025-03-16
 * 描述：
 *
 */
data class ContactData(
    val contactId: Long = 0,
    val displayName: String = "",
    val phoneNum: String = "",
    val phoneHomeNum: String = "",
    val phoneWorkNum: String = "",
    val phoneOtherNum: String = "",
    val emailNum: String? = null,
    val photoId: String? = null,
    val profileBytes: ByteArray? = null
) : Parcelable {
    // 获取所有非空电话号码的列表
    fun getAllPhoneNumbers(): List<PhoneNumber> {
        val numbers = mutableListOf<PhoneNumber>()

        if (phoneNum.isNotBlank()) {
            numbers.add(PhoneNumber("1", phoneNum, PhoneType.MOBILE))
        }

        if (phoneHomeNum.isNotBlank()) {
            numbers.add(PhoneNumber("2", phoneHomeNum, PhoneType.HOME))
        }

        if (phoneWorkNum.isNotBlank()) {
            numbers.add(PhoneNumber("3", phoneWorkNum, PhoneType.WORK))
        }

        if (phoneOtherNum.isNotBlank()) {
            numbers.add(PhoneNumber("4", phoneOtherNum, PhoneType.OTHER))
        }

        return numbers
    }

    // 判断是否有电话号码
    fun hasPhoneNumber(): Boolean {
        return phoneNum.isNotBlank() || phoneHomeNum.isNotBlank() ||
                phoneWorkNum.isNotBlank() || phoneOtherNum.isNotBlank()
    }

    // Parcelable 实现
    constructor(parcel: Parcel) : this(
        contactId = parcel.readLong(),
        displayName = parcel.readString() ?: "",
        phoneNum = parcel.readString() ?: "",
        phoneHomeNum = parcel.readString() ?: "",
        phoneWorkNum = parcel.readString() ?: "",
        phoneOtherNum = parcel.readString() ?: "",
        emailNum = parcel.readString(),
        photoId = parcel.readString(),
        profileBytes = parcel.createByteArray()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeLong(contactId)
        parcel.writeString(displayName)
        parcel.writeString(phoneNum)
        parcel.writeString(phoneHomeNum)
        parcel.writeString(phoneWorkNum)
        parcel.writeString(phoneOtherNum)
        parcel.writeString(emailNum)
        parcel.writeString(photoId)
        parcel.writeByteArray(profileBytes)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<ContactData> {
        override fun createFromParcel(parcel: Parcel): ContactData {
            return ContactData(parcel)
        }

        override fun newArray(size: Int): Array<ContactData?> {
            return arrayOfNulls(size)
        }
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as ContactData

        if (contactId != other.contactId) return false
        if (displayName != other.displayName) return false
        if (phoneNum != other.phoneNum) return false
        if (phoneHomeNum != other.phoneHomeNum) return false
        if (phoneWorkNum != other.phoneWorkNum) return false
        if (phoneOtherNum != other.phoneOtherNum) return false
        if (emailNum != other.emailNum) return false
        if (photoId != other.photoId) return false
        if (profileBytes != null) {
            if (other.profileBytes == null) return false
            if (!profileBytes.contentEquals(other.profileBytes)) return false
        } else if (other.profileBytes != null) return false

        return true
    }

    override fun hashCode(): Int {
        var result = contactId.hashCode()
        result = 31 * result + displayName.hashCode()
        result = 31 * result + phoneNum.hashCode()
        result = 31 * result + phoneHomeNum.hashCode()
        result = 31 * result + phoneWorkNum.hashCode()
        result = 31 * result + phoneOtherNum.hashCode()
        result = 31 * result + (emailNum?.hashCode() ?: 0)
        result = 31 * result + (photoId?.hashCode() ?: 0)
        result = 31 * result + (profileBytes?.contentHashCode() ?: 0)
        return result
    }
}