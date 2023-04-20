package com.sevensec.database;

import androidx.room.Dao;
import androidx.room.Insert;

import com.sevensec.database.table.AppUsage;

@Dao
public interface AppUsageDao {

    @Insert
    void addAppData(AppUsage appUsage);

    @Insert
    void updateAppData(AppUsage appUsage);
}
