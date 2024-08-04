package com.gdet.testapp

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

interface WeatherSDKCallback {
    fun onConnected()
    fun onDisconnected()
    fun onWeatherReceived(weather: String)
    fun onError(error: String)
}

class WeatherSDK private constructor(private val context: Context) {
    private val TAG = "WeatherSDK"
    private var weatherService: IWeatherService? = null
    private val _connectionState = MutableStateFlow(false)
    val connectionState: StateFlow<Boolean> = _connectionState
    private val callbacks = mutableListOf<WeatherSDKCallback>()
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())
    private var retryJob: Job? = null

    private val serviceCallback = object : IWeatherServiceCallback.Stub() {
        override fun onWeatherUpdated(weather: String) {
            notifyCallbacks { it.onWeatherReceived(weather) }
        }
    }

    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            Log.i(TAG, "onServiceConnected: ")
            weatherService = IWeatherService.Stub.asInterface(service)
            weatherService?.addCallback(serviceCallback)
            _connectionState.value = true
            notifyCallbacks { it.onConnected() }
            retryJob?.cancel() // 取消重连任务
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            Log.i(TAG, "onServiceDisconnected: ")
            weatherService = null
            _connectionState.value = false
            notifyCallbacks { it.onDisconnected() }
            // 断线重连，包括服务进程死掉的情况
            retryConnection()
        }
    }

    init {
        bindService()
    }

    private fun bindService() {
        Log.i(TAG, "bindService: 开始连接")
        val intent = Intent()
        intent.setComponent(ComponentName("com.gdet.testapp", "com.gdet.testapp.WeatherService"))
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    private fun retryConnection() {
        retryJob = coroutineScope.launch {
            while (!_connectionState.value) {
                delay(5000) // 等待5秒
                bindService()
            }
        }
    }

    suspend fun getWeather(city: String): String? = coroutineScope {
        try {
            val weather = weatherService?.getWeather(city)
            weather
        } catch (e: Exception) {
            notifyCallbacks { it.onError(e.message ?: "获取天气失败") }
            if (e.message == "服务未连接") {
                retryConnection()
            }
            ""
        }
    }


    fun addCallback(callback: WeatherSDKCallback) {
        callbacks.add(callback)
    }

    fun removeCallback(callback: WeatherSDKCallback) {
        callbacks.remove(callback)
    }

    private fun notifyCallbacks(action: (WeatherSDKCallback) -> Unit) {
        callbacks.forEach(action)
    }

    fun unbind() {
        weatherService?.removeCallback(serviceCallback)
        context.unbindService(serviceConnection)
        coroutineScope.cancel()
        callbacks.clear()
    }

    companion object {
        @Volatile
        private var instance: WeatherSDK? = null

        fun getInstance(context: Context): WeatherSDK {
            return instance ?: synchronized(this) {
                instance ?: WeatherSDK(context.applicationContext).also { instance = it }
            }
        }
    }
}