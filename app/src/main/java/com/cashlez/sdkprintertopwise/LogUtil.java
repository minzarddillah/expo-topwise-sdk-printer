package com.cashlez.sdkprintertopwise;

import android.util.Log;

class LogUtil {

    private static final String TAG_PREFIX = "TPW-";
    private static final boolean DEBUG = true;

    static void d(String tag, String msg) {
        if (DEBUG) {
            Log.d(TAG_PREFIX + tag, msg);
        }
    }

    static void e(String tag, String msg) {
        Log.e(TAG_PREFIX + tag, msg);
    }

    static String getStackTrace() {
        StackTraceElement traceElement = ((new Exception()).getStackTrace())[1];
        return traceElement.getFileName() + " | " + traceElement.getLineNumber() + " | " + traceElement.getMethodName() + " ";
    }

}
