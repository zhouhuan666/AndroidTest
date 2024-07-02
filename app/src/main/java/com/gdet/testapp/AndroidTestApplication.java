package com.gdet.testapp;

import android.app.Application;

import com.blankj.utilcode.util.Utils;

public class AndroidTestApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Utils.init(this);
    }
}
