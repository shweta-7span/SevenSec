package com.sevensec.activities.fragments;

import android.app.AlertDialog;
import android.app.Dialog;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.sevensec.R;
import com.sevensec.utils.Dlog;

public class BreathingTimerDialogFragment extends DialogFragment {

    int position = 0;

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogTheme);
        String[] arrBreathingTimer = requireActivity().getResources().getStringArray(R.array.arrBreathingTimer);

        View view = getLayoutInflater().inflate(R.layout.breathing_timer_dialog, null);

//        builder.setTitle(R.string.select_app_switch_delay)
        builder.setCustomTitle(view)
                .setSingleChoiceItems(arrBreathingTimer, position, (dialog, i) -> position = i)
                .setPositiveButton(getString(R.string.save), (dialog, i) -> Dlog.d("BreathingTimerDialog onPositiveButtonClick"))
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> {
                    Dlog.d("BreathingTimerDialog onNegativeButtonClick");
                    dialog.dismiss();
                });
        return builder.create();
    }
}