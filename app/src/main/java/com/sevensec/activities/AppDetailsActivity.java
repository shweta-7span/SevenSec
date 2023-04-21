package com.sevensec.activities;

import static com.sevensec.utils.Constants.STR_PASS_APP_INFO;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.sevensec.R;
import com.sevensec.database.AppUsageDao;
import com.sevensec.database.DatabaseHelper;
import com.sevensec.databinding.ActivityAppDetailsBinding;
import com.sevensec.model.AppInfoModel;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.Utils;

import java.util.Date;

public class AppDetailsActivity extends AppCompatActivity {

    ActivityAppDetailsBinding binding;
    AppUsageDao appUsageDao;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_details);
        appUsageDao = DatabaseHelper.getDatabase(this).appUsageDao();

        AppInfoModel appInfoModel = getIntent().getParcelableExtra(STR_PASS_APP_INFO);

        assert appInfoModel != null;
        Dlog.d("AppName: " + appInfoModel.getAppName());
        Dlog.d("PackageName: " + appInfoModel.getPackageName());

        long totalAppUsageTime = appUsageDao.getTotalAppUsageTimeForDay(new Date());
        binding.tvAppUsageTime.setText(Utils.getTimeInFormat(totalAppUsageTime));
    }
}