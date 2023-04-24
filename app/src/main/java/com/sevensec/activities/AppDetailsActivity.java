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
import com.sevensec.model.AppUsageByDate;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.Utils;


import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AppDetailsActivity extends AppCompatActivity {

    ActivityAppDetailsBinding binding;
    AppInfoModel appInfoModel;
    AppUsageDao appUsageDao;
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("EEE, dd MMM");
    Calendar cal;
    Date dbFirstDate, currentDate;
    String appName, packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_details);

        appUsageDao = DatabaseHelper.getDatabase(this).appUsageDao();
        currentDate = new Date();
        cal = Calendar.getInstance();

        appInfoModel = getIntent().getParcelableExtra(STR_PASS_APP_INFO);

        assert appInfoModel != null;
        appName = appInfoModel.getAppName();
        packageName = appInfoModel.getPackageName();
        Dlog.d("AppName: " + appName);
        Dlog.d("PackageName: " + packageName);

        dbFirstDate = appUsageDao.getFirstDate(packageName);
        Dlog.d("getFirstDate: " + dateFormat.format(dbFirstDate));

        initAndOpenDatePicker();

        binding.ibPrev.setOnClickListener(v -> {
//            cal.add(Calendar.DATE, -1);
//            showAppUsageForSelectedDate(cal.getTime());

            cal.add(Calendar.WEEK_OF_YEAR, -1);
//            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

            cal.add(Calendar.DAY_OF_WEEK, -6);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startDate = cal.getTime(); // start date of previous week

            cal.add(Calendar.DAY_OF_WEEK, 6);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date endDate = cal.getTime();

            Dlog.d("startDate: " + dateFormat.format(startDate));
            Dlog.d("endDate: " + dateFormat.format(endDate));

            showAppUsageForSelectedDate(cal.getTime(), startDate, endDate);

        });
        binding.ibNext.setOnClickListener(v -> {
//            cal.add(Calendar.DATE, 1);
//            showAppUsageForSelectedDate(cal.getTime());

            cal.add(Calendar.DAY_OF_WEEK, 1);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date startDate = cal.getTime(); // start date of next week

            cal.add(Calendar.DAY_OF_WEEK, 6);
            cal.set(Calendar.HOUR_OF_DAY, 0);
            cal.set(Calendar.MINUTE, 0);
            cal.set(Calendar.SECOND, 0);
            cal.set(Calendar.MILLISECOND, 0);
            Date endDate = cal.getTime();

            Dlog.d("startDate: " + dateFormat.format(startDate));
            Dlog.d("endDate: " + dateFormat.format(endDate));

            showAppUsageForSelectedDate(cal.getTime(), startDate, endDate);
        });

        cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date startDate = cal.getTime();

        cal.add(Calendar.DAY_OF_WEEK, 6);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        Date endDate = cal.getTime();

        Dlog.d("startDate: " + dateFormat.format(startDate));
        Dlog.d("endDate: " + dateFormat.format(endDate));

        showAppUsageForSelectedDate(currentDate, startDate, endDate);
    }

    private void initAndOpenDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {
            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            //TODO: NEED TO SHOW THE WEEK IN WHICH THE SELECTED DATE IS INCLUDE
//            showAppUsageForSelectedDate(cal.getTime());
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(AppDetailsActivity.this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(dbFirstDate.getTime());
        datePickerDialog.getDatePicker().setMaxDate(currentDate.getTime());

        binding.tvDate.setOnClickListener(v -> datePickerDialog.show());
    }

    private void showAppUsageForSelectedDate(Date selectedDate, Date startDate, Date endDate) {
        binding.tvDate.setText(dateFormat.format(startDate) + " - " + dateFormat.format(endDate));
//        showTotalUsage(selectedDate);
        showTotalUsage(startDate, endDate);

        Dlog.d("showAppUsageForSelectedDate currentDate: " + currentDate);
        Dlog.d("showAppUsageForSelectedDate dbFirstDate: " + dbFirstDate);
        Dlog.d("showAppUsageForSelectedDate selectedDate: " + selectedDate);
        Dlog.d("showAppUsageForSelectedDate startDate: " + startDate);
        Dlog.d("showAppUsageForSelectedDate endDate: " + endDate);

        if (currentDate.compareTo(startDate) >= 0 && currentDate.compareTo(endDate) <= 0) {
            binding.ibNext.setVisibility(View.INVISIBLE);
        } else {
            binding.ibNext.setVisibility(View.VISIBLE);
        }

        if (dbFirstDate.compareTo(startDate) >= 0 && dbFirstDate.compareTo(endDate) <= 0) {
            binding.ibPrev.setVisibility(View.INVISIBLE);
        } else {
            binding.ibPrev.setVisibility(View.VISIBLE);
        }
    }

    private void showTotalUsage(Date startDate, Date endDate) {
//        long totalAppUsageTime = appUsageDao.getTotalAppUsageTimeForDay(packageName, date);
//        binding.tvAppUsageTime.setText(Utils.getAppUsageTimeInFormat(totalAppUsageTime));

        List<AppUsageByDate> totalAppUsageTime = appUsageDao.getTotalAppUsageTimeForDay(packageName, startDate, endDate);
        Dlog.e("totalAppUsageTime: " + totalAppUsageTime);

        StringBuilder s = new StringBuilder(100);

        for (int i = 0; i < totalAppUsageTime.size(); i++) {
            Dlog.d("Date: " + totalAppUsageTime.get(i).date);
            Dlog.d("Usage: " + Utils.getAppUsageTimeInFormat(totalAppUsageTime.get(i).total_usage));

            s.append(totalAppUsageTime.get(i).date + " :" + Utils.getAppUsageTimeInFormat(totalAppUsageTime.get(i).total_usage) + "\n");
        }
        binding.tvAppUsageTime.setText(s);
    }
}