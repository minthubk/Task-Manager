package com.ulysses.taskmanager.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * @author Ulysses
 * */
public class TaskSQLiteHelper extends SQLiteOpenHelper {

	private static final String DB_NAME 		= "taskDB.sqlite3";
	private static final int VERSION 			= 1;
	
	//Table task
	public static final String TASK_TABLE 		= "task";
	public static final String TASK_ID 			= "id";
	public static final String TASK_KIND		= "kind";
	public static final String TASK_GOOGLE_ID	= "googleId";
	public static final String TASK_TITLE 		= "title";
	public static final String TASK_UPDATED		= "updated";
	public static final String TASK_POSITION	= "position";	
	public static final String TASK_NOTES 		= "notes";
	public static final String TASK_STATUS 		= "status";
	public static final String TASK_DUE 		= "due";
	public static final String TASK_COMPLETED	= "completed";
	
	//Table taskList
	public static final String TASK_LIST_TABLE		= "taskList";
	public static final String TASK_LIST_ID			= "id";
	public static final String TASK_LIST_GOOGLE_ID	= "googleId";
	public static final String TASK_LIST_KIND		= "kind";
	public static final String TASK_LIST_TITLE		= "title";	
	
	
	public TaskSQLiteHelper(Context context) {
		super(context, DB_NAME, null, VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		drop(db);
		create(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//unused
	}
	
	/**
	 * Creates the entire db structures
	 * */
	protected void create(SQLiteDatabase db) {
		db.execSQL(
				"create table " + TASK_TABLE +" (" +
				TASK_ID + " integer primary key autoincrement not null," +
				TASK_KIND + " text," +
				TASK_GOOGLE_ID + " text," +
				TASK_TITLE + " text," +
				TASK_UPDATED + " text," +
				TASK_POSITION + " text," +
				TASK_NOTES + " text," +
				TASK_STATUS + " text," +
				TASK_DUE + " text," +
				TASK_COMPLETED + " text" +
				");"				
			);
		
		db.execSQL(
				"create table " + TASK_LIST_TABLE +" (" +
				TASK_LIST_ID + " integer primary key autoincrement not null," +
				TASK_LIST_KIND + " text," +
				TASK_LIST_GOOGLE_ID + " text," +
				TASK_LIST_TITLE + " text" +
				");"				
			);
	}

	/**
	 * Drops the entire db structures
	 * */
	protected void drop(SQLiteDatabase db) {
		db.execSQL("drop table if exists "+ TASK_TABLE +";");
		db.execSQL("drop table if exists "+ TASK_LIST_TABLE +";");	
	}
	

}
