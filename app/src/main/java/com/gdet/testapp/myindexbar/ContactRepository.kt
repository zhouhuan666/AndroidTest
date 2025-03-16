package com.gdet.testapp.myindexbar

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

/**
 *
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2025-03-16
 * 描述：
 *
 */
class ContactRepository {

    // 模拟从数据源获取联系人数据
    fun getContacts(): Flow<List<ContactData>> = flow {
        // 模拟网络延迟
        kotlinx.coroutines.delay(500)

        // 模拟联系人数据
        val contacts = listOf(
            ContactData(
                displayName = "爸",
                phoneNum = "/1-386-710-4653",
                phoneHomeNum = "/1-531-237-9899",
                phoneWorkNum = "/1-531-666-9899",
                phoneOtherNum = "/1-531-777-9899"
            ),
            ContactData(
                displayName = "班主任樊继豪",
                phoneNum = "/1-515-065-1931"
            ),
            ContactData(
                displayName = "铂岸物业"
            ),
            ContactData(
                displayName = "宇飞 蔡"
            ),
            ContactData(
                displayName = "少勇 陈",
                phoneNum = "/11111111"
            ),
            ContactData(
                displayName = "翔宇 陈",
                phoneNum = "/22222"
            ),
            ContactData(
                displayName = "安迪",
                phoneNum = "/133-3399-2653",
                phoneHomeNum = "/133-3399-2653"
            ),
            ContactData(
                displayName = "阿里巴巴",
                phoneNum = "/123456789"
            ),
            ContactData(
                displayName = "Aron",
                phoneNum = "/987654321"
            ),
            ContactData(
                displayName = "鲍勃",
                phoneNum = "/555555555",
                phoneWorkNum = "/555555556"
            ),
            ContactData(
                displayName = "Bill",
                phoneNum = "/444444444"
            ),
            ContactData(
                displayName = "曹操",
                phoneNum = "/333333333"
            ),
            ContactData(
                displayName = "单田芳",
                phoneNum = "/111222333",
                phoneHomeNum = "/111222334"
            ),
            ContactData(
                displayName = "单纯",
                phoneNum = "/333222111"
            ),
            ContactData(
                displayName = "仇人",
                phoneNum = "/444555666"
            ),
            ContactData(
                displayName = "曾国藩",
                phoneNum = "/666777888"
            ),
            ContactData(
                displayName = "David",
                phoneNum = "/222222222"
            ),
            ContactData(
                displayName = "丁丁",
                phoneNum = "/999999999"
            ),
            ContactData(
                displayName = "Emma",
                phoneNum = "/888888888"
            ),
            ContactData(
                displayName = "方方",
                phoneNum = "/777777777"
            ),
            ContactData(
                displayName = "高高",
                phoneNum = "/666666666"
            ),
            ContactData(
                displayName = "黄河",
                phoneNum = "/123123123"
            ),
            ContactData(
                displayName = "Jack",
                phoneNum = "/456456456"
            ),
            ContactData(
                displayName = "Kevin",
                phoneNum = "/789789789"
            ),
            ContactData(
                displayName = "李四",
                phoneNum = "/321321321"
            ),
            ContactData(
                displayName = "Mary",
                phoneNum = "/654654654"
            ),
            ContactData(
                displayName = "牛牛",
                phoneNum = "/987987987"
            ),
            ContactData(
                displayName = "欧阳",
                phoneNum = "/135792468"
            ),
            ContactData(
                displayName = "朴树",
                phoneNum = "/246813579"
            ),
            ContactData(
                displayName = "钱钱",
                phoneNum = "/112233445"
            ),
            ContactData(
                displayName = "沈阳",
                phoneNum = "/556677889"
            ),
            ContactData(
                displayName = "Tom",
                phoneNum = "/998877665"
            ),
            ContactData(
                displayName = "王五",
                phoneNum = "/554433221"
            ),
            ContactData(
                displayName = "小明",
                phoneNum = "/112233445"
            ),
            ContactData(
                displayName = "杨过",
                phoneNum = "/556677889"
            ),
            ContactData(
                displayName = "张三",
                phoneNum = "/998877665",
                phoneHomeNum = "/998877666",
                phoneWorkNum = "/998877667",
                phoneOtherNum = "/998877668"
            )
        )

        emit(contacts)
    }.flowOn(Dispatchers.IO)
}