package com.airlocksoftware.v3.fragment;

import android.app.ActionBar;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.fragment.CommentsFragment;
import com.airlocksoftware.hackernews.fragment.StoryFragment;
import com.airlocksoftware.hackernews.fragment.WebFragment;
import com.airlocksoftware.hackernews.model.Story;
import com.airlocksoftware.hackernews.utils.StringUtils;
import com.airlocksoftware.v3.otto.ShowStoryEvent;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static android.app.ActionBar.Tab;

/**
 * Created by matthewbbishop on 12/7/13.
 */
public class MainFragment extends BaseFragment implements ViewPager.OnPageChangeListener {

  /* ActionBar (injected by Activity) */
  @Inject ActionBar mActionBar;

  /* ViewPager and adapter */
  @InjectView(R.id.viewpager) ViewPager mViewPager;
  private Adapter mAdapter;

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.frg_main, container, false);
    ButterKnife.inject(this, root);
    initViews();
    return root;
  }

  private void initViews() {
    initViewPager();
  }

  private void initViewPager() {
    mAdapter = new Adapter(getChildFragmentManager());
    mViewPager.setAdapter(mAdapter);
    mViewPager.setOffscreenPageLimit(2);
    mViewPager.setOnPageChangeListener(this);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    initActionBar();
  }

  private void initActionBar() {
    mActionBar.setTitle("HN");

    mActionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_TABS);
    mActionBar.setDisplayShowTitleEnabled(true);

    Tab tab = mActionBar.newTab()
            .setText("Stories")
            .setTabListener(new Tabs(0));
    mActionBar.addTab(tab);
    tab = mActionBar.newTab()
            .setText("Comments")
            .setTabListener(new Tabs(1));
    mActionBar.addTab(tab);
    tab = mActionBar.newTab()
            .setText("Article")
            .setTabListener(new Tabs(2));
    mActionBar.addTab(tab);
  }

  @Subscribe public void onShowStoryEvent(ShowStoryEvent ev) {
    mAdapter.setActiveStory(ev.getStory());
    mViewPager.setCurrentItem(1);
  }

  @Override public void onPageScrolled(int i, float v, int i2) { /* no op */ }

  @Override public void onPageScrollStateChanged(int i) { /* no op */ }

  @Override public void onPageSelected(int i) {
    mActionBar.selectTab(mActionBar.getTabAt(i));
  }


  private static class Adapter extends FragmentPagerAdapter {

    private Story mActiveStory;

    public Adapter(FragmentManager fm) {
      super(fm);
    }

    @Override public Fragment getItem(int i) {
      switch (i) {
        case 0:
          return new StoryFragment();
        case 1:
          CommentsFragment commentsFragment = new CommentsFragment();
          boolean isJobsPost = Story.isYCombinatorJobPost(mActiveStory);
          if (!isJobsPost) {
            commentsFragment.setStory(mActiveStory);
          }
          return commentsFragment;
        case 2:
          WebFragment webFragment = new WebFragment();
          webFragment.setUrl(mActiveStory.url);
          return webFragment;
        default:
          throw new IllegalStateException(
                  "MainActivity.Adapter attempted to access an illegal position = " + i);
      }
    }

    @Override public int getCount() {
      if (mActiveStory != null) {
        if (StringUtils.isNotBlank(mActiveStory.url)) return 3;
        else return 2;
      } else return 1;
    }

    public void clearActiveStory() {
      mActiveStory = null;
      notifyDataSetChanged();
    }

    public void setActiveStory(Story activeStory) {
      mActiveStory = activeStory;
      notifyDataSetChanged();
    }
  }

  private class Tabs implements ActionBar.TabListener {

    private int mPosition;

    public Tabs(int position) {
      mPosition = position;
    }

    @Override
    public void onTabSelected(Tab tab, FragmentTransaction _) {
      mViewPager.setCurrentItem(mPosition);
    }

    @Override
    public void onTabUnselected(Tab tab, FragmentTransaction _) { /* no op */ }

    @Override
    public void onTabReselected(Tab tab, FragmentTransaction _) { /* no op */ }
  }
}
