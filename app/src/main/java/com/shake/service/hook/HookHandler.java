package com.shake.service.hook;

import android.content.ComponentName;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.util.Pair;

import com.shake.classloader.MyApplication;
import com.shake.service.MyServiceManager;
import com.shake.service.ProxyService;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * Created by shake on 17-4-7.
 */
public class HookHandler implements InvocationHandler {


    Object mRealObject;

    public HookHandler(Object object) {
        this.mRealObject = object;
    }


    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {

        if ("startService".equals(method.getName())) {
            // 只拦截这个方法
            // API 23:
            // public ComponentName startService(IApplicationThread caller, Intent service,
            //        String resolvedType, int userId) throws RemoteException

            /**
             * 第一步，找出原本的 targetService 的 intent
             */
            Pair<Integer, Intent> targetIntent = findFirstIntentOfArgs(args);


            /**
             *第二步，构造新的intent，使其指向 ProxyService
             */
            Intent stubIntent = new Intent();
            //获取当前应用的包名
            String stubPackage = MyApplication.getContext().getPackageName();
            //这里我们把启动的Service替换为ProxyService, 让ProxyService接收生命周期回调
            ComponentName componentName = new ComponentName(stubPackage, ProxyService.class.getName());
            stubIntent.setComponent(componentName);
            //把我们真正要启动的TargetService先存起来
            Log.i("TAG", "targetIntent的第一个参数是什么: " + targetIntent.first + ",第二个又是：" + targetIntent.second);
            stubIntent.putExtra(AMSHook.EXTRA_TARGET_INTENT, targetIntent.second);

            /**
             * 第三步，替换掉Intent，达到欺骗AMS的目的
             */
            args[targetIntent.first] = stubIntent;

            Log.i("TAG", "成功hook服务...");

        }

        //     public int stopService(IApplicationThread caller, Intent service,
        // String resolvedType, int userId) throws RemoteException
        if ("stopService".equals(method.getName())) {
            //获取到停止服务的那个intent
            Intent intent = findFirstIntentOfArgs(args).second;
            //判断这个intent是插件的 intent 才去hook
            if (!TextUtils.equals(MyApplication.getContext().getPackageName(), intent.getComponent().getPackageName())) {
                return MyServiceManager.getsInstance().stopService(intent);
            }

        }

        return method.invoke(mRealObject, args);
    }

    /**
     * 找到原本要启动目标服务的 intent 出来
     *
     * @param args
     * @return
     */
    private Pair<Integer, Intent> findFirstIntentOfArgs(Object[] args) {
        int index = 0;

        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof Intent) {
                index = i;
                break;
            }
        }
        return Pair.create(index, (Intent) args[index]);
    }
}
