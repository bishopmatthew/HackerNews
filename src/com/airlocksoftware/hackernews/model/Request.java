package com.airlocksoftware.hackernews.model;

public enum Request {
	NEW, MORE, REFRESH, EMPTY;
	/**
	 * When you don't have any good data to make a request on, use Request.EMPTY. All of the loaders will then return
	 * Result.EMPTY
	 **/

	public boolean isEmpty() {
		return this.equals(Request.EMPTY);
	}

	public static boolean isEmpty(Request req) {
		return req.isEmpty();
	}
}
