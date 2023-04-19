package com.sevensec.activities;

import static com.sevensec.utils.Constants.STR_APP_INFO;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Bundle;

import com.sevensec.R;
import com.sevensec.databinding.ActivityAppDetailsBinding;
import com.sevensec.model.AppInfoModel;
import com.sevensec.utils.Dlog;

import java.util.List;

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

        UsageStatsManager usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        long currentTime = System.currentTimeMillis();

        List<UsageStats> usageStatsList = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, currentTime - 1000 * 60 * 60 * 24, currentTime);

        for (UsageStats usageStats : usageStatsList) {
            if (usageStats.getPackageName().equals(appInfoModel.getPackageName())) {
                long totalTimeInForeground = usageStats.getTotalTimeInForeground();
                Dlog.d("Total time in foreground: " + totalTimeInForeground);
            }
        }
    }
}