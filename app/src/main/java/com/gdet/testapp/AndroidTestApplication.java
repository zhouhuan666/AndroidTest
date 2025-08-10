package com.gdet.testapp;

import android.app.Application;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.blankj.utilcode.util.Utils;
import com.fawcar.system.btphone.sdk.BtPhoneApi;
import com.fawcar.system.btphone.sdk.BtPhoneSDK;

import dagger.hilt.android.HiltAndroidApp;

/**
 * Hilt示例应用程序类
 * @HiltAndroidApp 注解告诉Hilt这是应用程序的入口点
 * 这个注解会触发Hilt的代码生成，包括一个作为应用程序依赖项容器的基类
 */
@HiltAndroidApp
public class AndroidTestApplication extends Application {
    private static final String TAG = "HiltApp";
    @Override
    public void onCreate() {
        super.onCreate();
        Log.d(TAG, "Hilt应用程序启动 - Application onCreate()");
        
        Utils.init(this);
//        BtPhoneApi btPhoneApi = BtPhoneSDK.getInstance().createServiceManager(this);
//        btPhoneApi.init();
        startService();
        
        Log.d(TAG, "Hilt依赖注入容器已准备就绪");
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
