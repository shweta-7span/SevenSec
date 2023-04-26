package com.sevensec.activities;

import static com.sevensec.utils.Constants.STR_PASS_APP_INFO;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.sevensec.R;
import com.sevensec.database.AppUsageDao;
import com.sevensec.database.DatabaseHelper;
import com.sevensec.databinding.ActivityAppDetailsBinding;
import com.sevensec.model.AppInfoModel;
import com.sevensec.model.AppUsageByDate;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.Utils;
import com.sevensec.utils.WeekType;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class AppDetailsActivity extends AppCompatActivity {

    ActivityAppDetailsBinding binding;
    AppInfoModel appInfoModel;
    AppUsageDao appUsageDao;
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormatForDay = new SimpleDateFormat("EEE");
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

        binding.ivAppIcon.setImageBitmap(appInfoModel.getAppIconBitmap());
        binding.tvAppName.setText(appName);

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

        List<AppUsageByDate> usageByDateList = new ArrayList<>();

        while (calendar.getTime().before(endDate) || calendar.getTime().equals(endDate)) {

            long totalAppUsageTime = appUsageDao.getTotalAppUsageTimeForDay(packageName, calendar.getTime());

            AppUsageByDate appUsageByDate = new AppUsageByDate(dateFormatForDay.format(calendar.getTime()), totalAppUsageTime);
            usageByDateList.add(appUsageByDate);

            calendar.add(Calendar.DAY_OF_MONTH, 1);
        }

        showBarChart(usageByDateList);
    }

    private void showBarChart(List<AppUsageByDate> usageByDateList) {
        // Create a BarDataSet
        BarDataSet dataSet = new BarDataSet(getDataEntries(usageByDateList), "App Usage");

        // Create a BarData object
        BarData data = new BarData(dataSet);

        // Set the x-axis value formatter
        ValueFormatter xAxisFormatter = new ValueFormatter() {
            @Override
            public String getFormattedValue(float value) {
                return usageByDateList.get((int) value).getDate();
            }
        };
        XAxis xAxis = binding.barChartView.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTextSize(12f);
        xAxis.setValueFormatter(xAxisFormatter);

        YAxis yAxis = binding.barChartView.getAxisLeft();
        yAxis.setTextSize(12f);
        yAxis.setValueFormatter(yAxisFormatter);

        //Remove label from Right Side
        binding.barChartView.getAxisRight().setEnabled(false);

        //Show formatted value on the bar
        dataSet.setValueFormatter(barValueFormatter);
        dataSet.setValueTextSize(10f);

        //Set Color of Bar
        dataSet.setColor(R.color.primary500);

        //Remove Lines form background
        binding.barChartView.getXAxis().setDrawGridLines(false);

        //Remove Description
        binding.barChartView.getDescription().setEnabled(false);

        //Remove extra space in bottom of X Axis
        binding.barChartView.getAxisLeft().setAxisMinimum(0f);
        binding.barChartView.getAxisRight().setAxisMinimum(0f);

        //Remove Title
        binding.barChartView.getLegend().setEnabled(false);

        binding.barChartView.setData(data);
        binding.barChartView.invalidate();
    }

    ValueFormatter yAxisFormatter = new ValueFormatter() {
        @Override
        public String getFormattedValue(float value) {
            long timeMills = (long) value;
            return  Utils.getAppUsageTimeInFormat(timeMills, true);
        }
    };

    //Set the Bar Value formatter
    ValueFormatter barValueFormatter = new ValueFormatter() {
        @Override
        public String getFormattedValue(float value) {
            Dlog.i("App Usage: " + Utils.getAppUsageTimeInFormat((long) value, false));
            return Utils.getAppUsageTimeInFormat((long) value, false);
        }
    };

    private List<BarEntry> getDataEntries(List<AppUsageByDate> usageByDateList) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < usageByDateList.size(); i++) {
            entries.add(new BarEntry(i, usageByDateList.get(i).getUsage()));
        }
        return entries;
    }
}