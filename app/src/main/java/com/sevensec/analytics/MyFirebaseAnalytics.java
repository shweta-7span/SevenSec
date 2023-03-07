package com.sevensec.analytics;
import static com.sevensec.utils.Constants.IS_PRODUCTION_MODE;

import android.content.Context;
import android.os.Bundle;

import com.google.firebase.analytics.FirebaseAnalytics;

public class MyFirebaseAnalytics {

    private static FirebaseAnalytics mFirebaseAnalytics;

    private MyFirebaseAnalytics() {
    }

    public static void init(Context context){
        if (mFirebaseAnalytics == null && IS_PRODUCTION_MODE)
            mFirebaseAnalytics = FirebaseAnalytics.getInstance(context);
    }

    public static void setUser(String deviceId) {
        if(IS_PRODUCTION_MODE) {
            mFirebaseAnalytics.setUserId(deviceId);
        }
    }

    public static void appOpenLog(String strData) {
        if(IS_PRODUCTION_MODE) {
            Bundle bundle = new Bundle();
            bundle.putString(FirebaseAnalytics.Param.CONTENT, strData);
            mFirebaseAnalytics.logEvent(FirebaseAnalytics.Event.APP_OPEN, bundle);
        }
    }

    public static void log(String strLogParam, String strEventName, String strData) {
        if(IS_PRODUCTION_MODE) {
            Bundle bundle = new Bundle();
            bundle.putString(strLogParam, strData);
            mFirebaseAnalytics.logEvent(strEventName, bundle);
        }
    }
}