package com.sevensec.activities;

import static android.Manifest.permission.POST_NOTIFICATIONS;
import static com.sevensec.utils.Constants.BATTERY_OPTIMIZATION_REQUEST_CODE;
import static com.sevensec.utils.Constants.IN_APP_UPDATE_REQUEST_CODE;
import static com.sevensec.utils.Constants.NOTIFICATION_PERMISSION_REQUEST_CODE;
import static com.sevensec.utils.Constants.OVERLAY_REQUEST_CODE;
import static com.sevensec.utils.Constants.PERMISSION_POPUP_DELAY;
import static com.sevensec.utils.Constants.PREF_APP_SWITCH_DURATION;
import static com.sevensec.utils.Constants.PREF_APP_SWITCH_POSITION;
import static com.sevensec.utils.Constants.PREF_DEVICE_ID;
import static com.sevensec.utils.Constants.PREF_IS_APP_LAUNCH_FIRST_TIME;
import static com.sevensec.utils.Constants.STR_PASS_APP_INFO;
import static com.sevensec.utils.Constants.STR_XIAOMI;
import static com.sevensec.utils.Constants.PREF_IS_XIAOMI_OVERLAY_DONE;
import static com.sevensec.utils.Constants.USAGE_ACCESS_REQUEST_CODE;
import static com.sevensec.utils.Constants.XIAOMI_OVERLAY_REQUEST_CODE;
import static com.sevensec.utils.Utils.isAccessGranted;
import static com.sevensec.utils.Utils.isDrawOverlayPermissionGranted;

import android.app.SearchManager;
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
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.analytics.FirebaseAnalytics;
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MainActivity extends FireStoreDataOperation implements SingleChoiceDialogFragment.SingleChoiceListener, ActionClickInterface {

    ActivityMainBinding binding;
    PowerManager pm;
    MenuItem itemSettings, itemSearch;
    boolean isPermissionGranted = false;
    PermissionDialog permissionDialog;
    int permissionCode;

    MyListAdapter adapter;
    List<AppInfoModel> appInfoModelList = new ArrayList<>();

    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main);

        pm = (PowerManager) getSystemService(POWER_SERVICE);
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);

        SharedPref.writeBoolean(PREF_IS_APP_LAUNCH_FIRST_TIME, false);
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

        itemSearch = menu.findItem(R.id.action_search);
        itemSearch.setVisible(isPermissionGranted);

        // Retrieve the SearchView and plug it into SearchManager
        final SearchView searchView = (SearchView) itemSearch.getActionView();
        SearchManager searchManager = (SearchManager) getSystemService(SEARCH_SERVICE);

        assert searchView != null;
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getComponentName()));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                Dlog.d("query: " + query);
                return false; // close the keyboard
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Dlog.d("newText: " + newText);
                filter(newText);
                return true;
            }
        });

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

            String DEVICE_ID = SharedPref.readString(PREF_DEVICE_ID, "");
            Dlog.d("ManinActivity DEVICE_ID: " + DEVICE_ID);

            //Store DEVICE_ID in FireStore
            checkDeviceIsStored(DEVICE_ID);
            isPermissionGranted = true;
            if (itemSettings != null) {
                itemSettings.setVisible(true);
            }
            if (itemSearch != null) {
                itemSearch.setVisible(true);
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
        appInfoModelList = PermissionHelper.loadInstalledApps(getApplicationContext());
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
        List<String> favAppList = Utils.getFavAppList();

        if (favAppList.size() > 0) {
            //Show Selected Apps on Top
            Collections.sort(appInfoModelList, (appInfoModel, t1) -> {

                if (favAppList.contains(t1.getPackageName())) return 1;
                else if (favAppList.contains(appInfoModel.getPackageName())) return -1;
                else return 0;
            });
        }

        adapter = new MyListAdapter(getApplicationContext(), appInfoModelList, favAppList, appInfoModel -> {

            if (favAppList.contains(appInfoModel.getPackageName())) {
                Intent intent = new Intent(getApplicationContext(), AppDetailsActivity.class);
                intent.putExtra(STR_PASS_APP_INFO, appInfoModel);
                startActivity(intent);
            } else {
                new AlertDialog.Builder(MainActivity.this, R.style.MyAlertDialogTheme)
                        .setMessage(Html.fromHtml(getString(R.string.msg_for_disabled_app) + appInfoModel.getAppName() + "</b> is currently disabled. Please enable it to view its usage data."))
                        .setPositiveButton(android.R.string.ok, null)
                        .create()
                        .show();
            }
        });
        binding.recyclerView.addItemDecoration(new DividerItemDecoration(getApplicationContext(), DividerItemDecoration.VERTICAL));
        binding.recyclerView.setHasFixedSize(true);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    void filter(String text) {
        List<AppInfoModel> searchAppList = new ArrayList<>();
        for (AppInfoModel appInfoModel : appInfoModelList) {
            //or use .equal(text) with you want equal match
            //use .toLowerCase() for better matches
            if (appInfoModel.getAppName().toLowerCase().contains(text.toLowerCase())) {
                searchAppList.add(appInfoModel);
            }
        }
        adapter.updateList(searchAppList);
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
                    if (!SharedPref.readBoolean(PREF_IS_XIAOMI_OVERLAY_DONE, false)) {
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
        Dlog.w("onPositiveButtonClick: appSwitchDuration: " + Integer.parseInt(selectedItem.split(" ")[0]) * ((position == 0) ? 1 : 60));

        SharedPref.writeInteger(PREF_APP_SWITCH_POSITION, position);

        int durationInSeconds = Integer.parseInt(selectedItem.split(" ")[0]) * ((position == 0) ? 1 : 60);
        SharedPref.writeInteger(PREF_APP_SWITCH_DURATION, durationInSeconds);
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
            SharedPref.writeBoolean(PREF_IS_XIAOMI_OVERLAY_DONE, true);

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
