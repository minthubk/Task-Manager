package com.ulysses.taskmanager.controller;

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
import com.ulysses.taskmanager.R;
import com.ulysses.taskmanager.model.LocalTask;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class AccountLoaderAsync extends AsyncTask<Object, Object, Tasks> {

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
	
	
	private Context context;
	
	@Override
	protected Tasks doInBackground(Object... params) {
		
		this.context 	= (Context) params[0]; 
		
		service = new Tasks(transport, accessProtectedResource,	new JacksonFactory());
		service.setKey(ClientCredentials.KEY);
		service.setApplicationName("Google Tasks");
		accountManager = new GoogleAccountManager(context);		
		
		return service;
	}
	

		
}
