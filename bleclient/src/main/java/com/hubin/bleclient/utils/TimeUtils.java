package com.hubin.bleclient.utils;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @项目名： BleDemo
 * @包名： com.hubin.bleclient.utils
 * @文件名: TimeUtils
 * @创建者: 胡英姿
 * @创建时间: 2018/7/3 16:26
 * @描述： 时间转换工具类
 */
public class TimeUtils {

    /**
     * 将时间戳转换为日期
     * @param timeStamp 时间戳
     */
    public static String timeStamp2Date(String timeStamp) {
        timeStamp=timeStamp.substring(2, timeStamp.length()-3);
        return timeStamp2Date(timeStamp,"yyyy-MM-dd HH:mm:ss");
    }

    public static String timeToData(long time) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
        Date date = new Date(time);
        return simpleDateFormat.format(date);
    }
    /**
     * 时间戳转换成日期格式字符串
     * @param seconds 精确到秒的字符串
     * @return
     */
    public static String timeStamp2Date(String seconds,String format) {
        if(seconds == null || seconds.isEmpty() || seconds.equals("null")){
            return "";
        }
        if(format == null || format.isEmpty()) format = "yyyy-MM-dd HH:mm:ss";
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(new Date(Long.valueOf(seconds+"000")));
    }
    /**
     * 日期格式字符串转换成时间戳
     * @param date_str 字符串日期
     * @param format 如：yyyy-MM-dd HH:mm:ss
     * @return
     */
    public static String date2TimeStamp(String date_str,String format){
        try {
            SimpleDateFormat sdf = new SimpleDateFormat(format);
            return String.valueOf(sdf.parse(date_str).getTime()/1000);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * 取得当前时间戳（精确到秒）
     * @return
     */
    public static String timeStamp(){
        long time = System.currentTimeMillis();
        String t = String.valueOf(time/1000);
        return t;
    }

    //  输出结果：
    //	timeStamp=1417792627
    //	date=2014-12-05 23:17:07
    //	1417792627
    public static void main(String[] args) {
        String timeStamp = timeStamp();
        System.out.println("timeStamp="+timeStamp);

        String date = timeStamp2Date(timeStamp, "yyyy-MM-dd HH:mm:ss");
        System.out.println("date="+date);

        String timeStamp2 = date2TimeStamp(date, "yyyy-MM-dd HH:mm:ss");
        System.out.println(timeStamp2);
    }
}
