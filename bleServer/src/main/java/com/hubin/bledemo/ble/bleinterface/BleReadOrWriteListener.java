package com.hubin.bledemo.ble.bleinterface;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;

/*
 *  @项目名：  AiiageSteamRobot
 *  @包名：    com.aiiage.steam.robot.ble.bleinterface
 *  @文件名:   BleReadOrWriteListener
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/19 17:41
 *  @描述：    BLE 服务端 读写状态回调接听器
 */
public interface BleReadOrWriteListener {
    //收到纯字符串指令
    void onStringCmd(String msg);

    /**
     * 接收文件完成
     * @param total 文件大小
     * @param path  存储路径
     */
    void onReceiveSuccess(String total, String path);

    //远程客户端请求读取本地特征
    void onCharacteristicReadRequest(BluetoothDevice device, BluetoothGattCharacteristic
            characteristic, int offset);
    //远程客户端请求写入本地特征
    void onCharacteristicWriteRequest(BluetoothDevice device, BluetoothGattCharacteristic
            characteristic, int offset);

    //远程客户端请求读取本地描述
    void onDescriptorReadRequest(BluetoothDevice device, BluetoothGattDescriptor descriptor, int offset);
    //远程客户端请求写入本地描述
    void onDescriptorWriteRequest(BluetoothDevice device, BluetoothGattDescriptor descriptor, int offset);
}
