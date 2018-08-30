package com.hubin.bleclient.ble.bleException;

import java.io.Serializable;

/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.exception
 * @文件名: BleException
 * @创建者: 胡英姿
 * @创建时间: 2018/7/4 11:22
 * @描述： 蓝牙异常基类
 */
public abstract class BleException implements Serializable {
    private static final String TAG = "BleException";

    public static final int ERROR_CODE_GATT_NULL = 400;//连接异常
    public static final int ERROR_CODE_CONNECT_OVER = 401;//连接超时

    public static final int ERROR_CODE_MTU_FAIL = 402;//mtu申请失败

    public static final int ERROR_CODE_SCAN_FAIL = 500;//扫描失败
    public static final int ERROR_CODE_BLE_NOT_ENABLE = 501;//蓝牙未启用
    public static final int ERROR_CODE_BLE_NOT_SUPPORT = 502;//不支持蓝牙
    public static final int ERROR_CODE_BLE_SCANNER_NOPOINTER = 503;//扫描器为空
    public static final int ERROR_CODE_BLE_NOT_CONNECT = 504;//蓝牙未连接
    public static final int ERROR_CODE_BLE_DISCOVERSERVICES_FAIL = 505;//服务发现操作失败


    public static final int ERROR_CODE_READ_UUID_NOPOINTER = 600;//UUID为空
    public static final int ERROR_CODE_READ_NOT_FOUND_SERVICE = 601;//没有发现服务
    public static final int ERROR_CODE_READ_NOT_FOUND_CHARACTERISTIC = 602;//没有发现特征
    public static final int ERROR_CODE_READ_NOT_SUPPORT = 603;//特征不支持读
    public static final int ERROR_CODE_READ_OPERATION_FAILED = 604;//读取操作失败
    public static final int ERROR_CODE_READ_EMPTY_DATA = 605;//读到的数据为空

    public static final int ERROR_CODE_WRITE_NOT_FOUND_CALLBACK = 700;//没有实现回调
    public static final int ERROR_CODE_WRITE_OPERATION_FAILED = 701;//写操作失败
    public static final int ERROR_CODE_WRITE_VALUE_FAILED = 702;//特征赋值失败

    public static final int ERROR_CODE_NOTIFY_NOT_SUPPORT = 800;//特征不支持notify
    public static final int ERROR_CODE_NOTIFY_OPERATION_FAILED = 801;//notify操作失败
    public static final int ERROR_CODE_NOTIFY_DESCRIPTOR_NOT_FOUND = 802;//没有发现描述
    public static final int ERROR_CODE_NOTIFY_WRITEDESCRIPTOR_FAILED = 803;//写出描述失败

    @Override
    public String toString() {
        return  TAG+"."+getException() + " ,errorCode=" + getErrorCode();
    }

    public abstract String getException();

    public abstract int getErrorCode();
}
