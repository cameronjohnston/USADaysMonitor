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
import android.widget.Spinner;
import android.widget.TextView;

public class CalendarView extends ActionBarActivity implements OnItemSelectedListener {

    boolean daysOfSelectedMonthInUSA[];
    int selectedMonth;
    Spinner selectMonthSpinner;
    String daysToDisplay;
    TextView daysInUSATV;

    @Override
    public void onItemSelected(AdapterView<?> parent, View view,
                               int position, long id) {
        String selected_month = (String)selectMonthSpinner.getSelectedItem();
        switch(selected_month) {
            case "January":
                selectedMonth = 0;
            case "February":
                selectedMonth = 1;
            case "March":
                selectedMonth = 2;
            case "April":
                selectedMonth = 3;
            case "May":
                selectedMonth = 4;
            case "June":
                selectedMonth = 5;
            case "July":
                selectedMonth = 6;
            case "August":
                selectedMonth = 7;
            case "September":
                selectedMonth = 8;
            case "October":
                selectedMonth = 9;
            case "November":
                selectedMonth = 10;
            case "December":
                selectedMonth = 11;
            default:
                selectedMonth = 0;
        }
    }

    @Override
    public void onNothingSelected(AdapterView<?> parent) {

    }
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

        Log.d("CalendarView", "Entering onCreate");
		
		selectMonthSpinner = (Spinner) findViewById(R.id.selectMonthSpinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this, R.array.months, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        selectMonthSpinner.setAdapter(adapter);

        daysInUSATV = (TextView) findViewById(R.id.daysInUSATextView);
		daysOfSelectedMonthInUSA = new boolean[31];
		
		setContentView(R.layout.activity_calendar_view);
		
		addItemSelectedListenerToSpinner();
		// updateDaysInUSAList();

        Log.d("CalendarView", "Exiting onCreate");
	}
	
	private void addItemSelectedListenerToSpinner() {
        Log.d("CalendarView", "Entering addItemSelectedListenerToSpinner");
		//selectMonthSpinner.setOnItemSelectedListener(this);
        Log.d("CalendarView", "Exiting addItemSelectedListenerToSpinner");
	}

    /*
	private void updateDaysInUSAList() {
        Log.d("CalendarView", "Entering updateDaysInUSAList");
		boolean firstDay = true;
		daysToDisplay = "";
		for(int i=0; i<31; i++) {
			if(data.inUSA[selectedMonth][i]) {
				if(firstDay) {
                    Log.d("CalendarView", "updateDaysInUSAList: Adding first day to list:" + i+1);
					daysToDisplay+= Integer.toString(i+1);
					firstDay = false;
				}
				else {
                    Log.d("CalendarView", "updateDaysInUSAList: Adding day to list:" + i+1);
                    daysToDisplay += ", " + Integer.toString(i + 1);
                }
			}
		}
		if(firstDay) { // No days in USA
			daysToDisplay = "None";
		}
        Log.d("CalendarView", "updateDaysInUSAList: daysToDisplay=" + daysToDisplay);
		daysInUSATV.setText((CharSequence)daysToDisplay);
        Log.d("CalendarView", "Exiting updateDaysInUSAList");
	}
	*/

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
