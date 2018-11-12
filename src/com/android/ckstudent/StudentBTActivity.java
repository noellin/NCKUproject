package com.android.ckstudent;

import android.app.Activity;
import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

/*
 * This activity is to let student open their bluetooth to wait for call.
 */
public class StudentBTActivity extends Activity {
	static BluetoothAdapter bluetoothAdapter;
	String studentNo, url;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.student_waiting);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		View background = findViewById(R.id.bg);
		background.setBackgroundResource(R.drawable.custombg);
		//Get bluetooth instance.
		bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
		if (!bluetoothAdapter.isEnabled()) {
			bluetoothAdapter.enable();
		}
		//Get student information from MenuActivityS with bundle.
		Bundle bundle = StudentBTActivity.this.getIntent().getExtras();
		studentNo = bundle.getString("StudentNo");
		url = bundle.getString("Url");
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Alert");
		builder.setMessage("將自動開啟藍牙協助自動點名");
		builder.setPositiveButton("我知道了", null);
		builder.show();
	}
	
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
