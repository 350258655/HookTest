package com.shake.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

public class ProxyService extends Service {


    @Override
    public void onCreate() {
        super.onCreate();
        Log.i("TAG", "执行了代理服务的onCreate!");
    }

    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.i("TAG", "执行了代理服务的onStart() 第一个参数intent = [" + intent + "],第二个参数startId = [" + startId + "]");
        MyServiceManager.getsInstance().onStart(intent,startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("TAG", "执行了代理服务的onDestroy!");
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
