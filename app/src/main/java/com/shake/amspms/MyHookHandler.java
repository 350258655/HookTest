package com.shake.amspms;

import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Arrays;

/**
 * 一个简单的用来演示的动态代理对象 (PMS和AMS都使用这一个类)
 * 只是打印日志和参数; 以后可以修改参数等达到更加高级的功能
 */
public class MyHookHandler implements InvocationHandler{

    //原本对象的引用
    private Object mBase;

    public MyHookHandler(Object base){
        this.mBase = base;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Log.i("TAG", "你被hook了");
        Log.i("TAG", "调用了什么方法："+method.getName()+"，传入的参数是："+ Arrays.toString(args));

        return method.invoke(mBase,args);
    }
}
