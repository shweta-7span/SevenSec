package com.sevensec.activities;

import static com.sevensec.utils.Constants.PREF_APP_SWITCH_DURATION;
import static com.sevensec.utils.Constants.PREF_APP_SWITCH_POSITION;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import android.content.DialogInterface;
import android.os.Bundle;

import com.sevensec.R;
import com.sevensec.activities.fragments.BreathingTimerDialogFragment;
import com.sevensec.activities.fragments.AppSwitchDelayDialogFragment;
import com.sevensec.databinding.ActivitySettingsBinding;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;

public class SettingsActivity extends AppCompatActivity implements AppSwitchDelayDialogFragment.SingleChoiceListener{

    ActivitySettingsBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_settings);

        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);

            getSupportActionBar().setTitle(R.string.settings);
        }

        binding.llSwitchDelay.setOnClickListener(v -> openAppSwitchingPopup());
        binding.llBreathingTimer.setOnClickListener(v -> openBreathingTimerPopup());
    }

    private void openAppSwitchingPopup() {
        DialogFragment singleChoiceDialog = new AppSwitchDelayDialogFragment();
        singleChoiceDialog.setCancelable(false);
        singleChoiceDialog.show(getSupportFragmentManager(), "App Switch Dialog");
    }

    @Override
    public void onAppSwitchPositiveButtonClick(int position, String selectedItem) {
        Dlog.w("onPositiveButtonClick: selectedItem: " + selectedItem);
        Dlog.w("onPositiveButtonClick: appSwitchDuration: " + Integer.parseInt(selectedItem.split(" ")[0]) * ((position == 0) ? 1 : 60));

        SharedPref.writeInteger(PREF_APP_SWITCH_POSITION, position);

        int durationInSeconds = Integer.parseInt(selectedItem.split(" ")[0]) * ((position == 0) ? 1 : 60);
        SharedPref.writeInteger(PREF_APP_SWITCH_DURATION, durationInSeconds);
    }

    @Override
    public void onAppSwitchNegativeButtonClick(DialogInterface dialog) {
        dialog.dismiss();
    }

    private void openBreathingTimerPopup() {
        DialogFragment singleChoiceDialog = new BreathingTimerDialogFragment();
        singleChoiceDialog.setCancelable(false);
        singleChoiceDialog.show(getSupportFragmentManager(), "Breathing Timer Dialog");
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}