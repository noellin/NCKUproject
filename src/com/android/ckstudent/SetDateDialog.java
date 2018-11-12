package com.android.ckstudent;

import java.util.Calendar;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;

public class SetDateDialog extends DialogFragment {
	View view;
	int currentYear, month, day, selectedMonth, selectedDay;
	Calendar calendar;
	DatePicker datePicker;
	static SetDateDialog newInstance(int currentYear, int month, int day) {
		SetDateDialog fragment = new SetDateDialog();
		//Pass by bundle.
		Bundle args = new Bundle();
		args.putInt("year", currentYear);
		args.putInt("month", month);
		args.putInt("day", day);
		fragment.setArguments(args);
		return fragment;
	}
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		view = getActivity().getLayoutInflater().inflate(R.layout.datepicker, null);
		currentYear = getArguments().getInt("year");
		month = getArguments().getInt("month");
		day = getArguments().getInt("day");
		//Set date picker.
		datePicker = (DatePicker)view.findViewById(R.id.datePicker1);
		calendar = Calendar.getInstance(Locale.getDefault());
		datePicker.init(currentYear, month, day, dateChange);
		selectedMonth = month;
		selectedDay = day;
		return new AlertDialog.Builder(getActivity()).setIcon(R.drawable.ic_launcher)
	   			.setView(view)
	   			.setPositiveButton("新增", new DialogInterface.OnClickListener() {
	   				public void onClick(DialogInterface dialog, int whichButton) {
	   					((AssignmentDetailActivity)getActivity()).changeDate(currentYear, selectedMonth+1, selectedDay);
	   				}
	   			}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
	   				public void onClick(DialogInterface dialog, int whichButton) {
	   				}
	   			}).create();
	}
	
	//Set date change.
	private OnDateChangedListener dateChange = new OnDateChangedListener() {
		@Override
		public void onDateChanged(DatePicker datePick, int year, int month, int day) {
			currentYear = year;
			selectedMonth = month;
			selectedDay = day;
		}
	};
}
