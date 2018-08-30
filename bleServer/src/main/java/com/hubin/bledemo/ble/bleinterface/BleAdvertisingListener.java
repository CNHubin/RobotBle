package com.hubin.bledemo.ble.bleinterface;

import android.bluetooth.le.AdvertiseSettings;

/*
 *  @项目名：  BleDemo
 *  @包名：    com.hubin.bledemo.ble.bleinterface
 *  @文件名:   BleAdvertisingListener
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/19 9:59
 *  @描述：    BLE 对外广播设置的回调接口
 */
public interface BleAdvertisingListener {

    /**
     * 广播设置并启动成功
     * @param settingsInEffect
     */
     void onStartSuccess(AdvertiseSettings settingsInEffect);

    /**
     * 广播设置或启动失败
     * @param exception 异常
     */
     void onStartFailure(Exception exception);
}
