package com.airlocksoftware.hackernews.exception;

/**
 * HackerNewsException :: Generic HackerNews exception class to make catching
 *                        errors easier / more concise.
 *
 * Author:  pkillian
 * Project: HackerNews2
 * Date:    1/11/14
 */
public class HackerNewsException extends RuntimeException {
	protected HackerNewsException() {
		super();
	}

	protected HackerNewsException(String message) {
		super(message);
	}
}
