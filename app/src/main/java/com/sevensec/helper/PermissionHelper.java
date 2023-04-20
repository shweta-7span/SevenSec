package com.sevensec.helper;

import static com.sevensec.utils.Constants.APP_PACKAGE_NAME;
import static com.sevensec.utils.Constants.PERMISSION_POPUP_DELAY;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Build;
import android.os.Handler;

import com.sevensec.model.AppInfoModel;
import com.sevensec.service.MyForegroundService;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.Utils;

import java.util.ArrayList;
import java.util.List;

public class PermissionHelper {

    public static List<AppInfoModel> loadInstalledApps(Context context) {
        PackageManager packageManager = context.getPackageManager();
        List<AppInfoModel> appInfoModelList = new ArrayList<>();
//        List<ApplicationInfo> packs = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);
        List<ResolveInfo> packs = packageManager.queryIntentActivities(mainIntent, 0);

        for (int i = 0; i < packs.size(); i++) {

            ApplicationInfo applicationInfo = packs.get(i).activityInfo.applicationInfo;

            // skip system apps if they shall not be included
//            if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
//                continue;
//            }
            Dlog.v( "onCreate appName: " + packageManager.getApplicationLabel(applicationInfo));
            Dlog.d( "onCreate installed: " + applicationInfo.packageName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Dlog.d( "onCreate info: " + ApplicationInfo.getCategoryTitle(context, applicationInfo.category));
            }

            AppInfoModel appInfoModel = new AppInfoModel();
            appInfoModel.setAppInfo(applicationInfo);
            appInfoModel.setAppIconBitmap(Utils.getBitmapFromDrawable(packageManager.getApplicationIcon(applicationInfo)));
            appInfoModel.setAppName(packageManager.getApplicationLabel(applicationInfo).toString());
            appInfoModel.setPackageName(applicationInfo.packageName);

            List<String> favAppList = Utils.getFavAppList();
            if(favAppList.contains(applicationInfo.packageName)){
                appInfoModel.setFavorite(true);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ApplicationInfo.getCategoryTitle(context, applicationInfo.category) != null)
                    appInfoModel.setCategory(ApplicationInfo.getCategoryTitle(context, applicationInfo.category).toString());
            }

            if (!appInfoModel.getPackageName().equals(APP_PACKAGE_NAME)) {
                appInfoModelList.add(appInfoModel);
            }
        }
        Dlog.w( "onCreate appInfoModelList length: " + appInfoModelList.size());
        return appInfoModelList;
    }

    public static void startForegroundService(Context context) {
        new Handler().postDelayed(() -> {
            //ask rto enable Autostart
            Utils.startPowerSaverIntent(context);
        }, PERMISSION_POPUP_DELAY);

        //Start service
        context.startService(new Intent(context, MyForegroundService.class));
    }
}
