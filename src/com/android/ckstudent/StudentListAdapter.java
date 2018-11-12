package com.android.ckstudent;

import java.util.ArrayList;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

public class StudentListAdapter extends ArrayAdapter<Item> {
	Context context;
	ArrayList<Item> items;
	public StudentListAdapter(Context context,ArrayList<Item> items) {
		super(context,0, items);
		this.context = context;
		this.items = items;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = convertView;
		final Item i = items.get(position);
		if (i != null) {
			if(i.isSection()){
				SectionItem si = (SectionItem)i;
				v = inflater.inflate(R.layout.item_section, null);
				v.setOnClickListener(null);
				v.setOnLongClickListener(null);
				v.setLongClickable(false);
				final TextView sectionView = (TextView) v.findViewById(R.id.list_item_section_text);
				sectionView.setText(si.getTitle());
			}else{
				EntryItem ei = (EntryItem)i;
				v = inflater.inflate(R.layout.item_entry, null);
				final TextView name = (TextView)v.findViewById(R.id.list_item_entry_title);
				if(name != null)
					name.setText(ei.name);
			}
		}
		return v;
	}
}
