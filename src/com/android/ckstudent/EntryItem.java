package com.android.ckstudent;

public class EntryItem implements Item {
	public final String name;
	public final String date;
	public final String id;
	public EntryItem(String name, String date, String id) {
		this.name = name;
		this.date = date;
		this.id = id;
	}
	@Override
	public boolean isSection() {
		return false;
	}
}
