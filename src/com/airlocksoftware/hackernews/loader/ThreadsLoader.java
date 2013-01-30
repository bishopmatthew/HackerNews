package com.airlocksoftware.hackernews.loader;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.airlocksoftware.database.DbInterface;
import com.airlocksoftware.hackernews.cache.CacheDbOpener;
import com.airlocksoftware.hackernews.model.Request;
import com.airlocksoftware.hackernews.model.Result;
import com.airlocksoftware.hackernews.model.Timestamp;
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
		CacheDbOpener opener = new CacheDbOpener(getContext());
		DbInterface db = new DbInterface(getContext(), opener);

		ThreadsResponse response = null;

		// check for more
		Timestamp timestamp = Timestamp.cachedByPrimaryId(db, CommentsParser.THREAD_TIMESTAMP_ID);
		if (mRequest == Request.MORE && timestamp != null && timestamp.secondaryId.equals(mUsername)) {
			response = CommentsParser.parseThreadsPage(getContext(), mUsername, timestamp.fnid);
		}

		// either this is a new request or we have no moreFnid
		if (response == null || response.result == Result.FAILURE || response.result == Result.FNID_EXPIRED) {
			response = CommentsParser.parseThreadsPage(getContext(), mUsername);
		}

		// delete the old one if it exists
		if (timestamp != null) timestamp.delete(db);

		// need to cache new moreFnid
		if (response.timestamp != null) {
			response.timestamp.create(db);
		}

		opener.close();
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
