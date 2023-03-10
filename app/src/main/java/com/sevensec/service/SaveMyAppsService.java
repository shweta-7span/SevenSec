package com.sevensec.service;

import static com.sevensec.utils.Constants.APP_PACKAGE_NAME;
import static com.sevensec.utils.Constants.CHECK_TOP_APPLICATION_DELAY;
import static com.sevensec.utils.Constants.OPEN_ATTEMPT_SCREEN_DELAY;
import static com.sevensec.utils.Constants.STR_APP_SWITCH_DURATION;
import static com.sevensec.utils.Constants.STR_FAV_APP_LIST;
import static com.sevensec.utils.Constants.STR_LAST_WARN_APP;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.sevensec.R;
import com.sevensec.activities.AttemptActivity;
import com.sevensec.activities.MainActivity;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

public class SaveMyAppsService extends Service {

    String TAG;
    String activityOnTop = "";
    String lastAppPN = "";
    public static SaveMyAppsService instance;
    //    String[] androidStrings;
    List<String> favAppList = new ArrayList<>();

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());
    }

    @Override
    public IBinder onBind(Intent intent) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // TODO Auto-generated method stub

        TAG = getApplicationContext().getClass().getName();
        //        androidStrings = getResources().getStringArray(R.array.arrFavApps);
        lastAppPN = APP_PACKAGE_NAME;
        Log.d(TAG, "onStartCommand: ");

        scheduleMethod();
        instance = this;
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground() {
        String NOTIFICATION_CHANNEL_ID = APP_PACKAGE_NAME;
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);
        PendingIntent contentIntent = PendingIntent.getActivity(this,
                0, new Intent(this, MainActivity.class),
                PendingIntent.FLAG_IMMUTABLE);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle(getString(R.string.app_is_running))
                .setContentText(getString(R.string.checking_selected_apps))
                .setContentIntent(contentIntent)
                .setSmallIcon(R.drawable.seven_sec)
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private void scheduleMethod() {
        /*Timer timer = new Timer();
        TimerTask t = new TimerTask() {
            @Override
            public void run() {
                checkRunningApps();
            }
        };
        timer.scheduleAtFixedRate(t, 0, 500);*/

        checkRunningApps();
    }

    public void checkRunningApps() {

        SharedPref.init(getApplicationContext());
        favAppList = SharedPref.readListString(STR_FAV_APP_LIST);
        Log.w(TAG, "TEST favAppList: " + favAppList.size());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            /*------For [Build.VERSION.SDK_INT < 20]---------*/
            ActivityManager mActivityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
            ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
            activityOnTop = ar.topActivity.getPackageName();

        } else {
            /*------For [Build.VERSION.SDK_INT > 20]---------*/
            //String activityOnTop = mActivityManager.getAppTasks().get(0).getTaskInfo().topActivity.getPackageName();
            //String activityOnTop = mActivityManager.getRunningAppProcesses().get(0).processName;

            UsageStatsManager sUsageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
            long endTime = System.currentTimeMillis();
            long beginTime = endTime - 10000;
            String result = "";
            UsageEvents.Event event = new UsageEvents.Event();
            UsageEvents usageEvents = sUsageStatsManager.queryEvents(beginTime, endTime);
            while (usageEvents.hasNextEvent()) {
                usageEvents.getNextEvent(event);
                if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                    result = event.getPackageName();
                }
            }
            if (!android.text.TextUtils.isEmpty(result)) {
                activityOnTop = result;
            }
        }
        Log.v(TAG, "TEST activity on Top: " + activityOnTop);
        saveAppCloseTime(activityOnTop, lastAppPN);

        // Provide the packageName(s) of apps here, you want to show attempt activity
