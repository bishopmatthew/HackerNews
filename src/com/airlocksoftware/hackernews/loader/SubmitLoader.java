package com.airlocksoftware.hackernews.loader;

import java.io.IOException;

import android.util.Log;
import com.crashlytics.android.Crashlytics;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.airlocksoftware.hackernews.activity.SubmitActivity.SendMode;
import com.airlocksoftware.hackernews.data.ConnectionManager;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.model.NewStoryResult;

public class SubmitLoader extends AsyncTaskLoader<NewStoryResult> {

	final String mSelfText, mUrl, mTitle;
	final SendMode mSendMode;

	// Constants
	private static final String TAG = SubmitLoader.class.getSimpleName();
	private static final String REPLY_EXTENSION = "/r";

	// Don't care about HTTP vs HTTPS
	private static final String MATCH_NEWEST_PAGE = "://news.ycombinator.com/newest";

	// Match substrings for error messages
	private static final String MATCH_POST_TOO_FAST = "submitting too fast";

	// Match duplicate posts
	private static final String MATCH_DUPLICATE_PAGE = "item?id=";

	private enum ErrorMessage {
		POST_SUCCESS, POST_TOO_FAST, POST_DUPLICATE
	}

	public ErrorMessage mErrorMessage = ErrorMessage.POST_SUCCESS;

	public SubmitLoader(Context context, SendMode sendMode, String title, String content) {
		super(context);
		mSendMode = sendMode;
		switch (mSendMode) {
		case URL:
			mSelfText = "";
			mTitle = title;
			mUrl = content;
			break;
		case SELF_TEXT:
			mSelfText = content;
			mTitle = title;
			mUrl = "";
			break;
		default:
			mSelfText = mUrl = mTitle = null;
			break;
		}
	}

	@Override
	public NewStoryResult loadInBackground() {
		if (mSendMode == SendMode.EMPTY) return NewStoryResult.EMPTY;

		UserPrefs data = new UserPrefs(getContext());

		try {

			String replyFnid = getReplyFnid(data);
			Connection.Response response = sendSubmission(data, replyFnid);

			validateResponse(response);

			switch (mErrorMessage) {
			case POST_SUCCESS:
				return NewStoryResult.SUCCESS;
			case POST_DUPLICATE:
				return NewStoryResult.POST_DUPLICATE;
			case POST_TOO_FAST:
				return NewStoryResult.POST_TOO_FAST;
			default:
				return NewStoryResult.FAILURE;
			}

		} catch (Exception e) {
			// any exception here probably means we have NO_CONNECTION or there's an error with the website.
			e.printStackTrace();
		}

		return NewStoryResult.FAILURE;

	}

	private boolean validateResponse(Connection.Response res) {

		if (res == null) return false;

		if (res.headers() != null && res.headers().get("Location") != null) {
			Crashlytics.setString("SubmitLoader :: responseLocationHeader", res.headers().get("Location"));
		}

		if (res.url() != null) {
			Crashlytics.setString("SubmitLoader :: responseURL", res.url().toString());
		}

		// This used to work
		if (res.statusCode() == 302 && res.headers().get("Location").equals("newest")) {
			mErrorMessage = ErrorMessage.POST_SUCCESS;

		// This currently works
		} else if (res.statusCode() == 200 && res.url().toString().contains(MATCH_NEWEST_PAGE)) {
			mErrorMessage = ErrorMessage.POST_SUCCESS;

		// If you post too fast, HN complains
		} else if (res.body().contains(MATCH_POST_TOO_FAST)) {
			Crashlytics.setBool("SubmitLoader :: responsePostTooFast", true);
			mErrorMessage = ErrorMessage.POST_TOO_FAST;

		// If the URL contains 'item?id=', it's a duplicate post
		} else if (res.url() != null && res.url().toString().contains(MATCH_DUPLICATE_PAGE)) {
			Crashlytics.setBool("SubmitLoader :: responsePostDuplicate", true);
			mErrorMessage = ErrorMessage.POST_DUPLICATE;
		}

		// Only POST_SUCCESS is 100% clean and successful
		return mErrorMessage == ErrorMessage.POST_SUCCESS;
	}

	private Response sendSubmission(UserPrefs data, String replyFnid) throws IOException {
		return ConnectionManager.authConnect(REPLY_EXTENSION, data.getUserCookie())
														.data("fnid", replyFnid)
														.data("t", mTitle)
														.data("u", mUrl)
														.data("x", mSelfText)
														.method(Method.POST)
														.execute();
	}

	private String getReplyFnid(UserPrefs data) throws IOException {
		return ConnectionManager.authConnect(ConnectionManager.SUBMIT_URL, data.getUserCookie())
														.get()
														.select("input[name=fnid]")
														.first()
														.attr("value");
	}

	/**
	 * Handles a request to start the Loader.
	 */
	@Override
	protected void onStartLoading() {
		forceLoad();
	}
}