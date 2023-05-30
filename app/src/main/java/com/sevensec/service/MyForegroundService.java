package com.sevensec.service;

import static com.sevensec.utils.Constants.APP_PACKAGE_NAME;
import static com.sevensec.utils.Constants.CHECK_TOP_APPLICATION_DELAY;
import static com.sevensec.utils.Constants.CHECK_TOP_APPLICATION_WHEN_ATTEMPT_OPEN_DELAY;
import static com.sevensec.utils.Constants.OPEN_ATTEMPT_SCREEN_DELAY;
import static com.sevensec.utils.Constants.PREF_BLOCK_APP_OPEN_TIME;
import static com.sevensec.utils.Constants.PREF_APP_SWITCH_DURATION;
import static com.sevensec.utils.Constants.PREF_DEVICE_ID;
import static com.sevensec.utils.Constants.STR_LAST_WARN_APP;
import static com.sevensec.utils.Constants.getIsUserOpenBlockAppKey;

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

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.sevensec.R;
import com.sevensec.activities.AttemptActivity;
import com.sevensec.activities.MainActivity;
import com.sevensec.database.AppUsageRoomDbHelper;
import com.sevensec.repo.FireStoreDataOperation;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class MyForegroundService extends Service {

    String TAG;
    String activityOnTop = "";
    String lastAppPN = APP_PACKAGE_NAME;
    public static MyForegroundService instance;
    //    String[] androidStrings;
    List<String> favAppList = new ArrayList<>();

    AppUsageRoomDbHelper appUsageRoomDbHelper;

    FireStoreDataOperation fireStoreDataOperation;

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.O)
            startMyOwnForeground();
        else
            startForeground(1, new Notification());

        fireStoreDataOperation = new FireStoreDataOperation();
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
        instance = this;
        appUsageRoomDbHelper = new AppUsageRoomDbHelper(this);

        //androidStrings = getResources().getStringArray(R.array.arrFavApps);
        Dlog.d("onStartCommand: " + lastAppPN);

        //scheduleMethod();
        checkRunningApps();

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

    /*private void scheduleMethod() {
        Timer timer = new Timer();
        TimerTask t = new TimerTask() {
            @Override
            public void run() {
                checkRunningApps();
            }
        };
        timer.scheduleAtFixedRate(t, 0, 500);
    }*/

    public void checkRunningApps() {

        SharedPref.init(getApplicationContext());
        favAppList = Utils.getFavAppList();
        Dlog.w("TEST favAppList: " + favAppList.size());

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            /*------For [Build.VERSION.SDK_INT < 20]---------*/
            ActivityManager mActivityManager = (ActivityManager) this.getSystemService(Context.ACTIVITY_SERVICE);
            List<ActivityManager.RunningTaskInfo> RunningTask = mActivityManager.getRunningTasks(1);
            ActivityManager.RunningTaskInfo ar = RunningTask.get(0);
            activityOnTop = ar.topActivity.getPackageName();

        } else {

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
        Dlog.v("TEST activity on Top: " + activityOnTop);
        saveAppCloseTime(activityOnTop, lastAppPN);

        // Provide the packageName(s) of apps here, you want to show attempt activity
        if (favAppList.contains(activityOnTop)) {

            Dlog.v("TEST lastAppPN: " + lastAppPN);

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
                        setLastApp(activityOnTop);
                        Dlog.e("TEST After lastAppPN: " + lastAppPN);

                        // Show Password Activity
                        Dlog.w("TEST Show Password Activity");
                        Intent intent = new Intent(MyForegroundService.this, AttemptActivity.class);
                        intent.putExtra(STR_LAST_WARN_APP, lastAppPN);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);

                        //call the method again after execution of the above code
                        callAgain(CHECK_TOP_APPLICATION_WHEN_ATTEMPT_OPEN_DELAY);

                    }, OPEN_ATTEMPT_SCREEN_DELAY);

                } else {
                    setLastApp(activityOnTop);
                    Dlog.d("TEST isAppSwitchTimeExpire False");
                    //call the method again after execution of the above code
                    callAgain(CHECK_TOP_APPLICATION_DELAY);
                }

            } else {
                Dlog.d("TEST Don't Show Password Activity");
                //call the method again after execution of the above code
                callAgain(CHECK_TOP_APPLICATION_DELAY);
            }

        } else {
            // DO nothing
            Dlog.w("TEST DO nothing: " + activityOnTop);

            if (activityOnTop.equals(lastAppPN) || activityOnTop.equals(APP_PACKAGE_NAME)) {
                Dlog.d("TEST Don't Update: lastAppPN: " + lastAppPN);
            } else {
                Dlog.w("TEST Update lastAppPN: " + activityOnTop);
                setLastApp(activityOnTop);
            }

            //call the method again after execution of the above code
            callAgain(CHECK_TOP_APPLICATION_DELAY);
        }
    }

    public void setLastApp(String lastAppPackage) {
        Dlog.d("TEST setLastApp before lastAppPN: " + lastAppPN);
        lastAppPN = lastAppPackage;
        Dlog.w("TEST setLastApp after lastAppPN: " + lastAppPN);
    }

    private void callAgain(long delay) {
        Dlog.d("callAgain");
        new Handler().postDelayed(() -> checkRunningApps(), delay);
    }

    private void saveAppCloseTime(String activityOnTop, String lastAppPN) {
        //Check user open the app after click on continue button of Attempt Screen OR not
        if (SharedPref.readBoolean(getIsUserOpenBlockAppKey(lastAppPN), false)) {
            if (!activityOnTop.equals(lastAppPN) &&
                    !activityOnTop.equals(APP_PACKAGE_NAME) &&
                    favAppList.contains(lastAppPN)) {

                getAppUsage(lastAppPN, System.currentTimeMillis());
            }
        }
    }

    private void getAppUsage(String appPackage, long appCloseTime) {
        Dlog.d("AppSwitch: closed Time for " + appPackage + " :" + appCloseTime);

        long appOpenTime = SharedPref.readLong(PREF_BLOCK_APP_OPEN_TIME, appCloseTime);

        long appUsageTimeMillis = appCloseTime - appOpenTime;
        Dlog.v("appUsageTimeMillis: " + Utils.getTimeInFormat(appUsageTimeMillis));

        Dlog.d("appPackage: " + appPackage);
        Dlog.d("appOpenTime: " + appOpenTime);

        //Store appUsage in Room database
        appUsageRoomDbHelper.storeAppUsageData(lastAppPN, appOpenTime, appCloseTime, appUsageTimeMillis);

        //Store appUsage in FireStore
        String DEVICE_ID = SharedPref.readString(PREF_DEVICE_ID, "");
        long totalAppUsageTimeInSeconds = appUsageRoomDbHelper.getTotalAppUsageTimeForDate(lastAppPN, new Date()) / 1000;

        fireStoreDataOperation.checkAppUsageForCurrentDate(DEVICE_ID, lastAppPN, totalAppUsageTimeInSeconds);
    }

    private boolean isAppSwitchTimeExpire(String lastAppPN) {

        //Default AppSwitchDuration when user installed the app & not change the AppSwitchDuration
        String[] arrAppSwitchDelay = getResources().getStringArray(R.array.arrAppSwitchDelay);
        int durationInSeconds = Integer.parseInt(arrAppSwitchDelay[arrAppSwitchDelay.length - 1].split(" ")[0]) * 60;

        long appSwitchDuration = SharedPref.readInteger(PREF_APP_SWITCH_DURATION, durationInSeconds);
        Dlog.w("isAppSwitchTimeExpire: appSwitchDuration: " + appSwitchDuration);

        if (appSwitchDuration == 0) {
            Dlog.v("AppSwitch: " + lastAppPN + " ,elapsedSeconds already: " + 0);
            return true;

        } else {
            long appCloseTime = appUsageRoomDbHelper.getAppCloseTimeForApp(lastAppPN, appSwitchDuration);

            long lastUsedDifference = Math.abs(appCloseTime - new Date().getTime());
            long elapsedSeconds = lastUsedDifference / 1000;

            Dlog.v("AppSwitch: " + lastAppPN + " ,elapsedSeconds: " + elapsedSeconds);

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
        Dlog.e("MyService: onDestroy: ");
        startMyService();
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        super.onTaskRemoved(rootIntent);
        Dlog.e("MyService: onTaskRemoved: ");
        startMyService();
    }

    void startMyService() {
        Intent broadcastIntent = new Intent("com.sevenSec.MyForegroundService.RestartSensor");
        sendBroadcast(broadcastIntent);
    }
}
