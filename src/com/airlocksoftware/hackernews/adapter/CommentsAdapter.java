package com.airlocksoftware.hackernews.adapter;

import android.content.Context;
import android.content.res.Resources;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.activity.MainActivity;
import com.airlocksoftware.hackernews.activity.MainActivity.CommentsTab;
import com.airlocksoftware.hackernews.activity.ReplyActivity;
import com.airlocksoftware.hackernews.activity.UserActivity;
import com.airlocksoftware.hackernews.model.Comment;
import com.airlocksoftware.hackernews.model.Story;
import com.airlocksoftware.hackernews.view.SharePopup;
import com.airlocksoftware.holo.adapters.GroupAdapter;
import com.airlocksoftware.holo.image.IconView;
import com.airlocksoftware.holo.type.FontText;
import com.airlocksoftware.holo.utils.AnimUtils;
import com.airlocksoftware.holo.utils.Utils;
import com.airlocksoftware.holo.utils.ViewUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * Adapter for CommentsFragment. Uses ViewHolder class to cache Views that need to have data re-bound when it changes.
 * **/
public class CommentsAdapter extends GroupAdapter<Comment> {

	private Context mContext;
	private SharePopup mShare;
	private ListView mList;
	private Story mParentStory;
	private int mContainerWidth = 0;

	private class ViewHolder {
		Comment comment;
		Story story;
		public FontText username, commentTxt, foldedTxt, storyTitleTxt, upvoteBtnTxt;
		public IconView upvoteIndicator, upvoteBtnIcon;
		public View depthMargin, depth, foldedContainer, ctrlContainer, upvoteBtn, userBtn, replyBtn, shareBtn,
				ctrlDivider, mainContainer;
	}

	// Constants
	private final int HIGHLIGHT_COLOR;
	private final int DEFAULT_COLOR;
	private final int[] DEPTH_COLORS = { R.color.depth_orange_light, R.color.depth_yellow, R.color.depth_orange_dark,
			R.color.depth_red, R.color.depth_purple };

	public CommentsAdapter(Context context, ListView list, SharePopup share) {
		mContext = context;
		mList = list;
		mShare = share;

		// preload colors from resources
		Resources res = mContext.getResources();
		for (int i = 0; i < DEPTH_COLORS.length; i++) {
			DEPTH_COLORS[i] = res.getColor(DEPTH_COLORS[i]);
		}
		HIGHLIGHT_COLOR = res.getColor(R.color.bright_accent);
		DEFAULT_COLOR = res.getColor(Utils.getThemedResourceId(mContext, R.attr.textColorPrimary));
	}

	@Override
	public View getView(Comment comment, View convertView, ViewGroup parent) {
		return getCommentView(null, comment, convertView, parent);
	}

