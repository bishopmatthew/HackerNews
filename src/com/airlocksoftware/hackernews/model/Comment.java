package com.airlocksoftware.hackernews.model;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import com.airlocksoftware.database.DbUtils;
import com.airlocksoftware.database.SqlObject;
import com.airlocksoftware.hackernews.activity.LoginActivity;
import com.airlocksoftware.hackernews.activity.LoginActivity.PostAction;
import com.airlocksoftware.hackernews.cache.DbHelperSingleton;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.loader.AsyncVotingService;

import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
/** Encapsulates data about a comment. Since it's extending SqlObject, any public, non-static, 
 * non-transient field is cached in the database**/
public class Comment extends SqlObject {

	public long commentId;
	public String username;
	public String ago;
	public String html;
	public int depth;
	public String replyUrl;
	public String whence;
	public String auth;
	public boolean isUpvoted = false;
	public long storyId;

	public static final String COMMENT_ID = "commentId";
	public static final String USERNAME = "username";
	public static final String AGO = "ago";
	public static final String HTML = "html";
	public static final String DEPTH = "depth";
	public static final String REPLY_URL = "replyUrl";
	public static final String WHENCE = "go_to";
	public static final String AUTH = "auth";
	public static final String IS_UPVOTED = "isUpvoted";
	public static final String IS_FOLDED = "isFolded";
	public static final String CHILD_COUNT = "childCount";
	public static final String STORY_ID = "storyId";

	public static final long CACHE_EXPIRATION = 1000 * 60 * 30; // 30 minutes
	public static final long JAN_1_2012 = 1325376000000L; // expected minimum date
	private static final String TAG = Comment.class.getSimpleName();

	// used to cache the generated html -- NOTE: transient tells the ORM not to store it
	private transient Spanned mSpannedHtml = null;

	// used to hold folded comments
	public transient boolean isFolded = false;
	public transient int mChildCount = 0;
	public transient List<Comment> mChildren;

	public Comment() {
		// default constructor
	}

	public Spanned generateSpannedHtml() {
		if (mSpannedHtml == null && html != null) mSpannedHtml = Html.fromHtml(html);
		return mSpannedHtml;
	}

	public boolean upvote(Context context) {
		UserPrefs data = new UserPrefs(context);
		if (data.isLoggedIn()) {
			// create the vote and save it to database
			Vote vote = new Vote();
			vote.auth = auth;
			vote.username = data.getUsername();
			vote.whence = whence;
			vote.itemId = commentId;
			SQLiteDatabase db = DbHelperSingleton.getInstance(context)
																						.getWritableDatabase();
			vote.create(db);

			// update comments upvote status
			isUpvoted = true;
			update(db);

			// run async voting service
			AsyncVotingService service = new AsyncVotingService(context);
			service.execute();

			return true;
		} else {
			Intent intent = new Intent(context, LoginActivity.class);
			intent.putExtra(LoginActivity.POST_ACTION, PostAction.UPVOTE);
			intent.putExtra(LoginActivity.POST_COMMENT, this);
			context.startActivity(intent);

			return false;
		}
	}

	public boolean create(SQLiteDatabase db) {
		return super.createAndGenerateId(db);
	}

	/** Loads a comment from the cache base on on it's commentId **/
	public static Comment readFromCommentId(SQLiteDatabase db, long cId) {
		Comment comment = new Comment();
		Cursor c = db.query(comment.getTableName(), comment.getColNames(), COMMENT_ID + "=?",
				new String[] { Long.toString(cId) }, null, null, null);

		if (c.moveToFirst()) {
			comment.readFromCursor(c);
		}

		c.close();
		return comment;
	}

	/** Loads a list of comments form the cache based on the parent storyId. **/
	public static List<Comment> getFromCache(SQLiteDatabase db, long sId) {
		Comment firstComment = new Comment();
		List<Comment> comments = null;
		Cursor c = db.query(firstComment.getTableName(), firstComment.getColNames(), STORY_ID + "=?",
				new String[] { Long.toString(sId) }, null, null, null);
		if (c.moveToFirst()) {
			comments = new ArrayList<Comment>();
			firstComment.readFromCursor(c);
			comments.add(firstComment);
			c.moveToNext();
			for (int i = 1; i < c.getCount(); i++) {
				Comment comment = new Comment();
				comment.readFromCursor(c);
				comments.add(comment);
				c.moveToNext();
			}
		}

		c.close();
		return comments;
	}

	/** Deletes any cached comment rows matching storyId **/
	public static void clearCache(SQLiteDatabase db, String sId) {
		Comment comment = new Comment();
		db.delete(comment.getTableName(), STORY_ID + "=?", new String[] { sId });
	}

	/**
	 * Adds these values to the cache, and deletes any expired comments
	 * 
	 * @param timestamp
	 **/
	public static void cacheValues(SQLiteDatabase db, List<Comment> comments, CommentsTimestamp timestamp) {
		// make sure we have a comment to run queries against
		if (comments == null || comments.size() < 1) return;
		Comment first = comments.get(0);
		if (first == null) first = new Comment();

		// delete any old comments (Timestamp.TIME < System.currentTimeMillis() - CACHE_EXPIRATION)
		Cursor c = db.query(timestamp.getTableName(), timestamp.getColNames(), StoryTimestamp.TIME + "<? AND " + StoryTimestamp.TIME
				+ ">? ",
				new String[] { Long.toString(System.currentTimeMillis() - CACHE_EXPIRATION), Long.toString(JAN_1_2012) }, null,
				null, null);

		if (c.moveToFirst()) {
			for (int i = 1; i < c.getCount(); i++) {
				StoryTimestamp ts = new StoryTimestamp();
				ts.readFromCursor(c);
				c.moveToNext();

				Comment.clearCache(db, ts.secondaryId);
				ts.delete(db);
				Log.d(TAG, "Deleting comments with storyId=" + ts.secondaryId);
			}
		}

		// delete old
		StoryTimestamp toDelete = new StoryTimestamp();
		toDelete.id = DbUtils.getId(db, timestamp.getTableName(), StoryTimestamp.SECONDARY_ID, timestamp.secondaryId);
		toDelete.delete(db);

		// create new
		timestamp.create(db); // should replace old value or insert it

		// cache new comments
		for (Comment comment : comments) {
			comment.create(db);
		}
	}
}
