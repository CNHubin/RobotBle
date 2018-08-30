package com.hubin.bleclient;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

import com.hubin.bleclient.adapter.MainBleAdapter;
import com.hubin.bleclient.ble.BleCore.BleDevice;
import com.hubin.bleclient.ble.bleException.BleException;
import com.hubin.bleclient.ble.bleInterface.BleConnectListener;
import com.hubin.bleclient.ble.bleInterface.BleScanListener;
import com.hubin.bleclient.ble.blecontrol.BleClientHelper;
import com.hubin.bleclient.utils.LogUtils;
import com.hubin.bleclient.utils.ThreadUtils;
import com.hubin.bleclient.utils.ToastUtils;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity  {
    public static final int REQUEST_CODE_OPEN_GPS = 1;
    public static final int REQUEST_CODE_PERMISSION_LOCATION = 2;
    public static final int REQUEST_ENABLE_BT = 3;
    private Button mOpenBleBtn;
    private Button mScanBleBtn;
    private ListView mListview;
    private MainBleAdapter mMainBleAdapter;
    private List<BleDevice> mList;
    private Button mStopScanBleBtn;
    private Button mcloseBleBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initListener();

        if (BleClientHelper.checkBleConnected()) {
            Intent mIntent = new Intent(MainActivity.this, BleDataActivity.class);
            startActivity(mIntent);
        }
    }

    private void initView() {
        mOpenBleBtn = findViewById(R.id.main_btn01);
        mScanBleBtn = findViewById(R.id.main_btn02);
        mStopScanBleBtn = findViewById(R.id.main_btn03);
        mcloseBleBtn = findViewById(R.id.main_btn04);
        mListview = findViewById(R.id.main_listview);
    }

    private void initData() {
        //操作蓝牙
        mList = new ArrayList<>();
        mMainBleAdapter = new MainBleAdapter(this,mList);
        mListview.setAdapter(mMainBleAdapter);

    }
    private void initListener() {
        mOpenBleBtn.setOnClickListener(mOnClickListener);
        mScanBleBtn.setOnClickListener(mOnClickListener);
        mStopScanBleBtn.setOnClickListener(mOnClickListener);
        mcloseBleBtn.setOnClickListener(mOnClickListener);

        mListview.setOnItemClickListener(mOnItemClickListener);
    }


    //ListView条目点击监听
    private AdapterView.OnItemClickListener mOnItemClickListener=new AdapterView.OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//            final BleDevice bleDevice = mList.get(position);
