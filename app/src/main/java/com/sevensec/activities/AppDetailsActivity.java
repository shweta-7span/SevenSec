package com.sevensec.activities;

import static com.sevensec.utils.Constants.STR_PASS_APP_INFO;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.View;

import com.sevensec.R;
import com.sevensec.database.AppUsageDao;
import com.sevensec.database.DatabaseHelper;
import com.sevensec.databinding.ActivityAppDetailsBinding;
import com.sevensec.model.AppInfoModel;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AppDetailsActivity extends AppCompatActivity {

    ActivityAppDetailsBinding binding;
    AppInfoModel appInfoModel;
    AppUsageDao appUsageDao;
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM");
    Calendar cal;
    Date dbFirstDate;

    String appName, packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_details);

        appUsageDao = DatabaseHelper.getDatabase(this).appUsageDao();
        cal = Calendar.getInstance();

        appInfoModel = getIntent().getParcelableExtra(STR_PASS_APP_INFO);

        assert appInfoModel != null;
        appName = appInfoModel.getAppName();
        packageName = appInfoModel.getPackageName();
        Dlog.d("AppName: " + appName);
        Dlog.d("PackageName: " + packageName);


        dbFirstDate = appUsageDao.getFirstDate(packageName);
        Dlog.d("getFirstDate: " + dateFormat.format(dbFirstDate));

        showAppUsageForCurrentDate();

        if (dateFormat.format(dbFirstDate).equals(dateFormat.format(new Date())))
            binding.ibPrev.setVisibility(View.INVISIBLE);
        binding.ibPrev.setOnClickListener(v -> changeDate(-1));

        binding.ibNext.setVisibility(View.INVISIBLE);
        binding.ibNext.setOnClickListener(v -> changeDate(1));
    }

    private void showAppUsageForCurrentDate() {
        binding.tvDate.setText(dateFormat.format(new Date()));
        showTotalUsage(new Date());
    }

    private void changeDate(int count) {
        cal.add(Calendar.DATE, count);
        binding.tvDate.setText(dateFormat.format(cal.getTime()));

        showTotalUsage(cal.getTime());

        Dlog.d("new Date: " + dateFormat.format(new Date()));
        Dlog.d("cal Date: " + dateFormat.format(cal.getTime()));

        if (dateFormat.format(new Date()).equals(dateFormat.format(cal.getTime()))) {
            binding.ibNext.setVisibility(View.INVISIBLE);
        } else {
            binding.ibNext.setVisibility(View.VISIBLE);
        }

        if (dateFormat.format(dbFirstDate).equals(dateFormat.format(cal.getTime()))) {
            binding.ibPrev.setVisibility(View.INVISIBLE);
        } else {
            binding.ibPrev.setVisibility(View.VISIBLE);
        }
    }

    private void showTotalUsage(Date date) {
        long totalAppUsageTime = appUsageDao.getTotalAppUsageTimeForDay(packageName, date);
        binding.tvAppUsageTime.setText(Utils.getAppUsageTimeInFormat(totalAppUsageTime));
    }
}