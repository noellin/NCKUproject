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

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import android.widget.TextView;

/*
 * This activity is to show association details included their name, place, time and activities.
 */
public class ClubContent extends Activity {
	ListView listView;
	TextView textView1, textView2, textView3;
	FestivalAdapter adapter;
	ArrayList<Festival> festivals;
	int clubId;
	String url;
	String clubName;
	String picURL;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.club_view);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		listView = (ListView)findViewById(R.id.recentList);
		textView1 = (TextView)findViewById(R.id.textView5);
		textView2 = (TextView)findViewById(R.id.textView6);
		textView3 = (TextView)findViewById(R.id.textView7);
        festivals = new ArrayList<Festival>();
		adapter = new FestivalAdapter(this, festivals);
		listView.setAdapter(adapter);
		listView.setOnItemClickListener(listener);
		View background = findViewById(R.id.bg);
		background.setBackgroundResource(R.drawable.custombg);
		//Get information bundle from AssociationFragment.
		Bundle bundle = this.getIntent().getExtras();
        clubName = bundle.getString("clubName");
        clubId = bundle.getInt("clubId");
        url = bundle.getString("Url");
        //Use association name to get data.
        getClub.execute();
        //Get recent activities they held.
        getClubRecent.execute();
	}

	//Get data by WebApi.
	AsyncTask<Void, Void, Club> getClub = new AsyncTask<Void, Void, Club>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create dialog.
			mDialog = new ProgressDialog(ClubContent.this);
	        mDialog.setMessage("Loading...");
	        //mDialog.setCancelable(false);
	        mDialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
	        mDialog.show();
		}
		@Override
		protected Club doInBackground(Void... arg0) {
			try {
				//Set connection parameters.
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
				HttpConnectionParams.setSoTimeout(httpParameters, 5000);
				//Construct http client and http get request objects.
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpRequest = new HttpGet(url + "ClubInformation/" + clubId);
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
			        JSONArray jsonArray = new JSONArray(builder.toString());
			        //Convert into JSONObject.
					JSONObject jsonObject = jsonArray.getJSONObject(0);
					//Convert JSON to association details.
					Club club = new Club();
					club.name = jsonObject.getString("Name");
					club.place = jsonObject.getString("Place");
					club.time = jsonObject.getString("Date");
					return club;
			    }
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			return null;
		}
		@Override
		protected void onPostExecute(Club club) {
			super.onPostExecute(club);
			//Set the textview.
			textView1.setText(club.name);
			textView2.setText(club.place);
			textView3.setText(club.time);
			mDialog.dismiss();
		}
	};
	
	//Get the recent activity by WebApi.
	AsyncTask<Void, Void, Void> getClubRecent = new AsyncTask<Void, Void, Void>() {
		@Override
		protected Void doInBackground(Void... arg0) {
			try {
				//Set connection parameters.
				HttpParams httpParameters = new BasicHttpParams();
				HttpConnectionParams.setConnectionTimeout(httpParameters, 5000);
				HttpConnectionParams.setSoTimeout(httpParameters, 5000);
				//Construct http client and http get request objects.
				DefaultHttpClient httpClient = new DefaultHttpClient();
				HttpGet httpRequest = new HttpGet(url + "ActivityInformation?clubname=" + clubName);
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
			        //Turn into JSONObject.
			        JSONArray jsonArray =  new JSONArray(builder.toString());
			        //Convert JSON to recent activity list.
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Festival festival = new Festival();
						festival.id = jsonObject.getString("Id");
						festival.name = jsonObject.getString("Name");
						festival.date = jsonObject.getString("Date");
						festivals.add(festival);
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
			adapter.notifyDataSetChanged();
		}
	};
	
	OnItemClickListener listener = new OnItemClickListener() {
		@Override
		public void onItemClick(AdapterView<?> adapter, View v, int position, long id) {
			Intent intent = new Intent();
			Bundle bundle = new Bundle();
			bundle.putString("Url", url);
			bundle.putString("festivalId", festivals.get(position).id);
			bundle.putString("festivalName", festivals.get(position).name);
			bundle.putString("festivalDate", festivals.get(position).date);
			intent.putExtras(bundle);
			intent.setClass(ClubContent.this, FestivalContent.class);
	        startActivity(intent);
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
