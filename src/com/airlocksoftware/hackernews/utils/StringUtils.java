package com.airlocksoftware.hackernews.utils;

/**
 * Created by matthewbbishop on 11/24/13.
 */
public class StringUtils {

  public static boolean isNotBlank(String string) {
    return !isBlank(string);
  }

  public static boolean isBlank(String string) {
    return string == null || string.length() <= 0;
  }

  /**
   * Imported from commons.lang, available under the Apache license.
   */
  public static String capitalize(final String str) {
    int strLen;
    if (str == null || (strLen = str.length()) == 0) {
      return str;
    }

    char firstChar = str.charAt(0);
    if (Character.isTitleCase(firstChar)) {
      // already capitalized
      return str;
    }

    return new StringBuilder(strLen)
            .append(Character.toTitleCase(firstChar))
            .append(str.substring(1))
            .toString();
  }

}
