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

    boolean daysOfSelectedMonthInUSA[];
    int selectedMonth;
    ImageButton prevMonthButton, nextMonthButton;
    Spinner selectMonthSpinner;
    String daysToDisplay;
    TextView daysInUSATV;

    @Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_calendar_view);

        Log.d("CalendarView", "Entering onCreate");

        prevMonthButton = (ImageButton) findViewById(R.id.prevMonthButton);
        nextMonthButton = (ImageButton) findViewById(R.id.nextMonthButton);
        setButtonOnClickListeners();
		
		selectMonthSpinner = (Spinner) findViewById(R.id.selectMonthSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.months, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectMonthSpinner.setAdapter(adapter);
        addItemSelectedListenerToSpinner();
        selectMonthSpinner.setSelection(Data.today.get(Calendar.MONTH));

        daysInUSATV = (TextView) findViewById(R.id.daysInUSATextView);
		daysOfSelectedMonthInUSA = new boolean[31];

		updateDaysInUSAList();

        Log.d("CalendarView", "Exiting onCreate");
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

    public void onItemSelected(AdapterView<?> parent, View view,
                               int position, long id) {
        Log.d("CalendarView", "Entering onItemSelected");
        selectMonthSpinner.setSelection(position);
        Log.d("CalendarView", "onItemSelected: position of selected month="+position);
        selectedMonth = position;
        updateDaysInUSAList();
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
	
	private void addItemSelectedListenerToSpinner() {
        Log.d("CalendarView", "Entering addItemSelectedListenerToSpinner");
		selectMonthSpinner.setOnItemSelectedListener(this);
        Log.d("CalendarView", "Exiting addItemSelectedListenerToSpinner");
	}

    private void updateDaysInUSAList() {
        Log.d("CalendarView", "Entering updateDaysInUSAList, selectedMonth="+selectedMonth);
		boolean firstDay = true;
		daysToDisplay = "";
		for(int i=0; i<31; i++) {
			if(Data.inUSA[selectedMonth][i]) {
				if(firstDay) {
                    Log.d("CalendarView", "updateDaysInUSAList: Adding first day to list:" + (i+1));
					daysToDisplay+= Integer.toString(i+1) + getEnding(i+1);
					firstDay = false;
				}
				else {
                    Log.d("CalendarView", "updateDaysInUSAList: Adding day to list:" + (i+1));
                    daysToDisplay += ", " + Integer.toString(i + 1) + getEnding(i+1);
                }
			}
		}
		if(firstDay) { // No days in USA
			daysToDisplay = "None";
		}
        Log.d("CalendarView", "updateDaysInUSAList: daysToDisplay=" + daysToDisplay);
		daysInUSATV.setText(daysToDisplay);
        Log.d("CalendarView", "Exiting updateDaysInUSAList");
	}

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
