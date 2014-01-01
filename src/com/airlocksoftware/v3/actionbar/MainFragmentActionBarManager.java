package com.airlocksoftware.v3.actionbar;

import android.app.ActionBar;
import android.content.Context;
import android.content.res.Resources;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;
import android.view.View;

import com.airlocksoftware.v3.activity.components.BackPressedListener;
import com.airlocksoftware.v3.activity.components.BackPressedManager;
import com.airlocksoftware.v3.dagger.ForActivity;
import com.airlocksoftware.v3.fragment.MainFragment;
import com.airlocksoftware.v3.otto.TabSelectedEvent;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import static com.airlocksoftware.v3.actionbar.MainFragmentTab.COMMENTS;
import static com.airlocksoftware.v3.actionbar.MainFragmentTab.STORIES;
import static com.airlocksoftware.v3.actionbar.MainFragmentTab.fromPagerPosition;

/**
 * Manages the interactions between the 3 fragments that are part of the ViewPager in MainFragment and the ActionBar.
 *
 * Created by matthewbbishop on 12/17/13.
 */
public class MainFragmentActionBarManager implements ViewPager.OnPageChangeListener, ActionBarTabsView.OnTabClickListener {

  private final MainFragment mMainFragment;

  @Inject @ForActivity Context mContext;

  @Inject ActionBar mActionBar;
  private final View mActionBarView;
  private final ActionBarTabsView mActionBarTabs;
  @Inject ActionBarDrawerToggle mDrawerToggle;

  private final ViewPager mViewPager;
  private final MainFragment.Adapter mAdapter;

  @Inject BackPressedManager mBackPressedManager;

  private int mLastPosition = 0;

  public MainFragmentActionBarManager(MainFragment mainFragment, View decorView, ViewPager viewPager,
                                      MainFragment.Adapter adapter) {
    mMainFragment = mainFragment;
    mViewPager = viewPager;
    mAdapter = adapter;

    mMainFragment.inject(this);

    /* Find action bar content view (i.e. everything but the background) */
    int actionBarId = Resources.getSystem().getIdentifier("action_bar", "id", "android");
    mActionBarView = decorView.findViewById(actionBarId);

    /* initialize the action bar content */
    mAdapter.getItem(0).setHasOptionsMenu(true);
    
    /* Add the tabs even though they will not be shown until setDisplayShowCustomEnabled() */
    mActionBarTabs = new ActionBarTabsView(mContext, this);
    mActionBar.setCustomView(mActionBarTabs);

    mBackPressedManager.addListener(mBackPressedListener);
  }

  @Override public void onPageScrollStateChanged(int state) {
    switch(state) {
      case ViewPager.SCROLL_STATE_IDLE:
        /* make sure alpha and underline are at correct values */
        mActionBarView.setAlpha(quadraticAlphaEasing(0));
        MainFragmentTab page = MainFragmentTab.fromPagerPosition(mViewPager.getCurrentItem());
        mActionBarTabs.transformUnderline(page == MainFragmentTab.ARTICLE ? 1 : 0);
        break;
    }
  }
  
  @Override public void onPageSelected(int position) {
    /* Switch the fragment that gets to display it's menu */
    mAdapter.getItem(mLastPosition).setHasOptionsMenu(false);
    mAdapter.getItem(position).setHasOptionsMenu(true);
    mLastPosition = position;

    /* Switch the nav -- Spinner & drawer indicator on STORIES, Tabs & up indicator on COMMENTS and ARTICLE */
    if(MainFragmentTab.fromPagerPosition(position) == STORIES) {
      mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_LIST);
      mActionBar.setDisplayShowCustomEnabled(false);
      mDrawerToggle.setDrawerIndicatorEnabled(true);
    } else {
      mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
      mDrawerToggle.setDrawerIndicatorEnabled(false);

      /* We don't show the tabs if the story doesn't have a url */
      mActionBar.setDisplayShowCustomEnabled(mAdapter.getCount() >= 3);
    }

    /* Ensure that the ActionBar is shown */
    if(!mActionBar.isShowing()) mActionBar.show();
  }
  
  @Override public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    /* Action bar content fades between the stories and comments pages (since the action bar is totally changed) */
    MainFragmentTab page = fromPagerPosition(position);
    if(page == STORIES) {
      mActionBarView.setAlpha(quadraticAlphaEasing(positionOffset));
    } else if(page == COMMENTS) {
      /* Transform the underline on the ActionBarTabsView */
      mActionBarTabs.transformUnderline(positionOffset);
    }
  }

  /** Quadratic easying function that fits (1, 0), (0.5, 0), (1, 1) */
  private static float quadraticAlphaEasing(float x) {
    return (4 * (x * x)) - (4 * x) + 1;
  }

  @Subscribe public void onTabClick(TabSelectedEvent ev) {
    mViewPager.setCurrentItem(MainFragmentTab.toPagerPosition(ev.getPage()));
  }

  /** When one of the tabs is click, changed the current item of the ViewPager **/
  @Override public void onTabClick(MainFragmentTab page) {
    mViewPager.setCurrentItem(MainFragmentTab.toPagerPosition(page));
  }

  /** So we can control what happens when the home button is clicked, depending on the current position of the
   * ViewPager **/
  public boolean onOptionsItemSelected(MenuItem item) {
    switch(item.getItemId()) {
      case android.R.id.home:
        mViewPager.setCurrentItem(0);
        return true;
      default:
        return false;
    }
  }

  private BackPressedListener mBackPressedListener = new BackPressedListener() {
    @Override public boolean onBackPressed() {
      if(mLastPosition > 0) {
        mViewPager.setCurrentItem(0);
        return true;
      }
      return false;
    }
  };

}
