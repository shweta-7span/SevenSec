package com.sevensec.activities.fragments;

import static com.sevensec.utils.Constants.PREF_APP_SWITCH_POSITION;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import com.sevensec.R;
import com.sevensec.utils.SharedPref;

public class AppSwitchDelayDialogFragment extends DialogFragment {

    int position = 0;

    public interface SingleChoiceListener {
        void onAppSwitchPositiveButtonClick(int position, String selectedItem);

        void onAppSwitchNegativeButtonClick(DialogInterface dialog);
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

        position = SharedPref.readInteger(PREF_APP_SWITCH_POSITION, arrAppSwitchDelay.length - 1);

        View view = getLayoutInflater().inflate(R.layout.app_switch_delay_dialog, null);

//        builder.setTitle(R.string.select_app_switch_delay)
        builder.setCustomTitle(view)
                .setSingleChoiceItems(arrAppSwitchDelay, position, (dialog, i) -> position = i)
                .setPositiveButton(getString(R.string.save), (dialog, i) -> mListener.onAppSwitchPositiveButtonClick(position, arrAppSwitchDelay[position]))
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> mListener.onAppSwitchNegativeButtonClick(dialog));
        return builder.create();
    }
}