package com.ulysses.taskmanager;


import com.ulysses.taskmanager.controller.TaskListAdapter;
import com.ulysses.taskmanager.controller.TaskManagerApplication;
import com.ulysses.taskmanager.model.LocalTask;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;

/**
 * @author Ulysses
 * */
public class ViewTaskActivity extends ListActivity  {
    
	private Button newTaskButton;
	private Button removeTaskButton;
	private Button syncTaskButton;
	private TaskListAdapter taskAdapter;
	private TaskManagerApplication taskApp;

	/** 
     * Called when the activity is first created. 
     * */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        setUpView();
        taskApp = (TaskManagerApplication)getApplication();
        taskAdapter = new TaskListAdapter(this, taskApp.getCurrentTasks());
        setListAdapter(taskAdapter);
    }
    
    /**
	 * This method sets everything about this activity
	 * */
	private void setUpView() {
		newTaskButton		= (Button) findViewById(R.id.new_button);
		syncTaskButton		= (Button) findViewById(R.id.sync_button);
		removeTaskButton	= (Button) findViewById(R.id.remove_button);
		
		newTaskButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				openNewTaskActivity();
			}
		});
		
		syncTaskButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				openSyncTaskActivity();
			}
		});
		
		
		removeTaskButton.setOnClickListener(new View.OnClickListener() {			
			public void onClick(View v) {
				removeTask();
			}
		});
	}
	
	protected void openSyncTaskActivity() {
		Intent intent = new Intent(ViewTaskActivity.this, SyncTaskActivity.class);
		startActivity(intent);		
	}

	/**
	 * This method opens the NewTaskActivity
	 * */
	protected void openNewTaskActivity() {
		Intent intent = new Intent(ViewTaskActivity.this, NewTaskActivity.class);
		startActivity(intent);
	}

	/**
	 * This method removes all tasks masks completed
	 * */
	protected void removeTask() {
		Long[] ids = taskAdapter.removeCompletedTasks();
		taskApp.deleteTasks(ids);		
	}

	/**
	 * This method force reloads the listview on resume the app 
	 * */
	@Override
	protected void onResume() {
		super.onResume();
		taskAdapter.forceReload();
	}
	
	/**
	 * This method toggles the task status
	 * */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		taskAdapter.toggleTaskStatusAtPosition(position);
		LocalTask t = taskAdapter.getItem(position);
		taskApp.saveTask(t);
	}
}