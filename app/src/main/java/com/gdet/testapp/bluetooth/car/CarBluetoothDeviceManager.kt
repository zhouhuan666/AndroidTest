package com.gdet.testapp.bluetooth.car

import android.annotation.SuppressLint
import android.bluetooth.BluetoothA2dp
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.bluetooth.BluetoothHeadsetClient
import android.bluetooth.BluetoothProfile

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import java.util.concurrent.CopyOnWriteArrayList

/**
 * 车载蓝牙设备管理类 - 负责设备发现、配对和连接管理
 */
@SuppressLint("MissingPermission")
class CarBluetoothDeviceManager(private val context: Context) {

    companion object {
        private const val TAG = "CarBluetoothDeviceManager"
    }

    // 蓝牙核心
    private val bluetoothCore = CarBluetoothCore.getInstance(context)

    // 已配对的设备列表
    private val pairedDevices = CopyOnWriteArrayList<BluetoothDevice>()

    // 可发现的设备列表(未配对)
    private val discoverableDevices = CopyOnWriteArrayList<BluetoothDevice>()

    // 连接的设备列表
    private val connectedDevices = CopyOnWriteArrayList<BluetoothDevice>()

    // 事件分发器
    val eventDispatcher: BluetoothEventDispatcher
        get() = bluetoothCore.eventDispatcher

    // 设备发现广播接收器
    private val deviceDiscoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return

