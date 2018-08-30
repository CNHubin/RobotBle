package com.hubin.bleclient.utils;

import java.util.Random;

/**
 * @项目名： AiiageSteamRobot
 * @包名： com.aiiage.steam.common.utils
 * @文件名: MacUtils
 * @创建者: 胡英姿
 * @创建时间: 2018/7/25 16:10
 * @描述： mac地址工具
 */
public class MacUtils {

    private static final char hexDigits[] ={'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'A', 'B', 'C', 'D', 'E', 'F'};
    /**
     * 随机生成一个mac地址
     * @return
     */
    public static String randomMac() {
        Random random = new Random();
        StringBuffer mStringBuffer = new StringBuffer();
        mStringBuffer.append(String.format("%02x", random.nextInt(0xff))+":");
        mStringBuffer.append(String.format("%02x", random.nextInt(0xff))+":");
        mStringBuffer.append(String.format("%02x", random.nextInt(0xff))+":");
        mStringBuffer.append(String.format("%02x", random.nextInt(0xff))+":");
        mStringBuffer.append(String.format("%02x", random.nextInt(0xff))+":");
        mStringBuffer.append(String.format("%02x", random.nextInt(0xff)));
        return mStringBuffer.toString();
    }

    /**
     * 将16位mac地址切割成String表示
     * @param bytes
     * @return
     */
    public static String bytes2HexStringMac(final byte[] bytes) {
        if (bytes == null) return null;
        int len = bytes.length;
        if (len <= 0) return null;
        if (len!=6) return null;
        char[] ret = new char[(len+5) << 1];
        for (int i = 0, j = 0; i < len; i++) {
            ret[j++] = hexDigits[bytes[i] >>> 4 & 0x0f];
            ret[j++] = hexDigits[bytes[i] & 0x0f];
            if (i != len - 1) {
                ret[j++] = ':';
            }
        }
        return new String(ret);
    }
}
