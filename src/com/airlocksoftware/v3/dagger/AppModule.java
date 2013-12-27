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
package com.airlocksoftware.v3.dagger;

import android.content.Context;

import com.airlocksoftware.hackernews.adapter.StoryAdapter;
import com.airlocksoftware.v3.actionbar.ActionBarTabsView;
import com.airlocksoftware.v3.app.HNApp;
import com.squareup.otto.Bus;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

/**
 * A module for Android-specific dependencies which require a {@link Context} or {@link android.app.Application} to
 * create.
 */
@Module(
        injects = {
          /* Inject things that don't go anywhere else (generally to get an AppContext or Bus) */
          StoryAdapter.class,
          ActionBarTabsView.class
        },
        library = true,
        complete = false
)
public class AppModule {

  private final HNApp mApp;

  public AppModule(HNApp mApp) {
    this.mApp = mApp;
  }

  /** Allow the application context to be injected but require that it be annotated with {@link HNApp @Annotation} to
   * explicitly differentiate it from an activity context.
   */
  @Provides @Singleton @ForApplication Context provideApplicationContext() {
    return mApp;
  }

  @Provides @Singleton Bus provideBus() {
    return new Bus();
  }
}