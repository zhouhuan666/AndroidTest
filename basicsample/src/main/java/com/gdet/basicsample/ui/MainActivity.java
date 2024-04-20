package com.gdet.basicsample.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gdet.basicsample.R;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-12-23
 * 描述：
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (savedInstanceState == null) {
            ProductListFragment fragment = new ProductListFragment();
            getSupportFragmentManager().beginTransaction().add(R.id.fragment_container, fragment, ProductListFragment.TAG).commit();
        }


    }


}
