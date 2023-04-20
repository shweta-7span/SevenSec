package com.sevensec.database.table;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.Ignore;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverter;

import java.util.Date;

@Entity(tableName = "app_usage")
public class AppUsage {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @ColumnInfo(name = "app_name")
    private String appName;

    @ColumnInfo(name = "package_name")
    private String packageName;

    @ColumnInfo(name = "date")
    private Date date;

    @ColumnInfo(name = "app_open_time")
    private long appOpenTime;

    @ColumnInfo(name = "app_close_time")
    private long appCloseTime;

    @ColumnInfo(name = "app_usage_time")
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
