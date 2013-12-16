package com.airlocksoftware.hackernews.data;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

import com.airlocksoftware.hackernews.utils.StringUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Encapsulates methods for interacting with SharedPreferences that the user's preferences. *
 */
public class UserPrefs {

  public static final String NAME = UserPrefs.class.getSimpleName();

  // State
  private Context mContext;

  private SharedPreferences mPrefs;

  private SharedPreferences.Editor mEditor;

  // Constants
  public static final String PREFS_NAME = NAME + ".hn_user_data";

  public static final String USERNAME = NAME + ".username";

  public static final String PASSWORD = NAME + ".password";

  public static final String USER_COOKIE = NAME + ".user_cookie";

  public static final String USER_COOKIE_TIMESTAMP = NAME + ".user_cookie_timestamp";

  public static final String BUGSENSE_ENABLED = NAME + ".bugsense_enabled";

  public static final String OPEN_IN_BROWSER = NAME + ".open_in_browser";

  private static final String OPEN_IN_NEW_TAB = NAME + ".open_in_new_tab";

  public static final String THEME = NAME + ".theme";

  public static final String SEARCH_SORT_TYPE = NAME + ".searchSortType";

  public static final String SEARCH_TYPE = NAME + ".searchType";

  public static final String USE_COUNT = NAME + ".useCount";

  public static final String LAST_USE = NAME + ".lastUse";

  public static final String SHOW_GIVE_BACK = NAME + ".showGiveBack";

  @SuppressLint("SimpleDateFormat")
  public static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd");

  public UserPrefs(Context context) {
    mContext = context;
    mPrefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    mEditor = mPrefs.edit();
  }

  public void saveUsername(String username) {
    if (username != null) {
      mEditor.putString(USERNAME, username);
    } else {
      mEditor.remove(USERNAME);
    }
    mEditor.commit();
  }

  public void savePassword(String password) {
    if (password != null) {
      mEditor.putString(PASSWORD, password);
    } else {
      mEditor.remove(PASSWORD);
    }
    mEditor.commit();
  }

  public void saveUserCookie(String toSet) {
    if (toSet != null) {
      mEditor.putString(USER_COOKIE, toSet);
      mEditor.putLong(USER_COOKIE_TIMESTAMP, getCurrentTime());
    } else {
      mEditor.remove(USER_COOKIE);
    }
    mEditor.commit();
  }

  public String getUsername() {
    String username = mPrefs.getString(USERNAME, null);
    return username;
  }

  public String getPassword() {
    String password = mPrefs.getString(PASSWORD, null);
    return password;
  }

  public String getUserCookie() {
    String cookie = mPrefs.getString(USER_COOKIE, null);
    return cookie;
  }

  /**
   * Resets account data (doesn't notify news.ycombinator that cookie is invalid, though. I'm not sure if that's an
   * issue?). Would need to capture Logout button fnid
   */
  public void logout() {
    saveUserCookie(null);
    savePassword(null);
    saveUsername(null);
  }

  private long getCurrentTime() {
    Calendar cal = Calendar.getInstance();
    Long time = cal.getTimeInMillis();
    return time;
  }

  public boolean isLoggedIn() {
    return StringUtils.isNotBlank(getUserCookie());
  }

  public boolean getBugsenseEnabled() {
    boolean enabled = mPrefs.getBoolean(BUGSENSE_ENABLED, true);
    return enabled;
  }

  public void saveBugsenseEnabled(boolean enabled) {
    mEditor.putBoolean(BUGSENSE_ENABLED, enabled);
    mEditor.commit();
  }

  public boolean getOpenInBrowser() {
    boolean enabled = mPrefs.getBoolean(OPEN_IN_BROWSER, false);
    return enabled;
  }

  public void saveOpenInBrowser(boolean enabled) {
    mEditor.putBoolean(OPEN_IN_BROWSER, enabled);
    mEditor.commit();
  }

  public boolean getOpenInNewTab() {
    boolean enabled = mPrefs.getBoolean(OPEN_IN_NEW_TAB, true);
    return enabled;
  }

  public void saveOpenInNewTab(boolean enabled) {
    mEditor.putBoolean(OPEN_IN_NEW_TAB, enabled);
    mEditor.commit();
  }

  // public boolean showContributeActivity() {
  // boolean show = false;
  //
  // String today = DATE_FORMAT.format(new Date());
  // int useCount = mPrefs.getInt(USE_COUNT, 0);
  // String lastUse = mPrefs.getString(LAST_USE, today);
  // boolean showGiveBack = mPrefs.getBoolean(SHOW_GIVE_BACK, true);
  //
  // if (lastUse != today) {
  // // update last use & use count
  // useCount += 1;
  // lastUse = DATE_FORMAT.format(new Date());
  // mEditor.putInt(USE_COUNT, useCount);
  // mEditor.putString(LAST_USE, lastUse);
  // mEditor.commit();
  //
  // // show if it hasn't been shown today, showGiveBack is true, and number of days used is divisible by 5.
  // show = showGiveBack && (useCount % 5 == 0);
  // }
  //
  // return show;
  // }

  // public void updateShowGiveBack(boolean showGiveBack) {
  // mEditor.putBoolean(SHOW_GIVE_BACK, showGiveBack);
  // mEditor.commit();
  // }

  // public boolean getDonationEnabled() {
  // return false;
  // }

  public enum Theme {
    LIGHT, DARK
  }

  public Theme getTheme() {
    Theme theme = Theme.values()[mPrefs.getInt(THEME, Theme.LIGHT.ordinal())];
    return theme;
  }

  public void setTheme(Theme theme) {
    mEditor.putInt(THEME, theme.ordinal());
    mEditor.commit();
  }

  public long getCookieTimestamp() {
    return mPrefs.getLong(USER_COOKIE_TIMESTAMP, 0);
  }

}
