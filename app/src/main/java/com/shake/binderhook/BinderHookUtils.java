package com.shake.binderhook;

import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Map;

/**
 * Created by shake on 17-3-23.
 */
public class BinderHookUtils {


    public static void hookClipboardService() throws Exception {

        final String CLIPBOARD_SERVICE = "clipboard";

        /**
         * 第一、通过反射获取委托对象IBinder
         * 下面这一段的意思实际就是: IBinder readBinder = (IBinder)ServiceManager.getService("clipboard");
         * 只不过 ServiceManager这个类是@hide的
         */
        //1、获取ServiceManager
        Class<?> serverManager = Class.forName("android.os.ServiceManager");
        //2、反射获取方法对象，getService
        Method getServiceMethod = serverManager.getDeclaredMethod("getService",String.class);
        //3、通过反射方法，获取一个IBinder对象,这个IBinder就是委托对象
        IBinder realBinder = (IBinder) getServiceMethod.invoke("null",CLIPBOARD_SERVICE);

        Log.i("TAG", "反射获取到了IBinder");

        /**
         * 第二、通过JDK动态代理，构造一个代理的IBinder。这里是Hook getService的过程
         */
        IBinder proxyBinder = (IBinder) Proxy.newProxyInstance(serverManager.getClassLoader(),
                new Class<?>[]{IBinder.class},
                new ProxyIBinder(realBinder));


        /**
         * 第三、把hook的这个代理IBinder对象放进ServiceManager的cache里面
         * 以后查询的时候，会优先查询缓存里面的Binder，这样就会使用被我们修改过的Binder了
         */
        //获取ServiceManager的成员变量sCache，即一个HashMap集合
        Field cacheField = serverManager.getDeclaredField("sCache");
        cacheField.setAccessible(true);
        //获取存储IBinder的MAP
        Map<String,IBinder> cache = (Map<String, IBinder>) cacheField.get(null);
        //把代理的IBinder存进集合中
        cache.put(CLIPBOARD_SERVICE,proxyBinder);
    }


}
