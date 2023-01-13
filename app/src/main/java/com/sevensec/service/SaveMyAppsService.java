package com.sevensec.service;

import static com.sevensec.base.AppConstants.APP_PACKAGE_NAME;
import static com.sevensec.base.AppConstants.STR_LAST_WARN_APP;
import static com.sevensec.utils.Constants.STR_FAV_APP_LIST;

import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.sevensec.activities.AttemptActivity;
import com.sevensec.utils.SharedPref;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SaveMyAppsService extends Service {

    String TAG;
//    String CURRENT_PACKAGE_NAME;
    String activityOnTop = "";
    String lastAppPN = "";
    boolean noDelay = false;
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

        //CURRENT_PACKAGE_NAME = getApplicationContext().getPackageName();
        //Log.e(TAG + "Current PN", "" + CURRENT_PACKAGE_NAME);

        lastAppPN = APP_PACKAGE_NAME;

        scheduleMethod();
        instance = this;
        return START_STICKY;
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = APP_PACKAGE_NAME;
        String channelName = "Background Service";
        NotificationChannel chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
        chan.setLightColor(Color.BLUE);
        chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);

        NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        assert manager != null;
        manager.createNotificationChannel(chan);

        NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
        Notification notification = notificationBuilder.setOngoing(true)
                .setContentTitle("App is running in background")
                .setPriority(NotificationManager.IMPORTANCE_MIN)
                .setCategory(Notification.CATEGORY_SERVICE)
                .build();
        startForeground(2, notification);
    }

    private void scheduleMethod() {
        ScheduledExecutorService scheduler = Executors
                .newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                checkRunningApps();
            }
        }, 0, 500, TimeUnit.MILLISECONDS);
    }

    public void checkRunningApps() {

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

        // Provide the packageName(s) of apps here, you want to show password activity
        // you can make this check even better
//        if (activityOnTop.contains(WARNING_APP)/*||
//        if (Arrays.asList(androidStrings).contains(activityOnTop)/*||
        if (favAppList.contains(activityOnTop)/*||
                activityOnTop.contains(CURRENT_PACKAGE_NAME) */) {

            Log.v(TAG, "TEST lastAppPN: " + lastAppPN);

            if (!(activityOnTop.equals(lastAppPN) ||
                    (activityOnTop.equals(APP_PACKAGE_NAME)))) {

                lastAppPN = activityOnTop;
                Log.e(TAG, "TEST After lastAppPN: " + lastAppPN);

                // Show Password Activity
                Log.w(TAG, "TEST Show Password Activity");
                Intent intent = new Intent(this, AttemptActivity.class);
                intent.putExtra(STR_LAST_WARN_APP, lastAppPN);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);

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
    }

    public static void stop() {
        if (instance != null) {
            instance.stopSelf();
        }
    }
}
