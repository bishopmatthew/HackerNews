package com.airlocksoftware.hackernews.activity;

import com.airlocksoftware.hackernews.utils.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.fragment.AboutUserFragment;
import com.airlocksoftware.hackernews.fragment.SubmissionsFragment;
import com.airlocksoftware.hackernews.fragment.ThreadsFragment;
import com.airlocksoftware.hackernews.interfaces.SharePopupInterface;
import com.airlocksoftware.hackernews.view.SharePopup;
import com.airlocksoftware.holo.actionbar.interfaces.ActionBarClient;
import com.airlocksoftware.holo.adapters.FragmentPagerArrayAdapter;
import com.airlocksoftware.holo.checkable.CheckableView;
import com.airlocksoftware.holo.checkable.CheckableViewManager;
import com.airlocksoftware.holo.checkable.CheckableViewManager.OnCheckedViewChangedListener;
import com.airlocksoftware.holo.utils.ViewUtils;
import com.slidingmenu.lib.SlidingMenu;

public class UserActivity extends SlideoutMenuActivity implements SharePopupInterface {

  private String mUsername;

  private int mCurrentPosition = 0;

  private ViewPager mPager;

  private LinearLayout mTabContainer;

  private SharePopup mSharePopup;

  private FragmentPagerArrayAdapter mAdapter;

  private CheckableViewManager mTabManager;

  public static final String USERNAME = UserActivity.class.getSimpleName() + ".username";

  @Override
  public void onCreate(Bundle savedState) {
    super.onCreate(savedState);
    retrieveBundles(savedState, getIntent().getExtras());
    setContentView(R.layout.act_user);
    findViews();
    setupTabs();
    setupSlidingMenu();
    mAdapter = new FragmentPagerArrayAdapter(getSupportFragmentManager());
    setupFragments();
    mPager.setAdapter(mAdapter);
    mSharePopup = new SharePopup(this, null, getOverlayManager());

    // make sure ActionBar is setup & first tab is checked
    pageListener.onPageSelected(0);
  }

  private void setupSlidingMenu() {
    SlidingMenu menu = super.getSlidingMenu();

    // disable touchmode so that ViewPager works
    menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);

    // change username to match current user
    setActiveMenuItem(R.id.user_button);
    ((TextView) menu.findViewById(R.id.txt_user)).setText(mUsername);
  }

  private void setupFragments() {
    Fragment aboutUserFrag = new AboutUserFragment();
    Bundle aboutUserArgs = new Bundle();
    aboutUserArgs.putString(AboutUserFragment.USERNAME, mUsername);
    aboutUserFrag.setArguments(aboutUserArgs);
    mAdapter.addItem(aboutUserFrag);

    Fragment submissionsFragment = new SubmissionsFragment();
    Bundle submissionsArgs = new Bundle();
    submissionsArgs.putString(SubmissionsFragment.USERNAME, mUsername);
    submissionsFragment.setArguments(submissionsArgs);
    mAdapter.addItem(submissionsFragment);

    Fragment threadsFragment = new ThreadsFragment();
    Bundle threadsArgs = new Bundle();
    threadsArgs.putString(ThreadsFragment.USERNAME, mUsername);
    threadsFragment.setArguments(threadsArgs);
    mAdapter.addItem(threadsFragment);
  }

  private void setupTabs() {
    mTabManager = new CheckableViewManager();
    for (View tab : ViewUtils.directChildViews(mTabContainer)) {
      mTabManager.register((CheckableView) tab);
    }
    mTabManager.setOnCheckedChangedListener(tabListener);
    mPager.setOnPageChangeListener(pageListener);
  }

  private void findViews() {
    mPager = (ViewPager) findViewById(R.id.viewpager);
    mTabContainer = (LinearLayout) findViewById(R.id.cnt_tabs);
  }

  private void retrieveBundles(Bundle savedState, Bundle extras) {
    if (extras != null) {
      mUsername = extras.getString(USERNAME);
    }
    if (savedState != null) {
      mUsername = savedState.getString(USERNAME);
    }
    if (StringUtils.isBlank(mUsername)) {
      throw new RuntimeException("Username passed to UserActivity is blank!");
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(USERNAME, mUsername);
  }

  OnPageChangeListener pageListener = new ViewPager.OnPageChangeListener() {
    @Override
    public void onPageScrollStateChanged(int arg0) {
      // do nothing
    }

    @Override
    public void onPageScrolled(int arg0, float arg1, int arg2) {
      // do nothing
    }

    @Override
    public void onPageSelected(int position) {
      ((ActionBarClient) mAdapter.getItem(mCurrentPosition)).cleanupActionBar(UserActivity.this,
              getActionBarView().getController());
      ((ActionBarClient) mAdapter.getItem(position)).setupActionBar(UserActivity.this,
              getActionBarView().getController());

      mCurrentPosition = position;
      mTabManager.protectedCheck(mTabManager.getChildAt(position)
              .getId());
    }
  };

  private OnCheckedViewChangedListener tabListener = new OnCheckedViewChangedListener() {
    @Override
    public void onCheckedViewChanged(CheckableViewManager manager, int newIndex, int oldIndex) {
      mPager.setCurrentItem(newIndex);
    }
  };

  public static void startUserActivity(Context context, String username) {
    Intent intent = new Intent(context, UserActivity.class);
    intent.putExtra(UserActivity.USERNAME, username);
    context.startActivity(intent);
  }

  @Override
  public SharePopup getSharePopup() {
    return mSharePopup;
  }

}
