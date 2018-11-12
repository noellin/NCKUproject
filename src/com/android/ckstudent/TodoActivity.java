package com.android.ckstudent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;

/*
 * This activity is to show a to do list for user on both homework and contest.
 */
public class TodoActivity extends FragmentActivity {
	String type;
	private TabHost mTabHost;
	private TabManager mTabManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_main);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		View background = findViewById(R.id.bg);
		background.setBackgroundResource(R.drawable.custombg);
		//bundle
		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup();
		mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);
		//Set default page.
		mTabHost.setCurrentTab(0);
		//Dynamically add two tab for homework and contest.
        mTabManager.addTab(mTabHost.newTabSpec("Fragment1").setIndicator("作業"),
            HomeworkFragment.class, null);
        mTabManager.addTab(mTabHost.newTabSpec("Fragment2").setIndicator("考試"),
            ContestFragment.class, null);
	}
	
	//Add to do homework.
	private void addHomework(int year, int month, int day, String type, String taskName) {
		Event event = new Event();
		event.name = taskName;
		event.date = String.valueOf(year*10000 + month*100 + day);
		event.viewDate = month + "/" + day;
		event.type = "homework";
		JSONArray jsonArray = null;
		JSONObject jsonObject= new JSONObject();
		//Check file exists or not. If exists read it first.
		if (fileExistance("homework")) {
			FileInputStream fileInput;
			try {
				fileInput = openFileInput("homework");
				byte[] input = new byte[fileInput.available()];
				while (fileInput.read(input) != -1) {}
				//Convert to JSONArray.
				jsonArray = new JSONArray(new String(input));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			//If not exists, initial the JSONArray.
			jsonArray = new JSONArray();
		}
		try {
			//Add object to the end of the JSONArray.
			jsonObject.put("eventName", event.name);
			jsonObject.put("eventDate", event.date);
			jsonObject.put("eventType", event.type);
			jsonObject.put("eventviewDate", event.viewDate);
			jsonObject.put("eventContent", "");
			jsonArray.put(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//Write the change into file.
		FileOutputStream fileOutput;
		try {
			fileOutput = openFileOutput("homework", Context.MODE_PRIVATE);
			fileOutput.write(jsonArray.toString().getBytes());
			fileOutput.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	//Add to do contest.
	private void addContest(int year, int month, int day, String type, String taskName) {
		Event event = new Event();
		event.name = taskName;
		event.date = String.valueOf(year*10000 + month*100 + day);
		event.viewDate = month + "/" + day;
		event.type = "contest";
		JSONArray jsonArray = null;
		JSONObject jsonObject= new JSONObject();
		//Check file exists or not. If exists read it first.
		if (fileExistance("contest")) {
			FileInputStream fileInput;
			try {
				fileInput = openFileInput("contest");
				byte[] input = new byte[fileInput.available()];
				while (fileInput.read(input) != -1) {}
				//Convert to JSONArray.
				jsonArray = new JSONArray(new String(input));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			//If not exists, initial the JSONArray.
			jsonArray = new JSONArray();
		}
		
		try {
			//Add object to the end of the JSONArray.
			jsonObject.put("eventName", event.name);
			jsonObject.put("eventDate", event.date);
			jsonObject.put("eventType", event.type);
			jsonObject.put("eventviewDate", event.viewDate);
			jsonObject.put("eventContent", "");
			jsonArray.put(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//Write the change into file.
		FileOutputStream fileOutput;
		try {
			fileOutput = openFileOutput("contest", Context.MODE_PRIVATE);
			fileOutput.write(jsonArray.toString().getBytes());
			fileOutput.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	//Dialog left side click.
	public void doPositiveClick(int year, int month, int day, String type, String taskName) {
		//Accroding to the receive type to choose which function to run.
		if ("homework".equals(type)) {
			addHomework(year, month, day, type, taskName);
		} else {
			addContest(year, month, day, type, taskName);
		}
		Toast.makeText(getBaseContext(), "加入完成!", Toast.LENGTH_SHORT).show();
		mTabHost.setCurrentTab(1);
		mTabHost.setCurrentTab(0);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.task, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.addtask:
			//Show dialog to set task.
			SetTaskDialog dialogFragment = SetTaskDialog.newInstance();
	        dialogFragment.show(getSupportFragmentManager(), "dialog");
	        break;
		case android.R.id.home:
        	onBackPressed();
            break;
		}
		return true;
	}
	
	//Check file exists or not.
	private boolean fileExistance(String fname) {
		File file = getBaseContext().getFileStreamPath(fname);
		if (file.exists()) {
			return true;
		}
		else {
			return false;
		}    
	}
}
