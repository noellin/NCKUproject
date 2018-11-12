package com.android.ckstudent;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class CourseAdapter extends BaseAdapter {
	ArrayList<Course> courses;
	Context context;
	//Constructor
	CourseAdapter(Context context, ArrayList<Course> courses) {
		this.context = context;
		this.courses = courses;
	}
	@Override
	public int getCount() {
		return courses.size();
	}
	@Override
	public Object getItem(int position) {
		return courses.get(position);
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
				view = inflater.inflate(R.layout.row_courses, null);
				//Set course name to text.
				TextView textView = (TextView) view.findViewById(R.id.text1);
				textView.setText(courses.get(position).Name);
				//Set course date to text.
				textView = (TextView) view.findViewById(R.id.text2);
				textView.setText(courses.get(position).Date);
			} else {
				view = (View) convertView;
			}
			return view;
	}
}
