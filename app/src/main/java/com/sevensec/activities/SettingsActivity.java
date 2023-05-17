package com.sevensec.activities;

import static com.sevensec.utils.Constants.PREF_APP_SWITCH_DURATION;
import static com.sevensec.utils.Constants.PREF_APP_SWITCH_POSITION;
import static com.sevensec.utils.Constants.PREF_GOOGLE_AUTH_USER_NAME;
import static com.sevensec.utils.Constants.PREF_GOOGLE_AUTH_USER_PIC;
import static com.sevensec.utils.Constants.PREF_IS_GOOGLE_LOGIN_DONE;

import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.DialogFragment;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;

import com.bumptech.glide.Glide;
import com.sevensec.BuildConfig;
import com.sevensec.R;
import com.sevensec.activities.fragments.BreathingTimerDialogFragment;
import com.sevensec.activities.fragments.AppSwitchDelayDialogFragment;
import com.sevensec.databinding.ActivitySettingsBinding;
import com.sevensec.repo.FireBaseAuthOperation;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;

public class SettingsActivity extends FireBaseAuthOperation implements AppSwitchDelayDialogFragment.SingleChoiceListener {

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

        if (SharedPref.readBoolean(PREF_IS_GOOGLE_LOGIN_DONE, false)) {

            binding.llLogin.setVisibility(View.GONE);
            binding.btnLogout.setVisibility(View.VISIBLE);

            // Name, email address, and profile photo Url
            String name = SharedPref.readString(PREF_GOOGLE_AUTH_USER_NAME, "");
            Uri photoUrl = Uri.parse(SharedPref.readString(PREF_GOOGLE_AUTH_USER_PIC, "photoUrl.toString()"));

            Dlog.d("googleAuthUser name: " + name);
            Dlog.d("googleAuthUser photoUrl: " + photoUrl);

            Glide.with(getApplicationContext())
                    .load(photoUrl)
                    .into(binding.ivUser);
            binding.tvUsername.setText(name);
        } else {
            binding.llLogin.setVisibility(View.VISIBLE);
            binding.btnLogout.setVisibility(View.GONE);
        }

        binding.llLogin.setOnClickListener(v -> openLoginScreen());
        binding.llSwitchDelay.setOnClickListener(v -> openAppSwitchingPopup());
        binding.llBreathingTimer.setOnClickListener(v -> openBreathingTimerPopup());
        binding.llShare.setOnClickListener(v -> openShareAppPopup());
        binding.btnLogout.setOnClickListener(v -> logout(getApplicationContext()));
    }

    private void openLoginScreen() {
        startActivity(new Intent(getApplicationContext(), LoginActivity.class));
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

    private void openShareAppPopup() {
        try {
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            String shareMessage = getString(R.string.share_app_message);
            shareMessage = shareMessage + "https://play.google.com/store/apps/details?id=" + BuildConfig.APPLICATION_ID + "\n";
            shareIntent.putExtra(Intent.EXTRA_TEXT, shareMessage);
            startActivity(Intent.createChooser(shareIntent, "choose one"));
        } catch (Exception e) {
            Dlog.e("Share App Error: " + e.getMessage());//e.toString();
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}