package com.gdet.testapp.customctrlres.cp1;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-12-20
 * 描述：
 */
public class BaseView extends View {

    private static final String TAG = "BaseView";

    public BaseView(Context context) {
        super(context);
    }

    public BaseView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
    }

    public BaseView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(@NonNull Canvas canvas) {
        super.onDraw(canvas);

//        Paint paint=new Paint();
//        paint.setColor(0xFFFF0000);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setStrokeWidth(50);
//        paint.setAntiAlias(true);
//
//        canvas.drawCircle(190,200,150,paint);
//
//        paint.setColor(0x7EFFFF00);
//        canvas.drawCircle(190,200,100,paint);
////        canvas.drawRGB(255,0,255);
//
//        canvas.drawLine(100,100,200,200,paint);


//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        paint.setStrokeWidth(5);
//
//        float[] pts = {10, 10, 100, 100, 200, 200, 400, 400};
//        canvas.drawLines(pts,2,4,paint);

//        Paint paint=new Paint();
//        paint.setColor(Color.RED);
//        paint.setStrokeWidth(25);
//        float []pts={10, 10, 100, 100, 200, 200, 400, 400};
//        canvas.drawPoints(pts,2,4,paint);

        //绘制矩形
//        Paint paint=new Paint();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(15);
//        canvas.drawRect(10,10,100,100,paint);
//        paint.setStyle(Paint.Style.FILL);
//        RectF rectF=new RectF(210f,10f,300f,100f);
//        canvas.drawRect(rectF,paint);

        //圆角矩形
//        Paint paint=new Paint();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setStrokeWidth(15);
//        RectF rectF=new RectF(100,10,300,100);
//        canvas.drawRoundRect(rectF,20,20,paint);

        //圆形
//        Paint paint=new Paint();
//        paint.setAntiAlias(true);
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setStrokeWidth(15);
//        canvas.drawCircle(100,100,40,paint);

        //椭圆
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(5);
//        RectF rectF=new RectF(100,10,300,100);
//        canvas.drawRect(rectF,paint);
//        paint.setColor(Color.GREEN);
//        canvas.drawOval(rectF,paint);
        //弧
//        Paint paint=new Paint();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.FILL);
//        paint.setStrokeWidth(5);
//        RectF rectF=new RectF(10,10,100,100);
//        canvas.drawArc(rectF,0,90,false,paint);


        //判断矩形相交情况
//        Paint paint=new Paint();
//        paint.setStyle(Paint.Style.STROKE);
//        Rect rect1=new Rect(10,10,200,200);
//        Rect rect2=new Rect(190,10,250,200);
//        Rect rect3=new Rect(10,210,200,300);
//
//        paint.setColor(Color.RED);
//        canvas.drawRect(rect1,paint);
//        paint.setColor(Color.GREEN);
//        canvas.drawRect(rect2,paint);
//        paint.setColor(Color.YELLOW);
//        canvas.drawRect(rect3,paint);
//
//        boolean intersect1_2=Rect.intersects(rect1,rect2);
//        boolean intersect1_3=Rect.intersects(rect1,rect3);
//
//        Log.d(TAG, "onDraw: "+intersect1_2+"   "+intersect1_3);

        //合并两个矩形
//        Paint paint=new Paint();
//        paint.setStyle(Paint.Style.STROKE);
//        Rect rect1=new Rect(10,10,20,20);
//        Rect rect2=new Rect(100,100,110,110);
//        paint.setColor(Color.RED);
//        canvas.drawRect(rect1,paint);
//        paint.setColor(Color.GREEN);
//        canvas.drawRect(rect2,paint);
//        paint.setColor(Color.BLACK);
//        rect1.union(rect2);
//        canvas.drawRect(rect1,paint);


        //合并矩形与某个点
//        Rect rect1=new Rect(10,10,20,20);
//        rect1.union(100,100);
//        printResult(rect1);
//        rect1=new Rect();
//        rect1.union(100,100);
//        printResult(rect1);

        //提取颜色分量
//        int green = Color.green(0xFF000F00);
//        Log.d(TAG, "onDraw: " + green);

        //直线路径
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(5);
//        Path path=new Path();
//        path.moveTo(10,10);
//        path.lineTo(10,100);
//        path.lineTo(300,100);
//        path.close();
//
//        canvas.drawPath(path,paint);


        //弧线路径
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(5);
//        Path path=new Path();
//        path.moveTo(10,10);
//        RectF rectF=new RectF(100,10,200,100);
//        path.arcTo(rectF,0,90,true);
//        canvas.drawPath(path,paint);


        //添加路径
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(5);
//        Path path=new Path();
//        path.moveTo(10,10);
//        path.lineTo(100,50);
//        RectF rectF=new RectF(100,100,150,150);
//        path.addArc(rectF,0,90);
//        canvas.drawPath(path,paint);

        //添加矩形路径
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(5);
//        Path CCWRectPath=new Path();
//        RectF rectF1=new RectF(50,50,240,200);
//        CCWRectPath.addRect(rectF1, Path.Direction.CCW);
//        Path CWRectPath=new Path();
//        RectF rectF2=new RectF(290,50,480,200);
//        CWRectPath.addRect(rectF2, Path.Direction.CW);
//        String text="苦心人天不负";
//        canvas.drawPath(CCWRectPath,paint);
//        canvas.drawPath(CWRectPath,paint);
//
//        paint.setTextSize(35);
//        canvas.drawTextOnPath(text,CCWRectPath,0,18,paint);
//        canvas.drawTextOnPath(text,CWRectPath,0,18,paint);


        //添加圆角矩形路径
//        Paint paint = new Paint();
//        paint.setColor(Color.RED);
//        paint.setStyle(Paint.Style.STROKE);
//        paint.setStrokeWidth(5);
//        Path path = new Path();
//        RectF rectF1 = new RectF(50, 50, 240, 200);
//        path.addRoundRect(rectF1, 10, 15, Path.Direction.CCW);
//
//        RectF rectF2 = new RectF(290, 50, 480, 200);
//        float radi[] = {10, 15, 20, 25, 30, 35, 40, 45};
//        path.addRoundRect(rectF2,radi, Path.Direction.CCW);
//        canvas.drawPath(path,paint);


        //填充模式
        Paint paint=new Paint();
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.FILL);
        Path path=new Path();
        path.addRect(100,100,300,300, Path.Direction.CW);
        path.addCircle(300,300,100, Path.Direction.CW);
        path.setFillType(Path.FillType.INVERSE_EVEN_ODD);
        canvas.drawPath(path,paint);





    }

    private void printResult(Rect rect) {
        Log.d(TAG, "printResult: " + rect.toShortString());
    }
}
