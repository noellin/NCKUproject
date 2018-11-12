package com.android.ckstudent;

public class SectionItem implements Item {
	final String courseName;
	public SectionItem(String courseName) {
		this.courseName = courseName;
	}
	public String getTitle(){
		return courseName;
	}
	@Override
	public boolean isSection() {
		return true;
	}
}
