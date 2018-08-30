package com.hubin.bleclient.utils;

import java.util.Arrays;

/*
 *  @项目名：  BleDemo
 *  @包名：    com.hubin.bleclient.utils
 *  @文件名:   BytesUtils
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/8 20:13
 *  @描述：    TODO
 */
public class BytesUtils {
    /**
     * 拼接两个 byte 数组
     * */
    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }
}
