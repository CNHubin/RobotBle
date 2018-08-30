package com.hubin.bledemo.ble.bleconfig;


import com.hubin.bledemo.utils.LogUtils;

/*
 *  @项目名：  BleDemo
 *  @包名：    com.hubin.bleclient.ble.bleConfig
 *  @文件名:   FileSuffix
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/13 19:44
 *  @描述：    Ble发起PostFile时携带的文件后缀
 */
public enum FileSuffix {
    STRING(""),TEXT(".text"),XML(".xml"),AUDIO(".audio"),IMG(".img"),ZIP(".zip");

    private String value;
    private FileSuffix(String value) {
        this.value = value;
    }

    /**
     * 将枚举所对应的文件后缀 转换为传输时的字节码
     * @param fileSuffix
     * @return
     */
    public static byte[] suffixToBytes(FileSuffix fileSuffix) {
        int type = BLEConstants.BLUETOOTH_DATA_TYPE_ZIP;
        byte[] bytes = null;
        switch (fileSuffix) {
            case STRING:
                type = BLEConstants.BLUETOOTH_DATA_TYPE_STRING;
                break;
            case TEXT:
                type = BLEConstants.BLUETOOTH_DATA_TYPE_TEXT;
                break;
            case ZIP:
                type = BLEConstants.BLUETOOTH_DATA_TYPE_ZIP;
                break;
            case XML:
                type = BLEConstants.BLUETOOTH_DATA_TYPE_XML;
                break;
            case IMG:
                type = BLEConstants.BLUETOOTH_DATA_TYPE_IMG;
                break;
            case AUDIO:
                type = BLEConstants.BLUETOOTH_DATA_TYPE_AUDIO;
                break;
            default:
                LogUtils.e("suffixToBytes E : 不支持的传输文件类型，默认设为.zip！");
                break;
        }
        return new byte[]{(byte) type};
    }

    /**
     * 通过 type 获取文件后缀
     */
    public static FileSuffix bytesToSuffix(int type) {
        switch (type) {
            case  BLEConstants.BLUETOOTH_DATA_TYPE_STRING:
                return STRING;
            case BLEConstants.BLUETOOTH_DATA_TYPE_ZIP:
                return ZIP;
            case BLEConstants.BLUETOOTH_DATA_TYPE_TEXT:
                return TEXT;
            case BLEConstants.BLUETOOTH_DATA_TYPE_XML:
                return XML;
            case BLEConstants.BLUETOOTH_DATA_TYPE_IMG:
                return IMG;
            case BLEConstants.BLUETOOTH_DATA_TYPE_AUDIO:
                return AUDIO;
            default:
                LogUtils.e("bytesToSuffix E : 没有定义的文件后缀！");
                break;
        }
        return STRING;
    }

}
