package com.sevensec.activities;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static com.sevensec.utils.Constants.BATTERY_OPTIMIZATION_REQUEST_CODE;
import static com.sevensec.utils.Constants.IN_APP_UPDATE_REQUEST_CODE;
import static com.sevensec.utils.Constants.NOTIFICATION_PERMISSION_REQUEST_CODE;
import static com.sevensec.utils.Constants.OVERLAY_REQUEST_CODE;
import static com.sevensec.utils.Constants.PERMISSION_POPUP_DELAY;
import static com.sevensec.utils.Constants.STR_APP_SWITCH_DURATION;
import static com.sevensec.utils.Constants.STR_APP_SWITCH_POSITION;
import static com.sevensec.utils.Constants.STR_DEVICE_ID;
import static com.sevensec.utils.Constants.STR_FAV_APP_LIST;
import static com.sevensec.utils.Constants.STR_FIRST_TIME_APP_LAUNCH;
import static com.sevensec.utils.Constants.STR_XIAOMI;
import static com.sevensec.utils.Constants.STR_XIAOMI_OVERLAY;
import static com.sevensec.utils.Constants.USAGE_ACCESS_REQUEST_CODE;
import static com.sevensec.utils.Constants.XIAOMI_OVERLAY_REQUEST_CODE;
import static com.sevensec.utils.Utils.isAccessGranted;
import static com.sevensec.utils.Utils.isDrawOverlayPermissionGranted;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.sevensec.R;
import com.sevensec.activities.fragments.SingleChoiceDialogFragment;
import com.sevensec.adapter.MyListAdapter;
import com.sevensec.databinding.ActivityMainBinding;
import com.sevensec.helper.ActionClickInterface;
import com.sevensec.helper.PermissionDialog;
import com.sevensec.helper.PermissionHelper;
import com.sevensec.model.AppInfoModel;
import com.sevensec.repo.FireStoreDataOperation;
import com.sevensec.utils.Constants;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;

import java.util.Collections;
import java.util.List;

public class MainActivity extends FireStoreDataOperation implements SingleChoiceDialogFragment.SingleChoiceListener, ActionClickInterface {

    ActivityMainBinding binding;
    PowerManager pm;
    MenuItem itemSettings;
    boolean isPermissionGranted = false;
    PermissionDialog permissionDialog;
    int permissionCode;

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        pm = (PowerManager) getSystemService(POWER_SERVICE);

