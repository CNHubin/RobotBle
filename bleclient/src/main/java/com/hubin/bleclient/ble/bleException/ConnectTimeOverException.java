package com.hubin.bleclient.ble.bleException;

/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.exception
 * @文件名: ConnectTimeOverException
 * @创建者: 胡英姿
 * @创建时间: 2018/7/4 17:14
 * @描述： 连接超时的异常
 */
public class ConnectTimeOverException extends BleException {
    private static final String TAG = "ConnectTimeOverException";
    @Override
    public String getException() {
        return TAG;
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_CONNECT_OVER;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
