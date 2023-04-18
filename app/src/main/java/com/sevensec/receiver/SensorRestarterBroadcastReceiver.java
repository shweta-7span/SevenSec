package com.sevensec.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.sevensec.service.MyForegroundService;
import com.sevensec.utils.Dlog;

public class SensorRestarterBroadcastReceiver extends BroadcastReceiver {

    final String TAG = getClass().getName();

    @Override
    public void onReceive(Context context, Intent intent) {
        Dlog.i( "MyService: onReceive: Service Stopped!");

        //Start service
        context.startService(new Intent(context, MyForegroundService.class));
    }
}
