package com.ietscroll.general.enums;

public enum CollegeDefaults {
	
	COLLEGE_EMAIL_ENDS_WITH("@ietdavv.edu.in");
	
	private String value;
	
	CollegeDefaults(String value){
		this.setValue(value);
	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}
	
}
