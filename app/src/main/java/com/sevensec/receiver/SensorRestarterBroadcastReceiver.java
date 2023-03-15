package com.sevensec.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.sevensec.service.MyForegroundService;

public class SensorRestarterBroadcastReceiver extends BroadcastReceiver {

    final String TAG = getClass().getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(TAG, "MyService: onReceive: Service Stopped!");

        //Start service
        context.startService(new Intent(context, MyForegroundService.class));
    }
}
