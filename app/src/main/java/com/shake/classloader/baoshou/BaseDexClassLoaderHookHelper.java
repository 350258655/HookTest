package com.shake.classloader.baoshou;

import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;

import dalvik.system.DexClassLoader;
import dalvik.system.DexFile;

/**
 * Created by shake on 17-4-1.
 */
public class BaseDexClassLoaderHookHelper {

    public static void patchClassLoader(ClassLoader classLoader,File apkFile,File optDexFile) throws Exception {

        /**
         * 第一步，获取BaseDexClassLoader类中的pathList字段，即DexPathList对象
         */
        Field pathListField = DexClassLoader.class.getSuperclass().getDeclaredField("pathList");
        pathListField.setAccessible(true);
        Object dexPathListObject = pathListField.get(classLoader);


        /**
         * 第二步，获取DexPathList类中的 dexElements 字段，它是一个Element[] 数组
         */
        Field dexElementsField = dexPathListObject.getClass().getDeclaredField("dexElements");
        dexElementsField.setAccessible(true);
        Object[] elementsArray = (Object[]) dexElementsField.get(dexPathListObject);


        /**
         * 第三步，构造一个新的Element类型的对象，这个对象要存我们插件的信息
         */
        //Element类型
        Class<?> elementClass = elementsArray.getClass().getComponentType();
        //获取Element的构造器
        Constructor<?> constructor = elementClass.getConstructor(File.class,boolean.class,File.class, DexFile.class);
        //创建一个新的Element对象
        Object o = constructor.newInstance(apkFile,false,apkFile,DexFile.loadDex(apkFile.getCanonicalPath(),optDexFile.getAbsolutePath(),0));
        //根据这个Element对象，创建一个只有元素的数组
        Object[] pluginElements = new Object[]{o};


        /**
         * 第四步，创建一个新的Element[] 数组。这个数组是要在原始数组的基础上长度加1
         */
        Object[] newElements = (Object[]) Array.newInstance(elementClass,elementsArray.length + 1);


        /**
         * 第四步，把原始数组，和插件数组的内容复制到这个新的数组中
         */
        //把原始的Element[]数组复制进去
        System.arraycopy(elementsArray,0,newElements,0,elementsArray.length);
        //把插件的Element[]数组复制进去
        System.arraycopy(pluginElements,0,newElements,elementsArray.length,pluginElements.length);


        /**
         * 第五步，将dexElements字段的值 替换为新的数组
         */
        dexElementsField.set(dexPathListObject,newElements);


    }


}
