package com.airlocksoftware.v3.dagger;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;

import com.airlocksoftware.v3.activity.BaseActivity;
import com.airlocksoftware.v3.activity.MainActivity;
import com.airlocksoftware.v3.fragment.BaseFragment;
import com.airlocksoftware.v3.fragment.MainFragment;

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
                MainActivity.class,
                MainFragment.class
        },
        library = true,
        complete = false
)
public class ActivityModule {

  private final Activity mActivity;

  public ActivityModule(Activity activity) {
    mActivity = activity;
  }

  /**
   * Allow the activity context to be injected but require that it be annotated with {@link ForActivity @ForActivity} to
   * explicitly differentiate it from application context.
   */
  @Provides
  @Singleton
  @ForActivity
  Context provideActivityContext() {
    return mActivity;
  }

  @Provides
  @Singleton
  ActionBar provideActionBar() {
    return mActivity.getActionBar();
  }

}
