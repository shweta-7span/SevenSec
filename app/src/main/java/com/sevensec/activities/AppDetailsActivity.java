package com.sevensec.activities;

import static com.sevensec.utils.Constants.STR_APP_INFO;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.sevensec.R;
import com.sevensec.databinding.ActivityAppDetailsBinding;
import com.sevensec.model.AppInfoModel;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;

public class AppDetailsActivity extends AppCompatActivity {

    ActivityAppDetailsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_details);

        AppInfoModel appInfoModel = getIntent().getParcelableExtra(STR_APP_INFO);

        assert appInfoModel != null;
        Dlog.d("AppName: " + appInfoModel.getAppName());
        Dlog.d("PackageName: " + appInfoModel.getPackageName());

        long totalAppUsageTime = SharedPref.readLong(Utils.getAppUsageKey(appInfoModel.getPackageName()), 0);
        binding.tvAppUsageTime.setText(Utils.getTimeInFormat(totalAppUsageTime));
    }
}