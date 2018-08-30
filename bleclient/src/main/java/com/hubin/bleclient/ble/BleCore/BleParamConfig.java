package com.hubin.bleclient.ble.BleCore;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanSettings;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.BleCore
 * @文件名: BleParamConfig
 * @创建者: 胡英姿
 * @创建时间: 2018/7/2 15:06
 * @描述： 蓝牙所有对外开放的设置
 */
public class BleParamConfig {
    public static final int SCAN_MODE_LOW_POWER = 0;//低功耗(默认扫描模式)
    public static final int SCAN_MODE_BALANCED = 1; //均衡模式
    public static final int SCAN_MODE_LOW_LATENCY = 2;//低延迟模式（使用最高占空比进行扫描，建议仅当应用程序在前台运行时才使用此模式。）

    public static final int CALLBACK_TYPE_ALL_MATCHES =1;//为找到的每个符合过滤标准的蓝牙广告触发回拨。如果没有激活过滤器，则报告所有广告包。
    public static final int CALLBACK_TYPE_FIRST_MATCH =2;//结果回调仅针对接收到的与过滤标准匹配的第一个广告数据包触发。
    public static final int CALLBACK_TYPE_MATCH_LOST = 4;//当不再收到来自之前由第一个匹配回调报告的设备的广告时接收回调。

    public static final int MATCH_NUM_ONE_ADVERTISEMENT = 1; //每个过滤器匹配一个广播
    public static final int MATCH_NUM_FEW_ADVERTISEMENT = 2; //根据当前资源情况每个过滤器匹配少量广播
    public static final int MATCH_NUM_MAX_ADVERTISEMENT = 3; // 每个过滤器匹配尽量多的广播

    public static final int MATCH_MODE_AGGRESSIVE = 1; //信号弱的设备也进行扫描
    public static final int MATCH_MODE_STICKY =2;   //只扫描信号强的设备

    public static final int PHY_LE_1M = 1; //支持蓝牙5，1MPHY; 兼容4.1，4.2，4.3
    public static final int PHY_LE_2M = 2; //支持蓝牙5，2MPHY; 高速率
    public static final int PHY_LE_CODED = 3; //支持蓝牙5，CodedPHY;

    private String deviceName; //设备名字
    private String deviceAddress;//设备地址
    private UUID[] uuids; //根据UUID过滤

    //5.0以上
    private int scanMode; //扫描强度
    private int reportDelayTime;//设置扫描结果回调延时，
    //6.0以上
    private int callbackType;//扫描结果的回调类型
    private int numOfMatches ;//过滤器匹配尽量多的广播数量
    private int matchMode; //匹配模式
    //8.0以上
    private boolean isLegacy ; //是否扫描4.2以下的蓝牙设备
    private int phy ; //设置物理层  只有使用了setLegacy(false)  才能设置此物理层函数

    private int scanTimeOut; //扫描时间 默认5秒
    private int connectTimeOut ; //连接超时时间
    private int scanOrconnectGapTime ; //扫描停止后连接的间隔时间
    private int errorGapTime ; //发生连接错误时重连的间隔时间 只要针对133异常
    private int connectedDelay ; //连接成功后的延时时间
    private boolean isAutoConnect; //false直接连接到蓝牙，true 蓝牙可用时自动连接
    private int connectMaxCount; //连接最大次数


    private BleParamConfig(String deviceName, String deviceAddress, UUID[] uuids, int scanMode, int
            reportDelayTime, int callbackType, int numOfMatches, int matchMode, boolean isLegacy, int
            phy, int scanTimeOut, int connectTimeOut, int scanOrconnectGapTime, int errorGapTime, int
            connectedDelay, boolean isAutoConnect, int connectMaxCount) {
        this.deviceName = deviceName;
        this.deviceAddress = deviceAddress;
        this.uuids = uuids;
        this.scanMode = scanMode;
        this.reportDelayTime = reportDelayTime;
        this.callbackType = callbackType;
        this.numOfMatches = numOfMatches;
        this.matchMode = matchMode;
        this.isLegacy = isLegacy;
        this.phy = phy;
        this.scanTimeOut = scanTimeOut;
        this.connectTimeOut = connectTimeOut;
        this.scanOrconnectGapTime = scanOrconnectGapTime;
        this.errorGapTime = errorGapTime;
        this.connectedDelay = connectedDelay;
        this.isAutoConnect = isAutoConnect;
        this.connectMaxCount = connectMaxCount;
    }

