package com.ulysses.taskmanager.model;

import com.google.api.services.tasks.model.TaskList;

public class LocalTaskList {

	
	private long id;
	private TaskList googleTaskList;
	
	public LocalTaskList(TaskList googleTaskList) {
		super();
		this.googleTaskList = googleTaskList;
	}

	/**
	 * @return the googleTaskList
	 */
	public TaskList getGoogleTaskList() {
		return googleTaskList;
	}

	/**
	 * @param googleTaskList the googleTaskList to set
	 */
	public void setGoogleTaskList(TaskList googleTaskList) {
		this.googleTaskList = googleTaskList;
	}

	/**
	 * @return the id
	 */
	public long getId() {
		return id;
	}

	/**
	 * @param id the id to set
	 */
	public void setId(long id) {
		this.id = id;
	}
	
	
	
	
	
	
}