//        if (Arrays.asList(androidStrings).contains(activityOnTop)/*||
        if (favAppList.contains(activityOnTop)/*||
                activityOnTop.contains(CURRENT_PACKAGE_NAME) */) {

            Log.v(TAG, "TEST lastAppPN: " + lastAppPN);

            if (!(activityOnTop.equals(lastAppPN) ||
                    (activityOnTop.equals(APP_PACKAGE_NAME)))) {

                if (isAppSwitchTimeExpire(activityOnTop)) {

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {

                        //ISSUE:
                        //Sometimes attempt screen was not opened for blocked apps (like 'amazon', 'instagram').
                        //Because, earlier the 'lastAppPN = activityOnTop' will be written outside of this condition.
                        //So, when the condition satisfy then, before open the attempt activity immediately the method
                        //called again & condition was NOT satisfied. So, the Attempt screen was not opened.

                        //SOLUTION:
                        //Assign pkg to the 'lastAppPN' at this line, so it will wait for the delay &
                        //till then the condition "(!(activityOnTop.equals(lastAppPN))" will be satisfied.
                        //So, the attempt screen can open.
                        lastAppPN = activityOnTop;
                        Log.e(TAG, "TEST After lastAppPN: " + lastAppPN);

                        // Show Password Activity
                        Log.w(TAG, "TEST Show Password Activity");
                        Intent intent = new Intent(SaveMyAppsService.this, AttemptActivity.class);
                        intent.putExtra(STR_LAST_WARN_APP, lastAppPN);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                    }, OPEN_ATTEMPT_SCREEN_DELAY);
                }

            } else {
                Log.d(TAG, "TEST Don't Show Password Activity");
            }

        } else {
            // DO nothing
            Log.w(TAG, "TEST DO nothing: " + activityOnTop);

            if (activityOnTop.equals(lastAppPN) || activityOnTop.equals(APP_PACKAGE_NAME)) {
                Log.d(TAG, "TEST Don't Update");
            } else {
                Log.w(TAG, "TEST Update lastAppPN: " + activityOnTop);
                lastAppPN = activityOnTop;
            }
        }

        //call the method again after execution of the above code
        new Handler().postDelayed(() -> checkRunningApps(), CHECK_TOP_APPLICATION_DELAY);
    }

    private void saveAppCloseTime(String activityOnTop, String lastAppPN) {
        //Check user open the app after click on continue button of Attempt Screen OR not
        if (SharedPref.readBoolean(Utils.getIsLastAppOpenKey(lastAppPN), false)) {
            if (!activityOnTop.equals(lastAppPN) &&
                    !activityOnTop.equals(APP_PACKAGE_NAME) /*&&
                    favAppList.contains(lastAppPN)*/) {
                Log.d(TAG, "AppSwitch: closed Time for " + lastAppPN + " :" + new Date().getTime());
                SharedPref.writeLong(lastAppPN, new Date().getTime());
            }
        }
    }

    private boolean isAppSwitchTimeExpire(String lastAppPN) {
        /*long lastUsedDifference = Math.abs(SharedPref.readLong(lastAppPN, new Date().getTime() + ((1000 * 60) * 60)) - new Date().getTime());
        long elapsedMinutes = lastUsedDifference / (1000 * 60);

        Log.v(TAG, "App Switch: " + lastAppPN + ": " + elapsedMinutes);

        return elapsedMinutes >= 1;*/

        //Default AppSwitchDuration when user installed the app & not change the AppSwitchDuration
        String[] arrAppSwitchDelay = getResources().getStringArray(R.array.arrAppSwitchDelay);
        int durationInSeconds = Integer.parseInt(arrAppSwitchDelay[arrAppSwitchDelay.length - 1].split(" ")[0]) * 60;

        long appSwitchDuration = SharedPref.readInteger(STR_APP_SWITCH_DURATION, durationInSeconds);
        Log.w(TAG, "isAppSwitchTimeExpire: appSwitchDuration: " + appSwitchDuration);

        if (appSwitchDuration == 0) {
            Log.v(TAG, "AppSwitch: " + lastAppPN + " ,elapsedSeconds already: " + 0);
            return true;

        } else {
            long lastUsedDifference = Math.abs(SharedPref.readLong(lastAppPN, new Date().getTime() + (appSwitchDuration * 1000 * 60)) - new Date().getTime());
            long elapsedSeconds = lastUsedDifference / 1000;

            Log.v(TAG, "AppSwitch: " + lastAppPN + " ,elapsedSeconds: " + elapsedSeconds);

            return elapsedSeconds > appSwitchDuration;
        }
    }

    public static void stop() {
        if (instance != null) {
            instance.stopSelf();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        stop();
    }
}
