package com.android.ckstudent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

/*
 * This adapter is to show student.
 */
public class StudentAdapter extends BaseAdapter{
	ArrayList<Student> students;
	String courseNo;
	Context context;
	//Constructor
	StudentAdapter(Context context, ArrayList<Student> students) {
		this.context = context;
		this.students = students;
	}
	StudentAdapter(Context context, ArrayList<Student> students, String courseNo) {
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
		final int listPosition = position;
		if (convertView == null) {
				view = inflater.inflate(R.layout.row_student, null);
				TextView textView = (TextView) view.findViewById(R.id.text1);
				//Set student name to text.
				textView.setText(students.get(position).name);
				final CheckBox checkBox = (CheckBox)view.findViewById(R.id.checkBox1);
				//Get today's date.
				students.get(position).date = currentDate();
				//If status is true change checkbox.
				if (students.get(position).status == true) {
					checkBox.setChecked(true);
				}
				checkBox.setOnCheckedChangeListener(new CheckBox.OnCheckedChangeListener(){
					@Override
					public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
						//If checkbox changed, change the match attribute.
						if (checkBox.isChecked() == true) {
							students.get(listPosition).status = true;
							students.get(listPosition).time = currentTime();
						} else {
							students.get(listPosition).status = false;
							students.get(listPosition).time = null;
						}
					}
				});
				if (checkAttention(courseNo, position)) {
					view.setBackgroundResource(R.drawable.lightorange);
				}
			} else {
				view = convertView;
			}
			return view;
	}
	
	public String currentDate() {
		SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
		return sdf.format(new Date());
	}
	public String currentTime() {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm");
		return sdf.format(new Date());
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
