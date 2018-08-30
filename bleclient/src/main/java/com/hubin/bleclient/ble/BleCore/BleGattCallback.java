package com.hubin.bleclient.ble.BleCore;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothProfile;
import android.text.TextUtils;

import com.hubin.bleclient.ble.bleConfig.BLEConfig;
import com.hubin.bleclient.ble.bleException.BleException;
import com.hubin.bleclient.ble.bleException.CharacteristicReadException;
import com.hubin.bleclient.ble.bleException.ConnectFialException;
import com.hubin.bleclient.utils.ConvertUtils;
import com.hubin.bleclient.utils.LogUtils;
import com.hubin.bleclient.utils.ThreadUtils;

import java.lang.ref.WeakReference;
import java.util.Arrays;

/**
 * @项目名： AiiageSteamAndroid
 * @包名： com.aiiage.steam.mobile.ble.BleCore
 * @文件名: BleGattCallback
 * @创建者: 胡英姿
 * @创建时间: 2018/7/3 17:40
 * @描述： BLE 连接  读写等各种 gatt协议的回调
 */
public class BleGattCallback extends BluetoothGattCallback {
    private WeakReference<BleCore> mBleCoreWeak;
    private int mtu = BleCore.MTU_DEFAULT;

    public int getMtu() {//获取当前mtu值
        return mtu;
    }
    public BleGattCallback(BleCore bleCore) {
        mBleCoreWeak = new WeakReference<BleCore>(bleCore);
    }
    /**
     * 连接时失败的处理
     * @param gatt
     * @param status
     */
    private void connectFail(BluetoothGatt gatt,int status) {
        if (mBleCoreWeak.get().isMaxCount()) {
            mBleCoreWeak.get().setConnectFailCallback(gatt.getDevice(),new ConnectFialException(status));
            LogUtils.e("onConnectionStateChange E : 连接失败：status=" + status);
        } else {
            LogUtils.e("onConnectionStateChange E : 连接失败发起重连：status=" + status);
            ThreadUtils.mainHandler.post(()->{
                mBleCoreWeak.get().autoConnect();
            });
        }
    }

    //region ================================== 连接,发现服务 ==================================

