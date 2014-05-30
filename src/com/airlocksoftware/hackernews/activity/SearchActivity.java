package com.airlocksoftware.hackernews.activity;

import java.util.Arrays;

import org.apache.commons.lang3.StringUtils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.adapter.SearchAdapter;
import com.airlocksoftware.hackernews.loader.SearchLoader;
import com.airlocksoftware.hackernews.loader.SearchLoader.SearchResult;
import com.airlocksoftware.hackernews.model.Request;
import com.airlocksoftware.hackernews.model.Result;
import com.airlocksoftware.holo.actionbar.ActionBarButton;
import com.airlocksoftware.holo.actionbar.ActionBarButton.Priority;
import com.airlocksoftware.holo.adapters.SpinnerArrayAdapter;
import com.airlocksoftware.holo.type.FontEdit;
import com.airlocksoftware.holo.type.FontSpinner;
import com.airlocksoftware.holo.utils.ViewUtils;

/**
 * Perform searches using the www.hnsearch.com API. EditText goes in the ActionBar, and the SortType and SearchType as
 * set via Spinners in the header of the ListView. The search is performed in the background by SearchLoader.
 **/
public class SearchActivity extends BackActivity implements LoaderManager.LoaderCallbacks<SearchResult> {

	// State
	private Request mRequest = Request.NEW;
	private SortType mSort = SortType.RELEVANCE;
	private SearchType mSearchType = SearchType.ALL;
	private String mQuery;
	private int mPage = 0;

	private Result mLastResult;
	private boolean mIsLoading;

	// Views & Adapters
	private SearchAdapter mAdapter;
	private ListView mListView;
	private View mSearchHeader;
	private View mMoreFooter;

	private View mError, mLoading, mNoResults;
	private ActionBarButton mSearchButton;

	private FontSpinner mSortSpinner, mSearchTypeSpinner;
	private FontEdit mSearchBox;

