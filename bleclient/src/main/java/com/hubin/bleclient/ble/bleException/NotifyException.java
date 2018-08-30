package com.hubin.bleclient.ble.bleException;

/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.bleException
 * @文件名: NotifyException
 * @创建者: 胡英姿
 * @创建时间: 2018/7/13 11:48
 * @描述： notify异常
 */
public class NotifyException extends BleException {
    private static final String TAG = "NotifyException";
    private int status;

    public NotifyException(int status) {
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
