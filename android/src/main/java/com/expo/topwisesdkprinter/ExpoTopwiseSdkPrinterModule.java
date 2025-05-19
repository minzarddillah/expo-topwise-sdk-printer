package com.expo.topwisesdkprinter;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.RemoteException;
import android.util.Log;
import android.graphics.Typeface;

import androidx.annotation.NonNull;

import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.ReadableArray;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.module.annotations.ReactModule;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import com.topwise.cloudpos.aidl.printer.AidlPrinter;
import com.topwise.cloudpos.aidl.printer.AidlPrinterListener;
import com.topwise.cloudpos.aidl.printer.Align;
import com.topwise.cloudpos.aidl.printer.ImageUnit;
import com.topwise.cloudpos.aidl.printer.PrintItemObj;
import com.topwise.cloudpos.aidl.printer.PrintTemplate;
import com.topwise.cloudpos.aidl.printer.TextUnit;
import com.topwise.cloudpos.aidl.printer.TextUnit.TextSize;

@ReactModule(name = ExpoTopwiseSdkPrinterModule.NAME)
public class ExpoTopwiseSdkPrinterModule extends ReactContextBaseJavaModule {
    public static final String NAME = "ExpoTopwiseSdkPrinter";
    private static final String TAG = "ExpoTopwiseSdkPrinter";

    private final ReactApplicationContext reactContext;
    private AidlPrinter printerDev = null;
    private boolean printRunning = false;

    public ExpoTopwiseSdkPrinterModule(ReactApplicationContext reactContext) {
        super(reactContext);
        this.reactContext = reactContext;
        
        try {
            // Initialize printer device
            printerDev = DeviceServiceManager.getInstance().getPrintManager();
            
            // Initialize print template
            Typeface typeface = Typeface.DEFAULT;
            if (reactContext != null) {
                try {
                    typeface = Typeface.createFromAsset(reactContext.getAssets(), "topwise.ttf");
                } catch (Exception e) {
                    Log.e(TAG, "Error loading typeface: " + e.getMessage());
                }
            }
            PrintTemplate.getInstance().init(reactContext, typeface);
        } catch (Exception e) {
            Log.e(TAG, "Error initializing printer: " + e.getMessage());
            printerDev = null;
        }
    }

    @Override
    @NonNull
    public String getName() {
        return NAME;
    }

    private final AidlPrinterListener mListener = new AidlPrinterListener.Stub() {
        @Override
        public void onPrintResult(int result) throws RemoteException {
            Log.d(TAG, "Print result: " + result);
            printRunning = false;
        }
    };

    @ReactMethod
    public void isAvailable(Promise promise) {
        try {
            boolean isAvailable = (printerDev != null);
            promise.resolve(isAvailable);
        } catch (Exception e) {
            promise.reject("PRINTER_ERROR", "Error checking printer availability: " + e.getMessage());
        }
    }

    @ReactMethod
    public void printText(String text, int fontSize, String alignStr, Promise promise) {
        if (printerDev == null) {
            promise.reject("PRINTER_ERROR", "Printer not available");
            return;
        }

        if (printRunning) {
            promise.reject("PRINTER_ERROR", "Printer is busy");
            return;
        }

        try {
            printRunning = true;
            
            Align align = getAlignFromString(alignStr);
            TextSize textSize;
            
            if (fontSize <= 16) {
                textSize = TextSize.SMALL;
            } else if (fontSize <= 24) {
                textSize = TextSize.NORMAL;
            } else if (fontSize <= 32) {
                textSize = TextSize.LARGE; 
            } else {
                textSize = TextSize.XLARGE;
            }
            
            PrintTemplate template = PrintTemplate.getInstance();
            template.clear();
            template.add(new TextUnit(text, textSize, align));
            template.add(new TextUnit("\\n\\n"));
            
            printerDev.addRuiImage(template.getPrintBitmap(), 0);
            printerDev.printRuiQueue(mListener);
            
            promise.resolve(true);
        } catch (Exception e) {
            printRunning = false;
            promise.reject("PRINTER_ERROR", "Error printing text: " + e.getMessage());
        }
    }

