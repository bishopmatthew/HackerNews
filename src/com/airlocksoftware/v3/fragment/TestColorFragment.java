package com.airlocksoftware.v3.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.airlocksoftware.hackernews.R;

/**
 * Created by matthewbbishop on 12/8/13.
 */
public class TestColorFragment extends Fragment {

  public static final String COLOR = "COLOR";

  private int mColor;

  public static TestColorFragment newInstance(int color) {
    TestColorFragment frg = new TestColorFragment();
    Bundle args = new Bundle();
    args.putInt(COLOR, color);
    frg.setArguments(args);
    return frg;
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    mColor = getArguments().getInt(COLOR);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.vw_debug_menu, container, false);
    root.setBackgroundColor(mColor);
    return root;
  }
}
