package com.android.ckstudent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

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

import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ListView;

/*
 * This activity is to let student see their assignment.
 */
public class AssignmentStudentActivity extends ListActivity {
	String url, studentNo, accountType;
	EntryAdapter adapter;
	ArrayList<Event> events;
	ArrayList<Item> items;
	ArrayList<CourseCount> courseName;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		events = new ArrayList<Event>();
		items = new ArrayList<Item>();
		courseName = new ArrayList<CourseCount>();
		adapter = new EntryAdapter(this, items);
		this.setListAdapter(adapter);
		getListView().setBackgroundResource(R.drawable.custombg);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//Get value.
		Bundle bundle = this.getIntent().getExtras();
		url = bundle.getString("Url");
		studentNo = bundle.getString("StudentNo");
		accountType = bundle.getString("AccountType");
		//Get assignment data.
		getAssignmentData.execute();
	}
	
	//Get assignment data by Webapi use StudentNo.
	AsyncTask<Void, Void, Void> getAssignmentData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
			mDialog = new ProgressDialog(AssignmentStudentActivity.this);
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
				HttpGet httpRequest = new HttpGet(url + "Announcement?StudentNo=" + studentNo);
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
						event.name = jsonObject.getString("Name");
						event.date = jsonObject.getString("Date");
						event.course = jsonObject.getString("CourseName");
						//Change type.
						char[] c = event.date.toCharArray();
						event.viewDate = String.valueOf(c, 4, 2) + "/" + String.valueOf(c, 6, 2);
						events.add(event);
					}
			        //Separate
			        distribute();
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
			setListAdapter(getListAdapter());
		}
	};
	//Separate course and assign
	void distribute() {
		//Record how many course and assigns.
		for (int i=0; i<events.size(); i++) {
			if (!courseName.contains(events.get(i).course)) {
				CourseCount courseCount = new CourseCount();
				courseCount.name = events.get(i).course;
				courseCount.count = 1;
				courseName.add(courseCount);
			} else {
				for (int j=0; j<courseName.size(); j++) {
					if (events.get(i).course.equals(courseName.get(j).name)) {
						courseName.get(j).count++;
					}
				}
			}
		}
		//Create section list.
		for (int i=0; i<courseName.size(); i++) {
			items.add(new SectionItem(courseName.get(i).name));
			for (int j=0; j<events.size(); j++) {
				if (courseName.get(i).name.equals(events.get(j).course)) {
					items.add(new EntryItem(events.get(j).name, events.get(j).viewDate, events.get(j).id));
				}
			}
		}
	}
	
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		EntryItem item = (EntryItem)items.get(position);
		//Go to detail page.
		Intent intent = new Intent();
		intent.setClass(this, AssignmentDetailActivity.class);
		Bundle bundle = new Bundle();
		bundle.putString("EventId", item.id);
		bundle.putString("AccountType", accountType);
		bundle.putString("Url", url);
		intent.putExtras(bundle);
		startActivity(intent);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
        case android.R.id.home:
        	onBackPressed();
            break;
        }
		return true;
	}
}
