package com.shake.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

public class StubContentProvider extends ContentProvider {


    public static final String AUTHORITY = "com.shake.provider.StubContentProvider";

    @Override
    public boolean onCreate() {
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //noinspection ConstantConditions
       // return getContext().getContentResolver().query(getRealUri(uri), projection, selection, selectionArgs, sortOrder);

        

        return getContext().getContentResolver().query(getRealUri(uri), projection, selection, selectionArgs, sortOrder);
    }

    @Override
    public String getType(Uri uri) {
        return null;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        //noinspection ConstantConditions
        return getContext().getContentResolver().insert(getRealUri(uri), values);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        return 0;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        return 0;
    }

    /**
     * 为了使得插件的ContentProvder提供给外部使用，我们需要一个StubProvider做中转；
     * 如果外部程序需要使用插件系统中插件的ContentProvider，不能直接查询原来的那个uri
     * 我们对uri做一些手脚，使得插件系统能识别这个uri；
     *
     * 这里的处理方式如下：
     *
     * 原始查询插件的URI应该为：
     * content://plugin_auth/path/query
     *
     * 如果需要查询插件，需要修改为：
     *
     * content://stub_auth/plugin_auth/path/query
     *
     * 也就是，我们把插件ContentProvider的信息放在URI的path中保存起来；
     * 然后在StubProvider中做分发。
     *
     * 当然，也可以使用QueryParamerter,比如：
     * content://plugin_auth/path/query/ ->  content://stub_auth/path/query?plugin=plugin_auth
     * @param raw 外部查询我们使用的URI
     * @return 插件真正的URI
     */
    private Uri getRealUri(Uri raw) {

//        Log.i("TAG", "这是什么鬼，raw:" + raw);
//        String rawAuth = raw.getAuthority();
//        Log.i("TAG", "这是什么鬼，rawAuth:" + rawAuth);
//
//        if (!AUTHORITY.equals(rawAuth)) {
//            Log.i("TAG", "这是什么鬼，rawAuth:" + rawAuth);
//
//        }
//
//        String uriString = raw.toString();
//        uriString = uriString.replaceAll(rawAuth + '/', "");
//        Uri newUri = Uri.parse(uriString);
//        Log.i("TAG", "realUri:" + newUri);

        Log.i("TAG", "有没有进入这里 --- ");

        Uri newUri = Uri.parse("content://com.example.mac.plugintest.TestProvider");



        return newUri;
    }

}
