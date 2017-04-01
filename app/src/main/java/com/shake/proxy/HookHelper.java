package com.shake.proxy;

import android.app.Instrumentation;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Created by shake on 17-3-20.
 */
public class HookHelper {

    public static void attachContext() throws Exception {

        //先获取当前的ActivityThread对象，说明此时的ActivityThread对象是已经实例化好的了
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        //反射方法currentActivityThread，获取ActivityThread对象
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        //currentActivityThread是一个static函数所以可以直接invoke，不需要带实例参数
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        //拿到原始的mInstrumentation字段
        Field mInstrumentationField = activityThreadClass.getDeclaredField("mInstrumentation");
        mInstrumentationField.setAccessible(true);

        //从字段中获取Instrumentation对象
        Instrumentation instrumentation = (Instrumentation) mInstrumentationField.get(currentActivityThread);

        //创建代理对象
        Instrumentation myInstrumentation = new MyInstrumenttation(instrumentation);

        //偷梁换柱,第一个参数表示是哪个类的变量，第二个参数是变量的新值
        mInstrumentationField.set(currentActivityThread,myInstrumentation);
    }


}
