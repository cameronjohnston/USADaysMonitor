package com.caniborrowyourphone.usa;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Calendar;

public class MainActivity extends ActionBarActivity {
	
	protected static final int NUM_BYTES_FOR_STORING_DAYS = 12*31;
	protected static final String FILENAME = "usa_days_records.txt";
    protected static final String FILENAME_COUNTRY = "current_country.txt";

	public static boolean[][] inUSA = new boolean[12][31];
	private int numDaysInUSA = 0;
	Button enteringCanadaButton, enteringUSAButton;
	Country currentCountry;
	TextView locationTV, numDaysTV;
	Calendar today, alarmCalendar;
	LocationManager locationManager;
	FileInputStream fis;
	byte[] inputBytes, outputBytes;
	FileOutputStream fos;
	AlarmManager alarmMgr;
	Intent alarmIntent;
	PendingIntent alarmPendingIntent;
	
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
        today = Calendar.getInstance(); // Initialize to current date
		initializeDayCount();

		updateDayCount();

        updateLocationDisplay();

        setDailyAlarm(getApplicationContext());

        Log.d("MainActivity", "Exiting onCreate");
}
	
	private void setDailyAlarm(Context con) {
        Log.d("MainActivity", "Entering setDailyAlarm");
		alarmMgr = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);
		alarmIntent = new Intent(con, AlarmReceiver.class);
		alarmCalendar = Calendar.getInstance();
		alarmCalendar.setTimeInMillis(System.currentTimeMillis());
		alarmCalendar.set(Calendar.HOUR_OF_DAY, 12);
		alarmCalendar.set(Calendar.MINUTE, 12);
		alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(), AlarmManager.INTERVAL_DAY, alarmPendingIntent);
        Log.d("MainActivity", "Exiting setDailyAlarm");
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
        Log.d("MainActivity", "Entering initializeDayCount, calling readDaysFromFile and readCountryFromFile methods");
		int numDaysInUSA = readDaysFromFile();
        boolean currentlyInUSA = readCountryFromFile();
        if(currentlyInUSA) {
            currentCountry = Country.USA;
            Log.d("MainActivity", "Setting currentCountry to USA");
        }
        else {
            currentCountry = Country.CANADA;
            Log.d("MainActivity", "Setting currentCountry to Canada");
        }
        Log.d("MainActivity", "Exiting initializeDayCount. numDaysInUSA=" + numDaysInUSA);
		numDaysTV.setText(Integer.toString(numDaysInUSA));
	}

    // RETURNS: Number of days in USA in the past year (numDaysInUSA)
    protected int readDaysFromFile() {
        int i, j;
        int dayCount = 0;
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
            Log.d("MainActivity", "readDaysFromFile: File exists! Reading inputBytes...");
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
                        Log.d("MainActivity", "readDaysFromFile: inUSA=true for day: " + (i+1) + "/" + (j+1));
                        inUSA[i][j] = true;
                        dayCount++;
                    }
                    else {
                        inUSA[i][j] = false;
                    }
                }
            }
        }
        return dayCount;
    }

    // RETURNS: True if currently in USA, false otherwise
    protected boolean readCountryFromFile() {
        int numBytesRead = 0;
        try {
            fis = openFileInput(FILENAME_COUNTRY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        Log.d("MainActivity", "readCountryFromFile: File exists! Reading inputBytes...");
        try {
            numBytesRead = fis.read(inputBytes, 0, 1);
            fis.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(numBytesRead > 0) {
            if(inputBytes[0] == Country.USA.getValue()) {
                Log.d("MainActivity", "readCountryFromFile: Country read as USA, returning true...");
                return true;
            }
            else {
                Log.d("MainActivity", "readCountryFromFile: Country read as not USA, returning false...");
                return false;
            }
        }
        else {
            Log.d("MainActivity", "readCountryFromFile: numBytesRead=0, returning false...");
            return false;
        }
    }

    protected void updateDayCount() {
        Log.d("MainActivity", "Entering updateDayCount");
		if(currentCountry == Country.USA) { // Set today to true
            Log.d("MainActivity", "updateDayCount: Currently in USA");
			inUSA[today.get(Calendar.MONTH)][today.get(Calendar.DAY_OF_MONTH) - 1] = true;
            Log.d("MainActivity", "updateDayCount: Setting entry of inUSA to true: [" + today.get(Calendar.MONTH) + "][" + (today.get(Calendar.DAY_OF_MONTH) - 1) + "]");
		}
        int i, j;
		int index = 0;
		numDaysInUSA = 0;
		for(i=0; i<12; i++) {
			for(j=0; j<31; j++) {
				outputBytes[index++] = (byte) (inUSA[i][j] ? 1 : 0);
				if(inUSA[i][j]) numDaysInUSA++; // Add day if required
			}
		}
        Log.d("MainActivity", "updateDayCount: calling writeDaysToFile method, numDaysInUSA=" + numDaysInUSA);
        writeDaysToFile();
        Log.d("MainActivity", "updateDayCount: calling writeCountryToFile method, currentCountry=" + currentCountry);
        writeCountryToFile();

        Log.d("MainActivity", "Exiting updateDayCount: numDaysInUSA=" + numDaysInUSA);
		numDaysTV.setText(Integer.toString(numDaysInUSA));
	}

    protected void writeDaysToFile() {
        try {
            Log.d("MainActivity", "writeDaysToFile: Opening file: " + FILENAME);
            fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            Log.d("MainActivity", "writeDaysToFile: Writing outputBytes to file: " + FILENAME);
            fos.write(outputBytes);
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void writeCountryToFile() {
        try {
            Log.d("MainActivity", "writeCountryToFile: Opening file: " + FILENAME_COUNTRY);
            fos = openFileOutput(FILENAME_COUNTRY, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            Log.d("MainActivity", "writeCountryToFile: Writing currentCountry to file: " + FILENAME_COUNTRY);
            fos.write(currentCountry.getValue());
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
