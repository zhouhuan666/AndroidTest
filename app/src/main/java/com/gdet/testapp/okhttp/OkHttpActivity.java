package com.gdet.testapp.okhttp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gdet.testapp.R;

import java.io.IOException;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * Copyright (c) 2021-2023 极氪汽车（宁波杭州湾新区）有限公司.All rights reserved.
 *
 * @description:
 * @author: e-Huan.Zhou
 * @date: 2023/11/6 16:37:01
 * @version: V1.0
 */
public class OkHttpActivity extends AppCompatActivity {
    private static final String TAG = "OkHttpActivity";
    OkHttpClient okHttpClient = new OkHttpClient();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_okhttp);
        Log.d(TAG, "onCreate: ");
        doGetSync();
    }

    public void doGetSync() {
        new Thread(() -> {
            Request request = new Request.Builder().url("https://www.httpbin.org/get?name=test&b=123").build();

            try {
                Response response = okHttpClient.newCall(request).execute();
                Log.d(TAG, "doGetSync: " + response);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

        }).start();
    }

    void doGetAsync(View view) {
          Request request=new Request.Builder().url("https://www.httpbin.org/get?name=test&b=123\"");
    }
}
