package com.airlocksoftware.v3.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import com.airlocksoftware.v3.activity.BaseActivity;
import com.squareup.otto.Bus;

import javax.inject.Inject;

/**
 * Created by matthewbbishop on 12/8/13.
 */
public class BaseFragment extends Fragment {

  @Inject protected Bus mBus;

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

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    ((BaseActivity) getActivity()).inject(this);
  }

  public Bus getBus() {
    return mBus;
  }

}
