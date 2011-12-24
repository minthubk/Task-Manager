package com.ulysses.taskmanager.controller;

import java.util.ArrayList;


import com.ulysses.taskmanager.R;
import com.ulysses.taskmanager.model.LocalTask;
import com.ulysses.taskmanager.view.TaskListItem;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

/**
 * This Adapter holds the operations on listview
 * @author Ulysses
 * */
public class TaskListAdapter extends BaseAdapter {

	private ArrayList<LocalTask> tasks;
	private Context context;

	/**
	 * Constructor
	 * */
	public TaskListAdapter(Context context, ArrayList<LocalTask> tasks) {
		this.tasks = tasks;
		this.context = context;
	}
	
	public int getCount() {
		return tasks.size();
	}

	public LocalTask getItem(int position) {
		return (null == tasks) ? null : tasks.get(position);
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		TaskListItem taskListItem;
		if (null == convertView) {
			taskListItem = (TaskListItem)View.inflate(context, R.layout.task_list_item, null);
		} else {
			taskListItem = (TaskListItem)convertView;
		}
		taskListItem.setTask(tasks.get(position));
		return taskListItem;
	}

	/**
	 * Force the textlist reloads
	 * */
	public void forceReload() {
		notifyDataSetChanged();
	}

	/**
	 * Toggles the task status
	 * */
	public void toggleTaskStatusAtPosition(int position) {
		LocalTask task = getItem(position);
		task.toggleComplete();
		forceReload();
	}

	/**
	 * Removes all completed tasks
	 * */
	public Long[] removeCompletedTasks() {
		ArrayList<LocalTask> completedTasks = new ArrayList<LocalTask>();
		ArrayList<Long> completedIds = new ArrayList<Long>();
		for (LocalTask task : tasks) {
			if (task.isComplete()) {
				completedIds.add(task.getId());
				completedTasks.add(task);
			}
		}
		tasks.removeAll(completedTasks);
		forceReload();
		return completedIds.toArray(new Long[]{});
	}


}
