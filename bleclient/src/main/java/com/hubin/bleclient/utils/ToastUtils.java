package com.hubin.bleclient.utils;

/*
 *  @项目名：  TYLE
 *  @包名：    com.taiyao.mahjonghall.presenter.util
 *  @文件名:   ToastUtils
 *  @创建者:   胡英姿
 *  @创建时间:  2018-03-26 9:49
 *  @描述：    toast 工具类 防止吐司队列过多时 不停的弹出
 */

import android.content.Context;
import android.os.Looper;
import android.support.annotation.UiThread;
import android.widget.Toast;

import com.hubin.bleclient.base.BleClientApplication;

public class ToastUtils {

    private static Toast toast;

    /**
     * 弹出toast 可以在任意线程使用
     * @param msg
     */
    public static void toast(String msg) {
        if (Looper.myLooper() == Looper.getMainLooper()) {
            toast(BleClientApplication.getContext(),msg);
        } else {
            ThreadUtils.mainHandler.post(()->{
                toast(BleClientApplication.getContext(),msg);
            }) ;
        }
    }

    /**
     * 弹出Toast
     * @param context
     * @param msg
     */
    @UiThread  //只能在主线程中使用
    public static void toast( Context context, String msg) {
        if (context == null) {
            LogUtils.e("toast E : 没有Context：" + msg);
            return;
        }
        if (toast == null) {
            toast = Toast.makeText(context, msg, Toast.LENGTH_SHORT);
        } else {
            toast.setText(msg);
        }
        toast.show();
    }

}
