package com.caniborrowyourphone.usa;

import android.app.ActionBar;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;

import java.io.FileOutputStream;

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
    Button logoutButton;
    ImageButton backButton;
    Switch minimizeDataUsageSwitch;

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
        minimizeDataUsageSwitch = (Switch) findViewById(R.id.minimizeDataUsageSwitch);

        backButton.setOnClickListener(this);
        backTV.setOnClickListener(this);
        logoutButton.setOnClickListener(this);
        minimizeDataUsageSwitch.setOnClickListener(this);

        imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);

        Log.d(tag, "Exiting onCreate");
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateUserDisplay();
    }

    @Override
    public void onClick(View v) {
        if(v == logoutButton) {
            Data.justLoggedOut = true;
            Data.loggedIn = false;
            Data.email = "";
            writeEmailToFile();
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(myIntent, 0);
        }
        else if(v == minimizeDataUsageSwitch) {
            Data.mode = (minimizeDataUsageSwitch.isChecked() ? Mode.MINIMIZE_DATA_USAGE : Mode.REGULAR);
            Log.d(tag, "Updated mode=" + Data.mode);
        }
        else if(v == backButton || v == backTV) {
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(myIntent, 0);
        }
    }

    private void updateUserDisplay() {
        try {
            if (Data.email.equals("")) {
                Data.loggedIn = false;
                Data.email = "";
                writeEmailToFile();
                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivityForResult(myIntent, 0);
            } else {
                loggedInAsTV.setText(Data.email);
            }
        }
        catch (NullPointerException e) {
            Data.loggedIn = false;
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(myIntent, 0);
            e.printStackTrace();
        }
    }

    private void writeEmailToFile() {
        try {
            Log.d(tag, "writeEmailToFile: Opening file: " + Data.FILENAME_EMAIL);
            FileOutputStream fos = openFileOutput(Data.FILENAME_EMAIL, Context.MODE_PRIVATE);

            Log.d(tag, "writeEmailToFile: Writing email to file: " + Data.email);
            if (Data.email != null) fos.write(Data.email.getBytes());
            fos.close();
        } catch(Exception e) {
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
    public boolean onPrepareOptionsMenu (Menu menu) { return false; }
}
