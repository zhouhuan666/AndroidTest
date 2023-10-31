package com.gdet.testapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.gdet.testapp.gesturedetector.GestureDetectorActivity;
import com.gdet.testapp.widget.MainRecyclerViewAdapter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-10-07
 * 描述：
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    RecyclerView recyclerView;
    List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.rv);
        list.add("GestureDetector");
        LinearLayoutManager manager = new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false);
        recyclerView.setLayoutManager(manager);
        MainRecyclerViewAdapter mainRecyclerViewAdapter = new MainRecyclerViewAdapter(this, list);
        mainRecyclerViewAdapter.setOnItemClickListener(new MainRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                Log.d(TAG, "onItemClick: position " + position);
                if (position == 0) {
                    startActivity(new Intent(MainActivity.this, GestureDetectorActivity.class));
                }
            }
        });
        recyclerView.setAdapter(mainRecyclerViewAdapter);


    }
}
