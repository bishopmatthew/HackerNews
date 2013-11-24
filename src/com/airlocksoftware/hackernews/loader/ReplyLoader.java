package com.airlocksoftware.hackernews.loader;

import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Element;

import android.content.Context;
import android.support.v4.content.AsyncTaskLoader;

import com.airlocksoftware.hackernews.data.ConnectionManager;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.model.Result;

/**
 * Handles sending replies to Comments and Stories. *
 */
public class ReplyLoader extends AsyncTaskLoader<Result> {

    String mText;
    long mId = -1;

    private static final String REPLY_EXTENSION = "/r";

    /**
     * no data passed, not ready to send *
     */
    public ReplyLoader(Context context) {
        super(context);
    }

    public ReplyLoader(Context context, long id, String text) {
        super(context);
        mId = id;
        mText = text;
    }

    @Override
    public Result loadInBackground() {
        // no data passed, not ready to send
        if (mId == -1 || StringUtils.isBlank(mText)) return Result.EMPTY;

        Result result = Result.FAILURE; // default

        try {
            UserPrefs data = new UserPrefs(getContext());
            Element replyInput = getReplyInput(data);
            String replyFnid = replyInput.attr("value");
            String response = sendReply(data, replyFnid);
            if (StringUtils.isNotBlank(response)) result = Result.SUCCESS;

        } catch (Exception e) {
            // any exception here probably means we have NO_CONNECTION or there's an error with the website.
            e.printStackTrace();
        }

        return result;
    }

    /**
     * POSTS the reply*
     */
    private String sendReply(UserPrefs data, String replyFnid) throws IOException {
        return ConnectionManager.authConnect(REPLY_EXTENSION, data.getUserCookie())
                .data("fnid", replyFnid)
                .data("text", mText)
                .method(Method.POST)
                .execute()
                .parse()
                .text();
    }

    private Element getReplyInput(UserPrefs data) throws IOException {
        return ConnectionManager.authConnect(ConnectionManager.itemIdToUrlExtension(mId), data.getUserCookie())
                .get()
                .select("input[name=fnid]")
                .first();
    }

    /**
     * Handles a request to start the Loader.
     */
    @Override
    protected void onStartLoading() {
        forceLoad();
    }
}
