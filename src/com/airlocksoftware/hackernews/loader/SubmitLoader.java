package com.airlocksoftware.hackernews.loader;

import java.io.IOException;

import android.util.Log;
import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.airlocksoftware.hackernews.activity.SubmitActivity.SendMode;
import com.airlocksoftware.hackernews.data.ConnectionManager;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.model.Result;

public class SubmitLoader extends AsyncTaskLoader<Result> {



	final String mSelfText, mUrl, mTitle;
	final SendMode mSendMode;

	// Constants
	private static final String TAG = SubmitLoader.class.getSimpleName();
	private static final String REPLY_EXTENSION = "/r";

	// Don't care about HTTP vs HTTPS
	private static final String NEWEST_PAGE = "://news.ycombinator.com/newest";

	// Match substrings for error messages
	private static final String MATCH_POST_TOO_FAST = "submitting too fast";

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
	public Result loadInBackground() {
		if (mSendMode == SendMode.EMPTY) return Result.EMPTY;

		Result result = Result.FAILURE;

		UserPrefs data = new UserPrefs(getContext());
		try {

			String replyFnid = getReplyFnid(data);
			Connection.Response response = sendSubmission(data, replyFnid);

			boolean success = validateResponse(response);
			if (success) result = Result.SUCCESS;

		} catch (Exception e) {
			// any exception here probably means we have NO_CONNECTION or there's an error with the website.
			e.printStackTrace();
		}

		return result;
	}

	private boolean validateResponse(Connection.Response res) {

		// This used to work
		if (res.statusCode() == 302 && res.headers().get("Location").equals("newest")) {
			mErrorMessage = ErrorMessage.POST_SUCCESS;

		// This currently works
		} else if (res.statusCode() == 200 && res.url().toString().contains(NEWEST_PAGE)) {
			mErrorMessage = ErrorMessage.POST_SUCCESS;

		// If you post too fast, HN complains
		} else if (res.body().contains(MATCH_POST_TOO_FAST)) {
			mErrorMessage = ErrorMessage.POST_TOO_FAST;
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