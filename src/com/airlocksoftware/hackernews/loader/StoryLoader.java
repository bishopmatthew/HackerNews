package com.airlocksoftware.hackernews.loader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;
import android.util.Log;
import com.airlocksoftware.hackernews.cache.DbHelperSingleton;
import com.airlocksoftware.hackernews.model.*;
import com.airlocksoftware.hackernews.parser.StoryParser;
import com.airlocksoftware.hackernews.parser.StoryParser.StoryResponse;
import com.crashlytics.android.Crashlytics;
import org.apache.commons.lang3.StringUtils;

import java.util.List;

public class StoryLoader extends AsyncTaskLoader<StoryResponse> {

	private Page mPage;
	private Request mRequest;
	private String mUsername;
	// private String mMoreFnid;
	private StoryResponse mResponse;
	private boolean mResultsToDeliver = false;

	// Constants
	private static final String TAG = StoryLoader.class.getSimpleName();
	private static final String SUBMISSIONS_TIMESTAMP_ID = "Submissions";
	private static final String PAGE_TIMESTAMP_ID = "Page";

	/** Parse stories from Front Page, Ask, Best, or New **/
	public StoryLoader(Context context, Page page, Request request) {
		super(context);

		mPage = page;
		mRequest = request;
		mUsername = null;

		setCrashlyticsKeys();
	}

	/** Parse stories from the user's submissions page **/
	public StoryLoader(Context context, String username, Request request) {
		super(context);
		mPage = null;
		mRequest = request;
		mUsername = username;

		setCrashlyticsKeys();
	}

	private void setCrashlyticsKeys() {
		String pageString = (mPage != null) ? mPage.toString() : null;
		boolean usernameBlank = (mRequest == null || StringUtils.isBlank(mUsername));
		boolean reqEmpty = (mRequest == null || mRequest.isEmpty());

		Crashlytics.setString("StoryLoader :: mPage", pageString);
		Crashlytics.setBool("StoryLoader :: mUsernameBlank?", usernameBlank);
		Crashlytics.setBool("StoryLoader :: mRequestEmpty?", reqEmpty);
	}

	@Override
	public StoryResponse loadInBackground() {
		StoryResponse response = StoryResponse.NULL_RESPONSE;

		if (mUsername != null) response = loadSubmissions(mUsername, mRequest);
		else if (mPage != null) response = loadStories(mPage, mRequest);

		return response;
	}

	/** Loads the requested page of stories, either from news.ycombinator.com or from the cache. **/
	private StoryResponse loadStories(Page page, Request request) {

		SQLiteDatabase db = DbHelperSingleton.getInstance(getContext()).getWritableDatabase();
		StoryResponse response = null;

		// handle cache
		StoryTimestamp timestamp = StoryTimestamp.cachedByBothIds(db, PAGE_TIMESTAMP_ID, page.toString());
		if (timestamp != null && request == Request.NEW) {
			List<Story> stories = Story.cachedByPage(db, page);
			if (stories.size() > 0) {
				response = new StoryResponse();
				response.stories = stories;
				response.result = Result.SUCCESS;
				response.timestamp = timestamp;
			}
		}

		// no hit in the cache or it's a MORE or REFRESH request
		if (response == null) {
			String moreFnid = null;
			if (request == Request.MORE && timestamp != null) moreFnid = timestamp.fnid;
			response = StoryParser.parseStoryList(getContext(), page, request, moreFnid);

			switch (response.result) {
			case SUCCESS:
				Story.clearCache(db, page);
				Story.cacheValues(db, response.stories);
				break;
			case MORE:
				Story.cacheValues(db, response.stories);
				break;
			case FAILURE:
				if (moreFnid != null) response.result = Result.FNID_EXPIRED;
			default:
				break;
			}

			// need to cache new moreFnid
			if (response.timestamp != null) {
				// delete old one
				if (timestamp != null) timestamp.delete(db);

				// create new one
				response.timestamp.primaryId = PAGE_TIMESTAMP_ID;
				response.timestamp.secondaryId = page.toString();
				response.timestamp.create(db);

				Log.d(TAG, "new Story FNID = " + response.timestamp.fnid);
			}
		}

		mResultsToDeliver = true;
		mResponse = response;
		return response;

	}

	/** Loads StoryResposne from a user's submissions page, and caches the moreFnid (if any) in the timestamp db. **/
	private StoryResponse loadSubmissions(String username, Request request) {
		SQLiteDatabase db = DbHelperSingleton.getInstance(getContext())
																					.getWritableDatabase();

		StoryResponse response = null;

		// check for more
		StoryTimestamp timestamp = StoryTimestamp.cachedByPrimaryId(db, SUBMISSIONS_TIMESTAMP_ID);
		if (request == Request.MORE && timestamp != null && timestamp.secondaryId.equals(username)) {
			response = StoryParser.parseUserSubmissions(getContext(), username, timestamp.fnid);
		}

		// either this is a new request or we have no moreFnid
		if (response == null) {
			response = StoryParser.parseUserSubmissions(getContext(), username, null);
		}

		// delete the old one if it exists
		if (timestamp != null) timestamp.delete(db);

		// need to cache new moreFnid (if any)
		if (response.timestamp != null) {
			response.timestamp.primaryId = SUBMISSIONS_TIMESTAMP_ID;
			response.timestamp.secondaryId = username;
			response.timestamp.create(db);
		}

		mResultsToDeliver = true;
		mResponse = response;
		return response;
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		if (mResponse != null && mResultsToDeliver) {
			deliverResult(mResponse);
		} else {
			forceLoad();
		}
	}

	@Override
	public void deliverResult(StoryResponse response) {
		mResultsToDeliver = false;
		super.deliverResult(response);
	}

}
