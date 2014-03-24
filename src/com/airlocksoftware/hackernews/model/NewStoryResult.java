package com.airlocksoftware.hackernews.model;

public enum NewStoryResult {
	SUCCESS, POST_TOO_FAST, POST_DUPLICATE, FAILURE, EMPTY;

	public String toString() {
		switch (this) {
		case SUCCESS:
			return "Success";
		case POST_DUPLICATE:
			return "Duplicate Post";
		case POST_TOO_FAST:
			return "Posting Too Fast";
		case FAILURE:
			return "Failure";
		default:
			return "Empty";
		}
	}
}
