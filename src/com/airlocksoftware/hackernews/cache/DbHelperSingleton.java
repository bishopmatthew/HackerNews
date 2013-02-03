package com.airlocksoftware.hackernews.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.airlocksoftware.hackernews.model.Comment;
import com.airlocksoftware.hackernews.model.Story;
import com.airlocksoftware.hackernews.model.Timestamp;
import com.airlocksoftware.hackernews.model.Vote;

/**
 * create custom DatabaseHelper class that extends SQLiteOpenHelper
 */
public class DbHelperSingleton extends SQLiteOpenHelper {

	private static DbHelperSingleton mInstance = null;
	
	private Context mContext;
	
	private static final String DATABASE_NAME = "hacker_news_cache.db";
	private static final int DATABASE_VERSION = 2;

	public static DbHelperSingleton getInstance(Context context) {
		/**
		 * use the application context as suggested by CommonsWare.
		 * this will ensure that you dont accidentally leak an Activitys
		 * context (see this article for more information:
		 * http://developer.android.com/resources/articles/avoiding-memory-leaks.html)
		 */
		if (mInstance == null) {
			mInstance = new DbHelperSingleton(context.getApplicationContext());
		}
		return mInstance;
	}

	/**
	 * constructor should be private to prevent direct instantiation.
	 * make call to static factory method "getInstance()" instead.
	 */
	private DbHelperSingleton(Context context) {
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.mContext = context;
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		new Story().createTable(db);
		new Comment().createTable(db);
		new Timestamp().createTable(db);
		new Vote().createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + new Story().getTableName());
		db.execSQL("DROP TABLE IF EXISTS " + new Comment().getTableName());
		db.execSQL("DROP TABLE IF EXISTS " + new Timestamp().getTableName());
		db.execSQL("DROP TABLE IF EXISTS " + new Vote().getTableName());

		onCreate(db);
	}
}