    @ReactMethod
    public void printImage(String imageUri, String alignStr, Promise promise) {
        if (printerDev == null) {
            promise.reject("PRINTER_ERROR", "Printer not available");
            return;
        }

        if (printRunning) {
            promise.reject("PRINTER_ERROR", "Printer is busy");
            return;
        }

        try {
            printRunning = true;
            
            Uri uri = Uri.parse(imageUri);
            Bitmap bitmap = BitmapFactory.decodeStream(
                reactContext.getContentResolver().openInputStream(uri)
            );
            
            if (bitmap == null) {
                printRunning = false;
                promise.reject("PRINTER_ERROR", "Cannot load image from URI");
                return;
            }
            
            Align align = getAlignFromString(alignStr);
            
            PrintTemplate template = PrintTemplate.getInstance();
            template.clear();
            
            int width = bitmap.getWidth();
            int height = bitmap.getHeight();
            int newWidth = 384; // Printer width
            int newHeight = height * newWidth / width;
            
            template.add(new ImageUnit(align, bitmap, newWidth, newHeight));
            template.add(new TextUnit("\\n\\n"));
            
            printerDev.addRuiImage(template.getPrintBitmap(), 0);
            printerDev.printRuiQueue(mListener);
            
            promise.resolve(true);
        } catch (Exception e) {
            printRunning = false;
            promise.reject("PRINTER_ERROR", "Error printing image: " + e.getMessage());
        }
    }

    @ReactMethod
    public void printQRCode(String data, int size, String alignStr, Promise promise) {
        if (printerDev == null) {
            promise.reject("PRINTER_ERROR", "Printer not available");
            return;
        }

        if (printRunning) {
            promise.reject("PRINTER_ERROR", "Printer is busy");
            return;
        }

        try {
            printRunning = true;
            
            Align align = getAlignFromString(alignStr);
            
            // Create QR code bitmap
            Bitmap qrBitmap = QRCodeUtil.createQRImage(data, size, size, null);
            
            if (qrBitmap == null) {
                printRunning = false;
                promise.reject("PRINTER_ERROR", "Cannot generate QR code");
                return;
            }
            
            PrintTemplate template = PrintTemplate.getInstance();
            template.clear();
            
            template.add(new ImageUnit(align, qrBitmap, size, size));
            template.add(new TextUnit("\\n\\n"));
            
            printerDev.addRuiImage(template.getPrintBitmap(), 0);
            printerDev.printRuiQueue(mListener);
            
            promise.resolve(true);
        } catch (Exception e) {
            printRunning = false;
            promise.reject("PRINTER_ERROR", "Error printing QR code: " + e.getMessage());
        }
    }

