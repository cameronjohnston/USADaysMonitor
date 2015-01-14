package com.caniborrowyourphone.usa;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.TypedValue;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class for settings activity of USA Days Monitor app
 * Created by Cameron Johnston on 12/27/2014
 */
public class SettingsActivity extends ActionBarActivity implements View.OnClickListener {
    private static String tag = "SettingsActivity";
    final static Handler handler = new Handler();
    Dialog dialog;
    InputMethodManager imm;
    ActionBar actionBar;

    FileOutputStream fos;
    FileInputStream fis;
    byte[] twoInputBytes = new byte[2];
    byte[] twoOutputBytes = new byte[2];

    TextView loggedInAsTV, titleTextView, backTV, mobileDataInfoTV, locationInfoTV, aboutTV;
    Button logoutButton, changePasswordButton;
    ImageButton backButton;
    Switch usingDataSwitch, usingLocationSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(tag, "Entering onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        actionBar = getActionBar();
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar);

        titleTextView = (TextView) findViewById(R.id.titleTextView);
        titleTextView.setText(R.string.settings);
        backButton = (ImageButton) findViewById(R.id.backButton);
        backTV = (TextView) findViewById(R.id.backTextView);

        loggedInAsTV = (TextView) findViewById(R.id.loggedInAsTextView);
        logoutButton = (Button) findViewById(R.id.logoutButton);
        changePasswordButton = (Button) findViewById(R.id.changePasswordButton);
        usingDataSwitch = (Switch) findViewById(R.id.usingMobileDataSwitch);
        usingLocationSwitch = (Switch) findViewById(R.id.usingLocationSwitch);
        mobileDataInfoTV = (TextView) findViewById(R.id.usingMobileDataInfoTextView);
        locationInfoTV = (TextView) findViewById(R.id.usingLocationInfoTextView);
        aboutTV = (TextView) findViewById(R.id.aboutTextView);

        backButton.setOnClickListener(this);
        backTV.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
        changePasswordButton.setOnClickListener(this);
        usingDataSwitch.setOnClickListener(this);
        usingLocationSwitch.setOnClickListener(this);
        mobileDataInfoTV.setOnClickListener(this);
        locationInfoTV.setOnClickListener(this);
        aboutTV.setOnClickListener(this);

        imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);

        Log.d(tag, "Exiting onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        readSettingsFromFile();
        updateUserDisplay();
        updateSettings();
    }

    @Override
    public void onClick(View v) {
        if (v == logoutButton) {
            Data.justLoggedOut = true;
            Data.loggedIn = false;
            Data.email = "";
            writeEmailToFile();
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(myIntent, 0);
        } else if (v == changePasswordButton) {
            changePassword();
        } else if (v == usingDataSwitch) {
            Data.usingMobileData = usingDataSwitch.isChecked();
            Log.d(tag, "Updated usingMobileData=" + Data.usingMobileData);
            writeSettingsToFile();
        } else if (v == usingLocationSwitch) {
            Data.usingLocation = usingLocationSwitch.isChecked();
            Log.d(tag, "Updated usingLocation" + Data.usingLocation);
            writeSettingsToFile();
        } else if (v == mobileDataInfoTV) {
            showInfo(InfoToShow.MOBILE_DATA);
            Log.d(tag, "Opening info dialog on mobile data...");
        } else if (v == locationInfoTV) {
            showInfo(InfoToShow.LOCATION);
            Log.d(tag, "Opening info dialog on location services...");
        } else if (v == aboutTV) {
            showInfo(InfoToShow.ABOUT);
            Log.d(tag, "Opening info dialog about USA Days Monitor...");
        } else if (v == backButton || v == backTV) {
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(myIntent, 0);
        }
    }

    private void updateUserDisplay() {
        try {
            if (Data.email.equals("")) {
                Data.loggedIn = false;
                writeEmailToFile();
                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivityForResult(myIntent, 0);
            } else {
                loggedInAsTV.setText(Data.email);
            }
        } catch (NullPointerException e) {
            Data.loggedIn = false;
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(myIntent, 0);
            e.printStackTrace();
        }
    }

    void updateSettings() {
        usingDataSwitch.setChecked(Data.usingMobileData);
        usingLocationSwitch.setChecked(Data.usingLocation);
    }

    void changePassword() {
        dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(false);
        // Set GUI of login screen
        dialog.setContentView(R.layout.dialog_changepassword);
        dialog.setTitle("Change Password");

        Button changePasswordDialogButton = (Button) dialog.findViewById(R.id.changePasswordButton);
        Button cancelDialogButton = (Button) dialog.findViewById(R.id.cancelDialogButton);
        final EditText oldPassDialogET = (EditText) dialog.findViewById(R.id.oldPasswordDialogEditText);
        final EditText newPassDialogET = (EditText) dialog.findViewById(R.id.newPasswordDialogEditText);
        final EditText newPassRetypeDialogET = (EditText) dialog.findViewById(R.id.retypeNewPasswordDialogEditText);

        oldPassDialogET.setTypeface(Typeface.DEFAULT);
        oldPassDialogET.setTransformationMethod(new PasswordTransformationMethod());
        newPassDialogET.setTypeface(Typeface.DEFAULT);
        newPassDialogET.setTransformationMethod(new PasswordTransformationMethod());
        newPassRetypeDialogET.setTypeface(Typeface.DEFAULT);
        newPassRetypeDialogET.setTransformationMethod(new PasswordTransformationMethod());
        // Attached listener for login GUI button
        changePasswordDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(oldPassDialogET.getWindowToken(), 0);
                if (newPassDialogET.getText().toString().equals(newPassRetypeDialogET.getText().toString())) {
                    new DBQueryTask(Data.email, oldPassDialogET.getText().toString(),
                            newPassDialogET.getText().toString(), Query.CHANGE_PASSWORD).execute();
                } else {
                    Toast.makeText(SettingsActivity.this, "Unable to change password - New passwords do not match.",
                            Toast.LENGTH_LONG).show();
                }
            }
        });
        cancelDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(oldPassDialogET.getWindowToken(), 0);
                dialog.dismiss();
            }
        });
        dialog.show();
        Log.d(tag, "Exiting getStarted. GetStartedDialog should have just started.");
    }

    void showInfo(InfoToShow i) {
        dialog = new Dialog(this);
        // Set GUI of login screen
        dialog.setContentView(R.layout.dialog_info);

        Button okDialogButton = (Button) dialog.findViewById(R.id.okDialogButton);
        TextView infoDialogTV = (TextView) dialog.findViewById(R.id.infoDialogTextView);

        okDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        switch(i) {
            case MOBILE_DATA:
                dialog.setTitle("Use Mobile Data");
                infoDialogTV.setText(R.string.mobile_data_info_message);
                infoDialogTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                break;
            case LOCATION:
                dialog.setTitle("Use Location Services");
                infoDialogTV.setText(R.string.location_info_message);
                infoDialogTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16);
                break;
            case ABOUT:
                dialog.setTitle("About the App");
                infoDialogTV.setText(R.string.app_info_message);
                infoDialogTV.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
                break;
        }
        dialog.show();
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
            numBytesRead = fis.read(twoInputBytes, 0, 2);
            fis.close();
        } catch (IOException e) {
            Data.usingMobileData = true;
            Data.usingLocation = true;
            Log.e(tag, "readSettingsFromFile: IOException, setting usingMobileData and usingLocation to TRUE.");
            e.printStackTrace();
        }
        if(numBytesRead > 1) {
            Data.usingMobileData = (twoInputBytes[0] > 0);
            Data.usingLocation = (twoInputBytes[1] > 0);
            Log.d(tag, "readSettingsFromFile: usingMobileData="+Data.usingMobileData+", usingLocation="+Data.usingLocation);
        }
        else {
            Log.d(tag, "readSettingsFromFile: numBytesRead < 2, returning...");
        }
        Log.d(tag, "Exiting readSettingsFromFile");
    }

    private void writeSettingsToFile() {
        twoOutputBytes[0] = (Data.usingMobileData ? (byte)1 : (byte)0 );
        twoOutputBytes[1] = (Data.usingLocation ? (byte)1 : (byte)0 );
        Log.d(tag, "writeSettingsToFile: usingMobileData="+Data.usingMobileData+", usingLocation="+ Data.usingLocation);
        try {
            Log.d(tag, "writeSettingsToFile: Opening file: " + Data.FILENAME_SETTINGS);
            fos = openFileOutput(Data.FILENAME_SETTINGS, Context.MODE_PRIVATE);
        } catch (FileNotFoundException e1) {
            Log.e(tag, "writeSettingsToFile: FileNotFoundException");
            e1.printStackTrace();
        }
        try {
            Log.d(tag, "writeSettingsToFile: Writing outputBytes to file: " + Data.FILENAME_SETTINGS);
            fos.write(twoOutputBytes);
            fos.close();
        } catch (IOException e) {
            Log.e(tag, "writeSettingsToFile: IOException when trying to write to FileOutputStream");
            e.printStackTrace();
        }
    }

    private void writeEmailToFile() {
        try {
            Log.d(tag, "writeEmailToFile: Opening file: " + Data.FILENAME_EMAIL);
            fos = openFileOutput(Data.FILENAME_EMAIL, Context.MODE_PRIVATE);

            Log.d(tag, "writeEmailToFile: Writing email to file: " + Data.email);
            if (Data.email != null) fos.write(Data.email.getBytes());
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_settings, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return false;
    }

    private class DBQueryTask extends AsyncTask<String, Void, String> {
        private final static String tag = "DBQueryTask";
        private String email, oldPass, newPass;
        private Query query;

        public DBQueryTask(String e, String o, String n, Query q) {
            email = e;
            oldPass = o;
            newPass = n;
            query = q;
        }

        @Override
        protected String doInBackground(String... params) {
            Log.d(tag, "Entering doInBackground");
            String resp, url, line;
            HttpClient client;
            HttpResponse response;
            HttpEntity entity;
            InputStream isr;
            BufferedReader reader;
            try {
                switch (query) {
                    case CHANGE_PASSWORD:
                        url = "http://johnstonclan.ca/changePassword.php?" +
                                "email=" + email + "&oldpass=" + oldPass + "&newpass=" + newPass;
                        Log.d(tag, "Attempting to change password for " + email + " from " + oldPass + " to " + newPass);

                        // Send query to DB
                        client = new DefaultHttpClient();
                        response = client.execute(new HttpGet(url));
                        entity = response.getEntity();
                        isr = entity.getContent();
                        // Convert response to string
                        reader = new BufferedReader(new InputStreamReader(isr, "iso-8859-1"), 8);
                        line = reader.readLine();
                        Log.d(tag, "Reading line: " + line);
                        isr.close();

                        switch (line) {
                            case "success":
                                resp = "Password change successful.";
                                break;
                            case "wrongpass":
                                resp = "Unable to change password - old password is incorrect.";
                                break;
                            default:
                                resp = "Error: " + line;
                                break;
                        }

                        final String finalResp = resp;
                        if (line.equals("success")) {
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(SettingsActivity.this, finalResp, Toast.LENGTH_LONG).show();
                                    dialog.dismiss();
                                }
                            });
                        } else {
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(SettingsActivity.this, finalResp, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        break;
                    default:
                        Log.d(tag, "Unexpected DBQueryTask for SettingsActivity. Doing nothing.");
                        return "";
                }
            } catch (Exception e) {
                Log.d(tag, "Exception!");
                e.printStackTrace();
            }
            return "";
        }
    }
}

