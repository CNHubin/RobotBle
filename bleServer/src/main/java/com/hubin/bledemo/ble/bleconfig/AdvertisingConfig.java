package com.hubin.bledemo.ble.bleconfig;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.le.AdvertiseData;
import android.bluetooth.le.AdvertiseSettings;
import android.os.Build;
import android.os.ParcelUuid;
import android.support.annotation.RequiresApi;

import com.hubin.bledemo.utils.BytesUtils;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.UUID;

/*
 *  @项目名：  BleDemo
 *  @包名：    com.hubin.bledemo.ble.bleconfig
 *  @文件名:   AdvertisingConfig
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/19 10:23
 *  @描述：    Ble  Advertising 配置
 */
public class AdvertisingConfig {
    public static final String mac = "A1:B2:C3:D4:E5:F6";
    /**
     * 配置蓝牙
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static AdvertiseSettings getAdvertiseSettings() {
        AdvertiseSettings settings = new AdvertiseSettings.Builder()
                .setAdvertiseMode(AdvertiseSettings.ADVERTISE_MODE_BALANCED)//设置功耗
                .setConnectable(true) //设置可连接
                .setTimeout(0) //设置超时时间
                .setTxPowerLevel(AdvertiseSettings.ADVERTISE_TX_POWER_MEDIUM) //设置Tx功耗
                .build();
        return settings;
    }

    /**
     * 配置蓝牙发送设备
     * @return
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static AdvertiseData getAdvertiseData(BluetoothAdapter bluetoothAdapter) {
        AdvertiseData data = new AdvertiseData.Builder()
                .setIncludeDeviceName(true) //设置是否在广播的数据中包含设备名称
                .setIncludeTxPowerLevel(false) //数据包中是否包含功率级别

                //manufacturerId长度为2个字节，不能超过short的取值范围
//                .addManufacturerData(0x2018,new byte[]{0x01,0x02,0x03,0x04,0x05,0x06,0x07,0x08,0x09}) //添加制造商特定数据
                .addManufacturerData(2018, BytesUtils.macToBytes(mac)) //添加制造商特定数据

                .addServiceUuid(new ParcelUuid(BLEConstants.UUID_BLE_SERVICE)) //添加广播的uuid
                //添加不成功 不知原因
//                .addServiceData(new ParcelUuid(BLEConstants.UUID_BLE_SERVICE),new byte[]{(byte) 0x77})
                .build();
        return data;
    }



    /**
     * create AdvertiseDate for iBeacon
     */
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public static AdvertiseData createIBeaconAdvertiseData(UUID proximityUuid, short major, short minor, byte txPower) {

        String[] uuidstr = proximityUuid.toString().replaceAll("-", "").toLowerCase().split("");
        byte[] uuidBytes = new byte[16];
        for (int i = 1, x = 0; i < uuidstr.length; x++) {
            uuidBytes[x] = (byte) ((Integer.parseInt(uuidstr[i++], 16) << 4) | Integer.parseInt(uuidstr[i++], 16));
        }
        byte[] majorBytes = {(byte) (major >> 8), (byte) (major & 0xff)};
        byte[] minorBytes = {(byte) (minor >> 8), (byte) (minor & 0xff)};
        byte[] mPowerBytes = {txPower};
        byte[] manufacturerData = new byte[0x17];
        byte[] flagibeacon = {0x02, 0x15};

        System.arraycopy(flagibeacon, 0x0, manufacturerData, 0x0, 0x2);
        System.arraycopy(uuidBytes, 0x0, manufacturerData, 0x2, 0x10);
        System.arraycopy(majorBytes, 0x0, manufacturerData, 0x12, 0x2);
        System.arraycopy(minorBytes, 0x0, manufacturerData, 0x14, 0x2);
        System.arraycopy(mPowerBytes, 0x0, manufacturerData, 0x16, 0x1);

        AdvertiseData.Builder builder = new AdvertiseData.Builder();
        builder.addManufacturerData(0x004c, manufacturerData);

        AdvertiseData adv = builder.build();
        return adv;
    }

    //扫描响应数据
    public static byte[] createScanAdvertiseData(short major, short minor, byte txPower) {
        byte[] serverData = new byte[5];
        ByteBuffer bb = ByteBuffer.wrap(serverData);
        bb.order(ByteOrder.BIG_ENDIAN);
        bb.putShort(major);
        bb.putShort(minor);
        bb.put(txPower);
        return serverData;
    }
}
