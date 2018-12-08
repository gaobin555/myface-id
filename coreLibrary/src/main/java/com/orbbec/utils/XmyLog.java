package com.orbbec.utils;

import android.text.TextUtils;
import android.util.Log;
import com.orbbec.keyguard.core.BuildConfig;

/**
 *
 * @author jjchai
 * @date 2016/9/12
 */
public class XmyLog {

    public static String DEFAULT_TAG = "Keyguard";

    public static final boolean DEBUG = BuildConfig.LOG_DEBUG;

    public static int PRIORITY;

    private static final int INVALID_BYTES = -1;

    static {
        if (DEBUG) {
            PRIORITY = Log.VERBOSE;
        } else {
            PRIORITY = Log.INFO;
        }
    }

    private static String generateTag() {
        StackTraceElement caller = new Throwable().getStackTrace()[2];
        String tag = "%s.%s(L:%d)";
        String callerClazzName = caller.getClassName();
        callerClazzName = callerClazzName.substring(callerClazzName.lastIndexOf(".") + 1);
        tag = String.format(tag, callerClazzName, caller.getMethodName(), caller.getLineNumber());
        tag = TextUtils.isEmpty(DEFAULT_TAG) ? tag : DEFAULT_TAG + ":" + tag;
        return tag;
    }

    public static int v(String msg) {
        if (PRIORITY > Log.VERBOSE) {
            return INVALID_BYTES;
        }
        String tag = generateTag();
        return Log.v(tag, msg);
    }

    public static int v(String msg, Throwable tr) {
        if (PRIORITY > Log.VERBOSE) {
            return INVALID_BYTES;
        }
        String tag = generateTag();
        return Log.v(tag, msg, tr);
    }

    public static int d(String msg) {
        if (PRIORITY > Log.DEBUG) {
            return INVALID_BYTES;
        }
        String tag = generateTag();
        return Log.d(tag, msg);
    }

    public static int d(String msg, Throwable tr) {
        if (PRIORITY > Log.DEBUG) {
            return INVALID_BYTES;
        }
        String tag = generateTag();
        return Log.d(tag, msg, tr);
    }

    public static int i(String msg) {
        if (PRIORITY > Log.INFO) {
            return INVALID_BYTES;
        }
        String tag = generateTag();
        return Log.i(tag, msg);
    }

    public static int i(String msg, Throwable tr) {
        if (PRIORITY > Log.INFO) {
            return INVALID_BYTES;
        }
        String tag = generateTag();
        return Log.i(tag, msg, tr);
    }

    public static int w(String msg) {
        if (PRIORITY > Log.WARN) {
            return INVALID_BYTES;
        }
        String tag = generateTag();
        return Log.w(tag, msg);
    }

    public static int w(String msg, Throwable tr) {
        if (PRIORITY > Log.WARN) {
            return INVALID_BYTES;
        }
        String tag = generateTag();
        return Log.w(tag, msg, tr);
    }

    public static int w(Throwable tr) {
        if (PRIORITY > Log.WARN) {
            return INVALID_BYTES;
        }
        String tag = generateTag();
        return Log.w(tag, tr);
    }

    public static int e(String msg) {
        if (PRIORITY > Log.ERROR) {
            return INVALID_BYTES;
        }
        String tag = generateTag();
        return Log.e(tag, msg);
    }

    public static int e(String msg, Throwable tr) {
        if (PRIORITY > Log.ERROR) {
            return INVALID_BYTES;
        }
        String tag = generateTag();
        return Log.e(tag, msg, tr);
    }
}
