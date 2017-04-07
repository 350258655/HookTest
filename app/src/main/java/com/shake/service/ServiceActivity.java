package com.shake.service;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.mac.hooktest.R;
import com.shake.classloader.LoadApkUtils;
import com.shake.service.hook.AMSHook;
import com.shake.service.hook.ClassLoaderHook;

import java.io.File;

public class ServiceActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_service);
        Button btnstopsecond = (Button) findViewById(R.id.btn_stop_second);
        Button btnstartsecond = (Button) findViewById(R.id.btn_start_second);
        Button btnstopfirst = (Button) findViewById(R.id.btn_stop_first);
        Button btnstartfirst = (Button) findViewById(R.id.btn_start_first);

        btnstartfirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent().setComponent(
                        new ComponentName("com.example.mac.plugintest","com.example.mac.plugintest.FirstService")));
            }
        });


        btnstopfirst.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent().setComponent(
                        new ComponentName("com.example.mac.plugintest", "com.example.mac.plugintest.FirstService")));
            }
        });


        btnstartsecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startService(new Intent().setComponent(
                        new ComponentName("com.example.mac.plugintest", "com.example.mac.plugintest.SecondService")));
            }
        });


        btnstopsecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                stopService(new Intent().setComponent(
                        new ComponentName("com.example.mac.plugintest", "com.example.mac.plugintest.SecondService")));
            }
        });



    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);


        /**
         * 第一步，先拷贝插件APK到内存中
         */
        LoadApkUtils.copyFile(newBase);

        /**
         * 第一步，拦截startService, stopService等操作
         */
        // 拦截startService, stopService等操作
        AMSHook.hookAMS();

        /**
         * 第三步，Hook ClassLoader 让插件中的类能够被加载
         */
        File pluginApk = getFileStreamPath("plugin.apk");
        File pluginOdex = getFileStreamPath("plugin.odex");
        ClassLoaderHook.patchClassLoader(getClassLoader(),pluginApk,pluginOdex);

        /**
         * 第四步，解析插件中的Service组件，并且存储起来
         */
        MyServiceManager.getsInstance().preLoadService(pluginApk);

    }
}
