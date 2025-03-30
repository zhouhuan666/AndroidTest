package com.gdet.testapp.bluetooth.car

import java.util.concurrent.CopyOnWriteArraySet

/**
 * 蓝牙事件分发器 - 负责将蓝牙事件分发给监听器
 */
class BluetoothEventDispatcher {

    // 监听器集合
    private val listeners = CopyOnWriteArraySet<BluetoothLibCallback>()

    /**
     * 添加监听器
     */
    fun addListener(listener: BluetoothLibCallback) {
        listeners.add(listener)
    }

    /**
     * 移除监听器
     */
    fun removeListener(listener: BluetoothLibCallback) {
        listeners.remove(listener)
    }

    /**
     * 清空所有监听器
     */
    fun clear() {
        listeners.clear()
    }

    // ========== 通知方法 ==========

    fun dispatchBluetoothStateChanged(enabled: Boolean) {
        listeners.forEach { it.onBluetoothStateChanged(enabled) }
    }

    fun dispatchServiceConnected(profile: Int) {
        listeners.forEach { it.onBluetoothServiceConnected(profile) }
    }

    fun dispatchServiceDisconnected(profile: Int) {
        listeners.forEach { it.onBluetoothServiceDisconnected(profile) }
    }

    fun dispatchPairedDevicesChanged(devices: List<BluetoothDeviceInfo>) {
        listeners.forEach { it.onPairedDevicesChanged(devices) }
    }

    fun dispatchConnectedDevicesChanged(devices: List<BluetoothDeviceInfo>) {
        listeners.forEach { it.onConnectedDevicesChanged(devices) }
    }

    fun dispatchDiscoverableDevicesChanged(devices: List<BluetoothDeviceInfo>) {
        listeners.forEach { it.onDiscoverableDevicesChanged(devices) }
    }

    fun dispatchDiscoveryFinished() {
        listeners.forEach { it.onDiscoveryFinished() }
    }

    fun dispatchDeviceConnected(device: BluetoothDeviceInfo) {
        listeners.forEach { it.onDeviceConnected(device) }
    }

    fun dispatchDeviceDisconnected(device: BluetoothDeviceInfo) {
        listeners.forEach { it.onDeviceDisconnected(device) }
    }

    fun dispatchDevicePaired(device: BluetoothDeviceInfo) {
        listeners.forEach { it.onDevicePaired(device) }
    }

    fun dispatchDeviceUnpaired(device: BluetoothDeviceInfo) {
        listeners.forEach { it.onDeviceUnpaired(device) }
    }

    fun dispatchCallDeviceConnected(device: BluetoothDeviceInfo) {
        listeners.forEach { it.onCallDeviceConnected(device) }
    }

    fun dispatchCallDeviceDisconnected(device: BluetoothDeviceInfo) {
        listeners.forEach { it.onCallDeviceDisconnected(device) }
    }

    fun dispatchMediaDeviceConnected(device: BluetoothDeviceInfo) {
        listeners.forEach { it.onMediaDeviceConnected(device) }
    }

    fun dispatchMediaDeviceDisconnected(device: BluetoothDeviceInfo) {
        listeners.forEach { it.onMediaDeviceDisconnected(device) }
    }

    fun dispatchCallStarted(call: BluetoothCallInfo) {
        listeners.forEach { it.onCallStarted(call) }
    }

    fun dispatchCallEnded(call: BluetoothCallInfo) {
        listeners.forEach { it.onCallEnded(call) }
    }

    fun dispatchCallUpdated(call: BluetoothCallInfo) {
        listeners.forEach { it.onCallUpdated(call) }
    }

    fun dispatchMediaPlaybackStarted(device: BluetoothDeviceInfo) {
        listeners.forEach { it.onMediaPlaybackStarted(device) }
    }

    fun dispatchMediaPlaybackStopped(device: BluetoothDeviceInfo) {
        listeners.forEach { it.onMediaPlaybackStopped(device) }
    }

    fun dispatchScoAudioConnected() {
        listeners.forEach { it.onScoAudioConnected() }
    }

    fun dispatchScoAudioDisconnected() {
        listeners.forEach { it.onScoAudioDisconnected() }
    }

    fun dispatchRingEvent(deviceAddress: String) {
        listeners.forEach { it.onRingReceived(deviceAddress) }
    }

    fun dispatchCallWaitingEvent(deviceAddress: String, number: String?) {
        listeners.forEach { it.onCallWaiting(deviceAddress, number) }
    }
}