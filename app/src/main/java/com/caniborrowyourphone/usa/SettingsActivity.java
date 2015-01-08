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
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
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

    TextView loggedInAsTV, titleTextView, backTV;
    Button loginButton, createAccountButton, logoutButton;
    ImageButton backButton;
    Switch cloudStorageSwitch;

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
        loginButton = (Button) findViewById(R.id.loginButton);
        createAccountButton = (Button) findViewById(R.id.createAccountButton);
        logoutButton = (Button) findViewById(R.id.logoutButton);
        cloudStorageSwitch = (Switch) findViewById(R.id.usingCloudStorageSwitch);

        backButton.setOnClickListener(this);
        backTV.setOnClickListener(this);
        createAccountButton.setOnClickListener(this);
        loginButton.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
        cloudStorageSwitch.setOnClickListener(this);

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        Log.d(tag, "Exiting onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateSettingsDisplay();
    }

    @Override
    public void onClick(View v) {
        if(v == createAccountButton) {
            createAccount();
        }
        else if(v == loginButton) {
            login();
        }
        else if(v == logoutButton) {
            Log.d(tag, "Logout button pressed.");
            Toast.makeText(SettingsActivity.this, "Logout successful.", Toast.LENGTH_LONG).show();
            Data.email = "";
            updateSettingsDisplay();
        }
        else if(v == cloudStorageSwitch) {
            Log.d(tag, "toggleCloudStorage: usingCloudStorage="+cloudStorageSwitch.isChecked());
            Data.usingCloudStorage = cloudStorageSwitch.isChecked();
            if(cloudStorageSwitch.isChecked()) {
                try {
                    if(!Data.email.equals(""))
                        new DBQueryTask(Data.email, Query.ADD_DATA).execute();
                }
                catch(NullPointerException e) {
                    e.printStackTrace();
                }
            }
        }
        else if(v == backButton || v == backTV) {
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(myIntent, 0);
        }
    }

    private void updateSettingsDisplay() {
        try {
            loginButton.setVisibility(Data.email.equals("") ? View.VISIBLE : View.INVISIBLE);
            createAccountButton.setVisibility(Data.email.equals("") ? View.VISIBLE : View.INVISIBLE);
            logoutButton.setVisibility(Data.email.equals("") ? View.INVISIBLE : View.VISIBLE);
            cloudStorageSwitch.setVisibility(Data.email.equals("") ? View.INVISIBLE : View.VISIBLE);
        }
        catch (NullPointerException e) {
            e.printStackTrace();
            loginButton.setVisibility(View.VISIBLE);
            createAccountButton.setVisibility(View.VISIBLE);
            logoutButton.setVisibility(View.INVISIBLE);
            cloudStorageSwitch.setVisibility(View.INVISIBLE);
        }

        updateUserDisplay();
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

    private void createAccount() {
        Log.d(tag, "Create Account button pressed.");
        dialog = new Dialog(this);
        // Set GUI of login screen
        dialog.setContentView(R.layout.dialog_createaccount);
        dialog.setTitle("Create Account");

        Button createAccountDialogButton = (Button) dialog.findViewById(R.id.createAccountDialogButton);
        Button cancelDialogButton = (Button) dialog.findViewById(R.id.cancelDialogButton);
        final EditText emailDialogET = (EditText)dialog.findViewById(R.id.emailDialogEditText);
        final EditText passwordDialogET = (EditText)dialog.findViewById(R.id.passwordDialogEditText);
        final EditText retypeDialogET = (EditText)dialog.findViewById(R.id.retypePasswordDialogEditText);
        passwordDialogET.setTypeface(Typeface.DEFAULT);
        passwordDialogET.setTransformationMethod(new PasswordTransformationMethod());
        retypeDialogET.setTypeface(Typeface.DEFAULT);
        retypeDialogET.setTransformationMethod(new PasswordTransformationMethod());
        // Attached listener for login GUI button
        createAccountDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(retypeDialogET.getWindowToken(), 0);
                new DBQueryTask(emailDialogET.getText().toString(), passwordDialogET.getText().toString(),
                        retypeDialogET.getText().toString(), Query.CREATE_ACCOUNT).execute();
            }
        });
        cancelDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();

        Log.d(tag, "Exiting login. LoginDialog should have just started.");
    }
    private void login() {
        Log.d(tag, "Login button pressed.");
        dialog = new Dialog(this);
        // Set GUI of login screen
        dialog.setContentView(R.layout.dialog_login);
        dialog.setTitle("Login to your account");
        Button loginDialogButton = (Button) dialog.findViewById(R.id.loginDialogButton);
        Button cancelDialogButton = (Button) dialog.findViewById(R.id.cancelDialogButton);
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
        cancelDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
        Log.d(tag, "Exiting login. LoginDialog should have just started.");
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
    public boolean onPrepareOptionsMenu (Menu menu) {
        return false;
    }

    private class DBQueryTask extends AsyncTask<String, Void, String> {
        private final static String tag = "LoginTask";
        private String enteredEmail, enteredPassword, enteredRetype;
        private Query query;

        public DBQueryTask(String u, Query q) {
            Log.d(tag, "Entering constructor, u="+u+", q="+q);
            enteredEmail = u;
            enteredPassword = null;
            enteredRetype = null;
            query = q;
        }
        public DBQueryTask(String u, String p, Query q) {
            Log.d(tag, "Entering constructor, u="+u+", p="+p+", q="+q);
            enteredEmail = u;
            enteredPassword = p;
            enteredRetype = null;
            query = q;
        }
        public DBQueryTask(String u, String p, String r, Query q) {
            Log.d(tag, "Entering constructor, u="+u+", p="+p+", r="+r+", q="+q);
            enteredEmail = u;
            enteredPassword = p;
            enteredRetype = r;
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
                    if(enteredPassword.equals(enteredRetype)) {
                        url = "http://johnstonclan.ca/createUserWithoutData.php?email=" +
                                enteredEmail + "&password=" + enteredPassword;
                        Log.d(tag, "Attempting to create account with email="
                                + enteredEmail + ", password=" + enteredPassword);
                    }
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
                case CHECK_IF_USING_CLOUD:
                    url = "http://johnstonclan.ca/checkIfUsingCloudStorage.php?email="+enteredEmail;
                    Log.d(tag, "Checking if using cloud storage: "+enteredEmail);
                    break;
                case DISABLE_CLOUD:
                    url = "http://johnstonclan.ca/setUsingCloudStorage.php?email="+enteredEmail+"&using=0";
                    Log.d(tag, "Disabling cloud storage for "+enteredEmail);
                    break;
            }
                // Send query to DB, unless CREATE_ACCOUNT and password mismatch
                if(query != Query.CREATE_ACCOUNT || enteredPassword.equals(enteredRetype)) {
                    client = new DefaultHttpClient();
                    response = client.execute(new HttpGet(url));
                    entity = response.getEntity();
                    isr = entity.getContent();
                    // Convert response to string
                    reader = new BufferedReader(new InputStreamReader(isr,"iso-8859-1"),8);
                    line = reader.readLine();
                    Log.d(tag, "Reading line: "+line);
                    isr.close();
                }
                else { // CREATE_ACCOUNT password mismatch
                    Log.d(tag, "Unable to create account - password mismatch.");
                    resp = "Unable to create account - passwords do not match.";
                    final String finalResp1 = resp;
                    handler.post(new Runnable() {
                        public void run() {
                            Toast.makeText(SettingsActivity.this, finalResp1, Toast.LENGTH_LONG).show();
                        }
                    });
                    return "";
                }

                switch(query) {
                    case CREATE_ACCOUNT:
                        switch(line) {
                            case "success":
                                resp = "Account created successfully.";
                                Data.email = enteredEmail;
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
                                    Toast.makeText(SettingsActivity.this, finalResp1, Toast.LENGTH_LONG).show();
                                    updateSettingsDisplay();
                                    dialog.dismiss();
                                }
                            });
                        }
                        else {
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(SettingsActivity.this, finalResp1, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        break;
                    case LOGIN:
                        switch(line) {
                            case "success":
                                resp = "Login successful.";
                                Data.email = enteredEmail;
                                break;
                            case "userdne":
                                resp = "No account exists with this e-mail address, try again.";
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
                                    Toast.makeText(SettingsActivity.this, finalResp2, Toast.LENGTH_LONG).show();
                                    updateSettingsDisplay();
                                    dialog.dismiss();
                                }
                            });
                        }
                        else {
                            handler.post(new Runnable() {
                                public void run() {
                                    Toast.makeText(SettingsActivity.this, finalResp2, Toast.LENGTH_LONG).show();
                                }
                            });
                        }
                        break;
                    case ADD_DATA:
                        switch(line) {
                            case "success":
                                Log.d(tag, "ADD_DATA: Data added successfully.");
                                resp = "Data added to cloud storage successfully.";
                                break;
                            default:
                                Log.d(tag, "ADD_DATA: Error="+line);
                                resp = line;
                                break;
                        }
                        final String finalResp3 = resp;
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(SettingsActivity.this, finalResp3, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case READ_DATA:
                        // Parse json data
                        // String s = "";
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
                            /*
                            s = s +
                                    "\njan : "+json.getString("jan")+"\nfeb : "+json.getString("feb")+"\nmar : "+json.getString("mar")+
                                    "\napr : "+json.getString("apr")+"\nmay : "+json.getString("may")+"\njun : "+json.getString("jun")+
                                    "\njul : "+json.getString("jul")+"\naug : "+json.getString("aug")+"\nsep : "+json.getString("sep")+
                                    "\noct : "+json.getString("oct")+"\nnov : "+json.getString("nov")+"\ndec : "+json.getString("dec")+
                                    "\ncurrentlyInUSA : "+json.getString("currentlyInUSA")+"\nmonth : "+json.getString("monthOfLastUpdate")
                                    +"\nday : "+json.getString("dayOfLastUpdate");
                            */
                        }

                        Data.numDaysInUSA = 0;
                        for(int i=0; i<12; i++) { // For each month
                            for(int j=0; j<31; j++) { // For each day of the month
                                switch(months[i].charAt(j)) { // Extract single character from corresponding String
                                    case '0':
                                        Data.inUSA[i][j] = false;
                                        break;
                                    default:
                                        Data.inUSA[i][j] = true;
                                        Data.numDaysInUSA++;
                                        break;
                                }
                            }
                        }
                        Data.currentCountry = (currentlyInUSA.equals("0") ? Country.CANADA : Country.USA);
                        Data.monthOfLastUpdate = (byte) Integer.parseInt(monthOfLastUpdate);
                        Data.dayOfLastUpdate = (byte) Integer.parseInt(dayOfLastUpdate);

                        Log.d(tag, "READ_DATA: Updated currentCountry="+Data.currentCountry+", " +
                                "monthOfLastUpdate="+Data.monthOfLastUpdate+", dayOfLastUpdate="+Data.dayOfLastUpdate);
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
                                Toast.makeText(SettingsActivity.this, finalResp4, Toast.LENGTH_LONG).show();
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
                                Toast.makeText(SettingsActivity.this, finalResp5, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case CHECK_IF_USING_CLOUD:
                        switch(line) {
                            case "true":
                                Log.d(tag, "CHECK_IF_USING_CLOUD: Using cloud storage.");
                                resp = "Using cloud storage.";
                                break;
                            case "false":
                                Log.d(tag, "CHECK_IF_USING_CLOUD: Not using cloud storage.");
                                resp = "Not using cloud storage.";
                                break;
                            default:
                                Log.d(tag, "CHECK_IF_USING_CLOUD: Error="+line);
                                resp = line;
                                break;
                        }
                        final String finalResp7 = resp;
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(SettingsActivity.this, finalResp7, Toast.LENGTH_LONG).show();
                            }
                        });
                        break;
                    case DISABLE_CLOUD:
                        switch(line) {
                            case "success":
                                Log.d(tag, "DISABLE_CLOUD: Success.");
                                resp = "Successfully disabled cloud storage.";
                                break;
                            default:
                                Log.d(tag, "DISABLE_CLOUD: Error="+line);
                                resp = line;
                                break;
                        }
                        final String finalResp8 = resp;
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(SettingsActivity.this, finalResp8, Toast.LENGTH_LONG).show();
                            }
                        });
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
