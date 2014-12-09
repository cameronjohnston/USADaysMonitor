package com.caniborrowyourphone.usa;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.Spinner;
import android.widget.TextView;

public class CalendarView extends ActionBarActivity {

	boolean daysOfSelectedMonthInUSA[];
	int selectedMonth;
	Spinner selectMonthSpinner;
	String daysToDisplay;
	TextView daysInUSATV;
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		selectMonthSpinner = (Spinner) findViewById(R.id.selectMonthSpinner);
		daysInUSATV = (TextView) findViewById(R.id.daysInUSATextView);
		daysOfSelectedMonthInUSA = new boolean[31];
		
		setContentView(R.layout.activity_calendar_view);
		
//		addItemSelectedListenerToSpinner();
		updateDaysInUSAList();
	}
	
	private void addItemSelectedListenerToSpinner() {
		selectMonthSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

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
			
		});
	}
	
	private void updateDaysInUSAList() {
		boolean firstDay = true;
		daysToDisplay = "";
		for(int i=0; i<31; i++) {
			if(MainActivity.inUSA[selectedMonth][i]) {
				if(firstDay) {
					daysToDisplay+= Integer.toString(i+1);
					firstDay = false;
				}
				else {
					daysToDisplay += ", " + Integer.toString(i+1);
				}
			}
		}
		if(firstDay) { // No days in USA
			daysToDisplay = "None";
		}
		daysInUSATV.setText(daysToDisplay);
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
