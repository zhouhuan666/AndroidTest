package com.gdet.testapp.annotation;

import android.app.Activity;

/**
 * @author JNCHOU
 * 版本：1.0
 * 创建日期：2023-11-19
 * 描述：
 */
public class HuanButterKnife {

    public static void bind(Activity activity) {
        String name = activity.getClass().getName() + "_ViewBinding";
        try {
            Class<?> aClass = Class.forName(name);
            IBinder iBinder = (IBinder) aClass.newInstance();
            iBinder.bind(activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
