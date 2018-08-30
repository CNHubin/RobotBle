package com.hubin.bledemo.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

/*
 *  @项目名：  BleDemo
 *  @包名：    com.hubin.bledemo.base
 *  @文件名:   BleApp
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/7 11:54
 *  @描述：    TODO
 */
public class BleApp extends Application {
    private static Context mContext;
    private static Handler mHandler;
    @Override
    public void onCreate() {
        super.onCreate();
        //上下文
        mContext = getApplicationContext();
        //创建一个主线程的Handler
        mHandler = new Handler();

    }
    /**
     * 获取全局上下文
     * @return
     */
    public static Context getContext() {
        return mContext;
    }

    /**
     * 得到主线程的handler
     *
     * @return
     */
    public static Handler getMainThreadHandler() {
        return mHandler;
    }
}
