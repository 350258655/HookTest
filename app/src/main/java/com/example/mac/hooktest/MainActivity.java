package com.example.mac.hooktest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.shake.activity.HookActivity;
import com.shake.amspms.AMSPMSActivity;
import com.shake.binderhook.BinderActivity;
import com.shake.classloader.ClassLoaderActivity;
import com.shake.provider.ProviderActivity;
import com.shake.proxy.ProxyActivity;
import com.shake.receiver.ReceiverActivity;
import com.shake.service.ServiceActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        Button btnproxy = (Button) findViewById(R.id.btn_proxy);

        btnproxy.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProxyActivity.class));
            }
        });

        Button btnbinder = (Button) findViewById(R.id.btn_binder);
        btnbinder.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,BinderActivity.class));
            }
        });


        Button btnamspms = (Button) findViewById(R.id.btn_ams_pms);
        btnamspms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this,AMSPMSActivity.class));
            }
        });

        Button btnactivity = (Button) findViewById(R.id.btn_activity);
        btnactivity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, HookActivity.class));
            }
        });

        Button btnclassloader = (Button) findViewById(R.id.btn_classloader);
        btnclassloader.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ClassLoaderActivity.class));
            }
        });

        Button btnreceiver = (Button) findViewById(R.id.btn_receiver);
        btnreceiver.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ReceiverActivity.class));
            }
        });


        Button btnservice = (Button) findViewById(R.id.btn_service);
        btnservice.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ServiceActivity.class));
            }
        });


        Button btnprovider = (Button) findViewById(R.id.btn_provider);
        btnprovider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(MainActivity.this, ProviderActivity.class));
            }
        });



    }



}