            when (action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    handleDeviceFound(device)
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    eventDispatcher.dispatchDiscoveryFinished()
                }
                BluetoothDevice.ACTION_BOND_STATE_CHANGED -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                    handleBondStateChanged(device, bondState)
                }
                BluetoothDevice.ACTION_ACL_CONNECTED -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    handleDeviceConnected(device)
                }
                BluetoothDevice.ACTION_ACL_DISCONNECTED -> {
                    val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE)
                    handleDeviceDisconnected(device)
                }
            }
        }
    }

    init {
        // 注册设备发现广播接收器
        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
            addAction(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
            addAction(BluetoothDevice.ACTION_ACL_CONNECTED)
            addAction(BluetoothDevice.ACTION_ACL_DISCONNECTED)
        }
        context.registerReceiver(deviceDiscoveryReceiver, filter)

        // 初始化已配对设备列表
        updatePairedDevices()
    }

    /**
     * 启动设备发现
     */
    fun startDiscovery(): Boolean {
        return bluetoothCore.bluetoothAdapter?.let {
            if (it.isDiscovering) {
                it.cancelDiscovery()
            }
            discoverableDevices.clear()
            eventDispatcher.dispatchDiscoverableDevicesChanged(emptyList())
            it.startDiscovery()
        } ?: false
    }

    /**
     * 停止设备发现
     */
    fun stopDiscovery() {
        bluetoothCore.bluetoothAdapter?.cancelDiscovery()
    }

    /**
     * 获取已配对设备列表
     */
    fun getPairedDevices(): List<BluetoothDeviceInfo> {
        return pairedDevices.map { convertToDeviceInfo(it) }
    }

    /**
     * 获取可发现设备列表
     */
    fun getDiscoverableDevices(): List<BluetoothDeviceInfo> {
        return discoverableDevices.map { convertToDeviceInfo(it) }
    }

    /**
     * 获取已连接设备列表
     */
    fun getConnectedDevices(): List<BluetoothDeviceInfo> {
        return connectedDevices.map { convertToDeviceInfo(it) }
    }

    /**
     * 连接设备
     */
    fun connectDevice(address: String): Boolean {
        stopDiscovery()

        val device = findDeviceByAddress(address) ?: return false

        // 如果设备未配对，先配对
        if (device.bondState != BluetoothDevice.BOND_BONDED) {
            device.createBond()
            return false // 等待配对完成后再连接
        }

        // 连接HFP Client和A2DP
        bluetoothCore.initProfileProxy(BluetoothProfile.HEADSET_CLIENT) { success ->
            if (success) {
                connectHeadsetClientProfile(device)
            }
        }

        bluetoothCore.initProfileProxy(BluetoothProfile.A2DP) { success ->
            if (success) {
                connectA2dpProfile(device)
            }
        }

        return true
    }

    /**
     * 断开设备连接
     */
    fun disconnectDevice(address: String): Boolean {
        val device = findDeviceByAddress(address) ?: return false

        // 断开HFP Client
        bluetoothCore.getProfileProxy<BluetoothHeadsetClient>(BluetoothProfile.HEADSET_CLIENT)?.let {
            if (it.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED) {
                it.disconnect(device)
            }
        }

        // 断开A2DP
        bluetoothCore.getProfileProxy<BluetoothA2dp>(BluetoothProfile.A2DP)?.let {
            if (it.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED) {
                it.disconnect(device)
            }
        }

        return true
    }

    /**
     * 设置设备名称
     */
    fun renameDevice(address: String, newName: String): Boolean {
        val device = findDeviceByAddress(address) ?: return false

        try {
            device.setAlias(newName)
            updatePairedDevices()
            return true
        } catch (e: Exception) {
            Log.e(TAG, "重命名设备失败", e)
            return false
        }
    }

    /**
     * 忘记(取消配对)设备
     */
    fun forgetDevice(address: String): Boolean {
        val device = findDeviceByAddress(address) ?: return false
        return device.removeBond()
    }

    /**
     * 查找设备
     */
    fun findDeviceByAddress(address: String): BluetoothDevice? {
        // 先从已连接设备查找
        connectedDevices.find { it.address == address }?.let { return it }

        // 再从已配对设备查找
        pairedDevices.find { it.address == address }?.let { return it }

        // 最后从可发现设备查找
        discoverableDevices.find { it.address == address }?.let { return it }

        // 通过蓝牙适配器直接获取
        return try {
            bluetoothCore.bluetoothAdapter?.getRemoteDevice(address)
        } catch (e: Exception) {
            Log.e(TAG, "通过地址获取蓝牙设备失败: $address", e)
            null
        }
    }

    /**
     * 更新已配对设备列表
     */
    fun updatePairedDevices() {
        pairedDevices.clear()
        bluetoothCore.bluetoothAdapter?.bondedDevices?.let { pairedDevices.addAll(it) }
        val deviceInfos = pairedDevices.map { convertToDeviceInfo(it) }
        eventDispatcher.dispatchPairedDevicesChanged(deviceInfos)
    }

    /**
     * 将设备转换为设备信息对象
     */
    private fun convertToDeviceInfo(device: BluetoothDevice): BluetoothDeviceInfo {
        val headsetConnected = bluetoothCore.getProfileProxy<BluetoothHeadsetClient>(
            BluetoothProfile.HEADSET_CLIENT
        )?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED

        val a2dpConnected = bluetoothCore.getProfileProxy<BluetoothA2dp>(
            BluetoothProfile.A2DP
        )?.getConnectionState(device) == BluetoothProfile.STATE_CONNECTED

        return BluetoothDeviceInfo(
            address = device.address,
            name = device.name ?: "未知设备",
            deviceClass = device.bluetoothClass?.majorDeviceClass ?: 0,
            bondState = device.bondState,
            isConnected = headsetConnected || a2dpConnected,
            supportsCalling = headsetConnected,
            supportsMedia = a2dpConnected,
            batteryLevel = device.getBatteryLevel(),
            isCurrentCallDevice = false,  // 由CallManager设置
            isCurrentMediaDevice = false  // 由MediaManager设置
        )
    }

    /**
     * 连接HFP Client配置文件
     */
    private fun connectHeadsetClientProfile(device: BluetoothDevice) {
        bluetoothCore.getProfileProxy<BluetoothHeadsetClient>(BluetoothProfile.HEADSET_CLIENT)?.let {
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
     * 连接A2DP配置文件
     */
    private fun connectA2dpProfile(device: BluetoothDevice) {
        bluetoothCore.getProfileProxy<BluetoothA2dp>(BluetoothProfile.A2DP)?.let {
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
     * 处理发现设备
     */
    private fun handleDeviceFound(device: BluetoothDevice?) {
        device?.let {
            // 过滤掉已配对的设备
            if (it.bondState != BluetoothDevice.BOND_BONDED && !discoverableDevices.contains(it)) {
                discoverableDevices.add(it)
                val deviceInfos = discoverableDevices.map { dev -> convertToDeviceInfo(dev) }
                eventDispatcher.dispatchDiscoverableDevicesChanged(deviceInfos)
            }
        }
    }

    /**
     * 处理配对状态变化
     */
    private fun handleBondStateChanged(device: BluetoothDevice?, bondState: Int) {
        device?.let {
            when (bondState) {
                BluetoothDevice.BOND_BONDED -> {
                    // 配对成功
                    if (!pairedDevices.contains(it)) {
                        pairedDevices.add(it)
                    }
                    discoverableDevices.remove(it)

                    updatePairedDevices()
                    val deviceInfos = discoverableDevices.map { dev -> convertToDeviceInfo(dev) }
                    eventDispatcher.dispatchDiscoverableDevicesChanged(deviceInfos)
                    eventDispatcher.dispatchDevicePaired(convertToDeviceInfo(it))

                    // 自动连接新配对的设备
                    connectDevice(it.address)
                }
                BluetoothDevice.BOND_NONE -> {
                    // 取消配对
                    pairedDevices.remove(it)
                    connectedDevices.remove(it)

                    updatePairedDevices()
                    val deviceInfos = connectedDevices.map { dev -> convertToDeviceInfo(dev) }
                    eventDispatcher.dispatchConnectedDevicesChanged(deviceInfos)
                    eventDispatcher.dispatchDeviceUnpaired(convertToDeviceInfo(it))
                }

                else -> {}
            }
        }
    }

    /**
     * 处理设备连接
     */
    private fun handleDeviceConnected(device: BluetoothDevice?) {
        device?.let {
            if (!connectedDevices.contains(it)) {
                connectedDevices.add(it)
                val deviceInfos = connectedDevices.map { dev -> convertToDeviceInfo(dev) }
                eventDispatcher.dispatchConnectedDevicesChanged(deviceInfos)
                eventDispatcher.dispatchDeviceConnected(convertToDeviceInfo(it))
            }
        }
    }

    /**
     * 处理设备断开
     */
    private fun handleDeviceDisconnected(device: BluetoothDevice?) {
        device?.let {
            if (connectedDevices.contains(it)) {
                connectedDevices.remove(it)
                val deviceInfos = connectedDevices.map { dev -> convertToDeviceInfo(dev) }
                eventDispatcher.dispatchConnectedDevicesChanged(deviceInfos)
                eventDispatcher.dispatchDeviceDisconnected(convertToDeviceInfo(it))
            }
        }
    }

    /**
     * 释放资源
     */
    fun release() {
        try {
            context.unregisterReceiver(deviceDiscoveryReceiver)
        } catch (e: Exception) {
            Log.e(TAG, "解除设备发现广播接收器注册失败", e)
        }

        pairedDevices.clear()
        discoverableDevices.clear()
        connectedDevices.clear()
    }
}