package com.sevensec.activities;

import static com.sevensec.utils.Constants.STR_FIRST_TIME_APP_LAUNCH;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.sevensec.R;
import com.sevensec.utils.SharedPref;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        setContentView(R.layout.activity_splash);
        SharedPref.init(getApplicationContext());

        new Handler().postDelayed(() -> {
            if (SharedPref.readBoolean(STR_FIRST_TIME_APP_LAUNCH, true)) {
                startActivity(new Intent(getApplicationContext(), OnBoardingActivity.class));
            }else{
                startActivity(new Intent(getApplicationContext(), MainActivity.class));
            }
            finish();

        }, 3000);
    }
}