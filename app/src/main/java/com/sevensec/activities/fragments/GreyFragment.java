package com.sevensec.activities.fragments;

import static com.sevensec.utils.Constants.GRAY_PAGE_ANIMATION_TIMER;

import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.sevensec.R;

public class GreyFragment extends Fragment {

    public static GreyFragment newInstance() {
        return new GreyFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        new Handler().postDelayed(() -> {
            if (getActivity() != null) {
                getActivity().onBackPressed();
            }
        }, GRAY_PAGE_ANIMATION_TIMER);

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_grey, container, false);
    }
}