package com.hubin.bleclient.ble.BleCore;

import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.os.Message;
import android.support.annotation.RequiresApi;
import android.text.TextUtils;

import com.hubin.bleclient.ble.bleException.ScanFailedException;
import com.hubin.bleclient.utils.LogUtils;

import java.lang.ref.WeakReference;
import java.util.List;


/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.BleCore
 * @文件名: BleScanCallback
 * @创建者: 胡英姿
 * @创建时间: 2018/7/2 17:59
 * @描述： 蓝牙扫描结果的回调接口  api>21才可使用
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class BleScanCallback extends ScanCallback {
    private WeakReference<BleCore> mBleCoreWeak;

    public BleScanCallback(BleCore bleCore) {
        mBleCoreWeak = new WeakReference<BleCore>(bleCore);
    }

    @Override
    public void onScanResult(int callbackType, ScanResult result) {//找到设备
        if (result == null|| TextUtils.isEmpty(result.getDevice().getAddress())) {
            return;
        }
        LogUtils.i("onScanResult  D : 扫到设备："+result.getDevice()+" name="+result.getDevice().getName());
        Message scanMessage = mBleCoreWeak.get().getScanMessage();
        scanMessage.what=BleCore.MSG_SCAN_LOLLIPOP_DEVICE;
        scanMessage.obj =result;
        mBleCoreWeak.get().sendScanMsg(scanMessage);
    }

    @Override
    public void onBatchScanResults(List<ScanResult> results) {//分批次扫描时的回掉结果
        if (results == null || results.size() == 0) {
            return;
        }
        for (ScanResult result : results) {
            if (!TextUtils.isEmpty(result.getDevice().getAddress())) {
                Message scanMessage = mBleCoreWeak.get().getScanMessage();
                scanMessage.what=BleCore.MSG_SCAN_LOLLIPOP_DEVICE;
                scanMessage.obj =result;
                mBleCoreWeak.get().sendScanMsg(scanMessage);
                LogUtils.i("onBatchScanResults I : 分批回调，"+result.getDevice().getAddress());

            }
        }
    }

    @Override
    public void onScanFailed(int errorCode) { //扫描无法开始
        LogUtils.e("onScanFailed E : " + errorCode);
        mBleCoreWeak.get().setScanFailedCallback(new ScanFailedException(errorCode));
    }
}
