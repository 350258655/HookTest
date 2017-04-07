package com.shake.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.example.mac.hooktest.R;
import com.shake.classloader.LoadApkUtils;

import java.io.File;

public class ReceiverActivity extends AppCompatActivity {

    /**
     * 发送给插件的ACTION
     */
    static final String SEND_ACTION = "com.example.mac.plugintest.HOST_ACTION";

    /**
     * 接收来自插件广播的ACTION
     */
    static final String RECEIVER_ACTION = "com.example.mac.plugintest.PLUGIN_ACTION";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_receiver);


        File pluginApk = getFileStreamPath("plugin.apk");
        // 去Hook广播
        ReceiverHelper.preLoadReceiver(this,pluginApk);

        Button btnreceiver = (Button) findViewById(R.id.btn_receiver);
        btnreceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast.makeText(ReceiverActivity.this, "宿主的吐司，发送广播给插件！", Toast.LENGTH_SHORT).show();
                //发送广播给插件
                sendBroadcast(new Intent(SEND_ACTION));
            }
        });


        //注册为广播接收者
        registerReceiver(mReceiver,new IntentFilter(RECEIVER_ACTION));

    }


    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Toast.makeText(ReceiverActivity.this, "宿主的吐司，接收到插件的广播", Toast.LENGTH_SHORT).show();
        }
    };




    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);
        //先拷贝插件到内存中
        LoadApkUtils.copyFile(newBase);
    }
}
