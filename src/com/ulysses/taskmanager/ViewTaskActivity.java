package com.ulysses.taskmanager;


import java.io.IOException;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.samples.shared.android.ClientCredentials;
import com.google.api.services.tasks.Tasks;
import com.ulysses.taskmanager.controller.GoogleTaskLoaderAsync;
import com.ulysses.taskmanager.controller.TaskListAdapter;
import com.ulysses.taskmanager.controller.TaskManagerApplication;
import com.ulysses.taskmanager.model.LocalTask;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
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
	
	private static final String AUTH_TOKEN_TYPE 	= "Manage your tasks";
	private static final String PREF_FILE 			= "MyPrefs";
	private static final int DIALOG_ACCOUNTS 		= 0;
	private static final int MENU_ACCOUNTS 			= 0;
	public static final int REQUEST_AUTHENTICATE 	= 0;

	private Tasks service;
	private final HttpTransport transport = AndroidHttp.newCompatibleTransport();
	
	private GoogleAccessProtectedResource accessProtectedResource = 
			new GoogleAccessProtectedResource(null);

	private GoogleAccountManager accountManager;

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
        
        startGoogleObserver();        
    }
    
    /**
	 * Sets everything about this activity
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
	
	/**
	 * Opens the SyncTaskActivity
	 * */
	protected void openSyncTaskActivity() {
		Intent intent = new Intent(ViewTaskActivity.this, SyncTaskActivity.class);
		startActivity(intent);		
	}

	/**
	 * Opens the NewTaskActivity
	 * */
	protected void openNewTaskActivity() {
		Intent intent = new Intent(ViewTaskActivity.this, NewTaskActivity.class);
		startActivity(intent);
	}

	/**
	 * Removes all tasks masks completed
	 * */
	protected void removeTask() {
		Long[] ids = taskAdapter.removeCompletedTasks();
		taskApp.deleteTasks(ids);		
	}

	/**
	 * Force reloads the listview on resume the app 
	 * */
	@Override
	protected void onResume() {
		super.onResume();
		taskAdapter.forceReload();
	}
	
	/**
	 * Toggles the task status
	 * */
	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		super.onListItemClick(l, v, position, id);
		taskAdapter.toggleTaskStatusAtPosition(position);
		LocalTask t = taskAdapter.getItem(position);
		taskApp.saveTask(t);
	}
	
	
	protected void startGoogleObserver(){
		service = new Tasks(transport, accessProtectedResource,	new JacksonFactory());
		service.setKey(ClientCredentials.KEY);
		service.setApplicationName(getString(R.string.app_name));
		accountManager = new GoogleAccountManager(this);		
		loadAccount(false);
	}
	
	
	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
			case DIALOG_ACCOUNTS:
				return showDialogAccounts();
			}
		return null;
	}
	
	
	private AlertDialog showDialogAccounts(){
		
		final Account[] accounts = accountManager.getAccounts();
		final int size = accounts.length;
		
		String[] names = new String[size];
		for (int i = 0; i < size; i++) {
			names[i] = accounts[i].name;
		}
		
		AlertDialog.Builder dialogAccounts = 
				new AlertDialog.Builder(this)
				.setTitle(getString(R.string.select_account))
				.setItems(names, new DialogInterface.OnClickListener() {
					
					public void onClick(DialogInterface dialog, int which) {
						setAccount(accounts[which]);
					}
					
				});
		
		return dialogAccounts.create();
	}
	
	
	public void setAccount(final Account account) {
		
		//Set account on preferences
		SharedPreferences settings = getSharedPreferences(PREF_FILE, MODE_PRIVATE);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("accountName", account.name);
		editor.commit();
		
		accountManager.manager.getAuthToken(account, AUTH_TOKEN_TYPE, true,
				new AccountManagerCallback<Bundle>() {

					public void run(AccountManagerFuture<Bundle> future) {
						try {
							Bundle bundle = future.getResult();
							if (bundle.containsKey(AccountManager.KEY_INTENT)) {
								
								Intent intent = bundle.getParcelable(AccountManager.KEY_INTENT);
								intent.setFlags(intent.getFlags() & ~Intent.FLAG_ACTIVITY_NEW_TASK);								
								
								startActivityForResult(intent,REQUEST_AUTHENTICATE);
							} else if (bundle.containsKey(AccountManager.KEY_AUTHTOKEN)) {
								
								Log.d("setAccount", bundle.getString(AccountManager.KEY_AUTHTOKEN));
								accessProtectedResource.setAccessToken(bundle.getString(AccountManager.KEY_AUTHTOKEN));								
								onAuthToken();
								
							}
						} catch (Exception e) {
							handleException(e);
						}
					}
				}, null);
	}
	
	public void loadAccount(boolean tokenExpired) {
		
		//Load account from preferences
		SharedPreferences settings = getSharedPreferences(PREF_FILE, 0);
		String accountName = settings.getString("accountName", null);
		Account account = accountManager.getAccountByName(accountName);
		
		if (account != null) {
			
			if (tokenExpired) {
				
				accountManager.invalidateAuthToken(accessProtectedResource.getAccessToken());
				accessProtectedResource.setAccessToken(null);
				
			}
			
			setAccount(account);
		
		}else
			showDialog(DIALOG_ACCOUNTS);
		
	}

	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_AUTHENTICATE:
			if (resultCode == RESULT_OK) {
				loadAccount(false);
			} else {
				showDialog(DIALOG_ACCOUNTS);
			}
			break;
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		menu.add(0, MENU_ACCOUNTS, 0, "Switch Account");
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
			case MENU_ACCOUNTS:
				showDialog(DIALOG_ACCOUNTS);
				return true;
		}
		return false;
	}

	public void handleException(Exception e) {
		Log.e("handleException", "not good");
		
		e.printStackTrace();
		if (e instanceof HttpResponseException) {
			HttpResponse response = ((HttpResponseException) e).getResponse();
			int statusCode = response.getStatusCode();
			try {
				response.ignore();
			} catch (IOException e1) {
				e1.printStackTrace();
			}			
			if (statusCode == 401) {				
				loadAccount(true);
				return;
			}
		}		
	}

	public void onAuthToken() {
		
		Log.d("ViewTaskActivity", "onAuthToken");
		final GoogleTaskLoaderAsync loaderAsync = new GoogleTaskLoaderAsync();		
		
		new Thread(new Runnable() {			
			
			public void run() {

				try {
					
					service.tasklists.list().execute();
					loaderAsync.execute(service,taskApp,taskAdapter);	
					
				} catch (IOException e) {
					handleException(e);
				}
							
			}
			
		}).start();
	}
	
	
}