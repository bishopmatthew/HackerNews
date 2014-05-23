package com.airlocksoftware.hackernews.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.activity.ReplyActivity;
import com.airlocksoftware.hackernews.activity.UserActivity;
import com.airlocksoftware.hackernews.adapter.CommentsAdapter;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.interfaces.SharePopupInterface;
import com.airlocksoftware.hackernews.interfaces.TabletLayout;
import com.airlocksoftware.hackernews.loader.CommentsLoader;
import com.airlocksoftware.hackernews.model.Comment;
import com.airlocksoftware.hackernews.model.Request;
import com.airlocksoftware.hackernews.model.Result;
import com.airlocksoftware.hackernews.model.Story;
import com.airlocksoftware.hackernews.parser.CommentsParser.CommentsResponse;
import com.airlocksoftware.hackernews.view.SharePopup;
import com.airlocksoftware.holo.actionbar.ActionBarButton;
import com.airlocksoftware.holo.actionbar.ActionBarButton.Priority;
import com.airlocksoftware.holo.actionbar.interfaces.ActionBarClient;
import com.airlocksoftware.holo.actionbar.interfaces.ActionBarController;
import com.airlocksoftware.holo.image.IconView;
import com.airlocksoftware.holo.utils.Utils;
import com.airlocksoftware.holo.utils.ViewUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.Serializable;
import java.util.List;

/**
 * Displays a page of comments with the parent story as a header. Uses CommentsLoader to get data from the cache or the
 * web.
 **/
public class CommentsFragment extends Fragment implements ActionBarClient, LoaderCallbacks<CommentsResponse> {

	/**
	 * The story whose comments we are loading & displaying. Initialized to a new, empty Story to avoid null check on the
	 * object, but it's fields should always be null checked.
	 **/
	private Story mStory = new Story();
	{
		mStory.storyId = NO_STORY_ID;
	}

	private Request mRequest = Request.NEW;
	private Result mLastResult = Result.EMPTY;
	private boolean mIsLoading = false;
	private boolean mOpenInBrowser = false;
	private boolean mIsPaused = false;

	// Interfaces to the Activity
	private TabletLayout mTabletLayout;
	private CommentsFragment.Callbacks mCallbacks;

	// Views & Adapters
	private ListView mList;
	private CommentsAdapter mAdapter;

	/** The backing array of comments stored in onSaveInstanceState and restored in onActivityCreated **/
	private List<Comment> mTempComments;

	private View mHeaderView, mError, mEmpty, mLoading;
	private TextView mHeaderTitle, mHeaderUsername, mHeaderPoints, mHeaderSelfText;
	private IconView mUserIcon, mShareIcon, mUpvoteIcon;
//			mReplyIcon; disabled because the way replies work has changed
	private View mUpvoteButton, mSelfTextContainer;
	private ActionBarButton mBrowserButton, mRefreshButton;
	private SharePopup mShare;

