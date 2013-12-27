package com.airlocksoftware.v3.fragment;

import android.app.ActionBar;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.fragment.CommentsFragment;
import com.airlocksoftware.hackernews.fragment.StoryFragment;
import com.airlocksoftware.hackernews.fragment.WebFragment;
import com.airlocksoftware.hackernews.model.Story;
import com.airlocksoftware.hackernews.utils.StringUtils;
import com.airlocksoftware.v3.actionbar.MainFragmentActionBarManager;
import com.airlocksoftware.v3.actionbar.MainFragmentTab;
import com.airlocksoftware.v3.activity.components.BackPressedManager;
import com.airlocksoftware.v3.otto.ShowStoryEvent;
import com.airlocksoftware.v3.viewpager.MultiOnPageChangeListener;
import com.airlocksoftware.v3.viewpager.PageWrangler;
import com.squareup.otto.Subscribe;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by matthewbbishop on 12/7/13.
 */
public class MainFragment extends BaseFragment implements ActionBar.OnNavigationListener {

  /* Allows fragments (or other components) to register as a BackPressedListener */
  @Inject BackPressedManager mBackPressedManager;

  /* ActionBar and related */
  @Inject ActionBar mActionBar;
  /* The internal ActionBar view */
  private View mActionBarView;
  private ArrayAdapter mActionBarSpinnerAdapter;

  /* ViewPager and adapter */
  @InjectView(R.id.viewpager) ViewPager mViewPager;
  private Adapter mAdapter;

  private MultiOnPageChangeListener mPageChangeListener;
  private PageWrangler mPageWrangler;
  private MainFragmentActionBarManager mActionBarManager;

  @Override public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

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

    /* Allows multiple OnPageChangeListeners */
    mPageChangeListener = new MultiOnPageChangeListener();
    mViewPager.setOnPageChangeListener(mPageChangeListener);
    
    /* Page wrangler (does the animations between pages) */
    mPageWrangler = new PageWrangler(mViewPager);
    mPageChangeListener.addListener(mPageWrangler);
  }

  @Override public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    initActionBar();
  }

  private void initActionBar() {
    /* Manages which content is shown in the ActionBar, including which Fragment is having it's menu shown */
    View decorView = getActivity().getWindow().getDecorView();
    mActionBarManager = new MainFragmentActionBarManager(this, decorView, mViewPager, mAdapter);
    mPageChangeListener.addListener(mActionBarManager);
  }

  @Override public void onResume() {
    super.onResume();
    getBus().register(mActionBarManager);
  }

  @Override public void onPause() {
    super.onPause();
    getBus().unregister(mActionBarManager);
  }

  @Subscribe public void onShowStoryEvent(ShowStoryEvent ev) {
    if(ev.getStory() != null && ev.getStory().storyId != 0) {
      mAdapter.setActiveStory(ev.getStory());
      mViewPager.setCurrentItem(1);
    }
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    return mActionBarManager.onOptionsItemSelected(item);
  }

  @Override public boolean onNavigationItemSelected(int itemPosition, long itemId) {
    return false;
  }

  public static class Adapter extends FragmentPagerAdapter {

    private Story mActiveStory;

    private StoryFragment mStoryFragment;
    private CommentsFragment mCommentsFragment;
    private WebFragment mWebFragment;

    public Adapter(FragmentManager fm) {
      super(fm);
    }

    @Override public Fragment getItem(int i) {
      switch (MainFragmentTab.fromPagerPosition(i)) {
        case STORIES:
          if(mStoryFragment == null) mStoryFragment = new StoryFragment();
          return mStoryFragment;
        case COMMENTS:
          if(mCommentsFragment == null) {
            mCommentsFragment = new CommentsFragment();
            
            /* mb TODO disabled because it should pick up the active story from the StoryAdapter @Produce method */
//            boolean isJobsPost = Story.isYCombinatorJobPost(mActiveStory);
//            if (!isJobsPost) {
//              mCommentsFragment.setStory(mActiveStory);
//            }
          }
          return mCommentsFragment;
        case ARTICLE:
          if(mWebFragment == null) {
            mWebFragment = new WebFragment();
            mWebFragment.setUrl(mActiveStory.url);
          }
          return mWebFragment;
        default: return null;
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
}
