package com.airlocksoftware.v3.otto;

import com.airlocksoftware.v3.actionbar.MainFragmentTab;

/**
 * Created by matthewbbishop on 12/20/13.
 */
public class TabSelectedEvent {

  private MainFragmentTab mPage;

  public TabSelectedEvent(MainFragmentTab page) {
    mPage = page;
  }

  public MainFragmentTab getPage() {
    return mPage;
  }
}
