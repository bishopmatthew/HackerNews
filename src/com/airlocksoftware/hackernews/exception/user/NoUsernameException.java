package com.airlocksoftware.hackernews.exception.user;

import com.airlocksoftware.hackernews.exception.HackerNewsException;

/**
 * NoUsernameException :: Less crashes, more error handling from Story Loader
 *
 * Author:  pkillian
 * Project: HackerNews2
 * Date:    1/11/14
 */
public class NoUsernameException extends HackerNewsException {
	public NoUsernameException() {
		super();
	}

	public NoUsernameException(String message) {
		super(message);
	}
}
