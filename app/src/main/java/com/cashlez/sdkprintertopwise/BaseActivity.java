package com.cashlez.sdkprintertopwise;

import android.app.Activity;
import android.os.Bundle;
import android.os.Message;
import android.view.KeyEvent;

public class BaseActivity extends Activity {
	
	public ActivityManager activityManager;
	private DynamicPermissionTool permissionTool;
	private final int    REQUEST_CODE1          = 0;

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		activityManager = ActivityManager.getActivityManager();
		activityManager.addActivity(this);

		permissionTool = new DynamicPermissionTool(this);
		requestPermission();
	}


	private void requestPermission() {
		boolean isAllGranted = permissionTool.isAllPermissionGranted(permissionTool.permissions);

		if (!isAllGranted) {
			String[] deniedPermissions = permissionTool.getDeniedPermissions(permissionTool.permissions);
			permissionTool.requestNecessaryPermissions(this, deniedPermissions, REQUEST_CODE1);
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		switch (requestCode) {
			case REQUEST_CODE1:
				boolean isAllGranted = permissionTool.isAllPermissionGranted(grantResults);
				if (!isAllGranted) {
					finish();
				}
				break;
			default:
				break;
		}
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
	}

	public Message createMessage(String data){
		Message msg = new Message();
		Bundle bundle = new Bundle();
		bundle.putString("data",data);
		msg.setData(bundle);
		return msg;
	}
	
	// 返回主页面
	public void retMain() {
		activityManager.removeAllActivityExceptOne(MainActivity.class);
	}

	@Override
	public void onAttachedToWindow() {
		super.onAttachedToWindow();
	}
	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_HOME) {
			return true;
		}
		return super.onKeyDown(keyCode, event);
	}
}
