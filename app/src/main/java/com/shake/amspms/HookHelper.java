package com.shake.amspms;

import android.content.Context;
import android.content.pm.PackageManager;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by shake on 17-3-29.
 */
public class HookHelper {

    public static void hookActivityManager(){

        try {

            /**
             * 第一步，想办法获取原始对象
             */
            Class<?> amnClass = Class.forName("android.app.ActivityManagerNative");

            // 获取ActivityManagerNative中gDefault这个字段
            Field gDefaultField = amnClass.getDeclaredField("gDefault");
            gDefaultField.setAccessible(true);
            // 获取gDefault这个字段的值，正常来说，就是一个Singleton<IActivityManager> 类型的对象
            Object gDefault = gDefaultField.get(null);

            // 4.x以上的gDefault是一个 android.util.Singleton对象; 我们取出这个单例里面的字段
            Class<?> singletonClass = Class.forName("android.util.Singleton");
            // 获取单例中的字段
            Field mInstanceField = singletonClass.getDeclaredField("mInstance");
            mInstanceField.setAccessible(true);
            // 获取到gDefault对象中的,mInstance字段的值，也就是执行create()方法之后，返回的那个IActivityManager类型的对象
            // field.get(null)表示获取一个静态属性的值，而field.get(obj)则表示获取obj类中field这个属性的值
            Object realIActivityManager = mInstanceField.get(gDefault);


            /**
             * 第二步，根据原始对象，生成代理对象
             */
            // 创建一个这个对象的代理对象, 然后替换这个字段, 让我们的代理对象帮忙干活
            Class<?> iActivityManagerInterface = Class.forName("android.app.IActivityManager");

            //获取代理对象
            Object proxy = Proxy.newProxyInstance(Thread.currentThread().getContextClassLoader(),
                    new Class[]{iActivityManagerInterface},new MyHookHandler(realIActivityManager));


            /**
             * 第三步，替换原始对象为代理对象
             */
            //因为当外面调用gDefault.get()方法的时候。实际上返回的是mInstance这个字段的值
            //即IActivityManager类型的对象，这个时候我们把对象给替换掉，就可以了
            mInstanceField.set(gDefault,proxy);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }



    public static void hookPackageManager(Context context){


        try {

            /**
             * 第一步，想办法获取原始对象
             */
            //获取到ActivityThread这个类的class
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            //获取到这个class中的currentActivityThread方法
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            //通过反射这个方法，获取到ActivityThread这个类的对象
            Object activityThreadObject = currentActivityThreadMethod.invoke(null);

            // 去获取ActivityThread里面的原始对象，sPackageManager
            Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
            sPackageManagerField.setAccessible(true);
            Object sPackageManagerObject = sPackageManagerField.get(activityThreadObject);


            /**
             * 第二步，根据原始对象，去生成动态代理对象,这个对象应该得是一个接口类型的对象才能用JDK动态代理
             */
            Class<?> ipackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
            Object proxy = Proxy.newProxyInstance(ipackageManagerInterface.getClassLoader(),
                    new Class[]{ipackageManagerInterface},new MyHookHandler(sPackageManagerObject));


            /**
             * 第三步，替换原始对象为代理对象
             */
            //1、替换掉ActivityThread里面的 sPackageManager 字段
            sPackageManagerField.set(activityThreadObject,proxy);

            //2、替换 ApplicationPackageManager里面的 mPm对象
            PackageManager pm = context.getPackageManager();
            Field mPmField = pm.getClass().getDeclaredField("mPM");
            mPmField.setAccessible(true);
            mPmField.set(pm,proxy);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }



}
