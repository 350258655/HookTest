package com.shake.service;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.os.Binder;
import android.os.IBinder;
import android.util.Log;

import com.shake.classloader.MyApplication;
import com.shake.service.hook.AMSHook;

import java.io.File;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shake on 17-4-7.
 */
public class MyServiceManager {

    private static MyServiceManager sInstance;


    /**
     * 存储插件的Service信息
     */
    private Map<ComponentName, ServiceInfo> mServiceInfoMap = new HashMap<>();

    /**
     * 存储插件的Service对象
     */
    private Map<String, Service> mServiceMap = new HashMap<String, Service>();


    /**
     * 单例
     *
     * @return
     */
    public synchronized static MyServiceManager getsInstance() {
        if (sInstance == null) {
            sInstance = new MyServiceManager();
        }
        return sInstance;
    }


    /**
     * 启动某个插件Service; 如果Service还没有启动, 那么会创建新的插件Service
     *
     * @param proxyIntent
     * @param startId
     */
    public void onStart(Intent proxyIntent, int startId) {

        /**
         * 第一步，从缓存Map获取插件APK的服务组件信息
         */
        Intent targetIntent = proxyIntent.getParcelableExtra(AMSHook.EXTRA_TARGET_INTENT);
        //从缓存Map中获取插件APK中Service组件的信息
        ServiceInfo serviceInfo = selectPluginService(targetIntent);

        if (serviceInfo == null) {
            Log.e("TAG", "没有缓存到Service:" + serviceInfo.name);
        }

        /**
         * 第二步，假如Service还不存在，就创建
         */
        if (!mServiceMap.containsKey(serviceInfo.name)) {
            //创建的同时，其实
            proxyCreateService(serviceInfo);
        }

        /**
         * 第三步，从服务Map中取出服务，调用 TargetService的onStart()方法
         */
        Service service = mServiceMap.get(serviceInfo.name);
        service.onStart(targetIntent, startId);

    }


    /**
     * 停止某个插件Service, 当全部的插件Service都停止之后, ProxyService也会停止
     *
     * @param targetIntent
     * @return
     */
    public int stopService(Intent targetIntent) {

        // 1,获取ServiceInfo对象
        ServiceInfo serviceInfo = selectPluginService(targetIntent);

        // 2,根据 ServiceInfo 获取Service对象
        Service service = mServiceMap.get(serviceInfo.name);

        // 3,调用服务的 onDestroy方法
        service.onDestroy();

        // 4,停止ProxyService
        mServiceMap.remove(serviceInfo.name);
        if(mServiceMap.isEmpty()){
            //没有Service了，这个没有必要存在了
            Context appContext = MyApplication.getContext();
            appContext.stopService(new Intent().setComponent(new ComponentName(appContext.getPackageName(),ProxyService.class.getName())));
        }

        return 1;
    }


    /**
     * 将ServiceInfo作为参数，通过ActivityThread的handleCreateService方法创建出Service对象
     *
     * @param serviceInfo
     */
    private void proxyCreateService(ServiceInfo serviceInfo) {

        try {

            /**
             * 第一步，获取ActivityThread对象
             */
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object activityThreadObj = currentActivityThreadMethod.invoke(null);


            /**
             * 第二步，封装 handleCreateService 所需要的参数，即 CreateServiceData 对象
             */
            //1,创建CreateServiceData对象, 用来传递给ActivityThread的handleCreateService 当作参数
            Class<?> createServiceDataClass = Class.forName("android.app.ActivityThread$CreateServiceData");
            Constructor<?> constructor = createServiceDataClass.getDeclaredConstructor();
            constructor.setAccessible(true);
            //获取到 CreateServiceData 对象
            Object createServiceDataObj = constructor.newInstance();

            //TODO 2，封装 "IBinder token" 字段 , 这里的token也是疑问
            IBinder token = new Binder();
            Field tokenField = createServiceDataClass.getDeclaredField("token");
            tokenField.setAccessible(true);
            tokenField.set(createServiceDataObj, token);

            //3，封装 "ServiceInfo info" 字段
            // TODO 这里是疑问
            // 这个修改是为了loadClass的时候, LoadedApk会是主程序的ClassLoader, 我们选择Hook BaseDexClassLoader的方式加载插件
            serviceInfo.applicationInfo.packageName = MyApplication.getContext().getPackageName();
            Field infoField = createServiceDataClass.getDeclaredField("info");
            infoField.setAccessible(true);
            infoField.set(createServiceDataObj, serviceInfo);

            //4，封装 "CompatibilityInfo compatInfo" 字段
            Class<?> compatInfoClass = Class.forName("android.content.res.CompatibilityInfo");
            Field defaultCompatibilityField = compatInfoClass.getDeclaredField("DEFAULT_COMPATIBILITY_INFO");
            //获取到 CompatibilityInfo 的值
            Object defaultConpatibility = defaultCompatibilityField.get(null);
            //获取 CreateServiceData 对象中的 compatInfo 字段，待会填充这个
            Field compatInfoField = createServiceDataClass.getDeclaredField("compatInfo");
            compatInfoField.setAccessible(true);
            compatInfoField.set(createServiceDataObj, defaultConpatibility);


            /**
             * 第三步，构造 handleCreateService 方法，并且调用
             */
            Method handleCreateServiceMethod = activityThreadClass.getDeclaredMethod("handleCreateService", createServiceDataClass);
            handleCreateServiceMethod.setAccessible(true);
            handleCreateServiceMethod.invoke(activityThreadObj, createServiceDataObj);


            /**
             * 第四步，从ActivityThread的mServices字段里面取出 Service
             * handleCreateService创建出来的Service对象并没有返回, 而是存储在ActivityThread的mServices字段里面, 这里我们手动把它取出来
             */
            Field mServiceField = activityThreadClass.getDeclaredField("mServices");
            mServiceField.setAccessible(true);
            Map mServices = (Map) mServiceField.get(activityThreadObj);
            //取出服务,TODO 这个token
            Service service = (Service) mServices.get(token);

            //TODO 为什么要删除
            mServices.remove(token);

            //将此Service存储起来
            mServiceMap.put(serviceInfo.name, service);

        } catch (Exception e) {
            e.printStackTrace();
        }


    }

