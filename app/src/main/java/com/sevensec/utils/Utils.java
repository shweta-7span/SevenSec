package com.sevensec.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;

public class Utils {

    public static boolean isAccessGranted(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            try {
                PackageManager packageManager = context.getPackageManager();
                ApplicationInfo applicationInfo = packageManager.getApplicationInfo(context.getPackageName(), 0);
                AppOpsManager appOpsManager = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
                int mode = appOpsManager.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                        applicationInfo.uid, applicationInfo.packageName);
                return (mode == AppOpsManager.MODE_ALLOWED);

            } catch (PackageManager.NameNotFoundException e) {
                return false;
            }
        } else {
            return true;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    public static boolean isDrawOverlayPermissionGranted(Context context) {
        Log.v("App", "Package Name: " + context.getPackageName());

        // Check if we already have permission to draw over other apps
        if (!Settings.canDrawOverlays(context)) {
            Log.v("App", "Requesting Permission: " + Settings.canDrawOverlays(context));
            return false;
        } else {
            Log.v("App", "We already have permission for it.");
            // disablePullNotificationTouch();
            // Do your stuff, we got permission captain
            return true;
        }
    }
}
