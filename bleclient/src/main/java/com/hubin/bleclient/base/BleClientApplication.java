package com.hubin.bleclient.base;

import android.app.Application;
import android.content.Context;
import android.os.Handler;

/*
 *  @项目名：  BleDemo
 *  @包名：    com.hubin.bleclient.base
 *  @文件名:   BleClientApplication
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/8 17:28
 *  @描述：    TODO
 */
public class BleClientApplication extends Application {

    static BleClientApplication app;
    private static Handler mHandler;
    private static Context mContext;
    public static BleClientApplication getApp() {
        return app;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
        //创建一个主线程的Handler
        mHandler = new Handler();

        mContext = getApplicationContext();
    }

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