    @ReactMethod
    public void printReceipt(ReadableMap data, Promise promise) {
        if (printerDev == null) {
            promise.reject("PRINTER_ERROR", "Printer not available");
            return;
        }

        if (printRunning) {
            promise.reject("PRINTER_ERROR", "Printer is busy");
            return;
        }

        try {
            printRunning = true;
            
            PrintTemplate template = PrintTemplate.getInstance();
            template.clear();
            
            // Header
            if (data.hasKey("header")) {
                template.add(new TextUnit(data.getString("header"), TextSize.LARGE, Align.CENTER));
                template.add(new TextUnit("\\n"));
            }
            
            // Date/Time
            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            String dateTime = dateFormat.format(new Date());
            template.add(new TextUnit("Date: " + dateTime, TextSize.NORMAL, Align.LEFT));
            template.add(new TextUnit("\\n"));
            
            // Separator
            template.add(new TextUnit("--------------------------------", TextSize.NORMAL, Align.CENTER));
            template.add(new TextUnit("\\n"));
            
            // Items
            if (data.hasKey("items") && data.getArray("items") != null) {
                ReadableArray items = data.getArray("items");
                for (int i = 0; i < items.size(); i++) {
                    ReadableMap item = items.getMap(i);
                    if (item.hasKey("label") && item.hasKey("value")) {
                        String label = item.getString("label");
                        String value = item.getString("value");
                        template.add(new TextUnit(label + ": " + value, TextSize.NORMAL, Align.LEFT));
                        template.add(new TextUnit("\\n"));
                    }
                }
            }
            
            // Separator
            template.add(new TextUnit("--------------------------------", TextSize.NORMAL, Align.CENTER));
            template.add(new TextUnit("\\n"));
            
            // Footer
            if (data.hasKey("footer")) {
                template.add(new TextUnit(data.getString("footer"), TextSize.NORMAL, Align.CENTER));
            }
            
            template.add(new TextUnit("\\n\\n"));
            
            printerDev.addRuiImage(template.getPrintBitmap(), 0);
            printerDev.printRuiQueue(mListener);
            
            promise.resolve(true);
        } catch (Exception e) {
            printRunning = false;
            promise.reject("PRINTER_ERROR", "Error printing receipt: " + e.getMessage());
        }
    }

    @ReactMethod
    public void feedPaper(int lines, Promise promise) {
        if (printerDev == null) {
            promise.reject("PRINTER_ERROR", "Printer not available");
            return;
        }

        try {
            StringBuilder feed = new StringBuilder();
            for (int i = 0; i < lines; i++) {
                feed.append("\\n");
            }
            
            PrintTemplate template = PrintTemplate.getInstance();
            template.clear();
            template.add(new TextUnit(feed.toString()));
            
            printerDev.addRuiImage(template.getPrintBitmap(), 0);
            printerDev.printRuiQueue(mListener);
            
            promise.resolve(true);
        } catch (Exception e) {
            promise.reject("PRINTER_ERROR", "Error feeding paper: " + e.getMessage());
        }
    }

    @ReactMethod
    public void getPrinterStatus(Promise promise) {
        if (printerDev == null) {
            WritableMap statusMap = Arguments.createMap();
            statusMap.putBoolean("available", false);
            statusMap.putString("error", "Printer not initialized");
            promise.resolve(statusMap);
            return;
        }

        try {
            int status = printerDev.getPrinterState();
            
            WritableMap statusMap = Arguments.createMap();
            statusMap.putBoolean("available", true);
            statusMap.putInt("status", status);
            statusMap.putBoolean("busy", printRunning);
            
            // Parse printer state
            boolean hasPaper = (status & 0x01) == 0; // Paper out flag
            boolean overheated = (status & 0x02) != 0; // Overheated flag
            
            statusMap.putBoolean("hasPaper", hasPaper);
            statusMap.putBoolean("overheated", overheated);
            
            promise.resolve(statusMap);
        } catch (Exception e) {
            WritableMap statusMap = Arguments.createMap();
            statusMap.putBoolean("available", false);
            statusMap.putString("error", e.getMessage());
            promise.resolve(statusMap);
        }
    }

    @ReactMethod
    public void cutPaper(Promise promise) {
        if (printerDev == null) {
            promise.reject("PRINTER_ERROR", "Printer not available");
            return;
        }

        try {
            boolean result = printerDev.cutPaper(0); // Full cut
            promise.resolve(result);
        } catch (Exception e) {
            promise.reject("PRINTER_ERROR", "Error cutting paper: " + e.getMessage());
        }
    }

    private Align getAlignFromString(String alignStr) {
        if (alignStr == null) {
            return Align.LEFT;
        }
        
        switch (alignStr.toUpperCase()) {
            case "CENTER":
                return Align.CENTER;
            case "RIGHT":
                return Align.RIGHT;
            case "LEFT":
            default:
                return Align.LEFT;
        }
    }
}
