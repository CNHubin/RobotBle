package com.hubin.bledemo.ble.bleconfig;

import java.util.UUID;

/**
 * @author thearyong
 * @date 2017/12/14
 */

public class BLEConstants {
    public static final byte[] MSG_NOTIFY_CLOSE = {0X44}; //断开指令
    /**
     * BLE 蓝牙服务端 UUID
     * */
    public static final UUID UUID_BLE_SERVICE = UUID.fromString("0000a0a0-0000-1000-8000-00805f9b34fb");
//    public static final UUID UUID_BLE_SERVICE = UUID.fromString("6E6B5C64-FAF7-40AE-9C21-D4933AF45B23");
//    public static final UUID UUID_BLE_SERVICE = UUID.fromString("00001106-0000-1000-8000-00805F9B34FB");

    /**
     * 特征1 UUID，可读，订阅，通知
     * */
    public static UUID UUID_CHARACTERISTIC_READ = UUID.fromString("0000b0b0-0000-1000-8000-00805f9b34fb");
    /**
     * 特征2 UUID，获取 token
     */
    public static UUID UUID_CHARACTERISTIC_TOKEN = UUID.fromString("0000b1b1-0000-1000-8000-00805f9b34fb");
    /**
     * 特征3 UUID，获取 petid
     */
    public static UUID UUID_CHARACTERISTIC_PETID = UUID.fromString("0000b2b2-0000-1000-8000-00805f9b34fb");
    /**
     * 特征4 UUID，获取 petid和 token
     */
    public static UUID UUID_CHARACTERISTIC_PETID_TOKEN = UUID.fromString("0000b3b3-0000-1000-8000-00805f9b34fb");
    /**
     * 特征5 UUID，刷新 token
     */
    public static UUID UUID_CHARACTERISTIC_REFRESH_TOKEN = UUID.fromString("0000b4b4-0000-1000-8000-00805f9b34fb");
    /**
     * 特征6 UUID，用于Notify
     */
    public static UUID UUID_CHARACTERISTIC_NOTIFY = UUID.fromString("0000b5b5-0000-1000-8000-00805f9b34fb");
    //用于特征6 UUID   Notify通知的描述
    public static final UUID UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");



    /**
     * 特征6 UUID，可读可写
     * */
    public static UUID UUID_CHARACTERISTIC_WRITE = UUID.fromString("0000c0c0-0000-1000-8000-00805f9b34fb");
//    public static UUID UUID_CHARACTERISTIC_WRITE = UUID.fromString("477A2967-1FAB-4DC5-920A-DEE5DE685A3D");

    public static UUID UUID_DESCRIPTOR = UUID.fromString("0000d0d0-0000-1000-8000-00805f9b34fb");

    /**
     * 初始化
     */
    public static final int MSG_INIT = 0x0;


    /**
     * 服务端发送消息
     */
    public static final int MSG_SEVICE_TO_ACTIVITY = 0x1000;

    /**
     * ble 服务端接收的消息类型
     */
    public static final String BLUETOOTH_DATA_END = "@_END_@";
    public static final int BLUETOOTH_DATA_TYPE_STRING = 0x0;
    public static final int BLUETOOTH_DATA_TYPE_XML = 0x1;
    public static final int BLUETOOTH_DATA_TYPE_AUDIO = 0x2;
    public static final int BLUETOOTH_DATA_TYPE_IMG = 0x3;
    public static final int BLUETOOTH_DATA_TYPE_JSON = 0x4;
    public static final int BLUETOOTH_DATA_TYPE_TEXT = 0x5;
    public static final int BLUETOOTH_DATA_TYPE_ZIP = 0x6;

    public static final int BLUETOOTH_DATA_MSG_SUCCESS = 20200;
    public static final int BLUETOOTH_DATA_MSG_ERR = 20201;
    //通知上个设备断开
    public static final int BLUETOOTH_MSG_NOTIFY_DISCONNECT_LAST = 30001;

    /**
     * 命令---通知机器人同步数据
     */
    public static final String BLUETOOTH_CMD_SYNC_DATA = "sync_data";

}
