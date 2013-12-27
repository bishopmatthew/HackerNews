package com.airlocksoftware.hackernews.loader;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.airlocksoftware.hackernews.activity.SubmitActivity.SendMode;
import com.airlocksoftware.hackernews.data.ConnectionManager;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.model.Result;
import com.airlocksoftware.v3.api.Api;

public class SubmitLoader extends AsyncTaskLoader<Result> {

  final String mSelfText, mUrl, mTitle;

  final SendMode mSendMode;

  // Constants
  private static final String REPLY_EXTENSION = "/r";

  private static final String NEWEST_PAGE = "http://news.ycombinator.com/newest";

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
    if (mSendMode == SendMode.EMPTY) {
      return Result.EMPTY;
    }

    Result result = Result.FAILURE;

    UserPrefs data = new UserPrefs(getContext());
    try {

      String replyFnid = getReplyFnid(data);
      Connection.Response response = sendSubmission(data, replyFnid);
      boolean success = validateResponse(response);
      if (success) {
        result = Result.SUCCESS;
      }

    } catch (Exception e) {
      // any exception here probably means we have NO_CONNECTION or there's an error with the website.
      e.printStackTrace();
    }

    return result;
  }

  private boolean validateResponse(Connection.Response response) {
    // this used to work
    boolean success = response.statusCode() == 302 && response.headers()
            .get("Location")
            .equals("newest");
    // this currently works
    success = success || response.statusCode() == 200 && response.url()
            .toString()
            .equals(NEWEST_PAGE);
    return success;
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
    return ConnectionManager.authConnect(Api.SUBMIT_URL, data.getUserCookie())
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