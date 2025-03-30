package com.gdet.testapp.bluetooth.car

import android.annotation.SuppressLint
import android.content.Context

/**
 * 车载蓝牙管理器 - 模块化重构版本
 * 作为其他子模块的门面(Facade)，提供统一的访问接口
 */
@SuppressLint("MissingPermission")
class CarBluetoothManager private constructor(private val context: Context) {

    companion object {
        private const val TAG = "CarBluetoothManager"

        @Volatile
        private var instance: CarBluetoothManager? = null

        fun getInstance(context: Context): CarBluetoothManager {
            return instance ?: synchronized(this) {
                instance ?: CarBluetoothManager(context.applicationContext).also { instance = it }
            }
        }
    }

    // 蓝牙核心
    private val bluetoothCore = CarBluetoothCore.getInstance(context)

    // 设备管理器
    private val deviceManager = CarBluetoothDeviceManager(context)

    // 通话管理器
    private val callManager = CarBluetoothCallManager(context)

    // 媒体管理器
    private val mediaManager = CarBluetoothMediaManager(context)

    // 事件分发器
    private val eventDispatcher: BluetoothEventDispatcher
        get() = bluetoothCore.eventDispatcher

    /**
     * 添加蓝牙状态回调
     */
    fun addCallback(callback: BluetoothLibCallback) {
        eventDispatcher.addListener(callback)
    }

    /**
     * 移除蓝牙状态回调
     */
    fun removeCallback(callback: BluetoothLibCallback) {
        eventDispatcher.removeListener(callback)
    }

    // ========== 蓝牙基础功能 ==========

    /**
     * 开启蓝牙
     */
    fun enableBluetooth(): Boolean {
        return bluetoothCore.enableBluetooth()
    }

    /**
     * 关闭蓝牙
     */
    fun disableBluetooth(): Boolean {
        return bluetoothCore.disableBluetooth()
    }

    /**
     * 蓝牙是否开启
     */
    fun isBluetoothEnabled(): Boolean {
        return bluetoothCore.isBluetoothEnabled()
    }

    // ========== 设备管理功能 ==========

    /**
     * 启动设备发现
     */
    fun startDiscovery(): Boolean {
        return deviceManager.startDiscovery()
    }

    /**
     * 停止设备发现
     */
    fun stopDiscovery() {
        deviceManager.stopDiscovery()
    }

    /**
     * 获取已配对设备列表
     */
    fun getPairedDevices(): List<BluetoothDeviceInfo> {
        return deviceManager.getPairedDevices()
    }

    /**
     * 获取可发现设备列表
     */
    fun getDiscoverableDevices(): List<BluetoothDeviceInfo> {
        return deviceManager.getDiscoverableDevices()
    }

    /**
     * 获取已连接设备列表
     */
    fun getConnectedDevices(): List<BluetoothDeviceInfo> {
        return deviceManager.getConnectedDevices()
    }

    /**
     * 连接设备
     */
    fun connectDevice(address: String): Boolean {
        return deviceManager.connectDevice(address)
    }

    /**
     * 断开设备连接
     */
    fun disconnectDevice(address: String): Boolean {
        return deviceManager.disconnectDevice(address)
    }

    /**
     * 设置设备名称
     */
    fun renameDevice(address: String, newName: String): Boolean {
        return deviceManager.renameDevice(address, newName)
    }

    /**
     * 忘记(取消配对)设备
     */
    fun forgetDevice(address: String): Boolean {
        return deviceManager.forgetDevice(address)
    }

    // ========== 通话管理功能 ==========

    /**
     * 连接设备作为电话设备
     */
    fun connectAsPhoneDevice(address: String): Boolean {
        return callManager.connectAsCallDevice(address)
    }

    /**
     * 接听来电
     */
    fun acceptCall(): Boolean {
        return callManager.acceptCall()
    }

    /**
     * 拒绝来电
     */
    fun rejectCall(): Boolean {
        return callManager.rejectCall()
    }

    /**
     * 结束通话
     */
    fun terminateCall(): Boolean {
        return callManager.terminateCall()
    }

    /**
     * 拨打电话
     */
    fun dialNumber(number: String): Boolean {
        return callManager.dialNumber(number)
    }

    /**
     * 切换音频通道
     */
    fun switchAudioChannel(useBluetooth: Boolean): Boolean {
        return callManager.switchAudioChannel(useBluetooth)
    }

    /**
     * 获取当前通话信息
     */
    fun getCurrentCallInfo(): BluetoothCallInfo? {
        return callManager.getCurrentCallInfo()
    }

    /**
     * 是否有活动通话
     */
    fun hasActiveCall(): Boolean {
        return callManager.hasActiveCall()
    }

    // ========== 媒体管理功能 ==========

    /**
     * 连接设备作为媒体设备
     */
    fun connectAsMediaDevice(address: String): Boolean {
        return mediaManager.connectAsMediaDevice(address)
    }

    /**
     * 播放/暂停媒体
     */
    fun togglePlayPause() {
        mediaManager.togglePlayPause()
    }

    /**
     * 上一首
     */
    fun playPrevious() {
        mediaManager.playPrevious()
    }

    /**
     * 下一首
     */
    fun playNext() {
        mediaManager.playNext()
    }

    /**
     * 是否正在播放媒体
     */
    fun isMediaPlaying(): Boolean {
        return mediaManager.isPlaying()
    }

    /**
     * 获取当前媒体设备
     */
    fun getCurrentMediaDevice(): BluetoothDeviceInfo? {
        return mediaManager.getCurrentMediaDevice()
    }

    // ========== 音频控制功能 ==========

    /**
     * 调整蓝牙音量
     */
    fun adjustVolume(increase: Boolean) {
        callManager.adjustVolume(increase)
    }

    /**
     * 设置蓝牙音量
     */
    fun setVolume(volume: Int) {
        callManager.setVolume(volume)
    }

    /**
     * 获取当前蓝牙音量
     */
    fun getCurrentVolume(): Int {
        return callManager.getCurrentVolume()
    }

    /**
     * 获取最大蓝牙音量
     */
    fun getMaxVolume(): Int {
        return callManager.getMaxVolume()
    }

    /**
     * 释放资源
     */
    fun release() {
        callManager.release()
        mediaManager.release()
        deviceManager.release()
        bluetoothCore.release()
        instance = null
    }
}