package com.shake.binderhook;

import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * Created by shake on 17-3-23.
 * 代理的Binder
 * 当我们在外面，要获取一个服务的时候，比如下面：
 *
 * ActivityManager am = (ActivityManager)context.getSystemService(Context.ACTIVITY_SERVICE);
 *
 * 在他的源码中，它是通过ServiceManager去获取一个IBinder对象，然后再通过asInterface方法，对这个IBinder进行了处理
 * (即原本是在这个asInterface的处理方法是：先去查询本地有没有IBinder对象，有就返回，没有就返回远程的IBinder对象)
 *
 */
public class ProxyIBinder implements InvocationHandler {

    //这个是相当于远程的IBinder
    private IBinder proxyBinder;

    Class<?> stub;

    Class<?> iinterface;

    public ProxyIBinder(IBinder iBinder){
        this.proxyBinder = iBinder;

        try {
            this.stub = Class.forName("android.content.IClipboard$Stub");
            this.iinterface = Class.forName("android.content.IClipboard");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }


    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        Log.i("TAG", "正要去hook代理IBinder ");

        //TODO 这里的操作，就是去hook IBinder的queryLocalInterface方法
        if("queryLocalInterface".equals(method.getName())){
            // 这里直接返回真正被Hook掉的Service接口
            // 这里的 queryLocalInterface 就不是原本的意思了
            // 我们肯定不会真的返回一个本地接口, 因为我们接管了 asInterface方法的作用
            // 因此必须是一个完整的 asInterface 过的 IInterface对象, 既要处理本地对象,也要处理代理对象
            // 这只是一个Hook点而已, 它原始的含义已经被我们重定义了; 因为我们会永远确保这个方法不返回null
            // 让 IClipboard.Stub.asInterface 永远走到if语句的else分支里面

            Log.i("TAG", "正要去hook代理IBinder 。。。 ");
            ClassLoader loader = proxy.getClass().getClassLoader();
            ProxyQueryLocalInterface hander = new ProxyQueryLocalInterface( proxyBinder,stub);

            //获取经过处理的IBinder
            Object resultBinder = Proxy.newProxyInstance(loader,new Class[]{this.iinterface},hander);

            return resultBinder;
        }



        return method.invoke(proxyBinder,args);
    }
}
