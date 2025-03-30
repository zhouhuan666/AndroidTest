package com.gdet.testapp.bluetooth;


import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.settingslib.bluetooth.BluetoothCallback;
import com.android.settingslib.bluetooth.CachedBluetoothDevice;
import com.android.settingslib.bluetooth.LocalBluetoothManager;

import java.util.ArrayList;
import java.util.List;

public class BluetoothActivity extends AppCompatActivity {

    private static final String TAG = "BluetoothActivity";

    private static final int REQUEST_BLUETOOTH_PERMISSIONS = 100;

    LocalBluetoothManager localBluetoothManager;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        // 请求蓝牙权限
        requestBluetoothPermissions();


    }

    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_BLUETOOTH_PERMISSIONS) {
            boolean allPermissionsGranted = true;
            for (int result : grantResults) {
                if (result != PackageManager.PERMISSION_GRANTED) {
                    allPermissionsGranted = false;
                    break;
                }
            }
            Log.d(TAG, "onRequestPermissionsResult   " + allPermissionsGranted);

            if (allPermissionsGranted) {
                // 权限已获取，初始化蓝牙
                initBt();
            } else {
                // 权限被拒绝，处理这种情况
                Log.e(TAG, "蓝牙权限被拒绝");
                // 可以显示提示对话框或关闭活动
            }
        }
    }


    // 检查并请求蓝牙权限
    private void requestBluetoothPermissions() {
        // 存储需要请求的权限
        List<String> permissionsToRequest = new ArrayList<>();

        // 对于 Android 12 及以上版本，需要这些新的蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADVERTISE);
            }
        } else {
            // 对于旧版本 Android，检查传统蓝牙权限
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
        }

        // 如果有需要请求的权限，则发起请求
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    REQUEST_BLUETOOTH_PERMISSIONS);
        } else {
            // 已有所有权限，初始化蓝牙
            initBt();
        }
    }

    public void initBt() {

        LocalBluetoothManager.getInstance(this, new LocalBluetoothManager.BluetoothManagerCallback() {
            @Override
            public void onBluetoothManagerInitialized(Context appContext, LocalBluetoothManager bluetoothManager) {
                Log.d(TAG, "onBluetoothManagerInitialized: " + bluetoothManager);
                localBluetoothManager = bluetoothManager;
                initCallback();
            }
        });

    }

    public void initCallback() {
        localBluetoothManager.getEventManager().registerCallback(new BluetoothCallback() {
            @Override
            public void onBluetoothStateChanged(int bluetoothState) {
                Log.d(TAG, "onBluetoothStateChanged: " + bluetoothState);
            }

            @Override
            public void onScanningStateChanged(boolean started) {
                Log.d(TAG, "onScanningStateChanged: " + started);
            }

            @Override
            public void onDeviceAdded(CachedBluetoothDevice cachedDevice) {
                Log.d(TAG, "onDeviceAdded: " + cachedDevice);
            }

            @Override
            public void onDeviceDeleted(CachedBluetoothDevice cachedDevice) {
                Log.d(TAG, "onDeviceDeleted: " + cachedDevice);
            }

            @Override
            public void onDeviceBondStateChanged(CachedBluetoothDevice cachedDevice, int bondState) {
                Log.d(TAG, "onDeviceBondStateChanged: " + cachedDevice + ", bondState: " + bondState);
            }

            @Override
            public void onConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
                Log.d(TAG, "onConnectionStateChanged: " + cachedDevice + ", state: " + state);
            }

            @Override
            public void onActiveDeviceChanged(CachedBluetoothDevice activeDevice, int bluetoothProfile) {
                Log.d(TAG, "onActiveDeviceChanged: " + activeDevice + ", bluetoothProfile: " + bluetoothProfile);
            }

            @Override
            public void onAudioModeChanged() {
                Log.d(TAG, "onAudioModeChanged");
            }

            @Override
            public void onProfileConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state, int bluetoothProfile) {
                Log.d(TAG, "onProfileConnectionStateChanged: " + cachedDevice + ", state: " + state + ", bluetoothProfile: " + bluetoothProfile);
            }

            @Override
            public void onAclConnectionStateChanged(CachedBluetoothDevice cachedDevice, int state) {
                Log.d(TAG, "onAclConnectionStateChanged: " + cachedDevice + ", state: " + state);
            }
        });
    }
}
