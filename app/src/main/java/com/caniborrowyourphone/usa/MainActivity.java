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
    protected static final String FILENAME_DAYS = "usa_days_records.txt";
    protected static final String FILENAME_COUNTRY = "current_country.txt";
    protected static final String FILENAME_TIMESTAMP = "timestamp.txt";

    private FileInputStream fis;
    private byte[] inputBytes, outputBytes, twoOutputBytes;
    private FileOutputStream fos;

    Data data;

	Button enteringCanadaButton, enteringUSAButton;
	TextView locationTV, numDaysTV;
	Calendar today, alarmCalendar;
	LocationManager locationManager;

    private AlarmReceiver ar;
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

        Log.d("MainActivity", "Exiting onCreate");
    }

    @Override
    protected void onStart() { super.onStart(); }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("MainActivity", "Entering onResume");

        data = new Data();

        inputBytes = new byte[NUM_BYTES_FOR_STORING_DAYS];
        outputBytes = new byte[NUM_BYTES_FOR_STORING_DAYS];
        twoOutputBytes = new byte[2];

        today = Calendar.getInstance(); // Initialize to current date
        initializeDayCount();

        updateDayCount();

        updateLocationDisplay();

        ar = new AlarmReceiver();
        setDailyAlarm(this.getApplicationContext());

        Log.d("MainActivity", "Exiting onResume");
    }
	
	private void setDailyAlarm(Context con) {
        Log.d("MainActivity", "Entering setDailyAlarm");
		alarmMgr = (AlarmManager) con.getSystemService(Context.ALARM_SERVICE);
		alarmIntent = new Intent(con, AlarmReceiver.class);
        alarmPendingIntent = PendingIntent.getBroadcast(con, 0, alarmIntent, 0);
		alarmCalendar = Calendar.getInstance();
		alarmCalendar.setTimeInMillis(System.currentTimeMillis());
		alarmCalendar.set(Calendar.HOUR_OF_DAY, 14);
		alarmCalendar.set(Calendar.MINUTE, 27);
		alarmMgr.setInexactRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis(), 1000*30, alarmPendingIntent);
        Log.d("MainActivity", "Exiting setDailyAlarm");
    }
	
	private void setButtonOnClickListeners() {
		enteringCanadaButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				data.currentCountry = Country.CANADA;
				updateDayCount();
				updateLocationDisplay();
			}
			
		});
		
		enteringUSAButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				data.currentCountry = Country.USA;
				updateDayCount();
				updateLocationDisplay();
			}
			
		});
	}
	
	private void updateLocationDisplay() {
		if(data.currentCountry == Country.CANADA) {
			locationTV.setText("Canada");
		}
		else if(data.currentCountry == Country.USA) {
			locationTV.setText("USA");
		}
	}
	
	private void initializeDayCount() {
        Log.d("MainActivity", "Entering initializeDayCount, calling readDaysFromFile and readCountryFromFile methods");
		readDaysFromFile();
        readCountryFromFile();
        readTimestampFromFile();
        Log.d("MainActivity", "Exiting initializeDayCount. numDaysInUSA=" + data.numDaysInUSA);
		// numDaysTV.setText(Integer.toString(data.numDaysInUSA));
	}

    private void updateDayCount() {
        Log.d("MainActivity", "Entering updateDayCount");
		if(data.currentCountry == Country.USA) { // Set today to true
			data.inUSA[today.get(Calendar.MONTH)][today.get(Calendar.DAY_OF_MONTH) - 1] = true;
            Log.d("MainActivity", "updateDayCount: currentCountry = USA. Setting entry of inUSA to true: [" + today.get(Calendar.MONTH) + "][" + (today.get(Calendar.DAY_OF_MONTH) - 1) + "]");
		}

        Log.d("MainActivity", "updateDayCount: calling writeDaysToFile method, numDaysInUSA=" + data.numDaysInUSA);
        writeDaysToFile();
        Log.d("MainActivity", "updateDayCount: calling writeCountryToFile method, currentCountry=" + data.currentCountry);
        writeCountryToFile();
        Log.d("MainActivity", "updateDayCount: calling writeTimestampToFile method, month=" + data.monthOfLastUpdate + ", day=" + data.dayOfLastUpdate);
        writeTimestampToFile();

        Log.d("MainActivity", "Exiting updateDayCount: numDaysInUSA=" + data.numDaysInUSA);
		numDaysTV.setText(Integer.toString(data.numDaysInUSA));
	}

    private void readDaysFromFile() {
        int i, j;
        data.numDaysInUSA = 0;
        boolean fileExists = true;
        try {
            fis = openFileInput(FILENAME_DAYS);
        } catch (FileNotFoundException e) {
            fileExists = false;
            for(boolean[] row : data.inUSA) {
                Arrays.fill(row, false);
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
                        data.inUSA[i][j] = true;
                        data.numDaysInUSA++;
                    }
                    else {
                        data.inUSA[i][j] = false;
                    }
                }
            }
        }
    }

     private void readCountryFromFile() {
        int numBytesRead = 0;
        try {
            fis = openFileInput(FILENAME_COUNTRY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("MainActivity", "readCountryFromFile: FileNotFoundException, returning...");
            return;
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
                Log.d("MainActivity", "readCountryFromFile: Country read as USA, setting currentCountry to USA...");
                data.currentCountry = Country.USA;
            }
            else {
                Log.d("MainActivity", "readCountryFromFile: Country read as not USA, setting currentCountry to CANADA...");
                data.currentCountry = Country.CANADA;
            }
        }
        else {
            Log.d("MainActivity", "readCountryFromFile: numBytesRead=0, returning...");
        }
     }

    private void readTimestampFromFile() {
        Log.d("MainActivity", "Entering readTimestampFromFile");
        int numBytesRead = 0;
        try {
            fis = openFileInput(FILENAME_TIMESTAMP);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d("MainActivity", "readTimestampFromFile: FileNotFoundException, returning...");
            return;
        }

        Log.d("MainActivity", "readTimestampFromFile: File exists! Reading inputBytes...");
        try {
            numBytesRead = fis.read(inputBytes, 0, 2);
            fis.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        if(numBytesRead > 1) {
            data.monthOfLastUpdate = inputBytes[0];
            data.dayOfLastUpdate = inputBytes[1];
            Log.d("MainActivity", "readTimestampFromFile: monthOfLastUpdate="+data.monthOfLastUpdate+", dayOfLastUpdate="+data.dayOfLastUpdate);
        }
        else {
            Log.d("MainActivity", "readTimestampFromFile: numBytesRead < 2, returning...");
        }
        Log.d("MainActivity", "Exiting readTimestampFromFile");
    }

    private void writeDaysToFile() {
        int i, j;
        int index = 0;
        data.numDaysInUSA = 0;
        for(i=0; i<12; i++) {
            for(j=0; j<31; j++) {
                outputBytes[index++] = (byte) (data.inUSA[i][j] ? 1 : 0);
                if(data.inUSA[i][j]) data.numDaysInUSA++; // Add day if required
            }
        }
        try {
            Log.d("MainActivity", "writeDaysToFile: Opening file: " + FILENAME_DAYS);
            fos = openFileOutput(FILENAME_DAYS, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            Log.d("MainActivity", "writeDaysToFile: Writing outputBytes to file: " + FILENAME_DAYS);
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
            fos.write(data.currentCountry.getValue());
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void writeTimestampToFile() {
        twoOutputBytes[0] = (byte) today.get(Calendar.MONTH);
        twoOutputBytes[1] = (byte) today.get(Calendar.DAY_OF_MONTH);
        try {
            Log.d("MainActivity", "writeTimestampToFile: Opening file: " + FILENAME_TIMESTAMP);
            fos = openFileOutput(FILENAME_TIMESTAMP, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            Log.d("MainActivity", "writeTimestampToFile: Writing outputBytes to file: " + FILENAME_TIMESTAMP);
            fos.write(twoOutputBytes);
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
