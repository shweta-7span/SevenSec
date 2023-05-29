package com.sevensec.activities.fragments;

import static com.sevensec.utils.Constants.PREF_BREATHING_POSITION;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.sevensec.R;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;

public class BreathingTimerDialogFragment extends DialogFragment {

    int position = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogTheme);
        String[] arrBreathingTimer = requireActivity().getResources().getStringArray(R.array.arrBreathingTimer);

        position = SharedPref.readInteger(PREF_BREATHING_POSITION, 0);

        View view = getLayoutInflater().inflate(R.layout.breathing_timer_dialog, null);

        builder.setCustomTitle(view)
                .setSingleChoiceItems(arrBreathingTimer, position, (dialog, i) -> position = i)
                .setPositiveButton(getString(R.string.save), (dialog, i) -> {
                    Dlog.d("BreathingTimerDialog onPositiveButtonClick");
                    SharedPref.writeInteger(PREF_BREATHING_POSITION, position);
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                    Dlog.e("BreathingTimerDialog onNegativeButtonClick");
                    dialog.dismiss();
                });
        return builder.create();
    }
}