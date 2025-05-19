package com.cashlez.sdkprintertopwise;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.topwise.cloudpos.aidl.AidlDeviceService;
import com.topwise.cloudpos.aidl.buzzer.AidlBuzzer;
import com.topwise.cloudpos.aidl.camera.AidlCameraScanCode;
import com.topwise.cloudpos.aidl.cpucard.AidlCPUCard;
import com.topwise.cloudpos.aidl.decoder.AidlDecoderManager;
import com.topwise.cloudpos.aidl.emv.level2.AidlAmex;
import com.topwise.cloudpos.aidl.emv.level2.AidlEmvL2;
import com.topwise.cloudpos.aidl.emv.level2.AidlEntry;
import com.topwise.cloudpos.aidl.emv.level2.AidlPaypass;
import com.topwise.cloudpos.aidl.emv.level2.AidlPaywave;
import com.topwise.cloudpos.aidl.emv.level2.AidlPure;
import com.topwise.cloudpos.aidl.emv.level2.AidlQpboc;
import com.topwise.cloudpos.aidl.fingerprint.AidlFingerprint;
import com.topwise.cloudpos.aidl.iccard.AidlICCard;
import com.topwise.cloudpos.aidl.keymanager.AidlKeyManager;
import com.topwise.cloudpos.aidl.led.AidlLed;
import com.topwise.cloudpos.aidl.magcard.AidlMagCard;
import com.topwise.cloudpos.aidl.mqtt.AidlMQTT;
import com.topwise.cloudpos.aidl.pedestal.AidlPedestal;
import com.topwise.cloudpos.aidl.pinpad.AidlPinpad;
import com.topwise.cloudpos.aidl.pm.AidlPM;
import com.topwise.cloudpos.aidl.printer.AidlPrinter;
import com.topwise.cloudpos.aidl.psam.AidlPsam;
import com.topwise.cloudpos.aidl.rfcard.AidlRFCard;
import com.topwise.cloudpos.aidl.serialport.AidlSerialport;
import com.topwise.cloudpos.aidl.shellmonitor.AidlShellMonitor;
import com.topwise.cloudpos.aidl.smallscreen.AidlSmallScreen;
import com.topwise.cloudpos.aidl.system.AidlSystem;
import com.topwise.cloudpos.aidl.tm.AidlTM;

import java.lang.reflect.Method;

/**
 * @author caixh
 */
public class DeviceServiceManager {
    private static final String TAG = "DeviceServiceManager";

    private static final String DEVICE_SERVICE_PACKAGE_NAME = "com.android.topwise.topusdkservice";
    private static final String DEVICE_SERVICE_CLASS_NAME = "com.android.topwise.topusdkservice.service.DeviceService";
    private static final String ACTION_DEVICE_SERVICE = "topwise_cloudpos_device_service";

    private static DeviceServiceManager instance;
    private Context mContext;
    private AidlDeviceService mDeviceService;

    public static DeviceServiceManager getInstance() {
        Log.d(TAG,"getInstance()");
        if (null == instance) {
            synchronized (DeviceServiceManager.class) {
                instance = new DeviceServiceManager();
            }
        }
        return instance;
    }


    public boolean bindDeviceService(Context context) {
        Log.i(TAG,"bindDeviceService");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return true;
        }
        this.mContext = context;
        Intent intent = new Intent();
        intent.setAction(ACTION_DEVICE_SERVICE);
        intent.setClassName(DEVICE_SERVICE_PACKAGE_NAME, DEVICE_SERVICE_CLASS_NAME);

        try {
            boolean bindResult = mContext.bindService(intent, mConnection, Context.BIND_AUTO_CREATE);
            Log.i(TAG,"bindResult = " + bindResult);
            return bindResult;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public void unBindDeviceService() {
        Log.i(TAG,"unBindDeviceService");
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return;
        }
        try {
            mContext.unbindService(mConnection);
        } catch (Exception e) {
            Log.i(TAG,"unbind DeviceService service failed : " + e);
        }
    }


