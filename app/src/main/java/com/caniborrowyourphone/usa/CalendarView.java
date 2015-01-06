package com.caniborrowyourphone.usa;

import android.app.ActionBar;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;

import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Class for calendar view activity of USA Days Monitor app
 * Created by Cameron Johnston on 12/8/2014
 */
public class CalendarView extends ActionBarActivity implements OnItemSelectedListener {

    private static String tag = "CalendarView";

    int selectedMonth, dayOfWeek, resID;
    android.app.ActionBar actionBar;
    ImageButton prevMonthButton, nextMonthButton, calendarBackButton;
    Spinner selectMonthSpinner;
    Button[][] dayButtons;
    Button thisDayButton;
    String nameOfThisDayButton;
    TextView numDaysThisMonthTV, calendarBackTV, loggedInAsTV;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view);

        Log.d(tag, "Entering onCreate");

        actionBar = getActionBar();

        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar);

        prevMonthButton = (ImageButton) findViewById(R.id.prevMonthButton);
        nextMonthButton = (ImageButton) findViewById(R.id.nextMonthButton);
        calendarBackButton = (ImageButton) findViewById(R.id.calendarBackButton);
		
		selectMonthSpinner = (Spinner) findViewById(R.id.selectMonthSpinner);
        numDaysThisMonthTV = (TextView) findViewById(R.id.numDaysThisMonthTextView);
        calendarBackTV = (TextView) findViewById(R.id.calendarBackTextView);
        loggedInAsTV = (TextView) findViewById(R.id.loggedInAsTextView);

        initializeSpinner();

        setButtonOnClickListeners();

        addItemSelectedListenerToSpinner();

        updateCalendarDisplay(false);

        Log.d(tag, "Exiting onCreate");
	}

    @Override
    protected void onResume() {
        super.onResume();
        prevMonthButton.setVisibility(selectMonthSpinner.getSelectedItemPosition() == 0 ? View.INVISIBLE : View.VISIBLE);
        nextMonthButton.setVisibility(selectMonthSpinner.getSelectedItemPosition() == 12 ? View.INVISIBLE : View.VISIBLE);
        updateUserDisplay();
    }

    private void initializeSpinner() {
        int month, year;
        String entry;
        ArrayAdapter<String> dataAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<String>());
        dataAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectMonthSpinner.setAdapter(dataAdapter);
        year = Data.today.get(Calendar.YEAR) - 1;
        for(month=Data.today.get(Calendar.MONTH); month<12; month++) {
            entry = new DateFormatSymbols().getMonths()[month] + " " + year;
            Log.d(tag, "initializeSpinner: Adding entry "+entry);
            dataAdapter.add(entry);
        }
        year++;
        for(month=0; month<=Data.today.get(Calendar.MONTH); month++) {
            entry = new DateFormatSymbols().getMonths()[month] + " " + year;
            Log.d(tag, "initializeSpinner: Adding entry "+entry);
            dataAdapter.add(entry);
        }
        selectMonthSpinner.setSelection(12);
    }

    private void setButtonOnClickListeners() {
        prevMonthButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (selectMonthSpinner.getSelectedItemPosition() == 0)
                    selectMonthSpinner.setSelection(12);
                else
                    selectMonthSpinner.setSelection(selectMonthSpinner.getSelectedItemPosition() - 1);
            }

        });

        nextMonthButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(selectMonthSpinner.getSelectedItemPosition() == 12)
                    selectMonthSpinner.setSelection(0);
                else
                    selectMonthSpinner.setSelection(selectMonthSpinner.getSelectedItemPosition() + 1);
            }

        });
        calendarBackButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivityForResult(myIntent, 0);
            }

        });
        calendarBackTV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivityForResult(myIntent, 0);
            }

        });

    }

    private void updateCalendarDisplay(boolean prevYearFlag) {
        int dayOfMonth = 0;
        int numDaysThisMonth = 0;
        boolean thisWeekVisible = true;
        boolean sameMonthAsToday, dayAfterToday, doNotDisplay;
        dayButtons = new Button[6][7];
        Calendar c = Calendar.getInstance();
        // Set c to the first day of the selected month
        if((Data.today.get(Calendar.MONTH) < selectedMonth) || prevYearFlag)
            c.set(Data.today.get(Calendar.YEAR) - 1, selectedMonth, 1);
        else
            c.set(Data.today.get(Calendar.YEAR), selectedMonth, 1);
        Log.d(tag, "updateCalendarDisplay: c="+c.get(Calendar.DAY_OF_WEEK)+" "+c.get(Calendar.DAY_OF_MONTH)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR));
        sameMonthAsToday = (c.get(Calendar.MONTH) == Data.today.get(Calendar.MONTH));

        dayOfWeek = c.get(Calendar.DAY_OF_WEEK);
        for(int i=0; i<6; i++) {
            for(int j=0; j<7; j++) {
                Log.d(tag, "updateCalendarDisplay: Start of inner for loop, i="+i+", j="+j);
                switch(j) {
                    case 0:
                        nameOfThisDayButton = "sunButton" + (i+1); break;
                    case 1:
                        nameOfThisDayButton = "monButton" + (i+1); break;
                    case 2:
                        nameOfThisDayButton = "tueButton" + (i+1); break;
                    case 3:
                        nameOfThisDayButton = "wedButton" + (i+1); break;
                    case 4:
                        nameOfThisDayButton = "thuButton" + (i+1); break;
                    case 5:
                        nameOfThisDayButton = "friButton" + (i+1); break;
                    case 6:
                        nameOfThisDayButton = "satButton" + (i+1); break;
                    default:
                        nameOfThisDayButton = "sunButton1"; break;
                }
                resID = getResources().getIdentifier(nameOfThisDayButton, "id", getPackageName());
                Log.d(tag, "updateCalendarDisplay: nameOfThisDayButton="+nameOfThisDayButton+", resID ="+resID);
                thisDayButton = (Button) findViewById(resID);
                if(i==0 && j < (dayOfWeek-1)) { // not yet in month
                    Log.d(tag, "updateCalendarDisplay: not yet in month");
                    thisDayButton.setBackgroundColor(getResources().getColor(R.color.white));
                    thisDayButton.setText("");
                }
                else if(++dayOfMonth <= getNumDaysInMonth(c.get(Calendar.MONTH))) { // in month
                    Log.d(tag, "updateCalendarDisplay: Setting "+thisDayButton.getId()+" text to "+String.valueOf(dayOfMonth));
                    dayAfterToday = dayOfMonth > Data.today.get(Calendar.DAY_OF_MONTH);
                    doNotDisplay = (sameMonthAsToday && (prevYearFlag != dayAfterToday));
                    thisDayButton.setVisibility(View.VISIBLE);
                    thisDayButton.setText(String.valueOf(dayOfMonth));
                    if((Data.inUSA[c.get(Calendar.MONTH)][dayOfMonth - 1]) && !doNotDisplay) {
                        Log.d(tag, "updateCalendarDisplay: Changing background for day in USA: "+(c.get(Calendar.MONTH)+1)+"/"+dayOfMonth);
                        numDaysThisMonth++;
                        thisDayButton.setBackgroundResource(R.drawable.usaflag64x64_revisedblue2);
                    }
                    else {
                        thisDayButton.setBackgroundColor(getResources().getColor(R.color.white));
                    }
                }
                else { // past month
                    Log.d(tag, "updateCalendarDisplay: past month");
                    thisDayButton.setText("");
                    if(j==0) {
                        Log.d(tag, "updateCalendarDisplay: past month, j=0. Setting INVISIBLE");
                        thisWeekVisible = false;
                        thisDayButton.setVisibility(View.INVISIBLE);
                    }
                    if(thisWeekVisible) {
                        Log.d(tag, "updateCalendarDisplay: past month, setting VISIBLE with white background");
                        thisDayButton.setVisibility(View.VISIBLE);
                        thisDayButton.setBackgroundColor(getResources().getColor(R.color.white));
                    }
                    else {
                        Log.d(tag, "updateCalendarDisplay: past month, thisWeekVisible=false. Setting INVISIBLE");
                        thisDayButton.setVisibility(View.INVISIBLE);
                    }
                }
            }
        }
        numDaysThisMonthTV.setText(String.valueOf(numDaysThisMonth));
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

    public void onItemSelected(AdapterView<?> parent, View view,
                               int position, long id) {
        Log.d(tag, "Entering onItemSelected");
        boolean prevYearFlag = (position==0);
        selectMonthSpinner.setSelection(position);
        selectedMonth = (position + Data.today.get(Calendar.MONTH)) % 12;
        Log.d(tag, "onItemSelected: position of selected month="+position+", selectedMonth="+selectedMonth);
        prevMonthButton.setVisibility(selectMonthSpinner.getSelectedItemPosition() == 0 ? View.INVISIBLE : View.VISIBLE);
        nextMonthButton.setVisibility(selectMonthSpinner.getSelectedItemPosition() == 12 ? View.INVISIBLE : View.VISIBLE);
        updateCalendarDisplay(prevYearFlag);
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {}
	
	private void addItemSelectedListenerToSpinner() {
        Log.d(tag, "Entering addItemSelectedListenerToSpinner");
		selectMonthSpinner.setOnItemSelectedListener(this);
        Log.d(tag, "Exiting addItemSelectedListenerToSpinner");
	}

    private int getNumDaysInMonth(int month) {
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

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.calendar_view, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle action bar item clicks here. The action bar will
		// automatically handle clicks on the Home/Up button, so long
		// as you specify a parent activity in AndroidManifest.xml.
        Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
        startActivityForResult(myIntent, 0);
        return true;
	}
}
