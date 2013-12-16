package com.airlocksoftware.v3.dagger;

import android.app.Application;

import com.airlocksoftware.hackernews.BuildConfig;
import com.crashlytics.android.Crashlytics;

import java.util.Arrays;
import java.util.List;

import dagger.ObjectGraph;
import timber.log.Timber;

import static timber.log.Timber.HollowTree;
import static timber.log.Timber.plant;

/**
 * Created by matthewbbishop on 12/7/13.
 */
public class HNApp extends Application {

  private ObjectGraph mApplicationGraph;

  @Override
  public void onCreate() {
    super.onCreate();

    mApplicationGraph = ObjectGraph.create(getModules().toArray());

    if (BuildConfig.DEBUG) {
      plant(new Timber.DebugTree());
    } else {
      plant(new CrashReportingTree());
    }
  }

  public ObjectGraph getApplicationGraph() {
    return mApplicationGraph;
  }

  protected List<Object> getModules() {
    return Arrays.asList(
            new AndroidModule(this),
            new DemoModule()
    );
  }

  public void inject(Object object) {
    mApplicationGraph.inject(object);
  }

  /**
   * A tree which logs important information for crash reporting.
   */
  private static class CrashReportingTree extends HollowTree {

    @Override
    public void i(String message, Object... args) {
      Crashlytics.log(String.format(message, args));
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
      i(message, args); // Just add to the log.
    }

    @Override
    public void e(String message, Object... args) {
      i("ERROR: " + message, args); // Just add to the log.
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
      e(message, args);
      Crashlytics.logException(t);
    }
  }

}
