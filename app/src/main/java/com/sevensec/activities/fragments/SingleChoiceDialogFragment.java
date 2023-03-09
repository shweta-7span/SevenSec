package com.sevensec.activities.fragments;

import static com.sevensec.utils.Constants.STR_APP_SWITCH_DURATION;
import static com.sevensec.utils.Constants.STR_APP_SWITCH_POSITION;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.sevensec.R;
import com.sevensec.utils.SharedPref;

public class SingleChoiceDialogFragment extends DialogFragment {

    int position = 0;

    public interface SingleChoiceListener {
        void onPositiveButtonClick(int position, String selectedItem);

        void onNegativeButtonClick(DialogInterface dialog);
    }

    SingleChoiceListener mListener;

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (SingleChoiceListener) context;
        } catch (Exception e) {
            throw new ClassCastException(requireActivity() + "SingleChoiceListener must implemented.");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.MyAlertDialogTheme);
        String[] arrAppSwitchDelay = requireActivity().getResources().getStringArray(R.array.arrAppSwitchDelay);

        position = SharedPref.readInteger(STR_APP_SWITCH_POSITION, arrAppSwitchDelay.length - 1);

        builder.setTitle(R.string.select_app_switch_delay)
                .setSingleChoiceItems(arrAppSwitchDelay, position, (dialog, i) -> {
                    position = i;
                    SharedPref.writeInteger(STR_APP_SWITCH_POSITION, position);
                })
                .setPositiveButton(getString(R.string.save), (dialog, i) -> mListener.onPositiveButtonClick(position, arrAppSwitchDelay[position]))
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> mListener.onNegativeButtonClick(dialog));

        return builder.create();
    }
}