	// Listeners
	private OnClickListener mMoreListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mRequest = Request.MORE;
			mPage += 1;
			getSupportLoaderManager().restartLoader(0, null, SearchActivity.this);
		}
	};

	private OnClickListener mSearchBtnListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			performSearch();
		}
	};

	private OnEditorActionListener mSearchKeyboardListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_SEARCH) {
				performSearch();
				return true;
			} else return false;
		}
	};

	private SpinnerArrayAdapter<SortType> mSortAdapter = new SpinnerArrayAdapter<SortType>(this) {
		@Override
		public String getItemText(int position) {
			return getItem(position).getDisplayName();
		}

		@Override
		public void onItemClick(int position) {
			mSort = getItem(position);
			mSortSpinner.setText(mSort.getDisplayName());
			mSortSpinner.getDialog()
									.dismiss();
		}
	};

	private SpinnerArrayAdapter<SearchType> mSearchTypeAdapter = new SpinnerArrayAdapter<SearchType>(this) {
		@Override
		public String getItemText(int position) {
			return getItem(position).getDisplayName();
		}

		@Override
		public void onItemClick(int position) {
			SearchType current = getItem(position);
			mSearchType = current;
			mSearchTypeSpinner.setText(current.getDisplayName());
			mSearchTypeSpinner.getDialog()
												.dismiss();
		}
	};

	// Constants
	public static final String QUERY = SearchActivity.class.getSimpleName() + ".query";
	public static final String SORT = SearchActivity.class.getSimpleName() + ".sort";
	public static final String SEARCH_TYPE = SearchActivity.class.getSimpleName() + ".searchType";
	public static final int NUM_ITEMS_PER_PAGE = 10;

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		retrieveBundles(savedState, getIntent().getExtras());
		if (StringUtils.isBlank(mQuery)) mRequest = Request.EMPTY;
		LayoutInflater inflater = getLayoutInflater();
		setContentView(R.layout.act_search);
		findViews();
		addHeader(inflater);
		setupFooter(inflater);
		setupSpinners();
		setupListAdapter();
		setupActionBar(inflater);
		getSupportLoaderManager().initLoader(0, null, this);
		refreshContentVisibility();
	}

	private void setupListAdapter() {
		mAdapter = new SearchAdapter(this);
		mListView.setAdapter(mAdapter);
	}

	private void setupActionBar(LayoutInflater inflater) {
		mSearchBox = (FontEdit) inflater.inflate(R.layout.vw_actionbar_search, getActionBarView().getController()
																																															.getTitleGroup(), false);
		mSearchBox.setOnEditorActionListener(mSearchKeyboardListener);
		if (mQuery != null) mSearchBox.setText(mQuery);

		getActionBarView().getController()
				.getTitleGroup()
				.addView(mSearchBox);

		mSearchButton = new ActionBarButton(this).priority(Priority.HIGH);
		mSearchButton.setOnClickListener(mSearchBtnListener);

		getActionBarView().getController()
				.addButton(mSearchButton.text("Search")
				.icon(R.drawable.ic_action_search));
	}

	/** Create and add "more" footer **/
	private void setupFooter(LayoutInflater inflater) {
		mMoreFooter = inflater.inflate(R.layout.vw_more_link, mListView, false);
		mMoreFooter.setOnClickListener(mMoreListener);
		mMoreFooter.setVisibility(View.GONE);
		mListView.addFooterView(mMoreFooter, null, false);
	}

	private void setupSpinners() {
		// setup sort & search type spinners
		mSortAdapter.addAll(Arrays.asList(SortType.values()));
		mSortAdapter.setSelectedPosition(mSort.ordinal());
		mSearchTypeAdapter.addAll(Arrays.asList(SearchType.values()));
		mSearchTypeAdapter.setSelectedPosition(mSearchType.ordinal());

		mSortSpinner = (FontSpinner) mSearchHeader.findViewById(R.id.spin_sort);
		mSortSpinner.setAdapter(mSortAdapter);
		mSortSpinner.setText(mSortAdapter.getItemText(mSort.ordinal()));

		mSearchTypeSpinner = (FontSpinner) mSearchHeader.findViewById(R.id.spin_type);
		mSearchTypeSpinner.setAdapter(mSearchTypeAdapter);
		mSearchTypeSpinner.setText(mSearchTypeAdapter.getItemText(mSearchType.ordinal()));
	}

	/** setup header & adapters -- TODO put header in ActionBar if there's room (tablet layout) **/
	private void addHeader(LayoutInflater inflater) {
		mSearchHeader = inflater.inflate(R.layout.vw_search_header, mListView, false);
		ViewUtils.fixBackgroundRepeat(mSearchHeader);
		mListView.addHeaderView(mSearchHeader);
	}

	private void findViews() {
		mListView = (ListView) findViewById(android.R.id.list);
		mError = findViewById(R.id.cnt_error);
		mLoading = findViewById(R.id.cnt_loading);
		mNoResults = findViewById(R.id.cnt_noresults);

		// fix background repeat issues
		ViewUtils.fixBackgroundRepeat(mError);
		ViewUtils.fixBackgroundRepeat(mLoading);
		ViewUtils.fixBackgroundRepeat(mNoResults);

		mListView.setItemsCanFocus(true);
	}

	private void retrieveBundles(Bundle savedState, Bundle extras) {
		if (savedState != null) {
			mQuery = savedState.getString(QUERY);
			mSort = (SortType) savedState.getSerializable(SORT);
			mSearchType = (SearchType) savedState.getSerializable(SEARCH_TYPE);
		}

		if (extras != null) {
			mQuery = extras.getString(QUERY);
		}
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putString(QUERY, mQuery);
		outState.putSerializable(SORT, mSort);
		outState.putSerializable(SEARCH_TYPE, mSearchType);
		super.onSaveInstanceState(outState);
	}

	// LOADER INTERFACE
	@Override
	public Loader<SearchResult> onCreateLoader(int id, Bundle args) {
		mIsLoading = true;
		refreshContentVisibility();
		return new SearchLoader(this, mRequest, mQuery, mSort, mSearchType, mPage);
	}

	@Override
	public void onLoadFinished(Loader<SearchResult> loader, SearchResult data) {
		mIsLoading = false;
		mLastResult = data.result;

		if (data.result != Result.MORE) {
			// clear adapter unless this is a "more" result
			mAdapter.clear();
		}

		// hide more footer
		mMoreFooter.setVisibility(View.GONE);

		if ((data.result == Result.SUCCESS || data.result == Result.MORE) && data.items.size() > 0) {
			// successful run with new query or "more"
			mAdapter.addAll(data.items);

			// if there are more results for this query, show more button. Otherwise leave it hidden.
			if (data.hits - (data.page * NUM_ITEMS_PER_PAGE) > 0) mMoreFooter.setVisibility(View.VISIBLE);
		}

		refreshContentVisibility();
	}

	@Override
	public void onLoaderReset(Loader<SearchResult> loader) {
		// do nothing
	}

	// PRIVATE METHODS
	private void performSearch() {
		// hide keyboard
		InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
		inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);

		// get search term, reset start position,
		if (mSearchBox.getText() != null) {
			mQuery = mSearchBox.getText().toString();
		}

		mPage = 0;
		mRequest = Request.NEW;
		getSupportLoaderManager().restartLoader(0, null, SearchActivity.this);
	}

	/** Determines which view (Error, NoResults, Loading) show be visible. **/
	private void refreshContentVisibility() {
		// can't refresh visibility until / unless all views are created
		if (mAdapter == null || mNoResults == null || mError == null || mLoading == null) return;

		boolean adapterEmpty = mAdapter.getCount() < 1;
		boolean loadingVis = mIsLoading && adapterEmpty;
		boolean errorVis = mLastResult == Result.FAILURE && !mIsLoading;
		// Result.EMPTY refers to initial run of loader, not "no results" TODO should probably make this more logical
		boolean noResultsVis = !errorVis && !mIsLoading && adapterEmpty && mLastResult != Result.EMPTY;

		mSearchButton.showProgress(mIsLoading);

		mNoResults.setVisibility(ViewUtils.boolToVis(noResultsVis));
		mError.setVisibility(ViewUtils.boolToVis(errorVis));
		mLoading.setVisibility(ViewUtils.boolToVis(loadingVis));
	}

	// Enums
	@SuppressLint("DefaultLocale")
	public enum SortType {
		RELEVANCE, DATE;
		// RELEVANCE, DATE, POINTS;

		public String getDisplayName() {
			return StringUtils.capitalize(name().toLowerCase());
		}
	}

	@SuppressLint("DefaultLocale")
	public enum SearchType {
		ALL, STORIES, COMMENTS, USERS;

		public String getDisplayName() {
			return StringUtils.capitalize(name().toLowerCase());
		}
	}

}
