package com.airlocksoftware.v3.fragment;

import android.app.Fragment;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.v3.adapter.BindableAdapter;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by matthewbbishop on 12/7/13.
 */
public class TestStoryFragment extends Fragment {

  @InjectView(android.R.id.list)
  ListView mStoryList;

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.frg_test_story, container, false);
    ButterKnife.inject(this, root);
    setupViews();
    return root;
  }

  private void setupViews() {

  }

  private static class Adapter extends BindableAdapter<String> {

    public Adapter(Context context) {
      super(context);
    }

    @Override
    public int getCount() {
      return 0;
    }

    @Override
    public String getItem(int position) {
      return null;
    }

    @Override
    public long getItemId(int position) {
      return 0;
    }

    @Override
    public View newView(LayoutInflater inflater, int position, ViewGroup container) {
      return null;
    }

    @Override
    public void bindView(String item, int position, View view) {

    }
  }
}
