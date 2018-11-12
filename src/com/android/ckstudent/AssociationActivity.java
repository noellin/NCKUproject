package com.android.ckstudent;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TabHost;

/*
 * This activity is for Association using tabhost to show association and its activities.
 */
public class AssociationActivity extends FragmentActivity {
	String url;
	private TabHost mTabHost;
	private TabManager mTabManager;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.tab_main);
		getActionBar().setDisplayHomeAsUpEnabled(true);
		View background = findViewById(R.id.bg);
		background.setBackgroundResource(R.drawable.custombg);
		Bundle bundle = getIntent().getExtras();
		url = bundle.getString("Url");
		mTabHost = (TabHost)findViewById(android.R.id.tabhost);
		mTabHost.setup();
		mTabManager = new TabManager(this, mTabHost, R.id.realtabcontent);
		//Set default page.
		mTabHost.setCurrentTab(0);
		//Dynamically add three tab for activities, associtation information.
		//Also you could add the activity that you want to track to favorite.
        mTabManager.addTab(mTabHost.newTabSpec("Fragment1").setIndicator("最新活動",
        		this.getResources().getDrawable(android.R.drawable.ic_dialog_alert)),
            FestivalFragment.class, null);
        mTabManager.addTab(mTabHost.newTabSpec("Fragment2").setIndicator("社團資訊"),
            ClubFragment.class, null);
        mTabManager.addTab(mTabHost.newTabSpec("Fragment3").setIndicator("收藏活動"),
            FavoriteFragment.class, null);
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.association, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		super.onOptionsItemSelected(item);
		switch (item.getItemId()) {
		case R.id.poster:
			//Poster
			Bundle bundle = new Bundle();
			Intent intent = new Intent();
			bundle.putString("Url", url);
			intent.putExtras(bundle);
			intent.setClass(AssociationActivity.this, PosterPagerActivity.class);
			startActivity(intent);
			break;
		case android.R.id.home:
        	onBackPressed();
            break;
		}
		return true;
	}
}
