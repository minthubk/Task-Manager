package com.ulysses.taskmanager;


import com.google.api.services.tasks.model.Task;
import com.ulysses.taskmanager.controller.TaskManagerApplication;
import com.ulysses.taskmanager.model.LocalTask;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * @author Ulysses
 * */
public class NewTaskActivity extends Activity {

	private Button cancelButton;
	private Button addTaskButton;
	private EditText taskEditText;
	private boolean isChanged;
	private AlertDialog unsavedChangesDialog;
	private AlertDialog emptyDialog;

	/**
	 * Called when the activity is first created.
	 * */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.new_task);

		setUpView();
	}

	/**
	 * This method sets everything about this activity
	 * */
	private void setUpView() {
		taskEditText 	= (EditText) findViewById(R.id.task_name);
		addTaskButton 	= (Button) findViewById(R.id.add_button);
		cancelButton 	= (Button) findViewById(R.id.cancel_button);

		addTaskButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				addTask();
			}
		});

		cancelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				cancel();
			}
		});

		taskEditText.addTextChangedListener(new TextWatcher() {
			public void onTextChanged(CharSequence s, int start, int before,
					int count) {
				isChanged = true;
			}

			public void afterTextChanged(Editable s) {
				// unused
			}

			public void beforeTextChanged(CharSequence s, int start, int count,int after) {
				// unused
			}
		});
	}

	/**
	 * This method closes the activity. First, it verifies if there is a unsaved changes
	 * */
	protected void cancel() {
		String text = taskEditText.getText().toString().trim();
		
		if (isChanged && !text.equals("") ) {
			showConfirmDialog();
		} else {
			finish();
		}
	}

	/**
	 * This method shows a dialog box to confirm the unsaved changes
	 * */
	private void showConfirmDialog() {
		
		if (unsavedChangesDialog == null){
			
			unsavedChangesDialog = new AlertDialog.Builder(this)
					.setTitle(R.string.unsaved_changes_title)
					.setMessage(R.string.unsaved_changes_message)
					.setPositiveButton(R.string.add_task,
							new AlertDialog.OnClickListener() {
								public void onClick(DialogInterface dialog,int which) {
									addTask();
								}
							})
					.setNeutralButton(R.string.discard,
							new AlertDialog.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									finish();
								}
							})
					.setNegativeButton(android.R.string.cancel,
							new AlertDialog.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int which) {
									unsavedChangesDialog.cancel();
								}
							}).create();
		}
		
		unsavedChangesDialog.show();
	}

	/**
	 * This method adds a new task and closes the activity
	 * */
	protected void addTask() {
		String taskName = taskEditText.getText().toString().trim();
		
		if (!taskName.equals("")){
			
			Task googleTask = new Task();
			googleTask.setTitle(taskName);
			
			LocalTask localTask = new LocalTask(googleTask);
			localTask.setComplete(false);
			
			((TaskManagerApplication) getApplication()).addTask(localTask);
			
			finish();
		}else{
			
			if (emptyDialog == null){
				
				emptyDialog = new AlertDialog.Builder(this)
							.setTitle(R.string.empty_task)
							.setMessage(R.string.task_empty_message)
							.setPositiveButton(android.R.string.ok, 
									new AlertDialog.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											emptyDialog.cancel();
										}
									})
							.setNegativeButton(R.string.discard, 
									new AlertDialog.OnClickListener() {
										public void onClick(DialogInterface dialog, int which) {
											finish();
										}
							}).create();
			}
			
			emptyDialog.show();
			
			
		}
	}

}
