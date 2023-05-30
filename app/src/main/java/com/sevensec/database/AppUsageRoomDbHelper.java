package com.sevensec.database;

import static com.sevensec.utils.Constants.PREF_BLOCK_APP_OPEN_TIME;
import static com.sevensec.utils.Constants.getIsUserOpenBlockAppKey;

import android.content.Context;

import com.sevensec.database.table.AppUsage;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;

import java.util.Date;
import java.util.List;

public class AppUsageRoomDbHelper {

    AppUsageDao appUsageDao;

    public AppUsageRoomDbHelper(Context mContext) {
        appUsageDao = DatabaseHelper.getDatabase(mContext).appUsageDao();
    }

    public void addAppOpenTimeInDB(String appLabel, String appPackageName, Date currentDate, long appUsageStartTime) {

        SharedPref.writeBoolean(getIsUserOpenBlockAppKey(appPackageName), true);
        SharedPref.writeLong(PREF_BLOCK_APP_OPEN_TIME, appUsageStartTime);

        appUsageDao.addAppData(new AppUsage(appLabel, appPackageName, currentDate, appUsageStartTime));
    }

    public void storeAppUsageData(String appPackage, long appOpenTime, long appCloseTime, long appUsageTimeMillis) {

        List<AppUsage> appUsageList = appUsageDao.getAppUsageByPackageNameAndOpenTime(appPackage, appOpenTime);

        if (appUsageList.size() > 0) {
            AppUsage appUsage = appUsageList.get(0);

            appUsage.setAppCloseTime(appCloseTime);
            appUsage.setAppUsageTime(appUsageTimeMillis);

            appUsageDao.updateAppData(appUsage);
        } else {
            Dlog.e("Not get this Data");
        }
    }

    public long getAppCloseTimeForApp(String appPackage, long appSwitchDuration) {

        long appCloseTime;

        AppUsage appUsage = appUsageDao.getAppCloseTime(appPackage);

        if (appUsage == null) {
            Dlog.d("close time: NULL");
            appCloseTime = new Date().getTime() + (appSwitchDuration * 1000 * 60);
        } else {
            Dlog.d("close time: " + appUsage.getAppCloseTime());
            appCloseTime = appUsage.getAppCloseTime();
        }

        return appCloseTime;
    }

    public long getTotalAppUsageTimeForDate(String appPackage, Date currentDate) {
        return appUsageDao.getTotalAppUsageTimeForDay(appPackage, currentDate);
    }
}
