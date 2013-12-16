package com.airlocksoftware.hackernews.activity;

import android.os.Bundle;
import android.view.View;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.holo.actionbar.ActionBarView;
import com.slidingmenu.lib.SlidingMenu;

/**
 * Extends the SlideoutMenu activity to: - finish the activity when the ActionBar "up" button is pressed - display the
 * SlidingMenu on long press
 */
public class BackActivity extends SlideoutMenuActivity {

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    SlidingMenu menu = super.getSlidingMenu();
    menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
    super.removeMenuCheckState();

    ActionBarView ab = getActionBarView();
    View upButton = ab.getUpButton();

    ab.getUpIndicator()
            .iconSource(R.drawable.ic_actionup_back);

    upButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        BackActivity.this.finish();
      }
    });

    upButton.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        toggle();
        return true;
      }
    });

  }
}
