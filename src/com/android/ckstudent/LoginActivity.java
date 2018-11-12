package com.android.ckstudent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpPost;
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
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/*
 * This activity is to let user can do login.
 * With login process, we can retreive user data from WebApi.
 * Also, set data in apps.
 */
public class LoginActivity extends Activity {
	Button btn1;
	EditText edt1, edt2;
	String url = "http://192.168.0.104:50264/api/";
	String userId, userType, userMac;
	ConnectivityManager manager;
	NetworkInfo info;
	static BluetoothAdapter bluetoothAdapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_login);
		View background = findViewById(R.id.bg);
		background.setBackgroundResource(R.drawable.custombg);
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		manager = (ConnectivityManager)getSystemService(CONNECTIVITY_SERVICE);
		info = manager.getActiveNetworkInfo();
		btn1 = (Button)this.findViewById(R.id.button1);
		edt1 = (EditText)this.findViewById(R.id.editText1);
		edt2 = (EditText)this.findViewById(R.id.editText2);
		btn1.setBackgroundResource(R.color.common_signin_btn_dark_text_default);
		btn1.setOnClickListener(loginOnClickListener);
	}
	
	private Button.OnClickListener loginOnClickListener = new Button.OnClickListener(){
		@Override
		public void onClick(View v) {
			//Check edittext is empty or not.
			if (info == null) {
				AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
				builder.setTitle("Warning!");
				builder.setMessage("請連接網路後再繼續");
				builder.setPositiveButton("我知道了", null);
				builder.show();
			} else if (!"".equals(edt1.getEditableText().toString()) && !"".equals(edt2.getEditableText().toString())) {
				//Start login.
				loginProcess.execute();
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
				builder.setTitle("Warning!");
				builder.setMessage("請不要空白!");
				builder.setPositiveButton("我知道了", null);
				builder.show();
			}
		}
	};
	
	//Login process through WebApi using basic webauthentication.
	AsyncTask<Void, Void, Void> loginProcess = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
			mDialog = new ProgressDialog(LoginActivity.this);
	        mDialog.setMessage("Loading...");
	        mDialog.setCancelable(false);
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
				httpClient.setParams(httpParameters);
				HttpPost httpRequest = new HttpPost(url + "Membership");
				JSONArray jsonArray = new JSONArray();
				JSONObject jsonObject = new JSONObject();
				jsonObject.put("UserName", edt1.getEditableText().toString());
				jsonObject.put("Password", edt2.getEditableText().toString());
				jsonArray.put(jsonObject);
				StringEntity stringEntity = new StringEntity(jsonArray.toString(), HTTP.UTF_8);
				stringEntity.setContentType("application/json");
				httpRequest.setHeader("Content-type", "application/json");
				httpRequest.setHeader("Accept", "application/json");
				httpRequest.setEntity(stringEntity);
				//Send request to web server.
				HttpResponse httpResponse = httpClient.execute(httpRequest);
				//Process the response.
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					HttpEntity httpEntity = httpResponse.getEntity();
			        InputStream content = httpEntity.getContent();
			        BufferedReader reader = new BufferedReader(new InputStreamReader(content));
			        String line = reader.readLine().substring(1, 8);
			        userId = edt1.getEditableText().toString();
			        userType = line;
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
			//Use bundle to pass account information to MenuActivity.
			Bundle bundle = new Bundle();
			Intent intent = new Intent();
			bundle.putString("Url", url);
			bundle.putString("UserId", userId);
			bundle.putString("UserType", userType);
			intent.putExtras(bundle);
			mDialog.dismiss();
			if ("Teacher".equals(userType)) {
				startActivity(intent.setClass(LoginActivity.this, MenuActivity.class));
			} else if ("Student".equals(userType)) {
				//If user type is student then upload MAC address.
				if (bluetoothAdapter != null) {
					userMac = bluetoothAdapter.getAddress();
					putStudentMAC.execute();
				} else {
					Bundle mBundle = new Bundle();
					Intent mIntent = new Intent();
					mBundle.putString("Url", url);
					mBundle.putString("UserId", userId);
					mBundle.putString("UserType", userType);
					mIntent.putExtras(mBundle);
					startActivity(mIntent.setClass(LoginActivity.this, MenuActivityS.class));
				}
			} else {
				AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
				builder.setTitle("錯誤");
				builder.setMessage("網路錯誤或是帳號密碼錯誤");
				builder.setPositiveButton("我知道了", listener);
				builder.show();
			}
		}
	};
	
	DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			Intent intent = new Intent();
			startActivity(intent.setClass(LoginActivity.this, LoginActivity.class));
			finish();
		}
	};
	
	//Post MAC address to Web Server by WebApic.
	AsyncTask<Void, Void, Void> putStudentMAC = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
			mDialog = new ProgressDialog(LoginActivity.this);
	        mDialog.setMessage("Uploading...");
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
				httpClient.setParams(httpParameters);
				HttpPut httpRequest = new HttpPut(url + "StudentData");
				JSONArray jsonArray = new JSONArray();
				JSONObject jsonObject = new JSONObject();
				try {
					//Convert information into JSON.
					jsonObject.put("StudentNo", userId);
					jsonObject.put("MacAddress", userMac);
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
				HttpResponse httpResponse = httpClient.execute(httpRequest);
				if (httpResponse.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
					return null;
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mDialog.dismiss();
			//Use bundle to pass account information to MenuActivityS.
			Bundle bundle = new Bundle();
			Intent intent = new Intent();
			bundle.putString("Url", url);
			bundle.putString("UserId", userId);
			bundle.putString("UserType", userType);
			intent.putExtras(bundle);
			startActivity(intent.setClass(LoginActivity.this, MenuActivityS.class));
		}
	};
	@Override
	protected void onStop() {
		super.onStop();
		finish();
	}
}
