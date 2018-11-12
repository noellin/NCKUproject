package com.android.ckstudent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.provider.CalendarContract.Events;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.SimpleAdapter;
import android.widget.AdapterView.OnItemClickListener;

/*
 * This activity is for teacher to show the accepted meeting, if teacher click on the list, 
 * it can also add it to google calendar or cancel it.   
 */
public class TalkTeacherActivity extends Activity {
	GridView gridView;
	String url, TeacherNo, temp;
	String GoogleEmail = "jkuiop66@gmail.com"; //  modify to the demo peoson's google account
	boolean processStatus = false;
	// accepted array
	int NumberAccepted, chooseNumber;
	int year, month, day, starthour, startminute, endyear, endmonth, endday, duration, endhour, endminute;
	String[] Name, Id, StudentNo, showDate, Date, StartTime, Duration;
	int Index, NumberProcessing;
	String[] NameRequest, IdRequest, StudentNoRequest, showDateRequest, DateRequest, StartTimeRequest;
	String[] DurationRequest, StatusRequest, StudentRequest;
	boolean[] isCheck; 
	int[] chooseItem;
	int[] dayInMonth = {31,28,31,30,31,30,31,31,30,31,30,31};
	char[] DateString, StartTimeString, DurationString;
	long mCalendarID;
    final String[] COLUMNS = new String[] {
    		CalendarContract.Events.TITLE,
        	CalendarContract.Events.DTSTART
    };
    final int[] VIEW_IDS = new int[] {
    		android.R.id.text1,
    		android.R.id.text2
    };
    final String[] PROJECTIONS = new String[] {
    		CalendarContract.Events._ID,
           	CalendarContract.Events.TITLE,
           	CalendarContract.Events.DTSTART
    };
    final String[] CALENDAR_PROJECTIONS = new String[] {
    		CalendarContract.Calendars._ID,
           	CalendarContract.Calendars.ACCOUNT_NAME,
           	CalendarContract.Calendars.ACCOUNT_TYPE 
    };
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.talkteacher_activity);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		View background = findViewById(R.id.bg);
		background.setBackgroundResource(R.drawable.custombg);
	    //get Bundle url, teacher number
	    Bundle bundle = this.getIntent().getExtras();  
	    url = bundle.getString("Url");
	    TeacherNo = bundle.getString("TeacherNo");
	    // get the meeting that have already been accepted  
		Get_DataAccepted.execute(); 
		// set a loop for waiting data loading
		while(processStatus == false){};
		processStatus = false;
		// create items and put student name, date, time, duration inside
        List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
        for (int i = 0; i < NumberAccepted; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("Name", Name[i]);
            item.put("Date", showDate[i]);
            item.put("StartTime", StartTime[i]);
            item.put("Duration", Duration[i]);
            items.add(item);
        }
        
        //create an adapter and put items inside, and put adapter into gridview
        SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.grid_item, 
        		new String[]{"Name", "Date", "StartTime", "Duration"}, new int[]{R.id.text1, R.id.text2, R.id.text3, R.id.textView4});
        
        	gridView = (GridView)findViewById(R.id.gridview);  
        	gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new OnItemClickListener(){

				@Override
				public void onItemClick(AdapterView<?>parents, View view, final int position, long id) {
					// click to show an alert dialog
					AlertDialog.Builder builder = new AlertDialog.Builder(TalkTeacherActivity.this);
					builder.setCancelable(false);
					builder.setTitle("處理導談");
					builder.setMessage("請選擇要加入Google Calendar或是取消預約");
					// a button to add the meeting event to google calendar
		    		builder.setNegativeButton("加入Google Calendar",new DialogInterface.OnClickListener(){ 
		    			@Override
		    			public void onClick(DialogInterface dialog, int which) {
		    				// capture which one to add into google calendar
		    				Index = position;
		    				// add chosen one to google calendar
		    				PutInGoogleCalendar.execute(); 		        				
		    			};	
		    		});
		    		// a button to cancel the meeting 
		    		builder.setNeutralButton("取消預約",new DialogInterface.OnClickListener(){ // back to Teacher activity
		    			@Override
		    			public void onClick(DialogInterface dialog, int which) {
		    				// capture index, so we can search it and delete
							Index = position; 
							// cancel meeting
		                	Put_Data_Cancel.execute();
		    				};	
		    			});
		    		// a button to come back to talkTeacherActivity 
		    		builder.setPositiveButton("返回",new DialogInterface.OnClickListener(){ // back to Teacher activity
		    			@Override
		    			public void onClick(DialogInterface dialog, int which) {
		    				refreshLayout();
		    				};	
		    			});
		    		// show dialog
		    		builder.show();
				}
            });
	}
	// to get the accepted meeting by webapi
	AsyncTask<Void, Void, Void> Get_DataAccepted = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		
		protected void onPreExecute() {
			super.onPreExecute();
			// Create progress dialog.
			mDialog = new ProgressDialog(TalkTeacherActivity.this);
	        mDialog.setMessage("Loading...");
	        mDialog.setCancelable(false);
	        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        mDialog.show();
		}
		@Override
		protected Void doInBackground(Void... arg0) {
			// set process status false to indicate loading data
			processStatus = false;
			try {				
				//Construct http client and http get request objects.
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpRequest = new HttpGet( url + "meeting?TeacherNo=" + TeacherNo + "&Status=Accepted");		
				//Send request to web server.
				HttpResponse httpResponse = httpClient.execute(httpRequest);	
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
					NumberAccepted = jsonArray.length();
					// Dynamically allocate for string
					Name = new String[NumberAccepted];
					Id = new String[NumberAccepted];
					StudentNo = new String[NumberAccepted];
					showDate= new String [NumberAccepted];
					Date = new String[NumberAccepted];
					StartTime = new String[NumberAccepted];
					Duration = new String[NumberAccepted];

					// Convert JSON to array list
					for (int i = 0; i < NumberAccepted; i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						//convert into string	
						Id[i] = jsonObject.getString("Id");
						Name[i] = jsonObject.getString("StudentName");
						StudentNo[i] = jsonObject.getString("StudentNo");
						Date[i] = jsonObject.getString("Date");
						StartTime[i] = jsonObject.getString("StartTime");
						Duration[i] = jsonObject.getString("Duration");
						// turn string into char
						DateString = jsonObject.getString("Date").toCharArray();
						// turn string yyyymmdd to yyyy/mm/dd 
						showDate[i] = "導談日期: " + String.valueOf(DateString, 0, 4) + "/" +String.valueOf(DateString, 4, 2) + "/"
								+ String.valueOf(DateString, 6, 2)  ; 
					}
				} 
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			// set process status true to indicate the data loading down
			processStatus = true;
			return null;
		}
		
		@Override
		protected void onPostExecute(Void result) {
			mDialog.dismiss();
			super.onPostExecute(result);
		}
		
	};

	AsyncTask<Void, Void, Void> Get_DataProcessing = new AsyncTask<Void, Void, Void>() {
	   	// get the requests sent by student to be deal with 
	   	@Override
	   	protected Void doInBackground(Void... arg0) {
	   		// set process status false to indicate loading data
	   		processStatus = false;
    		try {				
    			//Construct http client and http get request objects. 
				HttpClient httpClient = new DefaultHttpClient();
				HttpGet httpRequest = new HttpGet(url + "meeting?TeacherNo=" + TeacherNo + "&Status=Processing");
				//Send request to web server.
				HttpResponse httpResponse = httpClient.execute(httpRequest);
				int statusCode = httpResponse.getStatusLine().getStatusCode();			   
				//Process the response.
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
							
					NumberProcessing = jsonArray.length();
					// Dynamically allocate for string
					NameRequest = new String[NumberProcessing];
					IdRequest = new String[NumberProcessing];
					StatusRequest = new String[NumberProcessing];
					StudentNoRequest = new String[NumberProcessing];
					showDateRequest = new String[NumberProcessing];
					DateRequest = new String[NumberProcessing];
					StartTimeRequest = new String[NumberProcessing];
					DurationRequest = new String[NumberProcessing];
					StudentRequest= new String[NumberProcessing];
					isCheck = new boolean [NumberProcessing];
							
					// Convert JSON to array list
					for (int i = 0; i < NumberProcessing; i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						NameRequest[i] = jsonObject.getString("StudentName");
						IdRequest[i] = jsonObject.getString("Id");
						StatusRequest[i] = jsonObject.getString("Status");
						StudentNoRequest[i] = jsonObject.getString("StudentNo");
						DateRequest[i] = jsonObject.getString("Date");
						StartTimeRequest[i] = jsonObject.getString("StartTime");
						DurationRequest[i] = jsonObject.getString("Duration");				
						// turn string into char
						DateString = jsonObject.getString("Date").toCharArray(); 
						//format yyyymmdd into yyyy/mm/dd
						showDateRequest[i] = String.valueOf(DateString, 0, 4) + "/" +String.valueOf(DateString, 4, 2) + "/"
     										+ String.valueOf(DateString, 6, 2)  ; 
						// combine all data in a string 
						StudentRequest[i] = NameRequest[i] + " 時間: " + showDateRequest[i] + " " + StartTimeRequest[i]
												+ " 導談長度：" + DurationRequest[i] + "分鐘";
						// set all meeting are false, indicating that are not chosen
						isCheck[i] = false;
					}
			    }
    		} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
    		// set process status true to indicate loading data is done
			processStatus = true;
			return null;
		}
	};
	
	// to reject the student scheduled meeting for teacher 
    AsyncTask<Void, Void, Void> Put_Data_Rejected = new AsyncTask<Void, Void, Void>() {

    	@Override
	   	protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// after reject meeting, show the talkTeacherActivity 
			refreshLayout();
		}
		   	
	   	@Override
		protected Void doInBackground(Void... arg0) {
	   		try {		
	   			//Construct http client and http get request objects.
				HttpClient httpClient = new DefaultHttpClient();
				HttpPut httpPut = new HttpPut(url + "meeting/");
				JSONArray jsonArr = new JSONArray();
					
				for (int i = 0; i < chooseNumber; i++){
					JSONObject jsonObj = new JSONObject();
					// put data into JSONobject
					jsonObj.put("Id", "" + IdRequest[chooseItem[i]]);						
					jsonObj.put("TeacherNo", TeacherNo);
					jsonObj.put("Date", DateRequest[chooseItem[i]]);
					jsonObj.put("StartTime",  StartTimeRequest[chooseItem[i]]);
					jsonObj.put("Duration", DurationRequest[chooseItem[i]]);
					jsonObj.put("StudentNo", StudentNoRequest[chooseItem[i]]);							
					jsonObj.put("Status", "Rejected");
					// put JSONobject into JSONarray
					jsonArr.put(jsonObj);
				}
							
				StringEntity entity = new StringEntity(jsonArr.toString());
				httpPut.setEntity(entity);
				httpPut.setHeader("Accept", "application/json");
				httpPut.setHeader("Content-type", "application/json");		
				//Send request to web server.
				HttpResponse httpResponse = httpClient.execute(httpPut);
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
			   
	// to accept the student scheduled meeting for teacher 
	 AsyncTask<Void, Void, Void> Put_Data_Accepted = new AsyncTask<Void, Void, Void>() { 
		    	
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			// after reject meeting, show the talkTeacherActivity, also the user can see the accepted meeting shown on talkTeacherActivity
			refreshLayout();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {			
				//Construct http client and http get request objects.
				HttpClient httpClient = new DefaultHttpClient();
				HttpPut httpPut = new HttpPut(url + "meeting"); 
				JSONArray jsonArr = new JSONArray();
				for (int i = 0; i < chooseNumber; i++){
					JSONObject jsonObj = new JSONObject();
					//put data into JSONobject
					jsonObj.put("Id", IdRequest[chooseItem[i]]);							
					jsonObj.put("TeacherNo", TeacherNo);
					jsonObj.put("Date", DateRequest[chooseItem[i]]);
					jsonObj.put("StartTime", StartTimeRequest[chooseItem[i]]);
					jsonObj.put("Duration", DurationRequest[chooseItem[i]]);
					jsonObj.put("StudentNo", StudentNoRequest[chooseItem[i]]);						
					jsonObj.put("Status", "Accepted");
					// put JSONobject into JSONarray
					jsonArr.put(jsonObj);
				}
			
				StringEntity entity = new StringEntity(jsonArr.toString());
				httpPut.setEntity(entity);
				httpPut.setHeader("Accept", "application/json");
				httpPut.setHeader("Content-type", "application/json");	
				//Send request to web server.
				HttpResponse httpResponse = httpClient.execute(httpPut);
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
				    
	// to cancel the meeting that had been accepted by teacher
	AsyncTask<Void, Void, Void> Put_Data_Cancel = new AsyncTask<Void, Void, Void>() {
				
		@Override
		protected void onPostExecute(Void result) {
    		super.onPostExecute(result);
    		AlertDialog.Builder builder = new AlertDialog.Builder(TalkTeacherActivity.this);
			builder.setTitle("已取消預約");
			// show dialog
			builder.show();
			refreshLayout();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			try {		
				//Construct http client and http get request objects.
				HttpClient httpClient = new DefaultHttpClient();
				HttpPut httpPut = new HttpPut(url + "meeting");
				JSONArray jsonArr = new JSONArray();
						
				JSONObject jsonObj = new JSONObject();
				//put data into JSONobject
				jsonObj.put("Id", "" + Id[Index]);
				jsonObj.put("TeacherNo", TeacherNo);
				jsonObj.put("Date", Date[Index]);
				jsonObj.put("StartTime",  StartTime[Index]);
				jsonObj.put("Duration", Duration[Index]);
				jsonObj.put("StudentNo", StudentNo[Index]);
				jsonObj.put("Status", "Rejected");
				// put JSONobject into JSONarray
				jsonArr.put(jsonObj);
	
				StringEntity entity = new StringEntity(jsonArr.toString());
				httpPut.setEntity(entity);
				httpPut.setHeader("Accept", "application/json");
				httpPut.setHeader("Content-type", "application/json");	
				//Send request to web server.
				HttpResponse httpResponse = httpClient.execute(httpPut);
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
					
	AsyncTask<Void, Void, Void> PutInGoogleCalendar = new AsyncTask<Void, Void, Void>() { // put in google calendar

		@Override
    	protected void onPostExecute(Void result) {
			super.onPostExecute(result);
		 	// turn all string into char 
			DateString = Date[Index].toCharArray(); 
			StartTimeString = StartTime[Index].toCharArray(); 
			DurationString = Duration[Index].toCharArray();
			// get year from char and turn it into integer
			temp = String.valueOf(DateString, 0, 4); 
		    year = Integer.valueOf(temp);
		    
		    // get month from char, format it and turn into integer
		    if (String.valueOf(DateString, 4, 1) == "0")
		    	temp = String.valueOf(DateString, 5, 1); 
		    else
		    	temp = String.valueOf(DateString, 4, 2); 
		    month = Integer.valueOf(temp);
		    		
		    // get day from char, format it and turn into integer
		    if (String.valueOf(DateString, 6, 1) == "0")
		    	temp = String.valueOf(DateString, 7, 1); 
		    else
		    	temp = String.valueOf(DateString, 6, 2); 
		   	day = Integer.valueOf(temp);
		   	// get hour from char, format it and turn into integer
		    if (String.valueOf(StartTimeString, 0, 1) == "0" )
		  		temp = String.valueOf(StartTimeString, 1, 1); 
			else
				temp = String.valueOf(StartTimeString, 0, 2); 
		   	starthour = Integer.valueOf(temp);
		   	// get minute from char, format it and turn into integer
		    if (String.valueOf(StartTimeString, 3, 1) == "0" )
		    	temp = String.valueOf(StartTimeString, 4, 1); 
		   	else
				temp = String.valueOf(StartTimeString, 3, 2); 
		    startminute = Integer.valueOf(temp);
		    // get duration from char, and turn it into integer 
			temp = String.valueOf(DurationString, 0, DurationString.length ); 
			duration = Integer.valueOf(temp);
			
			// set the meeting end time
		 	endyear = year;
	   		endmonth = month;
	   		endday = day;
			endhour = starthour;    
			endminute = startminute + duration;
			
			// calculate end minute
		 	while (endminute > 60){
		    	endminute = endminute - 60;
		   		endhour = endhour + 1;
	 		}
		 	
		 	 // if meeting last to the next day		
		 	if (endhour >= 24){     
				endhour = endhour - 24;
				endday = endday +1;
		   	}
		    // check whether it has 29 day in February		
		    if (year % 4 == 0){      
				dayInMonth[1] = 29;
			}
		    // check the meeting last to the next month
		    if (endday > dayInMonth[month - 1] ){ 
		    	endday = endday - dayInMonth[month - 1];
		   		endmonth = endmonth + 1;
	 		}
		    // check the meeting last to the next year
		    if (endmonth > 12){  
		    	endmonth = endmonth - 12;
				endyear = endyear + 1;
			}

			Calendar beginTime = Calendar.getInstance();
			beginTime.set(year, month-1, day, starthour, startminute);
			Calendar endTime = Calendar.getInstance();
			endTime.set(endyear, endmonth-1, endday, endhour, endminute);

			ContentResolver contentResolver = getContentResolver();
		    ContentValues values = new ContentValues();
		    // set content
		    values.put(Events.CALENDAR_ID, mCalendarID); 							
		    values.put(Events.TITLE, "NCKU Student Meeting");
		    values.put(Events.AVAILABILITY, Events.AVAILABILITY_BUSY);
		    values.put(Events.EVENT_LOCATION, "Office");
		    values.put(Events.DESCRIPTION, "A meeting with student" + Name[Index]);
		    values.put(Events.DTSTART, beginTime.getTimeInMillis());
			values.put(Events.DTEND, endTime.getTimeInMillis());
		    values.put(Events.EVENT_TIMEZONE, "Asia/Taipei");			// Time zone must be set
		    contentResolver.insert(Events.CONTENT_URI, values);
				    
		    // show an alert dialog to hint the meeting that has been added to calendar 
		 	AlertDialog.Builder builder = new AlertDialog.Builder(TalkTeacherActivity.this);
			builder.setTitle("已加入至Googoe Calendar");
			builder.show();  
			refreshLayout();
		}

		@Override
		protected Void doInBackground(Void... arg0) {
			Cursor cur = null;
			ContentResolver cr = getContentResolver();
			Uri uri = CalendarContract.Calendars.CONTENT_URI;   
			String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND (" 
			                        + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?))";
			String[] selectionArgs = new String[] {GoogleEmail, "com.google"}; 	// Replace with your own email address
			// Submit the query and get a Cursor object back. 
			cur = cr.query(uri, CALENDAR_PROJECTIONS, selection, selectionArgs, null);
					
			mCalendarID = 1;
			while (cur.moveToNext()) {		      
				// Get the field values
			    mCalendarID = cur.getLong(0);
			}
			return null;
		}
	}; 
	
	// to set what and how much meetings to be deal with and set data in array
	public void setRequest(){ 
		chooseNumber = 0;
		// get number of choose
		for(int i = 0; i < NumberProcessing; i++){  
			if ( isCheck[i] == true)
				chooseNumber++ ;	
		}
		// Dynamically allocate for string
		chooseItem = new int [chooseNumber];
		int index = 0;
		// set "pointer", so that can be index for convenience
		for(int i = 0; i < NumberProcessing; i++){  
			if ( StatusRequest[i] == null){
				chooseItem[index] = i;
				index = index + 1 ;	
				}
			}
	}
	
	// on click 導談處理  button
	@Override
	public boolean onOptionsItemSelected(MenuItem item) { 
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.action_settings:
			// to get data by webApi
			Get_DataProcessing.execute();
			//set a loop for waiting data loading
			while(processStatus == false){} 
			AlertDialog.Builder builder = new AlertDialog.Builder(TalkTeacherActivity.this);
			builder.setCancelable(false);
			builder.setTitle("選擇學生進行操作");
	      
			// set a multiple choose item list
			builder.setMultiChoiceItems(StudentRequest, isCheck, new DialogInterface.OnMultiChoiceClickListener(){
				// click list, set it " chosen" by assign it a special value 
				public void onClick(DialogInterface dialog, int inx, boolean isChked) {		 
					if (isChked) {
						// must be deal with so set "null"
						StatusRequest[inx] = null; 
					}
					else 
						StatusRequest[inx] = "Processing";
			    	} 
			    });
			// set a button to schedule the chosen meeting        
			builder.setNegativeButton("接受", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
			    	// set a dialog to double check	  
					AlertDialog.Builder builder1 = new AlertDialog.Builder(TalkTeacherActivity.this);
					builder1.setCancelable(false);
					builder1.setTitle("確認接受要求");	
					builder1.setMessage("請按確認繼續");
					// click sure to send
					builder1.setNegativeButton("確認",new DialogInterface.OnClickListener(){ // on click, to accept meeting 
						@Override
			        	public void onClick(DialogInterface dialog, int which) {
							// to set the chosen requests 
			        		setRequest();
			        		// accept the meeting by webApi
			        		Put_Data_Accepted.execute();         				
			        	};	
			        });
			        // set cancel button to go back		
			        builder1.setPositiveButton("取消",new DialogInterface.OnClickListener(){ // back to Teacher activity
			        	@Override
			       		public void onClick(DialogInterface dialog, int which) {
			   				refreshLayout();		
	      				};	
	      			});
			        // show dialog
			        builder1.show(); 
			    };	
			    
			});
			// set a button to reject the chosen meetings     
			builder.setNeutralButton("拒絕", new DialogInterface.OnClickListener(){
				@Override
				public void onClick(DialogInterface dialog, int which) {
					// a dialog to double check whether to reject meeting
					AlertDialog.Builder builder2 = new AlertDialog.Builder(TalkTeacherActivity.this);
					builder2.setCancelable(false);
					builder2.setTitle("確認拒絕要求");
					builder2.setMessage("請按確認繼續");
					// click sure to reject
					builder2.setNegativeButton("確認",new DialogInterface.OnClickListener(){ // on click, reject meeting
						@Override
						public void onClick(DialogInterface dialog, int which) {
							// to set the chosen requests 
							setRequest(); 
			    			// to reject meeting by webApi
							Put_Data_Rejected.execute(); 
						};	
					});
					// set cancel button to go back
					builder2.setPositiveButton("取消",new DialogInterface.OnClickListener(){ // back to Teacher activity
						@Override
						public void onClick(DialogInterface dialog, int which) {
							refreshLayout();	        				
						};	
					});
					// show dialog
					builder2.show(); 
				};	
			});
			      
			// set cancel button to go back
			builder.setPositiveButton("取消", new DialogInterface.OnClickListener(){ // back to Teacher activity
				@Override
				public void onClick(DialogInterface dialog, int which){
					refreshLayout();
				};
			});
			builder.show();
			break;
		case android.R.id.home:
        	onBackPressed();
            break;
		}
		return true;
	}
	// refresh itself
	public void refreshLayout(){
		Intent intent = new Intent();
	    Bundle bundle = new Bundle();
	    bundle.putString("Url", url);
	    bundle.putString("TeacherNo", TeacherNo);
	    intent.putExtras(bundle);
		intent.setClass(TalkTeacherActivity.this, TalkTeacherActivity.class); 
		TalkTeacherActivity.this.startActivity(intent);
		TalkTeacherActivity.this.finish();	
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.talkteacher, menu);
		return true;
	}
}