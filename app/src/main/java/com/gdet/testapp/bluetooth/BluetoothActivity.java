package com.gdet.testapp.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class BluetoothActivity extends AppCompatActivity {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        BluetoothSocket socket = (BluetoothSocket) getIntent().getExtras().get("socket");
//        socket.create
    }
}
