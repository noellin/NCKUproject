package com.android.ckstudent;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/*
 * This adapter is to set the menu object layout.
 */
public class MenuAdapter extends BaseAdapter {
	Context context;
	List<Icon> icons = new ArrayList<Icon>();
	MenuAdapter(Context context) {
		this.context = context;
		icons.add(new Icon("課堂點名", R.drawable.check));
		icons.add(new Icon("社團活動", R.drawable.club));
		icons.add(new Icon("師生導談", R.drawable.talk));
		icons.add(new Icon("待辦事項", R.drawable.todo));
		icons.add(new Icon("地圖導覽", R.drawable.map));
		icons.add(new Icon("課堂公告", R.drawable.announce));
	}
	@Override
	public int getCount() {
		return icons.size();
	}
	@Override
	public Object getItem(int position) {
		return icons.get(position);
	}
	@Override
	public long getItemId(int position) {
		return icons.get(position).drawableId;
	}
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		View view;
		if (convertView == null) {
			view = inflater.inflate(R.layout.menu_item, null);
			ImageView imageView = (ImageView)view.findViewById(R.id.squareImageView1);
			TextView textView = (TextView)view.findViewById(R.id.textView1);
			imageView.setImageResource(icons.get(position).drawableId);
			textView.setText(icons.get(position).name);
		} else {
			view = convertView;
		}
		return view;
	}

}
