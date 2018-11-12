package com.android.ckstudent;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.ListActivity;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

public class StudentListActivity extends ListActivity {
	ArrayList<Item> items;
	ArrayList<String> presence, absence;
	StudentListAdapter adapter;
	JSONArray jsonArray;
	String studentList;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getIntent().getExtras();
		studentList = bundle.getString("StudentList");
		presence = new ArrayList<String>();
		absence = new ArrayList<String>();
		//String to ArrayList.
		convertString();
		items = new ArrayList<Item>();
		//Section.
		init();
		adapter = new StudentListAdapter(this, items);
		setListAdapter(adapter);
	}
	//Convert string into ArrayList.
	private void convertString() {
		try {
			jsonArray = new JSONArray(studentList);
			for (int i=0; i<jsonArray.length(); i++) {
				JSONObject jsonObject = jsonArray.getJSONObject(i);
				if ("true".equals(jsonObject.getString("StudentStatus"))) {
					presence.add(jsonObject.getString("StudentName"));
				} else {
					absence.add(jsonObject.getString("StudentName"));
				}
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
	}
	//Put into section.
	private void init() {
		items.add(new SectionItem("¥X®u"));
		for(int i=0; i<presence.size(); i++) {
			items.add(new EntryItem(presence.get(i), null, null));
		}
		items.add(new SectionItem("¯Ê®u"));
		for(int i=0; i<absence.size(); i++) {
			items.add(new EntryItem(absence.get(i), null, null));
		}
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		/*
		Item i = items.get(position);
		if (!i.isSection()) {
			EntryItem ei = (EntryItem)i;
			if (presence.contains(ei)) {
				//remove from presence.
				//add to absence.
			} else {
				//add to presence.
				//remove from absence.
			}
		}
		items.clear();
		init();
		*/
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case android.R.id.home:
			onPause();
            break;
		}
		return true;
	}
	@Override
	protected void onPause() {
		super.onPause();
		finish();
	}
}
