package com.hubin.bledemo.ble.bleinterface;

import android.bluetooth.BluetoothDevice;

/*
 *  @项目名：  AiiageSteamRobot
 *  @包名：    com.aiiage.steam.robot.ble.bleinterface
 *  @文件名:   BleConnectListener
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/19 17:44
 *  @描述：    TODO
 */
public interface BleConnectListener {
    /**
     * 新的连接
     * @param device
     */
    void onConnect(BluetoothDevice device);

    /**
     * 连接断开
     * @param device
     */
    void onDisconnect(BluetoothDevice device);
}
