package com.hubin.bleclient.ble.blecontrol;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import com.hubin.bleclient.ble.BleCore.BleDevice;
import com.hubin.bleclient.ble.bleConfig.BLEConfig;
import com.hubin.bleclient.ble.bleConfig.FileSuffix;
import com.hubin.bleclient.ble.bleConfig.GetParam;
import com.hubin.bleclient.ble.bleInterface.BleConnectListener;
import com.hubin.bleclient.ble.bleInterface.BleMtuListener;
import com.hubin.bleclient.ble.bleInterface.BleNotifyListener;
import com.hubin.bleclient.ble.bleInterface.BleReadListener;
import com.hubin.bleclient.ble.bleInterface.BleScanListener;
import com.hubin.bleclient.ble.bleInterface.BleWriteListener;
import com.hubin.bleclient.utils.FileUtils;
import com.hubin.bleclient.utils.LogUtils;

import java.io.File;
import java.util.List;

/**
 *  @项目名：  AiiageSteamAndroid
 *  @包名：    com.aiiage.steam.mobile.ble.blecontrol
 *  @文件名:   BleClientHelper
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/13 17:44
 *  @描述：    对BleClientManager中蓝牙操作的二次封装，方便使用。
 */
public class BleClientHelper {

    public static BleDevice getDevice() {
      return BleClientManager.getInstance().getBleDevice();
    }

    public static List<BluetoothDevice> getDeviceAll() {
        return BleClientManager.getInstance().getDeviceAll();
    }

    //发起扫描
    public static void scan(@NonNull final BleScanListener bleScanListener) {
        LogUtils.d("scan  D : 发起扫描！");
        BleClientManager.getInstance().scan(bleScanListener);
    }
    //中止扫描
    public static void cancelScan(){
        LogUtils.d("cancelScan  D : 断开扫描！");
        BleClientManager.getInstance().cancelScan();
    }

    //连接设备  如果连接失败内部可设定默认重连次数
    public static void connect(BleDevice bleDevice, BleConnectListener connectListener){
        LogUtils.d("connect  D : 连接设备！");
        BleClientManager.getInstance().connect(bleDevice,connectListener);
    }

    //检查蓝牙状态并自动连接缓存的设备
    public static void autoConnect(BleConnectListener bleConnectListener) {
        LogUtils.d("autoConnect  D : 检查蓝牙连接状态！");
        if (!BleClientManager.getInstance().checkBleConnected()) {//重连
            BleClientManager.getInstance().autoConnect(bleConnectListener);
        } else {
            bleConnectListener.onConnectSuccess(BleClientManager.getInstance().getBleDevice());
        }
    }

    //检查蓝牙连接状态
    public static boolean checkBleConnected(){
        return BleClientManager.getInstance().checkBleConnected();
    }

    //断开连接
    public static void disconnected(){
        LogUtils.d("disconnected  D : 断开当前连接！");
        BleClientManager.getInstance().disconnected();
    }

    //关闭Gatt
    public static void closeBle() {
        BleClientManager.getInstance().closeBle();
    }
    //断开GATT 并彻底释放
    public static void destroy(){
        LogUtils.d("destroy  D : 断开gatt!");
        BleClientManager.getInstance().destroy();
    }

    //设置 Notify
    public static void setNotify(BleNotifyListener bleNotifyListener) {
        BleClientManager.getInstance().openNotify(bleNotifyListener);
    }

    //停止notify
    public static void stopNotify() {
        BleClientManager.getInstance().stopNotify();
    }

    /**
     * 读取数据Ble蓝牙服务端
     * @param getParam 必须为GetParam枚举所标记的类型
     */
    public static void readString(GetParam getParam, BleReadListener bleReadListener) {
        switch (getParam) {
            case TOKEN:
                LogUtils.d("readString  D : 读取token！");
                BleClientManager.getInstance().readCharacteristic(BLEConfig.UUID_CHARACTERISTIC_TOKEN, bleReadListener);
                break;
            case PETID:
                LogUtils.d("readString  D : 读取petid！");
                BleClientManager.getInstance().readCharacteristic(BLEConfig.UUID_CHARACTERISTIC_PETID, bleReadListener);
                break;
            case PETID_TOKEN:
                LogUtils.d("readString  D : 读取token和petid！");
                BleClientManager.getInstance().readCharacteristic(BLEConfig.UUID_CHARACTERISTIC_PETID_TOKEN, bleReadListener);
                break;
            case REFRESH_TOKEN:
                LogUtils.d("readString  D : 刷新token！");
                BleClientManager.getInstance().readCharacteristic(BLEConfig.UUID_CHARACTERISTIC_REFRESH_TOKEN, bleReadListener);
                break;
            default:
                LogUtils.e("error 404: 您访问的字段不存在！");
                break;
        }
    }

    // 发送String数据到Ble服务端
    public static void sendString(String msg, BleWriteListener bleWriteListener) {
        LogUtils.d("sendString  D : 发送String:"+msg);
        BleClientManager.getInstance().sendCode(msg, bleWriteListener);
    }

    /**
     * 发送文件到Ble服务端
     * @param file 文件路径
     * @param fileSuffix 文件后缀
     */
    public static void sendFile(File file, FileSuffix fileSuffix, BleWriteListener bleWriteListener) {
        LogUtils.d("sendFile  D : 发送"+fileSuffix+"文件");
        BleClientManager.getInstance().sendCodeByBLE(fileSuffix, FileUtils.readFile2BytesByChannel(file), bleWriteListener);
    }

    public static void setMtu(int mtu, BleMtuListener bleMtuListener) {
        BleClientManager.getInstance().setMtu( mtu, bleMtuListener);
    }
}
