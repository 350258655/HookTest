package com.shake.classloader.hookamspms;

import android.os.Handler;

import java.lang.reflect.Field;

/**
 * Created by shake on 17-3-30.
 */
public class ActivityThreadHookHelper {



    /**
     * 这是系统进程返回到App进程之后，即将要去启动Activity。在这里，我们要把原本替换的那个StubActivity
     * 换回原本我们要启动的那个TargetActivity
     */
    public static void hookActivityThreadHandler(){
        // 设置它的回调, 根据源码:
        // 我们自己给他设置一个回调,就会替代之前的回调;
        /**
         * 设置它的回调, 根据源码:
         *  一般正常情况下，是会去执行最下面的 handleMessage(msg);
         *  而我们自己给他设置一个mCallback，就会拦截在执行最下面的操作之前，会先去执行 mCallback.handlerMessage(msg)
         *  而假如 mCallback.handlerMessage(msg) 返回true的话，就不会去执行 最下面的 handleMessage(msg) 了。
         *  所以我们只需要在 mCallback中将StubActivity的intent替换为TargetActivity的intent，再让handlerMessage(msg)操作
         *  继续执行就好，在 mCallback中手动调用 handler.handleMessage(msg) 方法
         */
        //        public void dispatchMessage(Message msg) {
        //            if (msg.callback != null) {
        //                handleCallback(msg);
        //            } else {
        //                if (mCallback != null) {
        //                    if (mCallback.handleMessage(msg)) {
        //                        return;
        //                    }
        //                }
        //                handleMessage(msg);
        //            }
        //        }




        try {

            /**
             * 第一步，获取ActivityThread中的mH对象，即Handler对象
             */
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Field currentActivityThreadField = activityThreadClass.getDeclaredField("sCurrentActivityThread");
            currentActivityThreadField.setAccessible(true);
            Object activityThreadObject = currentActivityThreadField.get(null);

            Field mHField = activityThreadClass.getDeclaredField("mH");
            mHField.setAccessible(true);
            //获取到了Handler对象
            Handler mH = (Handler) mHField.get(activityThreadObject);

            /**
             * 第二步，获取Handler对象中的mCallback字段
             */
            Field mCallbackField = Handler.class.getDeclaredField("mCallback");
            mCallbackField.setAccessible(true);


            /**
             * 第三步，将mCallback字段，替换成我们伪装过的mCallback字段，这里没有用动态代理，只是用了静态代理
             */
            mCallbackField.set(mH,new ActivityThreadHandlerCallback(mH));

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
