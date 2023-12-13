package com.gdet.testapp.kotlin.coroutine

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.gdet.testapp.R
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.math.log

/**
 *
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-12-01
 * 描述：
 *
 */
class CoroutineActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine)

        System.setProperty("kotlinx.coroutines.debug", "on")

//        lifecycleScope.launch {
//            logMsg("lifecycleScope.launch start")
//            val user = getUserInfo()  //获取用户信息
//            val list = getFriendList(user)   //获取好友列表
//            getChatRecord(list)  //获取聊天记录
//            logMsg("lifecycleScope.launch end")
//        }
//
//        Log.d("zzzzz", "onCreate:继续执行 ")

        val exceptionHandler = CoroutineExceptionHandler { coroutineContext, throwable ->
            log("exception  $throwable")
        }

        lifecycleScope.launch(context = exceptionHandler) {
            val i = 2 / 0
        }


    }

    suspend fun getUserInfo(): String {
        val result = withContext(Dispatchers.IO) {
            logMsg("getUserInfo withContext")
            delay(7000L)
            return@withContext "getUserInfo"
        }
        logMsg("getUserInfo result:$result")   //打印返回值和线程
        return result
    }

    //获取好友列表
    suspend fun getFriendList(user: String): String {
        val result = withContext(Dispatchers.Main) {  //开启协程，withContext是挂起函数
            logMsg("getFriendList withContext")     //打印协程的线程
            delay(2000L)                            //模拟网络请求耗时
            return@withContext "getFriendList"      //协程返回值
        }
        logMsg("getFriendList result:$result")     //打印返回值和线程
        return result
    }

    //获取聊天记录
    suspend fun getChatRecord(list: String): String {
        val result = withContext(Dispatchers.IO) {  //开启协程，withContext是挂起函数
            logMsg("getChatRecord withContext")     //打印协程的线程
            delay(2000L)                            //模拟网络请求耗时
            return@withContext "getFriendInfo"      //协程返回值
        }
        logMsg("getChatRecord result:$result")      //打印返回值和线程
        return result
    }


    fun logMsg(msg: String) {
        Log.d("TAG", "${Thread.currentThread().name}:$msg")
    }

    fun log(msg: String) {
        Log.d("TAG", "${Thread.currentThread().name} $msg")
    }
}