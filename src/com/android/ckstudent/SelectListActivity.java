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

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

/*
 * This activtiy is to show what courses teacher have.
 * Also, teacher use this activity to choose which course ready to call.
 */
public class SelectListActivity extends ListActivity {
	String url, teacherNo;
	CourseAdapter adapter;
	ArrayList<Course> courses;
	ListView listView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		courses = new ArrayList<Course>();
		adapter = new CourseAdapter(this, courses);
		this.setListAdapter(adapter);
		getListView().setBackgroundResource(R.drawable.custombg);
		getListView().setOnItemLongClickListener(listener);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		Bundle bundle = this.getIntent().getExtras();
		teacherNo = bundle.getString("TeacherNo");
		url = bundle.getString("Url");
		//Get course data.
		getCourseData.execute();
		Toast.makeText(this, "長按課程可選擇點名方式", Toast.LENGTH_LONG).show();
	}
	//Get course data by WebApi.
	AsyncTask<Void, Void, Void> getCourseData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
			mDialog = new ProgressDialog(SelectListActivity.this);
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
						Course course = new Course();
						course.No = jsonObject.getString("CourseNo");
						course.Name = jsonObject.getString("Name");
						courses.add(course);
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
			//Alert if list is null.
			if (courses.size() == 0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(SelectListActivity.this);
				builder.setTitle("Error");
				builder.setMessage("You don't have course or internet invalid.");
				builder.setPositiveButton("我知道了", null);
				builder.show();
			} else {
				//findViewById(android.R.id.empty).setVisibility(View.GONE);
			}
			setListAdapter(getListAdapter());
		}
	};
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		//Go to call activity.
		Bundle courseName = new Bundle();
		Intent intent = new Intent();
		intent.setClass(this, CallActivity.class);
		courseName.putString("Url", url);
		courseName.putString("courseNo", courses.get(position).No);
		intent.putExtras(courseName);
		startActivity(intent);
	}
	
	//Long click to show choise.
	private ListView.OnItemLongClickListener listener = new ListView.OnItemLongClickListener() {
		@Override
		public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
			SetCallModeDialog dialog =  SetCallModeDialog.newInstance(position);
			dialog.show(getFragmentManager(), "modeDialog");
			return true;
		}
		
	};
	//Selection
	public void doPositiveClick(int position, int selectedMode, int selectedNumber, int selectedRate) {
		Bundle courseName = new Bundle();
		Intent intent = new Intent();
		courseName.putString("courseNo", courses.get(position).No);
		courseName.putString("Url", url);
		switch (selectedMode) {
		case 0:
			//Bluetooth
			intent.putExtras(courseName);
			intent.setClass(this, CallBTActivity.class);
			startActivity(intent);
			break;
		case 1:
			//Manual
			intent.putExtras(courseName);
			intent.setClass(this, CallActivity.class);
			startActivity(intent);
			break;
		case 2:
			//Random
			courseName.putInt("Number", selectedNumber);
			intent.putExtras(courseName);
			intent.setClass(this, CallRandomActivity.class);
			startActivity(intent);
			break;
		case 3:
			//Rate
			courseName.putInt("Standard", selectedRate);
			intent.putExtras(courseName);
			intent.setClass(this, CallRateActivity.class);
			startActivity(intent);
			break;
		case 4:
			//Presentation
			intent.putExtras(courseName);
			intent.setClass(this, PresentationActivity.class);
			startActivity(intent);
			break;
		}
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
