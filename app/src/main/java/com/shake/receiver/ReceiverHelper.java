package com.shake.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.util.Log;

import java.io.File;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by shake on 17-4-6.
 */
public class ReceiverHelper {

    /**
     * 存ActivityInfo(key),和IntentFilter集合(value) 的集合。从静态注册广播中获取到的信息，
     * 存到这个集合中
     */
    public static Map<ActivityInfo, List<? extends IntentFilter>> sCache =
            new HashMap<ActivityInfo, List<? extends IntentFilter>>();


    public static void preLoadReceiver(Context context, File apk) {

        /**
         * 第一步、解析插件APK文件中的<receiver/>标签，并存储起来
         */
        parserReceiver(apk);

        /**
         * 第二步，把解析出来的每一个静态注册，都改为动态注册
         */
        ClassLoader cl = null;
        for (ActivityInfo info : sCache.keySet()) {
            Log.i("TAG", "总共有什么 receiver : " + info.name);
            List<? extends IntentFilter> intentFilters = sCache.get(info);
            if (cl == null) {
                //创建ClassLoader
                cl = ReceiverClassLoader.getPluginClassLoader(apk);
            }

            try {
                //把解析出来的每一个静态Receiver都注册为动态的
                for (IntentFilter filter : intentFilters) {
                    //创建一个广播接收者,这个BroadcastReceiver，其实就是根据插件中的 PluginReceiver 生成的BroadcastReceiver
                    BroadcastReceiver receiver = (BroadcastReceiver) cl.loadClass(info.name).newInstance();

                    //com.example.mac.plugintest.PluginReceiver
                    Log.i("TAG", "这个info的名字是什么: "+info.name);
                    //注册为广播接收者
                    context.registerReceiver(receiver,filter);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }


        }


    }


    /**
     * 解析插件APK文件中的<receiver/>标签，并存储起来
     *
     * @param apk
     */
    private static void parserReceiver(File apk) {
        try {

            /**
             * 第一步，获取PackageParser对象
             */
            Class<?> packageParserClass = Class.forName("android.content.pm.PackageParser");
            Object packageParserObj = packageParserClass.newInstance();


            /**
             * 第二步，根据PackageParser对象去调用它的 "parsePackage" 方法，返回一个Package对象
             */
            Method parserPackageMethod = packageParserClass.getDeclaredMethod("parsePackage", File.class, int.class);
            Object packageObj = parserPackageMethod.invoke(packageParserObj, apk, PackageManager.GET_RECEIVERS);


            /**
             * 第三步，获取Package对象里面的 receivers 字段
             * 注意这是一个 List<Activity> (没错,底层把<receiver>当作<activity>处理)
             * 接下来要做的就是根据这个List<Activity> 获取到Receiver对应的 ActivityInfo (依然是把receiver信息用activity处理了)
             */
            Field receiversField = packageObj.getClass().getDeclaredField("receivers");
            List receivers = (List) receiversField.get(packageObj);


            /**
             * 第四步，构造 generateActivityInfo 方法，和它的几个参数
             *   generateActivityInfo(Activity a, int flags,PackageUserState state, int userId)
             *
             */
            Class<?> packageParser$ActivityClass = Class.forName("android.content.pm.PackageParser$Activity");
            Class<?> packageUserStateClass = Class.forName("android.content.pm.PackageUserState");
            Class<?> userHandlerClass = Class.forName("android.os.UserHandle");
            //获取第三个参数PackageUserState的值
            Object packageUserStateObj = packageUserStateClass.newInstance();
            //获取第四个参数userId的值
            Method getCallingUserIdMethod = userHandlerClass.getDeclaredMethod("getCallingUserId");
            int userId = (int) getCallingUserIdMethod.invoke(null);
            //构造 generateActivityInfo 方法
            Method generateActivityInfoMethod = packageParserClass.getDeclaredMethod("generateActivityInfo",
                    packageParser$ActivityClass, int.class, packageUserStateClass, int.class);


            /**
             * 第五步，获取 PackageParser$Component中的字段 intents ，因为 IntentFilter的集合 就存在这个字段里面
             */
            Class<?> componentClass = Class.forName("android.content.pm.PackageParser$Component");
            Field intentsField = componentClass.getDeclaredField("intents");


            /**
             * 第六步，反射调用 generateActivityInfo 获取 ActivityInfo对象，
             * 获取 IntentFilter的集合(intents是属于Component的属性，但这里Activity是Component的子类，
             *   所以用 Activity 也可以获取到 IntentFilter的集合)
             *
             */
            for (Object receiver : receivers) {
                //获取ActivityInfo对象
                ActivityInfo info = (ActivityInfo) generateActivityInfoMethod.invoke(packageParserObj, receiver, 0, packageUserStateObj, userId);
                //获取IntentFilter的集合
                List<? extends IntentFilter> filters = (List<? extends IntentFilter>) intentsField.get(receiver);
                //存入到集合中
                sCache.put(info, filters);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }


}
