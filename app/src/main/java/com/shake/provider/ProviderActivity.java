package com.shake.provider;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.example.mac.hooktest.R;

public class ProviderActivity extends AppCompatActivity {



    // demo ContentProvider 的URI
    //private static Uri URI = Uri.parse("content://com.example.mac.plugintest.TestProvider");
    private static Uri URI = Uri.parse("content://com.shake.provider.StubContentProvider");

    static int count = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_provider);
        Button btnquery = (Button) findViewById(R.id.btn_query);
        Button btninsert = (Button) findViewById(R.id.btn_insert);


        btninsert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ContentValues values = new ContentValues();
                values.put("name", "name" + count++);
                getContentResolver().insert(URI, values);
            }
        });


        btnquery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = getContentResolver().query(URI,
                        null, null, null, null);
                assert cursor != null;
                while (cursor.moveToNext()) {
                    int count = cursor.getColumnCount();
                    StringBuilder sb = new StringBuilder("column: ");
                    for (int i = 0; i < count; i++) {
                        sb.append(cursor.getString(i) + ", ");
                    }

                    //Toast.makeText(ProviderActivity.this, "查询到插件的内容："+sb.toString(), Toast.LENGTH_SHORT).show();
                    Log.i("TAG", sb.toString());
                }
            }
        });


    }


    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(newBase);

//        /**
//         * 第一步，先拷贝插件APK到内存中
//         */
//        LoadApkUtils.copyFile(newBase);
//
//        /**
//         * 第三步，Hook ClassLoader 让插件中的类能够被加载
//         */
//        File pluginApk = getFileStreamPath("plugin.apk");
//        File pluginOdex = getFileStreamPath("plugin.odex");
//        ClassLoaderHook.patchClassLoader(getClassLoader(), pluginApk, pluginOdex);
//
//
//        /**
//         * 第三步，把插件的 ContentProviders 安装到宿主程序中
//         */
//        ProviderHelper.installProviders(newBase, getFileStreamPath("plugin.apk"));

    }
}
