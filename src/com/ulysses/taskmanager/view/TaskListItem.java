package com.ulysses.taskmanager.view;

import com.ulysses.taskmanager.model.LocalTask;

import android.content.Context;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class TaskListItem extends LinearLayout {

	private LocalTask task;
	private CheckedTextView checkbox;
	private TextView textView;

	public TaskListItem(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	@Override
	protected void onFinishInflate() {
		super.onFinishInflate();
		checkbox = (CheckedTextView)findViewById(android.R.id.text1);
		textView = (TextView)findViewById(android.R.id.text1);
	}

	public void setTask(LocalTask task) {
		this.task = task;
		checkbox.setText(task.getGoogleTask().getTitle());
		checkbox.setChecked(task.isComplete());
		
		if (task.isComplete())
			textView.setPaintFlags(textView.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		else
			textView.setPaintFlags(257);
	}

	public LocalTask getTask() {
		return task;
	}
	
}
