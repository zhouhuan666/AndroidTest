package com.gdet.weatherclient

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.gdet.testapp.WeatherSDK
import com.gdet.testapp.WeatherSDKCallback
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WeatherSDK.getInstance(this).addCallback(object : WeatherSDKCallback {
            override fun onConnected() {

                Log.i(TAG, "onConnected: ")
            }

            override fun onDisconnected() {
                Log.i(TAG, "onDisconnected: ")
            }

            override fun onWeatherReceived(weather: String) {
                Log.i(TAG, "onWeatherReceived: $weather")
            }

            override fun onError(error: String) {
                Log.i(TAG, "onError: $error")
            }

        })
        val button = findViewById<Button>(R.id.button)
        val textView = findViewById<TextView>(R.id.textview)
        button.setOnClickListener {
            lifecycleScope.launch {
                textView.text = WeatherSDK.getInstance(baseContext).getWeather("北京").toString()
            }

        }

    }
}