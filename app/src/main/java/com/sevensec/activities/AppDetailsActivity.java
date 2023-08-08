package com.sevensec.activities;

import static com.sevensec.utils.Constants.ADD_DAYS_FOR_END_DATE;
import static com.sevensec.utils.Constants.ADD_DAYS_FOR_NEXT_WEEK;
import static com.sevensec.utils.Constants.DB_APP_TOTAL_TIME;
import static com.sevensec.utils.Constants.PREF_DEVICE_ID;
import static com.sevensec.utils.Constants.REMOVE_DAYS_FOR_PREV_WEEK;
import static com.sevensec.utils.Constants.START_DAY;
import static com.sevensec.utils.Constants.STR_PASS_APP_INFO;
import static com.sevensec.utils.Utils.convertDateFormat;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;

import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sevensec.R;
import com.sevensec.database.AppUsageRoomDbHelper;
import com.sevensec.databinding.ActivityAppDetailsBinding;
import com.sevensec.model.AppInfoModel;
import com.sevensec.model.AppUsageByDate;
import com.sevensec.repo.FireStoreDataOperation;
import com.sevensec.repo.interfaces.AppUsageFromFireStore;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;
import com.sevensec.utils.WeekType;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public class AppDetailsActivity extends AppCompatActivity {

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd MMM");
    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat dateFormatForDay = new SimpleDateFormat("EEE");
    ActivityAppDetailsBinding binding;
    AppInfoModel appInfoModel;
    Calendar cal;
    Date currentDate, startDate, endDate;
    String appName, packageName, device_id;
    AppUsageRoomDbHelper appUsageRoomDbHelper;
    FireStoreDataOperation fireStoreDataOperation;
    boolean isSelectedWeekHaveData = false;

    ValueFormatter yAxisFormatter = new ValueFormatter() {
        @Override
        public String getFormattedValue(float value) {
            long timeMills = (long) value;
            return Utils.getAppUsageTimeInFormat(timeMills, true);
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_app_details);

        appUsageRoomDbHelper = new AppUsageRoomDbHelper(this);
        fireStoreDataOperation = new FireStoreDataOperation();

        device_id = SharedPref.readString(PREF_DEVICE_ID, "");

        currentDate = new Date();
        cal = Calendar.getInstance();

        appInfoModel = getIntent().getParcelableExtra(STR_PASS_APP_INFO);

        assert appInfoModel != null;
        appName = appInfoModel.getAppName();
        packageName = appInfoModel.getPackageName();
        Dlog.d("AppName: " + appName);
        Dlog.d("PackageName: " + packageName);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            getSupportActionBar().setTitle(appName + " Usage");
        }

        binding.ivAppIcon.setImageBitmap(appInfoModel.getAppIconBitmap());

            if (Utils.isInternetAvailable(this)) {
                Dlog.d("totalAppUsageTime isInternetAvailable");

                fireStoreDataOperation.getTotalAppUsageTimeForDate(device_id, packageName, new AppUsageFromFireStore() {
                            @Override
                            public void getTotalAppUsageFromFireStore(Map<String, Object> datesMap) {

                                Map<String, Object> currentDateMap = (Map<String, Object>) datesMap.get(Utils.getCurrentDateInFireStoreFormat(currentDate));
                                Dlog.d("getTotalAppUsageFromFireStore currentDateMap: " + currentDateMap);

                                long totalAppUsageTimeFromFireStore = 0;

                                if (currentDateMap == null) {
                                    Dlog.d("getTotalAppUsageFromFireStore: currentDateMap is NULL");

                                } else {
                                    Dlog.d("getTotalAppUsageFromFireStore: currentDateMap is exist !");
                                    totalAppUsageTimeFromFireStore = (long) currentDateMap.get(DB_APP_TOTAL_TIME) * 1000;
                                }

                                Dlog.d("getTotalAppUsageFromFireStore totalAppUsageTimeFromFireStore: " + totalAppUsageTimeFromFireStore);
                                showAppUsageForCurrentDate(totalAppUsageTimeFromFireStore);
                            }
                        }
                );
            } else {
                Dlog.d("totalAppUsageTime isInternet NOT Available");

                long totalAppUsageTimeFromRoom = appUsageRoomDbHelper.getTotalAppUsageTimeForDate(packageName, currentDate);
                Dlog.d("totalAppUsageTime from Room: " + totalAppUsageTimeFromRoom);

                showAppUsageForCurrentDate(totalAppUsageTimeFromRoom);
            }

        //Show "UsageTime" for the week in which the user selected date include
        initAndOpenDatePicker();

        binding.ibPrev.setOnClickListener(v -> setStartEndDate(WeekType.Previous));
        binding.ibNext.setOnClickListener(v -> setStartEndDate(WeekType.Next));

        setStartEndDate(WeekType.Current);
    }

    private void showAppUsageForCurrentDate(long appUsageTime) {
        String currentDateAppUsage = Utils.getAppUsageTimeInFormat(appUsageTime, false);
        binding.tvCurrentDayUsage.setText(String.format("%s", currentDateAppUsage.isEmpty() ? "0 Sec" : currentDateAppUsage));
    }

    private void setStartEndDate(WeekType type) {
        switch (type) {
            case Current:
                cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());
                startDate = getDateForSelectedType(START_DAY);
                endDate = getDateForSelectedType(ADD_DAYS_FOR_END_DATE);
                break;

            case Previous:
                cal.setTime(startDate);
                startDate = getDateForSelectedType(REMOVE_DAYS_FOR_PREV_WEEK);
                endDate = getDateForSelectedType(ADD_DAYS_FOR_END_DATE);
                break;

            case Next:
                cal.setTime(startDate);
                startDate = getDateForSelectedType(ADD_DAYS_FOR_NEXT_WEEK);
                endDate = getDateForSelectedType(ADD_DAYS_FOR_END_DATE);
                break;
        }

        showAppUsageForSelectedDate(startDate, endDate);
    }

    private void initAndOpenDatePicker() {
        DatePickerDialog.OnDateSetListener dateSetListener = (view, year, month, dayOfMonth) -> {

            cal.set(Calendar.YEAR, year);
            cal.set(Calendar.MONTH, month);
            cal.set(Calendar.DAY_OF_MONTH, dayOfMonth);

            cal.setTime(cal.getTime());
            cal.set(Calendar.DAY_OF_WEEK, cal.getFirstDayOfWeek());

            startDate = getDateForSelectedType(START_DAY);
            endDate = getDateForSelectedType(ADD_DAYS_FOR_END_DATE);

            showAppUsageForSelectedDate(startDate, endDate);
        };

        DatePickerDialog datePickerDialog = new DatePickerDialog(AppDetailsActivity.this, R.style.DialogTheme, dateSetListener, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH));
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
        Dlog.e("showAppUsageForSelectedDate() called with: startDate = [" + startDate + "], endDate = [" + endDate + "]");
        binding.tvDate.setText(String.format("%s - %s", dateFormat.format(startDate), dateFormat.format(endDate)));
        showTotalUsage(startDate, endDate);

