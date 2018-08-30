package com.hubin.bledemo.ble.bleconfig;

import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;


/*
 *  @项目名：  BleDemo
 *  @包名：    com.hubin.bledemo.ble.bleconfig
 *  @文件名:   GattConfig
 *  @创建者:   胡英姿
 *  @创建时间:  2018/6/19 10:35
 *  @描述：    Gatt协议配置
 */
public class GattConfig {

    //添加一个Gatt服务
    public static BluetoothGattService addGattService() {
        BluetoothGattService mGattService = new BluetoothGattService(BLEConstants.UUID_BLE_SERVICE,
                BluetoothGattService.SERVICE_TYPE_PRIMARY);

        //给服务添加特征
        addCharacteristicRead(mGattService); //读的特征
        addCharacteristicWrite(mGattService);//写的特征
        addCharacteristicToken(mGattService);//token的特征
        addCharacteristicPetid(mGattService);//Petid的特征
        addCharacteristicPetidorToken(mGattService);//用于同时读取Token和 Petid
        addCharacteristicRefreshToken(mGattService);//用于刷新token
        addCharacteristicNotify(mGattService);//Notify
        return mGattService;
    }

    //给服务添加一个 读的特征
    private static void addCharacteristicRead(BluetoothGattService gattService) {
        //添加指定UUID的可读characteristic
        BluetoothGattCharacteristic characteristicRead = new BluetoothGattCharacteristic(
                BLEConstants.UUID_CHARACTERISTIC_READ,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);
        //添加可读characteristic的descriptor,暂未用到
        BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(BLEConstants.UUID_DESCRIPTOR,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        characteristicRead.addDescriptor(descriptor);
        gattService.addCharacteristic(characteristicRead);
    }

    //给服务添加一个 写的特征
    private static void addCharacteristicWrite(BluetoothGattService gattService) {
        BluetoothGattCharacteristic characteristicWrite = new BluetoothGattCharacteristic(BLEConstants
                .UUID_CHARACTERISTIC_WRITE,
                BluetoothGattCharacteristic.PROPERTY_WRITE |
                        BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY |
                        BluetoothGattCharacteristic.PROPERTY_WRITE_NO_RESPONSE |
                        BluetoothGattCharacteristic.PROPERTY_WRITE,
                BluetoothGattCharacteristic.PERMISSION_WRITE);
        gattService.addCharacteristic(characteristicWrite);
    }

    //给服务添加一个 获取 token的特征
    private static void addCharacteristicToken(BluetoothGattService gattService) {
        BluetoothGattCharacteristic characteristicToken = new BluetoothGattCharacteristic(BLEConstants
                .UUID_CHARACTERISTIC_TOKEN,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);
        gattService.addCharacteristic(characteristicToken);
    }

    //给服务添加一个 获取 Petid的特征
    private static void addCharacteristicPetid(BluetoothGattService gattService) {
        BluetoothGattCharacteristic characteristicPetid = new BluetoothGattCharacteristic(
                BLEConstants.UUID_CHARACTERISTIC_PETID,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);
        gattService.addCharacteristic(characteristicPetid);
    }

    //给服务添加一个特征  用于同时读取Token和 Petid
    private static void addCharacteristicPetidorToken(BluetoothGattService gattService) {
        BluetoothGattCharacteristic characteristicPetidorToken = new BluetoothGattCharacteristic(
                BLEConstants.UUID_CHARACTERISTIC_PETID_TOKEN,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);
        gattService.addCharacteristic(characteristicPetidorToken);
    }

    //给服务添加一个特征用于刷新token
    private static void addCharacteristicRefreshToken(BluetoothGattService gattService) {

       /* BluetoothGattCharacteristic characteristicRefreshToken = new BluetoothGattCharacteristic(
                BLEConstants.UUID_CHARACTERISTIC_REFRESH_TOKEN,
                BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_WRITE |
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_WRITE);*/
        BluetoothGattCharacteristic characteristicRefreshToken = new BluetoothGattCharacteristic(
                BLEConstants.UUID_CHARACTERISTIC_REFRESH_TOKEN,
                BluetoothGattCharacteristic.PROPERTY_READ,
                BluetoothGattCharacteristic.PERMISSION_READ);
        //添加可读characteristic的descriptor,暂未用到
      /*  BluetoothGattDescriptor descriptor = new BluetoothGattDescriptor(BLEConstants.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR, BluetoothGattCharacteristic.PERMISSION_WRITE);
        characteristicRefreshToken.addDescriptor(descriptor);*/
        gattService.addCharacteristic(characteristicRefreshToken);
    }

    //给服务添加一个特征  用于 Notify
    private static void addCharacteristicNotify(BluetoothGattService gattService) {
        BluetoothGattCharacteristic notify = new BluetoothGattCharacteristic(BLEConstants
                .UUID_CHARACTERISTIC_NOTIFY,
                BluetoothGattCharacteristic.PROPERTY_READ |
                        BluetoothGattCharacteristic.PROPERTY_WRITE |
                        BluetoothGattCharacteristic.PROPERTY_NOTIFY,
                BluetoothGattCharacteristic.PERMISSION_READ |
                        BluetoothGattCharacteristic.PROPERTY_WRITE);
        //描述
        BluetoothGattDescriptor defs = new BluetoothGattDescriptor(BLEConstants
                .UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR,
                BluetoothGattDescriptor.PERMISSION_READ | BluetoothGattDescriptor.PERMISSION_WRITE);
//        notify.setValue("hello".getBytes());
        defs.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
        notify.addDescriptor(defs);
        gattService.addCharacteristic(notify);
        // mGattService.addService(mGattService);//为此服务添加包含服务
    }


}
