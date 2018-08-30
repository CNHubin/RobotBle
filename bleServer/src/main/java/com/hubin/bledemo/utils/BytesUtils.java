package com.hubin.bledemo.utils;

import java.util.Arrays;

/**
 * @author thearyong
 * @date 2018/2/2
 */

public class BytesUtils {
    private static String hexStr = "0123456789ABCDEF"; //全局
    /**
     * 拼接两个 byte 数组
     */
    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    /**
     * 将16进制的字符串转换为字节数组
     * @param hexString
     * @return
     */
    public static byte[] hexStringToBinary(String hexString){
        //hexString的长度对2取整，作为bytes的长度
        int len = hexString.length()/2;
        byte[] bytes = new byte[len];
        byte high = 0;//字节高四位
        byte low = 0;//字节低四位
        for(int i=0;i<len;i++){
            high = (byte)((hexStr.indexOf(hexString.charAt(2*i)))<<4);//右移四位得到高位
            low = (byte)hexStr.indexOf(hexString.charAt(2*i+1));
            bytes[i] = (byte) (high|low);//高地位做或运算
        }
        return bytes;
    }

    /**
     * 将mac地址转换位byte数组
     * @param mac
     * @return
     */
    public static byte[] macToBytes(String mac) {
        StringBuffer mStringBuffer = new StringBuffer();
        String[] split = mac.trim().split(":");
        for (String s : split) {
            mStringBuffer.append(s);
        }
        return hexStringToBinary(mStringBuffer.toString());
    }
}
