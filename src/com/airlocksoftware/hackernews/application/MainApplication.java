package com.airlocksoftware.hackernews.application;

import android.app.Application;
import android.content.Context;

/**
 * MainApplication :: Main Application class for interacting
 * with application outside of activities; helpful for getting
 * shared preferences outside of context scope, i.e. ConnectionManager
 */
public class MainApplication extends Application {
	private static MainApplication mInstance;

	@Override
	public void onCreate() {
		super.onCreate();
		mInstance = this;
	}

	public static MainApplication getInstance() {
		return mInstance;
	}
}
