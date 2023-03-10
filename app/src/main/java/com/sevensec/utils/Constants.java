package com.sevensec.utils;

import com.sevensec.BuildConfig;

public class Constants {

    //intent constant
    public static final String APP_PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    public static final String STR_LAST_WARN_APP = "STR_LAST_WARN_APP";

    //SharedPref Keys
    public static final String DB_COLLECTION_USERS = "Users";
    public static final String DB_COLLECTION_APPS = "Apps";
    public static final String DB_DOCUMENT_KEY_TYPE = "Type";

    public static final String DB_DOCUMENT_KEY_APP_NAME = "Name";
    public static final String DB_DOCUMENT_KEY_APP_PACKAGE = "Package";
    public static final String DB_DOCUMENT_KEY_APP_ATTEMPTS = "Attempts";

    public static final String STR_FIRST_TIME_APP_LAUNCH = "STR_FIRST_TIME_APP_LAUNCH";
    public static final String STR_DEVICE_ID = "STR_DEVICE_ID";
    public static final String STR_FAV_APP_LIST = "STR_FAV_APP_LIST";
    public static final String STR_APP_SWITCH_DURATION = "STR_APP_SWITCH_DURATION";
    public static final String STR_APP_SWITCH_POSITION = "STR_APP_SWITCH_POSITION";
    public static final String STR_SKIP_PROTECTED_APP_CHECK = "STR_SKIP_PROTECTED_APP_CHECK";
    public static final String ANDROID = "Android";

    public static final long DELAY_OPEN_GREY_PAGE = 500;
    public static final long GRAY_PAGE_ANIMATION_TIMER = 3000;
    public static final long PERMISSION_POPUP_DELAY = 300;
    public static final long OPEN_ATTEMPT_SCREEN_DELAY = 500;
    public static final long CHECK_TOP_APPLICATION_DELAY = 750;
//    public static final long DELAY_CHANGE_ATTEMPT_VIEW = GRAY_PAGE_ANIMATION_TIMER + DELAY_OPEN_GREY_PAGE;

//    public static final long APP_SWITCH_DURATION = 1000 * 60;
    //TODO: Check this boolean before give the .apk
    public static final boolean IS_PRODUCTION_MODE = false;
}
