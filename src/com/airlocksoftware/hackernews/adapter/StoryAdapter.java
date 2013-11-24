package com.airlocksoftware.hackernews.adapter;

import java.io.Serializable;
import java.util.List;

import com.airlocksoftware.hackernews.utils.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.FrameLayout;
import android.widget.ListView;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.activity.MainActivity;
import com.airlocksoftware.hackernews.activity.MainActivity.CommentsTab;
import com.airlocksoftware.hackernews.activity.UserActivity;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.fragment.StoryFragment;
import com.airlocksoftware.hackernews.interfaces.TabletLayout;
import com.airlocksoftware.hackernews.model.Story;
import com.airlocksoftware.hackernews.view.SharePopup;
import com.airlocksoftware.holo.adapters.GroupAdapter;
import com.airlocksoftware.holo.image.IconView;
import com.airlocksoftware.holo.type.FontText;
import com.airlocksoftware.holo.utils.AnimUtils;
import com.airlocksoftware.holo.utils.Utils;
import com.airlocksoftware.holo.utils.ViewUtils;

/**
 * Adapter for StoryFragment and SubmissionsFragment. *
 */
public class StoryAdapter extends GroupAdapter<Story> {

    private long mActiveStoryId;

    private SharePopup mShare;
    private Context mContext;
    private ListView mList;
    private StoryFragment.Callbacks mStoryCallbacks;
    private TabletLayout mTabletInterface;
    private LayoutInflater mInflater;

    // cached resource values
    private final int BRIGHT_ACCENT_COLOR;
    private final int TEXT_COLOR_PRIMARY;
    private final int TEXT_COLOR_SECONDARY;
    private final int DARK_ORANGE_COLOR;
    private final int GREY_50_COLOR;
    private final String UPVOTE;
    private final String UPVOTED;
    private final Drawable UPVOTE_INDICATOR;

    private static final int STORY_LAYOUT = R.layout.vw_story;
    private static final String STORIES = StoryAdapter.class.getSimpleName() + ".stories";
    private static final String ACTIVE_STORY = StoryAdapter.class.getSimpleName() + ".activeStory";

    private static class ViewHolder {
        public Story story;
        public FontText title, numPoints, numComments, domain, pointsLabel, upvoteLabel;
        public View commentsButton, webButton, ctrlContainer, container, divider;
        public FrameLayout upvoteBtn, shareBtn, userBtn;
        public IconView upvoteIcon, commentIcon;

        /**
         * Whether or not this View is in "active mode" (i.e. Orange background, white text.) *
         */
        public boolean activeMode = false;

        /**
         * Whether or not this view is in "upvoted mode".(i.e. disabled vote listener, orange icon, etc.) *
         */
        public boolean upvotedMode = false;
    }

    public StoryAdapter(Context context, ListView list, SharePopup share, StoryFragment.Callbacks storyCallbacks,
                        TabletLayout tabletLayout) {
        mContext = context;
        mList = list;
        mShare = share;
        mStoryCallbacks = storyCallbacks;
        mTabletInterface = tabletLayout;
        mInflater = LayoutInflater.from(mContext);

        // cache reusable resources
        Resources res = mContext.getResources();
        TEXT_COLOR_PRIMARY = res.getColor(Utils.getThemedResourceId(mContext, R.attr.textColorPrimary));
        TEXT_COLOR_SECONDARY = res.getColor(Utils.getThemedResourceId(mContext, R.attr.textColorSecondary));
        BRIGHT_ACCENT_COLOR = res.getColor(R.color.bright_accent);
        DARK_ORANGE_COLOR = res.getColor(R.color.orange_5);
        GREY_50_COLOR = res.getColor(R.color.grey_50);
        UPVOTE_INDICATOR = res.getDrawable(R.drawable.bg_story_upvote_indicator);
        UPVOTED = mContext.getString(R.string.upvoted);
        UPVOTE = mContext.getString(R.string.upvote);
    }

