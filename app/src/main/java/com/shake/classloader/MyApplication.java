package com.shake.classloader;

import android.app.Application;
import android.content.Context;

import com.shake.provider.ProviderHelper;
import com.shake.service.hook.ClassLoaderHook;

import java.io.File;

/**
 * Created by shake on 17-4-1.
 * 这个类只是为了方便获取全局Context的.
 */
public class MyApplication extends Application {

    private static Context sContext;

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        sContext = base;


        /**
         * 第一步，先拷贝插件APK到内存中
         */
        LoadApkUtils.copyFile(base);

        /**
         * 第三步，Hook ClassLoader 让插件中的类能够被加载
         */
        File pluginApk = getFileStreamPath("plugin.apk");
        File pluginOdex = getFileStreamPath("plugin.odex");
        ClassLoaderHook.patchClassLoader(getClassLoader(), pluginApk, pluginOdex);


        /**
         * 第三步，把插件的 ContentProviders 安装到宿主程序中
         */
        ProviderHelper.installProviders(base, getFileStreamPath("plugin.apk"));

    }

    public static Context getContext(){
        return sContext;
    }
}
