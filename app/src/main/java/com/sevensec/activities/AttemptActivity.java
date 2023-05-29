package com.sevensec.activities;

import static com.sevensec.utils.Constants.PREF_BLOCK_APP_OPEN_TIME;
import static com.sevensec.utils.Constants.PREF_BREATHING_POSITION;
import static com.sevensec.utils.Constants.PREF_DEVICE_ID;
import static com.sevensec.utils.Constants.getIsUserOpenBlockAppKey;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Movie;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import com.sevensec.R;
import com.sevensec.database.AppUsageDao;
import com.sevensec.database.DatabaseHelper;
import com.sevensec.database.table.AppUsage;
import com.sevensec.databinding.ActivityAttemptBinding;
import com.sevensec.repo.FireStoreDataOperation;
import com.sevensec.repo.interfaces.SetAttemptLastOpenTime;
import com.sevensec.service.MyForegroundService;
import com.sevensec.utils.Constants;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;

import java.io.InputStream;
import java.util.Date;

public class AttemptActivity extends AppCompatActivity implements SetAttemptLastOpenTime {

    ActivityAttemptBinding binding;

    private String appLabel;
    private String appPackageName;
    private AppUsageDao appUsageDao;

    FireStoreDataOperation fireStoreDataOperation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_attempt);

        fireStoreDataOperation = new FireStoreDataOperation();
        fireStoreDataOperation.setAttemptInterface(this);

        String DEVICE_ID = SharedPref.readString(PREF_DEVICE_ID, "");
        PackageManager packageManager = getPackageManager();

        appUsageDao = DatabaseHelper.getDatabase(this).appUsageDao();

        binding.tvBreathDesc.setVisibility(View.VISIBLE);
        binding.rlAttempt.setVisibility(View.GONE);

        if (getIntent().getStringExtra(Constants.STR_LAST_WARN_APP) != null) {
            appPackageName = getIntent().getStringExtra(Constants.STR_LAST_WARN_APP);
            Dlog.e("Last App's Package: " + appPackageName);
        }

        // When the warning page show & if user close our warning page and open
        // the fav app from recent then he can use the app as at that time
        // we save the app close time. So, by set the boolean 'false' we can solve it.
        SharedPref.writeBoolean(getIsUserOpenBlockAppKey(appPackageName), false);

        try {
            ApplicationInfo appInfo = packageManager.getApplicationInfo(appPackageName, PackageManager.GET_UNINSTALLED_PACKAGES);
            if (appInfo != null) {
                Drawable iconDrawable = packageManager.getApplicationIcon(appInfo);
                appLabel = packageManager.getApplicationLabel(appInfo).toString();
                binding.ivAppLogo.setImageDrawable(iconDrawable);
                binding.tvAppLabel.setText(appLabel);
                binding.tvActionDescription.setText(String.format(getString(R.string.do_you_want_to_still), appLabel));

//                binding.tvContinue.setText(String.format("%s %s", getString(R.string.strContinue), appLabel));
                binding.tvContinue.setText(getString(R.string.strContinue));
                binding.tvExit.setText(getString(R.string.exit));
            }
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            Dlog.e("getAppName Error: " + e.getMessage());
        }

        binding.tvContinue.setOnClickListener(view -> {
            addAppOpenTimeInDB(appLabel, appPackageName, new Date(), System.currentTimeMillis());
            finish();

            //Store LastOpenedBlockApp in Service's variable
            MyForegroundService.instance.setLastApp(appPackageName);
            openBlockApp(appPackageName);
        });

        binding.tvExit.setOnClickListener(view -> closeApp());

        int breathingSelectedPosition = SharedPref.readInteger(PREF_BREATHING_POSITION, 0) + 1;

        InputStream is = getResources().openRawResource(R.raw.breathing);
        Movie movie = Movie.decodeStream(is);
        int duration = movie.duration() * breathingSelectedPosition;
        Dlog.e(".gif duration: " + duration);

        new Handler().postDelayed(() -> {
            binding.tvBreathDesc.setVisibility(View.GONE);
            binding.rlAttempt.setVisibility(View.VISIBLE);
        }, duration);

        /*new Handler().postDelayed(() -> {
            FragmentManager manager = getSupportFragmentManager();
            FragmentTransaction transaction = manager.beginTransaction();
            transaction.setCustomAnimations(R.anim.slide_in_up, R.anim.nothing, R.anim.nothing, R.anim.slide_out_down);
            transaction.add(R.id.container, GreyFragment.newInstance(), "GREY");
            transaction.addToBackStack(null);
            transaction.commit();
        }, DELAY_OPEN_GREY_PAGE);*/

        fireStoreDataOperation.checkAppAddedOrNot(DEVICE_ID, appLabel, appPackageName);
    }

    private void addAppOpenTimeInDB(String appLabel, String appPackageName, Date currentDate, long appUsageStartTime) {
        SharedPref.writeBoolean(getIsUserOpenBlockAppKey(appPackageName), true);
        SharedPref.writeLong(PREF_BLOCK_APP_OPEN_TIME, appUsageStartTime);
        appUsageDao.addAppData(new AppUsage(appLabel, appPackageName, currentDate, appUsageStartTime));
    }

    private void openBlockApp(String appPackageName) {
        Intent i = getPackageManager().getLaunchIntentForPackage(appPackageName);
        i.setFlags(Intent.FLAG_ACTIVITY_BROUGHT_TO_FRONT);
        startActivity(i);
    }

    /*@Override
    public void setAttempt(int lastAttempt, String lastUsedTime) {
        super.setAttempt(lastAttempt, lastUsedTime);
        Dlog.d("setAttempt Attempt number: " + lastAttempt);
        Dlog.d("setAttempt lastUsedTime: " + lastUsedTime);

//        binding.tvAttempts.setText(String.valueOf(lastAttempt));
        if (lastAttempt == 1) {
            binding.tvAttempts.setText(String.format("%s%s%s%s%s", lastAttempt, " ", getString(R.string.attempt_to_open), " " + appLabel + " ", getString(R.string.within_24_hrs)));
        } else {
            binding.tvAttempts.setText(String.format("%s%s%s%s%s", lastAttempt, " ", getString(R.string.attempts_to_open), " " + appLabel + " ", getString(R.string.within_24_hrs)));
        }

        if (lastUsedTime != null) {
            if (!lastUsedTime.isEmpty())
                binding.tvLastUse.setText(String.format("Last attempt to open was %s ago", lastUsedTime));
        }
    }*/

    @Override
    public void addAttemptAndTimeListener(int lastAttempt, String lastUsedTime) {
        Dlog.d("setAttempt Attempt number: " + lastAttempt);
        Dlog.d("setAttempt lastUsedTime: " + lastUsedTime);

//        binding.tvAttempts.setText(String.valueOf(lastAttempt));
        if (lastAttempt == 1) {
            binding.tvAttempts.setText(String.format("%s%s%s%s%s", lastAttempt, " ", getString(R.string.attempt_to_open), " " + appLabel + " ", getString(R.string.within_24_hrs)));
        } else {
            binding.tvAttempts.setText(String.format("%s%s%s%s%s", lastAttempt, " ", getString(R.string.attempts_to_open), " " + appLabel + " ", getString(R.string.within_24_hrs)));
        }

        if (lastUsedTime != null) {
            if (!lastUsedTime.isEmpty())
                binding.tvLastUse.setText(String.format("Last attempt to open was %s ago", lastUsedTime));
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        closeApp();
    }

    private void closeApp() {
        SharedPref.writeBoolean(getIsUserOpenBlockAppKey(appPackageName), false);

        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
        homeIntent.addCategory(Intent.CATEGORY_HOME);
        startActivity(homeIntent);
        finish();
    }
}