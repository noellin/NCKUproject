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
import android.widget.TextView;

/*
 * This fragment is to show activity that user is interested in.
 */
public class FavoriteFragment extends ListFragment {
	String url;
	FestivalAdapter adapter;
	ArrayList<Festival> festivals;
	TextView txt;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		txt = (TextView)getActivity().findViewById(R.id.textView1);
		festivals = new ArrayList<Festival>();
		adapter = new FestivalAdapter(this.getActivity(), festivals);
		this.setListAdapter(adapter);
		//Check file exist or not. if exists then get the data.
		Bundle bundle = getActivity().getIntent().getExtras();
		url = bundle.getString("Url");
		if (fileExistance("festival")) {
			getFavorData();
		}
	}
	
	//Get the data stored in internal storage.
	private void getFavorData() {
		JSONArray jsonArray;
		JSONObject jsonObject;
		//Open the file and get data into JSONArray.
		FileInputStream fileInput;
		try {
			fileInput = getActivity().openFileInput("festival");
			byte[] input = new byte[fileInput.available()];
			while (fileInput.read(input) != -1) {}
			jsonArray = new JSONArray(new String(input));
			for (int i=0; i<jsonArray.length(); i++) {
				//Put each one into activity list.
				jsonObject = jsonArray.getJSONObject(i);
				Festival festival = new Festival();
				festival.name = jsonObject.getString("festivalName");
				festival.date = jsonObject.getString("festivalDate");
				festival.id = jsonObject.getString("festivalId");
				festivals.add(festival);
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
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		//Use bundle to pass information in order to start the activity detail page.
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("Url", url);
		bundle.putString("festivalName", festivals.get(position).name);
		bundle.putString("festivalDate", festivals.get(position).date);
		bundle.putString("festivalId", festivals.get(position).id);
		intent.putExtras(bundle);
		intent.setClass(getActivity(), FestivalContent.class);
        startActivity(intent);
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
		festivals.clear();
		if (fileExistance("festival")) {
			getFavorData();
		}
		setListAdapter(getListAdapter());
		super.onResume();
	}
	
}
