package com.shake.binderhook;

import android.annotation.TargetApi;
import android.content.ClipData;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by shake on 17-3-23.
 * 这个是代理的剪切板服务
 */
public class ProxyQueryLocalInterface implements InvocationHandler {

    //原始的Service对象
    Object base;

    public ProxyQueryLocalInterface(IBinder iBinder, Class<?> stubClass) {


        try {
            //获取asInterface方法实例
            Method asInterfaceMethod = stubClass.getDeclaredMethod("asInterface", IBinder.class);

            //TODO 这里的操作，就是去hook asInterface方法
            //asInterface是用于返回一个Service实例的。这里调用asInterface就是为了把
            //我们这个有过滤 queryLocalInterface 方法功能的IBinder对象(比如A)传给IBinder方法
            //使得asInterface中去调用obj.queryLocalInterface方法的时候，实际上是调用了
            //A.queryLocalInterface
            this.base = asInterfaceMethod.invoke(null, base);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        // 把剪切版的内容替换为 "you are hooked"
        if ("getPrimaryClip".equals(method.getName())) {
            Log.i("TAG", "hook了 getPrimaryClip方法");
            return ClipData.newPlainText(null, "你被hook了");
        }

        // 欺骗系统,使之认为剪切版上一直有内容
        if ("hasPrimaryClip".equals(method.getName())) {
            return true;
        }

        return method.invoke(base, args);
    }
}
