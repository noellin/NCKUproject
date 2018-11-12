package com.android.ckstudent;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;
import android.widget.Toast;

/*
 * This activity is to manage presentation fragment and attention fragment.
 */
public class PresentationActivity extends FragmentActivity {
	private TabHost mTabHost;
	private TabManager mTabManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
        setContentView(R.layout.tab_main);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        View background = findViewById(R.id.bg);
		background.setBackgroundResource(R.drawable.custombg);
		//bundle
		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup();
		mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);
		//Set default page.
		mTabHost.setCurrentTab(0);
		//Dynamically add two tab for homework and contest.
        mTabManager.addTab(mTabHost.newTabSpec("Fragment1").setIndicator("出席率"),
            PresentationFragment.class, null);
        mTabManager.addTab(mTabHost.newTabSpec("Fragment2").setIndicator("關注名單"),
            AttentionFragment.class, null);
        Toast.makeText(this, "點擊學生可將學生加入關注", Toast.LENGTH_LONG).show();
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
