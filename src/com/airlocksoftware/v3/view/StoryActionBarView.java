package com.airlocksoftware.v3.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.FrameLayout;

import com.airlocksoftware.hackernews.R;
import com.devspark.robototextview.widget.RobotoTextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import timber.log.Timber;

/**
 * Created by matthewbbishop on 12/16/13.
 */
public class StoryActionBarView extends FrameLayout {

  @InjectView(R.id.spin_story_title) RobotoTextView mStoryTypeSpinner;

  public StoryActionBarView(Context context) {
    this(context, null);
  }

  public StoryActionBarView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public StoryActionBarView(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);
    LayoutInflater.from(context).inflate(R.layout.vw_story_actionbar, this);
    ButterKnife.inject(this, this);

    Timber.d("Found RobotoTextView = " + mStoryTypeSpinner);
  }
}
