package com.hubin.bleclient;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.hubin.bleclient.ble.BleCore.BleDevice;
import com.hubin.bleclient.ble.bleConfig.BLEConfig;
import com.hubin.bleclient.ble.bleConfig.GetParam;
import com.hubin.bleclient.ble.bleException.BleException;
import com.hubin.bleclient.ble.bleInterface.BleMtuListener;
import com.hubin.bleclient.ble.bleInterface.BleNotifyListener;
import com.hubin.bleclient.ble.bleInterface.BleReadListener;
import com.hubin.bleclient.ble.bleInterface.BleWriteListener;
import com.hubin.bleclient.ble.blecontrol.BleClientHelper;
import com.hubin.bleclient.utils.ConvertUtils;
import com.hubin.bleclient.utils.LogUtils;
import com.hubin.bleclient.utils.ToastUtils;

import java.util.Arrays;

public class BleDataActivity extends AppCompatActivity {

    private TextView mShowText;
    private StringBuffer mStringBuffer = new StringBuffer();
    private BleDevice mBleDevice;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ble_data);
        initView();  //初始化视图
        initData();  //初始化数据

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
//        BleClientHelper.disconnected();
    }

    private void initView() {
        mShowText = findViewById(R.id.bledata_read_text);
        mShowText.setMovementMethod(ScrollingMovementMethod.getInstance());
    }

    private void initData() {

        mBleDevice = BleClientHelper.getDevice();
        ToastUtils.toast("连接成功：" + mBleDevice.getName());
        showText("连接成功：" + mBleDevice.getName());
//        setTitle(mBleDevice.getAddress()+"   rssi:"+mBleDevice.getRssi() +" 定位："+ RssiUtil.getDistance(mBleDevice.getRssi()));
        setTitle(mBleDevice.getAddress());
    }

    /**
     * 发送字符串
     * @param view
     */
    public void sendString(View view) {
//        BleClientManager.getInstance().sendCode("hello_寒武纪！",null);
        String data = "你好旱地！旱地你好!";
        BleClientHelper.sendString(data, new BleWriteListener() {
            @Override
            public void onWriteSuccess() {
                LogUtils.d("onReadSuccess  D : 发送成功："+data);
                showText("发送:"+data+" len="+data.getBytes().length);
            }

            @Override
            public void onWriteFailure(BleException exception) {
                LogUtils.e("onWriteFailure E : 发送失败"+exception.toString());
                ToastUtils.toast(exception.toString());
            }
        });
    }


    /**
     * 读取Token
     * @param view
     */
    public void readToken(View view) {
        BleClientHelper.readString(GetParam.TOKEN,mBleReadListener);
    }
    /**
     * 读取petid
     * @param view
     */
    public void readPetid(View view) {
        BleClientHelper.readString(GetParam.PETID,mBleReadListener);
    }

    /**
     * 读取Token和Petid
     * @param view
     */
    public void readTokenPetid(View view) {
        BleClientHelper.readString(GetParam.PETID_TOKEN,mBleReadListener);
    }

    /**
     * 拓展mtu
     * @param view
     */
    public void setMtu(View view) {
        //设置MTU，需要在设备连接之后进行操作。
        //默认每一个BLE设备都必须支持的MTU为23。
        //MTU为23，表示最多可以发送20个字节的数据。
        //在Android 低版本(API-17 到 API-20)上，没有这个限制。所以只有在API21以上的设备，才会有拓展MTU这个需求。
        //该方法的参数mtu，最小设置为23，最大设置为512。
        //并不是每台设备都支持拓展MTU，需要通讯双方都支持才行，也就是说，需要设备硬件也支持拓展MTU该方法才会起效果。调用该方法后，可以通过onMtuChanged(int mtu)
        // 查看最终设置完后，设备的最大传输单元被拓展到多少。如果设备不支持，可能无论设置多少，最终的mtu还是23。
      BleClientHelper.setMtu(512, new BleMtuListener() {
          @Override
          public void onRequestMtuSuccess(int mtu) {
              LogUtils.i("onMtuChanged: " + mtu);
              showText("设置成功mtu="+mtu);
          }

          @Override
          public void onRequestMtuFailure(BleException exception) {
              LogUtils.e("onsetMTUFailure:" + exception.toString());
              showText("mtu设置失败！");
          }
      });
    }
    /**
     * 订阅notify
     * @param view
     */
    public void notifyBtn(View view) {
        BleClientHelper.setNotify(new BleNotifyListener() {
            @Override
            public void onNotifySuccess() {
                // 打开通知操作成功
                LogUtils.d("onNotifySuccess  D : 打开通知操作成功");
                showText("打开Notify！");
            }

            @Override
            public void onNotifyFailure(BleException exception) {
                // 打开通知操作失败
                LogUtils.d("onNotifyFailure  D : 打开通知操作失败!");
                showText("打开Notify失败！");
            }

            @Override
            public void onReceiveMsg(byte[] data) {
                // 打开通知后，设备发过来的数据将在这里出现
                if (Arrays.equals(data, BLEConfig.MSG_NOTIFY_CLOSE)) {
                    LogUtils.w("onReceiveMsg  D : 收到断开指令:"+ ConvertUtils.bytes2HexString(data));
                    BleClientHelper.closeBle();
                    finish();
                    ToastUtils.toast("收到断开指令!");

                } else {
                    String str = new String(data);
                    LogUtils.d("onReceiveMsg  D : 收到:"+str);
                    showText("收到："+str+" len="+data.length);
                }
            }

            @Override
            public void onStopNotify() {
                LogUtils.d("onStopNotify  D : 停止Notify！");
                showText("停止Notify！");
            }
        });
    }

    /**
     * 停止Notify
     * @param view
     */
    public void stopNotifyBtn(View view) {
        BleClientHelper.stopNotify();
    }

    /**
     * 订阅indicate
     * @param view
     */
    public void indicateBtn(View view) {
    }

    /**
     * 停止Indicate
     * @param view
     */
    public void stopIndicateBtn(View view) {
//        BleClientManager.getInstance().stopIndicate();
    }


    /**
     * 显示内容到界面
     * @param msg
     */
    private void showText(String msg) {
        if (mStringBuffer.length() >800) {
            mStringBuffer.delete(0, mStringBuffer.length());
        }
        mStringBuffer.append(msg+"\r\n");
        mShowText.setText(mStringBuffer.toString());
    }



    private BleReadListener mBleReadListener = new BleReadListener() {
        /**
         * 读取特征成功
         *
         * @param text
         */
        @Override
        public void onReadSuccess(String text) {
            showText("收到:"+text+" len="+text.getBytes().length);
        }

        /**
         * 读取特征失败
         *
         * @param exception
         */
        @Override
        public void onReadFailure(com.hubin.bleclient.ble.bleException.BleException exception) {
            LogUtils.e("onWriteFailure E : 读取失败"+exception.toString());
            ToastUtils.toast(exception.toString());
        }
    };

}
