package com.hubin.bleclient.ble.blecontrol;

import android.bluetooth.BluetoothDevice;
import android.support.annotation.NonNull;

import com.hubin.bleclient.base.BleClientApplication;
import com.hubin.bleclient.ble.BleCore.BleCore;
import com.hubin.bleclient.ble.BleCore.BleDevice;
import com.hubin.bleclient.ble.BleCore.BleParamConfig;
import com.hubin.bleclient.ble.bleConfig.BLEConfig;
import com.hubin.bleclient.ble.bleConfig.FileSuffix;
import com.hubin.bleclient.ble.bleInterface.BleConnectListener;
import com.hubin.bleclient.ble.bleInterface.BleMtuListener;
import com.hubin.bleclient.ble.bleInterface.BleNotifyListener;
import com.hubin.bleclient.ble.bleInterface.BleReadListener;
import com.hubin.bleclient.ble.bleInterface.BleScanListener;
import com.hubin.bleclient.ble.bleInterface.BleWriteListener;

import java.util.List;
import java.util.UUID;

import static com.hubin.bleclient.ble.bleConfig.FileSuffix.STRING;


/**
 *  @项目名：  AiiageSteamAndroid
 *  @包名：    com.aiiage.steam.mobile.ble.blecontrol
 *  @文件名:   BleClientManager
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/13 17:44
 *  @描述：    蓝牙管理器
 */
public class BleClientManager {

    private static BleClientManager mInstance;
    private static BleCore mBleCore;

    //单例
    public static BleClientManager getInstance() {
        if (mInstance == null) {
            synchronized (BleClientManager.class) {
                if (mInstance == null) {
                    mInstance = new BleClientManager();

                    //配置蓝牙
                    BleParamConfig mBleParamConfig = new BleParamConfig.Builder()
                            .setUuids(new UUID[]{BLEConfig.UUID_BLE_SERVICE})
                            .setScanTimeOut(3000) //扫描时间 默认5秒
                            .setConnectTimeOut(8000)// 连接超时时间
                            .setScanOrconnectGapTime(200)//扫描停止后连接的间隔时间
                            .setErrorGapTime(1000)//发生连接错误时重连的间隔时间 只要针对133异常
                            .setConnectedDelay(500)//连接成功后的延时时间
                            .setConnectMaxCount(3)//连接最大次数
                            .setAutoConnect(false)//false直接连接到蓝牙，true 蓝牙可用时自动连接
                            .setMatchMode(BleParamConfig.MATCH_MODE_STICKY)//扫描模式，只扫描信号强的设备
                            .build();
                    mBleCore = new BleCore(mBleParamConfig, BleClientApplication.getContext(),true);//使用消息队列进行读写 高并发时会依次执行
                }
            }
        }
        return mInstance;
    }

    public BleDevice getBleDevice() {
       return mBleCore.getDevice();
    }

    public List<BluetoothDevice> getDeviceAll() {
        return mBleCore.getDeviceAll();
    }

    //region ================================== BLE扫描 ==================================
    //扫描
    public void scan(@NonNull final BleScanListener bleScanListener) {
        mBleCore.scan(null,bleScanListener);
    }

    //中止扫描
    public void cancelScan() {
        mBleCore.stopScan();
    }


    //endregion =============================== BLE扫描 ==================================


    //region ================================== BLE连接 ==================================
    // 检查蓝牙连接状态
    public boolean checkBleConnected() {
        return mBleCore.checkConnected();
    }

    /**
     * 连接1:根据设备连接
     * @param bleDevice
     */
    public void connect(BleDevice bleDevice, BleConnectListener connectListener) {
        mBleCore.connect(bleDevice, connectListener);
    }


    public void autoConnect(BleConnectListener connectListener) {
        mBleCore.autoConnect(connectListener);
    }


    /**
     * 手动断开连接
     */
    public void disconnected() {
        mBleCore.disconnect();
    }

    public void closeBle() {
        mBleCore.closeBle();
    }

    // 断开连接和释放回调句柄
    public void destroy() {
        mBleCore.destroy();
    }
    //endregion =============================== BLE连接 ==================================


    //region ================================== BLE发送数据 ==================================

    /**
     * 发送字符串内容，命令等
     *
     * @param content 字符串内容
     */
    public void sendCode(String content, BleWriteListener bleWriteListener) {
        sendCodeByBLE(STRING, content.getBytes(), bleWriteListener);
    }

    /**
     * @param fileSuffix       文件类型
     * @param data          文件字节码
     * @param bleWriteListener
     */
    public void sendCodeByBLE(FileSuffix fileSuffix, final byte[] data, BleWriteListener bleWriteListener) {
        /*mBleCore.writeCharacteristic(
                BLEConfig.UUID_BLE_SERVICE,
                BLEConfig.UUID_CHARACTERISTIC_WRITE,
                fileSuffix,
                data,
                bleWriteListener);*/

        //使用消息队列 进行读写
        mBleCore.sendWriteMsg(
                BLEConfig.UUID_BLE_SERVICE,
                BLEConfig.UUID_CHARACTERISTIC_WRITE,
                fileSuffix,
                data,
                bleWriteListener);
    }


    //endregion =============================== BLE发送数据 ==================================


    //region ================================== BLE接收数据 ==================================
    /**
     * 通过 ble 读String类型数据
     * @param characteristicUUID
     * @param bleReadListener
     */
    public void readCharacteristic(UUID characteristicUUID, final BleReadListener bleReadListener) {
//        mBleCore.readCharacteristic(BLEConfig.UUID_BLE_SERVICE,characteristicUUID,bleReadListener);
        mBleCore.sendReadMsg(
                BLEConfig.UUID_BLE_SERVICE,
                characteristicUUID,
                bleReadListener);
    }

    //endregion =============================== BLE接收数据 ==================================


    //region ================================== BLE通知 ==================================

    public void setMtu(int mtu, BleMtuListener bleMtuListener) {
        mBleCore.setMtu(mtu,bleMtuListener);
    }

    /**
     * 发起Notify
     * @param bleNotifyListener
     */
    public void openNotify(final BleNotifyListener bleNotifyListener) {
        mBleCore.setNotify(BLEConfig.UUID_BLE_SERVICE,
                BLEConfig.UUID_CHARACTERISTIC_NOTIFY,true,bleNotifyListener);
    }

    //停止Notify
    public void stopNotify() {
        mBleCore.setNotify(BLEConfig.UUID_BLE_SERVICE,
                BLEConfig.UUID_CHARACTERISTIC_NOTIFY,false,null);
    }
    //endregion =============================== BLE通知 ==================================
}
