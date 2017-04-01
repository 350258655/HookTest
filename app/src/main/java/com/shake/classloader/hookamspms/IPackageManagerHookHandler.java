package com.shake.classloader.hookamspms;

import android.content.pm.PackageInfo;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by shake on 17-4-1.
 *
 */
public class IPackageManagerHookHandler implements InvocationHandler {
    private Object mBase;

    public IPackageManagerHookHandler(Object base) {
        mBase = base;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        /**
         * 当执行到 getPackageInfo 方法的时候，直接拦截，返回返回一个 PackageInfo 对象，不让这个方法
         * 内部的逻辑被执行到
         */
        if (method.getName().equals("getPackageInfo")) {
            return new PackageInfo();
        }
        return method.invoke(mBase, args);
    }
}
