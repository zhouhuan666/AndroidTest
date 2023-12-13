package com.gdet.testapp.kotlin.coroutine

import android.os.Bundle
import android.util.Log
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.viewModelScope
import com.gdet.testapp.R
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

/**
 *
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-12-01
 * 描述：
 *
 */
class OkHttpActivity : AppCompatActivity() {

    private var startTime: Long = 0
    private var endTime: Long = 0
    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_coroutine)
        //打印协程名称
        System.setProperty("kotlinx.coroutines.debug", "on")
        val url = "https://www.wanandroid.com//hotkey/json"
        val client = OkHttpClient()
        val request = Request.Builder().url(url).build()
        val call = client.newCall(request)

        startTime = System.currentTimeMillis()
        mainViewModel.viewModelScope.launch {
            log("1")
            val deprecated = async(Dispatchers.IO) {
                    callback(call)
            }

            log("2")

            deprecated.invokeOnCompletion {
                endTime=System.currentTimeMillis()
                log("invokeOnCompletion: ${endTime - startTime}ms  error: $it")
            }
            log("3")
            deprecated.await().let {
                endTime=System.currentTimeMillis()
                log("await: ${endTime - startTime}ms  data: $it")
            }
            log("4")
        }
        log("7")

    }

    private suspend fun callback(call: Call): String = suspendCoroutine { continuation ->
        log("5")
        call.enqueue(object : okhttp3.Callback {
            override fun onFailure(call: Call, e: IOException) {
                continuation.resumeWithException(e)
            }

            override fun onResponse(call: Call, response: Response) {
                val data = response.body?.string() ?: ""
                continuation.resume(data)
                log("onResponse:$data")
            }

        })
        log("6")
    }

    fun log(msg: String) {
        Log.d(
            "LOG_PRINT",
            """
            -
            内容:$msg,
            线程:${Thread.currentThread().name}  
        """.trimIndent()
        )
    }


}