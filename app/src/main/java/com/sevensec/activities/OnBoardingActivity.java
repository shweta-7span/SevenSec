package com.sevensec.activities;

import static com.sevensec.utils.Constants.STR_DEVICE_ID;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sevensec.R;
import com.sevensec.adapter.CustomPagerAdapter;
import com.sevensec.databinding.ActivityOnBoardingBinding;
import com.sevensec.repo.FireBaseAuthOperation;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;

import java.util.Objects;

public class OnBoardingActivity extends FireBaseAuthOperation {

    ActivityOnBoardingBinding binding;
    String[] titleList;
    String[] descriptionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_on_boarding);

        titleList = getResources().getStringArray(R.array.arrOnBoardingTitleList);
        descriptionList = getResources().getStringArray(R.array.arrOnBoardingDescriptionList);

        //Store DEVICE_ID in Preference
        @SuppressLint("HardwareIds") String DEVICE_ID = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        Dlog.d("OnBoardingActivity onCreate DEVICE_ID: " + DEVICE_ID);
        SharedPref.writeString(STR_DEVICE_ID, DEVICE_ID);

        binding.viewPager.setAdapter(new CustomPagerAdapter(this, titleList, descriptionList, () -> {
            //OnClick of Skip Button
            loginAnonymously(getApplicationContext(), DEVICE_ID);
        }, () -> {
            if (binding.viewPager.getCurrentItem() != (Objects.requireNonNull(binding.viewPager.getAdapter()).getCount() - 1)) {
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
            } else {
                //OnClick of Last Next Button
                loginAnonymously(getApplicationContext(), DEVICE_ID);
            }
        }));

        binding.dotsIndicator.attachTo(binding.viewPager);
    }
}