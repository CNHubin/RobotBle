package com.hubin.bleclient.ble.bleException;

/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.exception
 * @文件名: CharacteristicReadException
 * @创建者: 胡英姿
 * @创建时间: 2018/7/6 16:20
 * @描述： 特征读取失败异常
 */
public class CharacteristicReadException extends BleException {
    private static final String TAG = "CharacteristicReadException";
    private int status;

    public CharacteristicReadException(int status) {
        this.status = status;
    }
    @Override
    public String getException() {
        return TAG;
    }

    @Override
    public int getErrorCode() {
        return status;
    }

    @Override
    public String toString() {
        return super.toString();
    }
}
