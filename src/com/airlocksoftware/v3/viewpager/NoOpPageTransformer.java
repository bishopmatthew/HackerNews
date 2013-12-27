package com.airlocksoftware.v3.viewpager;

import android.support.v4.view.ViewPager;
import android.view.View;

/**
 * Created by matthewbbishop on 12/15/13.
 */
public class NoOpPageTransformer implements ViewPager.PageTransformer {

  @Override public void transformPage(View page, float position) { /* no op */ }

}
