package com.gdet.testapp.fragment;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.blankj.utilcode.util.FragmentUtils;
import com.gdet.testapp.R;

public class FragmentTestActivity extends AppCompatActivity {

    private static final String TAG = "FragmentTestActivity";
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_fragment_test);
        FragmentA fragmentA=new FragmentA();
        FragmentB fragmentB=new FragmentB();
        FragmentC fragmentC=new FragmentC();
        Button toA = findViewById(R.id.toA);
        toA.setOnClickListener(v -> {
            Log.i(TAG, "onCreate: toA");
            if(FragmentUtils.findFragment(getSupportFragmentManager(), FragmentA.class)!=null){
                FragmentUtils.showHide(fragmentA,fragmentB,fragmentC);
            }else {
                FragmentUtils.add(getSupportFragmentManager(), fragmentA, R.id.container);
            }

        });
        Button toB = findViewById(R.id.toB);
        toB.setOnClickListener(v -> {
            Log.i(TAG, "onCreate: toA"+FragmentUtils.findFragment(getSupportFragmentManager(), FragmentB.class));
            if(FragmentUtils.findFragment(getSupportFragmentManager(), FragmentB.class)!=null){
                FragmentUtils.showHide(fragmentB,fragmentA,fragmentC);
            }else {
                FragmentUtils.add(getSupportFragmentManager(), fragmentB, R.id.container);
            }
        });
        Button toC = findViewById(R.id.toC);
        toC.setOnClickListener(v -> {
            Log.i(TAG, "onCreate: toA"+FragmentUtils.findFragment(getSupportFragmentManager(), FragmentC.class));
            if(FragmentUtils.findFragment(getSupportFragmentManager(), FragmentC.class)!=null){
                FragmentUtils.showHide(fragmentC,fragmentB,fragmentA);
            }else {
                FragmentUtils.add(getSupportFragmentManager(), fragmentC, R.id.container);
            }
        });
    }
}
