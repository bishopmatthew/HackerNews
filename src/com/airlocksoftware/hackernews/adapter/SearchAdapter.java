package com.airlocksoftware.hackernews.adapter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import android.content.Context;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.activity.MainActivity;
import com.airlocksoftware.hackernews.activity.MainActivity.CommentsTab;
import com.airlocksoftware.hackernews.model.SearchItem;
import com.airlocksoftware.holo.type.FontText;
import com.airlocksoftware.holo.utils.ViewUtils;

/**
 * Adapter for SearchFragment. Displays *
 */
public class SearchAdapter extends BaseAdapter {

  Context mContext;

  List<SearchItem> mSearchItems = new ArrayList<SearchItem>();

  private boolean moreLink;

  private static final int ITEM_TYPE_STORY = 0;

  private static final int ITEM_TYPE_COMMENT = 1;

  private static final int ITEM_TYPE_MORE = 2;

  public SearchAdapter(Context context) {
    mContext = context;
  }

  @Override
  public int getCount() {
    return mSearchItems.size() + ((moreLink) ? 1 : 0);
  }

  @Override
  public boolean isEnabled(int position) {
    return false;
  }

  @Override
  public SearchItem getItem(int position) {
    return mSearchItems.get(position);
  }

  @Override
  public long getItemId(int position) {
    return position;
  }

  @Override
  public View getView(int position, View convertView, ViewGroup container) {
    LayoutInflater inflater = LayoutInflater.from(mContext);
    SearchItem item = getItem(position);
    int viewType = getItemViewTypeBySearchItem(item);

    switch (viewType) {
      case ITEM_TYPE_STORY:

        StoryHolder sHolder;
        if (convertView == null) {
          convertView = inflater.inflate(R.layout.vw_story, container, false);
          ViewUtils.fixBackgroundRepeat(convertView);
          sHolder = getNewStoryHolder(convertView);
          convertView.setTag(sHolder);
        } else {
          sHolder = (StoryHolder) convertView.getTag();
        }
        bindStoryView(item, sHolder);

        break;
      case ITEM_TYPE_COMMENT:

        CommentHolder cHolder;
        if (convertView == null) {
          convertView = inflater.inflate(R.layout.vw_comment_search, container, false);
          ViewUtils.fixBackgroundRepeat(convertView);
          cHolder = getNewCommentsHolder(convertView);
          convertView.setTag(cHolder);
        } else {
          cHolder = (CommentHolder) convertView.getTag();
        }
        bindCommentsView(item, cHolder);

        break;
      case ITEM_TYPE_MORE:

        if (convertView == null) {
          convertView = inflater.inflate(R.layout.vw_more_link, container, false);
        }

        break;
    }

    return convertView;
  }

  private void bindCommentsView(SearchItem item, CommentHolder cHolder) {
    // if thread is [dead], there will be no item.discussion.title
    if (item.discussion != null && item.discussion.title != null) {
      cHolder.title.setVisibility(View.VISIBLE);
      cHolder.title.setText("on: " + item.discussion.title);
    } else {
      cHolder.title.setVisibility(View.GONE);
    }
    cHolder.username.setText(item.username);
    cHolder.comment.setText(Html.fromHtml(item.text));
    cHolder.item = item;
  }

  private void bindStoryView(SearchItem item, StoryHolder sHolder) {
    sHolder.title.setText(item.title);
    sHolder.numPoints.setText(Integer.toString(item.points));
    sHolder.numComments.setText(Integer.toString(item.num_comments));
    sHolder.domain.setText(item.domain);
    sHolder.item = item;
  }

  private CommentHolder getNewCommentsHolder(View convertView) {
    CommentHolder cHolder;
    cHolder = new CommentHolder();
    cHolder.username = (FontText) convertView.findViewById(R.id.txt_username);
    cHolder.comment = (FontText) convertView.findViewById(R.id.txt_comment);
    cHolder.title = (FontText) convertView.findViewById(R.id.txt_title);

    cHolder.title.setTag(cHolder);

    cHolder.title.setOnClickListener(mCommentListener);
    return cHolder;
  }

  private StoryHolder getNewStoryHolder(View convertView) {
    StoryHolder sHolder;
    sHolder = new StoryHolder();
    sHolder.title = (FontText) convertView.findViewById(R.id.txt_title);
    sHolder.numPoints = (FontText) convertView.findViewById(R.id.txt_num_pts);
    sHolder.numComments = (FontText) convertView.findViewById(R.id.txt_num_comments);
    sHolder.domain = (FontText) convertView.findViewById(R.id.txt_domain);
    sHolder.commentsButton = convertView.findViewById(R.id.btn_comments);
    sHolder.webButton = convertView.findViewById(R.id.btn_web);

    sHolder.commentsButton.setTag(sHolder);
    sHolder.webButton.setTag(sHolder);

    sHolder.commentsButton.setOnClickListener(mStoryListener);
    sHolder.webButton.setOnClickListener(mStoryListener);
    return sHolder;
  }

  @Override
  public int getItemViewType(int position) {
    if (position == mSearchItems.size()) {
      return ITEM_TYPE_MORE;
    } else {
      return getItemViewTypeBySearchItem(getItem(position));
    }
  }

  private int getItemViewTypeBySearchItem(SearchItem item) {
    if (item.type.equals("comment")) {
      return ITEM_TYPE_COMMENT;
    } else if (item.type.equals("submission")) {
      return ITEM_TYPE_STORY;
    } else {
      throw new RuntimeException("Received a search result that isn't a comment or a submission. Type = "
              + item.type);
    }
  }

  @Override
  public int getViewTypeCount() {
    return 3;
  }

  public void add(SearchItem item) {
    mSearchItems.add(item);
    notifyDataSetChanged();
  }

  public void addAll(Collection<SearchItem> items) {
    mSearchItems.addAll(items);
    notifyDataSetChanged();
  }

  public void clear() {
    mSearchItems.clear();
    notifyDataSetChanged();
  }

  private class StoryHolder {

    public SearchItem item;

    public FontText title, numPoints, numComments, domain;

    public View commentsButton, webButton;
  }

  private class CommentHolder {

    public SearchItem item;

    public FontText username, comment, title;
  }

  public View.OnClickListener mStoryListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      StoryHolder holder = (StoryHolder) v.getTag();
      CommentsTab tab = v.getId() == R.id.btn_comments ? CommentsTab.COMMENTS : CommentsTab.ARTICLE;
      MainActivity.startCommentsActivity(mContext, null, holder.item.id, holder.item.url, tab);
    }
  };

  public View.OnClickListener mCommentListener = new View.OnClickListener() {
    @Override
    public void onClick(View v) {
      CommentHolder holder = (CommentHolder) v.getTag();
      MainActivity.startCommentsActivity(mContext, null, holder.item.discussion.id, holder.item.url,
              CommentsTab.COMMENTS);
    }
  };

}