    @Override
    public View getView(Story story, View convertView, ViewGroup parent) {
        ViewHolder holder;
        if (convertView == null) {

            // inflate the layout
            convertView = mInflater.inflate(STORY_LAYOUT, parent, false);

            // find views for ViewHolder
            holder = new ViewHolder();
            findHolderViews(convertView, holder);

            // fix background repeats
            ViewUtils.fixBackgroundRepeat(holder.webButton);
            ViewUtils.fixBackgroundRepeat(holder.commentsButton);

            // attach click listeners
            attachClickListeners(holder);

            // set holder as tag on views that have click listeners
            attachTags(convertView, holder);

        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        bindView(story, holder);
        return convertView;
    }

    private void attachTags(View convertView, ViewHolder holder) {
        convertView.setTag(holder);
        holder.webButton.setTag(holder);
        holder.commentsButton.setTag(holder);
        holder.upvoteBtn.setTag(holder);
        holder.shareBtn.setTag(holder);
        holder.userBtn.setTag(holder);
    }

    private void attachClickListeners(ViewHolder holder) {
        holder.webButton.setOnClickListener(mClickListener);
        holder.commentsButton.setOnClickListener(mClickListener);
        holder.webButton.setOnLongClickListener(mLongListener);
        holder.commentsButton.setOnLongClickListener(mLongListener);
        holder.shareBtn.setOnClickListener(mShareListener);
        holder.upvoteBtn.setOnClickListener(mVoteListener);
        holder.userBtn.setOnClickListener(mUserListener);
    }

    private void findHolderViews(View convertView, ViewHolder holder) {
        holder.container = convertView;
        holder.title = (FontText) convertView.findViewById(R.id.txt_title);
        holder.numPoints = (FontText) convertView.findViewById(R.id.txt_num_pts);
        holder.numComments = (FontText) convertView.findViewById(R.id.txt_num_comments);
        holder.domain = (FontText) convertView.findViewById(R.id.txt_domain);
        holder.commentsButton = convertView.findViewById(R.id.btn_comments);
        holder.webButton = convertView.findViewById(R.id.btn_web);
        holder.ctrlContainer = convertView.findViewById(R.id.cnt_ctrl);
        holder.shareBtn = (FrameLayout) convertView.findViewById(R.id.btn_share);
        holder.userBtn = (FrameLayout) convertView.findViewById(R.id.btn_user);
        holder.upvoteBtn = (FrameLayout) convertView.findViewById(R.id.btn_upvote);
        holder.upvoteIcon = (IconView) convertView.findViewById(R.id.icv_upvote);
        holder.upvoteLabel = (FontText) convertView.findViewById(R.id.txt_upvote);
        holder.pointsLabel = (FontText) convertView.findViewById(R.id.txt_pts);
        holder.commentIcon = (IconView) convertView.findViewById(R.id.icv_comment);
        holder.divider = convertView.findViewById(R.id.divider);
    }

    public void setActiveStory(Story story) {
        if (story != null) {
            mActiveStoryId = story.storyId;
            notifyDataSetChanged();
        }
    }

    private void bindView(Story story, ViewHolder holder) {
        holder.story = story;
        if (holder.ctrlContainer.getVisibility() != View.GONE)
            holder.ctrlContainer.setVisibility(View.GONE);

        bindTextViews(story, holder);
        bindIsJobsPost(story, holder, Story.isYCombinatorJobPost(story));

        // holder.upvotedMode caches whether this view has been configured to show an upvoted story
        // if upvotedMode != story.isUpvoted, then we need reconfigure the view
        if (story.isUpvoted != holder.upvotedMode) {
            bindIsUpvoted(story, holder);
        }

        // if this is the active story & the view isn't already in activeMode,
        // change background & colors, hide divider, and disable click listeners
        if (mTabletInterface != null && mTabletInterface.isTabletLayout()) {
            boolean isActiveStory = story.storyId == mActiveStoryId;
            if (isActiveStory != holder.activeMode) {
                bindIsActiveStory(holder, isActiveStory);
            }
        }
    }

    private void bindIsActiveStory(ViewHolder holder, boolean isActiveStory) {
        int textColorSecondary = isActiveStory ? Color.WHITE : TEXT_COLOR_SECONDARY;
        holder.numPoints.setTextColor(textColorSecondary);
        holder.numComments.setTextColor(textColorSecondary);
        holder.domain.setTextColor(textColorSecondary);
        holder.pointsLabel.setTextColor(textColorSecondary);

        holder.container.setBackgroundColor(isActiveStory ? DARK_ORANGE_COLOR : Color.TRANSPARENT);
        holder.title.setTextColor(isActiveStory ? Color.WHITE : TEXT_COLOR_PRIMARY);
        holder.commentIcon.iconColor(isActiveStory ? Color.WHITE : GREY_50_COLOR);
        holder.divider.setVisibility(ViewUtils.boolToVis(!isActiveStory));

        holder.commentsButton.setOnClickListener(isActiveStory ? null : mClickListener);
        holder.commentsButton.setOnLongClickListener(isActiveStory ? null : mLongListener);
        holder.webButton.setOnClickListener(isActiveStory ? null : mClickListener);
        holder.webButton.setOnLongClickListener(isActiveStory ? null : mLongListener);
        holder.commentsButton.setClickable(!isActiveStory);
        holder.commentsButton.setLongClickable(!isActiveStory);
        holder.webButton.setClickable(!isActiveStory);
        holder.webButton.setLongClickable(!isActiveStory);

        holder.activeMode = isActiveStory;
    }

    @SuppressWarnings("deprecation")
    private void bindIsUpvoted(Story story, ViewHolder holder) {
        holder.upvoteIcon.iconColor(story.isUpvoted ? BRIGHT_ACCENT_COLOR : TEXT_COLOR_PRIMARY);
        holder.upvoteLabel.setTextColor(story.isUpvoted ? BRIGHT_ACCENT_COLOR : TEXT_COLOR_PRIMARY);
        holder.upvoteLabel.setText(story.isUpvoted ? UPVOTED : UPVOTE);
        holder.upvoteBtn.setClickable(!story.isUpvoted);
        holder.upvoteBtn.setOnClickListener(story.isUpvoted ? null : mVoteListener);
        holder.numPoints.setBackgroundDrawable(story.isUpvoted ? UPVOTE_INDICATOR : null);
        holder.upvotedMode = story.isUpvoted;
    }

    /**
     * check for YCombinator job post *
     */
    private void bindIsJobsPost(Story story, ViewHolder holder, boolean isJobPost) {
        // hide / show comments, points, & webButton
        holder.webButton.setVisibility(ViewUtils.boolToVis(StringUtils.isNotBlank(story.url) && !isJobPost));
        holder.commentIcon.setVisibility(ViewUtils.boolToVis(!isJobPost));
        holder.numComments.setVisibility(ViewUtils.boolToVis(!isJobPost));
        holder.numPoints.setVisibility(ViewUtils.boolToVis(!isJobPost));
        holder.pointsLabel.setVisibility(ViewUtils.boolToVis(!isJobPost));
    }

    private void bindTextViews(Story story, ViewHolder holder) {
        holder.title.setText(story.title);
        holder.numPoints.setText(Integer.toString(story.numPoints));
        holder.numComments.setText(Integer.toString(story.numComments));
        holder.domain.setText((story.domain != null) ? story.domain : "");
    }

    // LISTENERS
    private View.OnClickListener mUserListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();
            UserActivity.startUserActivity(mContext, holder.story.username);

        }
    };

    private View.OnClickListener mShareListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();
            mShare.shareStory(holder.story);
        }
    };

    private View.OnClickListener mVoteListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();
            Story story = holder.story;
            story.upvote(mContext);
            hideControls(holder);
            notifyDataSetChanged();
        }
    };

    public View.OnClickListener mClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();
            CommentsTab tab = v.getId() == R.id.btn_comments ? CommentsTab.COMMENTS : CommentsTab.ARTICLE;
            boolean openInBrowser = new UserPrefs(mContext).getOpenInBrowser();

            // check for open in browser
            if (openInBrowser && tab == CommentsTab.ARTICLE) {
                mContext.startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(holder.story.url)));
            }

            // if this is in a MainActivity, let mStoryCallbacks handle it
            if (mStoryCallbacks != null) {
                setActiveStory(holder.story);
                mStoryCallbacks.showCommentsForStory(holder.story, tab);
            } else {
                // this is from SubmissionsFragment, and should start a new CommentsActivity
                MainActivity.startCommentsActivity(mContext, null, holder.story, tab);
            }
        }
    };

    private View.OnLongClickListener mLongListener = new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            ViewHolder holder = (ViewHolder) v.getTag();

            if (holder.ctrlContainer.getVisibility() == View.GONE) {

                // scroll to make controls visible
                int height = v.getHeight();
                int ctrlContainerHeight = mContext.getResources()
                        .getDimensionPixelSize(R.dimen.slideout_ctrl_height);
                int scrollY = height + Utils.getWindowLocation(mContext, v).y + ctrlContainerHeight
                        - Utils.getScreenSize(mContext).y;
                if (scrollY > 0) mList.smoothScrollBy(scrollY, 200);

                showControls(holder);

            } else {
                hideControls(holder);
            }

            return true;
        }
    };

    private void showControls(ViewHolder holder) {
        // animate the showing of controls
        Animation inAnim = AnimationUtils.loadAnimation(mContext, R.anim.ctrl_scale_down);
        AnimUtils.startInAnimation(holder.ctrlContainer, inAnim);

        // hide divider
        holder.divider.setVisibility(View.GONE);
    }

    private void hideControls(ViewHolder holder) {
        // animate the hiding of controls
        Animation outAnim = AnimationUtils.loadAnimation(mContext, R.anim.ctrl_scale_up);
        AnimUtils.startOutAnimation(holder.ctrlContainer, outAnim);

        // show divider
        holder.divider.setVisibility(View.VISIBLE);
    }

    public Bundle onSaveInstanceState(Bundle bundle) {
        bundle.putLong(ACTIVE_STORY, mActiveStoryId);
        bundle.putSerializable(STORIES, (Serializable) this.getArray());
        return bundle;
    }

    @SuppressWarnings("unchecked")
    public void onRestoreInstanceState(Bundle savedInstanceState) {
        mActiveStoryId = savedInstanceState.getLong(ACTIVE_STORY);
        List<Story> stories = (List<Story>) savedInstanceState.getSerializable(STORIES);
        clear();
        if (stories != null) addAll(stories);
    }

}
