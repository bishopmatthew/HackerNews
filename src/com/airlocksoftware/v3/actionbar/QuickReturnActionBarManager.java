package com.airlocksoftware.v3.actionbar;

import android.app.ActionBar;
import android.content.Context;
import android.widget.AbsListView;
import android.widget.ListView;

/**
 * Manages the "Quick Return" design pattern for the ActionBar and a ListView. If the list is scrolling down, we hide
 * the ActionBar, and if it's scrolling up we show it.
 * Created by matthewbbishop on 12/23/13.
 */
public class QuickReturnActionBarManager implements AbsListView.OnScrollListener {

  private Context mContext;
  private final ActionBar mActionBar;
  private ListView mListView;

  /* The first visible position last reported by onScroll, used to check if we're scrolling up or down */
  private int mLastFirstVisibleItem;

  public QuickReturnActionBarManager(Context context, ActionBar actionBar, ListView listView) {
    mContext = context;
    mActionBar = actionBar;
    mListView = listView;
    mListView.setOnScrollListener(this);
  }

  @Override public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
    final int currentFirstVisibleItem = mListView.getFirstVisiblePosition();

    /* Scrolling down */
    if (currentFirstVisibleItem > mLastFirstVisibleItem)
      mActionBar.hide();
    /* Scrolling up */
    else if (currentFirstVisibleItem < mLastFirstVisibleItem)
      mActionBar.show();

    mLastFirstVisibleItem = currentFirstVisibleItem;
  }

  @Override public void onScrollStateChanged(AbsListView view, int scrollState) { /* no op */ }
}
