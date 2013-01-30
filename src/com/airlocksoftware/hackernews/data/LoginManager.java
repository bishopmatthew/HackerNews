package com.airlocksoftware.hackernews.data;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Connection.Method;
import org.jsoup.Connection.Response;
import org.jsoup.nodes.Document;


public class LoginManager {

	/**
	 * Attempts to log the user into http://news.ycombinator.com. If successful, returns a user authentication cookie.
	 * Else it returns null.
	 **/
	public static String login(String username, String password) {
		try {
			Document loginPage = ConnectionManager.anonConnect("/newslogin?whence=news")
																						.get();

			String fnid = loginPage.select("input[name=fnid]")
															.attr("value");

			Response response = ConnectionManager.anonConnect("/y")
																						.data("fnid", fnid)
																						.data("u", username)
																						.data("p", password)
																						.method(Method.POST)
																						.execute();

			String cookie = response.cookie("user");
			if (StringUtils.isNotBlank(cookie)) {
				return cookie;
			}
		} catch (Exception e) {
			// connection error
		}
		return null;
	}
	
	

}
