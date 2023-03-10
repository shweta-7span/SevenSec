package com.sevensec.utils;

import static com.sevensec.utils.Constants.STR_SKIP_PROTECTED_APP_CHECK;

import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.AppCompatCheckBox;

import com.sevensec.R;
import com.sevensec.activities.MainActivity;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

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

    public static List<Intent> POWER_MANAGER_INTENTS = Arrays.asList(
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.oppo.safe", "com.oppo.safe.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")).setData(android.net.Uri.parse("mobilemanager://function/entry/AutoStart"))
    );


    public static void startPowerSaverIntent(Context context) {
        boolean skipMessage = SharedPref.readBoolean(STR_SKIP_PROTECTED_APP_CHECK, false);

        String title;
        String message;

        if(Build.MANUFACTURER.equals("Xiaomi")){
             title = Build.MANUFACTURER + " Enable Autostart";
             message = String.format("%s requires to be enabled 'Autostart' to function properly.%n", context.getString(R.string.app_name));
        }else{
            title = Build.MANUFACTURER + " Protected Apps";
            message = String.format("%s requires to be enabled in 'Protected Apps' to function properly.%n", context.getString(R.string.app_name));
        }

        if (!skipMessage) {
            boolean foundCorrectIntent = false;
            for (Intent intent : POWER_MANAGER_INTENTS) {
                if (isCallable(context, intent)) {
                    foundCorrectIntent = true;

                    LayoutInflater factory = LayoutInflater.from(context);
                    View view = factory.inflate(R.layout.dont_show_again_popup, null);
                    CheckBox checkBox = view.findViewById(R.id.cbNotShowAgain);
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                        SharedPref.writeBoolean(STR_SKIP_PROTECTED_APP_CHECK, isChecked);
                    });

                    new AlertDialog.Builder(context, R.style.MyAlertDialogTheme)
                            .setTitle(title)
                            .setMessage(message)
                            .setView(view)
                            .setPositiveButton(R.string.go_to_settings, (dialog, which) -> context.startActivity(intent))
                            .setNegativeButton(R.string.cancel, null)
                            .show();
                    break;
                }
            }
            if (!foundCorrectIntent) {
                SharedPref.writeBoolean(STR_SKIP_PROTECTED_APP_CHECK, true);
            }
        }
    }


    private static boolean isCallable(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }
}
