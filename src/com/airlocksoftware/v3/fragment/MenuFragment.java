package com.airlocksoftware.v3.fragment;

import android.app.ActionBar;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.holo.image.IconView;
import com.airlocksoftware.v3.adapter.BindableAdapter;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;

/**
 * Created by matthewbbishop on 12/9/13.
 */
public class MenuFragment extends Fragment {

  /* ActionBar (injected by Activity) */
  @Inject ActionBar mActionBar;

  /* ViewPager and adapter */
  @InjectView(android.R.id.list) ListView mListView;
  Adapter mAdapter;

  @Override public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    View root = inflater.inflate(R.layout.frg_menu, container, false);
    ButterKnife.inject(this, root);
    initViews();
    return root;
  }

  private void initViews() {
    mAdapter = new Adapter(getActivity());
    mListView.setAdapter(mAdapter);
  }

  private static class MenuItem {
    int iconResId;
    String text;

    public MenuItem(int iconResId, String text) {
      this.iconResId = iconResId;
      this.text = text;
    }
  }

  private static class Adapter extends BindableAdapter<MenuItem> {

    List<MenuItem> mMenuItems = new ArrayList<MenuItem>();
    {
      mMenuItems.add(new MenuItem(R.drawable.ic_action_arrow_left, "Left"));
      mMenuItems.add(new MenuItem(R.drawable.ic_action_arrow_right, "Right"));
      mMenuItems.add(new MenuItem(R.drawable.ic_action_search, "Search"));
    }

    public Adapter(Context context) {
      super(context);
    }

    @Override public int getCount() {
      return mMenuItems.size();
    }

    @Override public MenuItem getItem(int position) {
      return mMenuItems.get(position);
    }

    @Override public long getItemId(int position) {
      return position;
    }

    @Override public View newView(LayoutInflater inflater, int position, ViewGroup container) {
      return inflater.inflate(R.layout.vw_menu_item, container, false);
    }

    @Override public void bindView(MenuItem item, int position, View view) {
      ((IconView) view.findViewById(R.id.icon)).iconSource(item.iconResId);
      ((TextView) view.findViewById(R.id.label)).setText(item.text);
    }
  }
}
