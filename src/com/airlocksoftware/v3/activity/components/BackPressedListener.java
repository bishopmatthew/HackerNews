package com.airlocksoftware.v3.activity.components;

/**
 * Listener so that components can register with BackPressedManager (in an Activity) and receive onBackPressed() events
 * Created by matthewbbishop on 12/21/13.
 */
public abstract class BackPressedListener {

  /** The priority with which this listener should be called with respect to all others. 0 is max priority, default
   * is Integer.MAX_VALUE (min priority) **/
  public int getPriority() {
    return Integer.MAX_VALUE;
  }

  public abstract boolean onBackPressed();

}
