package com.android.ckstudent;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.AdapterView.OnItemClickListener;

/*
 * This activity is to show menu, the entrance of the apps.
 * Mainly for student.
 */
public class MenuActivityS extends Activity {
	String studentNo, accountType;
	String url;
	GridView gridView;
	MenuAdapter adapter;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_menu);
		gridView = (GridView)this.findViewById(R.id.menuicon);
		adapter = new MenuAdapter(this);
		gridView.setAdapter(adapter);
		//Get information from LoginActivity with bundle.
		Bundle bundle = this.getIntent().getExtras();
		url = bundle.getString("Url");
        studentNo = bundle.getString("UserId");
        accountType = bundle.getString("UserType");
		
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				Bundle bundle = new Bundle();
				Intent intent = new Intent();
				bundle.putString("StudentNo", studentNo);
				switch (position) {
				case 0:
					//Call
					bundle.putString("Url", url);
					intent.putExtras(bundle);
					intent.setClass(MenuActivityS.this, StudentBTActivity.class);
					MenuActivityS.this.startActivity(intent);
					break;
				case 1:
					//Club
					bundle.putString("Url", url);
					intent.putExtras(bundle);
					intent.setClass(MenuActivityS.this, AssociationActivity.class);
					MenuActivityS.this.startActivity(intent);
					break;
				case 2:
					//Talk
					bundle.putString("Url", url);
					intent.putExtras(bundle);
					intent.setClass(MenuActivityS.this, TalkStudentActivity.class);
					MenuActivityS.this.startActivity(intent);
					break;
				case 3:
					//Todo
					intent.putExtras(bundle);
					intent.setClass(MenuActivityS.this, TodoActivity.class);
					MenuActivityS.this.startActivity(intent);
					break;
				case 4:
					//Map
					intent.putExtras(bundle);
					intent.setClass(MenuActivityS.this, MapActivity.class);
					MenuActivityS.this.startActivity(intent);
					break;
				case 5:
					//Assignment
					bundle.putString("Url", url);
					bundle.putString("AccountType", accountType);
					intent.putExtras(bundle);
					intent.setClass(MenuActivityS.this, AssignmentStudentActivity.class);
					MenuActivityS.this.startActivity(intent);
					break;
				}
			}
		});
	}
}
