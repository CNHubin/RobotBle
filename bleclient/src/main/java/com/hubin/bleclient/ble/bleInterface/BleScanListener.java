package com.hubin.bleclient.ble.bleInterface;


import com.hubin.bleclient.ble.BleCore.BleDevice;
import com.hubin.bleclient.ble.bleException.BleException;

import java.util.List;

/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.bleInterface
 * @文件名: BleScanListener
 * @创建者: 胡英姿
 * @创建时间: 2018/6/13 14:31
 * @描述： Ble扫描设备时的监听接口
 */
public interface BleScanListener {
    // BLE开始扫描
    void onScanStarted();

    //BLE正在扫描
    void onScanning(BleDevice bleDevice);

    // BLE扫描结束
    void onScanFinished(List<BleDevice> deviceList);

    //BLE发起扫描失败
    void onScanFailed(BleException exception);
}
