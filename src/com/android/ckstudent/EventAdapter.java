package com.android.ckstudent;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class EventAdapter extends BaseAdapter {
	ArrayList<Event> events;
	Context context;
	//Constructor
	EventAdapter(Context context, ArrayList<Event> events) {
		this.context = context;
		this.events = events;
	}
	@Override
	public int getCount() {
		return events.size();
	}
	@Override
	public Object getItem(int position) {
		return events.get(position);
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
			view = inflater.inflate(R.layout.row_task, null);
			TextView txt1 = (TextView)view.findViewById(R.id.textView1);
			TextView txt2 = (TextView)view.findViewById(R.id.textView2);
			//Set date and name to text.
			txt1.setText(events.get(position).viewDate);
			txt2.setText(events.get(position).name);
			} else {
				view = convertView;
			}
			return view;
	}
}
