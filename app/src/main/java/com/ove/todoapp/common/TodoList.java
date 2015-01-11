package com.ove.todoapp.common;

import java.util.Date;

public class TodoList {

	private Date createdDate;
	private long id;
	private String name;

	public TodoList() {
	}

	public Date getCreatedDate() {
		return createdDate;
	}

	public long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setCreatedDate(Date date) {
		createdDate = date;
	}

	public void setId(long l) {
		id = l;
	}

	public void setName(String s) {
		name = s;
	}
}