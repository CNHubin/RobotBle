package com.hubin.bleclient.utils;

/*
 *  @项目名：  BleDemo
 *  @包名：    com.hubin.bleclient.utils
 *  @文件名:   RssiUtil
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/28 11:13
 *  @描述：    功能：根据rssi计算距离
 */
public class RssiUtil {
    //A和n的值，需要根据实际环境进行检测得出
    private static final double A_Value = 50;
    /**
     * A - 发射端和接收端相隔1米时的信号强度
     */
    private static final double n_Value = 2.5;/** n - 环境衰减因子*/

    /**
     * 根据Rssi获得返回的距离,返回数据单位为m
     *
     * @param rssi
     * @return
     */
    public static double getDistance(int rssi) {
        int iRssi = Math.abs(rssi);
        double power = (iRssi - A_Value) / (10 * n_Value);
        return Math.pow(10, power);
    }
}