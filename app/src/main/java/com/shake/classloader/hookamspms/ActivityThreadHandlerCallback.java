package com.shake.classloader.hookamspms;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.os.Handler;
import android.os.Message;

import com.shake.activity.hook.AMSHookHelper;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

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
         * 这个object的类型，是 ActivityClientRecord 类型的对象
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


            /**
             * 第四步，往ActivityInfo这个类中，插入插件的包名。因为之前，我们只是在 mPackage 这个缓存
             * 中插入一个LoadedApk(插件的信息)，但是当系统调用流程到 getPackageInfo 的时候，即根据包名
             * 去查找缓存的时候，默认还是会使用当前宿主的包名去查询缓存 mPackage 中的信息。所以我们应该把这个
             * ActivityInfo的包名改为插件的包名，这样在执行 查询缓存的时候，才会用插件的包名去查询缓存
             */
            //获取 ActivityClientRecord 类中的 activityInfo属性
            Field activityInfoField = object.getClass().getDeclaredField("activityInfo");
            activityInfoField.setAccessible(true);
            //根据属性获取ActivityInfo对象
            ActivityInfo activityInfo = (ActivityInfo) activityInfoField.get(object);
            //给ActivityInfo附上插件的包名
            if(targetIntent.getPackage() == null){
                activityInfo.applicationInfo.packageName = targetIntent.getComponent().getPackageName();
            }else {
                activityInfo.applicationInfo.packageName = targetIntent.getPackage();
            }

            /**
             * 第五步，欺骗PMS，让PMS以为插件已经装在手机上。
             */
            hookPackageManager();

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    private void hookPackageManager() throws Exception {
        // 这一步是因为 initializeJavaContextClassLoader 这个方法内部无意中检查了这个包是否在系统安装
        // 如果没有安装, 直接抛出异常, 这里需要临时Hook掉 PMS, 绕过这个检查.
        /**
         * 第一步，获取ActivityThread对象
         */
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object currentActivityThread = currentActivityThreadMethod.invoke(null);

        /**
         * 第二步，获取ActivityThread里面原始的 sPackageManager，即IPackageManager对象
         */

        Field sPackageManagerField = activityThreadClass.getDeclaredField("sPackageManager");
        sPackageManagerField.setAccessible(true);
        Object sPackageManager = sPackageManagerField.get(currentActivityThread);

        /**
         * 第三步，根据原始对象，生成代理对象
         */
        Class<?> iPackageManagerInterface = Class.forName("android.content.pm.IPackageManager");
        Object proxy = Proxy.newProxyInstance(iPackageManagerInterface.getClassLoader(),
                new Class<?>[]{iPackageManagerInterface},
                new IPackageManagerHookHandler(sPackageManager));

        /**
         * 第四步，将原始对象替换为代理对象
         */
        // 1. 替换掉ActivityThread里面的 sPackageManager 字段
        sPackageManagerField.set(currentActivityThread, proxy);
    }
}
