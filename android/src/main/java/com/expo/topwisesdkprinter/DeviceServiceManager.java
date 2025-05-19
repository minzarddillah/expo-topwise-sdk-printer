package com.expo.topwisesdkprinter;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.util.Log;

import com.topwise.cloudpos.aidl.AidlDeviceService;
import com.topwise.cloudpos.aidl.printer.AidlPrinter;

/**
 * Device service manager for Topwise SDK
 * This class handles the connection to the Topwise device service
 */
public class DeviceServiceManager {
    private static final String TAG = "DeviceServiceManager";
    private static final String ACTION_DEVICE_SERVICE = "com.topwise.cloudpos.device_service";
    
    private static DeviceServiceManager mInstance;
    private AidlDeviceService mDeviceService;
    private Context mContext;
    private boolean mBound = false;
    
    private DeviceServiceManager() {
    }
    
    public static DeviceServiceManager getInstance() {
        if (mInstance == null) {
            mInstance = new DeviceServiceManager();
        }
        return mInstance;
    }
    
    /**
     * Initialize the device manager with a context
     * @param context Application context
     */
    public void init(Context context) {
        this.mContext = context;
        bindDeviceService();
    }
    
    /**
     * Bind to the Topwise device service
     */
    private void bindDeviceService() {
        if (mContext == null) {
            Log.e(TAG, "Context is null. Cannot bind device service.");
            return;
        }
        
        Intent intent = new Intent();
        intent.setAction(ACTION_DEVICE_SERVICE);
        intent.setPackage("com.android.topwise.topusdkservice");
        
        try {
            boolean bindResult = mContext.bindService(intent, mServiceConnection, Context.BIND_AUTO_CREATE);
            Log.d(TAG, "Bind device service result: " + bindResult);
        } catch (Exception e) {
            Log.e(TAG, "Bind device service exception: " + e.getMessage());
        }
    }
    
    /**
     * Unbind from the Topwise device service
     */
    public void unbindDeviceService() {
        if (mContext != null && mBound) {
            mContext.unbindService(mServiceConnection);
            mBound = false;
            mDeviceService = null;
        }
    }
    
    private ServiceConnection mServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG, "Device service connected");
            mDeviceService = AidlDeviceService.Stub.asInterface(service);
            mBound = true;
        }
        
        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG, "Device service disconnected");
            mDeviceService = null;
            mBound = false;
        }
    };
    
    /**
     * Get the printer manager
     * @return AidlPrinter interface or null if service not available
     */
    public AidlPrinter getPrintManager() {
        if (mDeviceService == null) {
            Log.e(TAG, "Device service not available");
            return null;
        }
        
        try {
            return AidlPrinter.Stub.asInterface(mDeviceService.getPrinter());
        } catch (Exception e) {
            Log.e(TAG, "Get printer exception: " + e.getMessage());
            return null;
        }
    }
}
