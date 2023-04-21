package com.sevensec.database;

import android.annotation.SuppressLint;

import androidx.room.TypeConverter;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DateConverter {
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy");

    @TypeConverter
    public static Date StringToDate(String stringDate) {
        try {
            return stringDate == null ? null : dateTimeFormat.parse(stringDate);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    @TypeConverter
    public static String dateToString(Date date){
        return date == null ? null : dateTimeFormat.format(date.getTime());
    }
}
