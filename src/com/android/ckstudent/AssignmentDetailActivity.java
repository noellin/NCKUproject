package com.android.ckstudent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

public class AssignmentDetailActivity extends Activity {
	Event eventFull;
	int month, day, eventDate, currentYear, selectedMonth, selectedDay;
	Spinner spinner1, spinner2;
	EditText editText1, editText2;
	TextView textView2, textView4;
	String accountType, url, date, eventId, eventCourse;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.assign_detail);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		View background = findViewById(R.id.bg);
		background.setBackgroundResource(R.drawable.custombg);
		editText1 = (EditText)this.findViewById(R.id.editText1);
		editText2 = (EditText)this.findViewById(R.id.editText2);
		textView2 = (TextView)this.findViewById(R.id.textView2);
		textView4 = (TextView)this.findViewById(R.id.textView4);
		//Let user click to edit.
		editText2.setClickable(true);
		editText2.setOnClickListener(click);
		editText2.setOnFocusChangeListener(focus);
		editText2.setKeyListener(null);
		//Get information from AssignmentActivity with bundle.
		Bundle bundle = new Bundle();
		bundle = this.getIntent().getExtras();
		url = bundle.getString("Url");
		eventId = bundle.getString("EventId");
		accountType = bundle.getString("AccountType");
		//Not allow student edit.
		if ("Student".equals(accountType)) {
			editText1.setEnabled(false);
			editText2.setEnabled(false);
		}
		//Get assignment data.
		getAssignmentData.execute();
	}
	
	//Let user use datepicker to change date.
	private OnClickListener click = new OnClickListener() {
		@Override
		public void onClick(View v) {
			SetDateDialog dialogFragment = SetDateDialog.newInstance(currentYear, month-1, day);
			dialogFragment.show(getFragmentManager(), "dialog");
		}
	};
	//Let user use datepicker to change date.
	private OnFocusChangeListener focus = new OnFocusChangeListener() {
		@Override
		public void onFocusChange(View v, boolean hasFocus) {
			if (hasFocus) {
				SetDateDialog dialogFragment = SetDateDialog.newInstance(currentYear, month-1, day);
				dialogFragment.show(getFragmentManager(), "dialog");
			}
		}
	};
	//Get assignment data by Webapi use EventId.
	AsyncTask<Void, Void, Event> getAssignmentData = new AsyncTask<Void, Void, Event>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
			mDialog = new ProgressDialog(AssignmentDetailActivity.this);
	        mDialog.setMessage("Loading...");
	        //mDialog.setCancelable(false);
	        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        mDialog.show();
		}
		@Override
		protected Event doInBackground(Void... arg0) {
			try {
				//Set connection parameters.
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
				HttpConnectionParams.setSoTimeout(httpParameters, 5000);
				//Construct http client and http get request objects.
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpRequest = new HttpGet(url + "Announcement?Id=" + eventId);
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
						eventFull = new Event();
						eventFull.name = jsonObject.getString("Name");
						date = jsonObject.getString("Date");
						eventFull.date = date;
						eventFull.course = jsonObject.getString("CourseNo");
						//Change string to each type.
						char[] c = date.toCharArray();
						String temp = String.valueOf(c, 0, 4);
						currentYear = Integer.valueOf(temp);
						temp = String.valueOf(c, 4, 2);
						month = Integer.valueOf(temp);
						temp = String.valueOf(c, 6, 2);
						day = Integer.valueOf(temp);
						eventFull.viewDate = String.valueOf(c, 4, 2) + "/" + String.valueOf(c, 6, 2);
						eventFull.type = jsonObject.getString("Type");
						eventFull.content = jsonObject.getString("Description");
					}
			        return eventFull;
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Event event) {
			super.onPostExecute(event);
			//Set name and date to text.
			textView2.setText(event.name);
			editText1.setText(event.content);
			editText2.setText(currentYear + "/" + event.viewDate);
			mDialog.dismiss();
		}
	};
	
	//Put assignment data by Webapi use EventId.
	AsyncTask<Void, Void, Void> putAssignmentData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
			mDialog = new ProgressDialog(AssignmentDetailActivity.this);
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
				HttpPut httpRequest = new HttpPut(url + "Announcement/");
				JSONObject jsonObject = new JSONObject();
				JSONArray jsonArray = new JSONArray();
				try {
					//Convert the result list into JSON.
					jsonObject.put("Id", eventId);
					jsonObject.put("Name", eventFull.name);
					jsonObject.put("Date", date);
					jsonObject.put("CourseNo", eventFull.course);
					jsonObject.put("Type", eventFull.type);
					jsonObject.put("Description", editText1.getText().toString());
					jsonArray.put(jsonObject);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				StringEntity stringEntity = new StringEntity(jsonArray.toString(), HTTP.UTF_8);
				stringEntity.setContentType("application/json");
				httpRequest.setHeader("Content-type", "application/json");
				httpRequest.setHeader("Accept", "application/json");
				httpRequest.setEntity(stringEntity);
				//Send request to web server.
				//HttpResponse httpResponse = httpClient.execute(httpRequest);
				httpClient.execute(httpRequest);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mDialog.dismiss();
			Toast.makeText(AssignmentDetailActivity.this, "修改成功", Toast.LENGTH_LONG).show();
		}
	};
	
	//Delete assignment data by Webapi use EventId.
	AsyncTask<Void, Void, Void> deleteAssignmentData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
			mDialog = new ProgressDialog(AssignmentDetailActivity.this);
	        mDialog.setMessage("Loading...");
	        //mDialog.setCancelable(false);
	        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        mDialog.show();
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				//Construct http client and http post request objects.
				HttpClient httpClient = new DefaultHttpClient();
				HttpDelete httpRequest = new HttpDelete(url + "Announcement/" + eventId);
				//HttpResponse httpResponse = httpClient.execute(httpRequest);
				httpClient.execute(httpRequest);
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mDialog.dismiss();
			Toast.makeText(AssignmentDetailActivity.this, "刪除成功", Toast.LENGTH_LONG).show();
		}
	};
	
	//Add assignment to to-do-list.
	private void addTodo(String type) {
		Event event = new Event();
		event.name = eventFull.name;
		event.date = eventFull.date;
		event.viewDate = eventFull.viewDate;
		event.content = eventFull.content;
		event.type = type;
		JSONArray jsonArray = null;
		JSONObject jsonObject= new JSONObject();
		//Check file exists or not. If exists read it first.
		if (fileExistance(type)) {
			FileInputStream fileInput;
			try {
				fileInput = openFileInput(type);
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
			jsonObject.put("eventContent", event.content);
			jsonArray.put(jsonObject);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		//Write the change into file.
		FileOutputStream fileOutput;
		try {
			fileOutput = openFileOutput(type, Context.MODE_PRIVATE);
			fileOutput.write(jsonArray.toString().getBytes());
			fileOutput.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		Toast.makeText(this, "加入待辦清單成功", Toast.LENGTH_SHORT).show();
	}
	
	//Change date form.
	void changeDate(int year, int month, int day) {
		date = String.valueOf(year*10000 + month*100 + day);
		editText2.setText(year + "/" + month + "/" + day);
		currentYear = year;
		this.month = month;
		this.day = day;
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
	
	//Use type to determine action bar.
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if ("Student".equals(accountType)) {
			getMenuInflater().inflate(R.menu.assignstudent, menu);
		} else {
			getMenuInflater().inflate(R.menu.assignedit, menu);
		}
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.addtodo:
			addTodo(eventFull.type);
			break;
		case R.id.put:
			//edit
			putAssignmentData.execute();
			break;
		case R.id.delete:
			//delete
			deleteAssignmentData.execute();
			break;
		case android.R.id.home:
        	onBackPressed();
            break;
		}
		return true;
	}
	
}
