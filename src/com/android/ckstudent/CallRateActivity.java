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
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.protocol.HTTP;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/*
 * This activity is to let teacher check presentation rate below standard.
 */
public class CallRateActivity extends ListActivity {
	StudentAdapter adapter;
	String courseNo, url;
	int rate;
	ArrayList<Student> students;
	ArrayList<Student> uploadStudents;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		students = new ArrayList<Student>();
		uploadStudents = new ArrayList<Student>();
		//Get bundle passed from SelectListActivity.
        Bundle course = CallRateActivity.this.getIntent().getExtras();
        courseNo = course.getString("courseNo");
        url = course.getString("Url");
        rate = course.getInt("Standard");
        adapter = new StudentAdapter(this, students, courseNo);
		this.setListAdapter(adapter);
		getListView().setBackgroundResource(R.drawable.custombg);
		//Get student list.
		getStudentData.execute();
	}
	
	//Get student list by using webapi.
	AsyncTask<Void, Void, Void> getStudentData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
			mDialog = new ProgressDialog(CallRateActivity.this);
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
				HttpGet httpRequest = new HttpGet(url + "RollCallResult?CourseNo=" + courseNo);
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
					//Convert JSON to student list.
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Student student = new Student();
						student.no = jsonObject.getString("StudentNo");
						student.name = jsonObject.getString("StudentName");
						if (Integer.valueOf(jsonObject.getString("AttendPercentage")) < rate) {
							students.add(student);
						}
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
			//if list is null.
			if (students.size() == 0) {
				AlertDialog.Builder builder = new AlertDialog.Builder(CallRateActivity.this);
				builder.setTitle("注意");
				builder.setMessage("沒有低於這個出席率的學生");
				builder.setPositiveButton("我知道了", notice);
				builder.show();
			}
			setListAdapter(getListAdapter());
		}
	};
	
	private DialogInterface.OnClickListener notice = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			finish();
		}
	};
	
	//Upload the presentation result by WebApi.
	AsyncTask<Void, Void, Void> postStudentData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create dialog. Consider using horizontal progress bar.
			mDialog = new ProgressDialog(CallRateActivity.this);
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
				HttpPost httpRequest = new HttpPost(url + "RollCall");
				JSONArray jsonArray = new JSONArray();
				try {
					//Convert the result list into JSON.
					for(int i = 0; i < uploadStudents.size(); i++) {
						JSONObject jsonObject = new JSONObject();
						jsonObject.put("CourseNo", courseNo);
						jsonObject.put("StudentNo", uploadStudents.get(i).no.toString());
						jsonObject.put("Date", uploadStudents.get(i).date.toString());
						jsonArray.put(jsonObject);
					}
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
			Toast.makeText(CallRateActivity.this, "上傳成功!", Toast.LENGTH_LONG).show();
			//End the activity.
			finish();
		}
	};
	
	//Choose only absent student ready to upload.
	public void createUploadList() {
		for (int i=0; i<students.size(); i++) {
			if (students.get(i).status == false) {
				uploadStudents.add(students.get(i));
			}
		}
	}
	
	private DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//Prepare upload list.
			createUploadList();
			postStudentData.execute();
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.uploadend:
			//Show alert to ensure.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Notice");
			builder.setMessage("確定上傳嗎?該節課一天僅能上傳一次!");
			builder.setPositiveButton("上傳", listener);
			builder.setNegativeButton("取消", null);
			builder.show();
			break;
		case android.R.id.home:
        	onBackPressed();
            break;
		}
		return true;
	}
}
