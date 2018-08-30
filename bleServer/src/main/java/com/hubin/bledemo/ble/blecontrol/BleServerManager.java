package com.hubin.bledemo.ble.blecontrol;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattServer;
import android.bluetooth.BluetoothGattServerCallback;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.AdvertiseCallback;
import android.bluetooth.le.AdvertiseSettings;
import android.bluetooth.le.BluetoothLeAdvertiser;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Looper;

import com.blankj.utilcode.util.ConvertUtils;
import com.blankj.utilcode.util.FileIOUtils;
import com.blankj.utilcode.util.FileUtils;
import com.blankj.utilcode.util.StringUtils;
import com.blankj.utilcode.util.TimeUtils;
import com.blankj.utilcode.util.ZipUtils;
import com.hubin.bledemo.base.BleApp;
import com.hubin.bledemo.ble.bleconfig.AdvertisingConfig;
import com.hubin.bledemo.ble.bleconfig.BLEConstants;
import com.hubin.bledemo.ble.bleconfig.FileSuffix;
import com.hubin.bledemo.ble.bleconfig.GattConfig;
import com.hubin.bledemo.ble.bleinterface.BleAdvertisingListener;
import com.hubin.bledemo.ble.bleinterface.BleConnectListener;
import com.hubin.bledemo.ble.bleinterface.BleReadOrWriteListener;
import com.hubin.bledemo.utils.BytesUtils;
import com.hubin.bledemo.utils.CRC;
import com.hubin.bledemo.utils.LogUtils;

import org.greenrobot.eventbus.EventBus;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import static com.hubin.bledemo.ble.bleconfig.BLEConstants.BLUETOOTH_DATA_MSG_ERR;

/*
 *  @项目名：  BleDemo
 *  @包名：    com.hubin.bledemo.ble
 *  @文件名:   BleServerManager
 *  @创建时间:  2018/6/7 11:50
 *  @描述：    Ble蓝牙设备管理器，操作蓝牙的核心单例
 */
public class BleServerManager {

    private static final int MUT_DEFAULT = 23;//mtu默认值
    private int mMtu = MUT_DEFAULT;
    private static final int CharacteristicRead_Max = 512; //读取特征的最大值

    private BluetoothDevice mDevices;
    private Context mContext = BleApp.getContext();
    private static BleServerManager mInstance;
    private BluetoothManager mBluetoothManager;
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothGattServer mBluetoothGattServer;
    private BluetoothLeAdvertiser mBluetoothLeAdvertiser;

    private boolean isRefreshJust = true;

    private BleConnectListener mBleConnectListener;//连接状态hui
    private BleReadOrWriteListener mBleReadOrWriteListener;

    //单例
    public static BleServerManager getInstance() {
        if (mInstance == null) {
            synchronized (BleServerManager.class) {
                if (mInstance == null) {
                    mInstance = new BleServerManager();
                }
            }
        }
        return mInstance;
    }

    public boolean init() {
        LogUtils.d("init  D : BleManeger初始化！");
        //获取蓝牙管理器
        mBluetoothManager = (BluetoothManager) mContext.
                getSystemService(Context.BLUETOOTH_SERVICE);
        if (mBluetoothManager == null) {
            LogUtils.e("init E : 无蓝牙管理器！");
            return false;
        }
        //得到适配器
        mBluetoothAdapter = mBluetoothManager.getAdapter();
        if (mBluetoothAdapter == null) {
            LogUtils.e("初始化失败，无法获取蓝牙适配器！");
            return false;
        }
        //检查是设备是否支持蓝牙
        if (!mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE)) {
            LogUtils.e("初始化失败，本设备不支持蓝牙！");
            return false;
        }
        //开启蓝牙
        if (!mBluetoothAdapter.isEnabled()) {
            LogUtils.v("启用蓝牙！");
            mBluetoothAdapter.enable();
        }

