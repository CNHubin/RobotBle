package com.hubin.bleclient.ble.bleException;

/**
 * @项目名： RobotBle
 * @包名： com.aiiage.steam.mobile.ble.exception
 * @文件名: CharacteristicWriteException
 * @创建者: 胡英姿
 * @创建时间: 2018/7/9 17:00
 * @描述： 特征写入失败异常
 */
public class CharacteristicWriteException extends BleException {
    private static final String TAG = "CharacteristicWriteException";
    private int status;

    public CharacteristicWriteException(int status) {
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
