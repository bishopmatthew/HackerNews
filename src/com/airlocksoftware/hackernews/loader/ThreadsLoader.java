package com.airlocksoftware.hackernews.loader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;

import com.airlocksoftware.hackernews.cache.DbHelperSingleton;
import com.airlocksoftware.hackernews.model.Request;
import com.airlocksoftware.hackernews.model.Result;
import com.airlocksoftware.hackernews.model.StoryTimestamp;
import com.airlocksoftware.hackernews.parser.CommentsParser;
import com.airlocksoftware.hackernews.parser.CommentsParser.ThreadsResponse;

public class ThreadsLoader extends AsyncTaskLoader<ThreadsResponse> {

	String mUsername;
	Request mRequest;

	public ThreadsLoader(Context context, Request request, String username) {
		super(context);
		mUsername = username;
		mRequest = request;
	}

	@Override
	public ThreadsResponse loadInBackground() {
		SQLiteDatabase db = DbHelperSingleton.getInstance(getContext())
																					.getWritableDatabase();

		ThreadsResponse response = null;

		// check for more
		StoryTimestamp storyTimestamp = StoryTimestamp.cachedByPrimaryId(db, CommentsParser.THREAD_TIMESTAMP_ID);
		if (mRequest == Request.MORE && storyTimestamp != null && storyTimestamp.secondaryId.equals(mUsername)) {
			response = CommentsParser.parseThreadsPage(getContext(), mUsername, storyTimestamp.fnid);
		}

		// either this is a new request or we have no moreFnid
		if (response == null || response.result == Result.FAILURE || response.result == Result.FNID_EXPIRED) {
			response = CommentsParser.parseThreadsPage(getContext(), mUsername);
		}

		// delete the old one if it exists
		if (storyTimestamp != null) storyTimestamp.delete(db);

		// need to cache new moreFnid
		if (response.timestamp != null) {
			response.timestamp.create(db);
		}

		return response;
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		forceLoad();
	}

}
