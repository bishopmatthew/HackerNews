package com.airlocksoftware.hackernews.data;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.apache.commons.lang3.StringUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;

/** Encapsulates methods for interacting with SharedPreferences that the user's preferences. **/
public class UserPrefs {

	// State
	private Context mContext;
	private SharedPreferences mPrefs;
	private SharedPreferences.Editor mEditor;

	// Constants
	public static final String PREFS_NAME = UserPrefs.class.getSimpleName() + ".hn_user_data";
	public static final String USERNAME = UserPrefs.class.getSimpleName() + ".username";
	public static final String PASSWORD = UserPrefs.class.getSimpleName() + ".password";
	public static final String USER_COOKIE = UserPrefs.class.getSimpleName() + ".user_cookie";
	public static final String USER_COOKIE_TIMESTAMP = UserPrefs.class.getSimpleName() + ".user_cookie_timestamp";

	public static final String BUGSENSE_ENABLED = UserPrefs.class.getSimpleName() + ".bugsense_enabled";
	public static final String OPEN_IN_BROWSER = UserPrefs.class.getSimpleName() + ".open_in_browser";
	public static final String THEME = UserPrefs.class.getSimpleName() + ".theme";
	public static final String SEARCH_SORT_TYPE = UserPrefs.class.getSimpleName() + ".searchSortType";
	public static final String SEARCH_TYPE = UserPrefs.class.getSimpleName() + ".searchType";

	public static final String USE_COUNT = UserPrefs.class.getSimpleName() + ".useCount";
	public static final String LAST_USE = UserPrefs.class.getSimpleName() + ".lastUse";
	public static final String SHOW_GIVE_BACK = UserPrefs.class.getSimpleName() + ".showGiveBack";

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
	 * Resets account data (doesn't notify news.ycombinator that cookie is invalid, though.
	 * I'm not sure if that's an issue?). Would need to capture Logout button fnid
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
		LIGHT, DARK;
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
