package com.airlocksoftware.hackernews.model;

import com.airlocksoftware.database.DbInterface;
import com.airlocksoftware.database.SqlObject;

/**
 * When the user upvotes something, the Vote is added to the Votes table and AsyncVotingService runs (which attempts to
 * make and uncompleted votes).
 **/
public class Vote extends SqlObject {

	public long itemId;
	public String username;
	public String auth;
	public String whence;

	public boolean success;

	public Vote() {
	}

	public boolean create(DbInterface db) {
		return super.create(db);
	}

}
