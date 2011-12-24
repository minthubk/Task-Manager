package com.ulysses.taskmanager.controller;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import com.google.api.services.tasks.model.Task;
import com.ulysses.taskmanager.model.LocalTask;
import com.ulysses.taskmanager.model.TaskSQLiteHelper;

import android.app.Application;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import static com.ulysses.taskmanager.model.TaskSQLiteHelper.*;
/**
 * This application holds info among activities
 * */
public class TaskManagerApplication extends Application {

	private SQLiteDatabase db;
	private ArrayList<LocalTask> currentTasks;
	
	@Override
	public void onCreate() {
		super.onCreate();
		
		TaskSQLiteHelper dbHelper = new TaskSQLiteHelper(this);
		db = dbHelper.getWritableDatabase();
		
		if (null == currentTasks) {
			loadTasks();
		}
	}
	
	@Override
	public void onTerminate() {
		super.onTerminate();
		db.close();
	}

	/**
	 * Returns all tasks
	 * */
	public ArrayList<LocalTask> getCurrentTasks() {
		return currentTasks;
	}
	
	/**
	 * Adds a new task
	 * */
	public int addTask(LocalTask task) {
		assert(null != task);
		
		if (task.getGoogleTask().getId() != null){
			//it's from google
			
			String where = String.format("%s = '%s'", TASK_GOOGLE_ID, task.getGoogleTask().getId());
			
			Cursor tasksCursor = db.query(TASK_TABLE, 
					new String[] {TASK_ID, TASK_UPDATED}, 
					where, null, null, null, null);
			
			tasksCursor.moveToFirst();
			
			if (tasksCursor.isAfterLast()){
				//not exists, so insert
				Log.d("addTask - new task", task.getGoogleTask().getTitle());
				tasksCursor.deactivate();
				insertOnDB(task);
				return CREATE_DB;
			}else{
								
				String stringDate = tasksCursor.getString(1);  
				SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");  
				
				try {
					
					Date localDate = format.parse(stringDate);
					Date googleDate = format.parse(task.getGoogleTask().getUpdated().toString());
					
					if (localDate.compareTo(googleDate) == 0)
						Log.d("compareTo","iguals");
						//return EQUAL;
					else if (localDate.compareTo(googleDate) < 0){
						Log.d("compareTo","less");
						//update local content
						tasksCursor.deactivate();
						updateOnDB(task);
						return UPDATE_DB;
					}else{						
						Log.d("compareTo","greater");
						tasksCursor.deactivate();
						//update google content
						return UPDATE_GOOGLE;
					}
				} catch (ParseException e) {
					Log.wtf("addTask", e.getMessage());					
				}  			
				
			}
			
		}else{
			//it's not from google			
			insertOnDB(task);	
			return CREATE_GOOGLE;
			
		}
		
		return -1;
	}
	
	private ContentValues setValues(LocalTask task){
		ContentValues values = new ContentValues();
		
		values.put(TASK_KIND, (task.getGoogleTask().getKind() == null)?
				"": task.getGoogleTask().getKind());

		values.put(TASK_GOOGLE_ID, (task.getGoogleTask().getId() == null)?
						"":task.getGoogleTask().getId());
		
		values.put(TASK_TITLE, (task.getGoogleTask().getTitle() == null)?
						"": task.getGoogleTask().getTitle());
		
		values.put(TASK_UPDATED, (task.getGoogleTask().getUpdated() == null)?
						"": task.getGoogleTask().getUpdated().toString() );
		
		values.put(TASK_POSITION, (task.getGoogleTask().getPosition() == null)?
						"": task.getGoogleTask().getPosition());
		
		values.put(TASK_NOTES, (task.getGoogleTask().getNotes() == null)?
						"": task.getGoogleTask().getNotes());
		
		values.put(TASK_STATUS, (task.getGoogleTask().getStatus() == null)?
						"":task.getGoogleTask().getStatus());
		
		values.put(TASK_DUE, (task.getGoogleTask().getDue() == null )?
						"":task.getGoogleTask().getDue().toString());
		
		values.put(TASK_COMPLETED, (task.getGoogleTask().getCompleted() == null)?
						"":task.getGoogleTask().getCompleted().toString());
		
		return values;
	}
	
	private void updateOnDB(LocalTask task) {
		assert(null != task);
		ContentValues values = setValues(task);
		
		String googleId = task.getGoogleTask().getId();
		String where = String.format("%s = '%s'", TASK_GOOGLE_ID, googleId);

		Log.d("update - "+googleId, task.getGoogleTask().getTitle());
		
		db.update(TASK_TABLE, values, where, null);
		
	}

	
	
	private void insertOnDB(LocalTask task){
		ContentValues values = setValues(task);
		task.setId(db.insert(TASK_TABLE, null, values));

		currentTasks.add(task);	
	}
	
	/**
	 * Loads all tasks from db
	 * */
	private void loadTasks() {
		currentTasks = new ArrayList<LocalTask>();
		
		Cursor tasksCursor = db.query(TASK_TABLE, 
				new String[] {TASK_ID, TASK_TITLE, TASK_STATUS}, 
				null, null, null, null, 
				String.format("%s,%s", TASK_STATUS, TASK_TITLE));
		
		tasksCursor.moveToFirst();
		LocalTask t;
		if (! tasksCursor.isAfterLast()) {
			do {
				
				long id 			= tasksCursor.getLong(0);
				String title 		= tasksCursor.getString(1);
				String status		= tasksCursor.getString(2);
				
				Task googleTask = new Task();				
				googleTask.setTitle(title);
				googleTask.setStatus(status);
				
				t = new LocalTask(googleTask);
				t.setId(id);

				currentTasks.add(t);
				
			} while (tasksCursor.moveToNext());
		}
		
		tasksCursor.close();
	}
	
	/**
	 * Updates task
	 * */
	public void saveTask(LocalTask t) {
		assert(null != t);
		ContentValues values = new ContentValues();
		values.put(TASK_TITLE, t.getGoogleTask().getTitle());
		values.put(TASK_STATUS, t.getGoogleTask().getStatus());
		
		long id = t.getId();
		String where = String.format("%s = %d", TASK_ID, id);

		db.update(TASK_TABLE, values, where, null);
	}
	
	/**
	 * Deletes one or more tasks by its id
	 * */
	public void deleteTasks(Long[] ids) {
		StringBuffer idList = new StringBuffer();
		for (int i=0; i<ids.length; i++) {
			idList.append(ids[i]);
			if (i < ids.length - 1) {
				idList.append(",");
			}
		}
		String where = String.format("%s in (%s)", TASK_ID, idList);
		db.delete(TASK_TABLE, where, null);
	}

	public String lastUpdate() {		
		//for example
		return "2011-10-02T19:03:15.000Z";
	}

	
}

