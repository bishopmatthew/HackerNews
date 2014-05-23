package com.airlocksoftware.hackernews.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.adapter.StoryAdapter;
import com.airlocksoftware.hackernews.interfaces.SharePopupInterface;
import com.airlocksoftware.hackernews.loader.StoryLoader;
import com.airlocksoftware.hackernews.model.Request;
import com.airlocksoftware.hackernews.model.Result;
import com.airlocksoftware.hackernews.model.StoryTimestamp;
import com.airlocksoftware.hackernews.parser.StoryParser.StoryResponse;
import com.airlocksoftware.holo.actionbar.interfaces.ActionBarClient;
import com.airlocksoftware.holo.actionbar.interfaces.ActionBarController;
import com.airlocksoftware.holo.image.IconView;
import com.airlocksoftware.holo.utils.ViewUtils;

public class SubmissionsFragment extends ListFragment implements ActionBarClient,
		LoaderManager.LoaderCallbacks<StoryResponse> {

	// State
	private StoryAdapter mAdapter;
	private String mUsername;
	private Request mRequest = Request.NEW;

	private Result mLastResult;
	private boolean mIsLoading = false;
	private int mScrollPosition;

	// Views
	private ListView mList;
	private View mError, mLoading, mMoreButton;
	private TextView mMoreButtonText;
	private IconView mMoreIcon;

	// Listeners
	private View.OnClickListener mMoreListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mRequest = Request.MORE;

			// change more link text & icon
			mMoreButtonText.setText(getActivity().getString(R.string.loading_));
			mMoreIcon.setVisibility(View.GONE);

			getLoaderManager().restartLoader(0, null, SubmissionsFragment.this);
			refreshContentVisibility();
		}
	};

	// Constants
	private static final String SCROLL_POSITION = SubmissionsFragment.class.getSimpleName() + ".scrollPosition";
	public static final String USERNAME = SubmissionsFragment.class.getSimpleName() + ".username";
	@SuppressWarnings("unused")
	private static final String TAG = SubmissionsFragment.class.getSimpleName();

	// Constructor
	public SubmissionsFragment() {
		// default no-arg constructor
	}

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		Bundle args = getArguments();
		mUsername = args.getString(USERNAME); // TODO can args ever be null?
		if(mUsername == null) throw new NullPointerException("No username was passed to SubmissionsFragment!");
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frg_loadinglist, container, false);
		findViews(view);
		setupMoreButton(inflater);
		
		mAdapter = new StoryAdapter(getActivity(), mList, ((SharePopupInterface) getActivity()).getSharePopup(), null, null);
		setListAdapter(mAdapter);

		refreshContentVisibility();
		return view;
	}

	private void findViews(View view) {
		mError = view.findViewById(R.id.cnt_error);
		mLoading = view.findViewById(R.id.cnt_loading);

		ViewUtils.fixBackgroundRepeat(mError);
		ViewUtils.fixBackgroundRepeat(mLoading);

		mList = (ListView) view.findViewById(android.R.id.list);
	}

	private void setupMoreButton(LayoutInflater inflater) {
		mMoreButton = inflater.inflate(R.layout.vw_more_link, mList, false);
		mMoreButton.setOnClickListener(mMoreListener);
		mMoreButton.setVisibility(View.GONE);
		mMoreButtonText = (TextView) mMoreButton.findViewById(R.id.txt_more);
		mMoreIcon = (IconView) mMoreButton.findViewById(R.id.icv_more);
		mList.addFooterView(mMoreButton, null, false);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// restoring saved state
		if (savedInstanceState != null) {
			mUsername = savedInstanceState.getString(USERNAME);
			mScrollPosition = savedInstanceState.getInt(SCROLL_POSITION, -1);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(USERNAME, mUsername);
		outState.putInt(SCROLL_POSITION, mScrollPosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	public Loader<StoryResponse> onCreateLoader(int id, Bundle args) {
		mIsLoading = true;
		if(mUsername == null) throw new NullPointerException("No username was passed to SubmissionsFragment!");
		return new StoryLoader(getActivity(), mUsername, mRequest);
	}

	@Override
	public void onLoadFinished(Loader<StoryResponse> loader, StoryResponse response) {
		mIsLoading = false;

		// Variable for showing Toasts
		Toast msg;

		// if NULL_RESPONSE, make a toast! Cheers! Drink responsibly; return early.
		if (response == null || response.isNull()) {
			msg = Toast.makeText(getActivity(), getActivity().getString(R.string.error_no_user), Toast.LENGTH_SHORT);
			msg.show();
			return;
		}

		mLastResult = response.result;

		switch (mLastResult) {

		case SUCCESS: // first page
			mAdapter.clear();
			mAdapter.addAll(response.stories);
			break;

		case MORE: // new data from web
			mAdapter.addAll(response.stories);
			break;

		case FNID_EXPIRED: // the link was expired - refresh the page
			msg = Toast.makeText(getActivity(), getActivity().getString(R.string.link_expired), Toast.LENGTH_SHORT);
			msg.show();

			// start loader with refresh request
			mRequest = Request.REFRESH;
			getLoaderManager().restartLoader(0, null, this);
			break;

		case FAILURE: // Show error message
			msg = Toast.makeText(getActivity(), getActivity().getString(R.string.problem_downloading_content), Toast.LENGTH_SHORT);
			msg.show();
			break;

		default:
			break;
		}
		refreshMoreButtonVisibility(response.timestamp);

		refreshContentVisibility();
	}

	private void refreshMoreButtonVisibility(StoryTimestamp timestamp) {
		mMoreButtonText.setText(getActivity().getString(R.string.load_more));
		mMoreIcon.setVisibility(View.VISIBLE);
		
		// show more link (timestamp is only not null if we have a valid moreFnid)
		mMoreButton.setVisibility(ViewUtils.boolToVis(timestamp != null));
	}

	@Override
	public void onLoaderReset(Loader<StoryResponse> arg0) {
		// do nothing
	}

	@Override
	public void setupActionBar(Context context, ActionBarController ab) {
		ab.setTitleText(context.getString(R.string.submissions));
	}

	@Override
	public void cleanupActionBar(Context context, ActionBarController ab) {
		ab.setTitleText(null);
	}

	/** Determines which view (List, Error, or Loading) show be visible. **/
	private void refreshContentVisibility() {
		// can't refresh visibility until / unless all views are created
		if (mAdapter == null || mError == null || mLoading == null) return;

		boolean loadingVis = mIsLoading;
		boolean errorVis = mLastResult == Result.FAILURE && !mIsLoading;
		boolean listVis = !loadingVis && !errorVis;

		mError.setVisibility(ViewUtils.boolToVis(errorVis));
		mLoading.setVisibility(ViewUtils.boolToVis(loadingVis));
		mList.setVisibility(ViewUtils.boolToVis(listVis));
	}

}
