package com.shake.classloader.hookamspms;

import com.shake.activity.hook.IActivityManagerHandler;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * Created by shake on 17-3-29.
 */
public class AMSHookHelper {

    public static final String EXTRA_TARGET_INTENT = "extra_target_intent";

    /**
     * 这是app进程即将进入系统进程去校验Activity信息的过程
     * 将要启动的TargerActivity，替换成在AndroidManifest中注册过的StubActivity
     */
    public static void hookActivityManagerNative(){

        try {

            /**
             * 第一步，获取原始对象，即ActivityManagerService的代理
             */
            Class<?> amnClass = Class.forName("android.app.ActivityManagerNative");

            Field gDefaultField = amnClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            Object gDefaultObject = gDefaultField.get(null);


            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            //获取gDefault对象的mInstance属性的值，即AMS对象
            Object realIActivityManager = mInstanceField.get(gDefaultObject);


            /**
             * 第二步，根据原始对象，生成一个代理对象
             */
            Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
            //这里的ClassLoader可能会错误
            Object proxy = Proxy.newProxyInstance(iActivityManagerInterface.getClassLoader(),
                    new Class<?>[]{iActivityManagerInterface},new IActivityManagerHandler(realIActivityManager));


            /**
             * 第三步，将原始对象替换成代理对象
             */
            mInstanceField.set(gDefaultObject,proxy);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }




}