    private final ServiceConnection mConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName componentName, IBinder service) {
            mDeviceService = AidlDeviceService.Stub.asInterface(service);
            Log.d(TAG,"gz mDeviceService" + mDeviceService);
            Log.i(TAG,"onServiceConnected  :  " + mDeviceService);
        }

        @Override
        public void onServiceDisconnected(ComponentName componentName) {
            Log.i(TAG,"onServiceDisconnected  :  " + mDeviceService);
            mDeviceService = null;
        }
    };

    public void getDeviceService() {
        if(mDeviceService == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            mDeviceService =  AidlDeviceService.Stub.asInterface(getService(ACTION_DEVICE_SERVICE));
        }
    }

    private static IBinder getService(String serviceName) {
        IBinder binder = null;
        try {
            ClassLoader cl = SmartPosApplication.getContext().getClassLoader();
            Class serviceManager = cl.loadClass("android.os.ServiceManager");
            Class[] paramTypes = new Class[1];
            paramTypes[0] = String.class;
            Method get = serviceManager.getMethod("getService", paramTypes);
            Object[] params = new Object[1];
            params[0] = serviceName;
            binder = (IBinder) get.invoke(serviceManager, params);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return binder;
    }


    public AidlSystem getSystemManager() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlSystem.Stub.asInterface(mDeviceService.getSystemService());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlBuzzer getBuzzer() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlBuzzer.Stub.asInterface(mDeviceService.getBuzzer());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlDecoderManager getDecoder() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlDecoderManager.Stub.asInterface(mDeviceService.getDecoder());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlLed getLed() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlLed.Stub.asInterface(mDeviceService.getLed());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlPinpad getPinpadManager(int devid) {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlPinpad.Stub.asInterface(mDeviceService.getPinPad(devid));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlPrinter getPrintManager() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlPrinter.Stub.asInterface(mDeviceService.getPrinter());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlTM getAidlTM() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlTM.Stub.asInterface(mDeviceService.getTM());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }


    public AidlICCard getICCardReader() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlICCard.Stub.asInterface(mDeviceService.getInsertCardReader());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
    public AidlKeyManager getKeyManager() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlKeyManager.Stub.asInterface(mDeviceService.getKeyManager());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlRFCard getRfCardReader() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlRFCard.Stub.asInterface(mDeviceService.getRFIDReader());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlPsam getPsamCardReader(int devid) {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlPsam.Stub.asInterface(mDeviceService.getPSAMReader(devid));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlMagCard getMagCardReader() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlMagCard.Stub.asInterface(mDeviceService.getMagCardReader());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlCPUCard getCPUCardReader() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlCPUCard.Stub.asInterface(mDeviceService.getCPUCard());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlSerialport getSerialPort(int port) {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlSerialport.Stub.asInterface(mDeviceService.getSerialPort(port));
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlShellMonitor getShellMonitor() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlShellMonitor.Stub.asInterface(mDeviceService.getShellMonitor());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlPedestal getPedestal() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlPedestal.Stub.asInterface(mDeviceService.getPedestal());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlEmvL2 getEmvL2() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlEmvL2.Stub.asInterface(mDeviceService.getL2Emv());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlPure getL2Pure() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlPure.Stub.asInterface(mDeviceService.getL2Pure());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlPaypass getL2Paypass() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlPaypass.Stub.asInterface(mDeviceService.getL2Paypass());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlPaywave getL2Paywave() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlPaywave.Stub.asInterface(mDeviceService.getL2Paywave());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlEntry getL2Entry() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlEntry.Stub.asInterface(mDeviceService.getL2Entry());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlAmex getL2Amex() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlAmex.Stub.asInterface(mDeviceService.getL2Amex());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlQpboc getL2Qpboc() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlQpboc.Stub.asInterface(mDeviceService.getL2Qpboc());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlCameraScanCode getCameraManager() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlCameraScanCode.Stub.asInterface(mDeviceService.getCameraManager());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public Bundle expandFunction(Bundle param) {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return mDeviceService.expandFunction(param);
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    // zhongfeiyu add pm by 2022/1/11 @{
    public AidlPM getPm() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlPM.Stub.asInterface(mDeviceService.getPM());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
    // @}

    //finger detect
    public AidlFingerprint getFingerprint(){
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlFingerprint.Stub.asInterface(mDeviceService.getFingerprint());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlSmallScreen getSmallScreen() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlSmallScreen.Stub.asInterface(mDeviceService.getSmallScreenManager());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }

    public AidlMQTT getMQTT() {
        try {
            getDeviceService();
            if (mDeviceService != null) {
                return AidlMQTT.Stub.asInterface(mDeviceService.getMQTTManager());
            }
        } catch (RemoteException e) {
            e.printStackTrace();
        }
        return null;
    }
}
