package com.airlocksoftware.hackernews.loader;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.v4.content.AsyncTaskLoader;
import com.airlocksoftware.hackernews.cache.DbHelperSingleton;
import com.airlocksoftware.hackernews.fragment.CommentsFragment;
import com.airlocksoftware.hackernews.model.*;
import com.airlocksoftware.hackernews.parser.CommentsParser;
import com.airlocksoftware.hackernews.parser.CommentsParser.CommentsResponse;

import java.util.List;

/**
 * Loads the comments page specified by storyId. If possible, tries to load the data from the db cache. Otherwise it
 * downloads the HTML of the comments page, parses it, caches it, and returns the data as a CommentsResponse.
 **/
public class CommentsLoader extends AsyncTaskLoader<CommentsResponse> {

	public Request mRequest;
	public long mStoryId;

	public CommentsLoader(Context context, Request request, long storyId) {
		super(context);
		mStoryId = storyId;
		mRequest = request;
	}

	@Override
	public CommentsResponse loadInBackground() {

		if (mStoryId == CommentsFragment.NO_STORY_ID || mStoryId == 0) {
			// either this is a first run or a YCombinator jobs post
			return new CommentsResponse(Result.EMPTY);
		}

		SQLiteDatabase db = DbHelperSingleton.getInstance(getContext()).getWritableDatabase();
		CommentsResponse response = null;

		List<Comment> comments = null;
		Story story = null;
		CommentsTimestamp timestamp = null;

		if (mRequest == Request.NEW) {
			story = Story.cachedById(db, mStoryId);
			comments = Comment.getFromCache(db, mStoryId);
			timestamp = CommentsTimestamp.cachedByBothIds(db, CommentsParser.COMMENT_TIMESTAMP_ID, Long.toString(mStoryId));

			if (story != null && comments != null && comments.size() > 0 && timestamp != null) {
				response = new CommentsResponse(Result.SUCCESS);
				response.comments = comments;
				response.timestamp = timestamp;
				response.story = story;
			}
		}

		if (response == null) {
			response = CommentsParser.parseCommentsPage(getContext(), mStoryId);
			if (response.result != Result.FAILURE) {
				// if the story has selfText, copy it to story & save
				if (story != null) {
					response.story.position = story.position;
					response.story.id = story.id;
					response.story.update(db);
				} else response.story.create(db);

				// cache the comments
				Comment.cacheValues(db, response.comments, response.timestamp);
			}
		}

		// generate the Spanned html
		for (Comment c : response.comments) {
			c.generateSpannedHtml();
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

	@Override
	public void deliverResult(CommentsResponse response) {
		if (isReset()) {
			// An async query came in while the loader is stopped. We
			// don't need the result.
		}

		if (isStarted()) {
			// If the Loader is currently started, we can immediately
			// deliver its results.
			super.deliverResult(response);
		}
	}

	/**
	 * Handles a request to stop the Loader.
	 */
	@Override
	protected void onStopLoading() {
		// Attempt to cancel the current load task if possible.
		cancelLoad();
	}

	/**
	 * Handles a request to cancel a load.
	 */
	@Override
	public void onCanceled(CommentsResponse response) {
		super.onCanceled(response);
	}

	/**
	 * Handles a request to completely reset the Loader.
	 */
	@Override
	protected void onReset() {
		super.onReset();
	}

}
