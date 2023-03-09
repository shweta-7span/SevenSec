package com.sevensec.utils;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import androidx.annotation.RequiresApi;

import java.util.Date;

public class Utils {

    public static boolean isAccessGranted(Context context) {
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
    }

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

    public static boolean check24Hour(Long lastTimeStamp) {
        long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;

        boolean moreThanDay = Math.abs(lastTimeStamp - (new Date().getTime())) > MILLIS_PER_DAY;
        Log.d("App", "FireStore: check24Hour moreThanDay: " + moreThanDay);

        return moreThanDay;
    }

    public static String getLastUsedTime(long difference) {

        System.out.println("difference : " + difference);

        StringBuilder s = new StringBuilder(100);

        long secondsInMilli = 1000;
        long minutesInMilli = secondsInMilli * 60;
        long hoursInMilli = minutesInMilli * 60;
        long daysInMilli = hoursInMilli * 24;

        long elapsedDays = difference / daysInMilli;
        difference = difference % daysInMilli;

        long elapsedHours = difference / hoursInMilli;
        difference = difference % hoursInMilli;

        long elapsedMinutes = difference / minutesInMilli;
        difference = difference % minutesInMilli;

        long elapsedSeconds = difference / secondsInMilli;

        if (elapsedDays != 0) {
            if (elapsedDays == 1)
                s.append(elapsedDays).append(" day ago");
            else
                s.append(elapsedDays).append(" days ago");
        } else if (elapsedHours != 0) {
            if (elapsedHours == 1)
                s.append(elapsedHours).append(" hr ago");
            else
                s.append(elapsedHours).append(" hrs ago");
        } else if (elapsedMinutes != 0) {
            if (elapsedMinutes == 1)
                s.append(elapsedMinutes).append(" min ago");
            else
                s.append(elapsedMinutes).append(" mins ago");
        } else if (elapsedSeconds != 0) {
            if (elapsedSeconds == 1)
                s.append(elapsedSeconds).append(" second ago");
            else
                s.append(elapsedSeconds).append(" seconds ago");
        }

        System.out.printf(
                "DIFFERENCE: %d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);

        return String.valueOf(s);
    }

    public static String getIsLastAppOpenKey(String lastAppPackage) {
        return "IS" + lastAppPackage;
    }
}
