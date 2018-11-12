package com.android.ckstudent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Random;

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

public class CallRandomActivity extends ListActivity {
	int max, number;
	StudentAdapter adapter;
	String courseNo, url;
	ArrayList<Integer> randomNumber;
	ArrayList<Student> students, randomStudents, uploadStudents;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		students = new ArrayList<Student>();
		randomStudents = new ArrayList<Student>();
		uploadStudents = new ArrayList<Student>();
		randomNumber = new ArrayList<Integer>();
		//Use bundle to get value from SelectModeActivity.
		Bundle bundle = this.getIntent().getExtras();
		url = bundle.getString("Url");
		number = bundle.getInt("Number");
		//Get bundle passed from SelectListActivity.
        courseNo = bundle.getString("courseNo");
        adapter = new StudentAdapter(this, randomStudents, courseNo);
		this.setListAdapter(adapter);
		getListView().setBackgroundResource(R.drawable.custombg);
		//Get student data.
		getStudentData.execute();
	}
	
	//Get data by WebApi.
	AsyncTask<Void, Void, Void> getStudentData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
			mDialog = new ProgressDialog(CallRandomActivity.this);
	        mDialog.setMessage("Loading...");
	        //mDialog.setCancelable(false);
	        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        mDialog.show();
		}
		@Override
		protected Void doInBackground(Void... params) {
			try {
				//Set connection parameters.
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
				HttpConnectionParams.setSoTimeout(httpParameters, 5000);
				//Construct http client and http get request objects.
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpRequest = new HttpGet(url + "StudentList?CourseNo=" + courseNo);
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
			        //Get how many students in class.
			        max = jsonArray.length();
			        //Check if number bigger than total students.
			        if (number > max) {
			        	//Set number to the total students.
			        	number = max;
			        }
			        //Generate students would called.
			        randomStudent(number, max);
			        //Convert JSON to course list.
			        for (int i = 0; i<jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Student student = new Student();
						student.no = jsonObject.getString("StudentNo");
						student.name = jsonObject.getString("StudentName");
						students.add(student);
					}
				}
			    //Add studens who been choosed into random student list.
			    for (int i=0; i<number; i++) {
			    	randomStudents.add(students.get(randomNumber.get(i)));
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
	
	//Upload the presentation result by WebApi.
	AsyncTask<Void, Void, Void> postStudentData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create dialog. Consider using horizontal progress bar.
			mDialog = new ProgressDialog(CallRandomActivity.this);
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
			//Show success message.
			Toast.makeText(CallRandomActivity.this, "上傳成功!", Toast.LENGTH_LONG).show();
			//End the activity.
			finish();
		}
	};
	
	//Determine the random number.
	private void randomStudent(int number, int max) {
		Random random = new Random();
		for (int i=0; i<number; i++) {
			Integer r = random.nextInt(max);
			if (randomNumber.contains(r)) {
				i--;
			} else {
				randomNumber.add(r);
			}
		}
	}
	
	//Choose only absent student ready to upload.
	public void createUploadList() {
		for (int i=0; i<randomStudents.size(); i++) {
			if (randomStudents.get(i).status == false) {
				uploadStudents.add(randomStudents.get(i));
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
			//Show alert dialog to ensure.
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
