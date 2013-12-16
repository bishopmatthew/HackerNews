package com.airlocksoftware.hackernews.model;

import android.database.sqlite.SQLiteDatabase;

import com.airlocksoftware.database.SqlObject;

/**
 * When the user upvotes something, the Vote is added to the Votes table and AsyncVotingService runs (which attempts to
 * make and uncompleted votes).
 */
public class Vote extends SqlObject {

  public long itemId;

  public String username;

  public String auth;

  public String whence;

  public boolean success;

  public Vote() {
  }

  public boolean create(SQLiteDatabase db) {
    return super.createAndGenerateId(db);
  }

}
