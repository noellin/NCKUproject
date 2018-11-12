package com.android.ckstudent;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;

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

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

/*
 * This fragment is to show students' presentation.
 * Also, teacher can click student to add in a attention list.
 */
public class PresentationFragment extends ListFragment {
	String url;
	RateAdapter adapter;
	String courseNo;
	ArrayList<StudentRate> students;
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return super.onCreateView(inflater, container, savedInstanceState);
	}
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		students = new ArrayList<StudentRate>();
		//Use bundle to get value.
		Bundle course = getActivity().getIntent().getExtras();
		url = course.getString("Url");
		courseNo = course.getString("courseNo");
		adapter = new RateAdapter(this.getActivity(), students, courseNo);
		this.setListAdapter(adapter);
		//Get student data.
		getStudentData.execute();
	}
	
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final int p = position;
		//Show dialog to add student in attention list.
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("關注此學生");
		builder.setMessage("加入觀察列表?");
		builder.setNegativeButton("取消", null);
		builder.setPositiveButton("加入", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				JSONArray jsonArray = null;
				JSONObject jsonObject= new JSONObject();
				//Check file exists or not. If exists read it first.
				if (fileExistance(courseNo)) {
					FileInputStream fileInput;
					try {
						fileInput = getActivity().openFileInput(courseNo);
						byte[] input = new byte[fileInput.available()];
						while (fileInput.read(input) != -1) {}
						//Convert into JSONArray.
						jsonArray = new JSONArray(new String(input));
					} catch (FileNotFoundException e) {
						e.printStackTrace();
					} catch (JSONException e) {
						e.printStackTrace();
					} catch (IOException e) {
						e.printStackTrace();
					}
				} else {
					//If not exists, initial the JSONArray.
					jsonArray = new JSONArray();
				}
				try {
					//Add object to the end of the JSONArray.
					jsonObject= new JSONObject();
					jsonObject.put("studentNo", students.get(p).no);
					jsonObject.put("studentName", students.get(p).name);
					jsonObject.put("studentRate", students.get(p).rate);
					jsonArray.put(jsonObject);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				//Write the change into file.
				FileOutputStream fileOutput;
				try {
					fileOutput = getActivity().openFileOutput(courseNo, Context.MODE_PRIVATE);
					fileOutput.write(jsonArray.toString().getBytes());
					fileOutput.close();
				} catch (FileNotFoundException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Toast.makeText(getActivity(), "加入成功", Toast.LENGTH_LONG).show();
				setListAdapter(getListAdapter());
			}
		});
		//Check if added or not.
		if (checkAttention(students.get(position).no)) {
			Toast.makeText(getActivity(), "已在關注列表", Toast.LENGTH_LONG).show();
		} else {
			builder.show();
		}
	}

	//Get student data by Webapi use CourseNo.
	AsyncTask<Void, Void, Void> getStudentData = new AsyncTask<Void, Void, Void>() {
		ProgressDialog mDialog;
		@Override
		protected void onPreExecute() {
			super.onPreExecute();
			//Create progress dialog.
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
				HttpGet httpRequest = new HttpGet(url + "RollCallResult?CourseNo=" + courseNo);
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
						StudentRate studentRate = new StudentRate();
						studentRate.no = jsonObject.getString("StudentNo");
						studentRate.name = jsonObject.getString("StudentName");
						studentRate.rate = jsonObject.getString("AttendPercentage");
						studentRate.absence = jsonObject.getString("AbsenceInCourse");
						students.add(studentRate);
					}
			    }
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			}
			quickSort(0, students.size()-1);
			return null;
		}
		@Override
		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			mDialog.dismiss();
			setListAdapter(getListAdapter());
		}
	};
	
	//Quick sort to sort the presentation rate from low to high.
	private void quickSort(int left, int right) {
		if (left > right) {
			return;
		}
		int pivot = Integer.valueOf(students.get(left).rate);
		int i = left + 1;
		int j = right;
		
		while(true) {
			while ( i <= right ) {
				if (Integer.valueOf(students.get(i).rate) > pivot) {
					break;
				}
				i++;
			}
			while ( j > left) {
				if (Integer.valueOf(students.get(j).rate) < pivot) {
					break;
				}
				j--;
			}
			if (i > j) {
				break;
			}
			Collections.swap(students, i, j);
		}
		Collections.swap(students, left, j);
		quickSort(left, j-1);
		quickSort(j+1, right);
	}
	
	//Check file exists or not.
	private boolean fileExistance(String fname) {
		File file = getActivity().getFileStreamPath(fname);
		if (file.exists()) {
			return true;
		}
		else {
			return false;
		}    
	}
	
	//Check student is in the list or not.
	private boolean checkAttention(String studentNo) {
		JSONArray jsonArray = null;
		JSONObject jsonObject= new JSONObject();
		//Check file exists or not.
		if (fileExistance(courseNo)) {
			FileInputStream fileInput;
			try {
				fileInput = getActivity().openFileInput(courseNo);
				byte[] input = new byte[fileInput.available()];
				while (fileInput.read(input) != -1) {}
				//Convert into JSONArray.
				jsonArray = new JSONArray(new String(input));
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (JSONException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			//Convert into JSONObject.
			for (int i=0; i<jsonArray.length(); i++) {
				try {
					jsonObject = jsonArray.getJSONObject(i);
					//Find if there is a same activity.
					if (studentNo.equals(jsonObject.getString("studentNo"))) {
						return true;
					}
				} catch (JSONException e) {
					e.printStackTrace();
				}
			}
		} else {
			return false;
		}
		return false;
	}
}
