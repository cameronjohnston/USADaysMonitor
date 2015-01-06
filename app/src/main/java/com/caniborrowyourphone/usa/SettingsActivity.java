package com.caniborrowyourphone.usa;

import android.app.Dialog;
import android.content.Context;
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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

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

    TextView accountTV, loggedInAsTV;
    Button loginButton, createAccountButton, logoutButton;
    Switch cloudStorageSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(tag, "Entering onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        accountTV = (TextView) findViewById(R.id.accountTextView);
        loggedInAsTV = (TextView) findViewById(R.id.loggedInAsTextView);
        loginButton = (Button) findViewById(R.id.loginButton);
        createAccountButton = (Button) findViewById(R.id.createAccountButton);
        logoutButton = (Button) findViewById(R.id.logoutButton);
        cloudStorageSwitch = (Switch) findViewById(R.id.usingCloudStorageSwitch);

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
            Data.username = "";
            updateSettingsDisplay();
        }
        else if(v == cloudStorageSwitch) {
            Log.d(tag, "toggleCloudStorage: usingCloudStorage="+cloudStorageSwitch.isChecked());
            Data.usingCloudStorage = cloudStorageSwitch.isChecked();
        }
    }

    private void updateSettingsDisplay() {
        accountTV.setText(Data.username.equals("") ? "None" : Data.username);

        loginButton.setVisibility(Data.username.equals("") ? View.VISIBLE : View.INVISIBLE);
        createAccountButton.setVisibility(Data.username.equals("") ? View.VISIBLE : View.INVISIBLE);
        logoutButton.setVisibility(Data.username.equals("") ? View.INVISIBLE : View.VISIBLE);
        cloudStorageSwitch.setVisibility(Data.username.equals("") ? View.INVISIBLE : View.VISIBLE);

        updateUserDisplay();
    }

    private void updateUserDisplay() {
        if(Data.username.equals("")) {
            loggedInAsTV.setVisibility(View.INVISIBLE);
        }
        else {
            loggedInAsTV.setText("Logged in as "+Data.username);
            loggedInAsTV.setVisibility(View.VISIBLE);
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
        final EditText usernameDialogET = (EditText)dialog.findViewById(R.id.usernameDialogEditText);
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
                new DBQueryTask(usernameDialogET.getText().toString(), passwordDialogET.getText().toString(),
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
        final EditText usernameDialogET = (EditText)dialog.findViewById(R.id.usernameDialogEditText);
        final EditText passwordDialogET = (EditText)dialog.findViewById(R.id.passwordDialogEditText);
        passwordDialogET.setTypeface(Typeface.DEFAULT);
        passwordDialogET.setTransformationMethod(new PasswordTransformationMethod());
        // Attached listener for login GUI button
        loginDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                imm.hideSoftInputFromWindow(passwordDialogET.getWindowToken(), 0);
                new DBQueryTask(usernameDialogET.getText().toString(), passwordDialogET.getText().toString(), Query.LOGIN).execute();
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

    private class DBQueryTask extends AsyncTask<String, Void, String> {
        private final static String tag = "LoginTask";
        private String enteredUsername, enteredPassword, enteredRetype;
        private Query query;

        public DBQueryTask(String u, Query q) {
            Log.d(tag, "Entering constructor, u="+u+", q="+q);
            enteredUsername = u;
            enteredPassword = null;
            enteredRetype = null;
            query = q;
        }
        public DBQueryTask(String u, String p, Query q) {
            Log.d(tag, "Entering constructor, u="+u+", p="+p+", q="+q);
            enteredUsername = u;
            enteredPassword = p;
            enteredRetype = null;
            query = q;
        }
        public DBQueryTask(String u, String p, String r, Query q) {
            Log.d(tag, "Entering constructor, u="+u+", p="+p+", r="+r+", q="+q);
            enteredUsername = u;
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
                        url = "http://johnstonclan.ca/createUser.php?username=" +
                                enteredUsername + "&password=" + enteredPassword;
                        Log.d(tag, "Attempting to create account with username="
                                + enteredUsername + ", password=" + enteredPassword);
                    }
                    break;
                case LOGIN:
                    url = "http://johnstonclan.ca/login.php?username=" +
                            enteredUsername + "&password=" + enteredPassword;
                    Log.d(tag, "Attempting to login with username="
                            +enteredUsername+", password="+enteredPassword);
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
                                Data.username = enteredUsername;
                                break;
                            default:
                                if(line.contains("Duplicate")) {
                                    resp = "Unable to create account - username already taken.";
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
                                Data.username = enteredUsername;
                                break;
                            case "userdne":
                                resp = "No such username, try again.";
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
                }
            }
            catch(Exception e) {
                Log.d(tag, "Exception!");
                e.printStackTrace();
            }
            return "";
        }
    }

    /*
    // This class taken from: http://developer.android.com/guide/topics/ui/dialogs.html
    public static class LoginDialogFragment extends DialogFragment {
        Handler dialogHandler = new Handler();
        EditText usernameDialogET, passwordDialogET;
        String usernameEntered, passwordEntered;
        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            // Get the layout inflater
            LayoutInflater inflater = getActivity().getLayoutInflater();

            usernameDialogET = (EditText) findViewById(R.id.usernameDialogEditText);
            passwordDialogET = (EditText) findViewById(R.id.passwordDialogEditText);

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            builder.setView(inflater.inflate(R.layout.dialog_login, null))
                    // Add action buttons
                    .setPositiveButton(R.string.login, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int id) {
                            new LoginTask(usernameEntered, passwordEntered).execute();
                        }
                    })
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            LoginDialogFragment.this.getDialog().cancel();
                        }
                    });
            return builder.create();
        }

        private class LoginTask extends AsyncTask<String, Void, String> {
            private final static String tag = "LoginTask";
            private String enteredUsername, enteredPassword;

            public LoginTask(String u, String p) {
                Log.d(tag, "Entering constructor, u="+u+", p="+p);
                enteredUsername = u;
                enteredPassword = p;
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
                    url = "http://johnstonclan.ca/login.php?username=" +
                            enteredUsername + "&password=" + enteredPassword;
                    client = new DefaultHttpClient();
                    Log.d(tag, "Attempting to login with username="
                            +enteredUsername+", password="+enteredPassword);
                    response = client.execute(new HttpGet(url));
                    entity = response.getEntity();
                    isr = entity.getContent();
                    // Convert response to string
                    reader = new BufferedReader(new InputStreamReader(isr,"iso-8859-1"),8);
                    line = reader.readLine();
                    Log.d(tag, "Reading line: "+line);
                    isr.close();

                    switch(line) {
                        case "success":
                            resp = "Login successful.";
                            Data.username = enteredUsername;
                            break;
                        case "userdne":
                            resp = "Incorrect username, try again.";
                            break;
                        case "wrongpass":
                            resp = "Incorrect password, try again.";
                            break;
                        default:
                            resp = "Login failed. "+line; break;
                    }

                    final String finalResp = resp;
                    if(line.equals("success")) {
                        handler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(appContext, finalResp, Toast.LENGTH_LONG).show();
                                LoginDialogFragment.this.getDialog().cancel();
                            }
                        });
                    }
                    else {
                        dialogHandler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(appContext, finalResp, Toast.LENGTH_LONG).show();
                            }
                        });
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
*/
}
