package com.sevensec.adapter;

import static com.sevensec.utils.Constants.PREF_FAV_APP_LIST;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sevensec.R;
import com.sevensec.helper.OnItemClickListener;
import com.sevensec.model.AppInfoModel;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;

import java.util.List;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {

    List<AppInfoModel> appInfoModelList;
    List<String> favAppList /*= new ArrayList<>()*/;
    private final Context mContext;
    OnItemClickListener onItemClickListener;

    public MyListAdapter(Context context, List<AppInfoModel> appInfoModelList, List<String> favAppList, OnItemClickListener onItemClickListener) {
        this.mContext = context;
        this.appInfoModelList = appInfoModelList;
        this.favAppList = favAppList;
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Dlog.w("onCreateViewHolder favAppList: " + favAppList.size());

        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View listItem = layoutInflater.inflate(R.layout.raw_list_item, parent, false);

        return new ViewHolder(listItem);
    }

    @SuppressLint("NotifyDataSetChanged")
    public void updateList(List<AppInfoModel> searchList) {
        appInfoModelList = searchList;
        notifyDataSetChanged();
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        AppInfoModel appInfoModel = appInfoModelList.get(position);
        holder.llAppInfo.setOnClickListener(v -> {
            if (favAppList.contains(appInfoModel.getPackageName())) {
                onItemClickListener.onClick(appInfoModel);
            }
        });

        holder.bind(mContext, appInfoModel, favAppList); // Pass the favorite list to the ViewHolder
    }

    @Override
    public int getItemCount() {
        return appInfoModelList.size();
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView ivRawAppIcon;
        public TextView tvRawAppName;
        public SwitchCompat rawSwitch;
        public LinearLayout llAppInfo;

        public ViewHolder(View itemView) {
            super(itemView);
            this.ivRawAppIcon = itemView.findViewById(R.id.ivRawAppIcon);
            this.tvRawAppName = itemView.findViewById(R.id.tvRawAppName);
            this.rawSwitch = itemView.findViewById(R.id.rawSwitch);
            this.llAppInfo = itemView.findViewById(R.id.llAppInfo);
        }

        public void bind(Context mContext, AppInfoModel appInfoModel, List<String> favAppList) {
            ivRawAppIcon.setImageDrawable(Utils.getDrawableFromBitmap(mContext, appInfoModel.getAppIconBitmap()));
            tvRawAppName.setText(appInfoModel.getAppName());

            rawSwitch.setTag(appInfoModel);
            rawSwitch.setOnCheckedChangeListener(null);
            rawSwitch.setChecked(appInfoModel.isFavorite());

            rawSwitch.setOnCheckedChangeListener((compoundButton, isChecked) -> {
                Dlog.d("onBindViewHolder onCheckedChanged checked: " + isChecked);
                Dlog.i("onBindViewHolder onCheckedChanged favAppList.size: " + favAppList.size());

                AppInfoModel appInfoModel1 = (AppInfoModel) compoundButton.getTag();

                if (isChecked) {
                    favAppList.add(appInfoModel1.getPackageName());
                    Dlog.d("Check favAppList.add: " + appInfoModel1.getPackageName());
                } else {
                    favAppList.remove(appInfoModel1.getPackageName());
                    Dlog.d("Check favAppList.remove: " + appInfoModel1.getPackageName());
                }

                Dlog.v("onBindViewHolder onCheckedChanged favAppList.size: " + favAppList.size());
                SharedPref.writeList(PREF_FAV_APP_LIST, favAppList);
            });
        }
    }
}
