package com.airlocksoftware.hackernews.data;

import com.airlocksoftware.v3.api.Api;

import org.jsoup.Connection;
import org.jsoup.Jsoup;

/**
 * Static methods for connecting to the http://news.ycombinator.com *
 */
public class ConnectionManager {

  /**
   * Connects to news.ycombinator.com using the cookie you've provided *
   */
  public static Connection authConnect(String baseUrlExtension, String userCookie) {
    return anonConnect(baseUrlExtension).cookie("user", userCookie);
  }

  /**
   * Connects to news.ycombinator.com with no user cookie authentication *
   */
  public static Connection anonConnect(String baseUrlExtension) {
    return Jsoup.connect(Api.BASE_URL + baseUrlExtension)
            .timeout(Api.TIMEOUT_MILLIS)
            .userAgent(Api.USER_AGENT);
  }

}
