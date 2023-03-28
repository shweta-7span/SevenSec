package com.sevensec.helper;

import static com.sevensec.utils.Constants.OVERLAY_REQUEST_CODE;
import static com.sevensec.utils.Constants.USAGE_ACCESS_REQUEST_CODE;
import static com.sevensec.utils.Constants.XIAOMI_OVERLAY_REQUEST_CODE;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

import androidx.appcompat.app.AlertDialog;

import com.sevensec.R;

public class PermissionDialog {
    AlertDialog.Builder permissionAlert;
    ActionClickInterface action;
    Context mContext;
    String allowPermission;
    View view;

    public PermissionDialog(Context context, ActionClickInterface action) {
        this.action = action;
        this.mContext = context;
        this.permissionAlert = new AlertDialog
                .Builder(context, R.style.MyAlertDialogTheme);
    }

    public void showAlert(String title, String description,int permissionCode) {
        LayoutInflater factory = LayoutInflater.from(mContext);
        view = factory.inflate(R.layout.permission_dialog, null);

        //        GifImageView imageView = view.findViewById(R.id.ivPermissionGif);
        ImageView imageView = view.findViewById(R.id.ivPermission);


        if (permissionCode == USAGE_ACCESS_REQUEST_CODE) {
            imageView.setImageResource(R.drawable.img_usage_access);
            allowPermission = mContext.getString(R.string.allow_usage_access_btn);
        } else if (permissionCode == OVERLAY_REQUEST_CODE) {
            imageView.setImageResource(R.drawable.img_display_over);
            allowPermission = mContext.getString(R.string.allow_overlay_btn);
        } else if (permissionCode == XIAOMI_OVERLAY_REQUEST_CODE) {
            imageView.setImageResource(R.drawable.overlay_xiaomi);
            allowPermission = mContext.getResources().getString(R.string.go_to_settings);
        } else {
            view = null;
            allowPermission = mContext.getString(R.string.disable);
        }
        permissionAlert.setTitle(title)
                .setMessage(description)
                .setView(view)
                .setCancelable(false)
                // Specifying a listener allows you to take an action before dismissing the dialog.
                // The dialog is automatically dismissed when a dialog button is clicked.
                .setPositiveButton(allowPermission, (dialog, which) -> {
                    action.onPositiveButtonClickAI();

                })
                // A null listener allows the button to dismiss the dialog and take no further action.
                .setNegativeButton(mContext.getString(R.string.cancel), null)
//                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

}
