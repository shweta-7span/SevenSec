package com.sevensec.utils;

import com.sevensec.BuildConfig;

public class Constants {

    //GENERAL
    public static final String APP_PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    public static final String STR_LAST_WARN_APP = "LAST_WARN_APP";
    public static final String STR_XIAOMI = "Xiaomi";
    public static final String STR_OPPO = "oppo";
    //Send Model in Detail Activity
    public static final String STR_PASS_APP_INFO = "PASS_APP_INFO";

    //FIRE_STORE DB KEYS
    public static final String DB_COLLECTION_USERS = "Users";
    public static final String DB_USER_ID = "USER_ID";
    public static final String DB_COLLECTION_APPS = "Apps";
    public static final String DB_DOCUMENT_KEY_TYPE = "Type";
    public static final String DB_DOCUMENT_KEY_APP_NAME = "Name";
    public static final String DB_DOCUMENT_KEY_APP_PACKAGE = "Package";
    public static final String DB_DOCUMENT_KEY_APP_ATTEMPTS = "Attempts";
    public static final String DB_ANDROID = "Android";

    //SHARED_PREF KEYS
    public static final String PREF_IS_APP_LAUNCH_FIRST_TIME = "IS_APP_LAUNCH_FIRST_TIME";
    public static final String PREF_DEVICE_ID = "DEVICE_ID";
    public static final String PREF_FAV_APP_LIST = "FAV_APP_LIST";
    public static final String PREF_APP_SWITCH_DURATION = "APP_SWITCH_DURATION";
    public static final String PREF_APP_SWITCH_POSITION = "APP_SWITCH_POSITION";
    public static final String PREF_BLOCK_APP_OPEN_TIME = "BLOCK_APP_OPEN_TIME";
    public static final String PREF_IS_SKIP_PROTECTED_APP_CHECKED = "IS_SKIP_PROTECTED_APP_CHECKED";
    public static final String PREF_IS_XIAOMI_OVERLAY_DONE = "IS_XIAOMI_OVERLAY_DONE";

    //ROOM DB STRINGS
    public static final String ROOM_DB_NAME = "7SecDB";
    public static final String TABLE_NAME = "app_usage";
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_APP_NAME = "app_name";
    public static final String COLUMN_PACKAGE_NAME = "package_name";
    public static final String COLUMN_DATE = "date";
    public static final String COLUMN_APP_OPEN_TIME = "app_open_time";
    public static final String COLUMN_APP_CLOSE_TIME = "app_close_time";
    public static final String COLUMN_APP_USAGE_TIME = "app_usage_time";

    public static String getIsUserOpenBlockAppKey(String lastAppPackage) {
        return "IS_" + lastAppPackage;
    }

    public static String getAppCloseTimeKey(String appPackage) {
        return appPackage + "_CLOSE_TIME";
    }

    //DELAY OR TIMERS
    public static final long SPLASH_DELAY = 1000;
    public static final long GRAY_PAGE_ANIMATION_TIMER = 3000;
    public static final long PERMISSION_POPUP_DELAY = 300;
    public static final long OPEN_ATTEMPT_SCREEN_DELAY = 500;
    public static final long CHECK_TOP_APPLICATION_DELAY = 750;
    public static final long CHECK_TOP_APPLICATION_WHEN_ATTEMPT_OPEN_DELAY = 1000;

    //REQUEST_CODE
    public static final int USAGE_ACCESS_REQUEST_CODE = 101;
    public static final int OVERLAY_REQUEST_CODE = 102;
    public static final int XIAOMI_OVERLAY_REQUEST_CODE = 103;
    public static final int BATTERY_OPTIMIZATION_REQUEST_CODE = 104;
    public static final int IN_APP_UPDATE_REQUEST_CODE = 105;
    public static final int NOTIFICATION_PERMISSION_REQUEST_CODE = 106;

    //Set NumOfDays in Calendar
    public static final int START_DAY = 0; //0 = SUN and 1 = MON
    public static final int ADD_DAYS_FOR_END_DATE = 6;
    public static final int REMOVE_DAYS_FOR_PREV_WEEK = -7;
    public static final int ADD_DAYS_FOR_NEXT_WEEK = 7;

}
