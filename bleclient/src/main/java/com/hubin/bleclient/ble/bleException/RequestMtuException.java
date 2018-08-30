package com.hubin.bleclient.ble.bleException;

/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.exception
 * @文件名: RequestMtuException
 * @创建者: 胡英姿
 * @创建时间: 2018/7/5 20:39
 * @描述： 申请MTU失败
 */
public class RequestMtuException extends BleException {
    private static final String TAG = "RequestMtuException";
    @Override
    public String getException() {
        return TAG;
    }

    @Override
    public int getErrorCode() {
        return ERROR_CODE_MTU_FAIL;
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
