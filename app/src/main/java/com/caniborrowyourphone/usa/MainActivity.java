package com.caniborrowyourphone.usa;

import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

/**
 * Class for launcher activity of USA Days Monitor app
 * Created by Cameron Johnston on 12/1/2014
 */
public class MainActivity extends ActionBarActivity {

    private static final String tag = "MainActivity";
    private final Handler handler = new Handler();

    private FileInputStream fis;
    private byte[] inputBytes, outputBytes, twoOutputBytes;
    private FileOutputStream fos;

    Button enteringCanadaButton, enteringUSAButton;
	TextView locationTV, numDaysTV, loggedInAsTV;
	LocationManager locationManager;
    LocationListener locationListener;
    Location currentLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(tag, "Entering onCreate");

        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        enteringCanadaButton = (Button) findViewById(R.id.enteringCanadaButton);
        enteringUSAButton = (Button) findViewById(R.id.enteringUSAButton);
        enteringCanadaButton.setBackgroundResource(R.drawable.canadaflag2000x1000_usaflagred);
        enteringUSAButton.setBackgroundResource(R.drawable.usaflag800x421_revisedblue2);

        setButtonOnClickListeners();
        setLocationListener();

        locationTV = (TextView) findViewById(R.id.locationTextView);
        numDaysTV = (TextView) findViewById(R.id.numDaysTextView);
        loggedInAsTV = (TextView) findViewById(R.id.loggedInAsTextView);

