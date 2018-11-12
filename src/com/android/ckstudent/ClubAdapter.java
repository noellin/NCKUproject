package com.android.ckstudent;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

public class ClubAdapter extends BaseAdapter {
	ArrayList<Club> clubs;
	Context context;
	//Constructor
	ClubAdapter(Context context, ArrayList<Club> clubs) {
		this.context = context;
		this.clubs = clubs;
	}
	@Override
	public int getCount() {
		return clubs.size();
	}
	@Override
	public Object getItem(int position) {
		return clubs.get(position);
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
				view = inflater.inflate(R.layout.row_club, null);
				//Set association name to text.
				TextView textView = (TextView) view.findViewById(R.id.club_name);
				textView.setText(clubs.get(position).name);
			} else {
				view = (View)convertView;
			}
			return view;
	}
}
