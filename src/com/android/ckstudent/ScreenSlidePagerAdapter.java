package com.android.ckstudent;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.BitmapFactory;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

public class ScreenSlidePagerAdapter extends PagerAdapter {
	Context context;
	JSONArray jsonArray;
	ScreenSlidePagerAdapter(Context context, JSONArray jsonArray) {
		this.context = context;
		this.jsonArray = jsonArray;
	}
	@Override
	public void destroyItem(ViewGroup container, int position, Object object) {
		//((ViewPager) container).removeViewAt(position);
	}

	@Override
	public Object instantiateItem(ViewGroup container, int position) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View v = inflater.inflate(R.layout.slide_fragment, null);
		try {
			JSONObject jsonObject = jsonArray.getJSONObject(position);
			String pic = jsonObject.getString("Picture");
			byte[] buffer = Base64.decode(jsonObject.getString("Picture"), Base64.DEFAULT);
			if ("null".equals(pic)) {
			} else {
				ImageView imageView = (ImageView)v.findViewById(R.id.imageView1);
				imageView.setImageBitmap(BitmapFactory.decodeByteArray(buffer, 0, buffer.length));
				((ViewPager)container).addView(v);
			}
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return v;
	}
	@Override
	public int getCount() {
		return jsonArray.length();
	}
	@Override
	public boolean isViewFromObject(View view, Object object) {
		return view == object;
	}
}
