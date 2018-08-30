package com.hubin.bleclient.ble.BleCore;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.bluetooth.le.ScanResult;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.text.TextUtils;

import com.hubin.bleclient.ble.bleConfig.BLEConfig;
import com.hubin.bleclient.ble.bleConfig.FileSuffix;
import com.hubin.bleclient.ble.bleException.BleException;
import com.hubin.bleclient.ble.bleException.CharacteristicReadException;
import com.hubin.bleclient.ble.bleException.CharacteristicWriteException;
import com.hubin.bleclient.ble.bleException.ConnectFialException;
import com.hubin.bleclient.ble.bleException.ConnectTimeOverException;
import com.hubin.bleclient.ble.bleException.NotifyException;
import com.hubin.bleclient.ble.bleException.RequestMtuException;
import com.hubin.bleclient.ble.bleException.ScanFailedException;
import com.hubin.bleclient.ble.bleInterface.BleConnectListener;
import com.hubin.bleclient.ble.bleInterface.BleMtuListener;
import com.hubin.bleclient.ble.bleInterface.BleNotifyListener;
import com.hubin.bleclient.ble.bleInterface.BleReadListener;
import com.hubin.bleclient.ble.bleInterface.BleScanListener;
import com.hubin.bleclient.ble.bleInterface.BleWriteListener;
import com.hubin.bleclient.utils.BytesUtils;
import com.hubin.bleclient.utils.CRC;
import com.hubin.bleclient.utils.ConvertUtils;
import com.hubin.bleclient.utils.LogUtils;
import com.hubin.bleclient.utils.ThreadUtils;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static android.bluetooth.BluetoothProfile.GATT;


/**
 *  @项目名：  AiiageSteamAndroid
 *  @包名：    com.aiiage.steam.mobile.ble.BleCore
 *  @文件名:   BleCore
 *  @创建者:   胡英姿
 *  @创建时间:  2018/7/2 10:02
 *  @描述：    封装了蓝牙所有的最终核心操作
 */
public class BleCore {
    public static final int MSG_SCAN_TIME_OVER = 0X10; //扫描时间到
    public static final int MSG_CONNECT_TIME_OVER = 0X11; //连接超时

    public static final int MSG_SCAN_OLD_DEVICE = 0X12; //api<21 扫描到的设备
    public static final int MSG_SCAN_LOLLIPOP_DEVICE = 0X13; //api>=21 扫描到的设备

    public static final int MSG_WRITE_CHARACTERISTIC = 0X14; //写特征
    public static final int MSG_READ_CHARACTERISTIC = 0X15; //读特征

    private Context mContext;
    private MainHandler mMainHandler;
    private ScanHandler mScanHandler;
    private W2RHandler mW2RHandler;

    private int connectCount = 0; //连接次数计数器
    public static final int MTU_DEFAULT = 23; //mtu默认值
    public static final int MTU_MAX = 512; //设置mtu值时的最大上限
    //缓存
    private BleDevice mBleDevice;
    private Map<String,BleDevice> mBleCacheMap; //扫描到的设备集合
    private Queue<byte[]> mDataBuffer; //分包队列

    //资源
    private BluetoothAdapter mBluetoothAdapter;
    private BluetoothManager mBluetoothManager;
    private BluetoothLeScanner mBluetoothLeScanner;
    private BluetoothGatt mBluetoothGatt;
    private BluetoothGattCharacteristic mCharacteristic;

    //配置
    private boolean isAutoConnect; //false直接连接到蓝牙，true 蓝牙可用时自动连接

    //回调
    private BleOldScanCallback mOldScanCallback;
    private BleScanCallback mBleScanCallback;
    private BleGattCallback mBleGattCallback;
    private BleScanListener mBleScanListener;
    private BleConnectListener mBleConnectListener;
    private BleReadListener mBleReadListener;
    private BleWriteListener mBleWriteListener;
    private BleMtuListener mBleMtuListener;
    private BleNotifyListener mBleNotifyListener;

    //状态
    private boolean isScaning; //是否在扫描中的标志位
    private boolean isAutoState; //是否正在重连的标志位
    private int mConnectionState = STATE_DISCONNECTED; //蓝牙当前连接状态
    public static final int STATE_DISCONNECTED = 0;//断开连接的状态
    public static final int STATE_CONNECTING = 1;//连接中的状态
    public static final int STATE_CONNECTED = 2;//已经连接的状态
    private AtomicBoolean isWriteOrRead = new AtomicBoolean(false);; //是否正在读写数据
    private boolean isPollWrite;//是否在循环写入的标志位
    private AtomicBoolean isScanAutoConnect = new AtomicBoolean(false); //是否是根据名字扫描重连的状态


    private BleParamConfig mBleParamConfig;

