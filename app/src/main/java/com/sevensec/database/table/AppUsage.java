package com.sevensec.database.table;

import static com.sevensec.utils.Constants.COLUMN_APP_CLOSE_TIME;
import static com.sevensec.utils.Constants.COLUMN_APP_NAME;
import static com.sevensec.utils.Constants.COLUMN_APP_OPEN_TIME;
import static com.sevensec.utils.Constants.COLUMN_APP_USAGE_TIME;
import static com.sevensec.utils.Constants.COLUMN_DATE;
import static com.sevensec.utils.Constants.COLUMN_PACKAGE_NAME;
import static com.sevensec.utils.Constants.TABLE_NAME;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;

import java.util.Date;

@Entity(tableName = TABLE_NAME)
public class AppUsage {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = COLUMN_APP_NAME)
    private String appName;

    @ColumnInfo(name = COLUMN_PACKAGE_NAME)
    private String packageName;

    @ColumnInfo(name = COLUMN_DATE)
    private Date date;

    @ColumnInfo(name = COLUMN_APP_OPEN_TIME)
    private long appOpenTime;

    @ColumnInfo(name = COLUMN_APP_CLOSE_TIME)
    private long appCloseTime;

    @ColumnInfo(name = COLUMN_APP_USAGE_TIME)
    private long appUsageTime;

    public AppUsage(int id, String appName, String packageName, Date date, long appOpenTime, long appCloseTime, long appUsageTime) {
        this.id = id;
        this.appName = appName;
        this.packageName = packageName;
        this.date = date;
        this.appOpenTime = appOpenTime;
        this.appCloseTime = appCloseTime;
        this.appUsageTime = appUsageTime;
    }

    @Ignore
    public AppUsage(String appName, String packageName, Date date, long appOpenTime) {
        this.appName = appName;
        this.packageName = packageName;
        this.date = date;
        this.appOpenTime = appOpenTime;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Date getDate() {
        return date;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public long getAppOpenTime() {
        return appOpenTime;
    }

    public void setAppOpenTime(long appOpenTime) {
        this.appOpenTime = appOpenTime;
    }

    public long getAppCloseTime() {
        return appCloseTime;
    }

    public void setAppCloseTime(long appCloseTime) {
        this.appCloseTime = appCloseTime;
    }

    public long getAppUsageTime() {
        return appUsageTime;
    }

    public void setAppUsageTime(long appUsageTime) {
        this.appUsageTime = appUsageTime;
    }

}
