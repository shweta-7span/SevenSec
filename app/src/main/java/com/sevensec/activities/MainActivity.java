package com.sevensec.activities;

import static com.sevensec.utils.Constants.STR_DEVICE_ID;
import static com.sevensec.utils.Constants.STR_FAV_APP_LIST;
import static com.sevensec.utils.Utils.isAccessGranted;
import static com.sevensec.utils.Utils.isDrawOverlayPermissionGranted;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sevensec.repo.FireStoreDataOperation;
import com.sevensec.R;
import com.sevensec.adapter.MyListAdapter;
import com.sevensec.databinding.ActivityMainBinding;
import com.sevensec.service.SaveMyAppsService;
import com.sevensec.model.AppInfoModel;
import com.sevensec.utils.Constants;
import com.sevensec.utils.SharedPref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends FireStoreDataOperation {

    String TAG;
    String DEVICE_ID;
    PowerManager pm;
    List<String> favAppList;
    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        pm = (PowerManager) getSystemService(POWER_SERVICE);
        TAG = getApplicationContext().getClass().getName();
        DEVICE_ID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Log.d(TAG, "onCreate DEVICE_ID: " + DEVICE_ID);

        SharedPref.init(getApplicationContext());
        SharedPref.writeString(STR_DEVICE_ID, DEVICE_ID);

        favAppList = SharedPref.readListString(STR_FAV_APP_LIST);

        PackageManager packageManager = getApplicationContext().getPackageManager();
        /*Intent mainIntent = new Intent(Intent.ACTION_MAIN, null);
        //mainIntent.addCategory(Intent.CATEGORY_LAUNCHER);

        List<ResolveInfo> appList = packageManager.queryIntentActivities(mainIntent, 0);
        Collections.sort(appList, new ResolveInfo.DisplayNameComparator(packageManager));

        for (int i = 0; i < appList.size(); i++) {
            Log.w(TAG, "onCreate All installed: " + appList.get(i));
        }*/

        List<AppInfoModel> appInfoModelList = new ArrayList<>();
        List<ApplicationInfo> packs = packageManager.getInstalledApplications(PackageManager.GET_META_DATA);

        for (int i = 0; i < packs.size(); i++) {

            ApplicationInfo a = packs.get(i);
            // skip system apps if they shall not be included
            if ((a.flags & ApplicationInfo.FLAG_SYSTEM) == 1) {
                continue;
            }
            Log.v(TAG, "onCreate appName: " + packageManager.getApplicationLabel(a).toString());
            Log.d(TAG, "onCreate installed: " + a.packageName);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Log.d(TAG, "onCreate info: " + ApplicationInfo.getCategoryTitle(getApplicationContext(), a.category));
            }

            AppInfoModel appInfoModel = new AppInfoModel();
            appInfoModel.setAppInfo(a);
            appInfoModel.setAppIcon(packageManager.getApplicationIcon(a));
            appInfoModel.setAppName(packageManager.getApplicationLabel(a).toString());
            appInfoModel.setPackageName(a.packageName);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (ApplicationInfo.getCategoryTitle(getApplicationContext(), a.category) != null)
                    appInfoModel.setCategory(ApplicationInfo.getCategoryTitle(getApplicationContext(), a.category).toString());
            }

            appInfoModelList.add(appInfoModel);
        }

        Log.w(TAG, "onCreate appInfoModelList length: " + appInfoModelList.size());

        //Alphabetically Sorting
        Collections.sort(appInfoModelList, new Comparator<AppInfoModel>() {
            @Override
            public int compare(AppInfoModel appInfoModel, AppInfoModel t1) {
                Log.v(TAG, "appInfoModel: " + appInfoModel.getAppName());
                Log.i(TAG, "t1: " + t1.getAppName());
                Log.w(TAG, "compare: " + appInfoModel.getAppName().compareToIgnoreCase(t1.getAppName()));
                return appInfoModel.getAppName().compareToIgnoreCase(t1.getAppName());
            }
        });

        if (favAppList.size() > 0) {
            //Show Selected Apps on Top
            Collections.sort(appInfoModelList, new Comparator<AppInfoModel>() {
                @Override
                public int compare(AppInfoModel appInfoModel, AppInfoModel t1) {

                    if (favAppList.contains(t1.getPackageName()))
                        return 1;
                    else if (!favAppList.contains(t1.getPackageName()))
                        return -1;
                    else
                        return 0;
                }
            });
        }

        Log.i(TAG, "onCreate appInfoModelList length after sorting: " + appInfoModelList.size());

        MyListAdapter adapter = new MyListAdapter(appInfoModelList, favAppList);
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);

        askPermissions();
    }

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    // Add same code that you want to add in onActivityResult method
                    Log.d(TAG, "onActivityResult: ");
                    askPermissions();
                }
            });

    private void askPermissions() {
        if (!isAccessGranted(getApplicationContext())) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivityIntent.launch(intent);
        } else {
            Log.e(TAG, "askPermissions app: " + Constants.APP_PACKAGE_NAME);
            Log.e(TAG, "askPermissions isBatteryOptimized: " + pm.isIgnoringBatteryOptimizations(Constants.APP_PACKAGE_NAME));

            if (!pm.isIgnoringBatteryOptimizations(Constants.APP_PACKAGE_NAME)) {
                Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                intent.setData(Uri.parse("package:" + Constants.APP_PACKAGE_NAME));
                startActivityIntent.launch(intent);

            } else {
                if (!isDrawOverlayPermissionGranted(getApplicationContext())) {
                    Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                            Uri.parse("package:" + Constants.APP_PACKAGE_NAME));
                    startActivityIntent.launch(intent); //It will call onActivityResult Function After you press Yes/No and go Back after giving permission
                } else {
                    Log.w(TAG, "onActivityResult All Permissions Granted: ");

                    //start service
                    startService(new Intent(this, SaveMyAppsService.class));

                    checkDeviceIsStored(DEVICE_ID);
                }
            }
        }
    }
}
