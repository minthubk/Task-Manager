package com.ulysses.taskmanager.model;

import java.util.Date;

import com.google.api.client.util.DateTime;
import com.google.api.services.tasks.model.Task;

/**
 * PS: I can't extends com.google.api.services.tasks.model.Task
 * so I have a private instance of it. 
 * @author Ulysses
 * */
public class LocalTask {
	
	private long id 		= 0;
	private Task googleTask = null;
	
	private static final String STATUS_COMPLETED 	= "completed";
	private static final String STATUS_NEEDS_ACTION = "needsAction";

	public LocalTask(Task googleTask) {
		this.googleTask = googleTask;
	}
	
	public long getId() {
		return id;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setComplete(boolean complete) {
		
		if (complete){
			googleTask.setStatus(STATUS_COMPLETED);
			googleTask.setCompleted(new DateTime(new Date()));
		}else{
			googleTask.setStatus(STATUS_NEEDS_ACTION);
			googleTask.setCompleted(null);
		}
		
	}

	public boolean isComplete() {		
		if (googleTask.getStatus() == null || googleTask.getStatus().equalsIgnoreCase(STATUS_NEEDS_ACTION))
			return false;
		else
			return true;
	}

	public void toggleComplete() {		
		if (isComplete())
			setComplete(false);
		else
			setComplete(true);
	}
	
	
	public Task getGoogleTask() {
		return googleTask;
	}

	public void setGoogleTask(Task googleTask) {
		this.googleTask = googleTask;
	}

	public String toString() {
		return googleTask.getTitle();
	}	
	
}
