package com.sevensec.activities;

import static com.sevensec.utils.Constants.STR_DEVICE_ID;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;

import com.sevensec.R;
import com.sevensec.databinding.ActivityAttemptBinding;
import com.sevensec.repo.FireStoreDataOperation;
import com.sevensec.utils.Constants;
import com.sevensec.utils.SharedPref;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

public class AttemptActivity extends FireStoreDataOperation {

    private final String TAG = getClass().getName();
    ActivityAttemptBinding binding;

    private String appLabel;
    private String lastAppPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_attempt);

        String DEVICE_ID = SharedPref.readString(STR_DEVICE_ID, "");
        PackageManager packageManager = getPackageManager();

        binding.tvBreathDesc.setVisibility(View.VISIBLE);
        binding.rlAttempt.setVisibility(View.GONE);

        if (getIntent().getStringExtra(Constants.STR_LAST_WARN_APP) != null) {
            lastAppPackage = getIntent().getStringExtra(Constants.STR_LAST_WARN_APP);
            Log.e(TAG, "Last App's Package: " + lastAppPackage);
        }

        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(lastAppPackage, PackageManager.GET_UNINSTALLED_PACKAGES);
            if (appInfo != null) {
                Drawable iconDrawable = packageManager.getApplicationIcon(appInfo);
                appLabel = packageManager.getApplicationLabel(appInfo).toString();
                binding.ivAppLogo.setImageDrawable(iconDrawable);
                binding.tvAppLabel.setText(appLabel);

                binding.tvContinue.setText(String.format("%s %s", getString(R.string.continue_with), appLabel));
                binding.tvNotGoWithApp.setText(String.format("%s %s", getString(R.string.not_go), appLabel));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "getAppName Error: " + e.getMessage());
        }

        binding.tvContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                finish();
            }
        });

        binding.tvNotGoWithApp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                homeIntent.addCategory(Intent.CATEGORY_HOME);
                startActivity(homeIntent);
                finish();
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                binding.tvBreathDesc.setVisibility(View.GONE);
                binding.rlAttempt.setVisibility(View.VISIBLE);
            }
        }, 4000);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(getApplicationContext(), GreyActivity.class);
                startActivity(intent);
            }
        }, 500);

        checkAppAddedOrNot(DEVICE_ID, appLabel, lastAppPackage);
    }

    @Override
    public void setAttempt(int lastAttempt, String lastUsedTime) {
        super.setAttempt(lastAttempt, lastUsedTime);

        binding.tvAttempts.setText(String.valueOf(lastAttempt));
        if (lastAttempt == 1) {
            binding.tvAttemptDesc.setText(String.format("%s%s%s", getString(R.string.attempt_to_open), " " + appLabel + " ", getString(R.string.within_24_hrs)));
        } else {
            binding.tvAttemptDesc.setText(String.format("%s%s%s", getString(R.string.attempts_to_open), " " + appLabel + " ", getString(R.string.within_24_hrs)));
        }

        if (lastUsedTime != null)
            binding.tvLastUse.setText(String.format("Last use: %s", lastUsedTime));
    }
}