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
import com.sevensec.utils.WeekType;


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
    Date dbFirstDate, currentDate, startDate, endDate;
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

        //Show "UsageTime" for the week in which the user selected date include
        initAndOpenDatePicker();

        binding.ibPrev.setOnClickListener(v -> setStartEndDate(WeekType.Previous));
        binding.ibNext.setOnClickListener(v -> setStartEndDate(WeekType.Next));

        setStartEndDate(WeekType.Current);
    }

    private void setStartEndDate(WeekType type) {
        switch (type) {
            case Current:
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                startDate = getDateForSelectedType(1);
                endDate = getDateForSelectedType(6);
                break;

            case Previous:
                cal.setTime(startDate);
                startDate = getDateForSelectedType(-7);
                endDate = getDateForSelectedType(6);
                break;

            case Next:
                cal.setTime(startDate);
                startDate = getDateForSelectedType(7);
                endDate = getDateForSelectedType(6);
                break;
        }

        Dlog.d("Check startDate: " + dateFormat.format(startDate));
        Dlog.d("Check endDate: " + dateFormat.format(endDate));

        showAppUsageForSelectedDate(startDate, endDate);
    }

    private void initAndOpenDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {

            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            cal.setTime(cal.getTime());
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

            startDate = getDateForSelectedType(1);
            endDate = getDateForSelectedType(6);

            Dlog.d("Pick startDate: " + dateFormat.format(startDate));
            Dlog.d("Pick endDate: " + dateFormat.format(endDate));

            showAppUsageForSelectedDate(startDate, endDate);
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(AppDetailsActivity.this, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
        datePickerDialog.getDatePicker().setMinDate(dbFirstDate.getTime());
        datePickerDialog.getDatePicker().setMaxDate(currentDate.getTime());

        binding.tvDate.setOnClickListener(v -> datePickerDialog.show());
    }

    private Date getDateForSelectedType(int numberOfDays) {
        cal.add(Calendar.DAY_OF_WEEK, numberOfDays);
        cal.set(Calendar.HOUR_OF_DAY, 0);
        cal.set(Calendar.MINUTE, 0);
        cal.set(Calendar.SECOND, 0);
        cal.set(Calendar.MILLISECOND, 0);
        return cal.getTime();
    }

    private void showAppUsageForSelectedDate(Date startDate, Date endDate) {
        binding.tvDate.setText(String.format("%s - %s", dateFormat.format(startDate), dateFormat.format(endDate)));
        showTotalUsage(startDate, endDate);

        Dlog.d("showAppUsageForSelectedDate currentDate: " + currentDate);
        Dlog.d("showAppUsageForSelectedDate dbFirstDate: " + dbFirstDate);
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
        // Get appUsage for each day of the selected week
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(startDate);
        StringBuilder s = new StringBuilder(100);

        while (calendar.getTime().before(endDate) || calendar.getTime().equals(endDate)) {
            long totalAppUsageTime = appUsageDao.getTotalAppUsageTimeForDay(packageName, calendar.getTime());

            s.append(dateFormat.format(calendar.getTime()))
                    .append(" :")
                    .append(Utils.getAppUsageTimeInFormat(totalAppUsageTime))
                    .append("\n");
            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        binding.tvAppUsageTime.setText(s);
    }
}