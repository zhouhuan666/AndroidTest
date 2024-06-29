package com.gdet.testapp.jetpack.lifecycle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.LifecycleObserver;

public class LifecycleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(android.os.Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        MyObserver lifecycleObserver = new MyObserver();
        getLifecycle().addObserver(lifecycleObserver);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }
}
