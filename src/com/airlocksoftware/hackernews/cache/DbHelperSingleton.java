package com.airlocksoftware.hackernews.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.airlocksoftware.hackernews.model.*;

/**
 * SQLiteOpenHelper singleton. Should be ok that the database is never closed as per:
 * http://www.androiddesignpatterns.com/2012/05/correctly-managing-your-sqlite-database.html
 */
public class DbHelperSingleton extends SQLiteOpenHelper {

	private static DbHelperSingleton mInstance = null;

	private static final String DATABASE_NAME = "hacker_news_cache.db";
	private static final int DATABASE_VERSION = 4;

	public static DbHelperSingleton getInstance(Context context) {
		/**
		 * Use the application context as suggested by CommonsWare.
		 * this will ensure that you don't accidentally leak an Activity's
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
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		new Story().createTable(db);
		new Comment().createTable(db);
		new StoryTimestamp().createTable(db);
		new CommentsTimestamp().createTable(db);
		new Vote().createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		db.execSQL("DROP TABLE IF EXISTS " + new Story().getTableName());
		db.execSQL("DROP TABLE IF EXISTS " + new Comment().getTableName());
		db.execSQL("DROP TABLE IF EXISTS " + new StoryTimestamp().getTableName());
		db.execSQL("DROP TABLE IF EXISTS " + new CommentsTimestamp().getTableName());
		db.execSQL("DROP TABLE IF EXISTS " + new Vote().getTableName());

		onCreate(db);
	}
}