package com.gdet.testapp.bluetooth.car

import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import android.view.KeyEvent

/**
 * 车载蓝牙媒体管理类 - 负责A2DP媒体播放控制
 */
@SuppressLint("MissingPermission")
class CarBluetoothMediaManager(private val context: Context) {

    companion object {
        private const val TAG = "CarBluetoothMediaManager"
    }

    // 蓝牙核心
    private val bluetoothCore = CarBluetoothCore.getInstance(context)

    // 设备管理器
    private val deviceManager = CarBluetoothDeviceManager(context)

    // 当前媒体设备
    private var currentMediaDevice: BluetoothDevice? = null

    // 是否正在播放媒体
    private var isMediaPlaying = false

    // 事件分发器
    val eventDispatcher: BluetoothEventDispatcher
        get() = bluetoothCore.eventDispatcher

    // 媒体状态广播接收器
    private val mediaStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return

            when (action) {
                BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED)
                    handleA2dpConnectionStateChanged(device, state)
                }
                BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val state = intent.getIntExtra(BluetoothA2dp.EXTRA_STATE, BluetoothA2dp.STATE_NOT_PLAYING)
                    handleA2dpPlayingStateChanged(device, state)
                }
            }
        }
    }

    init {
        // 初始化A2DP配置文件
        bluetoothCore.initProfileProxy(BluetoothProfile.A2DP) { success ->
            if (success) {
                // 获取当前连接的媒体设备
                updateConnectedMediaDevices()
            }
        }

        // 注册媒体状态广播接收器
        val filter = IntentFilter().apply {
            addAction(BluetoothA2dp.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothA2dp.ACTION_PLAYING_STATE_CHANGED)
        }
        context.registerReceiver(mediaStateReceiver, filter)
    }

    /**
     * 连接设备作为媒体设备
     */
    fun connectAsMediaDevice(address: String): Boolean {
        val device = deviceManager.findDeviceByAddress(address) ?: return false

        // 断开当前的媒体设备
        currentMediaDevice?.let {
            if (it.address != address) {
                disconnectMediaDevice(it)
            }
        }

        // 连接新的媒体设备
        connectA2dpProfile(device)
        currentMediaDevice = device

        return true
    }

    /**
     * 播放/暂停
     */
    fun togglePlayPause() {
        sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)
    }

    /**
     * 上一首
     */
    fun playPrevious() {
        sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_PREVIOUS)
    }

    /**
     * 下一首
     */
    fun playNext() {
        sendMediaButtonEvent(KeyEvent.KEYCODE_MEDIA_NEXT)
    }

    /**
     * 是否正在播放媒体
     */
    fun isPlaying(): Boolean {
        return isMediaPlaying
    }

    /**
     * 获取当前媒体设备
     */
    fun getCurrentMediaDevice(): BluetoothDeviceInfo? {
        return currentMediaDevice?.let { device ->
            val isConnected = getA2dp()?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED

            BluetoothDeviceInfo(
                address = device.address,
                name = device.name ?: "未知设备",
                deviceClass = device.bluetoothClass?.majorDeviceClass ?: 0,
                bondState = device.bondState,
                isConnected = isConnected,
                supportsCalling = false, // 由CallManager设置
                supportsMedia = isConnected,
                batteryLevel = device.getBatteryLevel(),
                isCurrentCallDevice = false, // 由CallManager设置
                isCurrentMediaDevice = true
            )
        }
    }

    /**
     * 更新已连接的媒体设备
     */
    private fun updateConnectedMediaDevices() {
        getA2dp()?.let { a2dp ->
            val connectedDevices = a2dp.connectedDevices
            if (connectedDevices.isNotEmpty()) {
                // 优先选择之前的媒体设备
                val device = if (currentMediaDevice != null && connectedDevices.contains(currentMediaDevice)) {
                    currentMediaDevice
                } else {
                    connectedDevices[0]
                }

                currentMediaDevice = device

                // 检查是否正在播放
                isMediaPlaying = a2dp.isA2dpPlaying(device)
            } else {
                currentMediaDevice = null
                isMediaPlaying = false
            }
        }
    }

    /**
     * 连接A2DP配置文件
     */
    private fun connectA2dpProfile(device: BluetoothDevice) {
        getA2dp()?.let {
            try {
                if (it.getConnectionState(device) != BluetoothProfile.STATE_CONNECTED) {
                    it.connect(device)
                } else {

                }
            } catch (e: Exception) {
                Log.e(TAG, "连接A2DP失败", e)
            }
        }
    }

    /**
     * 断开A2DP配置文件
     */
    private fun disconnectMediaDevice(device: BluetoothDevice) {
        getA2dp()?.let {
            try {
                if (it.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED) {
                    it.disconnect(device)
                } else {

                }
            } catch (e: Exception) {
                Log.e(TAG, "断开A2DP失败", e)
            }
        }
    }

    /**
     * 获取BluetoothA2dp代理
     */
    private fun getA2dp(): BluetoothA2dp? {
        return bluetoothCore.getProfileProxy(BluetoothProfile.A2DP)
    }

    /**
     * 发送媒体按键事件
     */
    private fun sendMediaButtonEvent(keyCode: Int) {
        val eventDown = KeyEvent(KeyEvent.ACTION_DOWN, keyCode)
        val eventUp = KeyEvent(KeyEvent.ACTION_UP, keyCode)

        val intent = Intent(Intent.ACTION_MEDIA_BUTTON)
        intent.putExtra(Intent.EXTRA_KEY_EVENT, eventDown)
        context.sendOrderedBroadcast(intent, null)

        intent.putExtra(Intent.EXTRA_KEY_EVENT, eventUp)
        context.sendOrderedBroadcast(intent, null)
    }

    /**
     * 处理A2DP连接状态变化
     */
    private fun handleA2dpConnectionStateChanged(device: BluetoothDevice?, state: Int) {
        Log.d(TAG, "A2DP连接状态变化: 设备=${device?.name}, 状态=$state")

        device?.let {
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    // 如果当前没有媒体设备，设置此设备为媒体设备
                    if (currentMediaDevice == null) {
                        currentMediaDevice = it
                    }

                    // 更新媒体播放状态
                    isMediaPlaying = getA2dp()?.isA2dpPlaying(it) ?: false

                    // 更新设备信息
                    val deviceInfo = createDeviceInfoWithMediaSupport(it)
                    eventDispatcher.dispatchMediaDeviceConnected(deviceInfo)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (currentMediaDevice == it) {
                        // 当前媒体设备断开
                        isMediaPlaying = false
                        currentMediaDevice = null

                        // 尝试寻找其他已连接的媒体设备
                        updateConnectedMediaDevices()
                    }

                    // 更新设备信息
                    val deviceInfo = createDeviceInfoWithMediaSupport(it)
                    eventDispatcher.dispatchMediaDeviceDisconnected(deviceInfo)
                }
            }
        }
    }

    /**
     * 处理A2DP播放状态变化
     */
    private fun handleA2dpPlayingStateChanged(device: BluetoothDevice?, state: Int) {
        Log.d(TAG, "A2DP播放状态变化: 设备=${device?.name}, 状态=$state")

        device?.let {
            when (state) {
                BluetoothA2dp.STATE_PLAYING -> {
                    isMediaPlaying = true
                    if (currentMediaDevice != it) {
                        currentMediaDevice = it
                    }

                    // 通知媒体播放开始
                    val deviceInfo = createDeviceInfoWithMediaSupport(it)
                    eventDispatcher.dispatchMediaPlaybackStarted(deviceInfo)
                }
                BluetoothA2dp.STATE_NOT_PLAYING -> {
                    isMediaPlaying = false

                    // 通知媒体播放停止
                    val deviceInfo = createDeviceInfoWithMediaSupport(it)
                    eventDispatcher.dispatchMediaPlaybackStopped(deviceInfo)
                }
            }
        }
    }

    /**
     * 创建带媒体支持信息的设备信息
     */
    private fun createDeviceInfoWithMediaSupport(device: BluetoothDevice): BluetoothDeviceInfo {
        val isConnected = getA2dp()?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED

        return BluetoothDeviceInfo(
            address = device.address,
            name = device.name ?: "未知设备",
            deviceClass = device.bluetoothClass?.majorDeviceClass ?: 0,
            bondState = device.bondState,
            isConnected = isConnected,
            supportsCalling = false, // 由CallManager设置
            supportsMedia = isConnected,
            batteryLevel = device.getBatteryLevel(),
            isCurrentCallDevice = false, // 由CallManager设置
            isCurrentMediaDevice = device == currentMediaDevice
        )
    }

    /**
     * 释放资源
     */
    fun release() {
        try {
            context.unregisterReceiver(mediaStateReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "解除媒体状态广播接收器注册失败", e)
        }

        currentMediaDevice = null
        isMediaPlaying = false
    }
}