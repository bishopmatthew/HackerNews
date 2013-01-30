package com.airlocksoftware.hackernews.cache;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.airlocksoftware.database.DbOpener;
import com.airlocksoftware.hackernews.model.Comment;
import com.airlocksoftware.hackernews.model.Story;
import com.airlocksoftware.hackernews.model.Timestamp;
import com.airlocksoftware.hackernews.model.Vote;

/** Extends the DatabaseUtils class DbOpener to handle the naming and versioning of the cache database. **/
public class CacheDbOpener extends DbOpener {

	private static final int DATABASE_VERSION = 2;
	private static final String DATABASE_NAME = "hacker_news_cache.db";
	private static final String TAG = CacheDbOpener.class.getSimpleName();

	public CacheDbOpener(Context context) {
		super(context, DATABASE_NAME, DATABASE_VERSION);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		Log.d(TAG, "onCreate()");
		new Story().createTable(db);
		new Comment().createTable(db);
		new Timestamp().createTable(db);
		new Vote().createTable(db);
	}

	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		Log.d(TAG, "onUpgrade()");
		db.execSQL("DROP TABLE IF EXISTS " + new Story().getTableName());
		db.execSQL("DROP TABLE IF EXISTS " + new Comment().getTableName());
		db.execSQL("DROP TABLE IF EXISTS " + new Timestamp().getTableName());
		db.execSQL("DROP TABLE IF EXISTS " + new Vote().getTableName());
		
		onCreate(db);
	}

}
