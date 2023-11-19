package com.gdet.testapp;

import androidx.annotation.DrawableRes;
import androidx.annotation.IntDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-11-18
 * 描述：
 */
public class Test {

    public static void setDrawable(@DrawableRes int id) {

    }

    public static void main(String[] args) {
        setDrawable(1212);
        setCurrentDay(WeekDay.Monday);
    }

    private static WeekDay mCurrentDay;

    //每一个成员就是一个week对象
    enum WeekDay {
        Sunday, Monday, Tuesday, Wednesday, Thursday, Friday
    }

    public static void setCurrentDay(@WekDay WeekDay currentDay) {
        mCurrentDay = currentDay;
    }


    private static final int SUNDAY = 0;
    private static final int MONDAY = 1;

    @IntDef({0,1})
    @Target({ElementType.PARAMETER,ElementType.FIELD})
    @Retention(RetentionPolicy.SOURCE)
    @interface WekDay {

    }

}
