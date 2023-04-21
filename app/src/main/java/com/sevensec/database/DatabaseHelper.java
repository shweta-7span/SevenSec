package com.sevensec.database;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;

import com.sevensec.database.table.AppUsage;
import com.sevensec.utils.Constants;

@Database(entities = AppUsage.class, exportSchema = false, version = 1)
@TypeConverters({DateConverter.class})
public abstract class DatabaseHelper extends RoomDatabase {

    private static final String DB_NAME = Constants.ROOM_DB_NAME;
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getDatabase(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context, DatabaseHelper.class, DB_NAME)
                    .fallbackToDestructiveMigration()
                    .allowMainThreadQueries()
                    .build();
        }

        return instance;
    }

    public abstract AppUsageDao appUsageDao();
}
