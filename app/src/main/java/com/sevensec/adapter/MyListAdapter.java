package com.sevensec.adapter;

import static com.sevensec.utils.Constants.STR_APP_INFO;
import static com.sevensec.utils.Constants.STR_FAV_APP_LIST;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.sevensec.R;
import com.sevensec.activities.AppDetailsActivity;
import com.sevensec.model.AppInfoModel;
import com.sevensec.utils.Dlog;
import com.sevensec.utils.SharedPref;
import com.sevensec.utils.Utils;

import java.util.List;

public class MyListAdapter extends RecyclerView.Adapter<MyListAdapter.ViewHolder> {

    List<AppInfoModel> appInfoModelList;
    List<String> favAppList /*= new ArrayList<>()*/;

    private final Context mContext;

    public MyListAdapter(Context context, List<AppInfoModel> appInfoModelList, List<String> favAppList) {
        this.mContext = context;
        this.appInfoModelList = appInfoModelList;
        this.favAppList = favAppList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        Dlog.w("onBindViewHolder favAppList: " + favAppList.size());

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

        holder.ivRawAppIcon.setImageDrawable(Utils.getDrawableFromBitmap(mContext, appInfoModelList.get(position).getAppIconBitmap()));
        holder.tvRawAppName.setText(appInfoModelList.get(position).getAppName());

        if (favAppList.size() > 0) {

            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                favAppList.stream().filter(o -> o.getAppName().equals(appInfoModelList.get(position).getAppName())).forEach(o -> {
                    holder.rawSwitch.setChecked(true);
                });
            }else{
                List<String> appNameList = new ArrayList<>();

                for (AppInfoModel appInfoModel: favAppList) {
                    appNameList.add(appInfoModel.getAppName());
                }

                if(appNameList.contains(appInfoModelList.get(position).getAppName())){
                    holder.rawSwitch.setChecked(true);
                    Dlog.d( "onBindViewHolder favAppList added");
                }else{
                    holder.rawSwitch.setChecked(false);
                    Dlog.d( "onBindViewHolder favAppList NotAdded");
                }
            }*/

            if (favAppList.contains(appInfoModelList.get(position).getPackageName())) {
                holder.rawSwitch.setChecked(true);
                Dlog.d("onBindViewHolder favAppList added");
            } else {
                holder.rawSwitch.setChecked(false);
                Dlog.d("onBindViewHolder favAppList NotAdded");
            }

        } else {
            holder.rawSwitch.setChecked(false);
        }

        holder.rawSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                Dlog.d("onBindViewHolder onCheckedChanged checked: " + b);

                if (b) {
                    favAppList.add(appInfoModelList.get(holder.getAdapterPosition()).getPackageName());
                } else {
                    favAppList.remove(appInfoModelList.get(holder.getAdapterPosition()).getPackageName());
                }

                Dlog.d("onBindViewHolder onCheckedChanged favAppList: " + favAppList.size());
                SharedPref.writeList(STR_FAV_APP_LIST, favAppList);
            }
        });

        holder.llAppInfo.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), AppDetailsActivity.class);
            intent.putExtra(STR_APP_INFO, appInfoModelList.get(holder.getAdapterPosition()));
            v.getContext().startActivity(intent);
        });
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
    }
}
