package com.sevensec.utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.sevensec.model.AppInfoModel;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPref {
    private static SharedPreferences mSharedPref;

    private SharedPref() {
    }

    public static void init(Context context) {
        if (mSharedPref == null)
            mSharedPref = context.getSharedPreferences(context.getPackageName(), Activity.MODE_PRIVATE);
    }

    public static String readString(String key, String defValue) {
        return mSharedPref.getString(key, defValue);
    }

    public static void writeString(String key, String value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putString(key, value);
        prefsEditor.apply();
    }

    public static boolean readBoolean(String key, boolean defValue) {
        return mSharedPref.getBoolean(key, defValue);
    }

    public static void writeBoolean(String key, boolean value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putBoolean(key, value);
        prefsEditor.apply();
    }

    public static Integer readInteger(String key, int defValue) {
        return mSharedPref.getInt(key, defValue);
    }

    public static void writeInteger(String key, Integer value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putInt(key, value).apply();
    }

    public static long readLong(String key, long defValue) {
        return mSharedPref.getLong(key, defValue);
    }

    public static void writeLong(String key, long value) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.putLong(key, value).apply();
    }

    public static <T> void writeList(String key, List<T> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);

        writeString(key, json);
    }

    public static List<String> readListString(String key){
        List<String> arrayItems = new ArrayList<>();
        String serializedObject = readString(key, null);

        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<String>>(){}.getType();
            arrayItems = gson.fromJson(serializedObject, type);
        }

        return arrayItems;
    }

    public static List<AppInfoModel> readList(String key){
        List<AppInfoModel> arrayItems = new ArrayList<>();
        String serializedObject = readString(key, null);

        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<AppInfoModel>>(){}.getType();
            arrayItems = gson.fromJson(serializedObject, type);
        }

        return arrayItems;
    }

    public static void clear(String key) {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.remove(key).apply();
    }

    public static void clearAll() {
        SharedPreferences.Editor prefsEditor = mSharedPref.edit();
        prefsEditor.clear();
    }
}