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
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;

/**
 * Class for calendar view activity of USA Days Monitor app
 * Created by Cameron Johnston on 12/8/2014
 */
public class CalendarView extends ActionBarActivity implements OnItemSelectedListener {

    private static String tag = "CalendarView";

    Dialog dialog;
    final Handler handler = new Handler();

    int selectedMonth, dayOfWeek, resID;
    android.app.ActionBar actionBar;
    ImageButton prevMonthButton, nextMonthButton, backButton;
    Spinner selectMonthSpinner;
    Button[][] dayButtons;
    Button thisDayButton, editDaysButton, saveChangesButton, discardChangesButton;
    String nameOfThisDayButton;
    TextView backTV, loggedInAsTV, titleTextView;
    LinearLayout calendarLinearLayout;
    FileOutputStream fos;
    boolean editing, changesMade, buttonClicked;
    boolean[][] inUSA_old, inUSAThisMonth;
    byte[] outputBytes;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view);

        actionBar = getActionBar();
        actionBar.setDisplayShowHomeEnabled(false);
        actionBar.setDisplayShowTitleEnabled(false);
        actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        actionBar.setCustomView(R.layout.actionbar);

        Log.d(tag, "Entering onCreate");

        prevMonthButton = (ImageButton) findViewById(R.id.prevMonthButton);
        nextMonthButton = (ImageButton) findViewById(R.id.nextMonthButton);
        backButton = (ImageButton) findViewById(R.id.backButton);
		
		selectMonthSpinner = (Spinner) findViewById(R.id.selectMonthSpinner);
        backTV = (TextView) findViewById(R.id.backTextView);
        loggedInAsTV = (TextView) findViewById(R.id.loggedInAsTextView);
        titleTextView = (TextView) findViewById(R.id.titleTextView);
        titleTextView.setText(R.string.calendar);

        calendarLinearLayout = (LinearLayout) findViewById(R.id.calendarLinearLayout);

        editDaysButton = (Button) findViewById(R.id.editDaysButton);
        saveChangesButton = (Button) findViewById(R.id.saveChangesButton);
        discardChangesButton = (Button) findViewById(R.id.discardChangesButton);

        editing = false;
        outputBytes = new byte[Data.NUM_BYTES_FOR_STORING_DAYS];

        initializeSpinner();

        setButtonOnClickListeners();

        addItemSelectedListenerToSpinner();

        updateCalendarDisplay(selectMonthSpinner.getSelectedItemPosition()==0);

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
        editDaysButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                editing = true;
                changesMade = false;
                inUSA_old = new boolean[12][31];
                for(int i=0; i<12; i++)
                    System.arraycopy(Data.inUSA[i], 0, inUSA_old[i], 0, 31);
                editDaysButton.setVisibility(View.INVISIBLE);
                saveChangesButton.setVisibility(View.VISIBLE);
                discardChangesButton.setVisibility(View.VISIBLE);
                Toast.makeText(CalendarView.this, "Click on a day to change whether you were in the USA.", Toast.LENGTH_LONG).show();
                updateCalendarDisplay(selectMonthSpinner.getSelectedItemPosition()==0);
            }

        });
        saveChangesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(changesMade) {
                    saveOrDiscardChanges(true, false);
                }
                else {
                    editing = false;
                    editDaysButton.setVisibility(View.VISIBLE);
                    saveChangesButton.setVisibility(View.INVISIBLE);
                    discardChangesButton.setVisibility(View.INVISIBLE);
                }
            }

        });
        discardChangesButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(changesMade) {
                    saveOrDiscardChanges(false, true);
                }
                else {
                    editing = false;
                    editDaysButton.setVisibility(View.VISIBLE);
                    saveChangesButton.setVisibility(View.INVISIBLE);
                    discardChangesButton.setVisibility(View.INVISIBLE);
                }
            }

        });
        backButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(editing && changesMade) {
                    Log.d(tag, "changesMade. Calling saveOrDiscardChanges");
                    saveOrDiscardChanges(false, false);
                    // After Dialog has completed, it will send a Runnable to handler with the code below.
                    // This will create an Intent and restart MainActivity.
                }
                else {
                    Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivityForResult(myIntent, 0);
                }
            }

        });
        backTV.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(editing && changesMade) {
                    Log.d(tag, "changesMade. Calling saveOrDiscardChanges");
                    saveOrDiscardChanges(false, false);
                    // After Dialog has completed, it will send a Runnable to handler with the code below.
                    // This will create an Intent and restart MainActivity.
                }
                else {
                    Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                    myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    startActivityForResult(myIntent, 0);
                }
            }

        });
    }

    void saveOrDiscardChanges(final boolean saving, final boolean discarding) {
        dialog = new Dialog(this);
        dialog.setCanceledOnTouchOutside(false);
        // Set GUI of login screen
        dialog.setContentView(R.layout.dialog_areyousure);
        if(saving || discarding) dialog.setTitle("Are You Sure?");
        else dialog.setTitle("Save Changes?");

        Button confirmDialogButton = (Button) dialog.findViewById(R.id.confirmDialogButton);
        Button cancelDialogButton = (Button) dialog.findViewById(R.id.cancelDialogButton);
        TextView infoDialogTV = (TextView) dialog.findViewById(R.id.infoDialogTextView);

        if(saving) {
            infoDialogTV.setText(R.string.save_changes_message);
            confirmDialogButton.setText("Save");
            cancelDialogButton.setText("Cancel");
        }
        else if(discarding) {
            infoDialogTV.setText(R.string.discard_changes_message);
            confirmDialogButton.setText("Discard");
            cancelDialogButton.setText("Cancel");
        }
        else {
            infoDialogTV.setText(R.string.discard_or_save_changes_message);
            confirmDialogButton.setText("Save");
            cancelDialogButton.setText("Discard");
        }
        confirmDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                editing = false;
                editDaysButton.setVisibility(View.VISIBLE);
                saveChangesButton.setVisibility(View.INVISIBLE);
                discardChangesButton.setVisibility(View.INVISIBLE);
                if(discarding) {
                    for (int i = 0; i < 12; i++)
                        System.arraycopy(inUSA_old[i], 0, Data.inUSA[i], 0, 31);
                }
                else {
                    writeDaysToFile();
                }
                dialog.dismiss();
                updateCalendarDisplay(selectMonthSpinner.getSelectedItemPosition()==0);
                if(!saving && !discarding) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivityForResult(myIntent, 0);
                        }
                    });
                }
            }
        });
        cancelDialogButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!saving && !discarding) {
                    editing = false;
                    editDaysButton.setVisibility(View.VISIBLE);
                    saveChangesButton.setVisibility(View.INVISIBLE);
                    discardChangesButton.setVisibility(View.INVISIBLE);
                    for (int i = 0; i < 12; i++)
                        System.arraycopy(inUSA_old[i], 0, Data.inUSA[i], 0, 31);
                    updateCalendarDisplay(selectMonthSpinner.getSelectedItemPosition()==0);
                    buttonClicked = true;
                }
                dialog.dismiss();
                if(!saving && !discarding) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
                            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            startActivityForResult(myIntent, 0);
                        }
                    });
                }
            }
        });
        dialog.show();
        Log.d(tag, "Exiting saveOrDiscardChanges. Dialog should be showing.");
    }

    private void updateCalendarDisplay(boolean prevYearFlag) {
        Data.today = Calendar.getInstance();
        int dayOfMonth = 0;
        boolean thisWeekVisible = true;
        boolean sameMonthAsToday, dayAfterToday, doNotDisplay;
        dayButtons = new Button[6][7];
        inUSAThisMonth = new boolean[6][7];
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
                thisDayButton = (Button) findViewById(resID);
                dayButtons[i][j] = thisDayButton;
                if(i==0 && j < (dayOfWeek-1)) { // not yet in month
                    thisDayButton.setBackgroundColor(getResources().getColor(R.color.white));
                    thisDayButton.setText("");
                }
                else if(++dayOfMonth <= getNumDaysInMonth(c.get(Calendar.MONTH))) { // in month
                    dayAfterToday = dayOfMonth > Data.today.get(Calendar.DAY_OF_MONTH);
                    doNotDisplay = (sameMonthAsToday && (prevYearFlag != dayAfterToday));
                    thisDayButton.setVisibility(View.VISIBLE);
                    thisDayButton.setText(String.valueOf(dayOfMonth));
                    if((Data.inUSA[c.get(Calendar.MONTH)][dayOfMonth - 1]) && !doNotDisplay) {
                        Log.d(tag, "Reading Data.inUSA=true for ["+c.get(Calendar.MONTH)+"]["+(dayOfMonth - 1)+"]");
                        inUSAThisMonth[i][j] = true;
                        thisDayButton.setBackgroundResource(R.drawable.usaflag64x64_revisedblue2);
                    }
                    else {
                        thisDayButton.setBackgroundColor(getResources().getColor(R.color.white));
                    }

                    // Set OnClickListener for edit mode:
                    final int iFinal = i, jFinal = j;
                    final Calendar cFinal = c;
                    if(doNotDisplay) {
                        dayButtons[i][j].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if(selectMonthSpinner.getSelectedItemPosition()==0)
                                    Toast.makeText(CalendarView.this, "Invalid change - Day is over 12 months ago, " +
                                            "and is now irrelevant for your tracking purposes.", Toast.LENGTH_LONG).show();
                                else Toast.makeText(CalendarView.this, "Invalid change - Day is in the future. " +
                                        "This is not a time travel app!", Toast.LENGTH_LONG).show();
                            }
                        });
                    }
                    else {
                        dayButtons[i][j].setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                if (editing) {
                                    Log.d(tag, "Click detected at [" + iFinal + "][" + jFinal + "]");
                                    changesMade = true;
                                    if (inUSAThisMonth[iFinal][jFinal]) {
                                        dayButtons[iFinal][jFinal].setBackgroundColor(getResources().getColor(R.color.white));
                                        inUSAThisMonth[iFinal][jFinal] = false;
                                        Log.d(tag, "Setting entry of inUSA to false: [" + cFinal.get(Calendar.MONTH) + "][" + dayButtons[iFinal][jFinal].getText());
                                        Data.inUSA[cFinal.get(Calendar.MONTH)][(Integer.parseInt((String) dayButtons[iFinal][jFinal].getText())) - 1] = false;
                                    } else {
                                        dayButtons[iFinal][jFinal].setBackgroundResource(R.drawable.usaflag64x64_revisedblue2);
                                        inUSAThisMonth[iFinal][jFinal] = true;
                                        Log.d(tag, "Setting entry of inUSA to true: [" + cFinal.get(Calendar.MONTH) + "][" + dayButtons[iFinal][jFinal].getText());
                                        Data.inUSA[cFinal.get(Calendar.MONTH)][(Integer.parseInt((String) dayButtons[iFinal][jFinal].getText())) - 1] = true;
                                    }
                                }
                            }
                        });
                    }
                }
                else { // past month
                    thisDayButton.setText("");
                    if(j==0) {
                        thisWeekVisible = false;
                        thisDayButton.setVisibility(View.INVISIBLE);
                    }
                    if(thisWeekVisible) {
                        thisDayButton.setVisibility(View.VISIBLE);
                        thisDayButton.setBackgroundColor(getResources().getColor(R.color.white));
                    }
                    else {
                        thisDayButton.setVisibility(View.INVISIBLE);
                    }
                }
            }
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
        }
        catch (NullPointerException e) {
            Data.loggedIn = false;
            Intent myIntent = new Intent(getApplicationContext(), MainActivity.class);
            myIntent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
            startActivityForResult(myIntent, 0);
            e.printStackTrace();
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
    @Override
    public boolean onPrepareOptionsMenu (Menu menu) {
        return false;
    }
}
