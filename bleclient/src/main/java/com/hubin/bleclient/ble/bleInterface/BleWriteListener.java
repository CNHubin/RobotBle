package com.hubin.bleclient.ble.bleInterface;


import com.hubin.bleclient.ble.bleException.BleException;

/*
 *  @项目名：  AiiageSteamAndroid
 *  @包名：    com.aiiage.steam.mobile.ble.bleInterface
 *  @文件名:   BleWriteListener
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/13 9:58
 *  @描述：     Ble读取写入的监听接口
 */
public interface BleWriteListener {

    /**
     * 发送成功
     */
    void onWriteSuccess();

    /**
     * 发送失败
     *
     * @param exception
     */
    void onWriteFailure(final BleException exception);
}
