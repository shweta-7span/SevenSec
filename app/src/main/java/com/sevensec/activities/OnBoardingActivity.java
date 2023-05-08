package com.sevensec.activities;

import static com.sevensec.utils.Constants.PREF_IS_APP_LAUNCH_FIRST_TIME;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sevensec.R;
import com.sevensec.adapter.CustomPagerAdapter;
import com.sevensec.databinding.ActivityOnBoardingBinding;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;

import java.util.Objects;

public class OnBoardingActivity extends AppCompatActivity {

    ActivityOnBoardingBinding binding;
    String[] titleList;
    String[] descriptionList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_on_boarding);

        titleList = getResources().getStringArray(R.array.arrOnBoardingTitleList);
        descriptionList = getResources().getStringArray(R.array.arrOnBoardingDescriptionList);

        binding.viewPager.setAdapter(new CustomPagerAdapter(this, titleList, descriptionList, () -> {

            //OnClick of Skip Button
            openLoginScreen();

        }, () -> {

            if (binding.viewPager.getCurrentItem() != (Objects.requireNonNull(binding.viewPager.getAdapter()).getCount() - 1)) {
                binding.viewPager.setCurrentItem(binding.viewPager.getCurrentItem() + 1);
            } else {
                //OnClick of Last Next Button
                Dlog.d("OnClick of Last Next Button");
                openLoginScreen();
            }
        }));

        binding.dotsIndicator.attachTo(binding.viewPager);
    }

    private void openLoginScreen() {

        SharedPref.writeBoolean(PREF_IS_APP_LAUNCH_FIRST_TIME, false);

        Utils.isLogin(this);
        finish();
    }
}