package com.android.ckstudent;

import java.io.IOException;

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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class AssignmentNewActivity extends Activity {
	EditText editText, editText2;
	TextView textView2, textView4;
	String url, type, course, date, viewDate, selectedMonth, selectedDay;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.assign_new);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		View background = findViewById(R.id.bg);
		background.setBackgroundResource(R.drawable.custombg);
		editText = (EditText)this.findViewById(R.id.editText1);
		editText2 = (EditText)this.findViewById(R.id.editText2);
		textView4 = (TextView)this.findViewById(R.id.textView4);
		//Use bundle to get value.
		Bundle bundle = this.getIntent().getExtras();
		course = bundle.getString("Course");
		type = bundle.getString("Type");
		url = bundle.getString("Url");
		date = bundle.getString("Date");
		//Change data form.
		char[] c = date.toCharArray();
		viewDate = String.valueOf(c, 4, 2) + "/" + String.valueOf(c, 6, 2);
		textView4.setText(viewDate);
	}
	
	//Post assignment to Webapi.
	AsyncTask<Void, Void, Void> postAssignmentData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create dialog. Consider using horizontal progress bar.
			mDialog = new ProgressDialog(AssignmentNewActivity.this);
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
				HttpPost httpRequest = new HttpPost(url + "Announcement");
				JSONObject jsonObject = new JSONObject();
				JSONArray jsonArray = new JSONArray();
				
				//Convert the result into JSON.
				jsonObject.put("Name", editText2.getText().toString());
				jsonObject.put("Date", date);
				jsonObject.put("CourseNo", course);
				jsonObject.put("Type", type);
				jsonObject.put("Description", editText.getText().toString());
				jsonArray.put(jsonObject);
				
				StringEntity stringEntity = new StringEntity(jsonArray.toString(), HTTP.UTF_8);
				stringEntity.setContentType("application/json");
				httpRequest.setHeader("Content-type", "application/json");
				httpRequest.setHeader("Accept", "application/json");
				httpRequest.setEntity(stringEntity);
				//Send request to web server.
				//HttpResponse httpResponse = httpClient.execute(httpRequest);
				httpClient.execute(httpRequest);
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			//show success message.
			Toast.makeText(AssignmentNewActivity.this, "上傳成功!", Toast.LENGTH_LONG).show();
			mDialog.dismiss();
			//finish();
		}
	};
	
	//Check input can not be null.
	private boolean checkInput() {
		if ("".equals(editText.getText().toString())
				|| "".equals(editText2.getText().toString())) {
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Alert");
			builder.setMessage("不可空白");
			builder.setPositiveButton("我知道了", null);
			builder.show();
			return false;
		}
		return true;
	}

	//Do post.
	private DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			postAssignmentData.execute();
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.assignteacher, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.post:
			if (checkInput()) {
				//Show dialog.
				AlertDialog.Builder builder = new AlertDialog.Builder(this);
				builder.setTitle("Notice");
				builder.setMessage("確定上傳嗎?");
				builder.setPositiveButton("上傳", listener);
				builder.setNegativeButton("取消", null);
				builder.show();
			}
			break;
		case R.id.delete:
			//Cancel add.
			finish();
			break;
		case android.R.id.home:
        	onBackPressed();
            break;
		}
		return true;
	}
}
