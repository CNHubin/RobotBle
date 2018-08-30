package com.hubin.bleclient.ble.bleException;

/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.exception
 * @文件名: ScanFiaLeException
 * @创建者: 胡英姿
 * @创建时间: 2018/7/6 11:37
 * @描述： 扫描失败异常
 */
public class ScanFailedException extends BleException {
    private static final String TAG = "ScanFailedException";
    private int status;

    public ScanFailedException(int status) {
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
