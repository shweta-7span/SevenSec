package com.sevensec.adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.viewpager.widget.PagerAdapter;

import com.sevensec.R;
import com.sevensec.activities.MainActivity;

public class CustomPagerAdapter extends PagerAdapter {

    final Context mContext;
    String[] titleList;
    String[] descriptionList;
    NextClickListener nextClickListener;
    SkipClickListener skipClickListener;

    public CustomPagerAdapter(Context context, String[] titleList, String[] descriptionList, SkipClickListener skipClickListener, NextClickListener nextClickListener) {

        mContext = context;
        this.titleList = titleList;
        this.descriptionList = descriptionList;
        this.skipClickListener = skipClickListener;
        this.nextClickListener = nextClickListener;
    }

    public interface NextClickListener {
        void onNextClick();
    }

    public interface SkipClickListener {
        void onSkipClick();
    }

    @NonNull
    @Override
    public Object instantiateItem(@NonNull ViewGroup collection, int position) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        ViewGroup layout = (ViewGroup) inflater.inflate(R.layout.raw_tab_item, collection, false);

        TextView tvTitle = layout.findViewById(R.id.tvOnBoardingTitle);
        TextView tvDescription = layout.findViewById(R.id.tvOnBoardingDesc);
        TextView tvSkip = layout.findViewById(R.id.tvSkip);
        TextView tvNext = layout.findViewById(R.id.tvNext);

        tvTitle.setText(titleList[position]);
        tvDescription.setText(descriptionList[position]);


        tvSkip.setOnClickListener(v -> {
            if (skipClickListener != null) {
                skipClickListener.onSkipClick();
            }
        });

        tvNext.setOnClickListener(v -> {
            if (nextClickListener != null) {
                nextClickListener.onNextClick();
            }
        });

        collection.addView(layout);
        return layout;
    }

    @Override
    public void destroyItem(ViewGroup collection, int position, @NonNull Object view) {
        collection.removeView((View) view);
    }

    @Override
    public int getCount() {
        return titleList.length;
    }

    @Override
    public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
        return view == object;
    }
}
