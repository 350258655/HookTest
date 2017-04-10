package com.shake.provider;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shake on 17-4-7.
 */
public class ProviderHelper {


    /**
     * 在进程内部安装provider, 也就是调用
     * ActivityThread.installContentProviders(Context context, List<ProviderInfo> providers)方法
     *
     * @param context
     * @param apkFile
     */
    public static void installProviders(Context context, File apkFile) {
        try {
            /**
             * 第一步，获取ActivityThread 对象
             */
            Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
            Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
            Object currentActivityThread = currentActivityThreadMethod.invoke(null);



            /**
             * 第二步，获取插件的APK中的 每个ContentProvider组件的 ProviderInfo。
             * 这也是 installContentProviders 方法的第二个参数
             */
            List<ProviderInfo> providerInfos = parseProviders(apkFile);
            //TODO 这是把插件中的ContentProvider的包名，修改为是宿主的包名？
            for (ProviderInfo providerInfo : providerInfos) {
                providerInfo.applicationInfo.packageName = context.getPackageName();
            }
            Log.i("TAG", "提供者信息：" + providerInfos.toString());



            /**
             * 第三步，构造并且调用 installContentProviders 方法, 安装到宿主APK中
             */
            Method installProvidersMethod = activityThreadClass.getDeclaredMethod("installContentProviders", Context.class, List.class);
            installProvidersMethod.setAccessible(true);
            installProvidersMethod.invoke(currentActivityThread, context, providerInfos);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 解析插件Apk文件中的 <provider>, 并存储起来
     * 主要是调用PackageParser类的generateProviderInfo方法
     *
     * @param apkFile
     * @return
     */
    private static List<ProviderInfo> parseProviders(File apkFile) {
        try {

            /**
             * 第一步，获取PackageParser对象
             */
            Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
            Object packageParserObj = packageParserClass.newInstance();


            /**
             * 第二步，根据PackageParser对象去调用它的 "parsePackage" 方法，返回一个Package对象
             */
            Method parsePackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class, int.class);
            // 首先调用parsePackage获取到apk对象对应的Package对象
            Object packageObj = parsePackageMethod.invoke(packageParserObj, apkFile, PackageManager.GET_PROVIDERS);


            /**
             * 第三步，读取Package对象里面的providers字段。然后接下来要做的就是
             * 根据这个List<Provider>，(这里的Provider是一个bean类)，获取到ContentProvider对应的ProviderInfo
             */
            // 读取Package对象里面的services字段
            // 接下来要做的就是根据这个List<Provider> 获取到Provider对应的ProviderInfo
            Field providersField = packageObj.getClass().getDeclaredField("providers");
            List providers = (List) providersField.get(packageObj);


            /**
             * 第四步，构造 generateProviderInfo 方法，和它的几个参数
             * generateProviderInfo(Provider p, int flags, PackageUserState state, int userId)
             *
             */
            // 调用generateProviderInfo 方法, 把PackageParser.Provider转换成ProviderInfo
            Class<?> packageParser$ProviderClass = Class.forName("android.content.pm.PackageParser$Provider");
            Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
            Class<?> userHandler = Class.forName("android.os.UserHandle");
            //获取第三个参数PackageUserState的值
            Object packageUserStateObj = packageUserStateClass.newInstance();
            //获取第四个参数userId的值
            Method getCallingUserIdMethod = userHandler.getDeclaredMethod("getCallingUserId");
            int userId = (Integer) getCallingUserIdMethod.invoke(null);
            // 需要调用 android.content.pm.PackageParser#generateProviderInfo
            Method generateProviderInfoMethod = packageParserClass.getDeclaredMethod("generateProviderInfo",
                    packageParser$ProviderClass, int.class, packageUserStateClass, int.class);


            /**
             * 第五步，根据providers，解析出插件APK中每个ContentProvider组件的 ProviderInfo，并存起来
             */
            //创建存储 ProviderInfo 的集合
            List<ProviderInfo> infos = new ArrayList<>();
            // 解析出intent对应的Provider组件
            for (Object service : providers) {
                //调用 generateProviderInfo 方法，返回一个 ProviderInfo 对象
                ProviderInfo info = (ProviderInfo) generateProviderInfoMethod.invoke(packageParserObj, service, 0, packageUserStateObj, userId);
                infos.add(info);
            }

            return infos;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
