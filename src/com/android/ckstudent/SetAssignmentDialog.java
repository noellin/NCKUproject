package com.android.ckstudent;

import java.util.Calendar;
import java.util.List;
import java.util.Locale;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.DatePicker.OnDateChangedListener;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.RadioGroup.OnCheckedChangeListener;

public class SetAssignmentDialog extends DialogFragment {
	View view;
	DatePicker datePicker;
	String choise, date, selectedCourse;
	Calendar calendar;
	Spinner spinner1;
	RadioGroup radioGroup;
	RadioButton radioButton1, radioButton2;
	ArrayAdapter<String> courseList;
	static List<String> courseNo, courseName;
	static SetAssignmentDialog newInstance(List<String> no, List<String> name) {
		SetAssignmentDialog fragment = new SetAssignmentDialog();
		courseNo = no;
		courseName = name;
		return fragment;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		view = getActivity().getLayoutInflater().inflate(R.layout.assigndialog_layout, null);
		spinner1 = (Spinner)view.findViewById(R.id.spinner1);
		radioGroup = (RadioGroup)view.findViewById(R.id.radioGroup1);
		radioButton1 = (RadioButton)view.findViewById(R.id.radioButton1);
		radioButton2 = (RadioButton)view.findViewById(R.id.radioButton2);
		datePicker = (DatePicker)view.findViewById(R.id.datePicker1);
		radioGroup.setOnCheckedChangeListener(listener);
		spinner1.setOnItemSelectedListener(selectCourse);
		//Get current date.
		calendar = Calendar.getInstance(Locale.getDefault());
		//Set datepicker.
		datePicker.init(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DATE), dateChange);
		date = String.valueOf(calendar.get(Calendar.YEAR)*10000 + (calendar.get(Calendar.MONTH) + 1)*100 + calendar.get(Calendar.DATE));
		//Get values from argument.
		choise = "homework";
		courseList = new ArrayAdapter<String>(getActivity(), R.layout.spinner_course, courseName);
		spinner1.setAdapter(courseList);
		return new AlertDialog.Builder(getActivity()).setIcon(R.drawable.ic_launcher)
	   			.setView(view)
	   			.setPositiveButton("新增", new DialogInterface.OnClickListener() {
	   				public void onClick(DialogInterface dialog, int whichButton) {
	   					((AssignmentActivity)getActivity()).addAssignment(selectedCourse, choise, date);
	   				}
	   			}).setNegativeButton("取消", new DialogInterface.OnClickListener() {
	   				public void onClick(DialogInterface dialog, int whichButton) {
	   				}
	   			}).create();
	}
	//Set date.
	private OnDateChangedListener dateChange = new OnDateChangedListener() {
		@Override
		public void onDateChanged(DatePicker datePick, int year, int month, int day) {
			date = String.valueOf(year*10000 + (month + 1)*100 + day);
		}
	};
	
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
	//Spinner course.
	private OnItemSelectedListener selectCourse = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
			//Write chosen course.
			selectedCourse = courseNo.get(position);
		}
		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	};
}
