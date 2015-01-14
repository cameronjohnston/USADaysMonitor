package com.caniborrowyourphone.usa;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.telephony.TelephonyManager;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
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

    android.support.v7.app.ActionBar actionBar;
    InputMethodManager imm;
    Dialog dialog, dialog2;

    private FileInputStream fis;
    private byte[] inputBytes, outputBytes, twoOutputBytes;
    private FileOutputStream fos;

    Button enteringCanadaButton, enteringUSAButton;
	TextView headerTV, locationTV, numDaysTV, loggedInAsTV;

	LocationManager locationManager;
    LocationListener locationListener;
    ConnectivityManager connManager;
    NetworkInfo mWifi;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(tag, "Entering onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        actionBar = getSupportActionBar();
        actionBar.setDisplayOptions(android.support.v7.app.ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar);

        ((TextView) findViewById(R.id.titleTextView)).setText("USA Days Monitor");
        findViewById(R.id.backButton).setVisibility(View.INVISIBLE);
        findViewById(R.id.backTextView).setVisibility(View.INVISIBLE);

        connManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        enteringCanadaButton = (Button) findViewById(R.id.enteringCanadaButton);
        enteringUSAButton = (Button) findViewById(R.id.enteringUSAButton);
        enteringCanadaButton.setBackgroundResource(R.drawable.canadaflag2000x1000_usaflagred);
        enteringUSAButton.setBackgroundResource(R.drawable.usaflag800x421_revisedblue2);

        setButtonOnClickListeners();
        setLocationListener(); // LocationListener will only be used if locationManager is set to receive updates

        headerTV = (TextView) findViewById(R.id.headerTextView);
        locationTV = (TextView) findViewById(R.id.locationTextView);
        numDaysTV = (TextView) findViewById(R.id.numDaysTextView);
        loggedInAsTV = (TextView) findViewById(R.id.loggedInAsTextView);

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        Log.d(tag, "Exiting onCreate");
    }

    @Override
    protected void onStart() { super.onStart(); }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(tag, "Entering onResume");

        initializeData();

        readEmailFromFile();
        try {
            if(Data.email.equals("")) {
                Log.d(tag, "onResume: e-mail=\"\". Setting loggedIn=false, skipping initializations and calling login().");
                Data.loggedIn = false;
                login("");
            }
            else {
                Log.d(tag, "onResume: e-mail="+Data.email+", loggedIn=true. Writing e-mail to file and performing initializations.");
                Data.loggedIn = true;
                writeEmailToFile();
                readLocalData();
                if(Data.usingLocation) {
                    String provider = ((TelephonyManager)this.getSystemService(Context.TELEPHONY_SERVICE)).getNetworkOperatorName();
                    try {
                        if (provider.equals("")) {
                            Log.d(tag, "No provider found, cannot set locationManager to send updates to LocationListener.");
                        }
                        else {
                            Log.d(tag, "Found provider: "+provider+". Setting locationManager to send updates to LocationListener.");
                            locationManager.requestLocationUpdates(provider, 1000 * 60 * 10, 1000, locationListener);
                        }
                    } catch(NullPointerException e) {
                        Log.d(tag, "Provider=null, cannot set locationManager to send updates to LocationListener.");
                        e.printStackTrace();
                    }
                }
                else {
                    locationManager.removeUpdates(locationListener);
                }
                updateDataAndDisplay();
            }
        }
        catch(NullPointerException e) {
            Log.d(tag, "onResume: e-mail=null. Setting loggedIn=false, skipping initializations and calling getStarted().");
            Data.loggedIn = false;
            getStarted();
            e.printStackTrace();
        }

        Data.justLoggedOut = false;
        Log.d(tag, "Exiting onResume");
    }

    private void getStarted() {
        dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(false);
        // Set GUI of login screen
        dialog.setContentView(R.layout.dialog_getstarted);
        dialog.setTitle("Getting Started");

        Button createAccountDialogButton = (Button) dialog.findViewById(R.id.createAccountDialogButton);
        final EditText emailDialogET = (EditText)dialog.findViewById(R.id.emailDialogEditText);
        final EditText passwordDialogET = (EditText)dialog.findViewById(R.id.passwordDialogEditText);
        final EditText retypeDialogET = (EditText)dialog.findViewById(R.id.retypePasswordDialogEditText);
        TextView loginTV = (TextView)dialog.findViewById(R.id.loginTextView);

        passwordDialogET.setTypeface(Typeface.DEFAULT);
        passwordDialogET.setTransformationMethod(new PasswordTransformationMethod());
        retypeDialogET.setTypeface(Typeface.DEFAULT);
        retypeDialogET.setTransformationMethod(new PasswordTransformationMethod());
        // Attached listener for login GUI button
        createAccountDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(retypeDialogET.getWindowToken(), 0);
                new DBQueryTask(emailDialogET.getText().toString(), passwordDialogET.getText().toString(), Query.CREATE_ACCOUNT).execute();
            }
        });
        loginTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(retypeDialogET.getWindowToken(), 0);
                dialog.dismiss();
                login("");
            }
        });
        dialog.show();
        Log.d(tag, "Exiting getStarted. GetStartedDialog should have just started.");
    }
    private void login(String accountCreatedMessage) {
        Log.d(tag, "entering login().");
        if(Data.justLoggedOut) {
            Toast.makeText(getApplicationContext(), "Logged out successfully.", Toast.LENGTH_LONG).show();
        }
        dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(false);
        // Set GUI of login screen
        dialog.setContentView(R.layout.dialog_login);
        dialog.setTitle("Login to your account");
        Button loginDialogButton = (Button) dialog.findViewById(R.id.loginDialogButton);
        TextView createAccountTV = (TextView) dialog.findViewById(R.id.createAccountTextView);
        TextView forgotPasswordTV = (TextView) dialog.findViewById(R.id.forgotPasswordTextView);
        final EditText emailDialogET = (EditText)dialog.findViewById(R.id.emailDialogEditText);
        final EditText passwordDialogET = (EditText)dialog.findViewById(R.id.passwordDialogEditText);
        passwordDialogET.setTypeface(Typeface.DEFAULT);
        passwordDialogET.setTransformationMethod(new PasswordTransformationMethod());
        // Attached listener for login GUI button
        loginDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(passwordDialogET.getWindowToken(), 0);
                new DBQueryTask(emailDialogET.getText().toString(), passwordDialogET.getText().toString(), Query.LOGIN).execute();
            }
        });
        createAccountTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(passwordDialogET.getWindowToken(), 0);
                dialog.dismiss();
                getStarted();
            }
        });
        forgotPasswordTV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(passwordDialogET.getWindowToken(), 0);
                forgotPassword();
            }
        });
        dialog.show();
        if(!accountCreatedMessage.equals("")) {
            Log.d(tag, "Creating new Dialog with accountCreatedMessage="+accountCreatedMessage);
            dialog2 = new Dialog(this);
            dialog2.setCanceledOnTouchOutside(false);
            // Set GUI of login screen
            dialog2.setContentView(R.layout.dialog_accountcreated);
            dialog2.setTitle("Activate Your Account");

            Button okDialogButton = (Button) dialog2.findViewById(R.id.okDialogButton);
            TextView accountCreatedTV = (TextView) dialog2.findViewById(R.id.accountCreatedTextView);
            accountCreatedTV.setText(accountCreatedMessage);
            okDialogButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    dialog2.dismiss();
                }
            });
            dialog2.show();
        }
        Log.d(tag, "Exiting login. LoginDialog should have just started.");
    }

    void forgotPassword() {
        dialog2 = new Dialog(this);
        dialog2.setCanceledOnTouchOutside(false);
        // Set GUI of login screen
        dialog2.setContentView(R.layout.dialog_forgotpassword);
        dialog2.setTitle("Forgot Password");

        Button sendEmailDialogButton = (Button) dialog2.findViewById(R.id.sendEmailDialogButton);
        Button cancelDialogButton = (Button) dialog2.findViewById(R.id.cancelDialogButton);
        final EditText emailDialogET = (EditText)dialog2.findViewById(R.id.emailDialogEditText);

        // Attached listener for login GUI button
        sendEmailDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(emailDialogET.getWindowToken(), 0);
                new DBQueryTask(emailDialogET.getText().toString(), Query.SEND_PASSWORD_EMAIL).execute();
            }
        });
        cancelDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(emailDialogET.getWindowToken(), 0);
                dialog2.dismiss();
            }
        });
        dialog2.show();
        Log.d(tag, "Exiting forgotPassword. ForgotPasswordDialog should have just started.");
    }

    void initializeData() {
        Data.inUSA = new boolean[12][31];
        Data.numDaysInUSA = 0;
        Data.monthOfLastUpdate = 0x0; // Should always be between 0-11 inclusive
        Data.dayOfLastUpdate = 0x0; // Should always be between 0-30 inclusive
        Data.today = Calendar.getInstance(); // Initialize to current date
        Data.usingMobileData = true;
        Data.usingLocation = true;

        inputBytes = new byte[Data.NUM_BYTES_FOR_STORING_DAYS];
        outputBytes = new byte[Data.NUM_BYTES_FOR_STORING_DAYS];
        twoOutputBytes = new byte[2];
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
                    updateDataAndDisplay();
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
                    updateDataAndDisplay();
                }
			}
			
		});
	}

    /**
     * Prepares the LocationListener to receive updates from the LocationManager.
     * NOTE: onLocationChanged will only be called if the LocationManager is
     * sending updates to the LocationListener.
     */
    private void setLocationListener() {
        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                // Ensure that a Geocoder services is available
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD && Geocoder.isPresent()) {
                    Log.d(tag, "onLocationChanged: Geocoder present, starting new GetAddressTask in background thread");
                    (new GetAddressTask(MainActivity.this)).execute(location);
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

    Location createLocation(String provider, double lat, double lng, float accuracy) {
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
	
	private void readLocalData() {
        Log.d(tag, "Entering readLocalData, calling read methods");
		readDaysFromFile();
        readCountryFromFile();
        readTimestampFromFile();
        readSettingsFromFile();
        Log.d(tag, "Exiting readLocalData. numDaysInUSA=" + Data.numDaysInUSA);
	}

    /**
     * Updates the day count display, then calls writeDataToFiles().
     * If able to, based on the settings, then pushes user data to the cloud.
     */
    private void updateDayCount() {
        Log.d(tag, "Entering updateDayCount");
        Data.today = Calendar.getInstance();
		if(Data.currentCountry == Country.USA) { // Set today to true
			Data.inUSA[Data.today.get(Calendar.MONTH)][Data.today.get(Calendar.DAY_OF_MONTH) - 1] = true;
            Log.d(tag, "updateDayCount: currentCountry = USA. Setting entry of inUSA to true: ["
                    + Data.today.get(Calendar.MONTH) + "][" + (Data.today.get(Calendar.DAY_OF_MONTH) - 1) + "]");
		}

        writeDataToFiles();
        mWifi = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if(mWifi.isConnected() || (Data.usingMobileData && isNetworkAvailable())) {
            new DBQueryTask(Data.email, Query.ADD_DATA).execute();
        }

        Log.d(tag, "Exiting updateDayCount: numDaysInUSA=" + Data.numDaysInUSA);
	}

    private void updateNumDaysInUSA() {
        Data.numDaysInUSA = 0;
        for(int i=0; i<12; i++) {
            for(int j=0; j<31; j++) {
                if(Data.inUSA[i][j]) {
                    Data.numDaysInUSA++;
                }
            }
        }
        numDaysTV.setText(Integer.toString(Data.numDaysInUSA));
    }

    private void writeDataToFiles() {
        Log.d(tag, "writeDataToFiles: calling writeDaysToFile, numDaysInUSA=" + Data.numDaysInUSA);
        writeDaysToFile();
        Log.d(tag, "writeDataToFiles: calling writeCountryToFile, currentCountry=" + Data.currentCountry);
        writeCountryToFile();
        Log.d(tag, "writeDataToFiles: calling writeTimestampToFile, month=" + Data.monthOfLastUpdate + ", day=" + Data.dayOfLastUpdate);
        writeTimestampToFile();
    }

    private void updateUserDisplay() {
        try {
            loggedInAsTV.setText(Data.email);
            headerTV.setPadding(0, (int) ((16 * getResources().getDisplayMetrics().density + 0.5f)), 0, 0);
            loggedInAsTV.setVisibility(View.VISIBLE);
        }
        catch (NullPointerException e) {
            getStarted();
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

    private void updateDataAndDisplay() {
        Data.today = Calendar.getInstance();
        updateDaysSinceTimestamp();
        updateDayCount();
        updateNumDaysInUSA();
        updateLocationDisplay();
        updateUserDisplay();
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

    private void readSettingsFromFile() {
        Log.d(tag, "Entering readSettingsFromFile");
        int numBytesRead = 0;
        try {
            fis = openFileInput(Data.FILENAME_SETTINGS);
        } catch (FileNotFoundException e) {
            Data.usingMobileData = true;
            Data.usingLocation = true;
            e.printStackTrace();
            Log.d(tag, "readSettingsFromFile: FileNotFoundException, setting usingMobileData and usingLocation to TRUE and returning...");
            return;
        }

        Log.d(tag, "readSettingsFromFile: File exists! Reading inputBytes...");
        try {
            numBytesRead = fis.read(inputBytes, 0, 2);
            fis.close();
        } catch (IOException e) {
            Data.usingMobileData = true;
            Data.usingLocation = true;
            Log.e(tag, "readSettingsFromFile: IOException, setting usingMobileData and usingLocation to TRUE.");
            e.printStackTrace();
        }
        if(numBytesRead > 1) {
            Data.usingMobileData = (inputBytes[0] > 0);
            Data.usingLocation = (inputBytes[1] > 0);
            Log.d(tag, "readSettingsFromFile: usingMobileData="+Data.usingMobileData+", usingLocation="+Data.usingLocation);
        }
        else {
            Log.d(tag, "readSettingsFromFile: numBytesRead < 2, returning...");
        }
        Log.d(tag, "Exiting readSettingsFromFile");
    }

    private void readDataFromCloud() {
        Log.d(tag, "Reading data from cloud for email="+Data.email);
        new DBQueryTask(Data.email, Query.READ_DATA).execute();
    }

    private void writeDaysToFile() {
        int i, j;
        int index = 0;
        for(i=0; i<12; i++) {
            for(j=0; j<31; j++) {
                outputBytes[index++] = (byte) (Data.inUSA[i][j] ? 1 : 0);
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
        Data.monthOfLastUpdate = (byte) (Data.today.get(Calendar.MONTH));
        Data.dayOfLastUpdate = (byte) (Data.today.get(Calendar.DAY_OF_MONTH ) - 1);
        twoOutputBytes[0] = Data.monthOfLastUpdate;
        twoOutputBytes[1] = Data.dayOfLastUpdate;
        Log.d(tag, "writeTimestampToFile: month="+Data.monthOfLastUpdate+", day="+ Data.dayOfLastUpdate);
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
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        return false;
    }

    private boolean isNetworkAvailable() {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
                        updateDataAndDisplay();
                    }
                });
                return addressText;
            } else {
                return "No address found";
            }
        }
    }

    private class DBQueryTask extends AsyncTask<String, Void, String> {
        private final static String tag = "DBQueryTask";
        private String enteredEmail, enteredPassword;
        private Query query;

        public DBQueryTask(String u, Query q) {
            Log.d(tag, "Entering constructor, u="+u+", q="+q);
            enteredEmail = u;
            enteredPassword = null;
            query = q;
        }
        public DBQueryTask(String u, String p, Query q) {
            Log.d(tag, "Entering constructor, u="+u+", p="+p+", q="+q);
            enteredEmail = u;
            enteredPassword = p;
            query = q;
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(tag, "Entering doInBackground");
            String resp, url = null, line;
            HttpClient client;
            HttpResponse response;
            HttpEntity entity;
            InputStream isr;
            BufferedReader reader;
            try {
                switch(query) {
                    case CREATE_ACCOUNT:
                        url = "http://johnstonclan.ca/createUserWithoutData.php?email=" +
                                enteredEmail + "&password=" + enteredPassword;
                        Log.d(tag, "Attempting to create account with email="
                                + enteredEmail + ", password=" + enteredPassword);
                        break;
                    case LOGIN:
                        url = "http://johnstonclan.ca/login.php?email=" +
                                enteredEmail + "&password=" + enteredPassword;
                        Log.d(tag, "Attempting to login with email="
                                +enteredEmail+", password="+enteredPassword);
                        break;
                    case ADD_DATA:
                        // Extract necessary elements to build URL
                        String inUSA = "";
                        for(int i=0; i<12; i++) { // For each month
                            for(int j=0; j<31; j++) { // For each day of the month
                                if(Data.inUSA[i][j]) inUSA += "1";
                                else inUSA += "0";
                            }
                            if(i==0) Log.d(tag, "ADD_DATA: inUSA for January: "+inUSA);
                        }
                        String currentlyInUS = (Data.currentCountry == Country.USA ? "1" : "0");

                        url = "http://johnstonclan.ca/addDataToUser.php?email="+enteredEmail+
                                "&inUSA="+inUSA+"&currentlyInUSA="+currentlyInUS;
                        Log.d(tag, "ADD_DATA: Adding data to user="+enteredEmail+", currentlyInUS="+currentlyInUS);
                        break;
                    case READ_DATA:
                        url = "http://johnstonclan.ca/readUserData.php?email="+enteredEmail;
                        Log.d(tag, "Reading user data: ="+enteredEmail);
                        break;
                    case SEND_ACTIVATION_EMAIL:
                        url = "http://johnstonclan.ca/sendActivationEmail.php?email="+enteredEmail;
                        Log.d(tag, "Sending activation e-mail to "+enteredEmail);
                        break;
                    case CHECK_IF_ACTIVATED:
                        url = "http://johnstonclan.ca/checkIfActivated.php?email="+enteredEmail;
                        Log.d(tag, "Checking if account activated: "+enteredEmail);
                        break;
                    case SEND_PASSWORD_EMAIL:
                        url = "http://johnstonclan.ca/sendForgotPasswordEmail.php?email="+enteredEmail;
                        Log.d(tag, "Attempting to send forgot password email: "+enteredEmail);
                        break;
                }
                // Send query to DB
                client = new DefaultHttpClient();
                response = client.execute(new HttpGet(url));
                entity = response.getEntity();
                isr = entity.getContent();
                // Convert response to string
                reader = new BufferedReader(new InputStreamReader(isr,"iso-8859-1"),8);
                line = reader.readLine();
                Log.d(tag, "Reading line: "+line);
                isr.close();

                switch(query) {
                    case CREATE_ACCOUNT:
                        switch(line) {
                            case "success":
                                resp = "Account created successfully.\n" +
                                        "You will receive an activation\n" +
                                        "e-mail within a few minutes at\n"+enteredEmail+
                                        ".\nFollow the e-mail link to\n" +
                                        "activate your account.\n" +
                                        "Note that you cannot log in until\n" +
                                        "completing this activation.";
                                break;
                            default:
                                if(line.contains("Duplicate")) {
                                    resp = "Unable to create account - e-mail already taken.";
                                }
                                else {
                                    resp = "Unable to create account. "+line;
                                }
                        }
                        final String finalResp1 = resp;
                        if(line.equals("success")) {
                            handler.post(new Runnable() {
                                public void run() {
                                    dialog.dismiss();
                                    login(finalResp1);
                                }
                            });
                        }
                        else {
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(MainActivity.this, finalResp1, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        break;
                    case LOGIN:
                        switch(line) {
                            case "success":
                                resp = "Login successful.";
                                Data.email = enteredEmail;
                                Data.loggedIn = true;
                                break;
                            case "inactive":
                                resp = "You have not activated your account yet.\n" +
                                        "Follow the link in the e-mail to activate.";
                                break;
                            case "userdne":
                                resp = "No account exists with this e-mail address.\n" +
                                        "Try again or create an account.";
                                break;
                            case "wrongpass":
                                resp = "Incorrect password, try again.";
                                break;
                            default:
                                resp = "Login failed. "+line; break;
                        }

                        final String finalResp2 = resp;
                        if(line.equals("success")) {
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(MainActivity.this, finalResp2, Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                    writeEmailToFile();
                                    readDataFromCloud();
                                }
                            });
                        }
                        else {
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(MainActivity.this, finalResp2, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        break;
                    case ADD_DATA:
                        switch(line) {
                            case "success":
                                Log.d(tag, "ADD_DATA: Data added successfully.");
                                break;
                            default:
                                Log.d(tag, "ADD_DATA: Error="+line);
                                break;
                        }
                        break;
                    case READ_DATA:
                        // Parse json data
                        String currentlyInUSA=null, monthOfLastUpdate=null, dayOfLastUpdate=null;
                        String months[] = new String[12];
                        JSONArray jArray = new JSONArray(line);

                        for(int i=0; i<jArray.length();i++){
                            JSONObject json = jArray.getJSONObject(i);
                            months[0] = json.getString("jan");  months[1] = json.getString("feb");  months[2] = json.getString("mar");
                            months[3] = json.getString("apr");  months[4] = json.getString("may");  months[5] = json.getString("jun");
                            months[6] = json.getString("jul");  months[7] = json.getString("aug");  months[8] = json.getString("sep");
                            months[9] = json.getString("oct");  months[10] = json.getString("nov");  months[11] = json.getString("dec");
                            currentlyInUSA = json.getString("currentlyInUSA"); monthOfLastUpdate = json.getString("monthOfLastUpdate");
                            dayOfLastUpdate = json.getString("dayOfLastUpdate");
                        }

                        for(int i=0; i<12; i++) { // For each month
                            for(int j=0; j<31; j++) { // For each day of the month
                                switch(months[i].charAt(j)) { // Extract single character from corresponding String
                                    case '0':
                                        Data.inUSA[i][j] = false;
                                        break;
                                    default:
                                        Data.inUSA[i][j] = true;
                                        break;
                                }
                            }
                        }
                        try {
                            Data.currentCountry = (currentlyInUSA.equals("0") ? Country.CANADA : Country.USA);
                        } catch(NullPointerException e) {
                            Data.currentCountry = Country.CANADA;
                            e.printStackTrace();
                        }
                        Data.monthOfLastUpdate = (byte) (Integer.parseInt(monthOfLastUpdate)-1);
                        Data.dayOfLastUpdate = (byte) (Integer.parseInt(dayOfLastUpdate)-1);

                        Log.d(tag, "READ_DATA: Updated currentCountry="+Data.currentCountry+", " +
                                "monthOfLastUpdate="+Data.monthOfLastUpdate+", dayOfLastUpdate="+Data.dayOfLastUpdate);
                        handler.post(new Runnable() {
                                         @Override
                                         public void run() {
                                             updateDataAndDisplay();
                                         }
                                     });
                        break;
                    case SEND_ACTIVATION_EMAIL:
                        switch(line) {
                            case "1":
                                Log.d(tag, "SEND_ACTIVATION_EMAIL: E-mail accepted to be sent.");
                                resp = "You should receive an e-mail shortly to activate your account.";
                                break;
                            default:
                                Log.d(tag, "SEND_ACTIVATION_EMAIL: Error="+line);
                                resp = line;
                                break;
                        }
                        final String finalResp4 = resp;
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, finalResp4, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case CHECK_IF_ACTIVATED:
                        switch(line) {
                            case "true":
                                Log.d(tag, "CHECK_IF_ACTIVATED: Activated.");
                                resp = "Account has been activated.";
                                break;
                            case "false":
                                Log.d(tag, "CHECK_IF_ACTIVATED: Not activated.");
                                resp = "Account has not been activated.";
                                break;
                            default:
                                Log.d(tag, "CHECK_IF_ACTIVATED: Error="+line);
                                resp = line;
                                break;
                        }
                        final String finalResp5 = resp;
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(MainActivity.this, finalResp5, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case SEND_PASSWORD_EMAIL:
                        switch(line) {
                            case "1":
                                Log.d(tag, "SEND_PASSWORD_EMAIL: E-mail accepted to be sent.");
                                resp = "You should receive an e-mail shortly with your account details.";
                                break;
                            case "userdne":
                                resp = "No account exists with this e-mail address.\n" +
                                        "Try again or create an account.";

                                break;
                            default:
                                Log.d(tag, "SEND_PASSWORD_EMAIL: Error="+line);
                                resp = line;
                                break;
                        }
                        final String finalResp6 = resp;
                        if(line.equals("1")) {
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(MainActivity.this, finalResp6, Toast.LENGTH_LONG).show();
                                    dialog2.dismiss();
                                }
                            });
                        }
                        else {
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(MainActivity.this, finalResp6, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        break;
                }
            }
            catch(Exception e) {
                Log.d(tag, "Exception!");
                e.printStackTrace();
            }
            return "";
        }
    }

}
