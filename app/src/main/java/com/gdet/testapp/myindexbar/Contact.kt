package com.gdet.testapp.myindexbar

/**
 *
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2025-03-15
 * 描述：
 *
 */
data class Contact(
    val name: String,
    val phoneNumber: String
) {
    val sortKey: String
        get() {
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

    // 用于组内排序的键
    val inGroupSortKey: String
        get() = name
}
