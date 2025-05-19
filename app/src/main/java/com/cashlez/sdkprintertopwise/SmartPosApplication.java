package com.cashlez.sdkprintertopwise;

import android.app.Application;
import android.content.Context;

public class SmartPosApplication extends Application {

    private static Context context;

	@Override
	public void onCreate() {
		super.onCreate();
		DeviceServiceManager.getInstance().bindDeviceService(this);
		context = this;
	}

	@Override
	public void onTerminate() {
		super.onTerminate();
		DeviceServiceManager.getInstance().unBindDeviceService();
	}

    public static Context getContext() {
        return context;
    }
}
