package com.hubin.bleclient.ble.BleCore;


import com.hubin.bleclient.ble.bleInterface.BleReadListener;

import java.util.UUID;

/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.BleCore
 * @文件名: BleReadBean
 * @创建者: 胡英姿
 * @创建时间: 2018/7/10 20:23
 * @描述： 读数据时需要的参数
 */
public class BleReadBean {
   public UUID serviceUUID;
   public UUID characteristicUUID;
   public BleReadListener bleReadListener;

    public BleReadBean(UUID serviceUUID, UUID characteristicUUID, BleReadListener bleReadListener) {
        this.serviceUUID = serviceUUID;
        this.characteristicUUID = characteristicUUID;
        this.bleReadListener = bleReadListener;
    }
}
