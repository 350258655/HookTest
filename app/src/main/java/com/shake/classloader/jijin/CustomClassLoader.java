package com.shake.classloader.jijin;

import dalvik.system.DexClassLoader;

/**
 * 自定义的ClassLoader, 用于加载"插件"的资源和代码
 * Created by shake on 17-4-1.
 */
public class CustomClassLoader extends DexClassLoader {

    public CustomClassLoader(String dexPath, String optimizedDirectory, String librarySearchPath, ClassLoader parent) {
        super(dexPath, optimizedDirectory, librarySearchPath, parent);
    }
}
