package com.shake.receiver;

import com.shake.classloader.LoadApkUtils;
import com.shake.classloader.MyApplication;

import java.io.File;

import dalvik.system.DexClassLoader;

/**
 * Created by shake on 17-4-6.
 */
public class ReceiverClassLoader extends DexClassLoader {

    public ReceiverClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }


    public static ReceiverClassLoader getPluginClassLoader(File plugin){
        return new ReceiverClassLoader(plugin.getPath(),
                LoadApkUtils.getPluginOptDexDir().getPath(),
                LoadApkUtils.getPluginOptDexDir().getPath(),
                MyApplication.getContext().getClassLoader());
    }
}
