package com.reformer.wifitest;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.RotateAnimation;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Toast;
import com.reformer.wifitest.view.IOSSwitchView;

public class MainActivity extends Activity {
	private WifiManager wifiManager = null;
	private Context context = null;
	public SetWifiHandler setWifiHandler;
	private WifiRelayListAdapter wifiListAdapter;
	private ListView wifi_list;
	private ImageButton refresh_list_btn;
	private IOSSwitchView wifi_on_off_btn;

	private LinkWifi linkWifi;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.layout_setwifirelay);
		context = this;

		linkWifi = new LinkWifi(context);

		wifiManager = (WifiManager) context
				.getSystemService(Service.WIFI_SERVICE);

		setWifiHandler = new SetWifiHandler(Looper.getMainLooper());

		wifi_list = (ListView) findViewById(R.id.wifi_list);
		refresh_list_btn = (ImageButton) findViewById(R.id.refresh_list_btn);
		wifi_on_off_btn = (IOSSwitchView) findViewById(R.id.wifi_on_off_btn);

		playRotateAnimation(refresh_list_btn, 1500, 4,
				Animation.RESTART, -1);

		refresh_list_btn.setOnClickListener(new OnClickListener() {
			public void onClick(View arg0) {
				// 刷新wifi列表
				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						playRotateAnimation(refresh_list_btn, 1500, 4,
								Animation.RESTART, -1);
						scanAndGetResult();
					}
				});
			}
		});

		if (linkWifi.checkWifiState()) {
			wifi_on_off_btn.setState(true);
		} else {
			wifi_on_off_btn.setState(false);
		}

		wifi_on_off_btn.setSwitchListener(new IOSSwitchView.OnSwitchListener() {
			@Override
			public void onSwitch(View view, boolean isSwitchOn) {
				wifiManager.setWifiEnabled(!linkWifi.checkWifiState());
				wifi_on_off_btn.setEnabled(false);
			}
		});


		regWifiReceiver();
		scanAndGetResult();
	}

	public static void playRotateAnimation(View v, long durationMillis, int repeatCount, int repeatMode, int dir) {
		AnimationSet animationSet = new AnimationSet(true);
		RotateAnimation rotateAnimation = new RotateAnimation(0.0F, (float)dir * 360.0F, 1, 0.5F, 1, 0.5F);
		rotateAnimation.setDuration(durationMillis);
		rotateAnimation.setRepeatCount(repeatCount);
		rotateAnimation.setFillAfter(true);
		animationSet.addAnimation(rotateAnimation);
		v.startAnimation(animationSet);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {

		return false;
	}

	private void regWifiReceiver() {
		IntentFilter labelIntentFilter = new IntentFilter();
		labelIntentFilter.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
		labelIntentFilter.addAction("android.net.wifi.STATE_CHANGE"); // ConnectivityManager.CONNECTIVITY_ACTION);
		labelIntentFilter.setPriority(1000); // 设置优先级，最高为1000
		context.registerReceiver(wifiResultChange, labelIntentFilter);

	}

	private void scanAndGetResult() {
		// 开始扫描
		context.unregisterReceiver(wifiResultChange);
		regWifiReceiver();
		wifiManager.startScan();
	}


	private final BroadcastReceiver wifiResultChange = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {

			String action = intent.getAction();

			if (action.equals(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)) {
				System.out.println("wifi列表刷新了");
				showWifiList();
			} else if (action.equals("android.net.wifi.STATE_CHANGE")) {
				System.out.println("wifi状态发生了变化");
				// 刷新状态显示
				showWifiList();
				wifi_on_off_btn.setEnabled(true);
			}
		}
	};

	private void showWifiList() {
		// 剔除ssid中的重复项，只保留相同ssid中信号最强的哪一个
		List<ScanResult> wifiList = wifiManager.getScanResults();
		List<ScanResult> newWifList = new ArrayList<ScanResult>();
		boolean isAdd = true;

		if (wifiList != null) {
			for (int i = 0; i < wifiList.size(); i++) {
				isAdd = true;
				for (int j = 0; j < newWifList.size(); j++) {
					if (newWifList.get(j).SSID.equals(wifiList.get(i).SSID)) {
						isAdd = false;
						if (newWifList.get(j).level < wifiList.get(i).level) {
							// ssid相同且新的信号更强
							newWifList.remove(j);
							newWifList.add(wifiList.get(i));
							break;
						}
					}
				}
				if (isAdd)
					newWifList.add(wifiList.get(i));
			}
		}

		wifiListAdapter = new WifiRelayListAdapter(context, newWifList,
				setWifiHandler);
		wifi_list.setAdapter(wifiListAdapter);
	}

	public class SetWifiHandler extends Handler {
		public SetWifiHandler(Looper mainLooper) {
			super(mainLooper);
		}

		public void handleMessage(Message msg) {
			switch (msg.what) {
			case 0:// 请求操作某一无线网络
				ScanResult wifiinfo = (ScanResult) msg.obj;
				configWifiRelay(wifiinfo);
				break;

			}
		}
	}

	private void configWifiRelay(final ScanResult wifiinfo) {

		// 如果本机已经配置过的话
		if (linkWifi.IsExsits(wifiinfo.SSID) != null) {
			final int netID = linkWifi.IsExsits(wifiinfo.SSID).networkId;

			String actionStr;
			// 如果目前连接了此网络
			if (wifiManager.getConnectionInfo().getNetworkId() == netID) {
				actionStr = "断开";
			} else {
				actionStr = "连接";
			}
			System.out
					.println("wifiManager.getConnectionInfo().getNetworkId()="
							+ wifiManager.getConnectionInfo().getNetworkId());

			new AlertDialog.Builder(context)
					.setTitle("提示")
					.setMessage("请选择你要进行的操作？")
					.setPositiveButton(actionStr,
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									if (wifiManager.getConnectionInfo()
											.getNetworkId() == netID) {
										wifiManager.disconnect();
									} else {
										WifiConfiguration config = linkWifi
												.IsExsits(wifiinfo.SSID);
										linkWifi.setMaxPriority(config);
										linkWifi.ConnectToNetID(config.networkId);
									}

								}
							})
					.setNeutralButton("忘记",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									wifiManager.removeNetwork(netID);
									return;
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									return;
								}
							}).show();

			return;
		}

		String capabilities = "";

		if (wifiinfo.capabilities.contains("WPA2-PSK")) {
			// WPA-PSK加密
			capabilities = "psk2";
		} else if (wifiinfo.capabilities.contains("WPA-PSK")) {
			// WPA-PSK加密
			capabilities = "psk";
		} else if (wifiinfo.capabilities.contains("WPA-EAP")) {
			// WPA-EAP加密
			capabilities = "eap";
		} else if (wifiinfo.capabilities.contains("WEP")) {
			// WEP加密
			capabilities = "wep";
		} else {
			// 无密码
			capabilities = "";
		}

		if (!capabilities.equals("")) {
			// 有密码，提示输入密码进行连接

			LayoutInflater factory = LayoutInflater.from(context);
			final View inputPwdView = factory.inflate(R.layout.dialog_inputpwd,
					null);
			new AlertDialog.Builder(context)
					.setTitle("请输入该无线的连接密码")
					.setMessage("无线SSID：" + wifiinfo.SSID)
					.setIcon(android.R.drawable.ic_dialog_info)
					.setView(inputPwdView)
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									EditText pwd = (EditText) inputPwdView
											.findViewById(R.id.etPassWord);
									String wifipwd = pwd.getText().toString();

									// 此处加入连接wifi代码
									int netID = linkWifi.CreateWifiInfo2(
											wifiinfo, wifipwd);

									linkWifi.ConnectToNetID(netID);
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								@Override
								public void onClick(DialogInterface dialog,
										int which) {
								}
							}).setCancelable(false).show();

		} else {
			// 无密码
			new AlertDialog.Builder(context)
					.setTitle("提示")
					.setMessage("你选择的wifi无密码，可能不安全，确定继续连接？")
					.setPositiveButton("确定",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

									// 此处加入连接wifi代码
									int netID = linkWifi.CreateWifiInfo2(
											wifiinfo, "");

									linkWifi.ConnectToNetID(netID);
								}
							})
					.setNegativeButton("取消",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {
									return;
								}
							}).show();

		}

	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		context.unregisterReceiver(wifiResultChange); // 注销此广播接收器
	}

}
