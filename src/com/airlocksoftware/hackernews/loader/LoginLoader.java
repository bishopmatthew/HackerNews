package com.airlocksoftware.hackernews.loader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;

import com.airlocksoftware.hackernews.cache.DbHelperSingleton;
import com.airlocksoftware.hackernews.data.LoginManager;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.model.*;
import com.airlocksoftware.hackernews.model.StoryTimestamp;

/**
 * Uses static method LoginManager.login() to perform the login. Notifies LoginActivity whether or not it was
 * successful.
 **/
public class LoginLoader extends AsyncTaskLoader<Result> {

	String mUsername;
	String mPassword;

	public LoginLoader(Context context, String username, String password) {
		super(context);
		mUsername = username;
		mPassword = password;
	}

	@Override
	public Result loadInBackground() {
		if (mUsername == null || mPassword == null) return Result.EMPTY;

		String newCookie = LoginManager.login(mUsername, mPassword);
		boolean isSuccess = newCookie != null;
		if (isSuccess) {
			// saves new user cookie and updates the timestamp
			UserPrefs prefs = new UserPrefs(getContext());
			prefs.saveUserCookie(newCookie);
			prefs.saveUsername(mUsername);
			prefs.savePassword(mPassword);

			// delete all caches after logging in
			SQLiteDatabase db = DbHelperSingleton.getInstance(getContext())
																						.getWritableDatabase();

			db.delete(new Story().getTableName(), null, null);
			db.delete(new Comment().getTableName(), null, null);
			db.delete(new StoryTimestamp().getTableName(), null, null);
			db.delete(new Vote().getTableName(), null, null);

			db.close();
		}

		return isSuccess ? Result.SUCCESS : Result.FAILURE;
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		forceLoad();
	}

}
