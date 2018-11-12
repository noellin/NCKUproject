package com.android.ckstudent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/*
 * This fragment is to show whole homework that user has.
 */
public class HomeworkFragment extends ListFragment {
	EventAdapter adapter;
	ArrayList<Event> events;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		events = new ArrayList<Event>();
		adapter = new EventAdapter(this.getActivity(), events);
		this.setListAdapter(adapter);
		//Check the file exists or not. If true, get the data.
		if (fileExistance("homework")) {
			getData();
		}
	}
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		//Use bundle to pass information in order to open the homework detail.
		Intent intent = new Intent();
		intent.setClass(getActivity(), TaskDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("eventName", events.get(position).name);
		bundle.putString("eventType", events.get(position).type);
		bundle.putString("eventDate", events.get(position).date);
		bundle.putString("eventviewDate", events.get(position).viewDate);
		bundle.putString("eventContent", events.get(position).content);
		intent.putExtras(bundle);
		startActivity(intent);
	}
	
	//If the file exists, get data from internal storage.
	private void getData() {
		JSONArray jsonArray;
		JSONObject jsonObject;
		FileInputStream fileInput;
		try {
			//Open the file and get content.
			fileInput = getActivity().openFileInput("homework");
			byte[] input = new byte[fileInput.available()];
			while (fileInput.read(input) != -1) {}
			//Convert into JSONArray.
			jsonArray = new JSONArray(new String(input));
			for (int i=0; i<jsonArray.length(); i++) {
				jsonObject = jsonArray.getJSONObject(i);
				Event event= new Event();
				//Convert into JSONObject.
				event.name = jsonObject.getString("eventName");
				event.date = jsonObject.getString("eventDate");
				event.viewDate = jsonObject.getString("eventviewDate");
				event.type = jsonObject.getString("eventType");
				event.content = jsonObject.getString("eventContent");
				events.add(event);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setListAdapter(getListAdapter());
	}
	
	//Check file exists or not.
	private boolean fileExistance(String fname) {
		File file = getActivity().getBaseContext().getFileStreamPath(fname);
		if (file.exists()) {
			return true;
		}
		else {
			return false;
		}    
	}
	@Override
	public void onResume() {
		events.clear();
		if (fileExistance("homework")) {
			getData();
		}
		setListAdapter(getListAdapter());
		super.onResume();
	}
	
}