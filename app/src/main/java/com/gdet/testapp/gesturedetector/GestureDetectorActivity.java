package com.gdet.testapp.gesturedetector;

import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.gdet.testapp.R;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-10-07
 * 描述：
 */
public class GestureDetectorActivity extends AppCompatActivity {
    private static final String TAG = "GestureDetectorActivity";

    private boolean mLongPressStarted = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_gesturedetector);
        Button back = findViewById(R.id.back);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });


        GestureDetector mGestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(@NonNull MotionEvent e) {
                Log.d(TAG, "onSingleTapConfirmed: ");
                return super.onSingleTapConfirmed(e);
            }

            @Override
            public void onLongPress(@NonNull MotionEvent e) {
                super.onLongPress(e);
                Log.d(TAG, "onLongPress: ");
                mLongPressStarted = true;
            }

            @Override
            public boolean onScroll(@Nullable MotionEvent e1, @NonNull MotionEvent e2, float distanceX, float distanceY) {
                Log.d(TAG, "onScroll: " + e2.getPointerCount());
                return super.onScroll(e1, e2, distanceX, distanceY);
            }

        }
        );

        View view = findViewById(R.id.gestureLayout);
        view.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (mLongPressStarted) {
                        Log.d(TAG, "onTouch: Long press ended");
                        mLongPressStarted = false;
                    }
                }
                mGestureDetector.onTouchEvent(event);
                return true;
            }
        });


    }


}
