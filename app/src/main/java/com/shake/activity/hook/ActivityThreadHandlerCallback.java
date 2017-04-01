package com.shake.activity.hook;

import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import java.lang.reflect.Field;

/**
 * Created by shake on 17-3-29.
 */
public class ActivityThreadHandlerCallback implements Handler.Callback {

    //Handler对象
    Handler mHandler;

    public ActivityThreadHandlerCallback(Handler handler){
        this.mHandler = handler;
    }


    @Override
    public boolean handleMessage(Message msg) {

        switch (msg.what){
            // ActivityThread里面 "LAUNCH_ACTIVITY" 这个字段的值是100
            // 本来使用反射的方式获取最好, 这里为了简便直接使用硬编码
            case 100:
                handleLaunchActivity(msg);
                break;
        }

        //执行Handler对象的handleMessage方法，也就是原本正常流程会去执行的，其实我们就是在中间插入了一个
        //handleLaunchActivity(msg)这个过程，而正是这个过程把StubActivity替换成真正的要启动的TargetActivity

        //在这里可以选择直接返回false，那么就会去执行下面的handlerMessage(msg)方法
        //或者手动调用handlerMessage(msg)，之后再返回true
        //return false;
        mHandler.handleMessage(msg);
        return true;
    }


    private void handleLaunchActivity(Message msg) {

        /**
         * 原本正常流程会执行下面的代码的
         */
        //case LAUNCH_ACTIVITY: {
        //    Trace.traceBegin(Trace.TRACE_TAG_ACTIVITY_MANAGER, "activityStart");
        //    final ActivityClientRecord r = (ActivityClientRecord) msg.obj;

        //    r.packageInfo = getPackageInfoNoCheck(
        //            r.activityInfo.applicationInfo, r.compatInfo);
        //    handleLaunchActivity(r, null);
        //    Trace.traceEnd(Trace.TRACE_TAG_ACTIVITY_MANAGER);
        // }


        /**
         * 根据源码所示，要启动的Activity的信息，都在msg.obj中，我们从msg.obj中取出StubActivity的Intent,
         * 再替换回TargetActivity的Intent
         */
        Object object = msg.obj;

        try {

            /**
             *第一步，取出之前存的StubActivity的intent
             */
            Field intentField = object.getClass().getDeclaredField("intent");
            intentField.setAccessible(true);
            Intent stubIntent = (Intent) intentField.get(object);

            /**
             * 第二步，取出之前存在StubActivity的Intent中的TargetActivity中的intent
             */
            Intent targetIntent = stubIntent.getParcelableExtra(AMSHookHelper.EXTRA_TARGET_INTENT);

            /**
             * 第三步，替换intent
             */
            stubIntent.setComponent(targetIntent.getComponent());

        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
