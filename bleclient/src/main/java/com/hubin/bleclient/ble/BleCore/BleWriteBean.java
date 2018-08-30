package com.hubin.bleclient.ble.BleCore;


import com.hubin.bleclient.ble.bleConfig.FileSuffix;
import com.hubin.bleclient.ble.bleInterface.BleWriteListener;

import java.util.UUID;

/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.BleCore
 * @文件名: BleWriteBean
 * @创建者: 胡英姿
 * @创建时间: 2018/7/10 20:16
 * @描述： 写数据时需要的参数
 */
public class BleWriteBean {
   public UUID serviceUUID;
   public UUID characteristicUUID;
   public FileSuffix fileSuffix;
   public byte[] data;
   public BleWriteListener bleWriteListener;

    public BleWriteBean(UUID serviceUUID, UUID characteristicUUID, FileSuffix fileSuffix, byte[] data,
                        BleWriteListener bleWriteListener) {
        this.serviceUUID = serviceUUID;
        this.characteristicUUID = characteristicUUID;
        this.fileSuffix = fileSuffix;
        this.data = data;
        this.bleWriteListener = bleWriteListener;
    }
}
