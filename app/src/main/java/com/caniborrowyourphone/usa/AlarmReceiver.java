package com.caniborrowyourphone.usa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Created by Cameron on 12/9/2014.
 */
public class AlarmReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Entering onReceive, starting AlarmService...");
        Intent service1 = new Intent(context, AlarmService.class);
        context.startService(service1);
        Log.d("AlarmReceiver", "Exiting onReceive");
    }


}
