package com.hubin.bleclient.ble.bleInterface;


import com.hubin.bleclient.ble.bleException.BleException;

/*
 *  @项目名：  AiiageSteamAndroid
 *  @包名：    com.aiiage.steam.mobile.ble.bleInterface
 *  @文件名:   BleNotifyListener
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/15 14:38
 *  @描述：    Ble通知接收的监听接口
 */
public interface BleNotifyListener {

     //notify开启成功
     void onNotifySuccess();

     //notify开启或关闭操作失败
     void onNotifyFailure(BleException exception);

     //收到消息
     void onReceiveMsg(byte[] data);

     //停止Notify
     void onStopNotify();
}
