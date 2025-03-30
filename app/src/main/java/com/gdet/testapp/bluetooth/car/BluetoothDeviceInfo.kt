package com.gdet.testapp.bluetooth.car

import android.bluetooth.BluetoothClass
import android.bluetooth.BluetoothDevice


/**
 * 蓝牙设备信息
 */
data class BluetoothDeviceInfo(
    val address: String,                // 设备地址
    val name: String,                   // 设备名称
    val deviceClass: Int,               // 设备类别
    val bondState: Int,                 // 配对状态
    val isConnected: Boolean,           // 是否已连接
    val supportsCalling: Boolean,       // 是否支持通话
    val supportsMedia: Boolean,         // 是否支持媒体播放
    val batteryLevel: Int = -1,         // 电池电量，-1表示未知
    val isCurrentCallDevice: Boolean,   // 是否为当前通话设备
    val isCurrentMediaDevice: Boolean   // 是否为当前媒体设备
) {
    // 设备图标类型
    fun getDeviceType(): DeviceType {
        return when {
            deviceClass == BluetoothClass.Device.Major.PHONE -> DeviceType.PHONE
            deviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO &&
                    (name.contains("car", ignoreCase = true) ||
                            name.contains("车", ignoreCase = true) ||
                            name.contains("汽车", ignoreCase = true)) -> DeviceType.CAR
            deviceClass == BluetoothClass.Device.Major.AUDIO_VIDEO -> DeviceType.HEADSET
            else -> DeviceType.OTHER
        }
    }

    // 是否已配对
    fun isPaired(): Boolean = bondState == BluetoothDevice.BOND_BONDED

    // 是否为对讲模式
    fun isIntercomMode(): Boolean = supportsCalling && !supportsMedia

    // 是否为娱乐模式
    fun isEntertainmentMode(): Boolean = !supportsCalling && supportsMedia

    // 是否为全功能模式
    fun isFullFunctionMode(): Boolean = supportsCalling && supportsMedia
}

/**
 * 设备类型枚举
 */
enum class DeviceType {
    PHONE,      // 手机
    HEADSET,    // 耳机
    CAR,        // 车载设备
    OTHER       // 其他
}

/**
 * 蓝牙通话信息
 */
data class BluetoothCallInfo(
    val callId: String,                 // 通话ID
    val deviceAddress: String,          // 设备地址
    val deviceName: String,             // 设备名称
    val phoneNumber: String? = null,    // 电话号码
    val startTime: Long,                // 开始时间
    var endTime: Long? = null,          // 结束时间
    var duration: Long = 0,             // 通话时长（秒）
    val state: CallState               // 通话状态
) {
    // 是否为来电
    fun isIncoming(): Boolean = state == CallState.INCOMING

    // 是否为去电
    fun isOutgoing(): Boolean = state == CallState.DIALING || state == CallState.ALERTING

    // 是否活动通话
    fun isActive(): Boolean = state == CallState.ACTIVE

    // 是否已结束通话
    fun isTerminated(): Boolean = state == CallState.TERMINATED

    // 计算通话时长
    fun calculateDuration(): Long {
        return if (endTime != null) {
            (endTime!! - startTime) / 1000
        } else if (isActive()) {
            (System.currentTimeMillis() - startTime) / 1000
        } else {
            0
        }
    }
}

/**
 * 通话状态枚举
 */
enum class CallState {
    UNKNOWN,     // 未知状态
    ACTIVE,      // 通话中
    HELD,        // 通话保持
    DIALING,     // 正在拨号
    ALERTING,    // 对方响铃中
    INCOMING,    // 来电
    WAITING,     // 呼叫等待
    TERMINATED   // 已结束
}

/**
 * 蓝牙状态回调接口
 */
interface BluetoothLibCallback {
    // 蓝牙开关状态变化
    fun onBluetoothStateChanged(enabled: Boolean) {}

    // 蓝牙服务连接
    fun onBluetoothServiceConnected(profile: Int) {}

    // 蓝牙服务断开
    fun onBluetoothServiceDisconnected(profile: Int) {}

    // 已配对设备列表变化
    fun onPairedDevicesChanged(devices: List<BluetoothDeviceInfo>) {}

    // 已连接设备列表变化
    fun onConnectedDevicesChanged(devices: List<BluetoothDeviceInfo>) {}

    // 可发现设备列表变化
    fun onDiscoverableDevicesChanged(devices: List<BluetoothDeviceInfo>) {}

    // 设备发现完成
    fun onDiscoveryFinished() {}

    // 设备连接
    fun onDeviceConnected(device: BluetoothDeviceInfo) {}

    // 设备断开
    fun onDeviceDisconnected(device: BluetoothDeviceInfo) {}

    // 设备配对
    fun onDevicePaired(device: BluetoothDeviceInfo) {}

    // 设备取消配对
    fun onDeviceUnpaired(device: BluetoothDeviceInfo) {}

    // 通话设备连接
    fun onCallDeviceConnected(device: BluetoothDeviceInfo) {}

    // 通话设备断开
    fun onCallDeviceDisconnected(device: BluetoothDeviceInfo) {}

    // 媒体设备连接
    fun onMediaDeviceConnected(device: BluetoothDeviceInfo) {}

    // 媒体设备断开
    fun onMediaDeviceDisconnected(device: BluetoothDeviceInfo) {}

    // 通话开始
    fun onCallStarted(call: BluetoothCallInfo) {}

    // 通话结束
    fun onCallEnded(call: BluetoothCallInfo) {}

    // 通话信息更新
    fun onCallUpdated(call: BluetoothCallInfo) {}

    // 来电铃声
    fun onRingReceived(deviceAddress: String) {}

    // 呼叫等待
    fun onCallWaiting(deviceAddress: String, number: String?) {}

    // 媒体播放开始
    fun onMediaPlaybackStarted(device: BluetoothDeviceInfo) {}

    // 媒体播放停止
    fun onMediaPlaybackStopped(device: BluetoothDeviceInfo) {}

    // SCO音频连接
    fun onScoAudioConnected() {}

    // SCO音频断开
    fun onScoAudioDisconnected() {}
}
