package com.shake.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

/**
 * Created by shake on 17-3-29.
 *  要注意的是,这个Activity并没有再Manifest中注册!!!
 */
public class TargetActivity extends android.app.Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.i("TAG", "执行了TargetActivity 的 onCreate方法！！！");
        TextView tv = new TextView(this);
        tv.setText("TargetActivity启动成功！！！");
        setContentView(tv);

    }


    @Override
    protected void onPause() {
        super.onPause();
        Log.i("TAG", "执行了TargetActivity 的 onPause方法！！！");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i("TAG", "执行了TargetActivity 的 onResume方法！！！");
    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.i("TAG", "执行了TargetActivity 的 onStop方法！！！");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.i("TAG", "执行了TargetActivity 的 onDestroy方法！！！");
    }
}