    /**
     * 构造
     * @param bleParamConfig 配置
     * @param isSendQueue  是否初始化读写数据线程 可以有效防止并发操作带来的问题
     *                     true 读写时请调用 sendReadMsg(...) 或者 sendWriteMsg(...) 将操作发送到消息队列
     *
     */
    public BleCore(@NonNull BleParamConfig bleParamConfig, Context context, boolean isSendQueue) {
        mContext = context;
        mBleParamConfig = bleParamConfig;
        isAutoConnect=bleParamConfig.getAutoConnect();
        mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (isSupportBle()) {//支持蓝牙
            mBluetoothManager = (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
        }
        //创建缓存集合
        mBleCacheMap = new LinkedHashMap<>();
        //创建主线程的Handler
        mMainHandler = new MainHandler(Looper.getMainLooper(),this);
        //创建扫描线程 以及handler
        HandlerThread mScanThread = new HandlerThread("scan");
        mScanThread.start();
        mScanHandler = new ScanHandler(mScanThread.getLooper(), this);

        if (isSendQueue) {
            //创建读写线程 以及handler
            HandlerThread mW2RThread = new HandlerThread("read2write");
            mW2RThread.start();
            mW2RHandler = new W2RHandler(mW2RThread.getLooper(),this);
        }
    }
    private boolean isBlueEnable() {//蓝牙是否启用
        return mBluetoothAdapter != null && mBluetoothAdapter.isEnabled();
    }
    private boolean isSupportBle() {//是否支持蓝牙
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2
                && mContext.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }
    public synchronized BleDevice getDevice() {
        if (mBleCacheMap != null) {
          return mBleCacheMap.get(mBleDevice.getAddress());
        }
        LogUtils.e("getDevice E : 没有拿到device！");
        return null;
    }
    public AtomicBoolean getIsWriteOrRead() {
        return isWriteOrRead;
    }

    public List<BluetoothDevice> getDeviceAll() {
      return mBluetoothManager.getConnectedDevices(GATT);
    }

    //region ================================== 扫描 ==================================

    /**
     * 发起扫描
     * @param bleName 根据蓝牙名字过滤扫描
     * @param bleScanListener
     */
    public synchronized void scan(String bleName,BleScanListener bleScanListener) {
        if (isScaning) {
            LogUtils.w("scan W : 正在扫描中...");
            return;
        }
        LogUtils.d("scan  D : 开始扫描！");
        if (!TextUtils.isEmpty(bleName)) {
            LogUtils.d("scan  D : 根据名字发起过滤扫描："+bleName);
        }
        isScaning = true;
        if (!isScanAutoConnect.get()) {
            this.mBleScanListener = bleScanListener;
            setScanStartedCallback();
        }
        if (!isBlueEnable()) {//蓝牙未启用
            setScanFailedCallback(new ScanFailedException(BleException.ERROR_CODE_BLE_NOT_ENABLE));
            return;
        }
        if (!isSupportBle()) {
            setScanFailedCallback(new ScanFailedException(BleException.ERROR_CODE_BLE_NOT_SUPPORT));
            return;
        }
        if (mBleCacheMap != null) {
            mBleCacheMap.clear();//清空缓存
        }
        if (checkConnected()) {//连接中
            mBleCacheMap.put(mBleDevice.getAddress(),mBleDevice);
            setScanningCallback(mBleDevice);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mBluetoothLeScanner = mBluetoothAdapter.getBluetoothLeScanner();

            if (mBluetoothLeScanner == null) {
                LogUtils.e("scan E : bluetoothLeScanner == null");
                setScanFailedCallback(new ScanFailedException(BleException.ERROR_CODE_BLE_SCANNER_NOPOINTER));
                return;
            }

            if (mBleScanCallback == null) {
                mBleScanCallback = new BleScanCallback(this);
            }

            if (TextUtils.isEmpty(bleName)) {
                mBluetoothLeScanner.startScan(mBleParamConfig.getGlobaltScanFilter(), mBleParamConfig.getGlobaltScanSettings(),mBleScanCallback);
            } else {
                mBluetoothLeScanner.startScan(mBleParamConfig.getBleNameScanFilter(bleName), mBleParamConfig.getGlobaltScanSettings(),mBleScanCallback);
            }

        } else {//兼容低版本
            if (mOldScanCallback == null) {
                mOldScanCallback = new BleOldScanCallback(this);
            }
            if (!TextUtils.isEmpty(mBleParamConfig.getDeviceName())) {
                mOldScanCallback.setFilterName(mBleParamConfig.getDeviceName());
                if ((!TextUtils.isEmpty(bleName))) {
                    mOldScanCallback.setFilterName(bleName);
                }
            }
            //过滤扫描
            mBluetoothAdapter.startLeScan(mBleParamConfig.getGlobalOldScanFilter(),mOldScanCallback);
        }
        sendMsgDelayed(MSG_SCAN_TIME_OVER,mBleParamConfig.getScanTimeOut()); //规定时间内停止扫描
    }

    public synchronized void stopScan() {
       LogUtils.d("stopScan  D : 停止扫描！");
        removeMsg(MSG_SCAN_TIME_OVER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBluetoothLeScanner != null) {
               mBluetoothLeScanner.stopScan(mBleScanCallback);
            }
        } else {
            mBluetoothAdapter.stopLeScan(mOldScanCallback);
        }
        isScaning = false;
        setScanFinishedCallback();
    }
    //停止扫描并且不回调出去
    private synchronized void stopScanNotCallback() {
       LogUtils.d("stopScan  D : 停止扫描不对外回调！");
        removeMsg(MSG_SCAN_TIME_OVER);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (mBluetoothLeScanner != null) {
               mBluetoothLeScanner.stopScan(mBleScanCallback);
            }
        } else {
            mBluetoothAdapter.stopLeScan(mOldScanCallback);
        }
        isScaning = false;
    }

    //缓存设备
    private synchronized void cacheDevice(BleDevice bleDevice) {
        if (isScanAutoConnect.get()&&bleDevice!=null) {//扫描重连的状态
            //刷新device
            mBleCacheMap.remove(mBleDevice.getAddress());
            mBleCacheMap.put(bleDevice.getAddress(),bleDevice);
            LogUtils.w("cacheDevice  W : 旧设备mac:"+mBleDevice);
            mBleDevice = bleDevice;//刷新缓存设备
            LogUtils.w("cacheDevice  W : 新设备mac:"+mBleDevice );
            stopScanNotCallback();//停止扫描并且不回掉
            isScanAutoConnect.set(false);//标志位清除
            LogUtils.d("cacheDevice  D : 重连新的device！");
            autoConnect(mBleConnectListener);//重连新的device！
            return;
        }


        if (mBleCacheMap.get(bleDevice.getAddress())==null) { //过滤重复
            mBleCacheMap.put(bleDevice.getAddress(),bleDevice);
            LogUtils.i("cacheDevice I  Thread:"+Thread.currentThread().getName()+"新设备 " + bleDevice.getName()+" :"+bleDevice);
            setScanningCallback(bleDevice);
        }
    }

    //endregion =============================== 扫描 ==================================

    //region ================================== 连接 ==================================

    //获取连接状态
    public int getConnectionState() {
        return mConnectionState;
    }

    //判断当前连接次数是否已经大于最大的重连次数
    public boolean isMaxCount() {
        return connectCount >= mBleParamConfig.getConnectMaxCount();
    }


    //是否已经连接上
    public boolean  checkConnected() {
        if (mBleDevice != null&&mBluetoothManager.getConnectionState(mBleDevice.getDevice(), GATT)
                    == BluetoothProfile.STATE_CONNECTED) {
            return true;
        }
        return false;
    }

    public synchronized void connect(String mac, BleConnectListener bleConnectListener){
        if (TextUtils.isEmpty(mac)) {
            LogUtils.e("connect E : mac地址不能为空！");
            return ;
        }

        //根据远程mac地址连接
        BluetoothDevice remoteDevice = mBluetoothAdapter.getRemoteDevice(mac);
//        connect(mBleCacheMap.get(mac),bleConnectListener);
        connect(remoteDevice,bleConnectListener);
    }
    public synchronized void connect(BleDevice bleDevice, BleConnectListener bleConnectListener) {
        if (bleDevice == null) {
            LogUtils.e("connect E : bleDevice不能为空！");
            return ;
        }
        if (mConnectionState == STATE_CONNECTING) {
            LogUtils.w("connect W : 连接中请勿频繁连接！");
            return;
        }
        mBleDevice = bleDevice;
        connectCount=0;//重置计数器
        connect(bleDevice.getDevice(),bleConnectListener);
    }
    private synchronized void connect(BluetoothDevice device, BleConnectListener bleConnectListener){
        if (device == null) {
            LogUtils.e("connect E : BluetoothDevice不能为空！");
            return ;
        }
        //设置为正在连接的状态
        mConnectionState=STATE_CONNECTING;//连接中
        mBleConnectListener = bleConnectListener;

        if (isScaning) {//正在扫描
            stopScan();//停止扫描
        }

        if (mBleGattCallback == null) {
            mBleGattCallback = new BleGattCallback(this);
        }

        connectBle(device);//发起连接
    }

    private synchronized void connectBle(BluetoothDevice device) {//延时发起连接
        connectCount++;//连接计数器
        LogUtils.d("connectBle  D : "+device.getAddress()+"发起第"+connectCount+"次连接！");
        mMainHandler.postDelayed(()->{
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O&&!isAutoConnect) { //>26 8.0 只有当isAutoConnect为false的时候此设置才会生效
                //首选PHY为 PHY_LE_2M_MASK
                LogUtils.i("connect  D : api>=26 android8.0 首选PHY为 PHY_LE_2M_MASK！");
                mBluetoothGatt = device.connectGatt(mContext,isAutoConnect, mBleGattCallback, BluetoothDevice.TRANSPORT_LE, BluetoothDevice
                                .PHY_LE_2M_MASK);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) { //>23  6.0 //连接双模设备时优先选择le设备
                LogUtils.i("connect  D :  api>=23 android6.0 连接双模设备时优先选择le设备!");
                mBluetoothGatt = device.connectGatt(mContext,isAutoConnect, mBleGattCallback, BluetoothDevice.TRANSPORT_LE);
            }else if (Build.VERSION.SDK_INT >=Build.VERSION_CODES.JELLY_BEAN_MR2){
                LogUtils.i("connect  D : api>=18 android4.4 默认发起连接！");
                mBluetoothGatt = device.connectGatt(mContext,isAutoConnect, mBleGattCallback);
            } else {
                LogUtils.e("connect E : 手机系统版本低于4.3不支持蓝牙4.0！");
                return ;
            }
            if (mBluetoothGatt != null) {
                setStartConnectCallback(device);//回调
                sendMsgDelayed(MSG_CONNECT_TIME_OVER,mBleParamConfig.getConnectTimeOut());//开始倒计时
            } else {//异常
                closeBle();
                if (isMaxCount()) {
                    setConnectFailCallback(device,new ConnectFialException(BleException.ERROR_CODE_GATT_NULL));
                } else {
                    LogUtils.e("connectBle E : 发现异常400开始重连！");
                    autoConnect();//发现异常400开始重连！
                }
            }
        },mBleParamConfig.getScanOrconnectGapTime());

    }

    public synchronized void autoConnect() {
        LogUtils.d("autoConnect  D : 重连缓存设备！");
        if (connectCount==mBleParamConfig.getConnectMaxCount()-1) {
            scanAutoConnect(mBleDevice.getName(),mBleConnectListener);//扫描重连
        } else {
            autoConnect(mBleConnectListener);
        }
    }

    public synchronized void autoConnect(BleConnectListener bleConnectListener) { //重连缓存设备
        if (mBleDevice != null) {
            isAutoState=true;
            LogUtils.i("superAutoConnect I : 延时等待"+mBleParamConfig.getErrorGapTime()+"ms...");
            mMainHandler.postDelayed(()->{
                connect(mBleDevice.getDevice(),bleConnectListener);
            },mBleParamConfig.getErrorGapTime());
        }
    }

    private synchronized void scanAutoConnect(String bleName,BleConnectListener bleConnectListener) { //重连缓存设备
        if (!TextUtils.isEmpty(bleName)) {
            isAutoState=true;
            LogUtils.d("superAutoConnect I : 根据名字扫描新设备并重连 延时等待"+mBleParamConfig.getErrorGapTime()+"ms...");
            isScanAutoConnect.set(true);
            mMainHandler.postDelayed(()->{
                scan(bleName,mBleScanListener);//发起名字过滤扫描
            },mBleParamConfig.getErrorGapTime());
        }
    }

    //endregion =============================== 连接 ==================================

    //region ================================== 读特征 ==================================
    public synchronized void sendReadMsg(UUID serviceUUID,UUID characteristicUUID,BleReadListener bleReadListener) {
        if (mW2RHandler == null) {
            LogUtils.e("sendReadMsg E : 没有初始化，请先调用 write2ReadThreadPrepare()");
            return;
        }
        BleReadBean mBleReadBean = new BleReadBean(serviceUUID,characteristicUUID,bleReadListener);
        Message message = mW2RHandler.obtainMessage();
        message.what = MSG_READ_CHARACTERISTIC;
        message.obj = mBleReadBean;
        mW2RHandler.sendMessage(message);
    }

    //切换到主线程执行读写
    private void readChangeThread(UUID serviceUUID,UUID characteristicUUID,BleReadListener bleReadListener) {
        isWriteOrRead.set(true);
        if (Looper.myLooper() == Looper.getMainLooper()){
            readCharacteristic(serviceUUID,characteristicUUID,bleReadListener);
        } else {
            ThreadUtils.mainHandler.post(()->{
                readCharacteristic(serviceUUID,characteristicUUID,bleReadListener);
            });
        }
    }
    public void readCharacteristic(UUID serviceUUID,UUID characteristicUUID,BleReadListener bleReadListener) {
        isWriteOrRead.set(true);
        mBleReadListener = bleReadListener;
        if (serviceUUID == null ||characteristicUUID==null|| mBluetoothGatt == null){
            LogUtils.e("read E : UUID为空不能读取！");
            setReadFailCallback(new CharacteristicReadException(BleException.ERROR_CODE_READ_UUID_NOPOINTER));
            return;
        }
        if (!checkConnected()) {
            LogUtils.e("readCharacteristic E : 设备未连接无法读取！");
            setWriteFailCallback(new CharacteristicWriteException(BleException.ERROR_CODE_BLE_NOT_CONNECT));
            return;
        }
//        List<BluetoothGattService> services = mBluetoothGatt.getServices();
//        LogUtils.i("read I : services="+services.toString());
        BluetoothGattService  mGattService = mBluetoothGatt.getService(serviceUUID);
        if (mGattService == null) {
            LogUtils.e("read E : 没有发现服务！");
            setReadFailCallback(new CharacteristicReadException(BleException.ERROR_CODE_READ_NOT_FOUND_SERVICE));
            return;
        }
        BluetoothGattCharacteristic mCharacteristic = mGattService.getCharacteristic(characteristicUUID);
        if (mCharacteristic == null) {
            LogUtils.e("read E : 没有发现特征！");
            setReadFailCallback(new CharacteristicReadException(BleException.ERROR_CODE_READ_NOT_FOUND_CHARACTERISTIC));
            return;
        }
        if (mBleGattCallback == null) {
            LogUtils.e("read E : 没有发现回调接口！");
            return;
        }
        if ((mCharacteristic.getProperties()& BluetoothGattCharacteristic.PROPERTY_READ) > 0){
            if (!mBluetoothGatt.readCharacteristic(mCharacteristic)) {
                LogUtils.e("read E : 读取操作失败！");
                setReadFailCallback(new CharacteristicReadException(BleException.ERROR_CODE_READ_OPERATION_FAILED));
            }
        } else {
            LogUtils.e("read E : 这个特征不支持阅读！");
            setReadFailCallback(new CharacteristicReadException(BleException.ERROR_CODE_READ_NOT_SUPPORT));
        }
    }

    //endregion =============================== 读特征 ==================================

    //region ================================== 写特征 ==================================

    //是否正在循环写入
    public boolean isPollWrite() {
        return isPollWrite;
    }


    /**
     * 通过消息队列写入特征
     * @param serviceUUID
     * @param characteristicUUID
     * @param fileSuffix
     * @param data
     * @param bleWriteListener
     */
    public synchronized void sendWriteMsg(UUID serviceUUID, UUID characteristicUUID, FileSuffix fileSuffix, byte[] data, BleWriteListener bleWriteListener) {
        if (mW2RHandler == null) {
            LogUtils.e("sendWriteMsg E : 没有初始化，请先调用 write2ReadThreadPrepare()");
            return;
        }
        BleWriteBean mBleWriteBean = new BleWriteBean(serviceUUID,characteristicUUID,fileSuffix,data,bleWriteListener);
        Message message = mW2RHandler.obtainMessage();
        message.what = MSG_WRITE_CHARACTERISTIC;
        message.obj = mBleWriteBean;
        mW2RHandler.sendMessage(message);
    }

    private void writeChangeThread(UUID serviceUUID, UUID characteristicUUID, FileSuffix fileSuffix, byte[] data, BleWriteListener bleWriteListener) {
        isWriteOrRead.set(true);
        if (Looper.myLooper() == Looper.getMainLooper()){
            writeCharacteristic(serviceUUID,characteristicUUID,fileSuffix,data,bleWriteListener);
        } else {
            ThreadUtils.mainHandler.post(()->{
                writeCharacteristic(serviceUUID,characteristicUUID,fileSuffix,data,bleWriteListener);
            });
        }
    }

    /**
     * 写入特征
     * @param serviceUUID
     * @param characteristicUUID
     * @param fileSuffix
     * @param data
     * @param bleWriteListener
     */
    public void writeCharacteristic(UUID serviceUUID, UUID characteristicUUID, FileSuffix fileSuffix, byte[] data, BleWriteListener bleWriteListener) {
        isWriteOrRead.set(true);
        mBleWriteListener = bleWriteListener;
        if (serviceUUID == null ||characteristicUUID==null|| mBluetoothGatt == null){
            LogUtils.e("read E : UUID|Gatt为空不能写入！");
            setWriteFailCallback(new CharacteristicWriteException(BleException.ERROR_CODE_READ_UUID_NOPOINTER));
            return;
        }
        if (!checkConnected()) {
            LogUtils.e("readCharacteristic E : 设备未连接无法写入！");
            setWriteFailCallback(new CharacteristicWriteException(BleException.ERROR_CODE_BLE_NOT_CONNECT));
            return;
        }
        BluetoothGattService  mGattService = mBluetoothGatt.getService(serviceUUID);
        if (mGattService == null) {
            LogUtils.e("read E : 没有发现服务！");
            setWriteFailCallback(new CharacteristicWriteException(BleException.ERROR_CODE_READ_NOT_FOUND_SERVICE));
            return;
        }
        mCharacteristic = mGattService.getCharacteristic(characteristicUUID);
        if (mCharacteristic == null) {
            LogUtils.e("read E : 没有发现特征！");
            setWriteFailCallback(new CharacteristicWriteException(BleException.ERROR_CODE_READ_NOT_FOUND_CHARACTERISTIC));
            return;
        }
        if (mBleGattCallback == null) {
            LogUtils.e("read E : 没有发现回调接口！");
            setWriteFailCallback(new CharacteristicWriteException(BleException.ERROR_CODE_WRITE_NOT_FOUND_CALLBACK));
            return;
        }
        //拼接头部
        data = BytesUtils.concat(spliceHead(data,fileSuffix), data);
        mDataBuffer= addByteBuffer(data,mBleGattCallback.getMtu()-3);
        mDataBuffer.offer(BLEConfig.BLUETOOTH_DATA_END.getBytes());//添加一个结束标记到缓冲区
        if (mDataBuffer.size()<2) {
            LogUtils.i("writeCharacteristic I : 一次性写入！");
            writeCharacteristic(mCharacteristic, mDataBuffer.poll());
        }else {
            //添加到缓冲区
            LogUtils.w("writeCharacteristic I : 准备分包写入,总次数="+mDataBuffer.size());
            isPollWrite = true;
            writeCharacteristic(mCharacteristic, mDataBuffer.poll());//写入第一行数据
        }
    }

    private void writeCharacteristic(BluetoothGattCharacteristic mCharacteristic,byte[] data) {
        LogUtils.i("writeCharacteristic  D : 写特征："+ ConvertUtils.bytes2HexString(data)+" len="+data.length+" loop:"+mDataBuffer.size());
        if (Looper.myLooper() != Looper.getMainLooper()) {
            LogUtils.w("writeCharacteristic  W : 当前不在主线程threadName="+Thread.currentThread().getName());
        }
        if (mCharacteristic.setValue(data)) {//特征赋值成功
            if (!mBluetoothGatt.writeCharacteristic(mCharacteristic)){
                //写出操作失败
                LogUtils.e("writeCharacteristic E : 写操作失败！");
                setWriteFailCallback(new CharacteristicWriteException(BleException.ERROR_CODE_WRITE_OPERATION_FAILED));
            }
        }else {//特征赋值失败
            LogUtils.e("writeCharacteristic E : 特征赋值失败！");
            setWriteFailCallback(new CharacteristicWriteException(BleException.ERROR_CODE_WRITE_VALUE_FAILED));
        }
    }
    //轮询缓冲区
    public void pollWriteCharacteristic() {
        if (mDataBuffer.peek() == null) {
            isPollWrite = false;
            setWriteSuccessCallback();
            return;
        }
        byte[] bytes = mDataBuffer.poll();//获取并移除队列头部
        mMainHandler.post(()->{
            writeCharacteristic(mCharacteristic, bytes);
        });
    }

    /**
     * 拼接数据头
     * @param data 数据头
     * @param fileSuffix 发生的文件类型
     * @return 数据头
     */
    private byte[] spliceHead(byte[] data,FileSuffix fileSuffix) {
        byte[] crc16 = new byte[2];
        long crcL = new CRC(CRC.Parameters.CRC16).calculateCRC(data);
        crc16[0] = new Long(crcL >> 8 & 0xff).byteValue();//crc
        crc16[1] = new Long(crcL & 0xff).byteValue();     //crc
        byte[] type= FileSuffix.suffixToBytes(fileSuffix);//文件类型
        LogUtils.i("spliceHead,数据头：" +ConvertUtils.bytes2HexString(crc16)+ ConvertUtils.bytes2HexString(type));
        return BytesUtils.concat(crc16,type);
    }

    private  Queue<byte[]> addByteBuffer(byte[] data, int count) {
        Queue<byte[]> byteQueue = new LinkedList<>();
        if (data != null) {
            int index = 0;
            do {
                byte[] rawData = new byte[data.length - index];
                byte[] newData;
                System.arraycopy(data, index, rawData, 0, data.length - index);
                if (rawData.length <= count) {
                    newData = new byte[rawData.length];
                    System.arraycopy(rawData, 0, newData, 0, rawData.length);
                    index += rawData.length;
                } else {
                    newData = new byte[count];
                    System.arraycopy(data, index, newData, 0, count);
                    index += count;
                }
                byteQueue.offer(newData);
            } while (index < data.length);
        }
        return byteQueue;
    }
    //endregion =============================== 写特征 ==================================

    //region ================================== 回调 ==================================

    //开始扫描
    private void setScanStartedCallback() {
        LogUtils.d("setScanStartedCallback  D 回调: 开始扫描！");
        if (mBleScanListener != null) {
            mMainHandler.post(()->{
                mBleScanListener.onScanStarted();
            });
        }
        isWriteOrRead.set(false);
    }
    //扫描中
    private void setScanningCallback(BleDevice device) {
        if (mBleScanListener != null) {
            mMainHandler.post(()->{
                mBleScanListener.onScanning(device);
            });
        }
    }
    //停止扫描
    private void setScanFinishedCallback() {
        if (isScanAutoConnect.get()) {
            //todo 没扫描到新设备扫描超时了
            isScanAutoConnect.set(false);//标志位状态切换
            LogUtils.d("setScanFinishedCallback  D : 名字扫描时超时了重连");
            autoConnect(mBleConnectListener);//名字扫描时超时了重连
            return;
        }
        if (mBleScanListener != null) {
            List<BleDevice> list= new ArrayList<>();
            if (mBleCacheMap != null) {
                for (Map.Entry<String, BleDevice> entry : mBleCacheMap.entrySet()) {
                    list.add(entry.getValue());
                }
            }
            mMainHandler.post(()->{
                LogUtils.d("setScanFinishedCallback  D 回调: 停止扫描！");
                mBleScanListener.onScanFinished(list);
            });
        }
    }
    //扫描失败
    public void setScanFailedCallback(BleException exception) {
        LogUtils.d("setScanFailedCallback  D 回调: 扫描失败"+exception);
        isScaning = false;
        if (mBleScanListener != null) {
            mMainHandler.post(()->{
                mBleScanListener.onScanFailed(exception);
            });
        }
    }


    //开始连接设备
    private void setStartConnectCallback(BluetoothDevice bleDevice) {
        isWriteOrRead.set(false);
        if (mBleConnectListener != null&&!isAutoState) {
            BleDevice device = mBleCacheMap.get(bleDevice.getAddress());
            mMainHandler.post(()->{
                mBleConnectListener.onStartConnect(device);
                LogUtils.d("setStartConnectCallback  D : 回调，开始连接设备"+bleDevice.getAddress());
            });
        }
    }
    //连接成功
    public void setConnectSuccessCallback(BluetoothDevice bleDevice) {
        mConnectionState =STATE_CONNECTED; //连接成功
        isAutoState = false;
        if (mBleConnectListener != null) {
            mMainHandler.postDelayed(()->{//一段延时时间后置为稳定状态，此状态才可进行其它操作
                //延时回调
                mBleDevice = mBleCacheMap.get(bleDevice.getAddress());
                LogUtils.d("setConnectSuccessCallback  D 回调: 连接成功"+mBleDevice.getAddress());
                mBleConnectListener.onConnectSuccess(mBleDevice);
            },mBleParamConfig.getConnectedDelay());
        }
    }
    //连接超时
    private void setConnectTimeOverCallback(BleException exception) {
        LogUtils.e("setConnectTimeOverCallback E 回调: 连接超时！");
        setConnectFailCallback(mBleDevice.getDevice(),exception);
    }
    //连接失败
    public void setConnectFailCallback(BluetoothDevice device, BleException exception) {
        LogUtils.e("setConnectFailCallback E : 回调，连接失败："+exception);
        mConnectionState = STATE_DISCONNECTED;//断开状态
        isAutoState = false;
        if (mBleConnectListener != null ) {
            mMainHandler.post(()->{
                BleDevice bleDevice = mBleCacheMap.get(device.getAddress());
                mBleConnectListener.onConnectFail(bleDevice, exception);
            });
        }
    }
    //连接中断
    public void setDisconnectedCallback(int status) {
        LogUtils.e("setDisconnectedCallback E 回调: 连接中断！");
        mConnectionState = STATE_DISCONNECTED;//连接中断
        isAutoState = false;
        if (mBleConnectListener != null) {
            mMainHandler.post(()->{
                mBleConnectListener.onDisconnected(status);
            });
        }
    }


    //mtu设置成功
    public void setMtuSuccessCallback(int mtu) {
        LogUtils.d("setMtuSuccessCallback  D 回调: mtu="+mtu);
        if (mBleMtuListener != null) {
            mMainHandler.post(()->{
                mBleMtuListener.onRequestMtuSuccess(mtu);
            });
        }
    }
    //mtu设置失败
    private void setMtuFailCallback(BleException exception) {
        LogUtils.e("setMtuFailCallback  D 回调: "+exception);
        if (mBleMtuListener != null) {
            mMainHandler.post(()->{
                mBleMtuListener.onRequestMtuFailure(exception);
            });
        }
    }


    //读取成功
    public void setReadSuccessCallback(byte[] value) {
        LogUtils.d("setReadSuccessCallback  D 回调: 读数据成功！");

        if (mBleReadListener != null) {
            mMainHandler.post(()->{
                mBleReadListener.onReadSuccess(new String(value));
            });
        }
        isWriteOrRead.set(false);
    }
    //读取失败
    public void setReadFailCallback(BleException exception) {
        LogUtils.d("setReadFailCallback  D 回调: 读数据失败！");

        if (mBleReadListener != null) {
            mMainHandler.post(()->{
            mBleReadListener.onReadFailure(exception);
            });
        }
        isWriteOrRead.set(false);
    }

    //写入成功
    public void setWriteSuccessCallback() {
        LogUtils.d("setWriteSuccessCallback  D 回调: 写入成功！");

        if (mBleWriteListener != null) {
            mMainHandler.post(()->{
                mBleWriteListener.onWriteSuccess();
            });
        }
        isWriteOrRead.set(false);
    }
    //写入失败
    private void setWriteFailCallback(BleException exception) {
        LogUtils.e("setWriteFailCallback E 回调: 写入失败"+exception);

        if (mBleWriteListener != null) {
            mMainHandler.post(()->{
                mBleWriteListener.onWriteFailure(exception);
            });
        }
        isWriteOrRead.set(false);
    }


    //notify开启成功
    public void setNotifySuccessCallback() {
        LogUtils.d("setNotifySuccessCallback  D : 回调，notify开启成功！");
        if (mBleNotifyListener != null) {
            mMainHandler.post(()->{
                mBleNotifyListener.onNotifySuccess();
            });
        }
    }
    //notify开启或关闭操作失败
    public void setNotifyFailCallback(BleException exception) {
        LogUtils.e("setNotifyFailCallback  D : 回调,"+exception);
        if (mBleNotifyListener != null) {
            mMainHandler.post(()->{
                mBleNotifyListener.onNotifyFailure(exception);
            });
        }
    }
    //收到消息
    public void setNotifyReceiveMsgCallback(byte[] data) {
        if (mBleNotifyListener != null) {
            mMainHandler.post(()->{
                mBleNotifyListener.onReceiveMsg(data);
            });
        }
    }
    //停止Notify
    public void setStopNotifyCallback() {
        LogUtils.d("setStopNotifyCallback  D : 回调, 停止Notify!");
        if (mBleNotifyListener != null) {
            mMainHandler.post(()->{
                mBleNotifyListener.onStopNotify();
            });
        }
    }
    //endregion =============================== 回调 ==================================

    //region ================================== 释放 ==================================
    //彻底销毁
    public synchronized void destroy() {
        closeBle();
        mBleDevice=null;
        mBluetoothGatt=null;
        mBluetoothLeScanner=null;
        mOldScanCallback=null;
        mBleScanCallback=null;
        mBleGattCallback=null;
        mBleScanListener =null;
        mBleConnectListener =null;
        mBleReadListener=null;
        mBleWriteListener=null;
        mBleMtuListener=null;
        mBleCacheMap.clear();//清空
        mBleCacheMap=null;

        mMainHandler.removeCallbacksAndMessages(null);
        mScanHandler.removeCallbacksAndMessages(null);
        if (mW2RHandler != null) {
            mW2RHandler.removeCallbacksAndMessages(null);
        }
    }

    //蓝牙关闭三步曲
    public synchronized void closeBle() {
        isWriteOrRead.set(false);
        disconnect();
        refreshBLECache();
        closeBleGatt();
        LogUtils.i("closeBle  D : 关闭Gatt！");
    }

    //断开已建立的连接，或者取消正在尝试的连接
    public synchronized void disconnect() {
        LogUtils.i("disconnect I : 断开连接！");
        if (mBluetoothGatt != null) {
            mBluetoothGatt.disconnect();
        }
        isWriteOrRead.set(false);
    }
    //清除内部缓存并强制从远程设备刷新服务。
    private synchronized void refreshBLECache() {
        try {
            final Method refresh = BluetoothGatt.class.getMethod("refresh");
            if (refresh != null && mBluetoothGatt != null) {
                boolean success = (Boolean) refresh.invoke(mBluetoothGatt);
                LogUtils.i("refreshDeviceCache I : refresh="+success);
            }
        } catch (Exception e) {
            LogUtils.i("refreshDeviceCache I : 刷新BLE时发生异常："+e.getMessage());
            e.printStackTrace();
        }
    }
    //关闭gatt
    private synchronized void closeBleGatt() {
        if (mBluetoothGatt != null) {
            mBluetoothGatt.close();
        }
    }

    //endregion =============================== 释放 ==================================

    //region ================================== 设置 ==================================

    //请求修改mtu
    public synchronized void setMtu(int mtu, BleMtuListener bleMtuListener) {
        LogUtils.d("setMtu  D : 设置mtu="+mtu);
        mBleMtuListener=bleMtuListener;
        if (mBluetoothGatt == null) {
            LogUtils.e("setMtu E : Gatt不能为空！");
            setMtuFailCallback(new RequestMtuException());
            return;
        }
        if (mtu < MTU_DEFAULT) {
            LogUtils.w("setMtu  W : mtu必须>23");
            setMtuFailCallback(new RequestMtuException());
            return;
        }
        if (mtu > MTU_MAX) {
            mtu = MTU_MAX;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (!mBluetoothGatt.requestMtu(mtu)) {
                LogUtils.e("setMtu E : 请求mtu失败！");
                setMtuFailCallback(new RequestMtuException());
            }
        } else {
            LogUtils.e("setMtu E : android系统版本<5.0,mtu请求失败！");
            setMtuFailCallback(new RequestMtuException());
        }
    }

    public void setNotify(UUID serviceUUID, UUID notifyUUID,boolean enable, BleNotifyListener bleNotifyListener) {
        if (bleNotifyListener != null) {
            mBleNotifyListener = bleNotifyListener;
        }
        if (serviceUUID == null ||notifyUUID==null|| mBluetoothGatt == null){
            LogUtils.e("setNotify E : UUID为空！");
            setNotifyFailCallback(new NotifyException(BleException.ERROR_CODE_READ_UUID_NOPOINTER));
            return;
        }
        if (!checkConnected()) {
            LogUtils.e("setNotify E : 设备未连接！");
            setNotifyFailCallback(new NotifyException(BleException.ERROR_CODE_BLE_NOT_CONNECT));
            return;
        }
        BluetoothGattService  mGattService = mBluetoothGatt.getService(serviceUUID);
        if (mGattService == null) {
            LogUtils.e("setNotify E : 没有发现服务！");
            setNotifyFailCallback(new NotifyException(BleException.ERROR_CODE_READ_NOT_FOUND_SERVICE));
            return;
        }
        BluetoothGattCharacteristic mCharacteristic = mGattService.getCharacteristic(notifyUUID);
        if (mCharacteristic == null) {
            LogUtils.e("setNotify E : 没有发现特征！");
            setNotifyFailCallback(new NotifyException(BleException.ERROR_CODE_READ_NOT_FOUND_CHARACTERISTIC));
            return;
        }
        if (mBleGattCallback == null) {
            LogUtils.e("setNotify E : 没有发现回调接口！");
            setNotifyFailCallback(new NotifyException(BleException.ERROR_CODE_WRITE_NOT_FOUND_CALLBACK));
            return;
        }
        if ((mCharacteristic.getProperties() | BluetoothGattCharacteristic.PROPERTY_NOTIFY) <= 0) {
            LogUtils.e("setNotify E : 该特征不支持notify！");
            setNotifyFailCallback(new NotifyException(BleException.ERROR_CODE_NOTIFY_NOT_SUPPORT));
            return;
        }
        boolean success = mBluetoothGatt.setCharacteristicNotification(mCharacteristic, enable);
        if (!success) {
            LogUtils.e("setNotify E : 设置失败！");
            setNotifyFailCallback(new NotifyException(BleException.ERROR_CODE_NOTIFY_OPERATION_FAILED));
            return;
        }
        BluetoothGattDescriptor descriptor = mCharacteristic.getDescriptor(BLEConfig.UUID_CLIENT_CHARACTERISTIC_CONFIG_DESCRIPTOR);
        if (descriptor == null) {
            LogUtils.e("setNotify E : 描述不存在！");
            setNotifyFailCallback(new NotifyException(BleException.ERROR_CODE_NOTIFY_DESCRIPTOR_NOT_FOUND));
            return;
        }

        descriptor.setValue(enable ? BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE : BluetoothGattDescriptor.DISABLE_NOTIFICATION_VALUE);
        if (!mBluetoothGatt.writeDescriptor(descriptor)) {
            LogUtils.e("setNotify E : 启用通知失败！");
            setNotifyFailCallback(new NotifyException(BleException.ERROR_CODE_NOTIFY_WRITEDESCRIPTOR_FAILED));
            return;
        }
    }
    //endregion =============================== 设置 ==================================

    //region ================================== 异步 ==================================
    //主线程
    private static  class MainHandler extends Handler{
        private final WeakReference<BleCore> mBleCoreWeak;

        public MainHandler(Looper looper,BleCore bleCore) {
            super(looper);
            mBleCoreWeak = new WeakReference<>(bleCore);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SCAN_TIME_OVER:
                    LogUtils.i("handleMessage  i : 收到消息，扫描时间到！");
                    mBleCoreWeak.get().stopScan();
                    break;
                case MSG_CONNECT_TIME_OVER:
                    LogUtils.w("handleMessage I :收到消息，连接超时！");
                    mBleCoreWeak.get().closeBle();//关闭蓝牙
                    if (mBleCoreWeak.get().isMaxCount()) {
                        mBleCoreWeak.get().setConnectTimeOverCallback(new ConnectTimeOverException());//回调
                    } else {
                        mBleCoreWeak.get().autoConnect();//重连
                    }
                    break;
                default:

                    break;
            }
        }
    }
    //扫描线程
    private static  class ScanHandler extends Handler {

        private final WeakReference<BleCore> mBleCoreWeak;

        ScanHandler(Looper looper, BleCore bleCore) {
            super(looper);
            mBleCoreWeak = new WeakReference<>(bleCore);
        }

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what){
                case MSG_SCAN_OLD_DEVICE: //老设备
                    BleDevice device = (BleDevice) msg.obj;
                    if (device != null&&!TextUtils.isEmpty(device.getAddress())){
                        mBleCoreWeak.get().cacheDevice(device);
                    }
                    break;
                case MSG_SCAN_LOLLIPOP_DEVICE://新设备
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                        ScanResult bleDevice = (ScanResult) msg.obj;
                        if (bleDevice != null&&!TextUtils.isEmpty(bleDevice.getDevice().getAddress())) {
                            mBleCoreWeak.get().cacheDevice(new BleDevice(bleDevice));
                        }
                    } else {
                        LogUtils.e("handleMessage E : 版本不兼容请调用 MSG_SCAN_OLD_DEVICE");
                    }
                    break;
            }
        }
    }
    //读写队列
    private static class W2RHandler extends Handler{
        private final WeakReference<BleCore> mBleCoreWeak;

        W2RHandler(Looper looper, BleCore bleCore) {
            super(looper);
            mBleCoreWeak = new WeakReference<>(bleCore);
        }

        @Override
        public void handleMessage(Message msg) {
            while (mBleCoreWeak.get().getIsWriteOrRead().get()){
                try {
                    LogUtils.i("handleMessage I : 读写排队...");
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }

            switch (msg.what) {
                case MSG_WRITE_CHARACTERISTIC://写
                    LogUtils.d("handleMessage  D : 收到消息，写数据！");
                    BleWriteBean parm = (BleWriteBean) msg.obj;
                    mBleCoreWeak.get().writeChangeThread(
                            parm.serviceUUID,
                            parm.characteristicUUID,
                            parm.fileSuffix,
                            parm.data,
                            parm.bleWriteListener);

                    break;
                case MSG_READ_CHARACTERISTIC://读
                    LogUtils.d("handleMessage  D : 收到消息，读数据！");
                    BleReadBean readParm = (BleReadBean) msg.obj;
                    mBleCoreWeak.get().readChangeThread(
                            readParm.serviceUUID,
                            readParm.characteristicUUID,
                            readParm.bleReadListener);

                    break;
                default:

                    break;
            }
        }
    }

    //发送延时消息
    private void sendMsgDelayed(int what,int time) {
        Message message = mMainHandler.obtainMessage();
        message.what=what;
        mMainHandler.sendMessageDelayed(message,time);
    }
    //移除延时消息
    public void removeMsg(int what) {
        if (mMainHandler != null) {
            mMainHandler.removeMessages(what);
        }
    }

    public Message getScanMessage() {
        return mScanHandler.obtainMessage();
    }
    //发送扫描时回调MSG到
    public void sendScanMsg(Message message) {
        if (mScanHandler != null&&message!=null) {
            mScanHandler.sendMessage(message);
        }
    }

    //endregion =============================== 异步 ==================================

}
