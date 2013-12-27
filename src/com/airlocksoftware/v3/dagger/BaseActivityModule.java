package com.airlocksoftware.v3.dagger;

import android.app.ActionBar;
import android.content.Context;

import com.airlocksoftware.v3.actionbar.MainFragmentActionBarManager;
import com.airlocksoftware.v3.activity.BaseActivity;
import com.airlocksoftware.v3.activity.MainActivity;
import com.airlocksoftware.v3.activity.components.BackPressedManager;
import com.airlocksoftware.v3.fragment.BaseFragment;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * Created by matthewbbishop on 12/7/13.
 */
@Module(
        injects = {
                BaseActivity.class,
                BaseFragment.class,
                MainFragmentActionBarManager.class,
                MainActivity.class
        },
        library = true,
        complete = false
)
public class BaseActivityModule {

  private final BaseActivity mActivity;

  public BaseActivityModule(BaseActivity activity) {
    mActivity = activity;
  }

  /**
   * Allow the activity context to be injected but require that it be annotated with {@link ForActivity @ForActivity} to
   * explicitly differentiate it from application context.
   */
  @Provides @Singleton @ForActivity Context provideActivityContext() {
    return mActivity;
  }

  @Provides @Singleton ActionBar provideActionBar() {
    return mActivity.getActionBar();
  }

  @Provides @Singleton BackPressedManager provideBackPressedManager() {
    return mActivity.getBackPressedManager();
  }

}
