package com.airlocksoftware.hackernews.loader;

import java.io.IOException;

import org.jsoup.Connection;
import org.jsoup.Connection.Method;
import org.jsoup.nodes.Document;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.AsyncTask;

import com.airlocksoftware.hackernews.cache.DbHelperSingleton;
import com.airlocksoftware.hackernews.data.ConnectionManager;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.model.Vote;

/**
 * When the user performs a vote, it gets added to the Votes table and this service attempts to run. If successful, it
 * removes the vote from the queue. Otherwise it leaves it there to run in the future.
 **/
public class AsyncVotingService extends AsyncTask<Void, Void, Void> {

	private static final String BAD_UPVOTE_RESPONSE = "Can't make that vote.";
	private Context mApplicationContext;

	public AsyncVotingService(Context applicationContext) {
		mApplicationContext = applicationContext;
	}

	@Override
	protected Void doInBackground(Void... empty) {
		UserPrefs prefs = new UserPrefs(mApplicationContext);
		SQLiteDatabase db = DbHelperSingleton.getInstance(mApplicationContext)
																					.getWritableDatabase();
		Cursor c = db.rawQuery("SELECT * FROM " + new Vote().getTableName(), null);

		if (c.moveToFirst()) {
			for (int i = 0; i < c.getCount(); i++) {
				Vote vote = new Vote();
				vote.readFromCursor(c);
				if (upvote(vote, prefs.getUserCookie())) {
					// if successful, remove vote from queue
					vote.delete(db);

					// TODO if a vote has already happened, response is the same
				}
				c.moveToNext();
			}
		}
		c.close();
		return null;
	}

	@Override
	protected void onCancelled(Void empty) {

	}

	private boolean upvote(Vote vote, String cookie) {
		String voteUrl = getVoteUrl(vote);
		Connection connection = ConnectionManager.authConnect(voteUrl, cookie);
		try {
			Connection.Response response = getResponse(connection);
			if (response.statusCode() == 200) {
				if (response.body() != null) return true;
				Document doc = response.parse();
				String text = doc.text();
				return !text.equals(BAD_UPVOTE_RESPONSE);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return false;
	}

	private Connection.Response getResponse(Connection connection) throws IOException {
		return connection.method(Method.GET)
											.timeout(ConnectionManager.TIMEOUT_MILLIS)
											.execute();
	}

	private String getVoteUrl(Vote vote) {
		return "/vote?for=" + Long.toString(vote.itemId) + "&dir=up&by=" + vote.username + "&auth=" + vote.auth
				+ "&go_to=" + vote.whence;
	}
}
