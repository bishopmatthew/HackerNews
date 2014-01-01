package com.airlocksoftware.hackernews.model;

import android.content.Context;
import android.net.Uri;

import com.airlocksoftware.hackernews.R;

public enum Page {

  FRONT, ASK, BEST, NEW, ACTIVE, CLASSIC;
  /* potential additions (may require different parsing / handling) */
  /* BEST_COMMENTS, NEW_COMMENTS, NOOB_COMMENTS, NOOB_STORIES */

  public static Page fromListPosition(int position) {
    switch (position) {
      case 0:
        return FRONT;
      case 1:
        return ASK;
      case 2:
        return BEST;
      case 3:
        return NEW;
      case 4:
        return ACTIVE;
      case 5:
        return CLASSIC;
      default:
        throw new IllegalStateException("Couldn't find " + position + " in StoryFragmentPage lookup table");
    }
  }

  private static String[] STORY_PAGES;

  public int toListPosition() {
    switch (this) {
      case FRONT:
        return 0;
      case ASK:
        return 1;
      case BEST:
        return 2;
      case NEW:
        return 3;
      case ACTIVE:
        return 4;
      case CLASSIC:
        return 5;
      default:
        throw new IllegalStateException("Couldn't find " + this + " in StoryFragmentPage lookup table");
    }
  }

  public String toDisplayName(Context context) {
    if (STORY_PAGES == null) STORY_PAGES = context.getResources().getStringArray(R.array.story_pages);
    return STORY_PAGES[toListPosition()];
  }

  public String toPath() {
    switch(this) {
      case FRONT:
        return "/news";
      case ASK:
        return "/ask";
      case BEST:
        return "/best";
      case NEW:
        return "/new";
      case ACTIVE:
        return "/active";
      case CLASSIC:
        return "/classic";
      default:
        throw new IllegalStateException("Couldn't find " + this + " in StoryFragmentPage lookup table");

    }
  }

  public static Uri toUri(Page page) {
    return Uri.EMPTY;
  }

}
