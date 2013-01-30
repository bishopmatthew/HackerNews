package com.airlocksoftware.hackernews.model;

import android.database.Cursor;

import com.airlocksoftware.database.DbInterface;
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

	public boolean create(DbInterface db) {
		return super.create(db);
	}

	public static Timestamp cachedByPrimaryId(DbInterface db, String id) {
		Timestamp stamp = new Timestamp();
		Cursor c = db.getDb()
									.query(stamp.getTableName(), stamp.getColNames(), PRIMARY_ID + "=?", new String[] { id }, null, null,
											null);

		if (c.moveToFirst()) {
			stamp.readFromCursor(c);
			c.close();
			return stamp;
		} else {
			c.close();
			return null;
		}
	}

	public static Timestamp cachedByBothIds(DbInterface db, String pId, String sId) {
		Timestamp stamp = new Timestamp();
		Cursor c = db.getDb()
									.query(stamp.getTableName(), stamp.getColNames(), PRIMARY_ID + "=? AND " + SECONDARY_ID + "=?",
											new String[] { pId, sId }, null, null, null);

		if (c.moveToFirst()) {
			stamp.readFromCursor(c);
		}

		c.close();
		return (stamp.primaryId != null) ? stamp : null;
	}

	public static void clearCache(DbInterface db, String pId, String sId) {
		Timestamp timestamp = new Timestamp();
		db.getDb()
			.delete(timestamp.getTableName(), PRIMARY_ID + "=? AND " + SECONDARY_ID + "=?", new String[] { pId, sId });
	}
}
