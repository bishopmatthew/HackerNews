package com.airlocksoftware.v3.viewpager;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.FrameLayout;

/**
 * Does the same basic job as a ViewPager.PageTransformer, but is also aware of which page it's on & thus can do
 * different transforms for different positions.
 * Created by matthewbbishop on 12/15/13.
 */
public class PageWrangler implements ViewPager.OnPageChangeListener {

  private static final float MAX_OVERLAY_OPACITY = 0.6f;
  private static final float MIN_SCALE = 0.85f;

  private final ViewPager mViewPager;

  public PageWrangler(ViewPager viewPager) {
    mViewPager = viewPager;
  }

  @Override public void onPageSelected(int position) { /* no op */ }

  @Override public void onPageScrollStateChanged(int state) {
    switch(state) {
      case ViewPager.SCROLL_STATE_IDLE:
        View current = mViewPager.getChildAt(mViewPager.getCurrentItem());
        current.setScaleX(1);
        current.setScaleY(1);
        current.setAlpha(1);
        break;
    }
  }

  @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    /* Find the views being switched between */
    View left = mViewPager.getChildAt(position);
    View right = null;
    if(position + 1 < mViewPager.getChildCount()) right = mViewPager.getChildAt(position + 1);

    /* Do different animations based on which page we're on. */
    switch(position) {
      case 0:{
        pageOverlaySlipAnim(left, right, positionOffset, positionOffsetPixels);
      }
      case 1:{
        /* no op (default transform) */
      }
    }
  }

  private void pageOverlaySlipAnim(View left, View right, float positionOffset, int positionOffsetPixels) {
    
    /* Translate the left view (StoryFragment) opposite the way the ViewPager is moving it s.t. it stays stationary */
    left.setTranslationX(positionOffsetPixels);

    /* Left view is a FrameLayout, so we use setForeground to add a black overlay atop it. The opacity of the overlay
     * increases proportional to position */
    FrameLayout frame = (FrameLayout) left;
    if(frame.getForeground() == null) frame.setForeground(new ColorDrawable(Color.BLACK));
    Drawable foreground = frame.getForeground();
    int alpha = (int) (255 * MAX_OVERLAY_OPACITY * positionOffset);
    foreground.setAlpha(alpha);

    /* Scale the child of left view so that the foreground overlay still fills the space */
    View child = frame.getChildAt(0);
    float scaleFactor = 1 + MIN_SCALE * positionOffset - positionOffset;
    child.setScaleX(scaleFactor);
    child.setScaleY(scaleFactor);

  }


}