	// Listeners
	private View.OnClickListener mVoteListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			if (mStory.upvote(getActivity())) bindStoryHeader();
		}
	};

	private View.OnClickListener mRefreshListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mRequest = Request.REFRESH;
			getLoaderManager().restartLoader(0, null, CommentsFragment.this);
		}
	};

	private View.OnClickListener mBrowserListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			Intent intent = new Intent(Intent.ACTION_VIEW).setData(Uri.parse(mStory.url));
			getActivity().startActivity(intent);
		}
	};

	private View.OnClickListener mReplyListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			ReplyActivity.startStoryReplyActivity(getActivity(), mStory);
		}
	};

	private View.OnClickListener mUserListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			UserActivity.startUserActivity(getActivity(), mStory.username);
		}
	};

	private View.OnClickListener mShareListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mShare.shareStory(mStory);
		}
	};

	// Constants
	public static final String STORY = CommentsFragment.class.getSimpleName() + ".story";
	public static final String LIST_STATE = CommentsFragment.class.getSimpleName() + ".listState";
	public static final String COMMENTS = CommentsFragment.class.getSimpleName() + ".comments";
	@SuppressWarnings("unused")
	private static final String TAG = CommentsFragment.class.getSimpleName();

	/** Time (in milliseconds) before a set of comments is considered expired and should be reloaded. **/
	private static final long CACHE_EXPIRATION = 1000 * 60 * 20; // 20 minutes
	public static final int NO_STORY_ID = -1;
	private int TEXT_COLOR_PRIMARY;
	private int HIGHLIGHT_COLOR;

	public CommentsFragment() {
		// default empty constructor
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		if (container == null) return null;
		View view = inflater.inflate(R.layout.frg_comments, container, false);
		return view;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		Activity activity = getActivity();

		// cache colors
		Resources res = activity.getResources();
		HIGHLIGHT_COLOR = res.getColor(R.color.bright_accent);
		TEXT_COLOR_PRIMARY = res.getColor(Utils.getThemedResourceId(activity, R.attr.textColorPrimary));

		// get the Callbacks from parent Activity
		if (mCallbacks == null || mTabletLayout == null) retrieveCallbacks(activity);

		// return early if fragment isn't in layout
		if (!mCallbacks.commentsFragmentIsInLayout()) {
			return;
		}

		mOpenInBrowser = new UserPrefs(activity).getOpenInBrowser();

		if (savedInstanceState != null) {
			setStory((Story) savedInstanceState.getSerializable(STORY));
			// restore the List<Comment> that backs the ListAdapter (can't use the automatic one from the
			// loader.onLoadFinished because it breaks comment folding)
			mTempComments = (List<Comment>) savedInstanceState.getSerializable(COMMENTS);
		}

		// start loading
		getLoaderManager().initLoader(0, null, this);

		// find all the views
		findViews(savedInstanceState);

		// get share interface
		mShare = ((SharePopupInterface) activity).getSharePopup();

		// setup adapter
		if (mAdapter == null) {
			mAdapter = new CommentsAdapter(activity, mList, mShare);
			mList.setAdapter(mAdapter);

			if (mTempComments != null) mAdapter.addAll(mTempComments);
		}

		ensureActionBarButtonsCreated(activity);
		refreshContentVisibility();
		refreshActionBarButtonVisibility();
	}

	private void findViews(Bundle savedInstanceState) {
		// make sure to findViewById from container view
		View container = getActivity().findViewById(R.id.cnt_comments);
		findLoadingAndErrorViews(container);
		setupHeader();
		findHeaderViews();

		// get list
		mList = (ListView) container.findViewById(android.R.id.list);
		mList.addHeaderView(mHeaderView, null, false);
	}

	private void findHeaderViews() {
		mHeaderTitle = (TextView) mHeaderView.findViewById(R.id.txt_title);
		mHeaderUsername = (TextView) mHeaderView.findViewById(R.id.txt_username);
		mHeaderSelfText = (TextView) mHeaderView.findViewById(R.id.txt_self);
		mHeaderPoints = (TextView) mHeaderView.findViewById(R.id.txt_points);

		mUserIcon = (IconView) mHeaderView.findViewById(R.id.icv_user);
		mShareIcon = (IconView) mHeaderView.findViewById(R.id.icv_share);
		mUpvoteIcon = (IconView) mHeaderView.findViewById(R.id.icv_upvote);
//		mReplyIcon = (IconView) mHeaderView.findViewById(R.id.icv_reply);

		mUserIcon.setOnClickListener(mUserListener);
		mShareIcon.setOnClickListener(mShareListener);
//		mReplyIcon.setOnClickListener(mReplyListener);

		mUpvoteButton = mHeaderView.findViewById(R.id.btn_upvote);
		mSelfTextContainer = mHeaderView.findViewById(R.id.cnt_txt_self);
	}

	private void setupHeader() {
		mHeaderView = LayoutInflater.from(getActivity())
																.inflate(R.layout.vw_comment_header, null);
		mHeaderView.setVisibility(View.GONE);
	}

	private void findLoadingAndErrorViews(View container) {
		mLoading = container.findViewById(R.id.cnt_loading);
		mEmpty = container.findViewById(R.id.cnt_emtpy);
		mError = container.findViewById(R.id.cnt_error);
		mError.findViewById(R.id.btn_error)
					.setOnClickListener(mRefreshListener);
		ViewUtils.fixBackgroundRepeat(mError);
		ViewUtils.fixBackgroundRepeat(mEmpty);
		ViewUtils.fixBackgroundRepeat(mLoading);
	}

	@Override
	public void onResume() {
		super.onResume();

		// set mTempComments to null here because on orientation change onLoadFinished is called (twice) from
		// Fragment.performStart(). onResume is called after that so we clear mTempComments.
		mTempComments = null;
	}

	@Override
	public void onPause() {
		super.onPause();
		mIsPaused = true;
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(STORY, mStory);
		if (mAdapter != null) outState.putSerializable(COMMENTS, (Serializable) mAdapter.getArray());
		super.onSaveInstanceState(outState);
	}

	// Loader Callbacks
	@Override
	public Loader<CommentsResponse> onCreateLoader(int id, Bundle args) {
		mIsLoading = true;
		refreshContentVisibility();
		return new CommentsLoader(getActivity(), mRequest, mStory.storyId);
	}

	@Override
	public void onLoadFinished(Loader<CommentsResponse> loader, CommentsResponse response) {
		if (mIsPaused) {
			// this loading came after onPause & onResume were called, but all data is already set so we ignore it
			mIsPaused = false;
			return;
		}

		// CommentsFragment.onLoadFinished exits early if this fragment isn't part of the layout anymore.
		if (!mCallbacks.commentsFragmentIsInLayout()) {
			return;
		}

		Story story = response.story;
		mIsLoading = false;
		mLastResult = response.result;

		if (response.result == Result.SUCCESS) {

			if (mTempComments == null) {
				// setup adapter & list
				mAdapter.clear();
				mAdapter.addAll(response.comments);

				// if it was a new request, we should scroll to the top of the page
				if (mRequest == Request.NEW) {
					mList.setSelection(0);
				}

			} else {
				// Comments have already been restored from List<Comment> in onActivityCreated() and onCreateView()
				// this fixes the bug where Comments that have been folded would get duplicated
				// onLoadFinished gets called twice on orientation change (seems like a bug in Fragment.performStart())
			}

			// check for cache expiration
			if (System.currentTimeMillis() - response.timestamp.time > CACHE_EXPIRATION) {
				// still show stuff, but restart loading
				mRequest = Request.REFRESH;
				getLoaderManager().restartLoader(0, null, this);
			}

			// if we're coming from ThreadsFragment or SearchActivity, we don't know the URL until now. Notify Callbacks,
			if (StringUtils.isNotBlank(story.url) && (mStory.url == null || !mStory.url.equals(story.url))) {
				mCallbacks.receivedStory(mStory);
			}

			// setup story header
			setStory(response.story);
			bindStoryHeader();
		}

		refreshContentVisibility();
	}

	@Override
	public void onLoaderReset(Loader<CommentsResponse> loader) {
		// no implementation necessary
	}

	@Override
	public void setupActionBar(Context context, ActionBarController controller) {
		ensureActionBarButtonsCreated(context);
		controller.addButton(mBrowserButton);
		controller.addButton(mRefreshButton);
	}

	@Override
	public void cleanupActionBar(Context context, ActionBarController controller) {
		controller.removeButton(mRefreshButton);
		controller.removeButton(mBrowserButton);
	}

	/**
	 * Called to set the inital story. Either this method or setStoryId should be called immediately after creating the
	 * fragment.
	 **/
	public void setStory(Story story) {
		mStory = story;
		refreshActionBarButtonVisibility();
		if (mAdapter != null) mAdapter.setParentStory(mStory);
	}

	/** Used by MainActivity to set the active story on StoryFragment. **/
	public Story getStory() {
		return mStory;
	}

	/**
	 * Called to set the inital story. Either this method or setStoryId should be called immediately after creating the
	 * fragment.
	 **/
	public void setStoryId(long storyId) {
		mStory.storyId = storyId;
	}

	/** Called when this Fragment should display a new Story in a Tablet layout. **/
	public void loadNewStory(Story story) {
		setStory(story);
		mRequest = Request.NEW;
		mAdapter.clear();
		getLoaderManager().restartLoader(0, null, CommentsFragment.this);
	}

	private void ensureActionBarButtonsCreated(Context context) {
		// create ActionBarButtons
		if (mBrowserButton == null) {
			mBrowserButton = new ActionBarButton(context);
			mBrowserButton.text(context.getString(R.string.open_in_browser))
										.icon(R.drawable.ic_action_web)
										.priority(Priority.HIGH)
										.onClick(mBrowserListener);
		}

		// setup refresh button
		if (mRefreshButton == null) {
			mRefreshButton = new ActionBarButton(context);
			mRefreshButton.text(context.getString(R.string.refresh_comments))
										.icon(R.drawable.ic_action_refresh)
										.priority(Priority.HIGH)
										.onClick(mRefreshListener);
		}
	}

	/** Bind data from mStory to views in mHeader **/
	private void bindStoryHeader() {
		mHeaderTitle.setText(mStory.title);
		mHeaderUsername.setText(mStory.username + "  \u2022  " + mStory.ago);
		mHeaderPoints.setText(Integer.toString(mStory.numPoints));

		// setup upvote button
		if (mStory.isUpvoted) {
			// mUpvoteButton.setClickable(false);
			mUpvoteButton.setOnClickListener(null);
			mUpvoteIcon.iconColor(HIGHLIGHT_COLOR);
		} else {
			// mUpvoteButton.setClickable(true);
			mUpvoteButton.setOnClickListener(mVoteListener);
			mUpvoteIcon.iconColor(TEXT_COLOR_PRIMARY);
		}

		// setup self text
		boolean hasSelfText = StringUtils.isNotBlank(mStory.selfText);
		if (hasSelfText) mHeaderSelfText.setText(Html.fromHtml(mStory.selfText));
		mSelfTextContainer.setVisibility(ViewUtils.boolToVis(hasSelfText));

		// setup reply button
//		mReplyIcon.setVisibility(ViewUtils.boolToVis(!mStory.isArchived)); // disabled because reply is broken
//		mReplyIcon.setVisibility(ViewUtils.boolToVis(!mStory.isArchived));
		mHeaderView.findViewById(R.id.icv_reply).setVisibility(View.GONE);
		mHeaderView.setVisibility(View.VISIBLE);
	}

	/**
	 * Ensure that the Activity this fragment is attached to implements the CommentsFragment.Callbacks & TabletLayout
	 * interface,
	 * and store it in mCallbacks.
	 **/
	private void retrieveCallbacks(Activity activity) {
		if (activity instanceof CommentsFragment.Callbacks) {
			mCallbacks = (CommentsFragment.Callbacks) activity;
		} else {
			throw new RuntimeException("The parent activity of a CommentFragment must implement CommentsFragment.Callbacks");
		}

		if (activity instanceof TabletLayout) {
			mTabletLayout = (TabletLayout) activity;
		} else {
			throw new RuntimeException("The parent activity of a CommentFragment must implement TabletLayout");
		}
	}

	private void refreshContentVisibility() {
		// can't refresh visibility until / unless all views are created
		if (mAdapter == null || mList == null || mError == null || mLoading == null || mEmpty == null) return;

		boolean isJobPost = Story.isYCombinatorJobPost(mStory);
		boolean errorVis = mLastResult == Result.FAILURE && mAdapter.getCount() < 1 && !mIsLoading;
		boolean emptyVis = (mLastResult == Result.EMPTY && mStory.storyId == NO_STORY_ID && !mIsLoading) || isJobPost;
		boolean loadingVis = mIsLoading && mAdapter.getCount() < 1 && !errorVis && !emptyVis;
		boolean listVis = !(emptyVis || errorVis || loadingVis || loadingVis);

		mRefreshButton.icon(errorVis ? R.drawable.ic_action_refresh_error : R.drawable.ic_action_refresh);
		mRefreshButton.showProgress(mIsLoading);
		mRefreshButton.setVisibility(ViewUtils.boolToVis(!emptyVis));

		mError.setVisibility(ViewUtils.boolToVis(errorVis));
		mEmpty.setVisibility(ViewUtils.boolToVis(emptyVis));
		mLoading.setVisibility(ViewUtils.boolToVis(loadingVis));
		mList.setVisibility(ViewUtils.boolToVis(listVis));
	}

	private void refreshActionBarButtonVisibility() {
		boolean showBrowser = mOpenInBrowser && StringUtils.isNotBlank(mStory.url);
		if (mBrowserButton != null) mBrowserButton.setVisibility(ViewUtils.boolToVis(showBrowser));
	}

	public interface Callbacks {
		/**
		 * Implemented by the parent Activity of this fragment. Used to notify the Activity if/when this fragment has
		 * received a new Story from CommentsLoader.
		 **/
		public void receivedStory(Story story);

		/**
		 * Allows the CommentsFragment to check if it is part of the layout in MainActivity after being recreated on
		 * orientation change.
		 * If it is not in layout, it's method's should return early.
		 **/
		public boolean commentsFragmentIsInLayout();
	}

}
