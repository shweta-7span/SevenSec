package com.sevensec.activities;

import static com.sevensec.utils.Constants.PREF_IS_APP_LAUNCH_FIRST_TIME;
import static com.sevensec.utils.Constants.SPLASH_DELAY;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sevensec.BuildConfig;
import com.sevensec.R;
import com.sevensec.databinding.ActivitySplashBinding;
import com.sevensec.utils.SharedPref;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    ActivitySplashBinding binding;

    @SuppressLint("HardwareIds")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_splash);

        SharedPref.init(getApplicationContext());

        binding.appVersion.setText(String.format("v %s", BuildConfig.VERSION_NAME));

        new Handler().postDelayed(() -> {
            if (SharedPref.readBoolean(PREF_IS_APP_LAUNCH_FIRST_TIME, true)) {
                startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));
            } else {
                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
            }
            finish();
        }, SPLASH_DELAY);
    }
}