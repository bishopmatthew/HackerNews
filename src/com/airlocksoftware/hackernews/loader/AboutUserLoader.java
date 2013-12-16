package com.airlocksoftware.hackernews.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.airlocksoftware.hackernews.model.User;
import com.airlocksoftware.hackernews.parser.UserParser;

/**
 * Downloads and parses a user's about page in the background via UserParser.parseUser() *
 */
public class AboutUserLoader extends AsyncTaskLoader<User> {

  String mUsername;

  public AboutUserLoader(Context context, String username) {
    super(context);
    mUsername = username;
  }

  @Override
  public User loadInBackground() {
    if (mUsername == null) {
      return null;
    }
    return UserParser.parseUser(mUsername);
  }

  /**
   * Handles a request to start the Loader.
   */
  @Override
  protected void onStartLoading() {
    forceLoad();
  }

}
