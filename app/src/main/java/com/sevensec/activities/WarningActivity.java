package com.sevensec.activities;

import static com.sevensec.base.AppConstants.STR_LAST_WARN_APP;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;
import com.sevensec.R;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class WarningActivity extends AppCompatActivity {

    private String TAG;
    private String lastAppPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_warning);

        TAG = getApplicationContext().getClass().getName();

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (getIntent().getStringExtra(STR_LAST_WARN_APP) != null) {
                    lastAppPackage = getIntent().getStringExtra(STR_LAST_WARN_APP);
                    Log.e(TAG, "Last App's Package: " +  lastAppPackage);
                }

                Intent intent = new Intent(getApplicationContext(), AttemptActivity.class);
                intent.putExtra(STR_LAST_WARN_APP, lastAppPackage);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        }, 500);
    }
}