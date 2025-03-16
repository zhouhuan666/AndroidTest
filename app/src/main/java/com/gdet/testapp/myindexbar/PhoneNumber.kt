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
// 电话号码类型
enum class PhoneType {
    MOBILE, HOME, WORK, OTHER;

    override fun toString(): String {
        return when(this) {
            MOBILE -> "手机"
            HOME -> "住宅"
            WORK -> "工作"
            OTHER -> "其他"
        }
    }
}

// 电话号码数据类
data class PhoneNumber(
    val id: String,
    val number: String,
    val type: PhoneType
) : Parcelable {
    constructor(parcel: Parcel) : this(
        id = parcel.readString() ?: "",
        number = parcel.readString() ?: "",
        type = PhoneType.valueOf(parcel.readString() ?: PhoneType.MOBILE.name)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(number)
        parcel.writeString(type.name)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PhoneNumber> {
        override fun createFromParcel(parcel: Parcel): PhoneNumber {
            return PhoneNumber(parcel)
        }

        override fun newArray(size: Int): Array<PhoneNumber?> {
            return arrayOfNulls(size)
        }
    }
}