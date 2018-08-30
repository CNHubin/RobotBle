package com.hubin.bleclient.ble.bleInterface;


import com.hubin.bleclient.ble.BleCore.BleDevice;
import com.hubin.bleclient.ble.bleException.BleException;

/*
 *  @项目名：  AiiageSteamAndroid
 *  @包名：    com.aiiage.steam.mobile.ble.bleInterface
 *  @文件名:   BleConnectListener
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/13 14:38
 *  @描述：     Ble连接状态监听接口
 */
public interface BleConnectListener {
    /**
     * BLE正在建立连接
     */
    void onStartConnect(BleDevice bleDevice);

    /**
     * BLE连接成功
     *
     * @param bleDevice
     */
    void onConnectSuccess(BleDevice bleDevice);

    /**
     * BLE连接失败
     *
     * @param bleDevice
     * @param exception
     */
    void onConnectFail(BleDevice bleDevice, BleException exception);

    /**
     *  BLE连接中断
     */
    void onDisconnected(int status);

}
