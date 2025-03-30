package com.gdet.testapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gdet.testapp.bluetooth.BluetoothActivity;
import com.gdet.testapp.coil.CoilActivity;
import com.gdet.testapp.compose.ComposeActivity;
import com.gdet.testapp.customctrlres.cp1.CustomCtrlResOneActivity;
import com.gdet.testapp.fragment.FragmentTestActivity;
import com.gdet.testapp.gesturedetector.GestureDetectorActivity;
import com.gdet.testapp.indexbar.IndexBarActivity;
import com.gdet.testapp.jetpack.lifecycle.LifecycleActivity;
import com.gdet.testapp.jetpack.livedata.LiveDataActivity;
import com.gdet.testapp.kotlin.coroutine.CoroutineActivity;
import com.gdet.testapp.kotlin.coroutine.OkHttpActivity;
import com.gdet.testapp.myindexbar.ContactIndexActivity;
import com.gdet.testapp.radiogroup.GroupActivity;
import com.gdet.testapp.retrofit.RetrofitActivity;
import com.gdet.testapp.rxjava.RxjavaActivity;
import com.gdet.testapp.touchevent.TouchEventActivity;
import com.gdet.testapp.view.ViewActivity;
import com.gdet.testapp.widget.MainRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-10-07
 * 描述：
 * 注解，单独没有意义
 * 注解+apt 用于生成一些java文件  butterknife dagger2  hilt databinding
 * 注解+代码埋点  aspactj arouter
 * 注解+反射 xutils。lifecycle
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";
    private RecyclerView recyclerView;
    private List<String> list;
    private MainRecyclerViewAdapter adapter;

    private final Map<Integer, Class<?>> activityMap = new HashMap<Integer, Class<?>>() {{
        put(0, GestureDetectorActivity.class);
        put(1, RxjavaActivity.class);
        put(2, TouchEventActivity.class);
        put(3, CoroutineActivity.class);
        put(4, OkHttpActivity.class);
        put(5, CustomCtrlResOneActivity.class);
        put(6, RetrofitActivity.class);
        put(7, LifecycleActivity.class);
        put(8, LiveDataActivity.class);
        put(9, FragmentTestActivity.class);
        put(10, ComposeActivity.class);
        put(11, GroupActivity.class);
        put(12, CoilActivity.class);
        put(13, ViewActivity.class);
        put(14, IndexBarActivity.class);
        put(15, ContactIndexActivity.class);
        put(16, BluetoothActivity.class);
    }};

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initData();
        initRecyclerView();
        initButton();
        requestBluetoothPermissions();
    }


    // 检查并请求蓝牙权限
    private void requestBluetoothPermissions() {
        Log.d(TAG, "requestBluetoothPermissions: ");
        // 存储需要请求的权限
        List<String> permissionsToRequest = new ArrayList<>();

        // 对于 Android 12 及以上版本，需要这些新的蓝牙权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.BLUETOOTH_SCAN);
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADVERTISE) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.BLUETOOTH_ADVERTISE);
            }
        } else {
            // 对于旧版本 Android，检查传统蓝牙权限
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.BLUETOOTH);
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(android.Manifest.permission.BLUETOOTH_CONNECT);
            }
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissionsToRequest.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
        }

        // 如果有需要请求的权限，则发起请求
        if (!permissionsToRequest.isEmpty()) {
            ActivityCompat.requestPermissions(this,
                    permissionsToRequest.toArray(new String[0]),
                    99);
        } else {
            // 已有所有权限，初始化蓝牙
//            initBt();
        }
    }

    // 处理权限请求结果
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 99) {
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
//                initBt();
            } else {
                // 权限被拒绝，处理这种情况
                Log.e(TAG, "蓝牙权限被拒绝");
                // 可以显示提示对话框或关闭活动
            }
        }
    }

    private void initData() {
        list = new ArrayList<>(Arrays.asList(
                "GestureDetector", "RxJava", "TouchEvent", "Coroutine",
                "Coroutine-OKHttp", "CustomCtrlResOne", "Retrofit", "Lifecycle",
                "LiveData", "FragmentTest", "ComposeActivity", "GroupActivity",
                "CoilActivity","ViewActivity","IndexBarActivity","ContactIndexActivity","BluetoothActivity"
        ));
    }

    private void initRecyclerView() {
        recyclerView = findViewById(R.id.rv);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new MainRecyclerViewAdapter(this, list);
        adapter.setOnItemClickListener((view, position) -> {
            Log.d(TAG, "onItemClick: position " + ((Button)view).getText() + position);
            Class<?> targetActivity = activityMap.get(position);
            if(targetActivity != null) {
                startActivity(new Intent(MainActivity.this, targetActivity));
            }
        });

        recyclerView.setAdapter(adapter);
    }

    private void initButton() {
        findViewById(R.id.bt1).setOnClickListener(v -> {
            list.clear();
            list.add("asdfsadfsadfasdfasdf");
            adapter.notifyDataSetChanged();
        });
    }
}

