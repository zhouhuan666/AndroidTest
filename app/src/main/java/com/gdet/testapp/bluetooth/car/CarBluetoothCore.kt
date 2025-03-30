package com.gdet.testapp.bluetooth.car

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothManager
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.util.Log
import java.util.concurrent.ConcurrentHashMap

/**
 * 车载蓝牙核心管理类 - 负责基础蓝牙功能和服务初始化
 */
@SuppressLint("MissingPermission")
class CarBluetoothCore(private val context: Context) {

    companion object {
        private const val TAG = "CarBluetoothCore"

        @Volatile
        private var instance: CarBluetoothCore? = null

        fun getInstance(context: Context): CarBluetoothCore {
            return instance ?: synchronized(this) {
                instance ?: CarBluetoothCore(context.applicationContext).also { instance = it }
            }
        }
    }

    // 蓝牙适配器
    val bluetoothAdapter: BluetoothAdapter? by lazy {
        val bluetoothManager = context.getSystemService(Context.BLUETOOTH_SERVICE) as BluetoothManager
        bluetoothManager.adapter
    }

    // 音频管理器
    val audioManager: AudioManager by lazy {
        context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    // 蓝牙配置文件
    private val profileProxies = ConcurrentHashMap<Int, BluetoothProfile>()

    // 服务监听器
    private val serviceListeners = ConcurrentHashMap<Int, BluetoothProfile.ServiceListener>()

    // 事件分发器
    val eventDispatcher = BluetoothEventDispatcher()

    // 蓝牙状态广播接收器
    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return

            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                val state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)
                handleBluetoothStateChanged(state)
            }
        }
    }

    init {
        // 注册蓝牙状态广播
        val filter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        context.registerReceiver(bluetoothStateReceiver, filter)
    }

    /**
     * 获取蓝牙配置文件代理
     */
    @Suppress("UNCHECKED_CAST")
    fun <T : BluetoothProfile> getProfileProxy(profile: Int): T? {
        return profileProxies[profile] as? T
    }

    /**
     * 初始化蓝牙配置文件代理
     */
    fun initProfileProxy(profile: Int, callback: (Boolean) -> Unit) {
        // 如果已经有此代理则直接返回
        if (profileProxies.containsKey(profile)) {
            callback(true)
            return
        }

        val listener = object : BluetoothProfile.ServiceListener {
            override fun onServiceConnected(profileId: Int, proxy: BluetoothProfile) {
                profileProxies[profileId] = proxy
                Log.d(TAG, "蓝牙配置文件连接成功: $profileId")
                eventDispatcher.dispatchServiceConnected(profileId)
                callback(true)
            }

            override fun onServiceDisconnected(profileId: Int) {
                profileProxies.remove(profileId)
                Log.d(TAG, "蓝牙配置文件断开: $profileId")
                eventDispatcher.dispatchServiceDisconnected(profileId)
            }
        }

        // 保存监听器避免被垃圾回收
        serviceListeners[profile] = listener

        // 获取配置文件代理
        val success = bluetoothAdapter?.getProfileProxy(context, listener, profile) ?: false
        if (!success) {
            Log.e(TAG, "获取蓝牙配置文件代理失败: $profile")
            callback(false)
        }
    }

    /**
     * 关闭蓝牙配置文件代理
     */
    fun closeProfileProxy(profile: Int) {
        val proxy = profileProxies[profile] ?: return
        bluetoothAdapter?.closeProfileProxy(profile, proxy)
        profileProxies.remove(profile)
        serviceListeners.remove(profile)
    }

    /**
     * 开启蓝牙
     */
    fun enableBluetooth(): Boolean {
        return bluetoothAdapter?.let {
            if (!it.isEnabled) {
                it.enable()
            } else {
                true
            }
        } ?: false
    }

    /**
     * 关闭蓝牙
     */
    fun disableBluetooth(): Boolean {
        return bluetoothAdapter?.let {
            if (it.isEnabled) {
                it.disable()
            } else {
                true
            }
        } ?: false
    }

    /**
     * 蓝牙是否开启
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothAdapter?.isEnabled == true
    }

    /**
     * 处理蓝牙状态变化
     */
    private fun handleBluetoothStateChanged(state: Int) {
        when (state) {
            BluetoothAdapter.STATE_ON -> {
                eventDispatcher.dispatchBluetoothStateChanged(true)
            }
            BluetoothAdapter.STATE_OFF -> {
                eventDispatcher.dispatchBluetoothStateChanged(false)
            }
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        try {
            context.unregisterReceiver(bluetoothStateReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "解除蓝牙状态广播接收器注册失败", e)
        }

        // 关闭所有配置文件代理
        profileProxies.keys.toList().forEach { profile ->
            closeProfileProxy(profile)
        }

        profileProxies.clear()
        serviceListeners.clear()
        eventDispatcher.clear()
        instance = null
    }
}