    //连接状态发生改变
    @Override
    public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
        mBleCoreWeak.get().removeMsg(BleCore.MSG_CONNECT_TIME_OVER);//移除连接计时器
        mtu = BleCore.MTU_DEFAULT;//恢复mtu值
        switch (newState) {
            case BluetoothProfile.STATE_CONNECTED: //连接成功 连接成功后最好延时500毫秒左右再进行其它操作
                LogUtils.d("onConnectionStateChange  D : 连接Gatt成功：status="+status);
                if (gatt.getDevice().getBondState() != BluetoothDevice.BOND_BONDING){
                    if (gatt != null) {
                        ThreadUtils.mainHandler.postDelayed(()->{
                            if (!gatt.discoverServices()) { //启动服务发现
                                LogUtils.w("onConnectionStateChange  W : 发现服务操作失败!");
                                mBleCoreWeak.get().closeBle();//释放
                                connectFail(gatt,status);
                            }
                        },500);
                    }else {
                        LogUtils.e("onConnectionStateChange E : GATT==null!");
                    }
                } else {
                    LogUtils.e("onConnectionStateChange E : 设备处于绑定状态！");
                }

                break;
            case BluetoothProfile.STATE_DISCONNECTED://连接失败,连接中断
                mBleCoreWeak.get().closeBle();//释放
                //1.正在连接的时候---连接失败
                if (mBleCoreWeak.get().getConnectionState() == BleCore.STATE_CONNECTING) {//是否连接中
                    connectFail(gatt,status);
                }
                //2.已经连接上的时候---连接中断
                if (mBleCoreWeak.get().getConnectionState() == BleCore.STATE_CONNECTED) {//是否已连接
                   mBleCoreWeak.get().setDisconnectedCallback(status);//回调
                    LogUtils.e("onConnectionStateChange E : 连接中断，status="+status);
                }
                break;
            default:
                LogUtils.e("onConnectionStateChange E : 其它连接状态： status="+status);
                break;
        }
    }

    //服务发现
    @Override
    public void onServicesDiscovered(BluetoothGatt gatt, int status) {
        if (status == BluetoothGatt.GATT_SUCCESS) {
            LogUtils.d("onServicesDiscovered  D : 服务发现成功 "+gatt.getDevice().getAddress());
            mBleCoreWeak.get().setConnectSuccessCallback(gatt.getDevice());
        } else {
            LogUtils.e("onServicesDiscovered E : 服务发现失败！status="+status);
            mBleCoreWeak.get().closeBle();//释放
            connectFail(gatt,status);//重连
        }
    }

    //endregion =============================== 连接,发现服务 ==================================

    //region ================================== 读写特征 ==================================
    //特征发生变化
    @Override
    public void onCharacteristicChanged(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
        LogUtils.d("onCharacteristicChanged  D : 特征发生变化:"+new String(characteristic.getValue())+" len="+characteristic.getValue().length);
        mBleCoreWeak.get().setNotifyReceiveMsgCallback(characteristic.getValue());
    }

    //读取特征
    @Override
    public void onCharacteristicRead(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                     int status) {
        LogUtils.d("onCharacteristicRead  D : 读到特征,status="+status+" "+new String(characteristic.getValue()));

        String text = new String(characteristic.getValue());
        if (TextUtils.isEmpty(text)||text.contains(BLEConfig.BLE_NULLPOINTER)) {
            LogUtils.e("onReadSuccess,but！ text=" + text);
            mBleCoreWeak.get().setReadFailCallback(new CharacteristicReadException(BleException.ERROR_CODE_READ_EMPTY_DATA));
            return;
        }
        if (text.endsWith(BLEConfig.BLUETOOTH_DATA_END)) {
            text = text.substring(0, text.length() - BLEConfig.BLUETOOTH_DATA_END.length());
        }
        LogUtils.d("onReadSuccess  D : 读到数据:"+text);
        mBleCoreWeak.get().setReadSuccessCallback(characteristic.getValue());
    }

    //写特征回调
    @Override
    public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                      int status) {
        LogUtils.d("onCharacteristicWrite  D : 写特征成功,"+ ConvertUtils.bytes2HexString(characteristic.getValue())+" len="+characteristic.getValue().length+" status="+status);
        if ( mBleCoreWeak.get().isPollWrite()) {
            mBleCoreWeak.get().pollWriteCharacteristic();//
        } else {
            mBleCoreWeak.get().setWriteSuccessCallback();
        }
    }

    //endregion =============================== 读写特征 ==================================

    //region ================================== 读写描述 ==================================
    //读取描述
    @Override
    public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        LogUtils.d("onDescriptorRead  D : 读描述：status="+status);

    }

    //写描述
    @Override
    public void onDescriptorWrite(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
        LogUtils.d("onDescriptorWrite  D : 写描述：status="+status+" 描述："+ConvertUtils.bytes2HexString(descriptor.getValue()));
        //订阅成功响应 0100
        //取消订阅响应 0000
        if (Arrays.equals(descriptor.getValue(), BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
            //启用notify
            LogUtils.d("onDescriptorWrite  D : 启用Notify成功！");
            mBleCoreWeak.get().setNotifySuccessCallback();
        } else if (Arrays.equals(descriptor.getValue(), BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)){
            //取消notify
            LogUtils.d("onDescriptorWrite  D : 取消Notify成功！");
            mBleCoreWeak.get().setStopNotifyCallback();
        }
    }

    //endregion =============================== 读写描述 ==================================

    //完成可靠的写入事务时调用回调。
    @Override
    public void onReliableWriteCompleted(BluetoothGatt gatt, int status) {
        LogUtils.d("onReliableWriteCompleted  D 完成可靠的写入: status="+status);

    }

    //远程设备的RSSI值
    @Override
    public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {
        LogUtils.d("onReadRemoteRssi  D : Rssi="+rssi+" status="+status);

    }

    //region ================================== Mtu,Phy更改 ==================================
    //mtu更改
    @Override
    public void onMtuChanged(BluetoothGatt gatt, int mtu, int status) {
        LogUtils.d("onMtuChanged  D mtu申请成功: mtu="+mtu+" status="+status);
        this.mtu = mtu;
        mBleCoreWeak.get().setMtuSuccessCallback(mtu);
    }

    //Phy 更改
    @Override
    public void onPhyUpdate(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        LogUtils.d("onPhyUpdate  D : txPhy="+txPhy+" rxPhy="+rxPhy+" status="+status);

    }

    //BluetoothGatt#readPhy 时回调
    @Override
    public void onPhyRead(BluetoothGatt gatt, int txPhy, int rxPhy, int status) {
        LogUtils.d("onPhyRead  D : txPhy"+txPhy+" rxPhy="+rxPhy+" status="+status);

    }

    //endregion =============================== Mtu,Phy更改 ==================================

}
