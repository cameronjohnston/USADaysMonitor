package com.caniborrowyourphone.usa;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Calendar;

import android.support.v7.app.ActionBarActivity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends ActionBarActivity {
	
	private static final int NUM_BYTES_FOR_STORING_DAYS = 12*31;
	private static final String FILENAME = "usa_days_records.txt";

	public static boolean[][] inUSA = new boolean[12][31];
	private int numDaysInUSA = 0;
	Button enteringCanadaButton, enteringUSAButton;
	Country currentCountry = Country.CANADA;
	TextView locationTV, numDaysTV;
	Calendar today, alarmCalendar;
	LocationManager locationManager;
	File file;
	InputStream in;
	FileInputStream fis;
	StringBuffer fileContent;
	String records;
	byte[] inputBytes, outputBytes;
	int n;
	FileOutputStream fos;
	AlarmManager alarmMgr;
	Intent alarmIntent;
	PendingIntent alarmPendingIntent;
	// ByteBuffer buffer;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Log.d("MainActivity", "Entering onCreate");

		setContentView(R.layout.activity_main);
		
		locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
		
		enteringCanadaButton = (Button) findViewById(R.id.enteringCanadaButton);
		enteringUSAButton = (Button) findViewById(R.id.enteringUSAButton);
		setButtonOnClickListeners();
		
		locationTV = (TextView) findViewById(R.id.locationTextView);
		numDaysTV = (TextView) findViewById(R.id.numDaysTextView);
		
		inputBytes = new byte[NUM_BYTES_FOR_STORING_DAYS];
		outputBytes = new byte[NUM_BYTES_FOR_STORING_DAYS];
		initializeDayCount();
		
		today = Calendar.getInstance(); // Initialize to current date
		inUSA[today.get(Calendar.MONTH)][today.get(Calendar.DAY_OF_MONTH) - 1] = false;
		updateDayCount();

        Log.d("MainActivity", "Exiting onCreate");
}
	
	private void setDailyAlarm() {
		alarmMgr = (AlarmManager) this.getSystemService(Context.ALARM_SERVICE);
		//alarmIntent = new Intent(this, AlarmReceiver.class);
		alarmCalendar = Calendar.getInstance();
		alarmCalendar.setTimeInMillis(System.currentTimeMillis());
		alarmCalendar.set(Calendar.HOUR_OF_DAY, 0);
		alarmCalendar.set(Calendar.MINUTE, 0);
		alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmPendingIntent);
	}
	
	private void setButtonOnClickListeners() {
		enteringCanadaButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				currentCountry = Country.CANADA;
				updateDayCount();
				updateLocationDisplay();
			}
			
		});
		
		enteringUSAButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				currentCountry = Country.USA;
				updateDayCount();
				updateLocationDisplay();
			}
			
		});
	}
	
	private void updateLocationDisplay() {
		if(currentCountry == Country.CANADA) {
			locationTV.setText("Canada");
		}
		else if(currentCountry == Country.USA) {
			locationTV.setText("USA");
		}
	}
	
	private void initializeDayCount() {
        Log.d("MainActivity", "Entering initializeDayCount");
		int i, j;
		numDaysInUSA = 0;
		boolean fileExists = true;
		try {
			fis = openFileInput(FILENAME);
		} catch (FileNotFoundException e) {
			fileExists = false;
			for(boolean[] row : inUSA) {
				Arrays.fill(row,  false);
			}
			e.printStackTrace();
		}
		if(fileExists) {
            Log.d("MainActivity", "initializeDayCount: File exists! Reading inputBytes...");
			try {
				fis.read(inputBytes);
				fis.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for(i=0; i<12; i++) { // For each month
				for(j=0; j<31; j++) { // For each day of the month
					if(inputBytes[i*31 + j] > 0) {
                        Log.d("MainActivity", "initializeDayCount: inUSA=true for day: " + i+1 + "/" + j+1);
						inUSA[i][j] = true;
						numDaysInUSA++;
					}
					else {
						inUSA[i][j] = false;
					}
				}
			}
		}
        Log.d("MainActivity", "Exiting initializeDayCount. numDaysInUSA=" + numDaysInUSA);
		numDaysTV.setText(Integer.toString(numDaysInUSA));
	}
	
	private void updateDayCount() {
        Log.d("MainActivity", "Entering updateDayCount");
		if(currentCountry == Country.USA) { // Set today to true
            Log.d("MainActivity", "updateDayCount: Currently in USA");
			inUSA[today.get(Calendar.MONTH)][today.get(Calendar.DAY_OF_MONTH - 1)] = true;
		}
		int index = 0;
		numDaysInUSA = 0;
		for(boolean[] row : inUSA) {
			for(boolean b : row) {
				outputBytes[index++] = (byte) (b ? 1 : 0);
				if(b) numDaysInUSA++; // Add day if required
			}
		}
		try {
            Log.d("MainActivity", "updateDayCount: Opening file: " + FILENAME);
			fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		try {
            Log.d("MainActivity", "updateDayCount: Writing outputBytes to file: " + FILENAME);
			fos.write(outputBytes);
			fos.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        Log.d("MainActivity", "Exiting updateDayCount: numDaysInUSA=" + numDaysInUSA);
		numDaysTV.setText(Integer.toString(numDaysInUSA));
	}
	
	public void viewCalendar(View view) {
        Log.d("MainActivity", "Entering viewCalendar");
		Intent i = new Intent(this, CalendarView.class);
		startActivity(i);
        Log.d("MainActivity", "Exiting viewCalendar. ViewCalendar activity should have just started.");
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
