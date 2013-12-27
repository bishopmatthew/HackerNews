package com.airlocksoftware.v3.api;

/**
 * Created by matthewbbishop on 12/26/13.
 */
public class Api {

  public static final String BASE_URL = "https://news.ycombinator.com";
  public static final String ITEMS_URL = "/item?id=";
  public static final String THREADS_URL = "/threads?id=";
  public static final String SUBMISSIONS_URL = "/submitted?id=";
  public static final String SUBMIT_URL = "/submit";
  public static final String USER_AGENT = System.getProperty("http.agent");
  public static final int TIMEOUT_MILLIS = 40 * 1000;

  /**
   * Converts an id into a string containing the extension (everything that goes after .com) of the URL *
   */
  public static String itemIdToUrlExtension(long id) {
    return ITEMS_URL + Long.toString(id);
  }

  /**
   * Converts an id into a string the full URL for that id. *
   */
  public static String itemIdToUrl(long id) {
    return BASE_URL + itemIdToUrlExtension(id);
  }
}
