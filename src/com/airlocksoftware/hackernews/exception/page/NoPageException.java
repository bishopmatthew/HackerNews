package com.airlocksoftware.hackernews.exception.page;

import com.airlocksoftware.hackernews.exception.HackerNewsException;

/**
 * NoPageException ::
 *
 * Author:  pkillian
 * Project: HackerNews2
 * Date:    1/11/14
 */
public class NoPageException extends HackerNewsException {
	public NoPageException() {
		super();
	}

	public NoPageException(String message) {
		super(message);
	}
}
