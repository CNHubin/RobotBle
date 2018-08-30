package com.hubin.bleclient.utils;

/*
 *  @项目名：  BleDemo
 *  @包名：    com.hubin.bleclient.utils
 *  @文件名:   StringUtils
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/9 9:20
 *  @描述：    TODO
 */
public class StringUtils {
    /**
     * Return whether the string is null or 0-length.
     *
     * @param s The string.
     * @return {@code true}: yes<br> {@code false}: no
     */
    public static boolean isEmpty(final CharSequence s) {
        return s == null || s.length() == 0;
    }
}
