package com.airlocksoftware.v3.viewpager;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.view.ViewPager.OnPageChangeListener;

/**
 * An implementation of ViewPager.OnPageChangeListener that dispatches events to the children that have been added
 * to it's List of listeners.
 *
 * Created by matthewbbishop on 12/17/13.
 */
public class MultiOnPageChangeListener implements OnPageChangeListener {

  private List<OnPageChangeListener> mListeners = new ArrayList<OnPageChangeListener>();

  public void addListener(OnPageChangeListener listener) {
    mListeners.add(listener);
  }

  public void removeListener(OnPageChangeListener listener) {
    mListeners.remove(listener);
  }

  public void clearListeners() {
    mListeners.clear();
  }

  @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    for(OnPageChangeListener listener : mListeners) {
      listener.onPageScrolled(position, positionOffset, positionOffsetPixels);
    }
  }

  @Override public void onPageSelected(int position) {
    for(OnPageChangeListener listener : mListeners) {
      listener.onPageSelected(position);
    }
  }

  @Override public void onPageScrollStateChanged(int state) {
    for(OnPageChangeListener listener : mListeners) {
      listener.onPageScrollStateChanged(state);
    }
  }
}