        Log.d(tag, "Exiting onCreate");
    }

    @Override
    protected void onStart() { super.onStart(); }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(tag, "Entering onResume");

        initializeData();

        inputBytes = new byte[Data.NUM_BYTES_FOR_STORING_DAYS];
        outputBytes = new byte[Data.NUM_BYTES_FOR_STORING_DAYS];
        twoOutputBytes = new byte[2];

        if(Data.email != null) writeEmailToFile();
        initializeDayCount();
        updateDayCount();
        updateLocationDisplay();
        updateUserDisplay();

        new GetDataTask(this).execute();

        Log.d(tag, "Exiting onResume");
    }

    private class GetDataTask extends AsyncTask<String, Void, String> {

        private final static String tag = "FillSpinnerTask";
        private Context context;

        public GetDataTask(Context con) {
            Log.d(tag, "Entering constructor");
            this.context = con;
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(tag, "Entering doInBackground");
            try{
                String result;
                InputStream isr;

                HttpClient httpclient = new DefaultHttpClient();
                HttpPost httppost = new HttpPost("http://johnstonclan.ca/readUserData.php?email=cameron@johnstonclan.ca");
                HttpResponse response = httpclient.execute(httppost);
                HttpEntity entity = response.getEntity();
                isr = entity.getContent();

                // Convert response to string
                BufferedReader reader = new BufferedReader(new InputStreamReader(isr,"iso-8859-1"),8);
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    Log.d(tag, "Reading line: "+line);
                    sb.append(line/* + "\n"*/);
                }
                isr.close();

                result=sb.toString();

                // Parse json data
                String s = "";
                JSONArray jArray = new JSONArray(result);

                for(int i=0; i<jArray.length();i++){
                    JSONObject json = jArray.getJSONObject(i);
                    s = s +
                            "\njan : "+json.getString("jan")+"\nfeb : "+json.getString("feb")+"\nmar : "+json.getString("mar")+
                            "\napr : "+json.getString("apr")+"\nmay : "+json.getString("may")+"\njun : "+json.getString("jun")+
                            "\njul : "+json.getString("jul")+"\naug : "+json.getString("aug")+"\nsep : "+json.getString("sep")+
                            "\noct : "+json.getString("oct")+"\nnov : "+json.getString("nov")+"\ndec : "+json.getString("dec")+
                            "\ncurrentlyInUSA : "+json.getString("currentlyInUSA")+"\nmonth : "+json.getString("monthOfLastUpdate")
                            +"\nday : "+json.getString("dayOfLastUpdate");
                }

                Log.d(tag, "getData: final string="+s);

            }catch(Exception e){
                return "Exception: " + e.getMessage();
            }
            return "";
        }
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
                if(Data.USING_DUMMY_LOCATION_SERVICES) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
                        (new GetAddressTask(getApplicationContext())).execute(createLocation("lsd", 49.25, -123.1, 3.0f)); // Vancouver, BC
                    }
                }
                else {
                    Data.currentCountry = Country.CANADA;
                    Data.today = Calendar.getInstance();
                    updateDayCount();
                    updateLocationDisplay();
                }
			}
			
		});
		
		enteringUSAButton.setOnClickListener(new OnClickListener() {

			@Override
			public void onClick(View v) {
                if(Data.USING_DUMMY_LOCATION_SERVICES) {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
                        (new GetAddressTask(getApplicationContext())).execute(createLocation("lsd", 49, -123.0647, 3.0f)); // Point Roberts, Washington
                    }
                }
                else {
                    Data.currentCountry = Country.USA;
                    Data.today = Calendar.getInstance();
                    updateDayCount();
                    updateLocationDisplay();
                }
			}
			
		});
	}

    private void setLocationListener() {
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Ensure that a Geocoder services is available
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
                    Log.d(tag, "onLocationChanged: Geocoder present, starting new GetAddressTask in background thread");
                    (new GetAddressTask(getApplicationContext())).execute(location);
                }
            }
            @Override
            public void onStatusChanged(String provider, int status, Bundle extras) {}
            @Override
            public void onProviderEnabled(String provider) {}
            @Override
            public void onProviderDisabled(String provider) {}
        };
    }

    protected Location createLocation(String provider, double lat, double lng, float accuracy) {
        // Create a new Location
        Location newLocation = new Location(provider);
        newLocation.setLatitude(lat);
        newLocation.setLongitude(lng);
        newLocation.setAccuracy(accuracy);
        return newLocation;
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
        Log.d(tag, "Entering initializeDayCount, calling read methods");
		readDaysFromFile();
        readCountryFromFile();
        readTimestampFromFile();
        readEmailFromFile();
        updateDaysSinceTimestamp();
        Log.d(tag, "Exiting initializeDayCount. numDaysInUSA=" + Data.numDaysInUSA);
	}

    private void updateDayCount() {
        Log.d(tag, "Entering updateDayCount");
		if(Data.currentCountry == Country.USA) { // Set today to true
			Data.inUSA[Data.today.get(Calendar.MONTH)][Data.today.get(Calendar.DAY_OF_MONTH) - 1] = true;
            Log.d(tag, "updateDayCount: currentCountry = USA. Setting entry of inUSA to true: [" + Data.today.get(Calendar.MONTH) + "][" + (Data.today.get(Calendar.DAY_OF_MONTH) - 1) + "]");
		}

        Log.d(tag, "updateDayCount: calling writeDaysToFile method, numDaysInUSA=" + Data.numDaysInUSA);
        writeDaysToFile();
        Log.d(tag, "updateDayCount: calling writeCountryToFile method, currentCountry=" + Data.currentCountry);
        writeCountryToFile();
        Log.d(tag, "updateDayCount: calling writeTimestampToFile method, month=" + Data.monthOfLastUpdate + ", day=" + Data.dayOfLastUpdate);
        writeTimestampToFile();

        Log.d(tag, "Exiting updateDayCount: numDaysInUSA=" + Data.numDaysInUSA);
		numDaysTV.setText(Integer.toString(Data.numDaysInUSA));
	}

    private void updateUserDisplay() {
        try {
            if (Data.email.equals("")) {
                loggedInAsTV.setVisibility(View.INVISIBLE);
            } else {
                loggedInAsTV.setText(Data.email);
                loggedInAsTV.setVisibility(View.VISIBLE);
            }
        }
        catch (NullPointerException e) {
            loggedInAsTV.setVisibility(View.INVISIBLE);
            e.printStackTrace();
        }
    }
    /**
     * Updates whether the user was in the USA for each day from the last update to today
     * Covers all cases, including the last update being from a previous month or year or today
     */
    private void updateDaysSinceTimestamp() {
        Log.d(tag, "Entering updateDaysSinceTimestamp");
        int i, j; // Using i to iterate through months, j to iterate through days
        if((int)Data.monthOfLastUpdate < Data.today.get(Calendar.MONTH)) { // Updated in a previous month
            Log.d(tag, "updateDaysSinceTimestamp: updated in a previous month");

            // First go through the month last updated, starting with the day after the last update
            for(j=(int)Data.dayOfLastUpdate + 1; j<getNumDaysInMonth((int)Data.monthOfLastUpdate); j++) {
                if(Data.currentCountry == Country.USA) {
                    Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+Data.monthOfLastUpdate+"]["+j+"]=true");
                    Data.inUSA[(int)Data.monthOfLastUpdate][j] = true;
                }
                else {
                    Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+Data.monthOfLastUpdate+"]["+j+"]=false");
                    Data.inUSA[(int)Data.monthOfLastUpdate][j] = false;
                }
            }

            // Now go through the months between monthOfLastUpdate and today
            for(i=(int)Data.monthOfLastUpdate + 1; i<Data.today.get(Calendar.MONTH); i++) {
                // In this case, we need to go through every day of the month
                for(j=0; j<getNumDaysInMonth(i); j++) {
                    if(Data.currentCountry == Country.USA) {
                        Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+i+"]["+j+"]=true");
                        Data.inUSA[i][j] = true;
                    }
                    else {
                        Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+i+"]["+j+"]=false");
                        Data.inUSA[i][j] = false;
                    }
                }
            }

            // Now go through the days up to and including today in the current month
            for(j=0; j<Data.today.get(Calendar.DAY_OF_MONTH); j++) {
                if(Data.currentCountry == Country.USA) {
                    Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+Data.today.get(Calendar.MONTH)+"]["+j+"]=true");
                    Data.inUSA[Data.today.get(Calendar.MONTH)][j] = true;
                }
                else {
                    Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+Data.today.get(Calendar.MONTH)+"]["+j+"]=false");
                    Data.inUSA[Data.today.get(Calendar.MONTH)][j] = false;
                }
            }
        }

        else if((int)Data.monthOfLastUpdate > Data.today.get(Calendar.MONTH)) { // Updated in a later month, therefore must be from the previous year
            Log.d(tag, "updateDaysSinceTimestamp: updated in a later month, previous year");

            // First go through the month last updated, starting with the day after the last update
            for(j=(int)Data.dayOfLastUpdate + 1; j<getNumDaysInMonth((int)Data.monthOfLastUpdate); j++) {
                if(Data.currentCountry == Country.USA) {
                    Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+Data.monthOfLastUpdate+"]["+j+"]=true");
                    Data.inUSA[(int)Data.monthOfLastUpdate][j] = true;
                }
                else {
                    Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+Data.monthOfLastUpdate+"]["+j+"]=false");
                    Data.inUSA[(int)Data.monthOfLastUpdate][j] = false;
                }
            }

            // Now go through the months between monthOfLastUpdate and end of the year
            for(i=(int)Data.monthOfLastUpdate + 1; i<12; i++) {
                // In this case, we need to go through every day of the month
                for(j=0; j<getNumDaysInMonth((int)Data.monthOfLastUpdate); j++) {
                    if(Data.currentCountry == Country.USA) {
                        Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+i+"]["+j+"]=true");
                        Data.inUSA[i][j] = true;
                    }
                    else {
                        Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+i+"]["+j+"]=false");
                        Data.inUSA[i][j] = false;
                    }
                }
            }

            // Now go through the months between start of the year and month before today
            for(i=0; i<Data.today.get(Calendar.MONTH); i++) {
                // In this case, we need to go through every day of the month
                for(j=0; j<getNumDaysInMonth((int)Data.monthOfLastUpdate); j++) {
                    if(Data.currentCountry == Country.USA) {
                        Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+i+"]["+j+"]=true");
                        Data.inUSA[i][j] = true;
                    }
                    else {
                        Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+i+"]["+j+"]=false");
                        Data.inUSA[i][j] = false;
                    }
                }
            }

            // Now go through the days up to and including today in the current month
            for(j=0; j<Data.today.get(Calendar.DAY_OF_MONTH); j++) {
                if(Data.currentCountry == Country.USA) {
                    Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+Data.today.get(Calendar.MONTH)+"]["+j+"]=true");
                    Data.inUSA[Data.today.get(Calendar.MONTH)][j] = true;
                }
                else {
                    Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+Data.today.get(Calendar.MONTH)+"]["+j+"]=false");
                    Data.inUSA[Data.today.get(Calendar.MONTH)][j] = false;
                }
            }
        }

        else { // monthOfLastUpdate must equal today's month
            Log.d(tag, "updateDaysSinceTimestamp: updated this month");
            // Go through all days after dayOfLastUpdate up to and including today
            for(j=(int)Data.dayOfLastUpdate + 1; j<Data.today.get(Calendar.DAY_OF_MONTH); j++) {
                if(Data.currentCountry == Country.USA) {
                    Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+Data.today.get(Calendar.MONTH)+"]["+j+"]=true");
                    Data.inUSA[Data.today.get(Calendar.MONTH)][j] = true;
                }
                else {
                    Log.d(tag, "updateDaysSinceTimestamp: Setting inUSA["+Data.today.get(Calendar.MONTH)+"]["+j+"]=false");
                    Data.inUSA[Data.today.get(Calendar.MONTH)][j] = false;
                }
            }
        }

        Log.d(tag, "Exiting updateDaysSinceTimestamp");
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
        int i, j, numBytesRead;
        Data.numDaysInUSA = 0;
        boolean fileExists = true;
        try {
            fis = openFileInput(Data.FILENAME_DAYS);
        } catch (FileNotFoundException e) {
            fileExists = false;
            for(boolean[] row : Data.inUSA) {
                Arrays.fill(row, false);
            }
            e.printStackTrace();
        }
        if(fileExists) {
            Log.d(tag, "readDaysFromFile: File exists! Reading inputBytes...");
            try {
                numBytesRead = fis.read(inputBytes);
                fis.close();
                if(numBytesRead < Data.NUM_BYTES_FOR_STORING_DAYS) {
                    Log.d(tag, "readDaysFromFile: Less bytes than expected read from file. Returning...");
                    return;
                }
            } catch (IOException e) {
                Log.e(tag, "IOException in readDaysFromFile when trying to read from FileInputStream");
                e.printStackTrace();
            }
            for(i=0; i<12; i++) { // For each month
                for(j=0; j<31; j++) { // For each day of the month
                    if(inputBytes[i*31 + j] > 0) {
                        Log.d(tag, "readDaysFromFile: inUSA=true for day: " + (i+1) + "/" + (j+1));
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
            fis = openFileInput(Data.FILENAME_COUNTRY);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(tag, "readCountryFromFile: FileNotFoundException, returning...");
            return;
        }

        Log.d(tag, "readCountryFromFile: File exists! Reading inputBytes...");
        try {
            numBytesRead = fis.read(inputBytes, 0, 1);
            fis.close();
        } catch (IOException e) {
            Log.e(tag, "readCountryFromFile: IOException when trying to read from FileInputStream");
            e.printStackTrace();
        }
        if(numBytesRead > 0) {
            if(inputBytes[0] == Country.USA.getValue()) {
                Log.d(tag, "readCountryFromFile: Country read as USA, setting currentCountry to USA...");
                Data.currentCountry = Country.USA;
            }
            else {
                Log.d(tag, "readCountryFromFile: Country read as not USA, setting currentCountry to CANADA...");
                Data.currentCountry = Country.CANADA;
            }
        }
        else {
            Log.d(tag, "readCountryFromFile: numBytesRead=0, returning...");
        }
     }

    private void readTimestampFromFile() {
        Log.d(tag, "Entering readTimestampFromFile");
        int numBytesRead = 0;
        try {
            fis = openFileInput(Data.FILENAME_TIMESTAMP);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(tag, "readTimestampFromFile: FileNotFoundException, returning...");
            return;
        }

        Log.d(tag, "readTimestampFromFile: File exists! Reading inputBytes...");
        try {
            numBytesRead = fis.read(inputBytes, 0, 2);
            fis.close();
        } catch (IOException e) {
            Log.e(tag, "readTimestampFromFile: IOException when trying to read from FileInputStream");
            e.printStackTrace();
        }
        if(numBytesRead > 1) {
            Data.monthOfLastUpdate = inputBytes[0];
            Data.dayOfLastUpdate = inputBytes[1];
            Log.d(tag, "readTimestampFromFile: monthOfLastUpdate="+Data.monthOfLastUpdate+", dayOfLastUpdate="+Data.dayOfLastUpdate);
        }
        else {
            Log.d(tag, "readTimestampFromFile: numBytesRead < 2, returning...");
        }
        Log.d(tag, "Exiting readTimestampFromFile");
    }

    private void readEmailFromFile() {
        Log.d(tag, "Entering readEmailFromFile");
        String temp = "";
        int c;
        try {
            fis = openFileInput(Data.FILENAME_EMAIL);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Log.d(tag, "readEmailFromFile: FileNotFoundException, returning...");
            return;
        }

        Log.d(tag, "readEmailFromFile: File exists! Reading inputBytes...");
        try {
            while( (c = fis.read()) != -1){
                temp += Character.toString((char)c);
            }
            fis.close();
        } catch (IOException e) {
            Log.e(tag, "readEmailFromFile: IOException when trying to read from FileInputStream");
            e.printStackTrace();
        }
        Data.email = temp;
        Log.d(tag, "Exiting readEmailFromFile, email="+temp);
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
            Log.d(tag, "writeDaysToFile: Opening file: " + Data.FILENAME_DAYS);
            fos = openFileOutput(Data.FILENAME_DAYS, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e1) {
            Log.e(tag, "writeDaysToFile: FileNotFoundException");
            e1.printStackTrace();
        }
        try {
            Log.d(tag, "writeDaysToFile: Writing outputBytes to file: " + Data.FILENAME_DAYS);
            fos.write(outputBytes);
            fos.close();
        } catch (IOException e) {
            Log.e(tag, "writeDaysToFile: IOException when trying to write to FileOutputStream");
            e.printStackTrace();
        }
    }

    private void writeCountryToFile() {
        if(Data.currentCountry != Country.USA) {
            Data.currentCountry = Country.CANADA;
        }
        try {
            Log.d(tag, "writeCountryToFile: Opening file: " + Data.FILENAME_COUNTRY);
            fos = openFileOutput(Data.FILENAME_COUNTRY, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e1) {
            Log.e(tag, "writeCountryToFile: FileNotFoundException");
            e1.printStackTrace();
        }
        try {
            Log.d(tag, "writeCountryToFile: Writing currentCountry to file: " + Data.FILENAME_COUNTRY);
            fos.write(Data.currentCountry.getValue());
            fos.close();
        } catch (IOException e) {
            Log.e(tag, "writeCountryToFile: IOException when trying to write to FileOutputStream");
            e.printStackTrace();
        }
    }

    private void writeTimestampToFile() {
        twoOutputBytes[0] = (byte) (Data.today.get(Calendar.MONTH));
        twoOutputBytes[1] = (byte) (Data.today.get(Calendar.DAY_OF_MONTH ) - 1);
        try {
            Log.d(tag, "writeTimestampToFile: Opening file: " + Data.FILENAME_TIMESTAMP);
            fos = openFileOutput(Data.FILENAME_TIMESTAMP, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e1) {
            Log.e(tag, "writeTimestampToFile: FileNotFoundException");
            e1.printStackTrace();
        }
        try {
            Log.d(tag, "writeTimestampToFile: Writing outputBytes to file: " + Data.FILENAME_TIMESTAMP);
            fos.write(twoOutputBytes);
            fos.close();
        } catch (IOException e) {
            Log.e(tag, "writeTimestampToFile: IOException when trying to write to FileOutputStream");
            e.printStackTrace();
        }
    }

    private void writeEmailToFile() {
        try {
            try {
                Log.d(tag, "writeEmailToFile: Opening file: " + Data.FILENAME_EMAIL);
                fos = openFileOutput(Data.FILENAME_EMAIL, Context.MODE_PRIVATE);
            } catch (FileNotFoundException e1) {
                Log.e(tag, "writeEmailToFile: FileNotFoundException");
                e1.printStackTrace();
            }
            try {
                Log.d(tag, "writeEmailToFile: Writing email to file: " + Data.email);
                if (Data.email != null) fos.write(Data.email.getBytes());
                fos.close();
            } catch (IOException e) {
                Log.e(tag, "writeEmailToFile: IOException when trying to write to FileOutputStream");
                e.printStackTrace();
            }
        }
        catch(NullPointerException e) {
            e.printStackTrace();
        }
    }

    public void viewSettings(View view) {
        Log.d(tag, "Entering viewSettings");
        Intent i = new Intent(this, SettingsActivity.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
        Log.d(tag, "Exiting viewSettings. SettingsActivity should have just started.");
    }
	
	public void viewCalendar(View view) {
        Log.d(tag, "Entering viewCalendar");
        /*
        { // Using the "View Calendar" button as an "update location" button as well
            currentLocation = locationManager.getLastKnownLocation(locationManager.getAllProviders().get(0));
            Log.d(tag, "viewCalendar: Current Location=" + currentLocation.toString());
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
                Log.d(tag, "viewCalendar: Geocoder present, starting new GetAddressTask in background thread");
                (new GetAddressTask(getApplicationContext())).execute(currentLocation);
            }
        }
        */
        Intent i = new Intent(this, CalendarView.class);
        i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        startActivity(i);
        Log.d(tag, "Exiting viewCalendar. ViewCalendar activity should have just started.");
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
        return id == R.id.action_settings || super.onOptionsItemSelected(item);
    }


    // This class copied from: http://developer.android.com/training/location/display-address.html
    private class GetAddressTask extends AsyncTask<Location, Void, String> {
        Context mContext;
        public GetAddressTask(Context context) {
            super();
            mContext = context;
        }
        /**
         * Get a Geocoder instance, get the latitude and longitude
         * look up the address, and return it
         *
         * @param params One or more Location objects
         * @return A string containing the address of the current
         * location, or an empty string if no address can be found,
         * or an error message
         */
        @Override
        protected String doInBackground(Location... params) {
            Log.d("GetAddressTask", "Entering doInBackground");
            Geocoder geocoder = new Geocoder(mContext, Locale.getDefault());
            // Get the current location from the input parameter list
            Location loc = params[0];
            Log.d("GetAddressTask", "doInBackground: location lat="+loc.getLatitude()+", long="+loc.getLongitude());
            // Create a list to contain the result address
            List<Address> addresses;
            try {
                /*
                 * Return 1 address.
                 */
                addresses = geocoder.getFromLocation(loc.getLatitude(),
                        loc.getLongitude(), 1);
            } catch (IOException e1) {
                Log.e("LocationSampleActivity",
                        "IO Exception in getFromLocation()");
                e1.printStackTrace();
                return ("IO Exception trying to get address");
            } catch (IllegalArgumentException e2) {
                // Error message to post in the log
                String errorString = "Illegal arguments " +
                        Double.toString(loc.getLatitude()) +
                        " , " +
                        Double.toString(loc.getLongitude()) +
                        " passed to address service";
                Log.e("LocationSampleActivity", errorString);
                e2.printStackTrace();
                return errorString;
            }
            // If the reverse geocode returned an address
            if (addresses != null && addresses.size() > 0) {
                // Get the first address
                Address address = addresses.get(0);
                /*
                 * Format the first line of address (if available),
                 * city, and country name.
                 */
                Log.d("GetAddressTask", "doInBackground - got address: "+address.toString());
                String addressText = String.format(
                        "%s, %s, %s",
                        // If there's a street address, add it
                        address.getMaxAddressLineIndex() > 0 ?
                                address.getAddressLine(0) : "",
                        // Locality is usually a city
                        address.getLocality(),
                        // The country of the address
                        address.getCountryName());
                // Return the text
                Log.d("GetAddressTask", "doInBackground: setting countryFromLocation to "+address.getCountryName());
                String countryFromLocation = address.getCountryName();
                Data.currentCountry = (countryFromLocation.equals("United States") ? Country.USA : Country.CANADA);
                Data.today = Calendar.getInstance();
                handler.post(new Runnable() {
                    public void run() {
                        updateDayCount();
                        updateLocationDisplay();
                    }
                });
                return addressText;
            } else {
                return "No address found";
            }
        }
    }

}
