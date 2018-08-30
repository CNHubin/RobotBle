package com.hubin.bleclient.ble.bleInterface;


import com.hubin.bleclient.ble.bleException.BleException;

/*
 *  @项目名：  AiiageSteamAndroid
 *  @包名：   com.aiiage.steam.mobile.ble.bleInterface
 *  @文件名:   BleReadListener
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/12 16:09
 *  @描述：    Ble读取特征的监听接口
 */
public interface BleReadListener {

    /**
     * 读取特征成功
     *
     * @param text
     */
    void onReadSuccess(String text);

    /**
     * 读取特征失败
     *
     * @param exception
     */
    void onReadFailure(BleException exception);
}