    /**
     * 初始化扫描规则
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public ScanSettings getGlobaltScanSettings() {
        ScanSettings.Builder builder =  new ScanSettings.Builder();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {//>=5.0
            builder.setScanMode(scanMode);
            builder.setReportDelay(reportDelayTime);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {//>=6.0
            builder.setCallbackType(callbackType);
            builder.setNumOfMatches(numOfMatches);
            builder.setMatchMode(matchMode);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {//>=8.0
            builder.setLegacy(isLegacy);  //不扫描低于4.2的蓝牙
            if (isLegacy==false&&BluetoothAdapter.getDefaultAdapter().isLeCodedPhySupported()) {//检查蓝牙芯片是否支持phy编码
                builder.setPhy(phy);  //设置物理层  只有使用了setLegacy(false)  才能设置此物理层函数
            }
        }
        return builder.build();
    }

    /**
     * 全局配置的扫描过滤器
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public List<ScanFilter> getGlobaltScanFilter() {
        List<ScanFilter> filters = new ArrayList<>();
        //配置过滤器
        if (uuids != null && uuids.length != 0) {//UUID过滤
            for (int i = 0; i < uuids.length; i++) {
                ScanFilter.Builder builder = new ScanFilter.Builder();
                builder.setServiceUuid(new ParcelUuid(uuids[i]));
                filters.add(builder.build());
            }
        } else {//名字和mac地址过滤
            ScanFilter.Builder builder = new ScanFilter.Builder();
            if (TextUtils.isEmpty(deviceAddress)) {
                builder.setDeviceAddress(deviceAddress);
            }
            if (TextUtils.isEmpty(deviceName)) {
                builder.setDeviceName(deviceName);
            }
            filters.add(builder.build());
        }
        return filters;
    }

    /**
     * 获取通过蓝牙名字扫描的过滤器
     * @param bleName
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public List<ScanFilter> getBleNameScanFilter(String bleName) {
        List<ScanFilter> filters = new ArrayList<>();

        if (uuids != null && uuids.length != 0) {//UUID过滤
            for (int i = 0; i < uuids.length; i++) {
                ScanFilter.Builder builder = new ScanFilter.Builder();
                builder.setServiceUuid(new ParcelUuid(uuids[i]));
                builder.setDeviceName(bleName);
                filters.add(builder.build());
            }
        }
        return filters;
    }

    //api21一下的过滤扫描 配置
    public UUID[] getGlobalOldScanFilter() {
        return uuids;
    }


    //region ================================== get ==================================
    public String getDeviceName() {
        return deviceName;
    }

    public String getDeviceAddress() {
        return deviceAddress;
    }

    public UUID[] getUuids() {
        return uuids;
    }

    public int getScanMode() {
        return scanMode;
    }

    public int getReportDelayTime() {
        return reportDelayTime;
    }

    public int getCallbackType() {
        return callbackType;
    }

    public int getNumOfMatches() {
        return numOfMatches;
    }

    public int getMatchMode() {
        return matchMode;
    }

    public boolean isLegacy() {
        return isLegacy;
    }

    public int getPhy() {
        return phy;
    }

    public int getScanTimeOut() {
        return scanTimeOut;
    }

    public int getConnectTimeOut() {
        return connectTimeOut;
    }

    public int getScanOrconnectGapTime() {
        return scanOrconnectGapTime;
    }

    public int getErrorGapTime() {
        return errorGapTime;
    }

    public int getConnectedDelay() {
        return connectedDelay;
    }

    public boolean getAutoConnect() {
        return isAutoConnect;
    }

    public int getConnectMaxCount() {
        return connectMaxCount;
    }

    //endregion =============================== get ==================================

    public static final class Builder{
        //5.0以上
        private int scanMode = SCAN_MODE_LOW_LATENCY; //扫描强度
        //设置扫描结果回调延时，
        // =0 或者不设置 每找到一个设备都会回调ScanCallback$onScanResult
        // >0会分批次返回扫描结果会回调在ScanCallback$onBatchScanResults中
        private int reportDelayTime=0;
        //6.0以上
        private int callbackType = CALLBACK_TYPE_ALL_MATCHES;//扫描结果的回调类型
        private int numOfMatches = MATCH_NUM_MAX_ADVERTISEMENT;//过滤器匹配尽量多的广播数量
        private int matchMode =MATCH_MODE_AGGRESSIVE; //匹配模式
        //8.0以上
        private boolean isLegacy ; //是否扫描4.2以下的蓝牙设备
        private int phy =PHY_LE_1M; //设置物理层  只有使用了setLegacy(false)  才能设置此物理层函数

        //扫描过滤
        private String deviceName; //设备名字
        private String deviceAddress;//设备地址
        private UUID[] uuids; //根据UUID过滤

        //其它设置
        private int scanTimeOut =5000; //扫描时间 默认5秒
        private int connectTimeOut =8000; //连接超时时间
        private int scanOrconnectGapTime =200; //扫描停止后连接的间隔时间
        private int errorGapTime = 2000; //发生连接错误时重连的间隔时间 只要针对133异常
        private int connectedDelay = 500; //连接成功后的延时时间
        private boolean isAutoConnect; //false直接连接到蓝牙，true 蓝牙可用时自动连接
        private int connectMaxCount = 3; //连接最大次数

        public Builder setDeviceName(String deviceName) {
            this.deviceName = deviceName;
            return this;
        }

        public Builder setDeviceAddress(String deviceAddress) {
            this.deviceAddress = deviceAddress;
            return this;
        }

        public Builder setUuids(UUID[] uuids) {
            this.uuids = uuids;
            return this;
        }

        public Builder setScanMode(int scanMode) {
            this.scanMode = scanMode;
            return this;
        }

        public Builder setReportDelayTime(int reportDelayTime) {
            this.reportDelayTime = reportDelayTime;
            return this;
        }

        public Builder setCallbackType(int callbackType) {
            this.callbackType = callbackType;
            return this;
        }

        public Builder setNumOfMatches(int numOfMatches) {
            this.numOfMatches = numOfMatches;
            return this;
        }

        public Builder setMatchMode(int matchMode) {
            this.matchMode = matchMode;
            return this;
        }

        public Builder setLegacy(boolean legacy) {
            isLegacy = legacy;
            return this;
        }

        public Builder setPhy(int phy) {
            this.phy = phy;
            return this;
        }

        public Builder setScanTimeOut(int scanTimeOut) {
            this.scanTimeOut = scanTimeOut;
            return this;
        }

        public Builder setConnectTimeOut(int connectTimeOut) {
            this.connectTimeOut = connectTimeOut;
            return this;
        }

        public Builder setScanOrconnectGapTime(int scanOrconnectGapTime) {
            this.scanOrconnectGapTime = scanOrconnectGapTime;
            return this;
        }

        public Builder setErrorGapTime(int errorGapTime) {
            this.errorGapTime = errorGapTime;
            return this;
        }

        public Builder setConnectedDelay(int connectedDelay) {
            this.connectedDelay = connectedDelay;
            return this;
        }

        public Builder setAutoConnect(boolean autoConnect) {
            isAutoConnect = autoConnect;
            return this;
        }

        public Builder setConnectMaxCount(int connectMaxCount) {
            this.connectMaxCount = connectMaxCount;
            return this;
        }

        public BleParamConfig build(){
           return new BleParamConfig(
                   deviceName,
                   deviceAddress,
                   uuids,
                   scanMode,
                   reportDelayTime,
                   callbackType,
                   numOfMatches,
                   matchMode,
                   isLegacy,
                   phy,
                   scanTimeOut,
                   connectTimeOut,
                   scanOrconnectGapTime,
                   errorGapTime,
                   connectedDelay,
                   isAutoConnect,
                   connectMaxCount);
        }
    }

}
