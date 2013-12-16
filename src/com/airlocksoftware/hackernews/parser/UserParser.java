package com.airlocksoftware.hackernews.parser;

import java.io.IOException;

import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;

import android.util.Log;

import com.airlocksoftware.hackernews.data.ConnectionManager;
import com.airlocksoftware.hackernews.model.User;

public class UserParser {

  private static final String TAG = "UserParser";

  public static User parseUser(String username) {
    try {

      User user = new User();
      user.username = username;

      // don't use user cookie so that "about" text appears correctly
      Document page = ConnectionManager.anonConnect("/user?id=" + username)
              .get();
      Elements trs = page.select("form > table > tbody > tr");

      user.created = trs.select("td:containsOwn(created:) + td")
              .first()
              .text();
      user.karma = Integer.parseInt(trs.select("td:containsOwn(karma:) + td")
              .first()
              .text());
      try {
        user.avg = Float.parseFloat(trs.select("td:containsOwn(avg:) + td")
                .first()
                .text());
      } catch (Exception e) {
        user.avg = -1.0f;
      }

      user.aboutHtml = trs.select("td:containsOwn(about:) + td")
              .first()
              .html();

      return user;
    } catch (IOException e) {
      e.printStackTrace();
      Log.d(TAG, "IOException parsing UserModel for: " + username);
      return null;
    } catch (NumberFormatException e) {
      e.printStackTrace();
      Log.d(TAG, "NumberFormatException parsing UserModel for: " + username);
      return null;
    } catch (NullPointerException e) {
      e.printStackTrace();
      Log.d(TAG, "NullPointerException parsing UserModel for: " + username);
      return null;
    }
  }

}
