/*
* Copyright (C) 2013 Square, Inc.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package com.airlocksoftware.v3.activity;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.airlocksoftware.v3.activity.components.BackPressedManager;
import com.airlocksoftware.v3.dagger.BaseActivityModule;
import com.airlocksoftware.v3.dagger.HNApp;
import com.squareup.otto.Bus;

import java.util.Arrays;
import java.util.List;

import javax.inject.Inject;

import dagger.ObjectGraph;

/**
 * Base activity which sets up a per-activity object graph and performs injection.
 */
public abstract class BaseActivity extends FragmentActivity {

  private ObjectGraph mActivityGraph;

  @Inject Bus mBus;
  @Inject BackPressedManager mBackPressedManager;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    // Create the activity graph by .plus-ing our modules onto the application graph.
    HNApp application = (HNApp) getApplication();
    Object[] objects = getModules().toArray();
    mActivityGraph = application.getApplicationGraph().plus(objects);

    // Inject ourselves so subclasses will have dependencies fulfilled when this method returns.
    mActivityGraph.inject(this);
  }

  @Override
  protected void onDestroy() {
    // Eagerly clear the reference to the activity graph to allow it to be garbage collected as
    // soon as possible.
    mActivityGraph = null;

    super.onDestroy();
  }

  @Override
  public void onResume() {
    super.onResume();
    mBus.register(this);
  }

  @Override
  public void onPause() {
    super.onPause();
    mBus.unregister(this);
  }

  @Override public void onBackPressed() {
    boolean handled = mBackPressedManager.onBackPressed();
    if(!handled) super.onBackPressed();
  }


  public BackPressedManager getBackPressedManager() {
    if(mBackPressedManager == null) mBackPressedManager = new BackPressedManager();
    return mBackPressedManager;
  }

  /** Otto bus that can be used by subclasses **/
  public Bus getBus() {
    return mBus;
  }

  /**
   * A list of modules to use for the individual activity graph. Subclasses can override this method to provide
   * additional modules provided they call and include the modules returned by calling {@code super.getModules()}.
   */
  protected List<Object> getModules() {
    return Arrays.<Object>asList(new BaseActivityModule(this));
  }

  /**
   * Inject the supplied {@code object} using the activity-specific graph.
   */
  public void inject(Object object) {
    mActivityGraph.inject(object);
  }
}