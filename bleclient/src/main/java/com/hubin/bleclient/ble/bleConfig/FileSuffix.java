package com.hubin.bleclient.ble.bleConfig;


import com.hubin.bleclient.utils.LogUtils;

/*
 *  @项目名：  AiiageSteamAndroid
 *  @包名：    com.aiiage.steam.mobile.ble.bleConfig
 *  @文件名:   FileSuffix
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/13 19:44
 *  @描述：    Ble发起PostFile时携带的文件后缀
 */
public enum FileSuffix {
    STRING, TEXT, XML, AUDIO, IMG, ZIP;

    /**
     * 将枚举所对应的文件后缀 转换为传输时的字节码
     *
     * @param fileSuffix
     * @return
     */
    public static byte[] suffixToBytes(FileSuffix fileSuffix) {
        int type = BLEConfig.BLUETOOTH_DATA_TYPE_ZIP;
        byte[] bytes = null;
        switch (fileSuffix) {
            case STRING:
                type = BLEConfig.BLUETOOTH_DATA_TYPE_STRING;
                break;
            case TEXT:
                type = BLEConfig.BLUETOOTH_DATA_TYPE_TEXT;
                break;
            case ZIP:
                type = BLEConfig.BLUETOOTH_DATA_TYPE_ZIP;
                break;
            case XML:
                type = BLEConfig.BLUETOOTH_DATA_TYPE_XML;
                break;
            case IMG:
                type = BLEConfig.BLUETOOTH_DATA_TYPE_IMG;
                break;
            case AUDIO:
                type = BLEConfig.BLUETOOTH_DATA_TYPE_AUDIO;
                break;
            default:
                LogUtils.e("suffixToBytes E : 不支持的传输文件类型，默认设为.zip！");
                break;
        }
        return new byte[]{(byte) type};
    }

}
