/**
 * 
 */
package com.ulysses.taskmanager.controller;

import java.io.IOException;
import java.util.List;

import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.ulysses.taskmanager.model.LocalTask;

import com.ulysses.taskmanager.controller.TaskManagerApplication;

import android.os.AsyncTask;
import android.util.Log;

import static com.ulysses.taskmanager.model.TaskSQLiteHelper.*;

/**
 * @author ulyfred
 */
public class GoogleTaskLoaderAsync extends AsyncTask<Object, Void, Boolean> {

	private TaskManagerApplication app ;
	private Tasks service;
	private TaskListAdapter taskAdapter;
	
	@Override
	protected Boolean doInBackground(Object... params) {

		Log.d("GoogleTaskLoaderAsync", "doInBackground");
		
		this.service 		= (Tasks) params[0];
		this.app			= (TaskManagerApplication) params[1];		
		this.taskAdapter	= (TaskListAdapter) params[2]; 
		
		try {
			List<Task> tasks;			
			
			tasks = service.tasks
				.list("@default")
				.setUpdatedMin(app.lastUpdate())
				.execute().getItems();		
			if (tasks != null) {
				for (Task task : tasks) {
					Log.d("doInBackground", task.getTitle());
			
					LocalTask t = new LocalTask(task);
					if (app.addTask(t) == UPDATE_GOOGLE){
						Log.d("doInBackground", "need to update google's content");
					}
				}
			} else {
				//unused - no tasks
				Log.d("GoogleTaskLoaderAsync", "no tasks");
			}
			
			return true;
		} catch (IOException e) {
			Log.e("doInBackground", e.getMessage());
			return false;
		}
	}


	@Override
	protected void onPostExecute(Boolean result) {		
		super.onPostExecute(result);
		
		Log.d("GoogleTaskLoaderAsync", "onPostExecute");
		
		taskAdapter.forceReload();
	}	
	
	
	
	

}