    /**
     * 选择匹配的ServiceInfo
     *
     * @param targetIntent
     * @return
     */
    private ServiceInfo selectPluginService(Intent targetIntent) {
        for (ComponentName componentName : mServiceInfoMap.keySet()) {
            if (componentName.equals(targetIntent.getComponent())) {
                ServiceInfo serviceInfo = mServiceInfoMap.get(componentName);
                return serviceInfo;
            }
        }

        return null;
    }


    /**
     * 解析Apk文件中的 <service>, 并存储起来
     * 主要是调用PackageParser类的generateServiceInfo方法
     *
     * @param pluginApk
     */
    public void preLoadService(File pluginApk) {

        try {

            /**
             * 第一步，获取PackageParser对象
             */
            Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
            Object packageParserObj = packageParserClass.newInstance();


            /**
             * 第二步，根据PackageParser对象去调用它的 "parsePackage" 方法，返回一个Package对象
             */
            Method parserPackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class, int.class);
            Object packageObj = parserPackageMethod.invoke(packageParserObj, pluginApk, PackageManager.GET_RECEIVERS);


            /**
             * 第三步，读取Package对象里面的services字段。然后接下来要做的就是
             *  根据这个List<Service>，(这里的Service并不是我们的服务对象，而是
             *  PackageParser的内部类。只是一个bean类)获取到Service对应的ServiceInfo
             */
            Field servicesField = packageObj.getClass().getDeclaredField("services");
            List services = (List) servicesField.get(packageObj);


            /**
             * 第四步，构造 generateServiceInfo 方法，和它的几个参数
             *  generateServiceInfo(Service s, int flags, PackageUserState state, int userId)
             *
             */
            Class<?> packageParser$ServiceClass = Class.forName("android.content.pm.PackageParser$Service");
            Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
            Class<?> userHandlerClass = Class.forName("android.os.UserHandle");
            //获取第三个参数PackageUserState的值
            Object packageUserStateObj = packageUserStateClass.newInstance();
            //获取第四个参数userId的值
            Method getCallingUserIdMethod = userHandlerClass.getDeclaredMethod("getCallingUserId");
            int userId = (int) getCallingUserIdMethod.invoke(null);
            //构造 generateServiceInfo 方法
            Method generateServiceInfoMethod = packageParserClass.getDeclaredMethod("generateServiceInfo",
                    packageParser$ServiceClass, int.class, packageUserStateClass, int.class);


            /**
             * 第五步，根据services，解析出插件APK中每个Service组件的 ServiceInfo，并存起来
             */
            for (Object service : services) {
                //调用 generateServiceInfo 方法，返回一个ServiceInfo对象
                ServiceInfo info = (ServiceInfo) generateServiceInfoMethod.invoke(packageParserObj,
                        service, 0, packageUserStateObj, userId);
                //存到Map中
                mServiceInfoMap.put(new ComponentName(info.packageName, info.name), info);
            }


        } catch (Exception e) {
            e.printStackTrace();
        }


    }
}
