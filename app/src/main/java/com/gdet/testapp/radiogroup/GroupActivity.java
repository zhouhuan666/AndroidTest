package com.gdet.testapp.radiogroup;

import android.content.res.Configuration;
import android.media.metrics.LogSessionId;
import android.os.Bundle;
import android.util.Log;
import android.widget.RadioGroup;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import com.gdet.testapp.R;

public class GroupActivity extends AppCompatActivity {

    private static final String TAG = "GroupActivity";

    private int lastCheckedId = -1; // 保存上次选中的RadioButton ID
    private boolean isRestoringState = false; // 标志，用于判断是否正在恢复状态

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_group);

        if (savedInstanceState != null) {
            Log.i(TAG, "onCreate: savedInstanceState != null");
            isRestoringState = true; // 设置标志，表示正在恢复状态
            lastCheckedId = savedInstanceState.getInt("lastCheckedId", -1); // 恢复保存的ID
            if (lastCheckedId != -1) {
//                radioGroup.check(lastCheckedId); // 恢复选中状态
            }
        }
        RadioGroup radioGroup = findViewById(R.id.radiogroup);
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                Log.i(TAG, "onCheckedChanged: " + checkedId+" isRestoringState  "+isRestoringState);
                if (!isRestoringState) { // 如果不是在恢复状态过程中
                    lastCheckedId = checkedId; // 更新保存的ID
                }
            }
        });


    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Log.i(TAG, "onSaveInstanceState: ");
        outState.putInt("lastCheckedId", lastCheckedId); // 保存当前选中的RadioButton ID
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart: ");
//        isRestoringState = false; // 完成状态恢复后，清除标志
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume: ");
        isRestoringState = false;
    }
}
