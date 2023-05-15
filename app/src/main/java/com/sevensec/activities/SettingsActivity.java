package com.sevensec.activities;

import static com.sevensec.utils.Constants.PREF_APP_SWITCH_DURATION;
import static com.sevensec.utils.Constants.PREF_APP_SWITCH_POSITION;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import android.content.DialogInterface;
import android.os.Bundle;

import com.sevensec.R;
import com.sevensec.activities.fragments.SingleChoiceDialogFragment;
import com.sevensec.databinding.ActivitySettingsBinding;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;

public class SettingsActivity extends AppCompatActivity implements SingleChoiceDialogFragment.SingleChoiceListener {

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
    }

    private void openAppSwitchingPopup() {
        DialogFragment singleChoiceDialog = new SingleChoiceDialogFragment();
        singleChoiceDialog.setCancelable(false);
        singleChoiceDialog.show(getSupportFragmentManager(), "Single Choice Dialog");
    }

    @Override
    public void onPositiveButtonClick(int position, String selectedItem) {
        Dlog.w("onPositiveButtonClick: selectedItem: " + selectedItem);
        Dlog.w("onPositiveButtonClick: appSwitchDuration: " + Integer.parseInt(selectedItem.split(" ")[0]) * ((position == 0) ? 1 : 60));

        SharedPref.writeInteger(PREF_APP_SWITCH_POSITION, position);

        int durationInSeconds = Integer.parseInt(selectedItem.split(" ")[0]) * ((position == 0) ? 1 : 60);
        SharedPref.writeInteger(PREF_APP_SWITCH_DURATION, durationInSeconds);
    }

    @Override
    public void onNegativeButtonClick(DialogInterface dialog) {
        dialog.dismiss();
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}