/*
 * Copyright (C) 2011 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.android.settingslib.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.BluetoothStatusCodes;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.ParcelUuid;
import android.util.Log;

import java.time.Duration;
import java.util.List;
import java.util.Set;

/**
 * LocalBluetoothAdapter provides an interface between the Settings app
 * and the functionality of the local {@link BluetoothAdapter}, specifically
 * those related to state transitions of the adapter itself.
 *
 * <p>Connection and bonding state changes affecting specific devices
 * are handled by {@link CachedBluetoothDeviceManager},
 * {@link BluetoothEventManager}, and {@link LocalBluetoothProfileManager}.
 *
 * @deprecated use {@link BluetoothAdapter} instead.
 */
@Deprecated
public class LocalBluetoothAdapter {
    private static final String TAG = "LocalBluetoothAdapter";

    /** This class does not allow direct access to the BluetoothAdapter. */
    private final BluetoothAdapter mAdapter;

    private LocalBluetoothProfileManager mProfileManager;

    private static LocalBluetoothAdapter sInstance;

    private int mState = BluetoothAdapter.ERROR;

    private static final int SCAN_EXPIRATION_MS = 5 * 60 * 1000; // 5 mins

    private long mLastScan;

    private LocalBluetoothAdapter(BluetoothAdapter adapter) {
        mAdapter = adapter;
    }

    void setProfileManager(LocalBluetoothProfileManager manager) {
        mProfileManager = manager;
    }

    /**
     * Get the singleton instance of the LocalBluetoothAdapter. If this device
     * doesn't support Bluetooth, then null will be returned. Callers must be
     * prepared to handle a null return value.
     * @return the LocalBluetoothAdapter object, or null if not supported
     */
    static synchronized LocalBluetoothAdapter getInstance() {
        if (sInstance == null) {
            BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
            if (adapter != null) {
                sInstance = new LocalBluetoothAdapter(adapter);
            }
        }

        return sInstance;
    }

    // Pass-through BluetoothAdapter methods that we can intercept if necessary

    public void cancelDiscovery() {
        mAdapter.cancelDiscovery();
    }

    public boolean enable() {
        return mAdapter.enable();
    }

    public boolean disable() {
        return mAdapter.disable();
    }

    public String getAddress() {
        return mAdapter.getAddress();
    }

    void getProfileProxy(Context context,
                         BluetoothProfile.ServiceListener listener, int profile) {
        mAdapter.getProfileProxy(context, listener, profile);
    }

    public Set<BluetoothDevice> getBondedDevices() {
        return mAdapter.getBondedDevices();
    }

    public String getName() {
        return mAdapter.getName();
    }

    public int getScanMode() {
        return mAdapter.getScanMode();
    }

    public BluetoothLeScanner getBluetoothLeScanner() {
        return mAdapter.getBluetoothLeScanner();
    }

    public int getState() {
        return mAdapter.getState();
    }

    public ParcelUuid[] getUuids() {
        List<ParcelUuid> uuidsList = mAdapter.getUuidsList();
        ParcelUuid[] uuidsArray = new ParcelUuid[uuidsList.size()];
        uuidsList.toArray(uuidsArray);
        return uuidsArray;
    }

    public boolean isDiscovering() {
        return mAdapter.isDiscovering();
    }

    public boolean isEnabled() {
        return mAdapter.isEnabled();
    }

    public int getConnectionState() {
        return mAdapter.getConnectionState();
    }

    public void setDiscoverableTimeout(int timeout) {
        mAdapter.setDiscoverableTimeout(Duration.ofSeconds(timeout));
    }

    public long getDiscoveryEndMillis() {
        return mAdapter.getDiscoveryEndMillis();
    }

    public void setName(String name) {
        mAdapter.setName(name);
    }

    public void setScanMode(int mode) {
        mAdapter.setScanMode(mode);
    }

    public boolean setScanMode(int mode, int duration) {
        return (mAdapter.setDiscoverableTimeout(Duration.ofSeconds(duration))
                == BluetoothStatusCodes.SUCCESS
                && mAdapter.setScanMode(mode) == BluetoothStatusCodes.SUCCESS);
    }

    public void startScanning(boolean force) {
        // Only start if we're not already scanning
        if (!mAdapter.isDiscovering()) {
            if (!force) {
                // Don't scan more than frequently than SCAN_EXPIRATION_MS,
                // unless forced
                if (mLastScan + SCAN_EXPIRATION_MS > System.currentTimeMillis()) {
                    return;
                }

                // If we are playing music, don't scan unless forced.
                A2dpProfile a2dp = mProfileManager.getA2dpProfile();
                if (a2dp != null && a2dp.isA2dpPlaying()) {
                    return;
                }
                A2dpSinkProfile a2dpSink = mProfileManager.getA2dpSinkProfile();
                if ((a2dpSink != null) && (a2dpSink.isAudioPlaying())) {
                    return;
                }
            }

            if (mAdapter.startDiscovery()) {
                mLastScan = System.currentTimeMillis();
            }
        }
    }

    public void stopScanning() {
        if (mAdapter.isDiscovering()) {
            mAdapter.cancelDiscovery();
        }
    }

    public synchronized int getBluetoothState() {
        // Always sync state, in case it changed while paused
        syncBluetoothState();
        return mState;
    }

    void setBluetoothStateInt(int state) {
        synchronized(this) {
            if (mState == state) {
                return;
            }
            mState = state;
        }

        if (state == BluetoothAdapter.STATE_ON) {
            // if mProfileManager hasn't been constructed yet, it will
            // get the adapter UUIDs in its constructor when it is.
            if (mProfileManager != null) {
                mProfileManager.setBluetoothStateOn();
            }
        }
    }

    // Returns true if the state changed; false otherwise.
    boolean syncBluetoothState() {
        int currentState = mAdapter.getState();
        if (currentState != mState) {
            setBluetoothStateInt(mAdapter.getState());
            return true;
        }
        return false;
    }

    public boolean setBluetoothEnabled(boolean enabled) {
        boolean success = enabled
                ? mAdapter.enable()
                : mAdapter.disable();

        if (success) {
            setBluetoothStateInt(enabled
                ? BluetoothAdapter.STATE_TURNING_ON
                : BluetoothAdapter.STATE_TURNING_OFF);
        } else {
            if (BluetoothUtils.V) {
                Log.v(TAG, "setBluetoothEnabled call, manager didn't return " +
                        "success for enabled: " + enabled);
            }

            syncBluetoothState();
        }
        return success;
    }

    public BluetoothDevice getRemoteDevice(String address) {
        return mAdapter.getRemoteDevice(address);
    }

    public List<Integer> getSupportedProfiles() {
        return mAdapter.getSupportedProfiles();
    }
}
