package com.orbbec.utils;

import android.util.Log;

/**
 * 日志工具类
 * @author lgp
 */
public class LogUtil {
    private static final String TAG = "gaobin";
    public static boolean isOpen = true;

    private LogUtil() {
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static void i(String msg) {
        if (isOpen) {
            Log.i(TAG, msg);
        }
    }

    public static void d(String msg) {
        if (isOpen) {
            Log.d(TAG, msg);
        }
    }

    public static void v(String msg) {
        if (isOpen) {
            Log.v(TAG, msg);
        }
    }
    public static void e(String msg) {
        if (isOpen) {
            Log.e(TAG, msg);
        }
    }

    public static void w(String msg) {
        if (isOpen) {
            Log.w(TAG, msg);
        }
    }

}
