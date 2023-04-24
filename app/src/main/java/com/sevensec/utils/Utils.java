package com.sevensec.utils;

import static com.sevensec.utils.Constants.IN_APP_UPDATE_REQUEST_CODE;
import static com.sevensec.utils.Constants.PREF_FAV_APP_LIST;
import static com.sevensec.utils.Constants.STR_OPPO;
import static com.sevensec.utils.Constants.PREF_IS_SKIP_PROTECTED_APP_CHECKED;
import static com.sevensec.utils.Constants.STR_XIAOMI;

import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.os.Build;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.google.android.gms.tasks.Task;
import com.google.android.play.core.appupdate.AppUpdateInfo;
import com.google.android.play.core.appupdate.AppUpdateManager;
import com.google.android.play.core.appupdate.AppUpdateManagerFactory;
import com.google.android.play.core.install.model.AppUpdateType;
import com.google.android.play.core.install.model.UpdateAvailability;
import com.sevensec.R;

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
        Dlog.v("Package Name: " + context.getPackageName());

        // Check if we already have permission to draw over other apps
        if (!Settings.canDrawOverlays(context)) {
            Dlog.v("Requesting Permission: " + Settings.canDrawOverlays(context));
            return false;
        } else {
            Dlog.v("We already have permission for it.");
            // disablePullNotificationTouch();
            // Do your stuff, we got permission captain
            return true;
        }
    }

    public static boolean check24Hour(Long lastTimeStamp) {
        long MILLIS_PER_DAY = 24 * 60 * 60 * 1000L;

        boolean moreThanDay = Math.abs(lastTimeStamp - (new Date().getTime())) > MILLIS_PER_DAY;
        Dlog.d("FireStore: check24Hour moreThanDay: " + moreThanDay);

        return moreThanDay;
    }

    public static String getTimeInFormat(long difference) {

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
            s.append(elapsedDays).append(elapsedDays == 1 ? " day" : " days");

        } else if (elapsedHours != 0) {
            s.append(elapsedHours).append(elapsedHours == 1 ? " hr" : " hrs");

        } else if (elapsedMinutes != 0) {
            s.append(elapsedMinutes).append(elapsedMinutes == 1 ? " min" : " mins");

        } else if (elapsedSeconds != 0) {
            s.append(elapsedSeconds).append(elapsedSeconds == 1 ? " sec" : " secs");
        }

        System.out.printf(
                "DIFFERENCE: %d days, %d hours, %d minutes, %d seconds%n",
                elapsedDays, elapsedHours, elapsedMinutes, elapsedSeconds);

        return String.valueOf(s);
    }

    public static String getAppUsageTimeInFormat(long difference) {

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
            s.append(elapsedDays).append(elapsedDays == 1 ? " day" : " days");
            if (elapsedHours != 0)
                s.append(" ").append(elapsedHours).append(elapsedHours == 1 ? " hr" : " hrs");

        } else if (elapsedHours != 0) {
            s.append(elapsedHours).append(elapsedHours == 1 ? " hr" : " hrs");
            if (elapsedMinutes != 0)
                s.append(" ").append(elapsedMinutes).append(elapsedMinutes == 1 ? " min" : " mins");

        } else if (elapsedMinutes != 0) {
            s.append(elapsedMinutes).append(elapsedMinutes == 1 ? " min" : " mins");
            if (elapsedSeconds != 0)
                s.append(" ").append(elapsedSeconds).append(elapsedSeconds == 1 ? " sec" : " secs");

        } else if (elapsedSeconds != 0) {
            s.append(elapsedSeconds).append(elapsedSeconds == 1 ? " sec" : " secs");
        }else{
            s.append("0 sec");
        }

        return String.valueOf(s);
    }

    public static List<Intent> POWER_MANAGER_INTENTS = Arrays.asList(
            new Intent().setComponent(new ComponentName("com.miui.securitycenter", "com.miui.permcenter.autostart.AutoStartManagementActivity")),
            new Intent().setComponent(new ComponentName("com.letv.android.letvsafe", "com.letv.android.letvsafe.AutobootManageActivity")),
            new Intent().setComponent(new ComponentName("com.huawei.systemmanager", "com.huawei.systemmanager.optimize.process.ProtectActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.permission.startup.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.coloros.safecenter", "com.coloros.safecenter.startupapp.StartupAppListActivity")),
            new Intent().setComponent(new ComponentName("com.samsung.android.lool", "com.samsung.android.sm.battery.ui.BatteryActivity")),
            new Intent().setComponent(new ComponentName("com.oneplus.security", "om.oneplus.security.chainlaunch.view.ChainLaunchAppListActivity")),
            new Intent().setComponent(new ComponentName("com.evenwell.powersaving.g3", "com.evenwell.powersaving.g3.exception.PowerSaverExceptionActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.AddWhiteListActivity")),
            new Intent().setComponent(new ComponentName("com.iqoo.secure", "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager")),
            new Intent().setComponent(new ComponentName("com.vivo.permissionmanager", "com.vivo.permissionmanager.activity.BgStartUpManagerActivity")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.autostart.AutoStartActivy")),
            new Intent().setComponent(new ComponentName("com.asus.mobilemanager", "com.asus.mobilemanager.entry.FunctionActivity")).setData(android.net.Uri.parse("mobilemanager://function/entry/AutoStart"))
    );


    public static void startPowerSaverIntent(Context context) {
        boolean skipMessage = SharedPref.readBoolean(PREF_IS_SKIP_PROTECTED_APP_CHECKED, false);

        String title;
        String message;

        Dlog.d("startPowerSaverIntent MANUFACTURER: " + Build.MANUFACTURER);

        if (Build.MANUFACTURER.equalsIgnoreCase(STR_XIAOMI)) {
            title = Build.MANUFACTURER + " Enable Autostart";
            message = String.format("%s requires to be enabled 'Autostart' to function properly.%n", context.getString(R.string.app_name));

        } else if (Build.MANUFACTURER.equalsIgnoreCase(STR_OPPO)) {
            title = Build.MANUFACTURER + " Enable Autostart";
            message = "Go to \"App Management\" → \"7Sec\" → Allow \"Auto Startup\".\n\n" +
                    "Tap \"Power Saver\" and select \"Allow Background Running\".\n";
        } else {
            title = Build.MANUFACTURER + " Protected Apps";
            message = String.format("%s requires to be enabled in 'Protected Apps' to function properly.%n", context.getString(R.string.app_name));
        }

        if (!skipMessage) {
            boolean foundCorrectIntent = false;
            for (Intent intent : POWER_MANAGER_INTENTS) {
                if (isCallable(context, intent)) {
                    Dlog.d("startPowerSaverIntent Intent: " + intent.getComponent());

                    foundCorrectIntent = true;

                    LayoutInflater factory = LayoutInflater.from(context);
                    View view = factory.inflate(R.layout.dont_show_again_popup, null);
                    CheckBox checkBox = view.findViewById(R.id.cbNotShowAgain);

                    AlertDialog.Builder builder = new AlertDialog.Builder(context, R.style.MyAlertDialogTheme)
                            .setTitle(title)
                            .setMessage(message)
                            .setView(view)
                            .setCancelable(false)
                            .setPositiveButton(R.string.go_to_settings, (dialog, which) -> {
                                SharedPref.writeBoolean(PREF_IS_SKIP_PROTECTED_APP_CHECKED, checkBox.isChecked());
                                try {
                                    if (!Build.MANUFACTURER.equalsIgnoreCase(STR_OPPO)) {
                                        context.startActivity(intent);

                                    } else {
                                        Dlog.d("startPowerSaverIntent else Manufacture: " + Build.MANUFACTURER);

                                        //For OPPO
                                        try {
                                            Intent intentOppo = new Intent();
                                            intentOppo.setClassName("com.coloros.safecenter",
                                                    "com.coloros.safecenter.permission.startup.StartupAppListActivity");
                                            context.startActivity(intentOppo);
                                        } catch (Exception e) {
                                            try {
                                                Intent intentOppo = new Intent();
                                                intentOppo.setClassName("com.oppo.safe",
                                                        "com.oppo.safe.permission.startup.StartupAppListActivity");
                                                context.startActivity(intentOppo);

                                            } catch (Exception ex) {
                                                try {
                                                    Intent intentOppo = new Intent();
                                                    intentOppo.setClassName("com.coloros.safecenter",
                                                            "com.coloros.safecenter.startupapp.StartupAppListActivity");
                                                    context.startActivity(intentOppo);
                                                } catch (Exception exx) {
                                                    Dlog.e("startPowerSaverIntent Exception: " + exx.getMessage());
                                                    context.startActivity(new Intent(Settings.ACTION_SETTINGS));
                                                }
                                            }
                                        }
                                    }
                                } catch (Exception e) {
                                    dialog.dismiss();
                                    Toast.makeText(context, "7Sec can't open the settings screen", Toast.LENGTH_LONG).show();
                                }
                            })
                            .setNegativeButton(R.string.cancel, null);

                    AlertDialog dialog = builder.create();
                    if (dialog != null) {
                        dialog.show();
                    }
                    checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setEnabled(!isChecked));
                    break;
                }
            }
            if (!foundCorrectIntent) {
                SharedPref.writeBoolean(PREF_IS_SKIP_PROTECTED_APP_CHECKED, true);
            }
        }
    }


    private static boolean isCallable(Context context, Intent intent) {
        List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,
                PackageManager.MATCH_DEFAULT_ONLY);
        return list.size() > 0;
    }

    public static void checkForInAppUpdate(Context mContext, Activity activity) {
        Dlog.d("APP UPDATE checkForUpdate");

        AppUpdateManager appUpdateManager = AppUpdateManagerFactory.create(mContext);

        // Returns an intent object that you use to check for an update.
        Task<AppUpdateInfo> appUpdateInfoTask = appUpdateManager.getAppUpdateInfo();

        // Checks that the platform will allow the specified type of update.
        appUpdateInfoTask.addOnSuccessListener(appUpdateInfo -> {
            Dlog.d("APP UPDATE checkForUpdate addOnSuccessListener");

            if (appUpdateInfo.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE
                    // This example applies an immediate update. To apply a flexible update
                    // instead, pass in AppUpdateType.FLEXIBLE
                    && appUpdateInfo.isUpdateTypeAllowed(AppUpdateType.IMMEDIATE)) {

                // Request the update.
                try {
                    Dlog.d("APP UPDATE checkForUpdate try");

                    appUpdateManager.startUpdateFlowForResult(
                            // Pass the intent that is returned by 'getAppUpdateInfo()'.
                            appUpdateInfo,
                            // Or 'AppUpdateType.FLEXIBLE' for flexible updates.
                            AppUpdateType.IMMEDIATE,
                            // The current activity making the update request.
                            activity,
                            // Include a request code to later monitor this update request.
                            IN_APP_UPDATE_REQUEST_CODE);

//                    Toast.makeText(activity, "Update available", Toast.LENGTH_SHORT).show();

                } catch (IntentSender.SendIntentException e) {
                    e.printStackTrace();
                    Dlog.e("APP UPDATE checkForUpdate error: " + e);
//                    Toast.makeText(activity, "Update error: " + e, Toast.LENGTH_SHORT).show();
                }
            } else {
                Dlog.d("APP UPDATE checkForUpdate else");
//                Toast.makeText(activity, "Update NOT available", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public static boolean isInternetAvailable(Context mContext) {
        ConnectivityManager cm = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);

        return cm.getActiveNetworkInfo() != null && cm.getActiveNetworkInfo().isConnected();
    }

    public static Bitmap getBitmapFromDrawable(Drawable drawable) {
        Bitmap bitmap;
        if (drawable instanceof BitmapDrawable) {
            bitmap = ((BitmapDrawable) drawable).getBitmap();
        } else {
            bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
            drawable.draw(canvas);
        }
        return bitmap;
    }

    public static Drawable getDrawableFromBitmap(Context context, Bitmap bitmap) {
        return new BitmapDrawable(context.getResources(), bitmap);
//        return new BitmapDrawable(bitmap);
    }

    public static List<String> getFavAppList() {
        return SharedPref.readListString(PREF_FAV_APP_LIST);
    }
}
