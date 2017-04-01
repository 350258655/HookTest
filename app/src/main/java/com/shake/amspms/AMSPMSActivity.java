package com.shake.amspms;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;

import com.example.mac.hooktest.R;

public class AMSPMSActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amspms);
        Button btnpms = (Button) findViewById(R.id.btn_pms);
        Button btnams = (Button) findViewById(R.id.btn_ams);

        btnams.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
              startActivity(new Intent(AMSPMSActivity.this,AMSPMSActivity.class));
            }
        });


        btnpms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //测试Hook
                getPackageManager().getInstalledApplications(0);
            }
        });




    }

    //这个方法比onCreate早，在这里hook比较合适
    @Override
    protected void attachBaseContext(Context newBase) {

        HookHelper.hookActivityManager();
        HookHelper.hookPackageManager(newBase);
        super.attachBaseContext(newBase);
    }
}
