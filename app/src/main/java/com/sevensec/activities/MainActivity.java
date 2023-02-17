package com.sevensec.activities;

import static com.sevensec.utils.Constants.STR_DEVICE_ID;
import static com.sevensec.utils.Constants.STR_FAV_APP_LIST;
import static com.sevensec.utils.Utils.isAccessGranted;
import static com.sevensec.utils.Utils.isDrawOverlayPermissionGranted;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;
import android.util.Log;
import android.view.View;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.databinding.DataBindingUtil;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sevensec.R;
import com.sevensec.adapter.MyListAdapter;
import com.sevensec.databinding.ActivityMainBinding;
import com.sevensec.model.AppInfoModel;
import com.sevensec.repo.FireStoreDataOperation;
import com.sevensec.service.SaveMyAppsService;
import com.sevensec.utils.Constants;
import com.sevensec.utils.SharedPref;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class MainActivity extends FireStoreDataOperation {

    String TAG = getClass().getName();
    ActivityMainBinding binding;
    PowerManager pm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        pm = (PowerManager) getSystemService(POWER_SERVICE);
        SharedPref.init(getApplicationContext());

        checkPermission();

        binding.btnPermission.setOnClickListener(view -> askPermissions());
    }

    private void checkPermission() {
        if (isAccessGranted(getApplicationContext()) &&
                isDrawOverlayPermissionGranted(getApplicationContext()) &&
                pm.isIgnoringBatteryOptimizations(Constants.APP_PACKAGE_NAME)) {

            Log.w(TAG, "onActivityResult All Permissions Granted: ");

            //Get Installed App list & show the list after sort it
            loadInstalledApps();
            binding.llPermission.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);

            //Start service
            startService(new Intent(this, SaveMyAppsService.class));

            //Store DEVICE_ID in Preference
            String DEVICE_ID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            Log.d(TAG, "onCreate DEVICE_ID: " + DEVICE_ID);
            SharedPref.writeString(STR_DEVICE_ID, DEVICE_ID);

            //Store DEVICE_ID in FireStore
            checkDeviceIsStored(DEVICE_ID);

        }else{
            Log.w(TAG, "onActivityResult All Permissions NOT Granted: ");
            binding.llPermission.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
        }
    }

    private void loadInstalledApps() {
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

        //Get Selected App list and sort the app list to show the selected apps on top
        List<String> favAppList = SharedPref.readListString(STR_FAV_APP_LIST);

        if (favAppList.size() > 0) {
            //Show Selected Apps on Top
            Collections.sort(appInfoModelList, new Comparator<AppInfoModel>() {
                @Override
                public int compare(AppInfoModel appInfoModel, AppInfoModel t1) {

                    if (favAppList.contains(t1.getPackageName())) return 1;
                    else if (favAppList.contains(appInfoModel.getPackageName())) return -1;
                    else return 0;
                }
            });
        }

        Log.i(TAG, "onCreate appInfoModelList length after sorting: " + appInfoModelList.size());

        MyListAdapter adapter = new MyListAdapter(appInfoModelList, favAppList);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            // Add same code that you want to add in onActivityResult method
            Log.d(TAG, "onActivityResult: ");
            checkPermission();
            askPermissions();
        }
    });

    private void askPermissions() {
        if (!isAccessGranted(getApplicationContext())) {
            showPermissionDialog("Usage Access Permission",
                    "Find the 7Sec app in the list and allow the Usage Access Permission.\n\nThen, come back.",
                    101);
        } else {
            Log.e(TAG, "askPermissions app: " + Constants.APP_PACKAGE_NAME);
            Log.e(TAG, "askPermissions isBatteryOptimized: " + pm.isIgnoringBatteryOptimizations(Constants.APP_PACKAGE_NAME));

            if (!isDrawOverlayPermissionGranted(getApplicationContext())) {
                showPermissionDialog("Overlay Permission",
                        "Find the 7Sec app in the list and allow the Overlay Permission.\n\nThen, come back.",
                        102);
            } else {
                if (!pm.isIgnoringBatteryOptimizations(Constants.APP_PACKAGE_NAME)) {
                    showPermissionDialog("Battery Optimization Permission",
                            "Take out the Battery Optimization for 7Sec to run in the background.",
                            103);
                }
            }

        }
    }

    private void showPermissionDialog(String title, String description, int permissionCode) {
        new AlertDialog
                .Builder(MainActivity.this, R.style.MyAlertDialogTheme)
                .setTitle(title)
                .setMessage(description)

                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton("Allow", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        if (permissionCode == 101) {
                            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
                            startActivityIntent.launch(intent);

                        } else if (permissionCode == 102) {
                            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + Constants.APP_PACKAGE_NAME));
                            startActivityIntent.launch(intent); //It will call onActivityResult Function After you press Yes/No and go Back after giving permission

                        } else if (permissionCode == 103) {
                            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
                            intent.setData(Uri.parse("package:" + Constants.APP_PACKAGE_NAME));
                            startActivityIntent.launch(intent);
                        }
                    }
                })

                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton("Cancel", null)
//                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }
}
