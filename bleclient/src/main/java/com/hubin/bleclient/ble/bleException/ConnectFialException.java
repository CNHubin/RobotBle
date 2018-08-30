package com.hubin.bleclient.ble.bleException;

/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.exception
 * @文件名: ConnectFialException
 * @创建者: 胡英姿
 * @创建时间: 2018/7/4 11:24
 * @描述： 连接异常
 */
public class ConnectFialException extends BleException {
    private static final String TAG = "ConnectFialException";
    private int status;
    public ConnectFialException(int status) {
        this.status = status;
    }

    @Override
    public String toString() {
        return super.toString();
    }

    @Override
    public String getException() {
        return TAG;
    }

    @Override
    public int getErrorCode() {
        return status;
    }
}
