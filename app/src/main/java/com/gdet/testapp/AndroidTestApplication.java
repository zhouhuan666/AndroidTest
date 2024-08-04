package com.gdet.testapp;

import android.app.Application;
import android.content.Intent;
import android.os.Build;

import com.blankj.utilcode.util.Utils;

public class AndroidTestApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
        startService();
    }

    private void startService() {
        // start swu
        Intent startServiceIntent = new Intent(this, WeatherService.class);
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            startForegroundService(startServiceIntent);
//        } else {
            startService(startServiceIntent);
//        }
    }
}