        //设置蓝牙名字
        mBluetoothAdapter.setName("huyz-vivo");
        return true;
    }

    public String getLocalAddress() {
        return mBluetoothAdapter.getAddress();
    }
    /**
     * 设置并开启蓝牙广播Advertising
     *
     * @throws Exception
     */
    public void startAdvertising(final BleAdvertisingListener bleAdvertisingListener) {
        if (mBluetoothAdapter == null) {
            LogUtils.e("enbleBle E : 没有蓝牙适配器！");
            return;
        }
        //执行蓝牙广播 api21以上才支持
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBluetoothLeAdvertiser != null) {
                LogUtils.d("startAdvertising  D : 蓝牙广播已经初始化！");
                return;
            }
            if (!mBluetoothAdapter.isMultipleAdvertisementSupported()) {
                EventBus.getDefault().post("该设备不支持Advertising！");
                LogUtils.e("startAdvertising E : 该设备不支持Advertising！");
                return;
            }
            mBluetoothLeAdvertiser = mBluetoothAdapter.getBluetoothLeAdvertiser();
            if (mBluetoothLeAdvertiser == null) {
                LogUtils.e("startAdvertising E : Failed to create advertiser!");
                if (bleAdvertisingListener != null) {
                    EventBus.getDefault().post("初始化Advertising失败！");
                    bleAdvertisingListener.onStartFailure(new Exception("Failed to create " +
                            "advertiser!"));
                }
                return;
            }
            //发起Advertising
            mBluetoothLeAdvertiser.startAdvertising(AdvertisingConfig.getAdvertiseSettings(),
                    AdvertisingConfig.getAdvertiseData(mBluetoothAdapter), new AdvertiseCallback() {
                        @Override
                        public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                            LogUtils.d("onStartSuccess  D : 成功启动蓝牙广播！");
                            if (bleAdvertisingListener != null) {
                                bleAdvertisingListener.onStartSuccess(settingsInEffect);
                            }

                            LogUtils.w("init  W : 设备硬件地址！"+getLocalAddress());
                        }

                        @Override
                        public void onStartFailure(int errorCode) {
                            if (bleAdvertisingListener != null) {
                                bleAdvertisingListener.onStartFailure(new Exception("onStartFailure " +
                                        "errorCode=" + errorCode));
                            }
                        }
                    });
        } else {
            if (bleAdvertisingListener != null) {
                bleAdvertisingListener.onStartFailure(new Exception("BluetoothLeAdvertiser Need Api21" +
                        " or above!"));
            }
        }
    }

    /**
     * 停止蓝牙广播
     *
     * @param bleAdvertisingListener
     */
    public void stopAdvertising(final BleAdvertisingListener bleAdvertisingListener) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            if (bleAdvertisingListener != null) {
                bleAdvertisingListener.onStartFailure(new Exception("BluetoothLeAdvertiser Need Api21" +
                        " or above!"));
            }
        } else {
            if (mBluetoothLeAdvertiser != null) {
                mBluetoothLeAdvertiser.stopAdvertising(new AdvertiseCallback() {
                    @Override
                    public void onStartSuccess(AdvertiseSettings settingsInEffect) {
                        if (bleAdvertisingListener != null) {
                            bleAdvertisingListener.onStartSuccess(settingsInEffect);
                        }
                    }

                    @Override
                    public void onStartFailure(int errorCode) {
                        if (bleAdvertisingListener != null) {
                            bleAdvertisingListener.onStartFailure(new Exception("onStartFailure " +
                                    "errorCode=" + errorCode));
                        }
                    }
                });
                mBluetoothLeAdvertiser = null;
                LogUtils.d("stopAdvertising  D : 关闭蓝牙广播Advertising！");
            }
        }
    }

    /**
     * 开启GATT服务
     */
    public void startGATTServer() {
        stopGATTServer();
        LogUtils.d("startGATTServer  D : 添加GATT服务");
        //BluetoothGattServer作为周边来提供数据；BluetoothGattServerCallback返回周边的状态。
        mBluetoothGattServer = mBluetoothManager.openGattServer(mContext, mGattServerCallback);
        if (mBluetoothGattServer == null) {
            LogUtils.e("Unable to create GATT server");
            return;
        }
        //添加服务
        mBluetoothGattServer.addService(GattConfig.addGattService());
    }

    public void stopGATTServer() {
        if (mBluetoothGattServer != null) {
            mBluetoothGattServer.close();
            mBluetoothGattServer = null;
            LogUtils.d("stopGATTServer  D : 关闭GATT服务！");
        }
    }

    //设置连接状态监听
    public void setOnBleConnectListener(BleConnectListener bleConnectListener) {
        mBleConnectListener = bleConnectListener;
    }

    //设置远程读写状态监听
    public void setOnReadOrWriteListener(BleReadOrWriteListener bleReadOrWriteListener) {
        mBleReadOrWriteListener = bleReadOrWriteListener;
    }

    //发送通知到客户端
    public void sendNotify(String msg) {
        EventBus.getDefault().post("发送：" + msg + " len=" + msg.getBytes().length);
        sendNotify(mDevices, msg.getBytes());
    }

    //region ================================== 接收数据处理 ==================================


    private byte[] mContent;
    private byte[] crc16;
    private int dataType;
    private int count = 0;

    /**
     * 处理接收数据：
     * 第1-2个字节，crc16校验位
     * 第3个字节，标记类型
     * 第4个字节开始，到结束标记符，全是数据内容
     * 结束标记 @_END_@
     *
     * @param value 当前传输的数据
     */
    private void doReceive(BluetoothDevice device, int requestId, int offset, byte[] value) {
        if (!Arrays.equals(BLEConstants.BLUETOOTH_DATA_END.getBytes(), value)) {
            //1.开始接收数据
            if (mContent == null) {
                crc16 = Arrays.copyOf(value, 2);
                LogUtils.i("crc16[0]=" + crc16[0] + ",crc16[1]=" + crc16[1]);
                dataType = value[2];
                mContent = Arrays.copyOfRange(value, 3, value.length);
                LogUtils.i("doReceive,first mContent=" + new String(mContent));
                count = 1;
            } else {
                mContent = BytesUtils.concat(mContent, value);
                count++;
                LogUtils.i("doReceive,count=" + count + ",mContent.length=" + mContent.length);
            }
        } else {
            //2.数据接收完毕
            long crcL = new CRC(CRC.Parameters.CRC16).calculateCRC(mContent);
            byte[] rawCrc16 = new byte[2];
            rawCrc16[0] = new Long(crcL >> 8 & 0xff).byteValue();
            rawCrc16[1] = new Long(crcL & 0xff).byteValue();

//            LogUtils.i("doReceive,start merge data,crc16=" + ConvertUtils.bytes2HexString(crc16) +
// ",rawCrc16=" + ConvertUtils.bytes2HexString(rawCrc16) + ",dataType=" + dataType + ",mContent
// .length=" + mContent.length);
            //获取对应特征
            BluetoothGattCharacteristic characteristicRead = mBluetoothGattServer.getService(BLEConstants
                    .UUID_BLE_SERVICE).getCharacteristic(BLEConstants.UUID_CHARACTERISTIC_READ);
            //校验数据失败
            if (!Arrays.equals(crc16, rawCrc16)) {
                mContent = null;
                //响应手机端写入失败

                characteristicRead.setValue("" + BLUETOOTH_DATA_MSG_ERR + "");
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,
                        offset, characteristicRead.getValue());
                return;
            }
            //响应手机端成功
            characteristicRead.setValue("" + BLEConstants.BLUETOOTH_DATA_MSG_SUCCESS + "");
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    characteristicRead.getValue());


            switch (FileSuffix.bytesToSuffix(dataType)) {
                case STRING:
                    //纯字符串指令回调
                    if (mBleReadOrWriteListener != null) {
                        mBleReadOrWriteListener.onStringCmd(new String(mContent));
                    }
                    break;
                default:
                    //文件数据
                    doBluetoothFile();
                    break;
            }
            mContent = null;
        }
    }

    /**
     * 处理文件数据
     */
    private void doBluetoothFile() {
        String filename = TimeUtils.millis2String(System.currentTimeMillis(), new SimpleDateFormat
                ("yyyy-MM-dd-HH-mm-ss", Locale.getDefault()));
        String cacheDir = mContext.getExternalCacheDir() + "/ble_receive/";
//        //1. 先删除原来的程序，再创建
        FileUtils.deleteDir(cacheDir);
        FileUtils.createOrExistsDir(cacheDir);
        String filepath = cacheDir + filename + FileSuffix.bytesToSuffix(dataType);
        FileIOUtils.writeFileFromBytesByStream(filepath, mContent);
        LogUtils.i("doBluetoothFile,file is saved,size=" + FileUtils.getFileSize(filepath));

        // 2. 如果是zip包，格式：
        // code_blocks.xml
        // images/xxx.jpg
        // audios/xxx.pcm

        if (dataType == BLEConstants.BLUETOOTH_DATA_TYPE_ZIP) {
            try {
                ZipUtils.unzipFile(filepath, cacheDir);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // 3.机器人读取固定路径文件 cacheDir + code_blocks.xml 并解析，执行动作
        File file = new File(cacheDir);
        for (File f : file.listFiles()) {
            if (f.getAbsolutePath().endsWith(".xml")) {
                filepath = f.getAbsolutePath();
                break;
            }
        }

        LogUtils.d("receive filepath:" + filepath);

        if (mBleReadOrWriteListener != null) {
            mBleReadOrWriteListener.onReceiveSuccess(FileUtils.getFileSize(filepath), filepath);
        }

    }


    //endregion =============================== 接收数据处理 ==================================


    //切换线程并发送通知到客户端
    private void sendNotify(final BluetoothDevice device, final byte[] value) {
        if (device == null) {
            LogUtils.e("sendNotify E : device不能为Null！");
            return;
        }
        if (Looper.myLooper() == Looper.getMainLooper()) {
            send(device, value);
        } else {
            BleApp.getMainThreadHandler().post(new Runnable() {
                @Override
                public void run() {
                    LogUtils.d("run  D : 已切换到主线程！");
                    send(device, value);
                }
            });
        }

    }

    //发送通知到客户端
    private void send(BluetoothDevice device, byte[] value) {
        LogUtils.d("send  D : 发送长度：" + value.length);

        //获取订阅的特征notify
        BluetoothGattCharacteristic notify = mBluetoothGattServer.getService(BLEConstants
                .UUID_BLE_SERVICE).getCharacteristic(BLEConstants.UUID_CHARACTERISTIC_NOTIFY);
        notify.setValue(value);
        BluetoothGattDescriptor descriptor = notify.getDescriptor(BLEConstants
                .UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR);
        descriptor.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);

        /*
        notify.getDescriptor(BLEConstants.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR).setValue
                (BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);*/

        if (mBluetoothGattServer != null && notify != null) {
            mBluetoothGattServer.notifyCharacteristicChanged(device, notify, false);
        }
    }


    private BluetoothGattServerCallback mGattServerCallback = new BluetoothGattServerCallback() {

        /**
         * 连接状态的回调
         * @param device 连接的设备
         * @param status 连接状态
         * @param newState
         */
        @Override
        public void onConnectionStateChange(BluetoothDevice device, int status, int newState) {
            mMtu = MUT_DEFAULT;
            if (byteBufferList != null) {
                byteBufferList.clear();
            }
            switch (newState) {
                case BluetoothProfile.STATE_CONNECTED://连接状态
                    List<BluetoothDevice> devicesAll = mBluetoothManager.getConnectedDevices(BluetoothProfile.GATT_SERVER);
                    for (int i = devicesAll.size() - 1; i >= 0; i--) {
                        if (!devicesAll.get(i).getAddress().equals(device.getAddress())) {
//                            LogUtils.i("onConnectionStateChange I : 断开过期连接:" + devicesAll.get(i)
//                                    .getAddress());
//                            mBluetoothGattServer.cancelConnection(devicesAll.get(i));
                            sendNotify(devicesAll.get(i),BLEConstants.MSG_NOTIFY_CLOSE);
                            LogUtils.w("onConnectionStateChange  W : 发送断开指令"+ devicesAll.get(i).getAddress());
                            EventBus.getDefault().post("断开指令:"+ConvertUtils.bytes2HexString(BLEConstants.MSG_NOTIFY_CLOSE)+"  mac:"+devicesAll.get(i).getAddress());
                        } else {
                            LogUtils.i("onConnectionStateChange I : 新的连接：" + devicesAll.get(i).getAddress()+"  "+ devicesAll.get(i).getName());
                            EventBus.getDefault().post("当前连接:"+devicesAll.get(i).getAddress());
                        }
                    }
                    mDevices = device;
                    if (mBleConnectListener != null) {
                        mBleConnectListener.onConnect(device);
                    }
                    break;
                case BluetoothProfile.STATE_DISCONNECTED://断开状态
                    LogUtils.e("onConnectionStateChange  D : 断开连接：" + device.getAddress());
                    if (byteBufferList != null) {
                        byteBufferList.clear();
                    }
                    if (mBleConnectListener != null) {
                        mBleConnectListener.onDisconnect(device);
                    }
                    LogUtils.d("onConnectionStateChange I : " + getDevicesList());
                    break;
                default:
                    LogUtils.e("onConnectionStateChange E : 其它状态！");
                    break;
            }
        }


        /**
         * 远程客户端请求读取本地特征
         * @param device 请求的远程设备
         * @param requestId 请求的标识
         * @param offset  偏移量
         * @param characteristic 要读取的特征
         */
        @Override
        public void onCharacteristicReadRequest(BluetoothDevice device, int requestId, int offset,
                                                BluetoothGattCharacteristic characteristic) {

            LogUtils.d("onCharacteristicReadRequest  D : 读取特征! mac= " + device.getAddress() +
                    "requestId=" + requestId);
            //拿取当前缓冲的数据
            byte[] data = getPacketData(requestId);
            if (data != null) {//缓冲区有数据
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,offset, data);
                if (data.length < mMtu - 1) {//结尾
                    if (mBleReadOrWriteListener != null) {
                        mBleReadOrWriteListener.onCharacteristicReadRequest(device,characteristic,offset);
                    }
                    LogUtils.d("onCharacteristicReadRequest  D : 响应mac= "+device.getAddress()+"endId="+requestId);
                }

            } else {//缓冲区没有数据
                String value = getData(characteristic.getUuid());
                if (!StringUtils.isEmpty(value)) {
                    //添加到缓冲区
                    addByteBuffer(value, requestId);
                    //响应
                    mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,offset,getPacketData(requestId));
                    LogUtils.d("onCharacteristicReadRequest  D 读取特征|"+"准备响应：" + value+" len= " + value.getBytes().length);
                    EventBus.getDefault().post("响应:" + value + " len=" + value.getBytes().length);
                } else {//没有获取到token
                    mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS,offset,"null".getBytes());
                }
            }
        }


        /**
         * 远程客户端请求写入本地特征
         * @param device 请求的远程设备
         * @param requestId 请求的标识
         * @param characteristic  要写入的特征
         * @param preparedWrite true：如果这个写入操作需要等候
         * @param responseNeeded true：如果远程设备需要相应
         * @param offset 偏移量
         * @param value 客服端想分配给描述符的值
         */
        @Override
        public void onCharacteristicWriteRequest(BluetoothDevice device, int requestId,
                                                 BluetoothGattCharacteristic characteristic,
                                                 boolean preparedWrite, boolean responseNeeded, int
                                                         offset, byte[] value) {
            super.onCharacteristicWriteRequest(device, requestId, characteristic, preparedWrite,
                    responseNeeded, offset, value);
            LogUtils.d("onCharacteristicWriteRequest  D : 写入中: mac=" + device.getAddress() + " len=" +
                    value.length);
            if (mBleReadOrWriteListener != null) {
                mBleReadOrWriteListener.onCharacteristicWriteRequest(device, characteristic, offset);
            }
            doReceive(device, requestId, offset, value);

            if (responseNeeded) {
                LogUtils.d("onCharacteristicWriteRequest  D : 响应：" + characteristic.getValue());
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                        characteristic.getValue());
            }
        }

        @Override
        public void onDescriptorReadRequest(BluetoothDevice device, int requestId, int offset,
                                            BluetoothGattDescriptor descriptor) {
            LogUtils.d("onDescriptorReadRequest  D : 远程客户端请求读取本地描述！mac=" + device.getAddress());

            if (mBleReadOrWriteListener != null) {
                mBleReadOrWriteListener.onDescriptorReadRequest(device, descriptor, offset);
            }
            mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,
                    null);
        }


        /**
         * 描述符写入请求
         * @param device
         * @param requestId
         * @param descriptor
         * @param preparedWrite
         * @param responseNeeded
         * @param offset
         * @param value
         */
        @Override
        public void onDescriptorWriteRequest(BluetoothDevice device, int requestId,
                                             BluetoothGattDescriptor descriptor,
                                             boolean preparedWrite, boolean responseNeeded,
                                             int offset, byte[] value) {
            mDevices = device;


            LogUtils.d("onDescriptorWriteRequest  D : 描述写入请求device=" + device.getAddress() + "描述:" +
                    (descriptor.getValue() == null ? "" : descriptor.getValue()) +
                    " 排队等候:" + preparedWrite + " 需要响应:" + responseNeeded);
            if (mBleReadOrWriteListener != null) {
                mBleReadOrWriteListener.onDescriptorWriteRequest(device, descriptor, offset);
            }
            EventBus.getDefault().post("写入描述:" + (descriptor.getValue() == null ? "" : ConvertUtils.bytes2HexString(descriptor.getValue())));

            if (responseNeeded) {
                EventBus.getDefault().post("写描述响应："+ConvertUtils.bytes2HexString(value));
                mBluetoothGattServer.sendResponse(device, requestId, BluetoothGatt.GATT_SUCCESS, offset,value);
            }

            if (Arrays.equals(value, BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE)) {
                //启用notify
                EventBus.getDefault().post("启用Notify！");
            } else if (Arrays.equals(value, BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE)){
                //取消notify
                EventBus.getDefault().post("停止Notify");
            }
        }

        /**
         * 如果远程客户端请求更改给定连接的MTU，则将调用此回调。
         * @param device The remote device that requested the MTU change
         * @param mtu    The new MTU size
         */
        @Override
        public void onMtuChanged(BluetoothDevice device, int mtu) {
            mMtu = mtu;
            if (byteBufferList != null) {
                byteBufferList.clear();
            }
            LogUtils.d("onMtuChanged  D : divice=" + device.getAddress() + " mtu修改为：" + mtu);
            EventBus.getDefault().post(" 修改mtu=" + mtu);
        }

        /**
         * 通知发送结果回调
         * @param device
         * @param status
         */
        @Override
        public void onNotificationSent(BluetoothDevice device, int status) {
            super.onNotificationSent(device, status);
            mDevices = device;
            if (status == BluetoothGatt.GATT_SUCCESS) {
                LogUtils.d("onNotificationSent  D : 通知发送成功!  状态：" + status);
            } else {
                LogUtils.e("onNotificationSent  D : 发送通知失败!  状态：" + status);
            }
        }

        @Override
        public void onServiceAdded(int status, BluetoothGattService service) {
            super.onServiceAdded(status, service);
            LogUtils.d("onServiceAdded  D : BLE蓝牙初始化完成！");
            EventBus.getDefault().post("GATT启动成功...");
        }
    };


    //用于帮助打印当前连接的所有设备
    public String getDevicesList() {
        StringBuffer mstr = new StringBuffer();
        List<BluetoothDevice> devicesAll = mBluetoothManager.getConnectedDevices(BluetoothProfile
                .GATT_SERVER);
        if (devicesAll == null || devicesAll.size() == 0) {
            mstr.append("当前无设备连接！");
            EventBus.getDefault().post("当前连接:无！");
            return mstr.toString();
        }
        for (int i = 0; i < devicesAll.size(); i++) {
            mstr.append("当前连接的设备" + (i + 1) + "：" + devicesAll.get(i).getAddress() + " name=" +
                    devicesAll.get(i).getName() + "\r\n");
        }
        return mstr.toString();
    }


    //region ================================== 发送数据分包 ==================================
    private ArrayList<byte[]> byteBufferList = new ArrayList<>();//作为数据分包的缓冲区

    /**
     * 数据添加到缓冲区
     *
     * @param data
     * @param requestId
     */
    private void addByteBuffer(String data, int requestId) {
        byteBufferList.add((requestId + "").getBytes());//第一位记录当前id
        byte[] bytes = data.getBytes();
        if (bytes.length < mMtu-1) {
            byteBufferList.add(bytes);
            LogUtils.i("addByteBuffer I : 无需分包！");
            return;
        }
        if (bytes.length>CharacteristicRead_Max) {
            LogUtils.w("addByteBuffer I : 超出传输上限，丢失尾部！");
            byte[] mbyte = new byte[512];
            for (int i = 0; i <mbyte.length; i++) {
                mbyte[i] = bytes[i];
            }
            bytes=mbyte;
        }
        //创建容器
        byte[] buffer;
        for (int i = 0; i < bytes.length; i += (mMtu - 1)) {
            LogUtils.i("addByteBuffer I : i=" + i);
            //当前是否是结尾
            if (bytes.length - i < mMtu - 1) {//已经是结尾
                buffer = new byte[bytes.length % (mMtu - 1)];
                LogUtils.i("addByteBuffer I : i==" + i + "时buffer长度= " + (bytes.length % (mMtu - 1)));
            } else {//不是结尾
                buffer = new byte[(mMtu - 1)];
                LogUtils.i("addByteBuffer I : i==0时buffer长度= " + (mMtu - 1));
            }
            //赋值
            for (int j = 0; j < buffer.length; j++) {
                buffer[j] = bytes[i + j];
            }
            //添加进缓冲区
            byteBufferList.add(buffer);
        }
        if (byteBufferList.get(byteBufferList.size() - 1).length == mMtu - 1) {
            byteBufferList.add(BLEConstants.BLUETOOTH_DATA_END.getBytes());//添加结束标记
            LogUtils.w("addByteBuffer E : 尾包添加结束标记：" + BLEConstants.BLUETOOTH_DATA_END);
        }
    }

    /**
     * 获取当前缓冲区需要发送的数据
     *
     * @param requestId
     */
    public byte[] getPacketData(int requestId) {
        if (byteBufferList.size() != 0) {
            int fastId = Integer.parseInt(new String(byteBufferList.get(0)));
            int count = requestId - fastId;
            if (count < byteBufferList.size() - 1) {
                return byteBufferList.get(count + 1);
            } else {//缓冲区数据已发送完毕
                byteBufferList.clear();
                return null;
            }
        }
        return null;
    }

    //endregion =============================== 发送数据分包 ==================================

    public String getData(UUID uuid) {
        String value = null;
        if (uuid.equals(BLEConstants.UUID_CHARACTERISTIC_TOKEN)) {
            LogUtils.d("onCharacteristicReadRequest  D : 读取token！");
            value = ".*`OkKK=8Fs|Rphtwi";
        } else if (uuid.equals(BLEConstants.UUID_CHARACTERISTIC_PETID)) {
            LogUtils.d("onCharacteristicReadRequest  D : 读取petid！");
            value = "149";
        } else if (uuid.equals(BLEConstants.UUID_CHARACTERISTIC_PETID_TOKEN)) {
            value = "149@-@.*`OkKK=8Fs|Rphtwi";
            LogUtils.d("onCharacteristicReadRequest  D : 同时读取token和Petid！");
        } else if (uuid.equals(BLEConstants.UUID_CHARACTERISTIC_REFRESH_TOKEN)) {
            LogUtils.d("refresh token...");
            value = "refresh token...";
            EventBus.getDefault().post("refresh token...");
        }
        return value;
    }
}
