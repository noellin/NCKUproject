package com.android.ckstudent;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class SetCallModeDialog extends DialogFragment {
	View view;
	int selectedMode = 0;
	int selectedNumber ,position, selectedRate;
	Spinner spinner1, spinner2, spinner3;
	Integer[] number = new Integer[]{5, 10, 15, 20, 25, 30, 35, 40, 45, 50};
	Integer[] rate = new Integer[]{30, 50, 70};
	String[] mode = new String[]{"自動點名", "手動點名", "隨機點名", "指定點名", "出席一覽"};
	ArrayAdapter<String> modeList;
	ArrayAdapter<Integer> numberList, rateList;
	static SetCallModeDialog newInstance(int position) {
		SetCallModeDialog fragment = new SetCallModeDialog();
		//Use bundle and argument to pass value.
		Bundle args = new Bundle();
		args.putInt("position", position);
		fragment.setArguments(args);
		return fragment;  
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		view = getActivity().getLayoutInflater().inflate(R.layout.modedialog_layout, null);
		position = getArguments().getInt("position");
		spinner1 = (Spinner)view.findViewById(R.id.spinner1);
		spinner2 = (Spinner)view.findViewById(R.id.spinner2);
		spinner3 = (Spinner)view.findViewById(R.id.spinner3);
		spinner1.setOnItemSelectedListener(selectMode);
		//Set spinner.
		modeList = new ArrayAdapter<String>(getActivity(), R.layout.spinner_course, mode);
		numberList = new ArrayAdapter<Integer>(getActivity(), R.layout.spinner_course, number);
		rateList = new ArrayAdapter<Integer>(getActivity(), R.layout.spinner_course, rate);
		spinner1.setAdapter(modeList);
		spinner2.setAdapter(numberList);
		spinner3.setAdapter(rateList);
		return new AlertDialog.Builder(getActivity())
			.setIcon(R.drawable.ic_launcher)
			.setView(view)
			.setPositiveButton("開始", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
					//Set default.
					if (selectedNumber == 0) {
						selectedNumber = 5;
					}
					if (selectedRate == 0) {
						selectedRate = 30;
					}
					((SelectListActivity)getActivity()).doPositiveClick(position, selectedMode, selectedNumber, selectedRate);
				}
			}).setNegativeButton("關閉", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int whichButton) {
				}
			}).create();
	}

	private OnItemSelectedListener selectMode = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
			selectedMode = position;
			//Determine spinner enabled or not.
			if (position != 2) {
				spinner2.setEnabled(false);
			} else {
				spinner2.setEnabled(true);
			}
			if (position != 3) {
				spinner3.setEnabled(false);
			} else {
				spinner3.setEnabled(true);
			}
			spinner2.setOnItemSelectedListener(selectNumber);
			spinner3.setOnItemSelectedListener(selectRate);
		}
		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	};
	
	//Select number.
	private OnItemSelectedListener selectNumber = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
			selectedNumber = number[position];
		}
		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	};
	
	//Select rate.
	private OnItemSelectedListener selectRate = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
			selectedRate = rate[position];
		}
		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	};
}
