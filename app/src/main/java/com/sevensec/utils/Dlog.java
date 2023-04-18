package com.sevensec.utils;

import android.util.Log;

import com.sevensec.BuildConfig;

public class Dlog {

    static final String TAG = "SevenSec";
    static final boolean isDebug = BuildConfig.DEBUG;

    /** Log Level Error **/
    public static void e(String message) {
        if (isDebug) Log.e(TAG, message);
    }
    /** Log Level Warning **/
    public static void w(String message) {
        if (isDebug)Log.w(TAG, message);
    }
    /** Log Level Information **/
    public static void i(String message) {
        if (isDebug)Log.i(TAG, message);
    }
    /** Log Level Debug **/
    public static void d(String message) {
        if (isDebug)Log.d(TAG, message);
    }
    /** Log Level Verbose **/
    public static void v(String message) {
        if (isDebug)Log.v(TAG, message);
    }
}
