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

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

/*
 * This activity is to show the detail of the task.
 */
public class TaskDetailActivity extends Activity {
	ArrayList<Event> events;
	EditText editText;
	TextView textView2, textView4;
	String eventType, eventName, eventviewDate, eventDate, eventContent;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.task_detail);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		View background = findViewById(R.id.bg);
		background.setBackgroundResource(R.drawable.custombg);
		textView2 = (TextView)this.findViewById(R.id.textView2);
		textView4 = (TextView)this.findViewById(R.id.textView4);
		editText = (EditText)this.findViewById(R.id.editText1);
		events = new ArrayList<Event>();
		//Get information from TodoActivity with bundle.
		Bundle bundle = new Bundle();
		bundle = this.getIntent().getExtras();
		eventType = bundle.getString("eventType");
		eventName = bundle.getString("eventName");
		eventviewDate = bundle.getString("eventviewDate");
		eventDate = bundle.getString("eventDate");
		eventContent = bundle.getString("eventContent");
		//Set name and date to text.
		textView2.setText(eventName);
		textView4.setText(eventviewDate);
		editText.setText(eventContent);
	}
	
	//Add new content or edit old content.
	private void setContent() {
		Event event = new Event();
		event.name = eventName;
		event.date = eventDate;
		event.viewDate = eventviewDate;
		event.type = eventType;
		event.content = eventContent;
		JSONArray jsonArray = null;
		JSONObject jsonObject= new JSONObject();
		//Check file exists or not. If exists read it first.
		FileInputStream fileInput;
		try {
			fileInput = openFileInput(eventType);
			byte[] input = new byte[fileInput.available()];
			while (fileInput.read(input) != -1) {}
			//Convert to JSONArray.
			jsonArray = new JSONArray(new String(input));
			for (int i=0; i<jsonArray.length(); i++) {
				jsonObject = jsonArray.getJSONObject(i);
				if (eventName.equals(jsonObject.getString("eventName"))) {
					jsonObject.remove("eventContent");
					jsonObject.put("eventContent", editText.getText().toString());
				}
			}
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		//Write the change into file.
		FileOutputStream fileOutput;
		try {
			fileOutput = openFileOutput(eventType, Context.MODE_PRIVATE);
			fileOutput.write(jsonArray.toString().getBytes());
			fileOutput.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	//Delete a task item.
	private void deleteTask(String type) {
		JSONArray jsonArray = new JSONArray();
		JSONObject jsonObject= new JSONObject();
		int index = 0;
		FileInputStream fileInput;
		try {
			//Open file.
			fileInput = openFileInput(type);
			byte[] input = new byte[fileInput.available()];
			while (fileInput.read(input) != -1) {}
			//Convert into JSONArray.
			jsonArray = new JSONArray(new String(input));
			for (int i=0; i<jsonArray.length(); i++) {
				jsonObject = jsonArray.getJSONObject(i);
				//Convert into JSONObject.
				Event event = new Event();
				event.name = jsonObject.getString("eventName");
				//Check the position of the activity.
				if (eventName.equals(event.name)) {
					index = i;
				}
				event.viewDate = jsonObject.getString("eventviewDate");
				events.add(event);
			}
			//Use the matche index to remove object.
			events.remove(index);
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
		if (events.size() != 0) {
			//Convert into JSONArray.
			for (int i=0; i<events.size(); i++) {
				try {
					jsonObject.put("eventName", events.get(i).name);
					jsonObject.put("eventviewDate", events.get(i).viewDate);
				} catch (JSONException e) {
					e.printStackTrace();
				}
				jsonArray.put(jsonObject);
			}
			FileOutputStream fileOutput;
			try {
				fileOutput = openFileOutput(type, Context.MODE_PRIVATE);
				fileOutput.write(jsonArray.toString().getBytes());
				fileOutput.close();
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			//If the deleted object is the only object in the list, delete the file.
			File file = getBaseContext().getFileStreamPath(type);
			file.delete();
		}
	}
	
	private DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
		@Override
		public void onClick(DialogInterface dialog, int which) {
			//Delete and show success message.
			deleteTask(eventType);
			Toast.makeText(getBaseContext(), "刪除完成!", Toast.LENGTH_SHORT).show();
			finish();
		}
	};
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.taskdetail, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.delete:
			//Show alert to ensure.
			AlertDialog.Builder builder = new AlertDialog.Builder(TaskDetailActivity.this);
			builder.setTitle("Are you sure?");
			builder.setMessage("你真的確定要刪除嗎?");
			builder.setPositiveButton("確定", listener);
			builder.show();
			break;
		case R.id.finish:
			//Storage change.
			setContent();
			finish();
			break;
		case android.R.id.home:
        	onBackPressed();
            break;
		}
		return true;
	}
}