        SharedPref.writeBoolean(STR_FIRST_TIME_APP_LAUNCH, false);
        if (ContextCompat.checkSelfPermission(this, POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED) {
            checkPermission();
        } else {
            ActivityCompat.requestPermissions(this, new String[]{POST_NOTIFICATIONS}, NOTIFICATION_PERMISSION_REQUEST_CODE);
        }
        permissionDialog = new PermissionDialog(MainActivity.this, MainActivity.this);

        binding.btnPermission.setOnClickListener(view -> askPermissions());

        Utils.checkForInAppUpdate(getApplicationContext(), this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        itemSettings = menu.findItem(R.id.action_settings);
        itemSettings.setVisible(isPermissionGranted);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            openAppSwitchingPopup();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void checkPermission() {
        if (isAccessGranted(getApplicationContext()) &&
                isDrawOverlayPermissionGranted(getApplicationContext()) &&
                pm.isIgnoringBatteryOptimizations(Constants.APP_PACKAGE_NAME)) {

            Dlog.w("onActivityResult All Permissions Granted: ");

            //Get Installed App list & show the list after sort it
            loadAllInstalledApps();

            PermissionHelper.startForegroundService(MainActivity.this);

            //Store DEVICE_ID in Preference
            @SuppressLint("HardwareIds") String DEVICE_ID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
            Dlog.d("onCreate DEVICE_ID: " + DEVICE_ID);
            SharedPref.writeString(STR_DEVICE_ID, DEVICE_ID);

            //Store DEVICE_ID in FireStore
            checkDeviceIsStored(DEVICE_ID);
            isPermissionGranted = true;
            if (itemSettings != null) {
                itemSettings.setVisible(true);
            }

        } else {
            Dlog.w("onActivityResult All Permissions NOT Granted: ");
            binding.llPermission.setVisibility(View.VISIBLE);
            binding.recyclerView.setVisibility(View.GONE);
            binding.llNoData.setVisibility(View.GONE);

            isPermissionGranted = false;
        }
    }

    private void loadAllInstalledApps() {
        List<AppInfoModel> appInfoModelList = PermissionHelper.loadInstalledApps(getApplicationContext());
        Dlog.w("onCreate appInfoModelList length: " + appInfoModelList.size());

        if (appInfoModelList.size() == 0) {
            binding.llPermission.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.GONE);
            binding.llNoData.setVisibility(View.VISIBLE);
        } else {
            binding.llPermission.setVisibility(View.GONE);
            binding.recyclerView.setVisibility(View.VISIBLE);
            binding.llNoData.setVisibility(View.GONE);
        }

        //Alphabetically Sorting
        Collections.sort(appInfoModelList, (appInfoModel, t1) -> {
            Dlog.v("appInfoModel: " + appInfoModel.getAppName());
            Dlog.i("t1: " + t1.getAppName());
            Dlog.w("compare: " + appInfoModel.getAppName().compareToIgnoreCase(t1.getAppName()));
            return appInfoModel.getAppName().compareToIgnoreCase(t1.getAppName());
        });

        //Get Selected App list and sort the app list to show the selected apps on top
        List<String> favAppList = SharedPref.readListString(STR_FAV_APP_LIST);

        if (favAppList.size() > 0) {
            //Show Selected Apps on Top
            Collections.sort(appInfoModelList, (appInfoModel, t1) -> {

                if (favAppList.contains(t1.getPackageName())) return 1;
                else if (favAppList.contains(appInfoModel.getPackageName())) return -1;
                else return 0;
            });
        }

        MyListAdapter adapter = new MyListAdapter(appInfoModelList, favAppList);
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    ActivityResultLauncher<Intent> startActivityIntent = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
        // Add same code that you want to add in onActivityResult method
        Dlog.d("onActivityResult: ");
        checkPermission();

        new Handler().postDelayed(this::askPermissions, PERMISSION_POPUP_DELAY);
    });

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (grantResults.length > 0) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                checkPermission();
            } else {
                if (shouldShowRequestPermissionRationale(POST_NOTIFICATIONS)) {

                    new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogTheme)
                            .setMessage(Html.fromHtml(getString(R.string.post_notification_permission_msg)))
                            .setPositiveButton(android.R.string.ok, (dialog, which) -> new Handler().postDelayed(() -> requestPermissions(new String[]{POST_NOTIFICATIONS},
                                    NOTIFICATION_PERMISSION_REQUEST_CODE), PERMISSION_POPUP_DELAY))
                            .setNegativeButton(android.R.string.cancel, null)
                            .create()
                            .show();
                }
            }
        }
    }

    private void askPermissions() {
        if (!isAccessGranted(getApplicationContext())) {
            showPermissionDialog(getString(R.string.usage_access_permission),
                    getString(R.string.usage_access_permission_msg),
                    USAGE_ACCESS_REQUEST_CODE);
        } else {
            Dlog.e("askPermissions app: " + Constants.APP_PACKAGE_NAME);
            Dlog.e("askPermissions isBatteryOptimized: " + pm.isIgnoringBatteryOptimizations(Constants.APP_PACKAGE_NAME));

            if (!isDrawOverlayPermissionGranted(getApplicationContext())) {
                showPermissionDialog(getString(R.string.overlay_permission),
                        getString(R.string.overlay_permission_msg),
                        OVERLAY_REQUEST_CODE);
            } else {

                if (Build.MANUFACTURER.equalsIgnoreCase(STR_XIAOMI)) {
                    if (!SharedPref.readBoolean(STR_XIAOMI_OVERLAY, false)) {
                        showPermissionDialog(getString(R.string.xiaomi_display_popup_window),
                                getString(R.string.xiaomi_display_popup_window_msg),
                                XIAOMI_OVERLAY_REQUEST_CODE);
                    } else {
                        batteryOptimizationRequest();
                    }
                } else {
                    batteryOptimizationRequest();
                }
            }

        }
    }

    private void batteryOptimizationRequest() {
        if (!pm.isIgnoringBatteryOptimizations(Constants.APP_PACKAGE_NAME)) {
            showPermissionDialog(getString(R.string.disable_battery_optimization),
                    getString(R.string.disable_battery_optimization_msg),
                    BATTERY_OPTIMIZATION_REQUEST_CODE);
        }
    }

    private void showPermissionDialog(String title, String description, int permissionCode) {
        this.permissionCode = permissionCode;
        if (permissionDialog != null) {
            permissionDialog.showAlert(title, description, permissionCode);
        }
    }

    private void openAppSwitchingPopup() {
        DialogFragment singleChoiceDialog = new SingleChoiceDialogFragment();
        singleChoiceDialog.setCancelable(false);
        singleChoiceDialog.show(getSupportFragmentManager(), "Single Choice Dialog");
    }

    @Override
    public void onPositiveButtonClick(int position, String selectedItem) {
        Dlog.w("onPositiveButtonClick: selectedItem: " + selectedItem);
        Dlog.w("onPositiveButtonClick: appSwitchDuration: " + Integer.parseInt(selectedItem.split(" ")[0]) * 60);

        SharedPref.writeInteger(STR_APP_SWITCH_POSITION, position);

        int durationInSeconds = Integer.parseInt(selectedItem.split(" ")[0]) * 60;
        SharedPref.writeInteger(STR_APP_SWITCH_DURATION, durationInSeconds);
    }

    @Override
    public void onNegativeButtonClick(DialogInterface dialog) {
        dialog.dismiss();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == IN_APP_UPDATE_REQUEST_CODE) {
            if (resultCode != RESULT_OK) {
                Dlog.e("APP UPDATE: onActivityResult: Update flow failed! Result code: " + resultCode);
                // If the update is cancelled or fails,
                // you can request to start the update again.
//                Toast.makeText(this, "Update Failed", Toast.LENGTH_SHORT).show();
                Utils.checkForInAppUpdate(getApplicationContext(), this);
            } else {
                Dlog.d("APP UPDATE: onActivityResult: Update flow done");
//                Toast.makeText(this, "Update Successfully Done", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onPositiveButtonClickAI() {
        if (permissionCode == USAGE_ACCESS_REQUEST_CODE) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivityIntent.launch(intent);

        } else if (permissionCode == OVERLAY_REQUEST_CODE) {
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + Constants.APP_PACKAGE_NAME));
            startActivityIntent.launch(intent); //It will call onActivityResult Function After you press Yes/No and go Back after giving permission

        } else if (permissionCode == XIAOMI_OVERLAY_REQUEST_CODE) {
            SharedPref.writeBoolean(STR_XIAOMI_OVERLAY, true);

            Intent intent = new Intent("miui.intent.action.APP_PERM_EDITOR");
            intent.setClassName("com.miui.securitycenter",
                    "com.miui.permcenter.permissions.PermissionsEditorActivity");
            intent.putExtra("extra_pkgname", getPackageName());
            startActivityIntent.launch(intent);
        } else if (permissionCode == BATTERY_OPTIMIZATION_REQUEST_CODE) {
            Intent intent = new Intent(Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS);
            intent.setData(Uri.parse("package:" + Constants.APP_PACKAGE_NAME));
            startActivityIntent.launch(intent);
        }
    }

    @Override
    public void onNegativeButtonClickAI() {

    }
}
