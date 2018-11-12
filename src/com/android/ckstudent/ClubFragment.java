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
 * This fragment is to show all the associations.
 */
public class ClubFragment extends ListFragment {
	ClubAdapter adapter;
	String url;
	ArrayList<Club> clubs;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		clubs = new ArrayList<Club>();
		adapter = new ClubAdapter(this.getActivity(), clubs);
		this.setListAdapter(adapter);
		Bundle bundle = getActivity().getIntent().getExtras();
		url = bundle.getString("Url");
		//Get associations data.
		getClubData.execute();
	}

	AsyncTask<Void, Void, Void> getClubData = new AsyncTask<Void, Void, Void>() {
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
				HttpGet httpRequest = new HttpGet(url + "ClubInformation");
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
			        //Turn into JSONArray.
					JSONArray jsonArray =  new JSONArray(builder.toString());
					//Convert JSON to association list.
					for (int i = 0; i < jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						Club club = new Club();
						club.id = jsonObject.getInt("Id");
						club.name = jsonObject.getString("Name");
						clubs.add(club);
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
			setListAdapter(getListAdapter());
			mDialog.dismiss();
		}
	};
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		//Use bundle to pass information in order to start the association detail page.
		Intent intent = new Intent();
		Bundle bundle = new Bundle();
		bundle.putString("Url", url);
		bundle.putInt("clubId", clubs.get(position).id);
		bundle.putString("clubName", clubs.get(position).name);
		intent.putExtras(bundle);
		intent.setClass(getActivity(), ClubContent.class);
        startActivity(intent);
	}
}
