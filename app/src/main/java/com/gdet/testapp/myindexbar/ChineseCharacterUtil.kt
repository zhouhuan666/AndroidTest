package com.gdet.testapp.myindexbar

import net.sourceforge.pinyin4j.PinyinHelper
import net.sourceforge.pinyin4j.format.HanyuPinyinCaseType
import net.sourceforge.pinyin4j.format.HanyuPinyinOutputFormat
import net.sourceforge.pinyin4j.format.HanyuPinyinToneType

/**
 *
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2025-03-15
 * 描述：
 *
 */
object ChineseCharacterUtil {
    // 常见多音字映射表（用于姓名）
    private val polyPhoneMap = mapOf(
        '单' to "SHAN",
        '仇' to "QIU",
        '曾' to "ZENG",
        '朴' to "PIAO",
        '查' to "ZHA",
        '翟' to "ZHAI",
        '覃' to "QIN",
        '冼' to "XIAN",
        '解' to "XIE",
        '繁' to "PO",
        '区' to "OU",
        '蕃' to "PAN",
        '折' to "SHE",
        '句' to "GOU",
        '种' to "CHONG",
        '秘' to "BI",
        '乐' to "YUE",
        '召' to "SHAO",
        '行' to "HANG",
        '藏' to "ZANG",
        '任' to "REN",
        '华' to "HUA",
        '过' to "GUO",
        '贾' to "JIA",
        '盖' to "GE",
        '万' to "WAN",
        '尉' to "YU",
        '黑' to "HE",
        '车' to "JU",
        '奇' to "QI",
        '都' to "DU",
        '空' to "KONG",
        '蒙' to "MENG",
        '卜' to "BO",
        '麦' to "MAI",
        '隽' to "JUAN",
        '哈' to "HA",
        '宁' to "NING",
        '缪' to "MIAO",
        '艾' to "AI",
        '能' to "NAI",
        '耿' to "GENG",
        '汤' to "TANG",
        '阚' to "KAN",
        '臧' to "ZANG",
        '沈' to "SHEN",
        '勒' to "LE",
        '冯' to "FENG",
        '戴' to "DAI",
        '沃' to "WO"
    )

    /**
     * 获取字符的拼音首字母，处理多音字
     */
    fun getFirstLetter(character: Char): String {
        // 先检查是否是多音字
        polyPhoneMap[character]?.let {
            return it.substring(0, 1)
        }

        // 使用默认拼音转换
        val format = HanyuPinyinOutputFormat().apply {
            caseType = HanyuPinyinCaseType.UPPERCASE
            toneType = HanyuPinyinToneType.WITHOUT_TONE
        }

        val pinyinArray = PinyinHelper.toHanyuPinyinStringArray(character, format)
        return if (pinyinArray != null && pinyinArray.isNotEmpty()) {
            pinyinArray[0].substring(0, 1)
        } else "#"
    }
}