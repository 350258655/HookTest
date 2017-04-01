package com.shake.classloader;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * Created by shake on 17-4-1.
 */
public class LoadApkUtils {


    /**
     * 插件apk的名称
     */
    private static String apkName = "plugin.apk";


    /**
     * 拷贝apk文件至内存中
     *
     * @param context
     */
    public static void copyFile(Context context) {

        //获取根目录文件
        File apk = context.getFileStreamPath(apkName);

        try {
            if (apk.exists()) {
                apk.delete();
                Toast.makeText(context, "删除旧插件", Toast.LENGTH_SHORT).show();
            }
            //构造一个文件输出流
            FileOutputStream fos = new FileOutputStream(apk);
            //构造一个输入流
            InputStream is = context.getAssets().open(apkName);
            //构造一个缓冲输入流
            BufferedInputStream bis = new BufferedInputStream(is);

            int len = -1;
            byte[] by = new byte[1024];

            while ((len = bis.read(by)) != -1) {
                fos.write(by, 0, len);
                fos.flush();
            }

            fos.close();
            bis.close();
            is.close();

            // 路径是 ： /data/user/0/com.example.mac.hooktest/files/plugin.apk
            Log.i("TAG", "APK拷贝到的路径是： " + apk);
            Toast.makeText(MyApplication.getContext(), "拷贝APK成功！", Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
        }

    }


    /**
     *最后需要返回的目录：/data/user/0/宿主包名/files/plugin/odex
     *
     * @return
     */
    public static File getPluginOptDexDir(){

        //在内存目录中，创建一个plugin的文件夹
        File baseDir = MyApplication.getContext().getFileStreamPath("plugin");
        if(!baseDir.exists()){
            baseDir.mkdirs();
        }

        Log.i("TAG", "baseDir的初始路径是: "+baseDir.getPath());

        //在baseDir目录中创建一个odex文件
        baseDir = new File(baseDir,"odex");

        if(!baseDir.exists()){
            baseDir.mkdirs();
            return baseDir;
        }

        Log.i("TAG", "baseDir的最终路径是: "+baseDir.getPath());

        return baseDir;
    }






}
