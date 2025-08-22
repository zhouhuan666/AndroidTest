package com.gdet.testapp;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.view.View;
import android.widget.LinearLayout;

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
import com.gdet.testapp.mvi.simple.CounterActivity;
import com.gdet.testapp.mvi.complete.UserListActivity;
import com.gdet.testapp.myindexbar.ContactIndexActivity;
import com.gdet.testapp.radiogroup.GroupActivity;
import com.gdet.testapp.retrofit.RetrofitActivity;
import com.gdet.testapp.rxjava.RxjavaActivity;
import com.gdet.testapp.touchevent.TouchEventActivity;
import com.gdet.testapp.view.ViewActivity;
import com.gdet.testapp.widget.MainRecyclerViewAdapter;
import com.gdet.testapp.hilt.presentation.HiltDemoActivity;
import com.gdet.testapp.jni.JniTestActivity;
import com.gdet.testapp.opencv.OpenCVDemoActivity;
import com.gdet.testapp.opencv.OpenCVHelper;

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
        put(17, CounterActivity.class);
        put(18, UserListActivity.class);
        put(19, HiltDemoActivity.class);
        put(20, JniTestActivity.class);
        put(21, OpenCVDemoActivity.class);
        put(22, com.gdet.testapp.someip.demo.SomeIpDemoActivity.class);
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

    // 在onCreate方法中添加SOME/IP演示按钮
    private void addSomeIpDemoButton(LinearLayout layout) {
        Button someIpDemoButton = new Button(this);
        someIpDemoButton.setText("SOME/IP演示");
        someIpDemoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, 
                    com.gdet.testapp.someip.demo.SomeIpDemoActivity.class);
                startActivity(intent);
            }
        });
        layout.addView(someIpDemoButton);
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
                "CoilActivity","ViewActivity","IndexBarActivity","ContactIndexActivity","BluetoothActivity",
                "MVI计数器示例", "MVI用户列表示例", "Hilt依赖注入示例", "JNI测试示例", "OpenCV图像处理演示", "SOME/IP演示"
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
                // 特殊处理OpenCV演示Activity
                if(targetActivity == OpenCVDemoActivity.class) {
                    handleOpenCVDemo();
                } else {
                    startActivity(new Intent(MainActivity.this, targetActivity));
                }
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

    /**
     * 处理OpenCV演示Activity的启动
     * 检查OpenCV库是否可用，并给出相应的提示
     */
    private void handleOpenCVDemo() {
        try {
            // 检查OpenCV是否可用
            if (OpenCVHelper.isOpenCVAvailable()) {
                // OpenCV可用，直接启动演示Activity
                Intent intent = new Intent(this, OpenCVDemoActivity.class);
                startActivity(intent);
                Log.i(TAG, "启动OpenCV演示Activity - OpenCV库已加载");
            } else {
                // OpenCV不可用，显示提示信息
                showOpenCVUnavailableDialog();
                Log.w(TAG, "OpenCV库未加载，显示配置提示");
            }
        } catch (Exception e) {
            // 处理可能的异常
            Log.e(TAG, "启动OpenCV演示时发生错误: " + e.getMessage(), e);
            showOpenCVErrorDialog(e.getMessage());
        }
    }

    /**
     * 显示OpenCV不可用的对话框
     */
    private void showOpenCVUnavailableDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("OpenCV功能不可用")
                .setMessage("OpenCV库尚未配置或加载失败。\n\n" +
                           "要启用OpenCV功能，请按以下步骤操作：\n" +
                           "1. 下载OpenCV Android SDK\n" +
                           "2. 解压到项目的libs/opencv/目录\n" +
                           "3. 重新编译项目\n\n" +
                           "详细说明请参考OPENCV_SETUP_GUIDE.md文件。\n\n" +
                           "是否仍要查看演示界面？（功能将不可用）")
                .setPositiveButton("查看演示界面", (dialog, which) -> {
                    // 仍然启动Activity，但功能不可用
                    Intent intent = new Intent(this, OpenCVDemoActivity.class);
                    startActivity(intent);
                })
                .setNegativeButton("取消", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("查看配置说明", (dialog, which) -> {
                    // 这里可以打开配置说明，或者显示更详细的帮助信息
                    showOpenCVSetupHelp();
                })
                .show();
    }

    /**
     * 显示OpenCV错误对话框
     */
    private void showOpenCVErrorDialog(String errorMessage) {
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("OpenCV启动错误")
                .setMessage("启动OpenCV演示时发生错误：\n\n" + errorMessage + 
                           "\n\n请检查OpenCV库的配置和安装。")
                .setPositiveButton("确定", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("查看帮助", (dialog, which) -> showOpenCVSetupHelp())
                .show();
    }

    /**
     * 显示OpenCV配置帮助信息
     */
    private void showOpenCVSetupHelp() {
        String helpMessage = "OpenCV Android集成配置步骤：\n\n" +
                            "📦 快速开始（当前状态）\n" +
                            "• 项目可正常运行，但OpenCV功能不可用\n" +
                            "• 这是正常的，OpenCV为可选功能\n\n" +
                            "🔧 完整OpenCV集成\n" +
                            "1. 下载OpenCV Android SDK\n" +
                            "   https://opencv.org/releases/\n\n" +
                            "2. 解压到项目目录\n" +
                            "   AndroidTest/libs/opencv/\n\n" +
                            "3. 重新编译项目\n" +
                            "   ./gradlew clean && ./gradlew assembleDebug\n\n" +
                            "📱 验证安装\n" +
                            "• 编译日志显示\"OpenCV found and enabled!\"\n" +
                            "• 本演示界面所有功能可正常使用\n\n" +
                            "📖 详细说明请查看项目根目录的\n" +
                            "OPENCV_SETUP_GUIDE.md文件";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("OpenCV配置帮助")
                .setMessage(helpMessage)
                .setPositiveButton("我知道了", (dialog, which) -> dialog.dismiss())
                .show();
    }
}

