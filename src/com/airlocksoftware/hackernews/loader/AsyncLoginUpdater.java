package com.airlocksoftware.hackernews.loader;

import java.util.Calendar;

import com.airlocksoftware.hackernews.utils.StringUtils;

import android.content.Context;
import android.os.AsyncTask;

import com.airlocksoftware.hackernews.data.LoginManager;
import com.airlocksoftware.hackernews.data.UserPrefs;

/**
 * Responsible for updating the user cookie in the background. It gets started by the MainActivity, and should run once
 * a week.
 */
public class AsyncLoginUpdater extends AsyncTask<Void, Void, Void> {

  Context mApplicationContext;

  public static final long COOKIE_EXPIRATION = 1000 * 60 * 60 * 24 * 7; // one weeks

  /**
   * Be sure to use an Application context in case of rotation happening while this is running *
   */
  public AsyncLoginUpdater(Context applicationContext) {
    mApplicationContext = applicationContext;
  }

  @Override
  protected Void doInBackground(Void... arg0) {
    UserPrefs prefs = new UserPrefs(mApplicationContext);
    long timestamp = prefs.getCookieTimestamp();
    String username = prefs.getUsername();
    String password = prefs.getPassword();
    long currentTime = Calendar.getInstance()
            .getTimeInMillis();
    boolean expired = currentTime - timestamp > COOKIE_EXPIRATION;
    if (expired && StringUtils.isNotBlank(username) && StringUtils.isNotBlank(password)) {
      String newCookie = LoginManager.login(username, password);
      if (newCookie != null) {
        // saves new user cookie and updates the timestamp
        prefs.saveUserCookie(newCookie);
      }
    }
    return null;
  }

}
