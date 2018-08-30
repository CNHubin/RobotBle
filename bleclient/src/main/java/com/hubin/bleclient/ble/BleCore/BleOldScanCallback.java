package com.hubin.bleclient.ble.BleCore;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.os.Message;
import android.text.TextUtils;

import java.lang.ref.WeakReference;

/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.BleCore
 * @文件名: BleOldScanCallback
 * @创建者: 胡英姿
 * @创建时间: 2018/7/10 11:32
 * @描述： api<21 的版本使用的扫描回调接口
 */
public class BleOldScanCallback implements BluetoothAdapter.LeScanCallback {
    private WeakReference<BleCore> mBleCoreWeak;
    private String mFilterName; //根据名称过滤，如果为Null 则不过滤

    public BleOldScanCallback(BleCore bleCore) {
        mBleCoreWeak = new WeakReference<BleCore>(bleCore);
    }

    public void setFilterName(String filterName) {
        mFilterName = filterName;
    }

    @Override
    public void onLeScan(BluetoothDevice device, int rssi, byte[] scanRecord) {
        if (device == null || TextUtils.isEmpty(device.getAddress())) {
            return;
        }
        if (!TextUtils.isEmpty(mFilterName)) {
            if (!device.getName().equals(mFilterName)) {
                return;
            }
        }

        Message scanMessage = mBleCoreWeak.get().getScanMessage();
        scanMessage.what=BleCore.MSG_SCAN_OLD_DEVICE;
        scanMessage.obj =new BleDevice(device,rssi,scanRecord, System.nanoTime());
        mBleCoreWeak.get().sendScanMsg(scanMessage);
    }
}
