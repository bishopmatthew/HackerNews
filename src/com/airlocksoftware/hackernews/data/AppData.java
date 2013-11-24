package com.airlocksoftware.hackernews.data;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * TODO
 *
 * @author matthewbbishop
 */
public class AppData {

    // State
    private Context mContext;
    private SharedPreferences mPrefs;
    private SharedPreferences.Editor mEditor;

    // Constants
    public static final String PREFS_NAME = AppData.class.getSimpleName() + ".data";
    public static final String STORY_LIST_POSITION = AppData.class.getSimpleName() + ".story_list_position";

    public AppData(Context context) {
        mContext = context;
        mPrefs = mContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        mEditor = mPrefs.edit();
    }

    public void saveStoryListPosition(int position) {
        mEditor.putInt(STORY_LIST_POSITION, position);
        mEditor.commit();
    }

    public int getStoryListPosition() {
        int position = mPrefs.getInt(STORY_LIST_POSITION, 0);
        return position;
    }

}
