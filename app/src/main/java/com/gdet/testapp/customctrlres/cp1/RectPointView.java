package com.gdet.testapp.customctrlres.cp1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-12-23
 * 描述：
 */
public class RectPointView extends View {

    private static final String TAG = "RectPointView";

    private Paint mPaint;

    private Rect mRect;

    private int mX, mY;

    public RectPointView(Context context) {
        super(context);
        init();
    }

    public RectPointView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        Log.d(TAG, "RectPointView: ");
        init();
    }


    private void init() {
        mPaint = new Paint();
        mPaint.setStyle(Paint.Style.STROKE);
        mRect = new Rect(100, 10, 300, 100);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        Log.d(TAG, "onMeasure: ");
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        Log.d(TAG, "onDraw: ");
        super.onDraw(canvas);
        if(mRect.contains(mX,mY)){
            mPaint.setColor(Color.RED);
        }else{
            mPaint.setColor(Color.GREEN);
        }
        canvas.drawRect(mRect,mPaint);
    }


    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        Log.d(TAG, "onLayout: ");
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        mX = (int) event.getX();
        mY = (int) event.getY();
        if(event.getAction() == MotionEvent.ACTION_DOWN){
            invalidate();
            return true;
        }else if (event.getAction()==MotionEvent.ACTION_UP){
            mX=-1;
            mY=-1;
        }
        postInvalidate();
        return super.onTouchEvent(event);
    }
}