//        Dlog.d("showAppUsageForSelectedDate currentDate: " + currentDate);
//        Dlog.d("showAppUsageForSelectedDate startDate: " + startDate);
//        Dlog.d("showAppUsageForSelectedDate endDate: " + endDate);

        if (currentDate.compareTo(startDate) >= 0 && currentDate.compareTo(endDate) <= 0) {
            binding.ibNext.setVisibility(View.INVISIBLE);
        } else {
            binding.ibNext.setVisibility(View.VISIBLE);
        }
    }

    private void showTotalUsage(Date startDate, Date endDate) {
        Dlog.i("showTotalUsage() called with: startDate = [" + startDate + "], endDate = [" + endDate + "]");
        isSelectedWeekHaveData = false;

        Calendar firebaseCalender = Calendar.getInstance();
        firebaseCalender.setTime(startDate);
        Dlog.d("showTotalUsage() called with: device_id = [" + device_id + "]");

        if (Utils.isInternetAvailable(this)) {
            List<AppUsageByDate> fireStoreUsageByDateList = new ArrayList<>();

            fireStoreDataOperation.getTotalAppUsageTimeForDate(device_id, packageName, new AppUsageFromFireStore() {
                        @RequiresApi(api = Build.VERSION_CODES.O)
                        @Override
                        public void getTotalAppUsageFromFireStore(Map<String, Object> datesMap) {

                            Dlog.d("datesMap:->" + datesMap);
                            long totalAppUsageTimeFromFireStore = 0;
                            while (firebaseCalender.getTime().before(endDate)) {
                                try {
                                    Dlog.d("getTotalAppUsageFromFireStore() called with: datesMap = [" + firebaseCalender.getTime() + "]");

                                    String inputDateStr = firebaseCalender.getTime().toString();
                                    String outputDateFormat = "dd-MM-yyyy";
                                    String convertedDateStr = convertDateFormat(inputDateStr, outputDateFormat);

                                    Object currentMap = datesMap.get(convertedDateStr);

                                    if (currentMap != null) {
                                        Dlog.d("currentDateMap:->" + currentMap);
                                        int totalTimeSpent = 0;
                                        try {
                                            JSONObject jsonObject = new JSONObject(currentMap.toString());
                                            totalTimeSpent = (int) jsonObject.get("total_time_spent");
                                            Dlog.d("totalTimeSpent:-> " + totalTimeSpent);

                                        } catch (JSONException e) {
                                            throw new RuntimeException(e);
                                        }
                                        totalAppUsageTimeFromFireStore = totalTimeSpent * 1000L;
                                        AppUsageByDate appUsageByDate = new AppUsageByDate(dateFormatForDay.format(firebaseCalender.getTime()), totalAppUsageTimeFromFireStore);
                                        fireStoreUsageByDateList.add(appUsageByDate);
                                    } else {
                                        totalAppUsageTimeFromFireStore = 0;
                                        AppUsageByDate appUsageByDate = new AppUsageByDate(dateFormatForDay.format(firebaseCalender.getTime()), totalAppUsageTimeFromFireStore);
                                        fireStoreUsageByDateList.add(appUsageByDate);
                                    }

                                    firebaseCalender.add(Calendar.DAY_OF_MONTH, 1);
                                } catch (Exception e) {
                                    throw new RuntimeException(e);
                                }
                            }

                            if (fireStoreUsageByDateList.size() > 0) {
                                Dlog.d("run: usageByDateList.size():->" + fireStoreUsageByDateList.size());
                                binding.barChartView.setVisibility(View.VISIBLE);
                                binding.llNoData.setVisibility(View.GONE);
                                showBarChart(fireStoreUsageByDateList);
                            } else {
                                Dlog.d("run: isSelectedWeekHaveData:->" + isSelectedWeekHaveData);
                                binding.barChartView.setVisibility(View.GONE);
                                binding.llNoData.setVisibility(View.VISIBLE);
                            }
                        }
                    }
            );
        } else {
            Dlog.d("totalAppUsageTime isInternet NOT Available");
            Calendar offlineCalender = Calendar.getInstance();
            offlineCalender.setTime(startDate);
            List<AppUsageByDate> databaseUsageByDateList = new ArrayList<>();

            while (offlineCalender.getTime().before(endDate)) {

                long totalAppUsageTime = appUsageRoomDbHelper.getTotalAppUsageTimeForDate(packageName, offlineCalender.getTime());

                if (totalAppUsageTime != 0) {
                    isSelectedWeekHaveData = true;
                }
                AppUsageByDate appUsageByDate = new AppUsageByDate(dateFormatForDay.format(offlineCalender.getTime()), totalAppUsageTime);
                databaseUsageByDateList.add(appUsageByDate);

                offlineCalender.add(Calendar.DAY_OF_MONTH, 1);
            }

            Dlog.i("isSelectedWeekHaveData: " + isSelectedWeekHaveData);

            if (isSelectedWeekHaveData) {
                binding.barChartView.setVisibility(View.VISIBLE);
                binding.llNoData.setVisibility(View.GONE);
                showBarChart(databaseUsageByDateList);
            } else {
                binding.barChartView.setVisibility(View.GONE);
                binding.llNoData.setVisibility(View.VISIBLE);
            }
        }
    }

    private void showBarChart(List<AppUsageByDate> aUsageByDateList) {
        Dlog.d("showBarChart() called with: usageByDateList = [" + new Gson().toJson(aUsageByDateList) + "]");
        // Create a BarDataSet
        BarDataSet dataSet = new BarDataSet(getDataEntries(aUsageByDateList), "");
        Dlog.d("showBarChart() called with: dataSet = [" + dataSet + "]");
        boolean isEmptyData = true;

        for (int i = 0; i < aUsageByDateList.size(); i++) {
                if(aUsageByDateList.get(i).getUsage() > 0) {
                    isEmptyData =false;
                    break;
                }
        }

        if (isEmptyData) {
            binding.barChartView.setVisibility(View.GONE);
            binding.llNoData.setVisibility(View.VISIBLE);
        } else {
            // Create a BarData object
            BarData data = new BarData(dataSet);
            //Dlog.d( "showBarChart() called with: data = [" + new Gson().toJson(data) + "]");
            Dlog.d("showBarChart() called with: data = [" + new GsonBuilder().serializeSpecialFloatingPointValues().create().toJson(data)
                    + "]");
            // Set the x-axis value formatter
            binding.barChartView.setVisibility(View.VISIBLE);
            binding.llNoData.setVisibility(View.GONE);
            ValueFormatter xAxisFormatter = new ValueFormatter() {
                @Override
                public String getFormattedValue(float value) {
                    if (aUsageByDateList.size() > (int) value) {
                        return aUsageByDateList.get((int) value).getDate();
                    } else {
                        return "No Data";
                    }
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
            dataSet.setColors(ContextCompat.getColor(this, R.color.primary700));

            //Remove Lines form background
            binding.barChartView.getXAxis().setDrawGridLines(false);

            //Remove Description
            binding.barChartView.getDescription().setEnabled(false);

            //Remove extra space in bottom of X Axis
            binding.barChartView.getAxisLeft().setAxisMinimum(0f);
            binding.barChartView.getAxisRight().setAxisMinimum(0f);

            //Remove Title
            binding.barChartView.getLegend().setEnabled(true);
            binding.barChartView.getLegend().setFormSize(0f);

            //Animation
            binding.barChartView.animateY(1000);

            //Stop zooming
            binding.barChartView.setScaleEnabled(false);

            binding.barChartView.setData(data);
            binding.barChartView.invalidate();
        }

    }

    private List<BarEntry> getDataEntries(List<AppUsageByDate> usageByDateList) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < usageByDateList.size(); i++) {
            entries.add(new BarEntry(i, usageByDateList.get(i).getUsage()));
        }
        return entries;
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /*private long getTotalAppUsageTime(Date date) {
        long totalAppUsageTime;
        if (Utils.isInternetAvailable(this)) {
            totalAppUsageTime = fireStoreDataOperation.getTotalAppUsageTimeForDate(device_id, packageName) * 1000;
        } else {
            totalAppUsageTime = appUsageRoomDbHelper.getTotalAppUsageTimeForDate(packageName, date);
        }
        return totalAppUsageTime;
    }*/
}