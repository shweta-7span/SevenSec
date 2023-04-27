package com.sevensec.activities;

import static com.sevensec.utils.Constants.PREF_DEVICE_ID;
import static com.sevensec.utils.Constants.PREF_IS_LOGIN;
import static com.sevensec.utils.Constants.SPLASH_DELAY;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;

import androidx.databinding.DataBindingUtil;

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

        //Store DEVICE_ID in Preference
        @SuppressLint("HardwareIds") String DEVICE_ID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Dlog.d("OnBoardingActivity onCreate DEVICE_ID: " + DEVICE_ID);
        SharedPref.writeString(PREF_DEVICE_ID, DEVICE_ID);

        checkForLogin(getApplicationContext(), DEVICE_ID);

        binding.tvReTry.setOnClickListener(v -> checkForLogin(getApplicationContext(), DEVICE_ID));
    }

    private void checkForLogin(Context context, String device_id) {
        if (Utils.isInternetAvailable(this)) {
            binding.rlSplash.setVisibility(View.VISIBLE);
            binding.llNoInternet.setVisibility(View.GONE);
            binding.appVersion.setText(String.format("v %s", BuildConfig.VERSION_NAME));

            if (SharedPref.readBoolean(PREF_IS_LOGIN, false)) {

                new Handler().postDelayed(() -> {
                    Utils.openOnBoardingORMain(context);
                    finish();
                }, SPLASH_DELAY);

            } else {
                loginAnonymously(context, device_id, this);
            }

        } else {
            binding.rlSplash.setVisibility(View.GONE);
            binding.llNoInternet.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void authFail() {
        SharedPref.writeBoolean(PREF_IS_LOGIN, false);
    }
}