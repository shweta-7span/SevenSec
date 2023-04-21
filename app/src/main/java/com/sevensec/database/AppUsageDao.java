package com.sevensec.database;

import static com.sevensec.utils.Constants.COLUMN_APP_OPEN_TIME;
import static com.sevensec.utils.Constants.COLUMN_APP_USAGE_TIME;
import static com.sevensec.utils.Constants.COLUMN_DATE;
import static com.sevensec.utils.Constants.COLUMN_PACKAGE_NAME;
import static com.sevensec.utils.Constants.TABLE_NAME;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.sevensec.database.table.AppUsage;

import java.util.Date;
import java.util.List;

@Dao
public interface AppUsageDao {

    @Insert
    void addAppData(AppUsage appUsage);

    @Update
    void updateAppData(AppUsage appUsage);

    @Query("SELECT * FROM " + TABLE_NAME + " WHERE " + COLUMN_APP_OPEN_TIME + "=:open_time AND " + COLUMN_PACKAGE_NAME + "=:package_name")
    List<AppUsage> getAppUsageByPackageNameAndOpenTime(String package_name, long open_time);

    @Query("select SUM(" + COLUMN_APP_USAGE_TIME + ") from " + TABLE_NAME + " WHERE " + COLUMN_PACKAGE_NAME + " =:package_name AND " + COLUMN_DATE + "=:Date")
    long getTotalAppUsageTimeForDay(String package_name, Date Date);
}
