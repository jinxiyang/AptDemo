package com.yang.apt.lib.bindview;

import android.app.Activity;

import java.lang.reflect.Method;

public class ButterKnife {

    public static void bind(Activity activity){
        String clazzName = activity.getClass().getName() + "_ViewBinding";
        try {
            Class<?> clazz = Class.forName(clazzName);
            Method method = clazz.getMethod("bind", activity.getClass());
            method.invoke(clazz.newInstance(), activity);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