	public View getCommentView(Story story, Comment comment, View convertView, ViewGroup parent) {
		// if mContainerWidth hasn't been set yet, set it to the width of the parent (with a minimum width of 300dp)
		if (mContainerWidth <= 0) {
			mContainerWidth = Math.max(parent.getWidth(), Utils.dpToPixels(mContext, 300));
		}

		ViewHolder holder;
		if (convertView == null) {
			LayoutInflater inflater = LayoutInflater.from(mContext);
			convertView = inflater.inflate(R.layout.vw_comment, parent, false);
			ViewUtils.fixBackgroundRepeat(convertView);
			holder = setupNewViewHolder(convertView);
			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		bindView(story, comment, holder);

		return convertView;
	}

	private ViewHolder setupNewViewHolder(View convertView) {
		ViewHolder holder = new ViewHolder();
		holder.mainContainer = (ViewGroup) convertView.findViewById(R.id.cnt_main);
		holder.username = (FontText) convertView.findViewById(R.id.txt_username);
		holder.commentTxt = (FontText) convertView.findViewById(R.id.txt_comment);
		holder.upvoteIndicator = (IconView) convertView.findViewById(R.id.icv_upvote_indicator);

		holder.depth = convertView.findViewById(R.id.depth);
		holder.depthMargin = convertView.findViewById(R.id.depth_margin);

		holder.foldedContainer = convertView.findViewById(R.id.cnt_folded);
		holder.foldedTxt = (FontText) convertView.findViewById(R.id.txt_folded);

		holder.ctrlContainer = convertView.findViewById(R.id.cnt_ctrl);
		holder.ctrlDivider = convertView.findViewById(R.id.divider_controls);
		holder.upvoteBtn = convertView.findViewById(R.id.btn_upvote);
		holder.upvoteBtnIcon = (IconView) convertView.findViewById(R.id.icv_upvote);
		holder.upvoteBtnTxt = (FontText) convertView.findViewById(R.id.txt_upvote);
		holder.userBtn = convertView.findViewById(R.id.btn_user);
		holder.replyBtn = convertView.findViewById(R.id.btn_reply);
		holder.shareBtn = convertView.findViewById(R.id.btn_share);

		holder.storyTitleTxt = (FontText) convertView.findViewById(R.id.txt_story_title);

		holder.upvoteBtn.setTag(holder);
		holder.userBtn.setTag(holder);
		holder.replyBtn.setTag(holder);
		holder.shareBtn.setTag(holder);
		holder.storyTitleTxt.setTag(holder);
		holder.commentTxt.setTag(holder);

		holder.mainContainer.setOnClickListener(foldListener);
		holder.mainContainer.setOnLongClickListener(ctrlListener);
		holder.foldedContainer.setOnClickListener(foldListener);
		holder.foldedContainer.setOnLongClickListener(ctrlListener);
		holder.commentTxt.setOnClickListener(foldListener);
		holder.commentTxt.setOnLongClickListener(ctrlListener);
		holder.commentTxt.setMovementMethod(LinkMovementMethod.getInstance());

		holder.userBtn.setOnClickListener(userListener);
		// Disabled reply button because it's broken
		holder.replyBtn.setVisibility(View.GONE);
//		holder.replyBtn.setOnClickListener(replyListener);
		holder.shareBtn.setOnClickListener(shareListener);
		holder.storyTitleTxt.setOnClickListener(threadListener);

		holder.mainContainer.setTag(holder);
		holder.foldedContainer.setTag(holder);
		holder.commentTxt.setTag(holder);
		return holder;
	}

	private void bindView(Story story, Comment comment, ViewHolder holder) {

		// set text & visibility of components
		holder.username.setText(comment.username + "  \u2022  " + comment.ago);
		holder.commentTxt.setText(comment.generateSpannedHtml());
		holder.ctrlContainer.setVisibility(View.GONE);
		holder.ctrlDivider.setVisibility(View.GONE);
		holder.comment = comment;
		holder.story = story;

		// visibility of reply button TODO always disabled temporarily because reply is broken
//		if (mParentStory != null) holder.replyBtn.setVisibility(mParentStory.isArchived ? View.GONE : View.VISIBLE);
		holder.replyBtn.setVisibility(View.GONE);

		// upvote icon color & click listener
		if (comment.isUpvoted) {
			holder.upvoteBtnTxt.setText("Upvoted!");
			holder.upvoteBtnTxt.setTextColor(HIGHLIGHT_COLOR);
			holder.upvoteBtnIcon.iconColor(HIGHLIGHT_COLOR);
			holder.upvoteBtn.setOnClickListener(null);
			holder.upvoteBtn.setClickable(false);
			holder.upvoteIndicator.setVisibility(View.VISIBLE);
		} else {
			holder.upvoteBtnTxt.setText("Upvote");
			holder.upvoteBtnTxt.setTextColor(DEFAULT_COLOR);
			holder.upvoteBtnIcon.iconColor(DEFAULT_COLOR);
			holder.upvoteBtn.setClickable(true);
			holder.upvoteBtn.setOnClickListener(voteListener);
			holder.upvoteIndicator.setVisibility(View.GONE);
		}

		// set depth indicator width and padding
		if (comment.depth > 0) {
			holder.depth.setVisibility(View.VISIBLE);
			holder.depthMargin.setVisibility(View.VISIBLE);

			// scale the depth indicators & margins based on the width of the container
			float depthWidth = mContainerWidth / 50.0f;

			LinearLayout.LayoutParams depthParams = (LinearLayout.LayoutParams) holder.depth.getLayoutParams();
			depthParams.width = (int) Math.min(depthWidth, Utils.dpToPixels(mContext, 10.0f)); // max width of 10dp
			holder.depth.setLayoutParams(depthParams);

			LinearLayout.LayoutParams depthMarginParams = (LinearLayout.LayoutParams) holder.depthMargin.getLayoutParams();
			depthMarginParams.width = (int) ((comment.depth - 1) * depthWidth);
			holder.depthMargin.setLayoutParams(depthMarginParams);
		} else {
			holder.depth.setVisibility(View.GONE);
			holder.depthMargin.setVisibility(View.GONE);
		}

		// setup depth indicator color
		int depthColorPos = (comment.depth) % DEPTH_COLORS.length;
		holder.depth.setBackgroundColor(DEPTH_COLORS[depthColorPos]);

		// setup folded indicator
		holder.foldedContainer.setVisibility((comment.isFolded) ? View.VISIBLE : View.GONE);
		holder.mainContainer.setVisibility((comment.isFolded) ? View.GONE : View.VISIBLE);
		if (comment.isFolded) {
			String foldedMessage = comment.username + " \u2022 " + comment.ago;
			if (comment.mChildCount == 1) {
				foldedMessage += "\n(1 child)";
			} else if (comment.mChildCount > 1) {
				foldedMessage += "\n(" + comment.mChildCount + " children)";
			} else {
				foldedMessage += "\n(no children)";
			}
			holder.foldedTxt.setText(foldedMessage);
		}

		// story title (for ThreadAdapter)
		if (story != null) {
			holder.storyTitleTxt.setVisibility(View.VISIBLE);
			holder.storyTitleTxt.setText("on: " + story.title);
		} else {
			holder.storyTitleTxt.setVisibility(View.GONE);
		}

		// remove the top margin if this is on a threads page
		// because the storyTitleText takes care of spacing with it's padding
		RelativeLayout.LayoutParams usernameParams = (LayoutParams) holder.username.getLayoutParams();
		usernameParams.topMargin = Utils.dpToPixels(mContext, (story == null) ? 10 : 0);
		holder.username.setLayoutParams(usernameParams);
	}

	public void setParentStory(Story parent) {
		mParentStory = parent;
	}

	private View.OnClickListener userListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			UserActivity.startUserActivity(mContext, holder.comment.username);
		}
	};

	private View.OnClickListener shareListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			mShare.shareComment(holder.comment);
		}
	};

	private View.OnClickListener replyListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			ReplyActivity.startCommentReplyActivity(mContext, holder.comment);
		}
	};

	private View.OnClickListener voteListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			Comment comment = holder.comment;
			if (comment.upvote(mContext)) {
				hideControls(holder);
				notifyDataSetChanged();
			} else {
				// login (handled automatically by Comment.upvote();)
			}
		}
	};

	private View.OnClickListener threadListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();

			MainActivity.startCommentsActivity(mContext, null, holder.story, CommentsTab.COMMENTS);
		}
	};

	private View.OnLongClickListener ctrlListener = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();
			if (holder.ctrlContainer.getVisibility() == View.GONE) {
				int scrollY = getScrollY(v);
				if (scrollY > 0) {
					// longer scroll distances need longer scroll times
					int millis = 100 + scrollY * 2;
					// TODO smooth scroll seems to be canceled by touch_up
					mList.smoothScrollBy(scrollY, millis);
				}
				showControls(holder);
			} else {
				hideControls(holder);
			}
			return true;
		}

		private int getScrollY(View v) {
			int viewHeight = v.getHeight();
			int ctrlContainerHeight = mContext.getResources()
																				.getDimensionPixelSize(R.dimen.slideout_ctrl_height);
			int windowLoc = Utils.getWindowLocation(mContext, v).y;
			int statusHeight = Utils.getStatusBarHeight(mContext);
			int screenHeight = Utils.getScreenSize(mContext).y;

			int scrollY = viewHeight + windowLoc + ctrlContainerHeight + statusHeight - screenHeight;
			return scrollY;
		}
	};

	private void hideControls(ViewHolder holder) {
		Animation outAnim = AnimationUtils.loadAnimation(mContext, R.anim.ctrl_scale_up);
		AnimUtils.startOutAnimation(holder.ctrlContainer, outAnim);
		holder.ctrlDivider.setVisibility(View.GONE);
	}

	private void showControls(ViewHolder holder) {
		Animation inAnim = AnimationUtils.loadAnimation(mContext, R.anim.ctrl_scale_down);
		AnimUtils.startInAnimation(holder.ctrlContainer, inAnim);
		holder.ctrlDivider.setVisibility(View.VISIBLE);
	}

	/**
	 * When the user clicks on the body of the comment, check if it's a link. If it isn't, the fold the comment & all it's
	 * children.
	 **/
	private View.OnClickListener foldListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ViewHolder holder = (ViewHolder) v.getTag();

			// check if this click was on a link
			boolean clickTargetIsLink = holder.commentTxt.getSelectionStart() != -1
					&& holder.commentTxt.getSelectionEnd() != -1;

			if (!clickTargetIsLink) {
				Comment parent = holder.comment;

				// hide child comments (if there are any)
				if (!parent.isFolded) {

					// get CommentsAdapter list of children
					List<Comment> allChildren = CommentsAdapter.this.getArray();

					// either create or clear the parent.mChildren List<Comment>
					if (parent.mChildren == null) {
						parent.mChildren = new ArrayList<Comment>();
					} else parent.mChildren.clear();

					// find index of parent comment, initialize foldedCommentsCount
					int startIndex = allChildren.indexOf(parent) + 1;
					int foldedCommentsCount = 0;

					// for comments with depth < currentComment.depth, move it to parent.mChildren
					for (int i = startIndex; i < allChildren.size(); i++) {
						Comment comment = allChildren.get(i);
						if (comment.depth > parent.depth) {
							parent.mChildren.add(comment);

							// be sure to include current comments' folded children in foldedCommentsCount
							foldedCommentsCount += 1 + comment.mChildCount;
						} else {
							// we've got a comment with depth > parent.depth, so we're done folding
							if (parent.mChildren.size() > 0) {
								allChildren.removeAll(parent.mChildren);
								parent.mChildCount = foldedCommentsCount;
							}
							break;
						}
					}

					// scroll to top of folded comment
					int scrollY = getScrollY(holder);
					if (scrollY < 0) {
						// long scroll distances need bigger scroll times
						int millis = 100 + Math.abs(scrollY) * 2;
						mList.smoothScrollBy(scrollY, millis);
					}

				} else if (parent.isFolded && parent.mChildCount > 0) {
					// unfold comments (if there are any)
					parent.mChildCount = 0;
					List<Comment> comments = CommentsAdapter.this.getArray();
					int startIndex = comments.indexOf(parent) + 1;
					comments.addAll(startIndex, parent.mChildren);
				}

				// update isFolded
				parent.isFolded = !parent.isFolded;
				notifyDataSetChanged();

			} else {
				// they clicked on a link - do nothing it should already be taken care of by autolink
			}
		}

		private int getScrollY(ViewHolder holder) {
			int windowLoc = Utils.getWindowLocation(mContext, holder.mainContainer).y;
			int statusHeight = Utils.getStatusBarHeight(mContext);
			int foldHeight = mContext.getResources()
																.getDimensionPixelSize(R.dimen.folded_comment_height);
			int scrollY = windowLoc - statusHeight - foldHeight;
			return scrollY;
		}
	};

}
