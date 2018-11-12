package com.android.ckstudent;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

/*
 * This fragment is to show students in attention list.
 * Students in the list means student's presentation rate is low enough
 * that teacher give attention on he.
 */
public class AttentionFragment extends ListFragment {
	AttentionAdapter adapter;
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
		adapter = new AttentionAdapter(this.getActivity(), students);
		this.setListAdapter(adapter);
		//Get bundle value.
		Bundle course = getActivity().getIntent().getExtras();
		courseNo = course.getString("courseNo");
		//Get attention list.
		getAttentionData();
		//Check list is null or not.
		if (students.size() == 0) {
			AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
			builder.setTitle("Alert");
			builder.setMessage("觀察列表中沒有學生");
			builder.setPositiveButton("我知道了", null);
			builder.show();
		}
	}

	//Remove student from list.
	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		final int p = position;
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle("移除");
		builder.setMessage("是否將此學生從觀察列表移除?");
		builder.setPositiveButton("取消", null);
		builder.setNegativeButton("移除", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				//Remove student from list.
				removeStudent(students.get(p).no);
			}
		});
		builder.show();
	}
	
	//Get attention data from internal storage.
	private void getAttentionData() {
		JSONArray jsonArray;
		JSONObject jsonObject;
		//Open the file and get data into JSONArray.
		FileInputStream fileInput;
		try {
			fileInput = getActivity().openFileInput(courseNo);
			byte[] input = new byte[fileInput.available()];
			while (fileInput.read(input) != -1) {}
			jsonArray = new JSONArray(new String(input));
			for (int i=0; i<jsonArray.length(); i++) {
				//Put each one into activity list.
				jsonObject = jsonArray.getJSONObject(i);
				StudentRate studentRate = new StudentRate();
				studentRate.no = jsonObject.getString("studentNo");
				studentRate.name = jsonObject.getString("studentName");
				students.add(studentRate);
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		setListAdapter(getListAdapter());
	}
	
	//Remove student from internal storage.
	private void removeStudent(String studentNo) {
		ArrayList<StudentRate> tempStudents = new ArrayList<StudentRate>();
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject= new JSONObject();
		int index = 0;
		FileInputStream fileInput;
		try {
			//Open file.
			fileInput = getActivity().openFileInput(courseNo);
			byte[] input = new byte[fileInput.available()];
			while (fileInput.read(input) != -1) {}
			//Convert into JSONArray.
			jsonArray = new JSONArray(new String(input));
			for (int i=0; i<jsonArray.length(); i++) {
				jsonObject = jsonArray.getJSONObject(i);
				StudentRate studentRate = new StudentRate();
				studentRate.no = jsonObject.getString("studentNo");
				studentRate.name = jsonObject.getString("studentName");
				//Check the position of the activity.
				if (studentNo.equals(studentRate.no)) {
					index = i;
				}
				tempStudents.add(studentRate);
			}
			//Use the matche index to remove object.
			tempStudents.remove(index);
			students.remove(index);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Initialize the JSONArray.
		jsonArray = new JSONArray();
		//If the arraysize isn't zero then write the entired data back.
		if (tempStudents.size() != 0) {
			//Convert into JSONArray.
			for (int i=0; i<tempStudents.size(); i++) {
				try {
					jsonObject.put("studentNo", tempStudents.get(i).no);
					jsonObject.put("studentName", tempStudents.get(i).name);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				jsonArray.put(jsonObject);
			}
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
		} else {
			//If the deleted object is the only object in the list, delete the file.
			File file = getActivity().getFileStreamPath(courseNo);
			file.delete();
		}
		setListAdapter(getListAdapter());
	}
}
