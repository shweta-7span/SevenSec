package com.sevensec.activities;

import static com.sevensec.utils.Constants.STR_PASS_APP_INFO;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
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

        DatePickerDialog.OnDateSetListener date = (view, year, month, dayOfMonth) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            showAppUsageForSelectedDate(cal.getTime());
        };

        binding.tvDate.setOnClickListener(v -> new DatePickerDialog(AppDetailsActivity.this, date, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH)).show());

        binding.ibPrev.setOnClickListener(v -> {
            cal.add(Calendar.DATE, -1);
            showAppUsageForSelectedDate(cal.getTime());
        });
        binding.ibNext.setOnClickListener(v -> {
            cal.add(Calendar.DATE, 1);
            showAppUsageForSelectedDate(cal.getTime());
        });

        showAppUsageForSelectedDate(new Date());
    }

    private void showAppUsageForSelectedDate(Date selectedDate) {
        binding.tvDate.setText(dateFormat.format(selectedDate));
        showTotalUsage(selectedDate);

        Dlog.d("current Date: " + dateFormat.format(new Date()));
        Dlog.d("selected Date: " + dateFormat.format(selectedDate));

        if (dateFormat.format(selectedDate).equals(dateFormat.format(new Date()))) {
            binding.ibNext.setVisibility(View.INVISIBLE);
        } else {
            binding.ibNext.setVisibility(View.VISIBLE);
        }

        if (dateFormat.format(selectedDate).equals(dateFormat.format(dbFirstDate))) {
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