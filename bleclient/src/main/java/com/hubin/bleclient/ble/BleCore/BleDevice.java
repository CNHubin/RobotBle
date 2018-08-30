package com.hubin.bleclient.ble.BleCore;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;
import android.os.Build;
import android.os.Parcel;
import android.os.ParcelUuid;
import android.os.Parcelable;
import android.text.TextUtils;

import com.hubin.bleclient.ble.bleConfig.BLEConfig;

import java.util.ArrayList;
import java.util.List;


/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.BleCore
 * @文件名: BleDevice
 * @创建者: 胡英姿
 * @创建时间: 2018/7/9 20:35
 * @描述： 自定义向下兼容的BleDevice 可以根据需要增加字段
 */
public class BleDevice  implements Parcelable {

    private BluetoothDevice mDevice;
    private byte[] mScanRecordBytes;//扫描记录的原始字节
    private int mRssi;
    private long mTimestampNanos;

    //api>21
    private int mPrimaryPhy;
    private int mSecondaryPhy;
    private int mAdvertisingSid;
    private int mTxPower;
    private int mPeriodicAdvertisingInterval;

    //自定义
    private String deviceName;
    private String address;
    private List<ParcelUuid> uuid;
    private byte[] mManufacturerSpecificData;//制造商数据

    public BleDevice(BluetoothDevice device, int rssi, byte[] scanRecord, long timestampNanos) {
        mDevice = device;
        mScanRecordBytes = scanRecord;
        mRssi = rssi;
        mTimestampNanos = timestampNanos;
        deviceName=device.getName();
        address=device.getAddress();

        uuid = new ArrayList<>();
        for (ParcelUuid parcelUuid : device.getUuids()) {
            uuid.add(parcelUuid);
        }

        //解析广播数据 获取制造商信息
        BleParser mBleParser = new BleParser();
        mBleParser.parseData(scanRecord);
        mManufacturerSpecificData = mBleParser.getManufacturerDataBytes(BLEConfig.BLE_MANUFACTURER_ID);
        if (TextUtils.isEmpty(deviceName)) {
            deviceName = mBleParser.getLocalName();
        }
        if (uuid == null || uuid.size() == 0) {
            uuid = mBleParser.getServiceUuids();
        }
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public BleDevice(ScanResult scanResult) {
        mDevice = scanResult.getDevice();
        mScanRecordBytes=scanResult.getScanRecord().getBytes();
        mRssi = scanResult.getRssi();
        mTimestampNanos=scanResult.getTimestampNanos();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mPrimaryPhy = scanResult.getPrimaryPhy();
            mSecondaryPhy=scanResult.getSecondaryPhy();
            mAdvertisingSid=scanResult.getAdvertisingSid();
            mTxPower=scanResult.getTxPower();
            mPeriodicAdvertisingInterval=scanResult.getPeriodicAdvertisingInterval();
        }

        deviceName =scanResult.getScanRecord().getDeviceName();
        if (TextUtils.isEmpty(deviceName)) {
            deviceName=mDevice.getName();
        }

        address = mDevice.getAddress();
        uuid = scanResult.getScanRecord().getServiceUuids();
        mManufacturerSpecificData = scanResult.getScanRecord().getManufacturerSpecificData(BLEConfig.BLE_MANUFACTURER_ID);
    }

    //region ================================== Parcelable ==================================
    protected BleDevice(Parcel in) {
        mDevice = in.readParcelable(BluetoothDevice.class.getClassLoader());
        mScanRecordBytes = in.createByteArray();
        mRssi = in.readInt();
        mTimestampNanos = in.readLong();
        //>21
        mPrimaryPhy = in.readInt();
        mSecondaryPhy = in.readInt();
        mAdvertisingSid = in.readInt();
        mTxPower = in.readInt();
        mPeriodicAdvertisingInterval = in.readInt();
        //自定义
        deviceName=in.readString();
        address=in.readString();
        uuid=in.readArrayList(ParcelUuid.class.getClassLoader());
        mManufacturerSpecificData =in.createByteArray();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(mDevice, flags);
        dest.writeByteArray(mScanRecordBytes);
        dest.writeInt(mRssi);
        dest.writeLong(mTimestampNanos);

        dest.writeInt(mPrimaryPhy);
        dest.writeInt(mSecondaryPhy);
        dest.writeInt(mAdvertisingSid);
        dest.writeInt(mTxPower);
        dest.writeInt(mPeriodicAdvertisingInterval);

        dest.writeString(deviceName);
        dest.writeString(address);
        dest.writeList(uuid);
        dest.writeByteArray(mManufacturerSpecificData);

    }
    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<BleDevice> CREATOR = new Creator<BleDevice>() {
        @Override
        public BleDevice createFromParcel(Parcel in) {
            return new BleDevice(in);
        }

        @Override
        public BleDevice[] newArray(int size) {
            return new BleDevice[size];
        }
    };
    //endregion =============================== Parcelable ==================================


    public BluetoothDevice getDevice() {
        return mDevice;
    }

    public byte[] getScanRecordBytes() {
        return mScanRecordBytes;
    }

    public int getRssi() {
        return mRssi;
    }

    public long getTimestampNanos() {
        return mTimestampNanos;
    }

    public int getPrimaryPhy() {
        return mPrimaryPhy;
    }

    public int getSecondaryPhy() {
        return mSecondaryPhy;
    }

    public int getAdvertisingSid() {
        return mAdvertisingSid;
    }

    public int getTxPower() {
        return mTxPower;
    }

    public int getPeriodicAdvertisingInterval() {
        return mPeriodicAdvertisingInterval;
    }

    public String getName() {
        return deviceName;
    }

    public String getAddress() {
        return address;
    }

    public List<ParcelUuid> getUuidList() {
        return uuid;
    }

    public byte[] getManufacturerSpecificData() {
        return mManufacturerSpecificData;
    }

    @Override
    public String toString() {
        return address;
    }

}
