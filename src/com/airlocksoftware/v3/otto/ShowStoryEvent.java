package com.airlocksoftware.v3.otto;

import com.airlocksoftware.hackernews.model.Story;

/**
 * Created by matthewbbishop on 12/8/13.
 */
public class ShowStoryEvent {

  private Story mStory;

  public ShowStoryEvent(Story story) {
    mStory = story;
  }

  public Story getStory() {
    return mStory;
  }
}
