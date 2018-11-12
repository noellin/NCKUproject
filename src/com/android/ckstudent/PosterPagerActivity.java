package com.android.ckstudent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

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
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;

/*
 * This activity is to show all the festival posters.
 */
public class PosterPagerActivity extends Activity {
	String url;
	ViewPager mPager;
	JSONArray jsonArray, picArray;
	ScreenSlidePagerAdapter mPagerAdapter;
	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.screen_slide);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        // Instantiate a ViewPager and a PagerAdapter.
        mPager = (ViewPager)findViewById(R.id.pager);
        Bundle bundle = getIntent().getExtras();
        url = bundle.getString("Url");
        jsonArray = new JSONArray();
        picArray = new JSONArray();
        getPosterData.execute();
    }
	@Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);
        getMenuInflater().inflate(R.menu.poster, menu);
		return true;
    }
	AsyncTask<Void, Void, Void> getPosterData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
			mDialog = new ProgressDialog(PosterPagerActivity.this);
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
				HttpGet httpRequest = new HttpGet(url + "ActivityInformation/");
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
					jsonArray =  new JSONArray(builder.toString());
					for (int i=0; i<jsonArray.length(); i++) {
						JSONObject jsonObject = jsonArray.getJSONObject(i);
						if ("null".equals(jsonObject.getString("Picture"))) {
						} else {
							picArray.put(jsonObject);
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
			mPagerAdapter = new ScreenSlidePagerAdapter(PosterPagerActivity.this, picArray);
	        mPager.setAdapter(mPagerAdapter);
		}
	};
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.viewlist:
        	finish();
            break;
        case android.R.id.home:
        	onBackPressed();
            break;
        }
		return true;
	}
}
