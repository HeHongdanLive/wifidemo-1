package com.reformer.wifitest;

import android.app.Activity;
import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import cn.com.reformer.wifikey.OnCompletedListener;
import cn.com.reformer.wifikey.RfWifiKey;

import java.util.LinkedList;


/**
 * Created by Administrator on 2015-09-08.
 */
public class ConnectActivity extends Activity {
    private Button mBtn_back;
    private Button mBtn_editConfig;
    private Button mBtn_openDoor;
    private EditText mEt_wifiName;
    private EditText mEt_wifiPassword;
    private EditText mEt_wifiOutputTime;
    private TextView mTv_config;
    private TextView mTv_result1;
    private RfWifiKey mRfWifiKey;
    private Handler mHandler;
    private String resultInfo1 = "";
    private static final String mDevPassword = "31313131313131313131313131313131";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.layout_connect);

        mHandler = new Handler();
        mEt_wifiName = (EditText)findViewById(R.id.et_name);
        mEt_wifiPassword= (EditText)findViewById(R.id.et_password);
        mEt_wifiOutputTime = (EditText)findViewById(R.id.et_outputTime);
        mTv_result1 =(TextView)findViewById(R.id.tv_result1);
        mTv_config = (TextView)findViewById(R.id.tv_config);
        mRfWifiKey = RfWifiKey.getKey();
        mRfWifiKey.setOnCompletedListener(new OnCompletedListener() {
            @Override
            public void OnCompleted(int i) {
                switch (i) {
                    case 0:
                        resultInfo1 = "开门成功";
                        break;
                    case 1:
                        resultInfo1 = "开门密码错误";
                        break;
                    case 2:
                        resultInfo1 = "通讯应答错误";
                        break;
                    case 3:
                        resultInfo1 = "通讯超时";
                        break;
                    case 4:
                        resultInfo1 = "发生异常";
                        break;
                }
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mTv_result1.setText(resultInfo1);
                    }
                });
            }
        });
        mBtn_back = (Button)findViewById(R.id.btn_menu_back);
        mBtn_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
        mBtn_editConfig = (Button)findViewById(R.id.btn_editConfig);
        mBtn_editConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mEt_wifiName.getText().toString().trim().equals("")||mEt_wifiPassword.getText().toString().length()<8){
                    resultInfo1 = "名称错误或密码长度不够8位";
                    mTv_result1.setText(resultInfo1);
                    return;
                }
               int ret = mRfWifiKey.editConfig(ConnectActivity.this,mEt_wifiName.getText().toString(),mEt_wifiPassword.getText().toString());
                if (ret != 0) {
                    resultInfo1 = "AP配置信息有误";
                    mTv_result1.setText(resultInfo1);
                }else{
                    resultInfo1 = "配置信息成功";
                    mTv_result1.setText(resultInfo1);
                    mHandler.postDelayed(quitRunnable,1000);
                }
            }
        });
        mBtn_openDoor = (Button)findViewById(R.id.btn_opendoor);
        mBtn_openDoor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int outputTime = 30;
                if (!mEt_wifiOutputTime.getText().toString().equals("")) {
                    outputTime = Integer.parseInt(mEt_wifiOutputTime.getText().toString());
                }
                int ret = mRfWifiKey.openDoor(ConnectActivity.this, outputTime, mDevPassword);
                if (ret == 0) {
                    resultInfo1 = "开门中...";
                }else if (ret == 1){
                    resultInfo1 = "wifi未打开";
                }else if (ret == 2){
                    resultInfo1 = "正在连接通讯...";
                }else if (ret == 3){
                    resultInfo1 = "未初始化...";
                }
                mTv_result1.setText(resultInfo1);
//
            }
        });
        WifiManager wifiManager = (WifiManager) ConnectActivity.this.getSystemService(Context.WIFI_SERVICE);
        WifiInfo info = wifiManager.getConnectionInfo();
        StringBuffer sb = new StringBuffer(256);
        sb.append("SSID: ");
        sb.append(info.getSSID());
        sb.append("\nBSSID: ");
        sb.append(info.getBSSID());
        sb.append("\nMAC: ");
        sb.append(info.getMacAddress());
        sb.append("\nSupplicant State: ");
        sb.append(info.getSupplicantState());
        sb.append("\nLink Speed: ");
        sb.append(info.getLinkSpeed());
        sb.append("\nNet id: ");
        sb.append(info.getNetworkId());
        sb.append("\nDescribe Contents: ");
        sb.append(info.describeContents());
        sb.append("\nRssi: ");
        sb.append(info.getRssi());
        mTv_config.setText(sb.toString());
    }

    private Runnable quitRunnable = new Runnable() {
        @Override
        public void run() {
            onBackPressed();
        }
    };
}
