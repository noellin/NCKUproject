package com.android.ckstudent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Calendar;
import java.util.Locale;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnFocusChangeListener;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TimePicker;

/*
 * this activity is for student to schedule a meeting request with teacher
 */
public class TalkStudentActivity extends Activity {
	Calendar _calendar;
	int year, month, day, clickyear, clickmonth, clickday, clickhour, clickminute;
	String url, StudentNo, Date, TeacherNo;
	String StartTime;
	String Duration = null;  // set null, so will check whether typing
	EditText edit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.talkstudent_activity);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		View background = findViewById(R.id.bg);
		background.setBackgroundResource(R.drawable.custombg);
	    //get Bundle url and student number
	    Bundle bundle = getIntent().getExtras();
	    url = bundle.getString("Url");
	    StudentNo = bundle.getString("StudentNo");
	    // set a calendar and get current year, month, date
		_calendar = Calendar.getInstance(Locale.getDefault());
		year = _calendar.get(Calendar.YEAR);
		month = _calendar.get(Calendar.MONTH);
		day = _calendar.get(Calendar.DATE);
		clickhour = _calendar.get(Calendar.HOUR_OF_DAY);
		clickminute = _calendar.get(Calendar.MINUTE);
		// initialize the day, month, year
		clickyear = year;
		clickmonth = month;
		clickday = day;

		// format the form of Date to be yyyymmdd (YearMonthDay)
		if (day < 10 && month < 9)
			Date =  year + "0" + (month + 1) + "0" + day;
		else if (day < 10)
			Date = year + "" + (month + 1) + "0" + day;
		else if (month < 9)
			Date = year + "0" + (month + 1) +"" + day;
		else
			Date =  year + "" + (month + 1) + "" + day;

		// format the form of Date to be yyyymmdd (YearMonthDay)

		if (clickhour < 10 && clickminute < 10)
			StartTime = "0" + clickhour + ":0" + clickminute;
		else if ( clickminute < 10)
			StartTime = clickhour + ":0" + clickminute;
		else if (clickhour < 10)
			StartTime = "0" + clickhour + ":" + clickminute;
		else
			StartTime = clickhour + ":" + clickminute;

		CalendarView calendarchoose = (CalendarView)findViewById(R.id.calendarView1);

		final EditText text = (EditText)findViewById(R.id.editText2);
		text.setClickable(true);
		text.setKeyListener(null);
		text.setOnClickListener(new OnClickListener(){
			//set on click
			@Override
			public void onClick(View v) {
				// click it to show an alert dialog, contains timepicker
				AlertDialog.Builder showTimePicker = new AlertDialog.Builder(TalkStudentActivity.this);
				View view = getLayoutInflater().inflate(R.layout.timepicker, null);
				// show a time picker
				showTimePicker.setView(view);
				showTimePicker.setTitle("選擇時間");
				// set the time in the text view for user to see
				text.setText(StartTime);

				final TimePicker timePicker = (TimePicker)view.findViewById(R.id.timePicker1);
			    timePicker.setIs24HourView(new Boolean(true));
			   	showTimePicker.setPositiveButton("確定", new DialogInterface.OnClickListener() {
			   	// click sure to set start time
					public void onClick(DialogInterface dialog, int whichButton) {
                        // set new chosen time in the text view
						text.setText(StartTime);
	   				}
				});

				timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {  // choose time picker
					public void onTimeChanged(TimePicker timePicker, int hh, int mn) {
						clickhour = hh;
						clickminute = mn;
						//format the form of StartTime
						if (clickhour < 10 && clickminute < 10)
				   			 StartTime = "0" + clickhour + ":0" + clickminute;
				   		  else if (clickminute < 10)
				   			 StartTime = clickhour + ":0" + clickminute;
				   		  else if (clickhour < 10)
				   			  StartTime = "0" + clickhour + ":" + clickminute;
				   		  else
				   			  StartTime = clickhour + ":" + clickminute;
						}
					 });

			   	showTimePicker.setNegativeButton("取消", null);
			   	showTimePicker.show();
			}

		});

		text.setOnFocusChangeListener(new OnFocusChangeListener(){
			// set on focus
			public void onFocusChange(View v, boolean hasFocus) {
				if (hasFocus){

					AlertDialog.Builder showTimePicker = new AlertDialog.Builder(TalkStudentActivity.this);
					View view = getLayoutInflater().inflate(R.layout.timepicker, null);
					showTimePicker.setView(view);
					showTimePicker.setTitle("選擇時間");
					text.setText(StartTime);
					final TimePicker timePicker = (TimePicker)view.findViewById(R.id.timePicker1);
					// set 24 hour
					timePicker.setIs24HourView(new Boolean(true));

					showTimePicker.setPositiveButton("確定", new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							text.setText(StartTime);
						}
					});

					timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {  // choose time picker
						public void onTimeChanged(TimePicker timePicker, int hh, int mn) {
							clickhour = hh;
							clickminute = mn;
							//format the form of StartTime
							if (clickhour < 10 && clickminute < 10)
								StartTime = "0" + clickhour + ":0" + clickminute;
							else if (clickminute < 10)
								StartTime = clickhour + ":0" + clickminute;
							else if (clickhour < 10)
								StartTime = "0" + clickhour + ":" + clickminute;
							else
								StartTime = clickhour + ":" + clickminute;
							}
						});

					showTimePicker.setNegativeButton("取消", null);
					showTimePicker.show();
				}
			}
		});

		//Let user use calendar to change date.
		calendarchoose.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {

			@Override
			public void onSelectedDayChange(CalendarView view, int chooseyear, int choosemonth, int dayOfMonth) {
	    		clickyear = chooseyear;
	   		  	clickmonth = ( choosemonth + 1 );
	   		  	clickday = dayOfMonth;
	   		  	// format the form of Date again because the user changed the Date
                if (clickday < 10 && clickmonth < 10)
	   		  		Date = clickyear + "0" + clickmonth + "0" + clickday;
	   		  	else if (clickday < 10)
	   		  		Date = clickyear + "" + clickmonth + "0" + clickday;
	   		  	else if (clickmonth < 10)
	   		  		Date = clickyear + "0" + clickmonth + "" + clickday;
	   		  	else
	   		  		Date = clickyear + "" + clickmonth + "" + clickday;
			}
		});

	     edit = (EditText)this.findViewById(R.id.editText1);
	     // an edit text to catch what user input the Duration
	     edit.setOnKeyListener(new View.OnKeyListener() {
	    	 public boolean onKey(View arg0, int arg1, KeyEvent arg2) {
	    		 if ( edit.getText() != null) {
	    			// if user input, catch the Duration
	    			Duration = edit.getText().toString();
	    			edit.setText(Duration);
	    		 }
	    		 return false;
	    	 }
	     });
	}

	/*
	 * there will be a serial action, because the student need to send a request in a short time
	 * first, get the student's teacher number
	 * second send a request
  	 */
	// get the student's teacher number by webApi
	AsyncTask<Void, Void, Void> Get_TeacherNo = new AsyncTask<Void, Void, Void>() {
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// if finish get teacher number, send a request by webApi
			Post_Data.execute();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				//Construct http client and http get request objects.
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpRequest = new HttpGet(url + "StudentData?StudentNo=" + StudentNo);
				//Send request to web server.
				HttpResponse httpResponse = httpClient.execute(httpRequest);
				//Process the response.
				int statusCode = httpResponse.getStatusLine().getStatusCode();
			    if (statusCode == 200) {
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
					//Convert JSON to teacher number
					for (int i=0; i<jsonArray.length();i++){
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						TeacherNo = jsonObject.getString("TeacherNo");
					}
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
    };

	// to send a request to teacher by webApi
    AsyncTask<Void, Void, Void> Post_Data = new AsyncTask<Void, Void, Void>() {

    	@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			AlertDialog.Builder builder = new AlertDialog.Builder(TalkStudentActivity.this);
			builder.setTitle("已送出");
			// show dialog to tell user the data was sent
			builder.show();
			// the refresh itself
			refreshLayout();
		}

    	@Override
		protected Void doInBackground(Void... arg0) {
			try {
				//Construct http client and http get request objects.
				HttpClient httpClient = new DefaultHttpClient();
				HttpPost httpPost = new HttpPost(url + "meeting");
				JSONArray jsonArr = new JSONArray();
				JSONObject jsonObj = new JSONObject();
				// put all data into JSONobject
				jsonObj.put("TeacherNo", TeacherNo);
				jsonObj.put("Date", Date);
				jsonObj.put("StartTime",  StartTime);
				jsonObj.put("Duration", Duration);
				jsonObj.put("StudentNo", StudentNo);
				jsonObj.put("Status", "Processing");
				// put JSONobject into JSONarray
				jsonArr.put(jsonObj);

				StringEntity entity = new StringEntity(jsonArr.toString());
				httpPost.setEntity(entity);
				httpPost.setHeader("Accept", "application/json");
				httpPost.setHeader("Content-type", "application/json");
				HttpResponse httpResponse = httpClient.execute(httpPost);
				if (httpResponse.getStatusLine().getStatusCode() == 200) {

				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
    };

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.talkstudent, menu);
		return true;
	}

	// a function to refresh itself
	public void refreshLayout(){
		Intent intent = new Intent();
	    Bundle bundle = new Bundle();
	    bundle.putString("Url", url);
	    bundle.putString("StudentNo", StudentNo);
	    intent.putExtras(bundle);
		intent.setClass(TalkStudentActivity.this, TalkStudentActivity.class);
		TalkStudentActivity.this.startActivity(intent);
		TalkStudentActivity.this.finish();
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		// click to the student request activity
		case R.id.action_settings:
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
		    bundle.putString("Url", url);
		    bundle.putString("StudentNo", StudentNo);
		    intent.putExtras(bundle);
        	intent.setClass(TalkStudentActivity.this, StudentRequestActivity.class);
        	TalkStudentActivity.this.startActivity(intent);
        	break;
        // a button click to send the meeting request
		case R.id.action_send:
			// show an alert dialog to show message
			AlertDialog.Builder builder = new AlertDialog.Builder(TalkStudentActivity.this);
			// if user didn't input Duration, set 10 minute
			if (Duration == null){
				builder.setTitle("注意");
				Duration = "10";
				builder.setMessage("你尚未填入導談時間長度，系統幫你預設10分鐘，如果要更改，請按取消。");
			}
			// if user input duration
			else {
				builder.setTitle("即將要求送出");
				builder.setMessage("請按確認繼續");
			}
			// set a Sure button, click it to get the student's teacher number and send a request by webApi
			builder.setNegativeButton("確認",new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// to get teacher number
					Get_TeacherNo.execute();
   				};
			});
			// cancel button
			builder.setPositiveButton("取消",new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					refreshLayout();
				};
			});
			builder.show(); // show dialog
			break;
		case android.R.id.home:
        	onBackPressed();
            break;
		}
		return true;
	}
}
