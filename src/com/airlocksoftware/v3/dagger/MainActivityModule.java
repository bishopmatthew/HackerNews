package com.airlocksoftware.v3.dagger;

import android.support.v4.app.ActionBarDrawerToggle;

import com.airlocksoftware.hackernews.fragment.CommentsFragment;
import com.airlocksoftware.hackernews.fragment.StoryFragment;
import com.airlocksoftware.v3.actionbar.MainFragmentActionBarManager;
import com.airlocksoftware.v3.activity.MainActivity;
import com.airlocksoftware.v3.fragment.MainFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by matthewbbishop on 12/20/13.
 */
@Module(
        injects = {
                MainFragment.class,
                StoryFragment.class,
                CommentsFragment.class,
                MainFragmentActionBarManager.class
        },
        library = true,
        complete = false
)
public class MainActivityModule {

  private final MainActivity mMainActivity;

  public MainActivityModule(MainActivity mainActivity) {
    mMainActivity = mainActivity;
  }

  @Provides @Singleton ActionBarDrawerToggle provideDrawerToggle() {
    return mMainActivity.getDrawerToggle();
  }

}
