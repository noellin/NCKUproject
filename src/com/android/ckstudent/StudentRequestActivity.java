package com.android.ckstudent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.GridView;
import android.widget.SimpleAdapter;

public class StudentRequestActivity extends Activity {
	String url, StudentNo, temp;
	boolean processStatus = false;
	int DataNumber, year, month, day;
	String[] Date, showDate, StartTime, Duration, Status;
	char[] DateString;
	GridView gridView;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.studentrequest_activity);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		View background = findViewById(R.id.bg);
		background.setBackgroundResource(R.drawable.custombg);
	    //get Bundle url and student number
	    Bundle bundle = this.getIntent().getExtras(); 
	    url = bundle.getString("Url");
	    StudentNo = bundle.getString("StudentNo");
	    // get all the student's requests
		Get_Request.execute();
		// set a loop for waiting data loading
		while(processStatus == false){};
		// create items and put status, date, time, duration inside
        List<Map<String, Object>> items = new ArrayList<Map<String,Object>>();
        for (int i = 0; i < DataNumber; i++) {
            Map<String, Object> item = new HashMap<String, Object>();
            item.put("Status", Status[i]);
            item.put("Date", showDate[i]);
            item.put("Time", StartTime[i]);
            item.put("Duration", Duration[i]);
            items.add(item);
        }
        
        //create an adapter and put items inside, and put adapter into gridview
        SimpleAdapter adapter = new SimpleAdapter(this, items, R.layout.grid_item, new String[]{"Status","Date","Time","Duration"},
        		new int[]{R.id.text1, R.id.text2, R.id.text3, R.id.textView4});
        gridView = (GridView)findViewById(R.id.gridview);
        gridView.setAdapter(adapter);
	}
	// get all the student's requests by webapi
	AsyncTask<Void, Void, Void> Get_Request = new AsyncTask<Void, Void, Void>() {
			@Override
			protected Void doInBackground(Void... arg0) {
				// set process status false to indicate loading data
				processStatus = false; 
				try {				
					//Construct http client and http get request objects.
					HttpClient httpClient = new DefaultHttpClient();
					HttpGet httpRequest = new HttpGet(url + "meeting?StudentNo=" + StudentNo );
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
				        // convert into JSONarray
						JSONArray jsonArray =  new JSONArray(builder.toString());
						DataNumber = jsonArray.length();
						// Dynamically allocate for string
						showDate = new String[DataNumber];
						Date = new String[DataNumber];
						StartTime = new String[DataNumber];
						Duration = new String[DataNumber];
						Status = new String[DataNumber];
						// Convert JSON to array list
						for (int i = 0; i < DataNumber; i++) {
							JSONObject jsonObject = jsonArray.getJSONObject(i);
							//convert into string
							Date[i] = "導談日期:" + jsonObject.getString("Date");
							StartTime[i] = "導談時間:" + jsonObject.getString("StartTime");
							Duration[i] = jsonObject.getString("Duration");
							Status[i] = jsonObject.getString("Status");
							// format a string yyyymmdd to be split and format yyyy/mm/dd
							DateString = jsonObject.getString("Date").toCharArray();
							showDate[i] = String.valueOf(DateString, 0, 4) + "/" +String.valueOf(DateString, 4, 2) + "/"
										+ String.valueOf(DateString, 6, 2);
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