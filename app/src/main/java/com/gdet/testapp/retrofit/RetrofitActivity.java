package com.gdet.testapp.retrofit;

import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gdet.testapp.R;

import java.io.IOException;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2024-04-20
 * 描述：
 */
public class RetrofitActivity extends AppCompatActivity implements View.OnClickListener {
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_retrofit);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        Button button = findViewById(R.id.simple_service);
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.simple_service) {
            SimpleService simpleService = new SimpleService();
            try {
                simpleService.execute();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
