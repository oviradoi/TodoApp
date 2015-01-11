package com.ove.todoapp.common;

import java.util.Date;

public class TodoNote {

	private boolean checked;
	private Date createdDate;
	private Date doneDate;
	private long id;
	private String text;

	public TodoNote() {
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public Date getDoneDate() {
		return doneDate;
	}

	public long getId() {
		return id;
	}

	public String getText() {
		return text;
	}

	public boolean isChecked() {
		return checked;
	}

	public void setChecked(boolean flag) {
		checked = flag;
	}

	public void setCreatedDate(Date date) {
		createdDate = date;
	}

	public void setDoneDate(Date date) {
		doneDate = date;
	}

	public void setId(long l) {
		id = l;
	}

	public void setText(String s) {
		text = s;
	}
}