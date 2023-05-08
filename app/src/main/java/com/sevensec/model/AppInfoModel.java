package com.sevensec.model;

import android.content.pm.ApplicationInfo;
import android.graphics.Bitmap;
import android.os.Parcel;
import android.os.Parcelable;

import com.sevensec.utils.Dlog;

public class AppInfoModel implements Parcelable {
    private ApplicationInfo appInfo;
    private Bitmap appIconBitmap;
    private String appName;
    private String packageName;
    private String category;
    private boolean isFavorite;

    public AppInfoModel() {
    }

    public ApplicationInfo getAppInfo() {
        return appInfo;
    }

    public void setAppInfo(ApplicationInfo appInfo) {
        this.appInfo = appInfo;
    }

    public Bitmap getAppIconBitmap() {
        return appIconBitmap;
    }

    public void setAppIconBitmap(Bitmap appIconBitmap) {
        this.appIconBitmap = appIconBitmap;
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

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setFavorite(boolean favorite) {
        isFavorite = favorite;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    public static final Creator<AppInfoModel> CREATOR = new Creator<AppInfoModel>() {
        @Override
        public AppInfoModel createFromParcel(Parcel in) {
            return new AppInfoModel(in);
        }

        @Override
        public AppInfoModel[] newArray(int size) {
            return new AppInfoModel[size];
        }
    };

    private AppInfoModel(Parcel in) {
        appInfo = in.readParcelable(ApplicationInfo.class.getClassLoader());
        appIconBitmap = in.readParcelable(getClass().getClassLoader());
        appName = in.readString();
        packageName = in.readString();
        category = in.readString();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeParcelable(appInfo, flags);

        Dlog.e("appIconBitmap Width: " + appIconBitmap.getWidth() +" ,Height: "+ appIconBitmap.getHeight());
        // Scale down the bitmap
        Bitmap scaledBitmap = Bitmap.createScaledBitmap(appIconBitmap, 300, 300, true);
        Dlog.i("scaledBitmap Width: " + scaledBitmap.getWidth() +" ,Height: "+ scaledBitmap.getHeight());

        dest.writeParcelable(scaledBitmap, flags);
        dest.writeString(appName);
        dest.writeString(packageName);
        dest.writeString(category);
    }
}
