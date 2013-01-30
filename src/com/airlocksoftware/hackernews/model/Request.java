package com.airlocksoftware.hackernews.model;

public enum Request {
	NEW, MORE, REFRESH,
	/**
	 * When you don't have any good data to make a request on, use Request.EMPTY. All of the loaders will then return
	 * Result.EMPTY
	 **/
	EMPTY
}
