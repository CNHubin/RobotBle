package com.hubin.bleclient.utils;

import android.text.TextUtils;
import android.util.Log;

import com.hubin.bleclient.BuildConfig;

import java.util.List;


public class LogUtils {
    /*开关：强制打印模式
    * BuildConfig不准确的时候，可以开启此强制打印模式，
    * true 强制打印日志  false 根据 BuildConfig的模式自动打印或不打印
    * */
    private static boolean isForcedMode = false;


    private static long mTimestamp = 0; //用于记时的变量
    /**
     * 是否打印日志到控制台
     * @return true 打印  false 不打印
     */
    public static boolean isPrint() {
        if (isForcedMode) {//如果是强制模式 必须打印
            return true;
        }
        return BuildConfig.DEBUG; //不是强制模式根据BuildConfig配置文件自动适应
    }


    public static final void v(String msg){
        if(isPrint()) {
            Log.v(getCaller(), msg);
        }
    }

    public static final void d(String msg){
        if(isPrint()) {
            Log.d(getCaller(), msg);
        }
    }


    public static final void i(String msg) {
        if (isPrint()) {
            Log.i(getCaller(), msg);
        }
    }

    public static final void w(String msg) {
        if (isPrint()) {
            Log.w(getCaller(), msg);
        }
    }

    public static final void e(String msg) {
        if(isPrint()) {
            Log.e(getCaller(), msg);
        }
    }


    /**
     * 以级别为 e 的形式输出msg信息,附带时间戳，用于输出一个时间段起始点
     * @param msg 需要输出的msg
     */
    public static void timeStart(String msg) {
        mTimestamp = System.currentTimeMillis();
        if (!TextUtils.isEmpty(msg)) {
            Log.w(getCaller(),"[开始时间：" + mTimestamp + "]" + msg);
        }
    }

    /** 以级别为 e 的形式输出msg信息,附带时间戳，用于输出一个时间段结束点* @param msg 需要输出的msg */
    public static void timeEnd(String msg) {
        long currentTime = System.currentTimeMillis();
        long elapsedTime = currentTime - mTimestamp;
        mTimestamp = currentTime;
        Log.w(getCaller(),"[结束时间："+currentTime+" 耗时：" + elapsedTime + "]" + msg);
    }

    public static <T> void printList(List<T> list) {
        if (list == null || list.size() < 1) {
            return;
        }
        int size = list.size();
        i("---begin---");
        for (int i = 0; i < size; i++) {
            i(i + ":" + list.get(i).toString());
        }
        i("---end---");
    }

    public static <T> void printArray(T[] array) {
        if (array == null || array.length < 1) {
            return;
        }
        int length = array.length;
        i("---begin---");
        for (int i = 0; i < length; i++) {
            i(i + ":" + array[i].toString());
        }
        i("---end---");
    }




    /**
     * 拼接此代码的类名和所在行数
     * @return
     */
    public static String getCaller(){

        StackTraceElement[] trace = Thread.currentThread().getStackTrace();
        int stackIndex= getStackIndex(trace);

        StringBuilder builder = new StringBuilder();
        builder.append(getSimpleClassName(trace[stackIndex].getClassName()))
                .append(".")
                .append(trace[stackIndex].getMethodName())
                .append("(")
                .append(trace[stackIndex].getFileName())
                .append(":")
                .append(trace[stackIndex].getLineNumber())
                .append(")");
        return builder.toString();
    }

    public static int getStackIndex(StackTraceElement[] trace) {

        int methodCount=2;

        for (int i = 0; i < trace.length; i++) {
            StackTraceElement e = trace[i];
            String name = e.getClassName();
            if (name.equals(LogUtils.class.getName())) {
                return i+methodCount;
            }
        }
        return 0;
    }

    public static String getSimpleClassName(String name) {
        int lastIndex = name.lastIndexOf(".");
        return name.substring(lastIndex + 1);
    }
}
