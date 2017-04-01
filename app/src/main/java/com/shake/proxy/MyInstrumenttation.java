package com.shake.proxy;

import android.app.Activity;
import android.app.Instrumentation;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.lang.reflect.Method;

/**
 * Created by shake on 17-3-20.
 */
public class MyInstrumenttation extends Instrumentation {

    // ActivityThread中原始的对象，保存起来
    Instrumentation mBase;

    public MyInstrumenttation(Instrumentation instrumentation) {
        this.mBase = instrumentation;
    }


    public ActivityResult execStartActivity(
            Context who, IBinder contextThread, IBinder token, Activity target,
            Intent intent, int requestCode, Bundle options) {

        //Hook之前，先打印Log
        Log.i("TAG", "执行了startActivity，参数如下 \n: " + "who = [" + who + "]," +
                "\ncontextThread = [" + contextThread + "], \ntoken = [" + token + "], " +
                "\ntarget = [" + target + "], \nintent = [" + intent +
                "], \nrequestCode = [" + requestCode + "], \noptions = [" + options + "]");

        //开始调用原始的方法，由于这个方法是隐藏的，所以需要使用反射调用
        try {
            Method execStartActivityMethod = Instrumentation.class.getDeclaredMethod("execStartActivity",
                    Context.class,IBinder.class,IBinder.class,Activity.class,
                    Intent.class,int.class,Bundle.class);

            execStartActivityMethod.setAccessible(true);
            return (ActivityResult)execStartActivityMethod.invoke(mBase,who,
                    contextThread,token,target,intent,requestCode,options);
        } catch (Exception e) {
            throw new RuntimeException("不支持,需要手动适配");
        }

    }

}
