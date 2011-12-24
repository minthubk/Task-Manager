package com.ulysses.taskmanager;

import java.io.IOException;
import java.util.List;


import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.googleapis.auth.oauth2.draft10.GoogleAccessProtectedResource;
import com.google.api.client.googleapis.extensions.android2.auth.GoogleAccountManager;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpResponseException;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.services.samples.shared.android.ClientCredentials;
import com.google.api.services.tasks.Tasks;
import com.google.api.services.tasks.model.Task;
import com.ulysses.taskmanager.controller.TaskManagerApplication;
import com.ulysses.taskmanager.model.LocalTask;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class SyncTaskActivity extends Activity {	

	// This must be the exact string, and is a special for alias OAuth 2 scope
	// "https://www.googleapis.com/auth/tasks"
	private static final String AUTH_TOKEN_TYPE = "Manage your tasks";

	private static final String PREF = "MyPrefs";
	private static final int DIALOG_ACCOUNTS = 0;
	private static final int MENU_ACCOUNTS = 0;
	public static final int REQUEST_AUTHENTICATE = 0;

	private final HttpTransport transport = 
			AndroidHttp.newCompatibleTransport();

	private Tasks service;
	private GoogleAccessProtectedResource accessProtectedResource = 
			new GoogleAccessProtectedResource(null);

	private GoogleAccountManager accountManager;

	/*
	 * (non-Javadoc)
	 * 
	 * @see android.app.Activity#onCreate(android.os.Bundle)
	 */
	@Override
	protected void onCreate(Bundle savedInstanceState) {		
		super.onCreate(savedInstanceState);
		setContentView(R.layout.sync_task);

		service = new Tasks(transport, accessProtectedResource,	new JacksonFactory());
		service.setKey(ClientCredentials.KEY);
		service.setApplicationName("Google Tasks");
		accountManager = new GoogleAccountManager(this);		
		gotAccount(false);
	}

	@Override
	protected Dialog onCreateDialog(int id) {
		switch (id) {
		case DIALOG_ACCOUNTS:
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Select a Google account");
			final Account[] accounts = accountManager.getAccounts();
			final int size = accounts.length;
			String[] names = new String[size];
			for (int i = 0; i < size; i++) {
				names[i] = accounts[i].name;
			}
			builder.setItems(names, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int which) {
					gotAccount(accounts[which]);
				}
			});
			return builder.create();
		}
		return null;
	}

	public void gotAccount(boolean tokenExpired) {
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		String accountName = settings.getString("accountName", null);
		Account account = accountManager.getAccountByName(accountName);
		if (account != null) {
			if (tokenExpired) {
				accountManager.invalidateAuthToken(accessProtectedResource
						.getAccessToken());
				accessProtectedResource.setAccessToken(null);
			}
			gotAccount(account);
			return;
		}
		showDialog(DIALOG_ACCOUNTS);
	}

	public void gotAccount(final Account account) {
		SharedPreferences settings = getSharedPreferences(PREF, 0);
		SharedPreferences.Editor editor = settings.edit();
		editor.putString("accountName", account.name);
		editor.commit();
		accountManager.manager.getAuthToken(account, AUTH_TOKEN_TYPE, true,
				new AccountManagerCallback<Bundle>() {

					public void run(AccountManagerFuture<Bundle> future) {
						try {
							Bundle bundle = future.getResult();
							if (bundle.containsKey(AccountManager.KEY_INTENT)) {
								Intent intent = bundle
										.getParcelable(AccountManager.KEY_INTENT);
								intent.setFlags(intent.getFlags()
										& ~Intent.FLAG_ACTIVITY_NEW_TASK);
								startActivityForResult(intent,
										REQUEST_AUTHENTICATE);
							} else if (bundle
									.containsKey(AccountManager.KEY_AUTHTOKEN)) {
								accessProtectedResource.setAccessToken(bundle
										.getString(AccountManager.KEY_AUTHTOKEN));
								onAuthToken();
							}
						} catch (Exception e) {
							handleException(e);
						}
					}
				}, null);
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		switch (requestCode) {
		case REQUEST_AUTHENTICATE:
			if (resultCode == RESULT_OK) {
				gotAccount(false);
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
				gotAccount(true);
				return;
			}
		}		
	}

	public void onAuthToken() {
		try {
			List<Task> tasks = service.tasks.list("@default").execute().getItems();
			if (tasks != null) {
				for (Task task : tasks) {
					Log.d("SyncTaskActivity", task.getTitle());

					LocalTask t = new LocalTask(task);
					((TaskManagerApplication) getApplication()).addTask(t);
				}
			} else {
				//unused - no tasks
			}

		} catch (IOException e) {
			handleException(e);
		}
		finish();
	}

}
