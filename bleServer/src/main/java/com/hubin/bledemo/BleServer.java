package com.hubin.bledemo;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.hubin.bledemo.base.BaseService;
import com.hubin.bledemo.ble.blecontrol.BleHelper;
import com.hubin.bledemo.ble.bleinterface.BleConnectListener;
import com.hubin.bledemo.ble.bleinterface.BleReadOrWriteListener;
import com.hubin.bledemo.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

public class BleServer extends BaseService {

private int count = 1;
    @Override
    public void onCreate() {
        super.onCreate();
        LogUtils.d("onCreate  D : 启动服务！");
        //启动BLE
        BleHelper.startBleServer();
        initListener();  //初始化监听器

        //注册EventBus
        EventBus.getDefault().register(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction("android.bluetooth.adapter.action.BLUETOOTH_ADDRESS_CHANGED");
//        filter.addAction(BluetoothAdapter.ACTION_BLUETOOTH_ADDRESS_CHANGED);
        filter.setPriority(IntentFilter.SYSTEM_HIGH_PRIORITY);
        registerReceiver(mReceiver, filter);
        LogUtils.d("onCreate  D : 注册蓝牙广播接收者！");

    }

    @Override
    public void onDestroy() {
        LogUtils.d("onDestroy  D : 关闭服务！");

        BleHelper.stopBleServer();
        EventBus.getDefault().unregister(this);//注销EventBus
        unregisterReceiver(mReceiver);
        super.onDestroy();
    }

    private void initListener() {
        BleHelper.setOnBleConnectListener(new BleConnectListener() {
            @Override
            public void onConnect(BluetoothDevice device) {
                sendToMainThread("连接："+device.getAddress());
            }

            @Override
            public void onDisconnect(BluetoothDevice device) {
                sendToMainThread("断开："+device.getAddress());
            }
        });

        BleHelper.setOnReadOrWriteListener(new BleReadOrWriteListener() {

            //收到纯字符串指令
            @Override
            public void onStringCmd(String msg) {
                LogUtils.d("doBluetoothCmd  D : 收到字符串：" + msg);
                sendToMainThread("收到消息："+msg+" len="+msg.getBytes().length);
            }

            /**
             * 接收文件完成
             *
             * @param total 文件大小
             * @param path  存储路径
             */
            @Override
            public void onReceiveSuccess(String total, String path) {
                sendToMainThread("收到文件："+total+" path:"+path);
            }

            @Override
            public void onCharacteristicReadRequest(BluetoothDevice device, BluetoothGattCharacteristic characteristic, int offset) {

            }

            @Override
            public void onCharacteristicWriteRequest(BluetoothDevice device, BluetoothGattCharacteristic characteristic, int offset) {

            }

            @Override
            public void onDescriptorReadRequest(BluetoothDevice device, BluetoothGattDescriptor descriptor, int offset) {
                sendToMainThread("读取描述响应：GATT_SUCCESS");
            }

            @Override
            public void onDescriptorWriteRequest(BluetoothDevice device, BluetoothGattDescriptor descriptor, int offset) {

            }
        });
    }

    /**
     * 发送消息至主线程
     * @param msg
     */
    public void sendToMainThread(final String msg) {
        EventBus.getDefault().post(msg);
    }

    /**
     * 接收来自主线程的消息
     */
    @Subscribe
    public void receiveToMain(String msg) {
        switch (msg) {
            case "notify":
//                BleHelper.sendNotify(msg+(count++));
               /* BleHelper.sendNotify("0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789" +
                        "-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789-0123456789" +
                        "-ABC");*/
                BleHelper.sendNotify("0123456789-0123456789-0123456789");
                break;
            default:

                break;
        }
    }





    private final BroadcastReceiver mReceiver =new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
//            if (BluetoothAdapter.ACTION_BLUETOOTH_ADDRESS_CHANGED.equals(action)) {
            if ("android.bluetooth.adapter.action.BLUETOOTH_ADDRESS_CHANGED".equals(action)) {
                String newAddress = intent.getStringExtra("android.bluetooth.adapter.extra.BLUETOOTH_ADDRESS");
//                String newAddress = intent.getStringExtra(BluetoothAdapter.EXTRA_BLUETOOTH_ADDRESS);

                if (newAddress != null) {
                    LogUtils.d("onReceive  D : 蓝牙适配器地址已更改为:"+newAddress);

                } else {
                    LogUtils.e("onReceive E : 找不到蓝牙适配器地址参数!");
                }
            }
        }
    };
}
