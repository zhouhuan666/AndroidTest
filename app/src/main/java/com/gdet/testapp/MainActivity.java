package com.gdet.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gdet.testapp.annotation.HuanButterKnife;
import com.gdet.testapp.compose.ComposeActivity;
import com.gdet.testapp.customctrlres.cp1.CustomCtrlResOneActivity;
import com.gdet.testapp.fragment.FragmentTestActivity;
import com.gdet.testapp.gesturedetector.GestureDetectorActivity;
import com.gdet.testapp.jetpack.lifecycle.LifecycleActivity;
import com.gdet.testapp.jetpack.livedata.LiveDataActivity;
import com.gdet.testapp.kotlin.coroutine.CoroutineActivity;
import com.gdet.testapp.kotlin.coroutine.OkHttpActivity;
import com.gdet.testapp.radiogroup.GroupActivity;
import com.gdet.testapp.retrofit.RetrofitActivity;
import com.gdet.testapp.rxjava.RxjavaActivity;
import com.gdet.testapp.touchevent.TouchEventActivity;
import com.gdet.testapp.widget.MainRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

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


    TextView textView;

    private static final String TAG = "MainActivity";
//    @BindView(R.id.rv)
    RecyclerView recyclerView;
    List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
//        HuanButterKnife.bind(this);
        recyclerView = findViewById(R.id.rv);
        list.add("GestureDetector");
        list.add("RxJava");
        list.add("TouchEvent");
        list.add("Coroutine");
        list.add("Coroutine-OKHttp");
        list.add("CustomCtrlResOne");
        list.add("Retrofit");
        list.add("Lifecycle");
        list.add("LiveData");
        list.add("FragmentTest");
        list.add("ComposeActivity");
        list.add("GroupActivity");
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        MainRecyclerViewAdapter mainRecyclerViewAdapter = new MainRecyclerViewAdapter(this, list);
        mainRecyclerViewAdapter.setOnItemClickListener(new MainRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d(TAG, "onItemClick: position " +((Button)view).getText()+ position);
                if (position == 0) {
                    startActivity(new Intent(MainActivity.this, GestureDetectorActivity.class));
                } else if (position == 1) {
                    startActivity(new Intent(MainActivity.this, RxjavaActivity.class));
                } else if (position == 2) {
                    startActivity(new Intent(MainActivity.this, TouchEventActivity.class));
                } else if (position == 3) {
                    startActivity(new Intent(MainActivity.this, CoroutineActivity.class));
                }else if (position == 4) {
                    startActivity(new Intent(MainActivity.this, OkHttpActivity.class));
                }else if (position == 5) {
                    startActivity(new Intent(MainActivity.this, CustomCtrlResOneActivity.class));
                }else if (position ==6) {
                    startActivity(new Intent(MainActivity.this, RetrofitActivity.class));
                } else if (position ==7) {
                    startActivity(new Intent(MainActivity.this, LifecycleActivity.class));
                } else if (position ==8) {
                    startActivity(new Intent(MainActivity.this, LiveDataActivity.class));
                }else if (position ==9) {
                    startActivity(new Intent(MainActivity.this, FragmentTestActivity.class));
                } else if (position==10) {
                    startActivity(new Intent(MainActivity.this, ComposeActivity.class));
                }else if (position==11) {
                    startActivity(new Intent(MainActivity.this, GroupActivity.class));
                }
            }
        });

        recyclerView.setAdapter(mainRecyclerViewAdapter);

        Button button1=findViewById(R.id.bt1);
        button1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                list.clear();
                list.add("asdfsadfsadfasdfasdf");
                mainRecyclerViewAdapter.notifyDataSetChanged();
            }
        });


    }
}
