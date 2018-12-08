package com.orbbec.utils;

import android.util.Log;

/**
 * 日志工具类
 * @author lgp
 */
public class LogUtil {
    private static final String TAG = "Test_lgp";
    public static boolean isOpen = false;

    private LogUtil() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static void d(String msg) {
        if (!isOpen) {
            Log.d(TAG, msg);
        }
    }

    public static void e(String msg) {
        if (!isOpen) {
            Log.e(TAG, msg);
        }
    }

}
