package com.hubin.bledemo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.method.ScrollingMovementMethod;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;

import com.hubin.bledemo.utils.ToastUtils;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

public class MainActivity extends AppCompatActivity {

    private Intent mIntent;
    private StringBuffer mStringBuffer = new StringBuffer();
    private TextView mTextView;
    private TextView mMacText;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mTextView = findViewById(R.id.main_text);
        mMacText = findViewById(R.id.main_mac_text);
        mTextView.setMovementMethod(ScrollingMovementMethod.getInstance());
        //注册EventBus
        EventBus.getDefault().register(this);

        mIntent = new Intent(this,BleServer.class);
        startService(mIntent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopService(mIntent);
        EventBus.getDefault().unregister(this);//注销EventBus
    }

    //EventBus注解 表示回调 参数要与发出的类型相同
    @Subscribe(threadMode = ThreadMode.MAIN)
    public void onBleServerCallback(String msg) {
        if (msg == null||msg.equals("notify")) {
            return;
        }
        if (msg.startsWith("当前连接:")) {
            mMacText.setText(msg);
            return;
        }
        ToastUtils.toast(msg);
        showText(msg);
    }

    public void showText(String msg) {
        if (mStringBuffer.length() > 500) {
            mStringBuffer.delete(0, mStringBuffer.length());
        }
        mStringBuffer.append(msg+"\r\n");
        mTextView.setText(mStringBuffer.toString());
    }

    /**
     * 发送通知
     * @param view
     */
    public void sendNotify(View view) {
        EventBus.getDefault().post("notify");
    }
}
