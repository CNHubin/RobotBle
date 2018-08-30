package com.hubin.bleclient.ble.bleInterface;


import com.hubin.bleclient.ble.bleException.BleException;

/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.bleInterface
 * @文件名: BleMtuListener
 * @创建者: 胡英姿
 * @创建时间: 2018/7/5 20:35
 * @描述： 设置MTU值的回调监听
 */
public interface BleMtuListener {

    void onRequestMtuSuccess(int mtu);

    void onRequestMtuFailure(BleException exception);
}
