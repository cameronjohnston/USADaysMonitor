package com.caniborrowyourphone.usa;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.util.Log;
import android.widget.Toast;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;

/**
 * Created by Cameron on 12/9/2014.
 */
public class AlarmReceiver extends BroadcastReceiver {

    protected static final int NUM_BYTES_FOR_STORING_DAYS = MainActivity.NUM_BYTES_FOR_STORING_DAYS;
    protected static final String FILENAME = MainActivity.FILENAME;
    protected static final String FILENAME_COUNTRY = MainActivity.FILENAME_COUNTRY;

    FileInputStream fis;
    byte[] inputBytes, outputBytes;
    FileOutputStream fos;

    public void onReceive(Context context, Intent intent) {
        Log.d("AlarmReceiver", "Entering onReceive, calling updateDayCount method...");
        Toast.makeText(context, "Alarm!", Toast.LENGTH_SHORT).show();
        Log.d("AlarmReceiver", "Exiting onReceive");
    }
}
