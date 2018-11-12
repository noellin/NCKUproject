package com.android.ckstudent;

import java.util.Calendar;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

/*
 * This fragment is to create a custom dialog that can add a task.
 * In the end, pass back to TodoActivity.
 */
public class SetTaskDialog extends DialogFragment {
	int currentYear;
	int selectedYear, selectedMonth, selectedDay;
	DatePicker datePicker;
	View view;
	String choise, date;
	Calendar calendar;
	RadioGroup radioGroup;
	RadioButton radioButton1, radioButton2;
	EditText editText;
	static SetTaskDialog newInstance() {
		SetTaskDialog fragment = new SetTaskDialog();
		return fragment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		view = getActivity().getLayoutInflater().inflate(R.layout.taskdialog_layout, null);
		editText = (EditText)view.findViewById(R.id.editText1);
		radioGroup = (RadioGroup)view.findViewById(R.id.radioGroup1);
		radioButton1 = (RadioButton)view.findViewById(R.id.radioButton1);
		radioButton2 = (RadioButton)view.findViewById(R.id.radioButton2);
		radioGroup.setOnCheckedChangeListener(listener);
		//Set datepicker.
		datePicker = (DatePicker)view.findViewById(R.id.datePicker1);
		//Get current date.
		calendar = Calendar.getInstance(Locale.getDefault());
		selectedYear = calendar.get(Calendar.YEAR);
		selectedMonth = calendar.get(Calendar.MONTH) + 1;
		selectedDay = calendar.get(Calendar.DATE);
		datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), dateChange);
		date = String.valueOf(calendar.get(Calendar.YEAR)*10000 + (calendar.get(Calendar.MONTH) + 1)*100 + calendar.get(Calendar.DATE));
		choise = "homework";
	   	return new AlertDialog.Builder(getActivity()).setIcon(R.drawable.ic_launcher)
	   			.setView(view)
	   			.setPositiveButton("新增", new DialogInterface.OnClickListener() {
	   				public void onClick(DialogInterface dialog, int whichButton) {
	   					String taskName = null;
	   					if ("".equals(editText.getText().toString())) {
	   						//Show alert if null.
	   						AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
	   						builder.setTitle("Alert");
	   						builder.setMessage("請勿空白");
	   						builder.setPositiveButton("我知道了", null);
	   						builder.show();
	   					} else {
	   						//Change task name.
	   						taskName = editText.getText().toString();
	   						((TodoActivity) getActivity()).doPositiveClick(selectedYear,
		   							selectedMonth, selectedDay, choise, taskName);
	   					}
	   				}
	   			}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
	   				public void onClick(DialogInterface dialog, int whichButton) {
	   				}
	   			}).create();
	}
	
	//Determine which one is chosen.
	private OnCheckedChangeListener listener = new OnCheckedChangeListener() {
	    public void onCheckedChanged(RadioGroup group, final int checkedId) {
	        switch (checkedId) {
	            case R.id.radioButton1:
	                choise = "homework";
	                break;
	            case R.id.radioButton2:
	            	choise = "contest";
	                break;
	            }
	    }
	};
	
	//Set date change.
	private OnDateChangedListener dateChange = new OnDateChangedListener() {
		@Override
		public void onDateChanged(DatePicker datePick, int year, int month, int day) {
			date = String.valueOf(year*10000 + (month + 1)*100 + day);
			selectedYear = year;
			selectedMonth = month + 1;
			selectedDay = day;
		}
	};
}