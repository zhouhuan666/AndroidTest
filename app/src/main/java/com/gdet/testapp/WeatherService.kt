package com.gdet.testapp

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.RemoteCallbackList

class WeatherService : Service() {
    private val callbacks = RemoteCallbackList<IWeatherServiceCallback>()

    override fun onBind(intent: Intent): IBinder {
        return binder
    }

    private val binder = object : IWeatherService.Stub() {
        override fun getWeather(city: String): String {
            // 实现获取天气的逻辑
            val weather = "城市: $city, 天气: 晴朗"
            notifyWeatherUpdate(weather)
            return weather
        }

        override fun addCallback(callback: IWeatherServiceCallback) {
            callbacks.register(callback)
        }

        override fun removeCallback(callback: IWeatherServiceCallback) {
            callbacks.unregister(callback)
        }
    }

    private fun notifyWeatherUpdate(weather: String) {
        val n = callbacks.beginBroadcast()
        for (i in 0 until n) {
            callbacks.getBroadcastItem(i).onWeatherUpdated(weather)
        }
        callbacks.finishBroadcast()
    }
}