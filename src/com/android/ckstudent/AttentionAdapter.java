package com.android.ckstudent;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class AttentionAdapter extends BaseAdapter {
	ArrayList<StudentRate> students;
	Context context;
	AttentionAdapter(Context context, ArrayList<StudentRate> students) {
		this.context = context;
		this.students = students;
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
				view = inflater.inflate(R.layout.row_attention, null);
				TextView textView = (TextView)view.findViewById(R.id.textView1);
				textView.setText(students.get(position).name);
			} else {
				view = (View) convertView;
			}
			return view;
	}
}
