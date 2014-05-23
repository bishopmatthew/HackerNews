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
import com.airlocksoftware.hackernews.adapter.ThreadsAdapter;
import com.airlocksoftware.hackernews.interfaces.SharePopupInterface;
import com.airlocksoftware.hackernews.loader.ThreadsLoader;
import com.airlocksoftware.hackernews.model.Request;
import com.airlocksoftware.hackernews.model.Result;
import com.airlocksoftware.hackernews.model.StoryTimestamp;
import com.airlocksoftware.hackernews.parser.CommentsParser.ThreadsResponse;
import com.airlocksoftware.holo.actionbar.interfaces.ActionBarClient;
import com.airlocksoftware.holo.actionbar.interfaces.ActionBarController;
import com.airlocksoftware.holo.image.IconView;
import com.airlocksoftware.holo.utils.ViewUtils;

public class ThreadsFragment extends ListFragment implements ActionBarClient,
		LoaderManager.LoaderCallbacks<ThreadsResponse> {

	// State
	private ListView mList;
	private ThreadsAdapter mAdapter;

	private View mError, mLoading, mMoreButton;
	private TextView mMoreButtonText;
	private IconView mMoreIcon;

	private int mScrollPosition;
	private String mUsername;

	Request mRequest = Request.EMPTY;
	private Result mLastResult;
	private boolean mIsLoading = false;

	// Listeners
	private View.OnClickListener mMoreListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			mRequest = Request.MORE;

			// change more link text & icon
			mMoreButtonText.setText(getActivity().getString(R.string.loading_));
			mMoreIcon.setVisibility(View.GONE);

			getLoaderManager().restartLoader(0, null, ThreadsFragment.this);
			refreshContentVisibility();
		}
	};

	// Constants
	private static final String SCROLL_POSITION = ThreadsFragment.class.getSimpleName() + ".scrollPosition";
	@SuppressWarnings("unused")
	private static final String TAG = ThreadsFragment.class.getSimpleName();
	public static final String USERNAME = ThreadsFragment.class.getSimpleName() + ".username";

	public ThreadsFragment() {
		// default no-arg constructor
	}

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		mUsername = getArguments().getString(USERNAME); // TODO can args ever be null?
		getLoaderManager().initLoader(0, null, this);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.frg_loadinglist, container, false);
		findViews(view);
		setupMoreButton(inflater);
		mAdapter = new ThreadsAdapter(getActivity(), mList, ((SharePopupInterface) getActivity()).getSharePopup());
		setListAdapter(mAdapter);
		refreshContentVisibility();
		return view;
	}

	private void setupMoreButton(LayoutInflater inflater) {
		// inflate & add the more button
		mMoreButton = inflater.inflate(R.layout.vw_more_link, mList, false);
		mMoreButton.setOnClickListener(mMoreListener);
		mMoreButton.setVisibility(View.GONE);
		mMoreButtonText = (TextView) mMoreButton.findViewById(R.id.txt_more);
		mMoreIcon = (IconView) mMoreButton.findViewById(R.id.icv_more);
		mList.addFooterView(mMoreButton, null, false);
	}

	private void findViews(View view) {
		mError = view.findViewById(R.id.cnt_error);
		mLoading = view.findViewById(R.id.cnt_loading);
		ViewUtils.fixBackgroundRepeat(mError);
		ViewUtils.fixBackgroundRepeat(mLoading);

		mList = (ListView) view.findViewById(android.R.id.list);
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		// restoring saved state
		if (savedInstanceState != null) {
			mUsername = savedInstanceState.getString(USERNAME);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(USERNAME, mUsername);
		outState.putInt(SCROLL_POSITION, mScrollPosition);
		super.onSaveInstanceState(outState);
	}

	@Override
	public Loader<ThreadsResponse> onCreateLoader(int id, Bundle args) {
		mIsLoading = true;
		return new ThreadsLoader(getActivity(), mRequest, mUsername);
	}

	@Override
	public void onLoadFinished(Loader<ThreadsResponse> loader, ThreadsResponse response) {
		mIsLoading = false;
		mLastResult = response.result;

		switch (mLastResult) {

		case SUCCESS: // first page
			mAdapter.clear();
			mAdapter.addThreads(response.threads);
			break;

		case MORE: // new data from web
			mAdapter.addThreads(response.threads);
			break;

		case FNID_EXPIRED: // the link was expired - refresh the page
			Toast.makeText(getActivity(), getActivity().getString(R.string.link_expired), Toast.LENGTH_SHORT)
						.show();

			// start loader with refresh request
			mRequest = Request.REFRESH;
			getLoaderManager().restartLoader(0, null, this);
			break;

		case FAILURE: // Show error message
			Toast.makeText(getActivity(), getActivity().getString(R.string.problem_downloading_content), Toast.LENGTH_SHORT)
						.show();
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
	public void onLoaderReset(Loader<ThreadsResponse> loader) {
		// TODO Auto-generated method stub
	}

	@Override
	public void setupActionBar(Context context, ActionBarController ab) {
		ab.setTitleText(context.getString(R.string.threads));
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
