package com.airlocksoftware.hackernews.data;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

/** Static methods for connecting to the http://news.ycombinator.com **/
public class ConnectionManager {

	public static final String BASE_URL = "http://news.ycombinator.com";
	public static final String ITEMS_URL = "/item?id=";
	public static final String THREADS_URL = "/threads?id=";
	public static final String SUBMISSIONS_URL = "/submitted?id=";
	public static final String SUBMIT_URL = "/submit";
	public static final String USER_AGENT = System.getProperty("http.agent");
	public static final int TIMEOUT_MILLIS = 20 * 1000; // 20 seconds

	/** Connects to news.ycombinator.com using the cookie you've provided **/
	public static Connection authConnect(String baseUrlExtension, String userCookie) {
		return anonConnect(baseUrlExtension).cookie("user", userCookie);
	}

	/** Connects to news.ycombinator.com with no user cookie authorization **/
	public static Connection anonConnect(String baseUrlExtension) {
		return Jsoup.connect(ConnectionManager.BASE_URL + baseUrlExtension)
								.timeout(TIMEOUT_MILLIS)
								.userAgent(ConnectionManager.USER_AGENT);
	}

	public static String itemIdToUrl(long id) {
		return ITEMS_URL + Long.toString(id);
	}

}
