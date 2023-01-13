package com.sevensec.activities;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.os.Handler;

import com.sevensec.R;


public class GreyActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        overridePendingTransition(R.anim.slide_in_up, R.anim.nothing);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grey);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                finish();
                GreyActivity.this.overridePendingTransition(R.anim.nothing, R.anim.slide_out_up);
            }
        }, 3000);
    }
}