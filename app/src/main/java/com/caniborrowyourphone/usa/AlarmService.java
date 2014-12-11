package com.caniborrowyourphone.usa;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import java.util.Calendar;

/**
 * Created by Cameron on 12/10/2014.
 */
public class AlarmService extends Service {

    @Override
    public IBinder onBind(Intent arg0) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();
    }

    @SuppressWarnings("static-access")
    @Override
    public void onStart(Intent intent, int startId) {
        super.onStart(intent, startId);
        Log.d("AlarmService", "Entering onStart");
        Toast.makeText(this.getApplicationContext(), "Alarm!", Toast.LENGTH_LONG).show();
        Log.d("AlarmService", "Exiting onStart");
    }
}