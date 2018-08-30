package com.hubin.bledemo.ble.blecontrol;

import android.bluetooth.le.AdvertiseSettings;

import com.hubin.bledemo.ble.bleinterface.BleAdvertisingListener;
import com.hubin.bledemo.ble.bleinterface.BleConnectListener;
import com.hubin.bledemo.ble.bleinterface.BleReadOrWriteListener;
import com.hubin.bledemo.utils.LogUtils;


/*
 *  @项目名：  BleDemo
 *  @包名：    com.hubin.bledemo.ble.blecontrol
 *  @文件名:   BleHelper
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/19 11:22
 *  @描述：    蓝牙服务端操作 助手
 */
public class BleHelper {
    //开启Ble
    public static void startBleServer() {
        if (!BleServerManager.getInstance().init()) {//初始化
            LogUtils.e("startBleServer E : 蓝牙初始化失败！");
            return;
        }

        BleServerManager.getInstance().startAdvertising(new BleAdvertisingListener() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                LogUtils.d("onStartSuccess  D : 广播配置完成，开启GATT服务！");
                BleServerManager.getInstance().startGATTServer();//开启GATT服务
            }
            @Override
            public void onStartFailure(Exception exception){
                LogUtils.e("onStartFailure E : "+exception.toString());
            }
        });
    }

    //停止Ble
    public static void stopBleServer() {

        BleServerManager.getInstance().stopAdvertising(new BleAdvertisingListener() {
            @Override
            public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                LogUtils.d("onStartSuccess  D : ");
            }
            @Override
            public void onStartFailure(Exception exception) {
                LogUtils.e("onStartFailure E : 停止蓝牙！");
            }
        });
        BleServerManager.getInstance().stopGATTServer();
    }

    //发送通知  Notify
    public static void sendNotify(String msg) {
        BleServerManager.getInstance().sendNotify(msg);
    }

    /**
     * 设置Gatt连接状态接听器
     * @param bleConnectListener
     */
    public static void setOnBleConnectListener(BleConnectListener bleConnectListener) {
        BleServerManager.getInstance().setOnBleConnectListener(bleConnectListener);
    }
    /**
     * 设置Gatt读写数据监听器
     * @param bleReadOrWriteListener
     */
    public static void setOnReadOrWriteListener(BleReadOrWriteListener bleReadOrWriteListener) {
        BleServerManager.getInstance().setOnReadOrWriteListener(bleReadOrWriteListener);
    }
}
