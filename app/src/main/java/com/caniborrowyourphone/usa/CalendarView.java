package com.caniborrowyourphone.usa;

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

import java.util.Calendar;

public class CalendarView extends ActionBarActivity implements OnItemSelectedListener {

    private static String tag = "CalendarView";

    int selectedMonth, dayOfWeek, resID;
    ImageButton prevMonthButton, nextMonthButton;
    Spinner selectMonthSpinner;
    Button[][] dayButtons;
    Button thisDayButton;
    String nameOfThisDayButton;
    TextView yearTextView, numDaysThisMonthTV;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view);

        Log.d(tag, "Entering onCreate");

        prevMonthButton = (ImageButton) findViewById(R.id.prevMonthButton);
        nextMonthButton = (ImageButton) findViewById(R.id.nextMonthButton);
        setButtonOnClickListeners();
		
		selectMonthSpinner = (Spinner) findViewById(R.id.selectMonthSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.months, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectMonthSpinner.setAdapter(adapter);
        addItemSelectedListenerToSpinner();
        selectMonthSpinner.setSelection(Data.today.get(Calendar.MONTH));
        yearTextView = (TextView) findViewById(R.id.yearTextView);
        numDaysThisMonthTV = (TextView) findViewById(R.id.numDaysThisMonthTextView);

        updateCalendarDisplay();

        Log.d(tag, "Exiting onCreate");
	}

    private void setButtonOnClickListeners() {
        prevMonthButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if (selectMonthSpinner.getSelectedItemPosition() == 0)
                    selectMonthSpinner.setSelection(11);
                else
                    selectMonthSpinner.setSelection(selectMonthSpinner.getSelectedItemPosition() - 1);
            }

        });

        nextMonthButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(selectMonthSpinner.getSelectedItemPosition() == 11)
                    selectMonthSpinner.setSelection(0);
                else
                    selectMonthSpinner.setSelection(selectMonthSpinner.getSelectedItemPosition() + 1);
            }

        });
    }

    private void updateCalendarDisplay() {
        int dayOfMonth = 0;
        int numDaysThisMonth = 0;
        boolean thisWeekVisible = true;
        dayButtons = new Button[6][7];
        Calendar c = Calendar.getInstance();
        // Set c to the first day of the selected month
        if(Data.today.get(Calendar.MONTH) < selectMonthSpinner.getSelectedItemPosition())
            c.set(Data.today.get(Calendar.YEAR) - 1, selectMonthSpinner.getSelectedItemPosition(), 1);
        else
            c.set(Data.today.get(Calendar.YEAR), selectMonthSpinner.getSelectedItemPosition(), 1);
        Log.d(tag, "updateCalendarDisplay: c="+c.get(Calendar.DAY_OF_WEEK)+" "+c.get(Calendar.DAY_OF_MONTH)+"/"+(c.get(Calendar.MONTH)+1)+"/"+c.get(Calendar.YEAR));

        yearTextView.setText(String.valueOf(c.get(Calendar.YEAR)));
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
                    thisDayButton.setVisibility(View.VISIBLE);
                    thisDayButton.setText(String.valueOf(dayOfMonth));
                    if(Data.inUSA[c.get(Calendar.MONTH)][dayOfMonth - 1]) {
                        Log.d(tag, "updateCalendarDisplay: Setting blue background on "+thisDayButton.getId()+" for day in USA: "+(c.get(Calendar.MONTH)+1)+"/"+dayOfMonth);
                        numDaysThisMonth++;
                        thisDayButton.setBackgroundResource(R.drawable.usaflag64x64);
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
        numDaysThisMonthTV.setText(String.valueOf(numDaysThisMonth) + " days of selected month in the USA");
    }

    public void onItemSelected(AdapterView<?> parent, View view,
                               int position, long id) {
        Log.d(tag, "Entering onItemSelected");
        selectMonthSpinner.setSelection(position);
        Log.d(tag, "onItemSelected: position of selected month="+position);
        selectedMonth = position;
        updateCalendarDisplay();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
	
	private void addItemSelectedListenerToSpinner() {
        Log.d(tag, "Entering addItemSelectedListenerToSpinner");
		selectMonthSpinner.setOnItemSelectedListener(this);
        Log.d(tag, "Exiting addItemSelectedListenerToSpinner");
	}

    /*
    private void updateDaysInUSAList() {
        Log.d(tag, "Entering updateDaysInUSAList, selectedMonth="+selectedMonth);
		boolean firstDay = true;
		daysToDisplay = "";
		for(int i=0; i<31; i++) {
			if(Data.inUSA[selectedMonth][i]) {
				if(firstDay) {
                    Log.d(tag, "updateDaysInUSAList: Adding first day to list:" + (i+1));
					daysToDisplay+= Integer.toString(i+1) + getEnding(i+1);
					firstDay = false;
				}
				else {
                    Log.d(tag, "updateDaysInUSAList: Adding day to list:" + (i+1));
                    daysToDisplay += ", " + Integer.toString(i + 1) + getEnding(i+1);
                }
			}
		}
		if(firstDay) { // No days in USA
			daysToDisplay = "None";
		}
        Log.d(tag, "updateDaysInUSAList: daysToDisplay=" + daysToDisplay);
		daysInUSATV.setText(daysToDisplay);

        Log.d(tag, "Exiting updateDaysInUSAList");
	}
	*/

    private String getEnding(int n) {
        switch(n) {
            case 1: case 21: case 31:
                return "st";
            case 2:case 22:
                return "nd";
            case 3:case 23:
                return "rd";
            default: return "th";
        }
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
		int id = item.getItemId();
		if (id == R.id.action_settings) {
			return true;
		}
		return super.onOptionsItemSelected(item);
	}
}
