package com.sevensec.activities;

import static com.sevensec.utils.Constants.PREF_DEVICE_ID;
import static com.sevensec.utils.Constants.PREF_IS_APP_LAUNCH_FIRST_TIME;
import static com.sevensec.utils.Constants.PREF_IS_LOGIN;
import static com.sevensec.utils.Constants.SPLASH_DELAY;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.google.android.material.snackbar.Snackbar;
import com.sevensec.BuildConfig;
import com.sevensec.R;
import com.sevensec.databinding.ActivitySplashBinding;
import com.sevensec.helper.AuthFailureListener;
import com.sevensec.repo.FireBaseAuthOperation;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends FireBaseAuthOperation implements AuthFailureListener {

    ActivitySplashBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        SharedPref.init(getApplicationContext());

        binding.appVersion.setText(String.format("v %s", BuildConfig.VERSION_NAME));

        //Store DEVICE_ID in Preference
        @SuppressLint("HardwareIds") String DEVICE_ID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Dlog.d("OnBoardingActivity onCreate DEVICE_ID: " + DEVICE_ID);
        SharedPref.writeString(PREF_DEVICE_ID, DEVICE_ID);

        if (SharedPref.readBoolean(PREF_IS_LOGIN, false)) {

            binding.progressBar.setVisibility(View.GONE);
            binding.llNoInternet.setVisibility(View.GONE);

            new Handler().postDelayed(() -> {
                if (SharedPref.readBoolean(PREF_IS_APP_LAUNCH_FIRST_TIME, true)) {
                    startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));
                } else {
                    startActivity(new Intent(getApplicationContext(), MainActivity.class));
                }
                finish();
            }, SPLASH_DELAY);

        } else {
            checkForLogin(getApplicationContext(), DEVICE_ID);
        }

        binding.tvReTry.setOnClickListener(v -> checkForLogin(getApplicationContext(), DEVICE_ID));
    }

    private void checkForLogin(Context context, String device_id) {
        if (Utils.isInternetAvailable(this)) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.llNoInternet.setVisibility(View.GONE);

            loginAnonymously(context, device_id, this);

        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.llNoInternet.setVisibility(View.VISIBLE);

            showSnackBar(getApplicationContext(), binding.rlMain);
        }
    }

    @Override
    public void authFail() {
        SharedPref.writeBoolean(PREF_IS_LOGIN, false);
    }

    public static void showSnackBar(Context mcontext, View parentLayout) {
        Snackbar.make(parentLayout, mcontext.getResources().getString(R.string.no_internet_connection), Snackbar.LENGTH_LONG)
                .setBackgroundTint(ContextCompat.getColor(mcontext, android.R.color.holo_red_light))
                .show();
    }
}