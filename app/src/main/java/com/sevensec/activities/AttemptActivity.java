package com.sevensec.activities;

import static com.sevensec.utils.Constants.STR_DEVICE_ID;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;

import com.sevensec.R;
import com.sevensec.databinding.ActivityAttemptBinding;
import com.sevensec.repo.FireStoreDataOperation;
import com.sevensec.utils.Constants;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;

import java.io.InputStream;

public class AttemptActivity extends FireStoreDataOperation {

    private final String TAG = getClass().getName();
    ActivityAttemptBinding binding;

    private String appLabel;
    private String lastAppPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_attempt);

        String DEVICE_ID = SharedPref.readString(STR_DEVICE_ID, "");
        PackageManager packageManager = getPackageManager();

        binding.tvBreathDesc.setVisibility(View.VISIBLE);
        binding.rlAttempt.setVisibility(View.GONE);

        if (getIntent().getStringExtra(Constants.STR_LAST_WARN_APP) != null) {
            lastAppPackage = getIntent().getStringExtra(Constants.STR_LAST_WARN_APP);
            Log.e(TAG, "Last App's Package: " + lastAppPackage);
        }

        // When the warning page show & if user close our warning page and open
        // the fav app from recent then he can use the app as at that time
        // we save the app close time. So, by set the boolean 'false' we can solve it.
        SharedPref.writeBoolean(Utils.getIsLastAppOpenKey(lastAppPackage), false);

        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(lastAppPackage, PackageManager.GET_UNINSTALLED_PACKAGES);
            if (appInfo != null) {
                Drawable iconDrawable = packageManager.getApplicationIcon(appInfo);
                appLabel = packageManager.getApplicationLabel(appInfo).toString();
                binding.ivAppLogo.setImageDrawable(iconDrawable);
                binding.tvAppLabel.setText(appLabel);

//                binding.tvContinue.setText(String.format("%s %s", getString(R.string.strContinue), appLabel));
                binding.tvContinue.setText(getString(R.string.strContinue));
                binding.tvExit.setText(getString(R.string.exit));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Log.e(TAG, "getAppName Error: " + e.getMessage());
        }

        binding.tvContinue.setOnClickListener(view -> {
            SharedPref.writeBoolean(Utils.getIsLastAppOpenKey(lastAppPackage), true);
            finish();
        });

        binding.tvExit.setOnClickListener(view -> closeApp());

        InputStream is = getResources().openRawResource(R.raw.breathe);
        Movie movie = Movie.decodeStream(is);
        int duration = movie.duration();
        Log.e(TAG, ".gif duration: " + duration);

        new Handler().postDelayed(() -> {
            binding.tvBreathDesc.setVisibility(View.GONE);
            binding.rlAttempt.setVisibility(View.VISIBLE);
        }, duration);

        /*new Handler().postDelayed(() -> {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.nothing, R.anim.nothing, R.anim.slide_out_down);
            transaction.add(R.id.container, GreyFragment.newInstance(), "GREY");
            transaction.addToBackStack(null);
            transaction.commit();
        }, DELAY_OPEN_GREY_PAGE);*/

        checkAppAddedOrNot(DEVICE_ID, appLabel, lastAppPackage);
    }

    @Override
    public void setAttempt(int lastAttempt, String lastUsedTime) {
        super.setAttempt(lastAttempt, lastUsedTime);

//        binding.tvAttempts.setText(String.valueOf(lastAttempt));
        if (lastAttempt == 1) {
            binding.tvAttempts.setText(String.format("%s%s%s%s%s", lastAttempt, " ", getString(R.string.attempt_to_open), " " + appLabel + " ", getString(R.string.within_24_hrs)));
        } else {
            binding.tvAttempts.setText(String.format("%s%s%s%s%s", lastAttempt, " ", getString(R.string.attempts_to_open), " " + appLabel + " ", getString(R.string.within_24_hrs)));
        }

        if (lastUsedTime != null)
            binding.tvLastUse.setText(String.format("Last use: %s", lastUsedTime));
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeApp();
    }

    private void closeApp() {
        SharedPref.writeBoolean(Utils.getIsLastAppOpenKey(lastAppPackage), false);

        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(homeIntent);
        finish();
    }
}