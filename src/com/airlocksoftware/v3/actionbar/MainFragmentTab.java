package com.airlocksoftware.v3.actionbar;

/**
 * Encapsulates which page corresponds to which position in the MainFragment ViewPager. This makes it explicit (rather
 * than using the implicit ordering of MainFragmentPager).
 * Created by matthewbbishop on 12/17/13.
 */
public enum MainFragmentTab {

  STORIES, COMMENTS, ARTICLE;

  public static MainFragmentTab fromPagerPosition(int pagerPosition) {
    switch (pagerPosition) {
      case 0:
        return STORIES;
      case 1:
        return COMMENTS;
      case 2:
        return ARTICLE;
      default:
        throw new IllegalStateException("Couldn't find " + pagerPosition + " in MainFragmentTab lookup table");
    }
  }

  public static int toPagerPosition(MainFragmentTab tab) {
    switch (tab) {
      case STORIES:
        return 0;
      case COMMENTS:
        return 1;
      case ARTICLE:
        return 2;
      default:
        throw new IllegalStateException("Couldn't find " + tab + " in MainFragmentTab lookup table");
    }
  }

}