//            BleClientHelper.connect(bleDevice, mBleConnectListener);
            //开始连接设备

            final BleDevice bleResult = mList.get(position);
            BleClientHelper.connect(bleResult, mBleConnectListener);
        }
    };

    private BleConnectListener mBleConnectListener = new BleConnectListener() {
        /**
         * BLE正在建立连接
         *
         * @param bleDevice
         */
        @Override
        public void onStartConnect(BleDevice bleDevice) {
            LogUtils.d("onStartConnect  D : 开始连接设备！");
        }

        /**
         * BLE连接成功
         *
         * @param bleDevice
         */
        @Override
        public void onConnectSuccess(BleDevice bleDevice) {
            LogUtils.d("onConnectSuccess  D : 连接成功:"+bleDevice.getName());
            Intent mIntent = new Intent(MainActivity.this,BleDataActivity.class);
//            mIntent.putExtra("device",bleDevice);
            startActivity(mIntent);
        }

        /**
         * BLE连接失败
         *
         * @param bleDevice
         * @param exception
         */
        @Override
        public void onConnectFail(BleDevice bleDevice, BleException exception) {
            LogUtils.e("onConnectFail  D : 连接失败："+bleDevice.getName()+" "+exception);
            ToastUtils.toast("连接失败请重试！");
        }

        @Override
        public void onDisconnected(int status) {
            LogUtils.e("onDisconnected E : 连接中断！"+status);
            ToastUtils.toast("连接中断！");
        }
    };

    //Button点击监听
    private View.OnClickListener mOnClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.main_btn01:
                    if (!BleClientHelper.checkBleConnected()) {
                        checkPermissions();
                    }
                    break;
                case R.id.main_btn02:
                    bleScan();
                    break;
                case R.id.main_btn03://停止扫描
                    BleClientHelper.cancelScan();
                    break;
                case R.id.main_btn04://断开当前
                    BleClientHelper.closeBle();
                    break;
                default:

                    break;
            }
        }
    };


    private void connectBleDialog() {

        final ViewGroup content = findViewById(android.R.id.content);
        final View view = LayoutInflater.from(this).inflate(R.layout.layout_connect_ble_view, null);
        TextView exitText = view.findViewById(R.id.tv_con_ble_exit);
        TextView sureText = view.findViewById(R.id.tv_con_ble_sure);

        exitText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content.removeView(view);
            }
        });

        sureText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                content.removeView(view);
                Intent enableBtIntent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivityForResult(enableBtIntent,REQUEST_ENABLE_BT);
            }
        });

        view.setOnTouchListener(new View.OnTouchListener() {
            @SuppressLint("ClickableViewAccessibility")
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return true;
            }
        });

        content.addView(view);
    }


    public void bleScan(){
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            connectBleDialog();
        } else {
            BleClientHelper.scan(new BleScanListener() {
                @Override
                public void onScanStarted() {
                    LogUtils.d("onScanStarted  D : 开始扫描！");
                    ThreadUtils.mainHandler.post(()->{
                        mList.clear();
                        mMainBleAdapter.setList(mList);
                    });
                }

                @Override
                public void onScanning(BleDevice bleDevice) {
                    LogUtils.d("onScanning  D : BLE正在扫描  Name="+bleDevice.getName()+" Mac="+bleDevice);
                    ThreadUtils.mainHandler.post(()->{
                        mList.add(bleDevice);
                        mMainBleAdapter.setList(mList);
                    });
                }

                @Override
                public void onScanFinished(List<BleDevice> deviceList) {
                    LogUtils.d("onScanFinished  D : 扫描完成！");
//                    mMainBleAdapter.setList(scanResultList);
                    ToastUtils.toast(MainActivity.this, "停止扫描！");
                }

                @Override
                public void onScanFailed(BleException exception) {
                    LogUtils.e("onScanFailed E : 扫描异常"+exception);

                }

            });
        }
    }


    //region ================================== 权限检查 ==================================

    private void checkPermissions() {
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            connectBleDialog();
            return;
        }

        String[] permissions = {Manifest.permission.ACCESS_FINE_LOCATION};
        List<String> permissionDeniedList = new ArrayList<>();
        for (String permission : permissions) {
            int permissionCheck = ContextCompat.checkSelfPermission(this, permission);
            if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
                onPermissionGranted(permission);
            } else {
                permissionDeniedList.add(permission);
            }
        }
        if (!permissionDeniedList.isEmpty()) {
            String[] deniedPermissions = permissionDeniedList.toArray(new String[permissionDeniedList.size()]);
            ActivityCompat.requestPermissions(this, deniedPermissions, REQUEST_CODE_PERMISSION_LOCATION);
        }
    }

    private void onPermissionGranted(String permission) {
        switch (permission) {
            case Manifest.permission.ACCESS_FINE_LOCATION:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !checkGPSIsOpen()) {
                    new AlertDialog.Builder(this)
                            .setTitle("提示")
                            .setMessage("当前手机扫描蓝牙需要打开定位功能")
                            .setPositiveButton("前往设置",
                                    new DialogInterface.OnClickListener() {
                                        @Override
                                        public void onClick(DialogInterface dialog, int which) {
                                            Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                                            startActivityForResult(intent, REQUEST_CODE_OPEN_GPS);
                                        }
                                    })

                            .setCancelable(false)
                            .show();
                }
                break;
            default:
                break;
        }
    }
    private boolean checkGPSIsOpen() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if (locationManager == null)
            return false;
        return locationManager.isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
    }

    //endregion =============================== 权限检查 ==================================


    @Override
    protected void onDestroy() {
//        ToastUtils.toast(this, "断开蓝牙！");
        BleClientHelper.destroy();
        super.onDestroy();
    }
}
