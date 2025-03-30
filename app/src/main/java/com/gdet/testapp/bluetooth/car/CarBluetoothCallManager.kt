package com.gdet.testapp.bluetooth.car

import android.annotation.SuppressLint
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadsetClient
import android.bluetooth.BluetoothHeadsetClientCall
import android.bluetooth.BluetoothProfile
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.util.Log

/**
 * 车载蓝牙通话管理类 - 负责HFP通话功能
 */
@SuppressLint("MissingPermission")
class CarBluetoothCallManager(private val context: Context) {

    companion object {
        private const val TAG = "CarBluetoothCallManager"
    }

    // 蓝牙核心
    private val bluetoothCore = CarBluetoothCore.getInstance(context)

    // 设备管理器
    private val deviceManager = CarBluetoothDeviceManager(context)

    // 当前通话设备
    private var currentCallDevice: BluetoothDevice? = null

    // 当前通话
    private var currentCall: BluetoothHeadsetClientCall? = null

    // 事件分发器
    val eventDispatcher: BluetoothEventDispatcher
        get() = bluetoothCore.eventDispatcher

    // 通话状态广播接收器
    private val callStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return

            when (action) {
                BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val state = intent.getIntExtra(BluetoothProfile.EXTRA_STATE, BluetoothProfile.STATE_DISCONNECTED)
                    handleHfpConnectionStateChanged(device, state)
                }
                BluetoothHeadsetClient.ACTION_CALL_CHANGED -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val call = intent.getParcelableExtra<BluetoothHeadsetClientCall>(BluetoothHeadsetClient.EXTRA_CALL)
                    handleCallChanged(device, call)
                }
                BluetoothHeadsetClient.ACTION_AG_EVENT -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)

                }
                AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED -> {
                    val state = intent.getIntExtra(AudioManager.EXTRA_SCO_AUDIO_STATE, AudioManager.SCO_AUDIO_STATE_ERROR)
                    handleScoAudioStateUpdated(state)
                }
            }
        }
    }

    init {
        // 初始化HFP Client配置文件
        bluetoothCore.initProfileProxy(BluetoothProfile.HEADSET_CLIENT) { success ->
            if (success) {
                // 获取当前连接的设备和通话状态
                updateConnectedCallDevices()
            }
        }

        // 注册通话状态广播接收器
        val filter = IntentFilter().apply {
            addAction(BluetoothHeadsetClient.ACTION_CONNECTION_STATE_CHANGED)
            addAction(BluetoothHeadsetClient.ACTION_CALL_CHANGED)
            addAction(BluetoothHeadsetClient.ACTION_AG_EVENT)
            addAction(AudioManager.ACTION_SCO_AUDIO_STATE_UPDATED)
        }
        context.registerReceiver(callStateReceiver, filter)
    }

    /**
     * 连接设备作为通话设备
     */
    fun connectAsCallDevice(address: String): Boolean {
        val device = deviceManager.findDeviceByAddress(address) ?: return false

        // 断开当前的通话设备
        currentCallDevice?.let {
            if (it.address != address) {
                disconnectCallDevice(it)
            }
        }

        // 连接新的通话设备
        connectHeadsetClientProfile(device)
        currentCallDevice = device

        return true
    }

    /**
     * 接听来电
     */
    fun acceptCall(): Boolean {
        return getHeadsetClient()?.let { client ->
            currentCallDevice?.let { device ->
                client.acceptCall(device, BluetoothHeadsetClient.CALL_ACCEPT_NONE)
                true
            }
        } ?: false
    }

    /**
     * 拒绝来电
     */
    fun rejectCall(): Boolean {
        return getHeadsetClient()?.let { client ->
            currentCallDevice?.let { device ->
                client.rejectCall(device)
                true
            }
        } ?: false
    }

    /**
     * 结束通话
     */
    fun terminateCall(): Boolean {
        return getHeadsetClient()?.let { client ->
            currentCallDevice?.let { device ->
                currentCall?.let { call ->
                    client.terminateCall(device, call)
                    true
                }
            }
        } ?: false
    }

    /**
     * 拨打电话
     */
    fun dialNumber(number: String): Boolean {
        return getHeadsetClient()?.let { client ->
            currentCallDevice?.let { device ->
                client.dial(device, number)
                true
            }
        } ?: false
    }

    /**
     * 切换音频通道
     */
    fun switchAudioChannel(useBluetooth: Boolean): Boolean {
        return try {
            if (useBluetooth) {
                if (!bluetoothCore.audioManager.isBluetoothScoOn) {
                    bluetoothCore.audioManager.isBluetoothScoOn = true
                    bluetoothCore.audioManager.startBluetoothSco()
                }
            } else {
                if (bluetoothCore.audioManager.isBluetoothScoOn) {
                    bluetoothCore.audioManager.isBluetoothScoOn = false
                    bluetoothCore.audioManager.stopBluetoothSco()
                }
            }
            true
        } catch (e: Exception) {
            Log.e(TAG, "切换音频通道失败", e)
            false
        }
    }

    /**
     * 调整蓝牙音量
     */
    fun adjustVolume(increase: Boolean) {
        val direction = if (increase) AudioManager.ADJUST_RAISE else AudioManager.ADJUST_LOWER
        bluetoothCore.audioManager.adjustStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO, direction, AudioManager.FLAG_SHOW_UI)
    }

    /**
     * 设置蓝牙音量
     */
    fun setVolume(volume: Int) {
        val maxVolume = bluetoothCore.audioManager.getStreamMaxVolume(AudioManager.STREAM_BLUETOOTH_SCO)
        val normalizedVolume = volume.coerceIn(0, maxVolume)
        bluetoothCore.audioManager.setStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO, normalizedVolume, AudioManager.FLAG_SHOW_UI)
    }

    /**
     * 获取当前蓝牙音量
     */
    fun getCurrentVolume(): Int {
        return bluetoothCore.audioManager.getStreamVolume(AudioManager.STREAM_BLUETOOTH_SCO)
    }

    /**
     * 获取最大蓝牙音量
     */
    fun getMaxVolume(): Int {
        return bluetoothCore.audioManager.getStreamMaxVolume(AudioManager.STREAM_BLUETOOTH_SCO)
    }

    /**
     * 获取当前通话信息
     */
    fun getCurrentCallInfo(): BluetoothCallInfo? {
        return currentCall?.let { call ->
            currentCallDevice?.let { device ->
                BluetoothCallInfo(
                    callId = call.hashCode().toString(),
                    deviceAddress = device.address,
                    deviceName = device.name ?: "未知设备",
                    phoneNumber = call.number,
                    startTime = System.currentTimeMillis(),
                    state = convertCallState(call.state)
                )
            }
        }
    }

    /**
     * 是否有活动通话
     */
    fun hasActiveCall(): Boolean {
        return currentCall != null && currentCall?.state == BluetoothHeadsetClientCall.CALL_STATE_ACTIVE
    }

    /**
     * 更新已连接的通话设备
     */
    private fun updateConnectedCallDevices() {
        getHeadsetClient()?.let { client ->
            val connectedDevices = client.connectedDevices
            if (connectedDevices.isNotEmpty()) {
                // 优先选择之前的通话设备
                val device = if (currentCallDevice != null && connectedDevices.contains(currentCallDevice)) {
                    currentCallDevice
                } else {
                    connectedDevices[0]
                }

                currentCallDevice = device
                updateCurrentCalls(device)
            } else {
                currentCallDevice = null
                currentCall = null
            }
        }
    }

    /**
     * 更新当前通话
     */
    private fun updateCurrentCalls(device: BluetoothDevice?) {
        device?.let { dev ->
            getHeadsetClient()?.let { client ->
                client.getCurrentCalls(dev)?.let { calls ->
                    // 查找活动通话
                    val activeCall = calls.firstOrNull {
                        it.state == BluetoothHeadsetClientCall.CALL_STATE_ACTIVE ||
                                it.state == BluetoothHeadsetClientCall.CALL_STATE_INCOMING ||
                                it.state == BluetoothHeadsetClientCall.CALL_STATE_DIALING ||
                                it.state == BluetoothHeadsetClientCall.CALL_STATE_ALERTING
                    }

                    if (activeCall != null) {
                        currentCall = activeCall
                        // 通知通话状态变化
                        notifyCallStateChanged(dev, activeCall)
                    } else {
                        currentCall = null
                    }
                }
            }
        }
    }

    /**
     * 连接HFP Client配置文件
     */
    private fun connectHeadsetClientProfile(device: BluetoothDevice) {
        getHeadsetClient()?.let {
            try {
                if (it.getConnectionState(device) != BluetoothProfile.STATE_CONNECTED) {
                    it.connect(device)
                } else {

                }
            } catch (e: Exception) {
                Log.e(TAG, "连接HFP Client失败", e)
            }
        }
    }

    /**
     * 断开HFP Client配置文件
     */
    private fun disconnectCallDevice(device: BluetoothDevice) {
        getHeadsetClient()?.let {
            try {
                if (it.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED) {
                    it.disconnect(device)
                } else {

                }
            } catch (e: Exception) {
                Log.e(TAG, "断开HFP Client失败", e)
            }
        }
    }

    /**
     * 获取BluetoothHeadsetClient代理
     */
    private fun getHeadsetClient(): BluetoothHeadsetClient? {
        return bluetoothCore.getProfileProxy(BluetoothProfile.HEADSET_CLIENT)
    }

    /**
     * 处理HFP连接状态变化
     */
    private fun handleHfpConnectionStateChanged(device: BluetoothDevice?, state: Int) {
        Log.d(TAG, "HFP连接状态变化: 设备=${device?.name}, 状态=$state")

        device?.let {
            when (state) {
                BluetoothProfile.STATE_CONNECTED -> {
                    // 如果当前没有通话设备，设置此设备为通话设备
                    if (currentCallDevice == null) {
                        currentCallDevice = it
                    }

                    // 更新通话状态
                    updateCurrentCalls(it)

                    // 更新设备信息
                    val deviceInfo = createDeviceInfoWithCallSupport(it)
                    eventDispatcher.dispatchCallDeviceConnected(deviceInfo)
                }
                BluetoothProfile.STATE_DISCONNECTED -> {
                    if (currentCallDevice == it) {
                        // 当前通话设备断开
                        currentCallDevice = null
                        currentCall = null

                        // 尝试寻找其他已连接的通话设备
                        updateConnectedCallDevices()
                    }

                    // 更新设备信息
                    val deviceInfo = createDeviceInfoWithCallSupport(it)
                    eventDispatcher.dispatchCallDeviceDisconnected(deviceInfo)
                }
            }
        }
    }

    /**
     * 处理通话状态变化
     */
    private fun handleCallChanged(device: BluetoothDevice?, call: BluetoothHeadsetClientCall?) {
        Log.d(TAG, "通话状态变化: 设备=${device?.name}, 通话=$call")

        if (device == null || call == null) return

        // 更新当前通话状态
        when (call.state) {
            BluetoothHeadsetClientCall.CALL_STATE_ACTIVE,
            BluetoothHeadsetClientCall.CALL_STATE_INCOMING,
            BluetoothHeadsetClientCall.CALL_STATE_DIALING,
            BluetoothHeadsetClientCall.CALL_STATE_ALERTING,
            BluetoothHeadsetClientCall.CALL_STATE_HELD,
            BluetoothHeadsetClientCall.CALL_STATE_WAITING -> {
                currentCallDevice = device
                currentCall = call
                notifyCallStateChanged(device, call)
            }
            BluetoothHeadsetClientCall.CALL_STATE_TERMINATED -> {
                if (currentCall?.id == call.id) {
                    notifyCallEnded(device, call)
                    currentCall = null

                    // 检查是否有其他正在进行的通话
                    updateCurrentCalls(device)
                }
            }
        }
    }


    /**
     * 处理SCO音频状态更新
     */
    private fun handleScoAudioStateUpdated(state: Int) {
        Log.d(TAG, "SCO音频状态更新: $state")

        when (state) {
            AudioManager.SCO_AUDIO_STATE_CONNECTED -> {
                // SCO音频已连接，通话音频正在通过蓝牙传输
                eventDispatcher.dispatchScoAudioConnected()
            }
            AudioManager.SCO_AUDIO_STATE_DISCONNECTED -> {
                // SCO音频已断开
                eventDispatcher.dispatchScoAudioDisconnected()
            }
        }
    }

    /**
     * 转换通话状态
     */
    private fun convertCallState(state: Int): CallState {
        return when (state) {
            BluetoothHeadsetClientCall.CALL_STATE_ACTIVE -> CallState.ACTIVE
            BluetoothHeadsetClientCall.CALL_STATE_HELD -> CallState.HELD
            BluetoothHeadsetClientCall.CALL_STATE_DIALING -> CallState.DIALING
            BluetoothHeadsetClientCall.CALL_STATE_ALERTING -> CallState.ALERTING
            BluetoothHeadsetClientCall.CALL_STATE_INCOMING -> CallState.INCOMING
            BluetoothHeadsetClientCall.CALL_STATE_WAITING -> CallState.WAITING
            BluetoothHeadsetClientCall.CALL_STATE_TERMINATED -> CallState.TERMINATED
            else -> CallState.UNKNOWN
        }
    }

    /**
     * 创建带通话支持信息的设备信息
     */
    private fun createDeviceInfoWithCallSupport(device: BluetoothDevice): BluetoothDeviceInfo {
        val isConnected = getHeadsetClient()?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED

        return BluetoothDeviceInfo(
            address = device.address,
            name = device.name ?: "未知设备",
            deviceClass = device.bluetoothClass?.majorDeviceClass ?: 0,
            bondState = device.bondState,
            isConnected = isConnected,
            supportsCalling = isConnected,
            supportsMedia = false, // 由MediaManager设置
            batteryLevel = device.getBatteryLevel(),
            isCurrentCallDevice = device == currentCallDevice,
            isCurrentMediaDevice = false // 由MediaManager设置
        )
    }

    /**
     * 通知通话状态变化
     */
    private fun notifyCallStateChanged(device: BluetoothDevice, call: BluetoothHeadsetClientCall) {
        val callInfo = BluetoothCallInfo(
            callId = call.hashCode().toString(),
            deviceAddress = device.address,
            deviceName = device.name ?: "未知设备",
            phoneNumber = call.number,
            startTime = System.currentTimeMillis(),
            state = convertCallState(call.state)
        )

        if (call.state == BluetoothHeadsetClientCall.CALL_STATE_INCOMING) {
            eventDispatcher.dispatchCallStarted(callInfo)
        } else {
            eventDispatcher.dispatchCallUpdated(callInfo)
        }
    }

    /**
     * 通知通话结束
     */
    private fun notifyCallEnded(device: BluetoothDevice, call: BluetoothHeadsetClientCall) {
        val callInfo = BluetoothCallInfo(
            callId = call.hashCode().toString(),
            deviceAddress = device.address,
            deviceName = device.name ?: "未知设备",
            phoneNumber = call.number,
            startTime = System.currentTimeMillis() - 1000, // 假设通话至少1秒
            endTime = System.currentTimeMillis(),
            state = CallState.TERMINATED
        )

        eventDispatcher.dispatchCallEnded(callInfo)
    }

    /**
     * 释放资源
     */
    fun release() {
        try {
            context.unregisterReceiver(callStateReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "解除通话状态广播接收器注册失败", e)
        }

        currentCallDevice = null
        currentCall = null
    }
}