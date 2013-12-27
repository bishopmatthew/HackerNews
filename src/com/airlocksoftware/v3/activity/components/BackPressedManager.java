package com.airlocksoftware.v3.activity.components;

import java.util.Comparator;
import java.util.PriorityQueue;

/**
 * Allows BackPressedListeners to register with it to provide onBackPressed() calls from an activity, handled according
 * to their priority. BackPressedManager should be created as a singleton in an Activity, and onBackPressed() in the
 * Activity should be passed through to this so it can handle it (or not).
 * Created by matthewbbishop on 12/21/13.
 */
public class BackPressedManager {

  private final Comparator<BackPressedListener> mComparator = new Comparator<BackPressedListener>(){
    @Override public int compare(BackPressedListener lhs, BackPressedListener rhs) {
      return lhs.getPriority() - rhs.getPriority();
    }
  };

  private final PriorityQueue<BackPressedListener> mListeners = new PriorityQueue<BackPressedListener>(3, mComparator);

  public BackPressedManager() {
  }

  public boolean onBackPressed() {
    for(BackPressedListener listener : mListeners) {
      if(listener.onBackPressed()) return true;
    }
    return false;
  }

  public void addListener(BackPressedListener backPressedListener) {
    mListeners.add(backPressedListener);
  }

  public void removeListener(BackPressedListener backPressedListener){
    mListeners.remove(backPressedListener);
  }

  public void clearListeners() {
    mListeners.clear();
  }
}
