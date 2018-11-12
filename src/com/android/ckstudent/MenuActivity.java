package com.android.ckstudent;

import android.os.Bundle;
import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

/*
 * This activity is to show menu, the entrance of the apps.
 * Mainly for teacher.
 */
public class MenuActivity extends Activity {
	String teacherNo, accountType;
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
		Bundle bundle = MenuActivity.this.getIntent().getExtras();
		url = bundle.getString("Url");
        teacherNo = bundle.getString("UserId");
        accountType = bundle.getString("UserType");
		
		gridView.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> adapter, View view, int position, long id) {
				Bundle bundle = new Bundle();
				Intent intent = new Intent();
				bundle.putString("TeacherNo", teacherNo);
				switch (position) {
				case 0:
					//Call
					bundle.putString("Url", url);
					intent.putExtras(bundle);
					intent.setClass(MenuActivity.this, SelectListActivity.class);
					MenuActivity.this.startActivity(intent);
					break;
				case 1:
					//Club
					bundle.putString("Url", url);
					intent.putExtras(bundle);
					intent.setClass(MenuActivity.this, AssociationActivity.class);
					MenuActivity.this.startActivity(intent);
					break;
				case 2:
					//Talk
					bundle.putString("Url", url);
					intent.putExtras(bundle);
					intent.setClass(MenuActivity.this, TalkTeacherActivity.class);
					MenuActivity.this.startActivity(intent);
					break;
				case 3:
					//Todo
					intent.putExtras(bundle);
					intent.setClass(MenuActivity.this, TodoActivity.class);
					MenuActivity.this.startActivity(intent);
					break;
				case 4:
					//Map
					intent.putExtras(bundle);
					intent.setClass(MenuActivity.this, MapActivity.class);
					MenuActivity.this.startActivity(intent);
					break;
				case 5:
					//Assignment
					bundle.putString("Url", url);
					bundle.putString("AccountType", accountType);
					intent.putExtras(bundle);
					intent.setClass(MenuActivity.this, AssignmentActivity.class);
					MenuActivity.this.startActivity(intent);
					break;
				}
			}
		});
	}
}
