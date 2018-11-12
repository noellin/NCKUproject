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

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/*
 * This fragment is to show the activity held by associations.
 */
public class FestivalFragment extends ListFragment {
	FestivalAdapter adapter;
	String url;
	ArrayList<Festival> festivals;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		festivals = new ArrayList<Festival>();
		adapter = new FestivalAdapter(this.getActivity(), festivals);
		this.setListAdapter(adapter);
		Bundle bundle = getActivity().getIntent().getExtras();
		url = bundle.getString("Url");
		//Get association activity list.
		getFestivalData.execute();
	}

	//This asynctask is for getting activity list using WEBapi.
	AsyncTask<Void, Void, Void> getFestivalData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create dialog.
			mDialog = new ProgressDialog(getActivity());
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
				HttpGet httpRequest = new HttpGet(url + "ActivityInformation");
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
					//Convert JSON to activity list.
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
			mDialog.dismiss();
			setListAdapter(getListAdapter());
		}
	};
	
	@Override
	public void onResume() {
		super.onResume();
		festivals.clear();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		//Use bundle to pass information in order to start the activity detail page.
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("Url", url);
		bundle.putString("festivalId", festivals.get(position).id);
		bundle.putString("festivalName", festivals.get(position).name);
		bundle.putString("festivalDate", festivals.get(position).date);
		intent.putExtras(bundle);
		intent.setClass(getActivity(), FestivalContent.class);
        startActivity(intent);
	}
}
