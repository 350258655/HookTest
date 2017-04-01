package com.shake.classloader.jijin;

import android.content.pm.ApplicationInfo;
import android.util.Log;

import com.shake.classloader.LoadApkUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by shake on 17-4-1.
 */
public class LoadedApkClassLoaderHookHelper {

    public static Map<String, Object> sLoadedApk = new HashMap<String, Object>();


    public static void hook(File apkFile) throws Exception {

        /**
         * 第一、先获取到当前的ActivtyThread对象
         */
        Class<?> activityThreadClass = Class.forName("android.app.ActivityThread");
        Method currentActivityThreadMethod = activityThreadClass.getDeclaredMethod("currentActivityThread");
        currentActivityThreadMethod.setAccessible(true);
        Object activityThreadObject = currentActivityThreadMethod.invoke(null);


        /**
         * 第二、获取到 mPackage 这个静态成员变量
         */
        Field mPackageField = activityThreadClass.getDeclaredField("mPackages");
        mPackageField.setAccessible(true);
        Map mPackageMap = (Map) mPackageField.get(activityThreadObject);


        /**
         *第三、分别构造CompatibilityInfo类型的参数和ApplicationInfo类型的参数，为了能够反射调用
         * getPackageInfoNoCheck方法返回LoadedApk对象
         */
        //先获取CompatibilityInfo对象
        Class<?> compatibilityInfoClass = Class.forName("android.content.res.CompatibilityInfo");
        Field defaultCompatibilityInfoField = compatibilityInfoClass.getDeclaredField("DEFAULT_COMPATIBILITY_INFO");
        defaultCompatibilityInfoField.setAccessible(true);
        Object compatibilityInfoObject = defaultCompatibilityInfoField.get(null);

        //获取ApplicationInfo对象。这里ApplicationInfo对象中的信息，已经是我们插件APK的信息了
        ApplicationInfo applicationInfoObject = generateApplicationInfo(apkFile);

        //获取getPackageInfoNoCheck方法
        Method getPackageInfoNoCheckMethod = activityThreadClass.getDeclaredMethod("getPackageInfoNoCheck", ApplicationInfo.class, compatibilityInfoClass);
        //调用getPackageInfoNoCheck返回LoaderApk对象
        Object loadedApkObject = getPackageInfoNoCheckMethod.invoke(activityThreadObject, applicationInfoObject, compatibilityInfoObject);


        /**
         * 第四、构造一个新的ClassLoader对象，这算是我们伪造的ClassLoader
         * 这个odexPath的地址是 ： data/user/0/com.example.mac.hooktest/files/plugin/odex
         * 只要它存在应用的根目录下就好
         */
        String odexPath = LoadApkUtils.getPluginOptDexDir().getPath();

        Log.i("TAG", "这个odex是什么: "+odexPath);
        //新创建一个ClassLoader
        ClassLoader classLoader = new CustomClassLoader(apkFile.getPath(),odexPath,null,ClassLoader.getSystemClassLoader());


        /**
         * 第五、从获取的LoaderApk中获取的ClassLoader字段
         */
        Field classLoaderField = loadedApkObject.getClass().getDeclaredField("mClassLoader");
        classLoaderField.setAccessible(true);


        /**
         * 第六、将伪造的ClassLoader的值写入ClassLoader字段中
         */
        classLoaderField.set(loadedApkObject, classLoader);


        /**
         * 第七、将LoaderApk对象存到mPackage这个Map中
         */
        // 由于是弱引用, 因此我们必须在某个地方存一份, 不然容易被GC; 那么就前功尽弃了.
        sLoadedApk.put(applicationInfoObject.packageName, loadedApkObject);
        // 创建一个弱引用
        WeakReference w = new WeakReference(loadedApkObject);
        //存到Map中
        mPackageMap.put(applicationInfoObject.packageName,w);

    }

    /**
     * 生成一个Applicaiton对象:
     * 这个方法的最终目的是调用 -->
     * android.content.pm.PackageParser#generateApplicationInfo(android.content.pm.PackageParser.Package,
     *   int, android.content.pm.PackageUserState)
     *
     * @param apkFile
     * @return
     */
    private static ApplicationInfo generateApplicationInfo(File apkFile) throws Exception {

        // 首先拿到我们得终极目标: generateApplicationInfo方法
        // API 23 !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
        // public static ApplicationInfo generateApplicationInfo(Package p, int flags,
        //    PackageUserState state) {
        // 其他Android版本不保证也是如此.

        /**
         * 第一步，获取反射核心类，android.content.pm.PackageParser
         */
        Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
        //利用无参构造器new出一个PackageParser对象
        Object packageParserObject = packageParserClass.newInstance();


        /**
         * 第二步、构造generateApplicationInfo方法的第一个参数：Package对象
         */
        Class<?> packageClass = Class.forName("android.content.pm.PackageParser$Package");
        //通过parserpackage方法，获取Package对象
        Method parserPackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class, int.class);
        //TODO 获取到Package对象，这个对象传入的参数，实际上我们替换成了插件APK的信息
        Object packageObject = parserPackageMethod.invoke(packageParserObject,apkFile,0);

        /**
         * 第三步、构造generateApplicationInfo方法的第一个参数：PackageUserState对象
         */
        Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
        // 第三个参数 mDefaultPackageUserState 我们直接使用默认构造函数构造一个出来即可
        Object packageUserStateObject = packageUserStateClass.newInstance();


        /**
         * 第四步、构造generateApplicationInfo方法并且调用
         */
        Method generateApplicationInfoMethod = packageParserClass.getDeclaredMethod("generateApplicationInfo",
                packageClass,int.class,packageUserStateClass);
        ApplicationInfo applicationInfo = (ApplicationInfo) generateApplicationInfoMethod.invoke(packageParserObject,
                packageObject,0,packageUserStateObject);

        /**
         * 第五步、修改ApplicationInfo中的信息
         */
        String apkPath = apkFile.getPath();
        applicationInfo.sourceDir = apkPath;
        applicationInfo.publicSourceDir = apkPath;

        return applicationInfo;
    }


}
