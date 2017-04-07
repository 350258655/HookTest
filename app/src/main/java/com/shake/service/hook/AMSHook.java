package com.shake.service.hook;

import java.lang.reflect.Field;
import java.lang.reflect.Proxy;

/**
 * Created by shake on 17-4-7.
 */
public class AMSHook {

    public static final String EXTRA_TARGET_INTENT = "extra_target_intent";

    public static void hookAMS(){

        try {

            /**
             * 第一步，获取原始对象，即ActivityManagerService的代理
             */
            //(1) 获取gDefault属性的值
            Class<?> activityManagerNativeClass = Class.forName("android.app.ActivityManagerNative");
            Field gDefaultField = activityManagerNativeClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            Object gDefaultObj = gDefaultField.get(null);

            //(2) 获取 gDefault 对象中的属性 mInstance 的值，也就是 AMS的代理
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            //获取到真正的AMS代理对象
            Object realAMSObject = mInstanceField.get(gDefaultObj);


            /**
             * 第二步，根据原始对象，生成一个代理对象
             */
            Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");
            Object proxyAMSObject = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class<?>[]{iActivityManagerInterface},
                    new HookHandler(realAMSObject));


            /**
             * 第三步，把原始对象替换成代理对象
             */
            mInstanceField.set(gDefaultObj,proxyAMSObject);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }

}
