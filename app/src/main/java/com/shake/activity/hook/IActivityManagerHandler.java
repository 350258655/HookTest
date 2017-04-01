package com.shake.activity.hook;

import android.content.ComponentName;
import android.content.Intent;
import android.util.Log;

import com.shake.activity.StubActivity;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by shake on 17-3-29.
 */
public class IActivityManagerHandler implements InvocationHandler {

    Object mBase;

    public IActivityManagerHandler(Object realIActivityManager) {
        mBase = realIActivityManager;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        //只是拦截startActivity这个方法
        if ("startActivity".equals(method.getName())) {

            /**
             * 第一步，从参数中找出原本属于 TargetActivity的Intent
             */
            Intent targetIntent;
            int index = 0;
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof Intent) {
                    index = i;
                    break;
                }
            }
            //取出属于TargetActivity的Intent
            targetIntent = (Intent) args[index];


            /**
             * 第二步，构造一个属于StubActivity的Intent
             */
            Intent stubIntent = new Intent();

            String stubPackage = "com.example.mac.hooktest";
            //构造一个ComponentName
            ComponentName componentName = new ComponentName(stubPackage, StubActivity.class.getName());
            //设置给StubActivity的intent
            stubIntent.setComponent(componentName);


            /**
             * 第三步，将TargetActivity的intent作为参数，赋给StubActivity的intent。方便在启动流程返回到App进程的
             * 时候，进行替换
             */
            stubIntent.putExtra(AMSHookHelper.EXTRA_TARGET_INTENT, targetIntent);


            /**
             * 第四步，将StubActivity的intent，设置给参数，欺骗AMS。这个时候假如不做后续的操作，也不会报错
             * 只不过启动的Activity会变成StubActivity
             */
            args[index] = stubIntent;
            Log.i("TAG", "成功hook");

            return method.invoke(mBase, args);
        }

        return method.invoke(mBase, args);
    }
}
