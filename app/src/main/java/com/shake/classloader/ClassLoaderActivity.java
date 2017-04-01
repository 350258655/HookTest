package com.shake.classloader;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.mac.hooktest.R;
import com.shake.classloader.baoshou.BaseDexClassLoaderHookHelper;
import com.shake.classloader.hookamspms.AMSHookHelper;
import com.shake.classloader.hookamspms.ActivityThreadHookHelper;
import com.shake.classloader.jijin.LoadedApkClassLoaderHookHelper;

import java.io.File;


public class ClassLoaderActivity extends AppCompatActivity {


    private static final int PATCH_BASE_CLASS_LOADER = 1;

    private static final int CUSTOM_CLASS_LOADER = 2;

    private static final int HOOK_METHOD = PATCH_BASE_CLASS_LOADER;


    /**
     * 插件包名
     */
    private String PACKAGE_NAME = "com.example.mac.plugintest";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_class_loader);

        Button btnbaoshou = (Button) findViewById(R.id.btn_baoshou);
        Button btnjijin = (Button) findViewById(R.id.btn_jijin);

        final Intent intent = new Intent();

        btnbaoshou.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ComponentName（组件名称）是用来打开其他应用程序中的Activity或服务的
                //第一个参数是包名，第二个参数是全类名
                ComponentName componentName = new ComponentName(PACKAGE_NAME,PACKAGE_NAME+".BaoshouActivity");
                intent.setComponent(componentName);
                startActivity(intent);
            }
        });


        btnjijin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //ComponentName（组件名称）是用来打开其他应用程序中的Activity或服务的
                //第一个参数是包名，第二个参数是全类名
                ComponentName componentName = new ComponentName(PACKAGE_NAME,PACKAGE_NAME+".JijinActivity");
                intent.setComponent(componentName);
                startActivity(intent);
            }
        });

    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);

        //先拷贝插件到内存中
        LoadApkUtils.copyFile(newBase);

        try {

            if(HOOK_METHOD == CUSTOM_CLASS_LOADER){
                LoadedApkClassLoaderHookHelper.hook(getFileStreamPath("plugin.apk"));
            }else{
                //这是。。。类似返回一个文件之类的吧
                File apkFile = getFileStreamPath("plugin.apk");
                File dexFile = getFileStreamPath("plugin.dex");
                BaseDexClassLoaderHookHelper.patchClassLoader(getClassLoader(),apkFile,dexFile);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        AMSHookHelper.hookActivityManagerNative();
        ActivityThreadHookHelper.hookActivityThreadHandler();

    }
}
