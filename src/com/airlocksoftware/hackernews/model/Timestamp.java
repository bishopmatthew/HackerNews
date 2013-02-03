package com.airlocksoftware.hackernews.model;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.airlocksoftware.database.SqlObject;

/** Used to store the timestamp of when pages were last downloaded. **/
public class Timestamp extends SqlObject {

	public long time;
	public String fnid;
	public String primaryId;
	public String secondaryId;

	public static final String TIME = "time";
	public static final String FNID = "fnid";
	public static final String PRIMARY_ID = "primaryId";
	public static final String SECONDARY_ID = "secondaryId";

	public Timestamp() {
	}

	public boolean create(SQLiteDatabase db) {
		return super.create(db);
	}

	public static Timestamp cachedByPrimaryId(SQLiteDatabase db, String id) {
		Timestamp timestamp = new Timestamp();
		Cursor c = db.query(timestamp.getTableName(), timestamp.getColNames(), PRIMARY_ID + "=?", new String[] { id },
				null, null, null);

		if (c.moveToFirst()) {
			timestamp.readFromCursor(c);
			c.close();
			return timestamp;
		} else {
			c.close();
			return null;
		}
	}

	public static Timestamp cachedByBothIds(SQLiteDatabase db, String pId, String sId) {
		Timestamp stamp = new Timestamp();
		Cursor c = db.query(stamp.getTableName(), stamp.getColNames(), PRIMARY_ID + "=? AND " + SECONDARY_ID + "=?",
				new String[] { pId, sId }, null, null, null);

		if (c.moveToFirst()) {
			stamp.readFromCursor(c);
		}

		c.close();
		return (stamp.primaryId != null) ? stamp : null;
	}

	public static void clearCache(SQLiteDatabase db, String pId, String sId) {
		Timestamp timestamp = new Timestamp();
		db.delete(timestamp.getTableName(), PRIMARY_ID + "=? AND " + SECONDARY_ID + "=?", new String[] { pId, sId });
	}
}
