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

	Button enteringCanadaButton, enteringUSAButton;
	TextView locationTV, numDaysTV;
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

        initializeData();

        inputBytes = new byte[NUM_BYTES_FOR_STORING_DAYS];
        outputBytes = new byte[NUM_BYTES_FOR_STORING_DAYS];
        twoOutputBytes = new byte[2];

        initializeDayCount();

        updateDayCount();

        updateLocationDisplay();

        // ar = new AlarmReceiver();
        // setDailyAlarm(this.getApplicationContext());

        Log.d("MainActivity", "Exiting onResume");
    }

    protected void initializeData() {

        Data.inUSA = new boolean[12][31];
        Data.numDaysInUSA = 0;

        Data.monthOfLastUpdate = 0x0; // Should always be between 0-11 inclusive
        Data.dayOfLastUpdate = 0x0; // Should always be between 0-30 inclusive

        Data.today = Calendar.getInstance(); // Initialize to current date
    }
	
	private void setButtonOnClickListeners() {
		enteringCanadaButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Data.currentCountry = Country.CANADA;
				updateDayCount();
				updateLocationDisplay();
			}
			
		});
		
		enteringUSAButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
				Data.currentCountry = Country.USA;
				updateDayCount();
				updateLocationDisplay();
			}
			
		});
	}
	
	private void updateLocationDisplay() {
		if(Data.currentCountry == Country.CANADA) {
			locationTV.setText("Canada");
		}
		else if(Data.currentCountry == Country.USA) {
			locationTV.setText("USA");
		}
	}
	
	private void initializeDayCount() {
        Log.d("MainActivity", "Entering initializeDayCount, calling readDaysFromFile and readCountryFromFile methods");
		readDaysFromFile();
        readCountryFromFile();
        readTimestampFromFile();
        updateDaysSinceTimestamp();
        Log.d("MainActivity", "Exiting initializeDayCount. numDaysInUSA=" + Data.numDaysInUSA);
		// numDaysTV.setText(Integer.toString(Data.numDaysInUSA));
	}

    private void updateDayCount() {
        Log.d("MainActivity", "Entering updateDayCount");
		if(Data.currentCountry == Country.USA) { // Set today to true
			Data.inUSA[Data.today.get(Calendar.MONTH)][Data.today.get(Calendar.DAY_OF_MONTH) - 1] = true;
            Log.d("MainActivity", "updateDayCount: currentCountry = USA. Setting entry of inUSA to true: [" + Data.today.get(Calendar.MONTH) + "][" + (Data.today.get(Calendar.DAY_OF_MONTH) - 1) + "]");
		}

        Log.d("MainActivity", "updateDayCount: calling writeDaysToFile method, numDaysInUSA=" + Data.numDaysInUSA);
        writeDaysToFile();
        Log.d("MainActivity", "updateDayCount: calling writeCountryToFile method, currentCountry=" + Data.currentCountry);
        writeCountryToFile();
        Log.d("MainActivity", "updateDayCount: calling writeTimestampToFile method, month=" + Data.monthOfLastUpdate + ", day=" + Data.dayOfLastUpdate);
        writeTimestampToFile();

        Log.d("MainActivity", "Exiting updateDayCount: numDaysInUSA=" + Data.numDaysInUSA);
		numDaysTV.setText(Integer.toString(Data.numDaysInUSA));
	}

    private void updateDaysSinceTimestamp() {
        Log.d("MainActivity", "Entering updateDaysSinceTimestamp");
        int i, j; // Using i to iterate through months, j to iterate through days
        if((int)Data.monthOfLastUpdate < Data.today.get(Calendar.MONTH)) { // Updated in a previous month
            Log.d("MainActivity", "updateDaysSinceTimestamp: updated in a previous month");

            // First go through the month last updated, starting with the day after the last update
            for(j=(int)Data.dayOfLastUpdate + 1; j<getNumDaysInMonth((int)Data.monthOfLastUpdate); j++) {
                if(Data.currentCountry == Country.USA) {
                    Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+Data.monthOfLastUpdate+"]["+j+"]=true");
                    Data.inUSA[(int)Data.monthOfLastUpdate][j] = true;
                }
                else {
                    Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+Data.monthOfLastUpdate+"]["+j+"]=false");
                    Data.inUSA[(int)Data.monthOfLastUpdate][j] = false;
                }
            }

            // Now go through the months between monthOfLastUpdate and today
            for(i=(int)Data.monthOfLastUpdate + 1; i<Data.today.get(Calendar.MONTH); i++) {
                // In this case, we need to go through every day of the month
                for(j=0; j<getNumDaysInMonth(i); j++) {
                    if(Data.currentCountry == Country.USA) {
                        Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+i+"]["+j+"]=true");
                        Data.inUSA[i][j] = true;
                    }
                    else {
                        Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+i+"]["+j+"]=false");
                        Data.inUSA[i][j] = false;
                    }
                }
            }

            // Now go through the days before today in the current month
            for(j=0; j<Data.today.get(Calendar.DAY_OF_MONTH) - 1; j++) {
                if(Data.currentCountry == Country.USA) {
                    Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+Data.today.get(Calendar.MONTH)+"]["+j+"]=true");
                    Data.inUSA[Data.today.get(Calendar.MONTH)][j] = true;
                }
                else {
                    Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+Data.today.get(Calendar.MONTH)+"]["+j+"]=false");
                    Data.inUSA[Data.today.get(Calendar.MONTH)][j] = false;
                }
            }
        }

        else if((int)Data.monthOfLastUpdate > Data.today.get(Calendar.MONTH)) { // Updated in a later month, therefore must be from the previous year
            Log.d("MainActivity", "updateDaysSinceTimestamp: updated in a later month, previous year");

            // First go through the month last updated, starting with the day after the last update
            for(j=(int)Data.dayOfLastUpdate + 1; j<getNumDaysInMonth((int)Data.monthOfLastUpdate); j++) {
                if(Data.currentCountry == Country.USA) {
                    Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+Data.monthOfLastUpdate+"]["+j+"]=true");
                    Data.inUSA[(int)Data.monthOfLastUpdate][j] = true;
                }
                else {
                    Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+Data.monthOfLastUpdate+"]["+j+"]=false");
                    Data.inUSA[(int)Data.monthOfLastUpdate][j] = false;
                }
            }

            // Now go through the months between monthOfLastUpdate and end of the year
            for(i=(int)Data.monthOfLastUpdate + 1; i<12; i++) {
                // In this case, we need to go through every day of the month
                for(j=0; j<getNumDaysInMonth((int)Data.monthOfLastUpdate); j++) {
                    if(Data.currentCountry == Country.USA) {
                        Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+i+"]["+j+"]=true");
                        Data.inUSA[i][j] = true;
                    }
                    else {
                        Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+i+"]["+j+"]=false");
                        Data.inUSA[i][j] = false;
                    }
                }
            }

            // Now go through the months between start of the year and month before today
            for(i=0; i<Data.today.get(Calendar.MONTH); i++) {
                // In this case, we need to go through every day of the month
                for(j=0; j<getNumDaysInMonth((int)Data.monthOfLastUpdate); j++) {
                    if(Data.currentCountry == Country.USA) {
                        Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+i+"]["+j+"]=true");
                        Data.inUSA[i][j] = true;
                    }
                    else {
                        Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+i+"]["+j+"]=false");
                        Data.inUSA[i][j] = false;
                    }
                }
            }

            // Now go through the days before today in the current month
            for(j=0; j<Data.today.get(Calendar.DAY_OF_MONTH) - 1; j++) {
                if(Data.currentCountry == Country.USA) {
                    Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+Data.today.get(Calendar.MONTH)+"]["+j+"]=true");
                    Data.inUSA[Data.today.get(Calendar.MONTH)][j] = true;
                }
                else {
                    Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+Data.today.get(Calendar.MONTH)+"]["+j+"]=false");
                    Data.inUSA[Data.today.get(Calendar.MONTH)][j] = false;
                }
            }
        }

        else { // monthOfLastUpdate must equal today's month
            Log.d("MainActivity", "updateDaysSinceTimestamp: updated this month");
            // Go through all days after dayOfLastUpdate and before today
            for(j=(int)Data.dayOfLastUpdate + 1; j<Data.today.get(Calendar.DAY_OF_MONTH) - 1; j++) {
                if(Data.currentCountry == Country.USA) {
                    Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+Data.today.get(Calendar.MONTH)+"]["+j+"]=true");
                    Data.inUSA[Data.today.get(Calendar.MONTH)][j] = true;
                }
                else {
                    Log.d("MainActivity", "updateDaysSinceTimestamp: Setting inUSA["+Data.today.get(Calendar.MONTH)+"]["+j+"]=false");
                    Data.inUSA[Data.today.get(Calendar.MONTH)][j] = false;
                }
            }
        }

        Log.d("MainActivity", "Exiting updateDaysSinceTimestamp");
    }

    int getNumDaysInMonth(int month) {
        switch(month) {
            case 0:
                return 31;
            case 1:
                if(Data.today.get(Calendar.YEAR) % 4 == 0) return 29;
                else return 28;
            case 2:
                return 31;
            case 3:
                return 30;
            case 4:
                return 31;
            case 5:
                return 30;
            case 6:
                return 31;
            case 7:
                return 31;
            case 8:
                return 30;
            case 9:
                return 31;
            case 10:
                return 30;
            case 11:
                return 31;
            default:
                return 31;
        }
    }

    private void readDaysFromFile() {
        int i, j;
        Data.numDaysInUSA = 0;
        boolean fileExists = true;
        try {
            fis = openFileInput(FILENAME_DAYS);
        } catch (FileNotFoundException e) {
            fileExists = false;
            for(boolean[] row : Data.inUSA) {
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
                        Data.inUSA[i][j] = true;
                        Data.numDaysInUSA++;
                    }
                    else {
                        Data.inUSA[i][j] = false;
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
                Data.currentCountry = Country.USA;
            }
            else {
                Log.d("MainActivity", "readCountryFromFile: Country read as not USA, setting currentCountry to CANADA...");
                Data.currentCountry = Country.CANADA;
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
            Data.monthOfLastUpdate = inputBytes[0];
            Data.dayOfLastUpdate = inputBytes[1];
            Log.d("MainActivity", "readTimestampFromFile: monthOfLastUpdate="+Data.monthOfLastUpdate+", dayOfLastUpdate="+Data.dayOfLastUpdate);
        }
        else {
            Log.d("MainActivity", "readTimestampFromFile: numBytesRead < 2, returning...");
        }
        Log.d("MainActivity", "Exiting readTimestampFromFile");
    }

    private void writeDaysToFile() {
        int i, j;
        int index = 0;
        Data.numDaysInUSA = 0;
        for(i=0; i<12; i++) {
            for(j=0; j<31; j++) {
                outputBytes[index++] = (byte) (Data.inUSA[i][j] ? 1 : 0);
                if(Data.inUSA[i][j]) Data.numDaysInUSA++; // Add day if required
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
        if(Data.currentCountry != Country.USA) {
            Data.currentCountry = Country.CANADA;
        }
        try {
            Log.d("MainActivity", "writeCountryToFile: Opening file: " + FILENAME_COUNTRY);
            fos = openFileOutput(FILENAME_COUNTRY, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        try {
            Log.d("MainActivity", "writeCountryToFile: Writing currentCountry to file: " + FILENAME_COUNTRY);
            fos.write(Data.currentCountry.getValue());
            fos.close();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    private void writeTimestampToFile() {
        twoOutputBytes[0] = (byte) (Data.today.get(Calendar.MONTH));
        twoOutputBytes[1] = (byte) (Data.today.get(Calendar.DAY_OF_MONTH ) - 1);
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
