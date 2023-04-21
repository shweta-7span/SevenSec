package com.sevensec.database;

import static com.sevensec.utils.Constants.COLUMN_APP_CLOSE_TIME;
import static com.sevensec.utils.Constants.COLUMN_APP_OPEN_TIME;
import static com.sevensec.utils.Constants.COLUMN_APP_USAGE_TIME;
import static com.sevensec.utils.Constants.COLUMN_DATE;
import static com.sevensec.utils.Constants.COLUMN_PACKAGE_NAME;
import static com.sevensec.utils.Constants.TABLE_NAME;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import com.sevensec.database.table.AppUsage;

import java.util.Date;

@Dao
public interface AppUsageDao {

    @Insert
    void addAppData(AppUsage appUsage);

    @Query("Update " + TABLE_NAME + " SET " + COLUMN_APP_CLOSE_TIME + " = :close_time, " + COLUMN_APP_USAGE_TIME + " = :usage_time WHERE " + COLUMN_APP_OPEN_TIME + "=:open_time AND " + COLUMN_PACKAGE_NAME + "=:package_name")
    void addCloseTime(String package_name, long open_time, long close_time, long usage_time);

    @Query("select SUM(" + COLUMN_APP_USAGE_TIME + ") from " + TABLE_NAME + " WHERE " + COLUMN_DATE + " = :Date")
    long getTotalAppUsageTimeForDay(Date Date);
}
