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
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

/*
 * This activity is to let teacher use bluetooth to check students' presentation.
 */
public class CallBTActivity extends ListActivity {
	StudentAdapter adapter;
	String courseNo;
	String url;
	ArrayList<Student> students;
	ArrayList<Student> uploadStudents;
	ArrayList<BTDevice> bluetoothDevices;
	static BluetoothAdapter bluetoothAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		//Add intent filter to catch action.
		IntentFilter filter = new IntentFilter();
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
		//Regist bluetooth receiver.
		registerReceiver(mReceiver, filter);
		students = new ArrayList<Student>();
		uploadStudents = new ArrayList<Student>();
		bluetoothDevices = new ArrayList<BTDevice>();
		//Get bundle passed from SelectListActivity.
        Bundle course = CallBTActivity.this.getIntent().getExtras();
        courseNo = course.getString("courseNo");
        url = course.getString("Url");
        adapter = new StudentAdapter(this, students, courseNo);
        this.setListAdapter(adapter);
        getListView().setBackgroundResource(R.drawable.custombg);
		//Get bluetooth instance.
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		//Check bluetooth is supported or not.
		if (!bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.enable();
		}
		//If discovery already start, cancel it.
		if (bluetoothAdapter.isDiscovering()) {
		    bluetoothAdapter.cancelDiscovery();
		}
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Notice");
		builder.setMessage("點名開始，請保持藍牙開啟");
		builder.setPositiveButton("我知道了", null);
		builder.show();
		//Get student list.
		getStudentData.execute();
	}
	//Set up a broadcastreceiver to listen to bluetooth signal.
	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			if (BluetoothDevice.ACTION_FOUND.equals(intent.getAction())) {
				BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
				BTDevice bluetoothDevice = new BTDevice();
				bluetoothDevice.macAddress = device.getAddress();
				//Check in bluetoothDevices duplicate or not.
				if (!checkDuplicate(bluetoothDevice.macAddress)) {
					//Add into bluetooth devices list.
					bluetoothDevices.add(bluetoothDevice);
					//Match to the student list.
					matchList(bluetoothDevice.macAddress);
				} 
			}
			else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(intent.getAction())) {
				bluetoothAdapter.startDiscovery();
			}
		}
	};
	
	//Get student list by using webapi.
	AsyncTask<Void, Void, Void> getStudentData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
			mDialog = new ProgressDialog(CallBTActivity.this);
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
					//Convert JSON to student list.
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Student student = new Student();
						student.no = jsonObject.getString("StudentNo");
						students.add(student);
					}
			    }
			    
			    httpRequest = new HttpGet(url + "StudentData");
				//Send request to web server.
			    httpResponse = httpClient.execute(httpRequest);
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
						student.mac = jsonObject.getString("MacAddress");
						for (int j=0; j<students.size(); j++) {
							if (student.no.equals(students.get(j).no)) {
								students.get(j).name = jsonObject.getString("Name");
								students.get(j).mac = jsonObject.getString("MacAddress");
							}
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
			setListAdapter(getListAdapter());
			//Start bluetooth discovery.
			bluetoothAdapter.startDiscovery();
		}
	};
	
	//Upload the presentation result by WebApi.
	AsyncTask<Void, Void, Void> postStudentData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create dialog. Consider using horizontal progress bar.
			mDialog = new ProgressDialog(CallBTActivity.this);
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
			Toast.makeText(CallBTActivity.this, "上傳成功!", Toast.LENGTH_LONG).show();
			//End the activity.
			finish();
		}
	};
	
	//Check duplicate or not.
	private boolean checkDuplicate(String macAddress) {
		for (int i=0; i<bluetoothDevices.size(); i++) {
			if (macAddress.equals(bluetoothDevices.get(i).macAddress)) {
				return true;
			}
		}
		return false;
	}
	
	//Match the receive device to student.
	private void matchList(String macAddress) {
		for (int i=0; i<students.size(); i++) {
			if (macAddress.equals(students.get(i).mac)) {
				students.get(i).status = true;
				Toast.makeText(this, "Received!", Toast.LENGTH_LONG).show();
				this.setListAdapter(getListAdapter());
			}
		}
	}
	
	//Choose only absent student ready to upload.
	private void createUploadList() {
		for (int i=0; i<students.size(); i++) {
			if (students.get(i).status == false) {
				uploadStudents.add(students.get(i));
			}
		}
	}
	
	private DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//Close bluetooth discovery and prepare list to upload.
			bluetoothAdapter.cancelDiscovery();
			createUploadList();
			postStudentData.execute();
		}
	};
	
	@Override
	protected void onPause() {
		super.onPause();
		bluetoothAdapter.cancelDiscovery();
		unregisterReceiver(mReceiver);
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.callbt, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		//Click the upload button on action bar.
		case R.id.uploadend:
			//Show alert to ensure.
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Notice");
			builder.setMessage("確定上傳嗎?該節課一天僅能上傳一次!");
			builder.setPositiveButton("上傳", listener);
			builder.setNegativeButton("取消", null);
			builder.show();
			break;
		case R.id.status:
			//check bluetooth discovery status.
			if (bluetoothAdapter.isDiscovering()) {
				Toast.makeText(CallBTActivity.this, "點名中..", Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(CallBTActivity.this, "已停止..", Toast.LENGTH_SHORT).show();
			}
			break;
		case android.R.id.home:
        	onBackPressed();
            break;
		}
		return true;
	}
}