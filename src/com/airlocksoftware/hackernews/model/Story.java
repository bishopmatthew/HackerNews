package com.airlocksoftware.hackernews.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.airlocksoftware.database.SqlObject;
import com.airlocksoftware.hackernews.activity.LoginActivity;
import com.airlocksoftware.hackernews.activity.LoginActivity.PostAction;
import com.airlocksoftware.hackernews.cache.DbHelperSingleton;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.loader.AsyncVotingService;

@SuppressWarnings("serial")
/** Encapsulates data about a Story (e.g. articles on the front page). Since it's extending SqlObject, any public, non-static, 
 * non-transient field is cached in the database**/
public class Story extends SqlObject {

	public long storyId;
	public int position;
	public String whence;
	public String url;
	public String title;
	public String domain;
	public int numPoints;
	public String username;
	public int numComments;
	public String ago;
	public String selfText;
	public boolean isUpvoted;

	// data used for caching purposes
	public String auth;
	public Page page;

	// is the Story archived (i.e. can't reply or comment)
	public boolean isArchived = false; // by default

	// constant values need to match field names above
	public static final String STORY_ID = "storyId";
	public static final String POSITION = "position";
	public static final String WHENCE = "go_to";
	public static final String URL = "url";
	public static final String TITLE = "title";
	public static final String DOMAIN = "domain";
	public static final String NUM_POINTS = "numPoints";
	public static final String USERNAME = "username";
	public static final String NUM_COMMENTS = "numComments";
	public static final String AGO = "ago";
	public static final String SELF_TEXT = "selfText";
	public static final String IS_UPVOTED = "isUpvoted";

	public static final String AUTH = "auth";
	public static final String PAGE = "page";

	public static final String IS_ARCHIVED = "isArchived";

	@Override
	public String toString() {
		String toReturn = "";
		toReturn += "postition: " + position;
		toReturn += "\nstoryId: " + storyId;
		if (whence != null) toReturn += "\ngo_to: " + whence;
		if (url != null) toReturn += "\nurl: " + url;
		if (title != null) toReturn += "\ntitle: " + title;
		if (domain != null) toReturn += "\ndomain: " + domain;
		toReturn += "\npoints: " + numPoints;
		if (username != null) toReturn += "\nuser: " + username;
		toReturn += "\ncomments: " + numComments;
		if (ago != null) toReturn += "\nago: " + ago;
		if (auth != null) toReturn += "\nauth: " + auth;
		if (selfText != null && !selfText.equals("")) {
			toReturn += "\nselfText: " + "yes";
		}
		{
			toReturn += "\nselfText: " + "no";
		}
		return toReturn;
	}

	public boolean upvote(Context context) {
		UserPrefs data = new UserPrefs(context);
		if (data.isLoggedIn()) {
			// create the vote and save it to database
			Vote vote = new Vote();
			vote.auth = auth;
			vote.username = data.getUsername();
			vote.whence = whence;
			vote.itemId = storyId;
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
			intent.putExtra(LoginActivity.POST_STORY, this);
			context.startActivity(intent);
			return false;
		}
	}

	public boolean create(SQLiteDatabase db) {
		return super.createAndGenerateId(db);
	}

	public static Story cachedById(SQLiteDatabase db, long sId) {
		Story story = new Story();
		Cursor c = db.query(story.getTableName(), story.getColNames(), STORY_ID + "=?",
				new String[] { Long.toString(sId) }, null, null, null);

		if (c.moveToFirst()) {
			story.readFromCursor(c);
			c.close();
			return story;
		} else {
			c.close();
			return null;
		}

	}

	/** Gets stories linked to this page from the cache **/
	public static List<Story> cachedByPage(SQLiteDatabase db, Page Page) {
		Story firstStory = new Story();
		List<Story> stories = new ArrayList<Story>();
		Cursor c = db.query(firstStory.getTableName(), firstStory.getColNames(), PAGE + "=?",
				new String[] { Page.toString() }, null, null, null);
		if (c.moveToFirst()) {
			firstStory.readFromCursor(c);
			stories.add(firstStory);
			c.moveToNext();
			for (int i = 1; i < c.getCount(); i++) {
				Story story = new Story();
				story.readFromCursor(c);
				stories.add(story);
				c.moveToNext();
			}
		}

		c.close();
		return stories;
	}

	/** Deletes any cached rows matching page **/
	public static void clearCache(SQLiteDatabase db, Page page) {
		Story story = new Story();
		db.delete(story.getTableName(), PAGE + "=?", new String[] { page.toString() });
	}

	/** Adds these values to the cache, copying over replyFnid & commentsTimestamp **/
	public static void cacheValues(SQLiteDatabase db, List<Story> stories) {
		for (Story story : stories) {
			story.create(db);
		}
	}

	/** Checks the story for attributes which indicate it is a YCombinator job post & doesn't have a comments page. **/
	public static boolean isYCombinatorJobPost(Story story) {

		boolean isJobPost = story != null && StringUtils.isNotBlank(story.title) && StringUtils.isNotBlank(story.url)
				&& story.storyId == 0;
		// TODO this is optional if the above doesn't work well
		// isJobPost = isJobPost && StringUtils.isBlank(story.username) && StringUtils.isBlank(story.ago);
		return isJobPost;
	}

}
