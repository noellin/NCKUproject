package com.android.ckstudent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.AdapterView.OnItemSelectedListener;

/*
 * This activity is to let teacher assign a homework or a contest notification to students.
 * Use teacher name to post a assignment.
 */
public class AssignmentActivity extends Activity {
	String url, teacherNo, selectedCourse, accountType;
	ListView listView;
	List<String> courseNo, courseName;
	TextView textView;
	Spinner spinner;
	ArrayAdapter<String> courseList;
	EventAdapter adapter;
	ArrayList<Event> events, subEvents;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.assign_list);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		View background = findViewById(R.id.bg);
		background.setBackgroundResource(R.drawable.custombg);
		textView = (TextView)this.findViewById(R.id.textView1);
		spinner = (Spinner)this.findViewById(R.id.spinner1);
		listView = (ListView)this.findViewById(R.id.listView1);
		events = new ArrayList<Event>();
		subEvents = new ArrayList<Event>();
		courseNo = new ArrayList<String>();
		courseName = new ArrayList<String>();
		adapter = new EventAdapter(this, subEvents);
		//Get value.
		Bundle bundle = this.getIntent().getExtras();
		url = bundle.getString("Url");
		teacherNo = bundle.getString("TeacherNo");
		accountType = bundle.getString("AccountType");
		textView.setText("½Ð¿ï¾Ü½Òµ{:");
		//Get course by WebApi.
		getCourseData.execute();
	}
	
	//Get course by Webapi use teahcerNo.
	AsyncTask<Void, Void, Void> getCourseData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
			mDialog = new ProgressDialog(AssignmentActivity.this);
	        mDialog.setMessage("Loading...");
	        //mDialog.setCancelable(false);
	        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        mDialog.show();
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				//Set connection parameters.
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
				HttpConnectionParams.setSoTimeout(httpParameters, 5000);
				//Construct http client and http get request objects.
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpRequest = new HttpGet(url + "CourseData?TeacherNo=" + teacherNo);
				//Send request to web server.
				HttpResponse httpResponse = httpClient.execute(httpRequest);
				//Process the response.
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity httpEntity = httpResponse.getEntity();
					InputStream content = httpEntity.getContent();			        
			        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			        StringBuilder builder = new StringBuilder();
			        String line;
			        while ((line = reader.readLine()) != null) {
			        	builder.append(line);
			        }
			        //Convert into JSONArray.
			        JSONArray jsonArray =  new JSONArray(builder.toString());
			        //Convert JSON to course list.
			        for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						courseNo.add(jsonObject.getString("CourseNo"));
						courseName.add(jsonObject.getString("Name"));
					}		
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mDialog.dismiss();
			courseList = new ArrayAdapter<String>(AssignmentActivity.this, R.layout.spinner_course, courseName);
			//Set spinner.
			spinner.setAdapter(courseList);
			spinner.setOnItemSelectedListener(chooseCourse);
			getAssignmentData.execute();
		}
	};
	
	//Get assignment by Webapi use selectedCourseNo.
	AsyncTask<Void, Void, Void> getAssignmentData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
			mDialog = new ProgressDialog(AssignmentActivity.this);
	        mDialog.setMessage("Loading...");
	        //mDialog.setCancelable(false);
	        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        mDialog.show();
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				//Set connection parameters.
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
				HttpConnectionParams.setSoTimeout(httpParameters, 5000);
				//Construct http client and http get request objects.
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpRequest = new HttpGet(url + "Announcement");
				//Send request to web server.
				HttpResponse httpResponse = httpClient.execute(httpRequest);
				//Process the response.
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity httpEntity = httpResponse.getEntity();
					InputStream content = httpEntity.getContent();			        
			        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			        StringBuilder builder = new StringBuilder();
			        String line;
			        while ((line = reader.readLine()) != null) {
			        	builder.append(line);
			        }
			        //Convert into JSONArray.
			        JSONArray jsonArray =  new JSONArray(builder.toString());
			        //Convert JSON to course list.
			        for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Event event = new Event();
						event.id = jsonObject.getString("Id");
						event.course = jsonObject.getString("CourseNo");
						event.name = jsonObject.getString("Name");
						event.date = jsonObject.getString("Date");
						//Turn string into integer.
						char[] c = event.date.toCharArray();
						event.viewDate = String.valueOf(c, 4, 2) + "/" + String.valueOf(c, 6, 2);
						events.add(event);
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mDialog.dismiss();
			//Set adapter.
			listView.setAdapter(adapter);
			getSubAssignmentData(courseNo.get(0));
			listView.setAdapter(listView.getAdapter());
			listView.setOnItemClickListener(forDetail);
		}
	};
	
	private OnItemClickListener forDetail = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
			Bundle bundle = new Bundle();
			Intent intent = new Intent();
			//Go to detail page.
			bundle.putString("EventId", events.get(position).id);
			bundle.putString("AccountType", accountType);
			bundle.putString("Url", url);
			intent.putExtras(bundle);
			intent.setClass(AssignmentActivity.this, AssignmentDetailActivity.class);
			startActivity(intent);
		}
	};
	
	//Get sub data.
	void getSubAssignmentData(String selectedCourse) {
		subEvents.clear();
		for (int i=0; i<events.size(); i++) {
			if ((events.get(i).course).equals(selectedCourse)) {
				subEvents.add(events.get(i));
			}
		}
		adapter.notifyDataSetChanged();
	}
	
	//Chosen selection.
	private OnItemSelectedListener chooseCourse = new OnItemSelectedListener() {
		@Override
		public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
			selectedCourse = courseNo.get(position);
			getSubAssignmentData(selectedCourse);
		}
		@Override
		public void onNothingSelected(AdapterView<?> adapterView) {
		}
	};
	
	//Go to new page if want a new assignment.
	void addAssignment(String selectedCourse, String type, String date) {
		Bundle bundle = new Bundle();
		Intent intent = new Intent();
		bundle.putString("Url", url);
		bundle.putString("Course", selectedCourse);
		bundle.putString("Type", type);
		bundle.putString("Date", date);
		intent.putExtras(bundle);
		intent.setClass(this, AssignmentNewActivity.class);
		startActivity(intent);
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.assignadd, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		//Click the upload button on action bar.
		case R.id.addassign:
			//Fragment to show information.
			SetAssignmentDialog dialogFragment = SetAssignmentDialog.newInstance(courseNo, courseName);
	        dialogFragment.show(getFragmentManager(), "dialog");
			break;
		case android.R.id.home:
        	onBackPressed();
            break;
		}
		return true;
	}
}
