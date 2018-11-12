package com.android.ckstudent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ProgressBar;
import android.widget.TextView;

/*
 * This adapter is to show student's list.
 */
public class RateAdapter extends BaseAdapter {
	ArrayList<StudentRate> students;
	String courseNo;
	Context context;
	RateAdapter(Context context, ArrayList<StudentRate> students) {
		this.context = context;
		this.students = students;
	}
	RateAdapter(Context context, ArrayList<StudentRate> students, String courseNo) {
		this.context = context;
		this.students = students;
		this.courseNo = courseNo;
	}
	@Override
	public int getCount() {
		return students.size();
	}
	@Override
	public Object getItem(int position) {
		return students.get(position);
	}
	@Override
	public long getItemId(int position) {
		return position;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view;
		if (convertView == null) {
				view = inflater.inflate(R.layout.row_rate, null);
				TextView textView = (TextView)view.findViewById(R.id.textView1);
				ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.progressBar1);
				textView.setText(students.get(position).name);
				textView = (TextView) view.findViewById(R.id.textView2);
				textView.setText(students.get(position).rate + " %");
				int progress = Integer.valueOf(students.get(position).rate);
				progressBar.setProgress(progress);
				if (checkAttention(courseNo, position)) {
					view.setBackgroundResource(R.drawable.lightorange);
				}
			} else {
				view = (View) convertView;
			}
			return view;
	}
	
	//Check student is in the attention list or not.
	private boolean checkAttention(String courseNo, int position) {
		JSONArray jsonArray = null;
		JSONObject jsonObject= new JSONObject();
		//Check file exists or not.
		if (fileExistance(courseNo)) {
			FileInputStream fileInput;
			try {
				fileInput = context.getApplicationContext().openFileInput(courseNo);
				byte[] input = new byte[fileInput.available()];
				while (fileInput.read(input) != -1) {}
				//Convert into JSONArray.
				jsonArray = new JSONArray(new String(input));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Convert into JSONObject.
			for (int i=0; i<jsonArray.length(); i++) {
				try {
					jsonObject = jsonArray.getJSONObject(i);
					//Find if there is a same activity.
					if (students.get(position).no.equals(jsonObject.getString("studentNo"))) {
						return true;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} else {
			return false;
		}
		return false;
	}
		
	//Check file exists or not.
	private boolean fileExistance(String fname) {
		File file = context.getFileStreamPath(fname);
		if (file.exists()) {
			return true;
		}
		else {
			return false;
		}    
	}
}
