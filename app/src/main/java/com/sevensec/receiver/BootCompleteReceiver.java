package com.sevensec.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.sevensec.service.SaveMyAppsService;

public class BootCompleteReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.w("BootCompleteReceiver", "onReceive: ");

        if (Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction())) {
            //Start service
            Intent i = new Intent(context, SaveMyAppsService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(i);
            } else {
                context.startService(i);
            }

            Log.w("BootCompleteReceiver", "Service started: ");
        }
    }
}
