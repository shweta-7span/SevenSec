package com.sevensec.model;

public class AppUsageByDate {
    String Date;
    long usage;

    public AppUsageByDate(String date, long usage) {
        Date = date;
        this.usage = usage;
    }

    public String getDate() {
        return Date;
    }

    public long getUsage() {
        return usage;
    }
}
