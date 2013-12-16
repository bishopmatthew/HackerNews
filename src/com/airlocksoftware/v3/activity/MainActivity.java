package com.airlocksoftware.v3.activity;

import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.widget.DrawerLayout;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewStub;
import android.widget.FrameLayout;

import com.airlocksoftware.hackernews.BuildConfig;
import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.fragment.CommentsFragment;
import com.airlocksoftware.hackernews.fragment.StoryFragment;
import com.airlocksoftware.hackernews.interfaces.SharePopupInterface;
import com.airlocksoftware.hackernews.interfaces.TabletLayout;
import com.airlocksoftware.hackernews.model.Story;
import com.airlocksoftware.hackernews.view.SharePopup;
import com.airlocksoftware.v3.fragment.MainFragment;
import com.airlocksoftware.v3.otto.ShowStoryEvent;

import butterknife.ButterKnife;
import butterknife.InjectView;

import static com.airlocksoftware.hackernews.activity.MainActivity.CommentsTab;

/**
 * Created by matthewbbishop on 12/7/13.
 */
public class MainActivity extends BaseActivity implements StoryFragment.Callbacks, TabletLayout,
        SharePopupInterface, CommentsFragment.Callbacks {


  @InjectView(R.id.drawer_layout) DrawerLayout mDrawerLayout;
  @InjectView(R.id.cnt_fragment) FrameLayout mFragmentContainer;
  @InjectView(R.id.debug_menu) ViewStub mDebugMenu;
  
  /* Navigation Drawer */
  private ActionBarDrawerToggle mDrawerToggle;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    /* Inflate views */
    setContentView(R.layout.act_main);
    /* Inject views */
    ButterKnife.inject(this);
    /* Setup the injected views */
    initViews();
    /* Setup the navigation drawer */
    initNavigationDrawer();
  }

  private void initNavigationDrawer() {
    int icon = R.drawable.ic_btn_play_store;
    int open = R.string.drawer_open;
    int close = R.string.drawer_close;
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, icon, open, close) {
      public void onDrawerClosed(View view) { /* no op */ }
      public void onDrawerOpened(View drawerView) { /* no op */ }
    };

    // Set the drawer toggle as the DrawerListener
    mDrawerLayout.setDrawerListener(mDrawerToggle);

    getActionBar().setDisplayHomeAsUpEnabled(true);
    getActionBar().setHomeButtonEnabled(true);
    getActionBar().setDisplayShowHomeEnabled(true);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Pass the event to ActionBarDrawerToggle, if it returns
    // true, then it has handled the app icon touch event
    if (mDrawerToggle.onOptionsItemSelected(item)) {
      return true;
    }
    // Handle your other action bar items...

    return super.onOptionsItemSelected(item);
  }

  private void initViews() {
    /* If we don't have any fragments, add the default (MainFragment) */
    FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
    ft.add(R.id.cnt_fragment, new MainFragment(), "MainFragment");
    ft.commit();
    /* If we're in debug mode, display the debug menu. */
    if (BuildConfig.DEBUG) {
      setupDebugMenu();
    }
  }

  private void setupDebugMenu() {
    mDebugMenu.inflate();
  }

  @Override
  public void showCommentsForStory(Story story, CommentsTab initalTab) {
//        Toast.makeText(this, "Show comments", Toast.LENGTH_SHORT).show();
    getBus().post(new ShowStoryEvent(story));
  }

  @Override
  public Story getActiveStory() {
    return new Story();
  }

  @Override
  public boolean storyFragmentIsInLayout() {
    return true;
  }

  @Override
  public boolean isTabletLayout() {
    return false;
  }

  @Override
  public SharePopup getSharePopup() {
//        return new SharePopup(this, null, null);
    return null;
  }

  @Override public void receivedStory(Story story) {
    /* mb TODO */
  }

  @Override public boolean commentsFragmentIsInLayout() {
    return true;
  }
}
