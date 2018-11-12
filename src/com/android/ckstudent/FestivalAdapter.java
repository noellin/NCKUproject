package com.android.ckstudent;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class FestivalAdapter extends BaseAdapter {
	ArrayList<Festival> festivals;
	Context context;
	//Constructor
	FestivalAdapter(Context context, ArrayList<Festival> festivals) {
		this.context = context;
		this.festivals = festivals;
	}
	@Override
	public int getCount() {
		return festivals.size();
	}
	@Override
	public Object getItem(int position) {
		return festivals.get(position);
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
				view = inflater.inflate(R.layout.row_festival, null);
				TextView textView = (TextView) view.findViewById(R.id.festival_name);
				//Set name and date in text.
				textView.setText(festivals.get(position).name);
				textView = (TextView) view.findViewById(R.id.festival_date);
				textView.setText(festivals.get(position).date);
			} else {
				view = (View)convertView;
			}
			return view;
	}
}
