package com.sevensec.activities;

import static com.sevensec.utils.Constants.PREF_DEVICE_ID;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.widget.Toast;

import androidx.databinding.DataBindingUtil;

import com.sevensec.R;
import com.sevensec.adapter.CustomPagerAdapter;
import com.sevensec.databinding.ActivityOnBoardingBinding;
import com.sevensec.helper.AuthFailureListener;
import com.sevensec.repo.FireBaseAuthOperation;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;

import java.util.Objects;

public class OnBoardingActivity extends FireBaseAuthOperation implements AuthFailureListener {

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
        SharedPref.writeString(PREF_DEVICE_ID, DEVICE_ID);

        binding.llOnBoarding.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);

        binding.viewPager.setAdapter(new CustomPagerAdapter(this, titleList, descriptionList, () -> {
            //OnClick of Skip Button
            Dlog.d("OnClick of Skip Button");
            login(DEVICE_ID);

        }, () -> {
            if (binding.viewPager.getCurrentItem() != (Objects.requireNonNull(binding.viewPager.getAdapter()).getCount() - 1)) {
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
            } else {
                //OnClick of Last Next Button
                Dlog.d("OnClick of Last Next Button");
                login(DEVICE_ID);
            }
        }));

        binding.dotsIndicator.attachTo(binding.viewPager);
    }

    private void login(String deviceId) {
        if(Utils.isInternetAvailable(this)){
            binding.llOnBoarding.setVisibility(View.GONE);
            binding.progressBar.setVisibility(View.VISIBLE);
            loginAnonymously(getApplicationContext(), deviceId, this);
        }else{
            Toast.makeText(OnBoardingActivity.this, R.string.check_internet, Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void authFail() {
        Dlog.w("authFail");
        binding.llOnBoarding.setVisibility(View.VISIBLE);
        binding.progressBar.setVisibility(View.GONE);
    }
}