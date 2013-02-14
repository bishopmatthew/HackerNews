package com.airlocksoftware.hackernews.activity;

import org.apache.commons.lang3.StringUtils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.TypedArray;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewPager;
import android.support.v4.view.ViewPager.OnPageChangeListener;
import android.view.View;
import android.view.ViewGroup;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.adapter.StoryAdapter;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.fragment.CommentsFragment;
import com.airlocksoftware.hackernews.fragment.StoryFragment;
import com.airlocksoftware.hackernews.fragment.WebFragment;
import com.airlocksoftware.hackernews.interfaces.SharePopupInterface;
import com.airlocksoftware.hackernews.interfaces.TabletLayout;
import com.airlocksoftware.hackernews.loader.AsyncLoginUpdater;
import com.airlocksoftware.hackernews.model.Page;
import com.airlocksoftware.hackernews.model.Story;
import com.airlocksoftware.hackernews.view.SearchOverflow;
import com.airlocksoftware.hackernews.view.SharePopup;
import com.airlocksoftware.hackernews.view.TextSettingsOverflow;
import com.airlocksoftware.holo.actionbar.ActionBarView;
import com.airlocksoftware.holo.actionbar.TwoPaneController;
import com.airlocksoftware.holo.actionbar.TwoPaneController.Side;
import com.airlocksoftware.holo.actionbar.interfaces.ActionBarController;
import com.airlocksoftware.holo.adapters.FragmentPagerArrayAdapter;
import com.airlocksoftware.holo.checkable.CheckableView;
import com.airlocksoftware.holo.checkable.CheckableViewManager;
import com.airlocksoftware.holo.checkable.CheckableViewManager.OnCheckedViewChangedListener;
import com.airlocksoftware.holo.image.IconView;
import com.airlocksoftware.holo.interfaces.OnBackPressedListener;
import com.airlocksoftware.holo.utils.FragmentUtils;
import com.airlocksoftware.holo.utils.Utils;
import com.airlocksoftware.holo.utils.ViewUtils;
import com.airlocksoftware.holo.webview.DisableableViewPager;
import com.slidingmenu.lib.SlidingMenu;

/**
 * The MainActivity is the host for the StoryFragment, CommentsFragment, and WebFragment.
 * 
 * It's also responsible for:
 * - deciding whether to show a two-pane or one-pane layout
 * - managing the ActionBar tabs for Comments & Web Fragment
 * - managing the SharePopup
 * - managing the Search and TextSettings in the overflow menu
 * - implementing the TabletLayout interface
 * - managing communications between fragments by implementing any Callback interfaces it's child fragments require
 * **/
public class MainActivity extends SlideoutMenuActivity implements SharePopupInterface, TabletLayout,
		CommentsFragment.Callbacks, StoryFragment.Callbacks {

	// Fragments
	private CommentsFragment mCommentsFragment;
	private WebFragment mWebFragment;
	private StoryFragment mStoryFragment;

	// ViewPager for CommentsFragment & WebFragment.
	private DisableableViewPager mCommentsViewPager;
	private FragmentPagerArrayAdapter mCommentsViewPagerAdapter;

	// tabs & tab manager
	private CheckableViewManager mTabManager;
	private ViewGroup mTabs;

	private SearchOverflow mSearch;
	private TextSettingsOverflow mTextSettings;
	private SharePopup mSharePopup;

	private ActivityType mActivityType;
	private Page mPage = Page.FRONT; // Which page to display in StoryFragment
	private Story mStory; // Which story to display in CommentsFragment & WebFragment
	private boolean mIsJobsPost = false; // if the current story is a YCombinator jobs post
	private CommentsTab mInitialTabPosition = CommentsTab.COMMENTS;
	private int mCurrentTabPosition = NO_CURRENT_POSITION;
	private boolean mOpenInBrowser = false;

	// Enums
	/** The "type" of this Activity, based on screen width and the data we have. **/
	public enum ActivityType {
		TABLET, STORY, COMMENTS;
	}

	/** Which tab we are showing. **/
	public enum CommentsTab {
		COMMENTS, ARTICLE;
	}

	// Constants
	private static final int VIEWPAGER_ID = R.id.viewpager;
	private static final int NO_CURRENT_POSITION = -1;

	private static final int COMMENTS_POS = 0;
	private static final int ARTICLE_POS = 1;
	private static final String STORY_FRAG_TAG = StoryFragment.class.getName();
	private static final String COMMENTS_FRAG_TAG = FragmentUtils.makeFragmentPagerTag(VIEWPAGER_ID, COMMENTS_POS);
	private static final String WEB_FRAG_TAG = FragmentUtils.makeFragmentPagerTag(VIEWPAGER_ID, ARTICLE_POS);

	public static final String IS_COMMENTS_ACTIVITY = MainActivity.class.getSimpleName() + ".isCommentsActivity";
	public static final String STORY = MainActivity.class.getSimpleName() + ".story";
	public static final String INITIAL_TAB_POSITION = MainActivity.class.getSimpleName() + ".initialTabPosition";
	public static final String PAGE = MainActivity.class.getSimpleName() + ".page";
	private static final String TAG = MainActivity.class.getSimpleName();

	// Listeners
	private OnCheckedViewChangedListener mTabListener = new OnCheckedViewChangedListener() {
		@Override
		public void onCheckedViewChanged(CheckableViewManager manager, int newIndex, int oldIndex) {
			// forwards tab check onto the ViewPager
			mCommentsViewPager.setCurrentItem(newIndex);
		}
	};

	private OnPageChangeListener mPageListener = new ViewPager.OnPageChangeListener() {
		@Override
		public void onPageScrollStateChanged(int arg0) {
			// No implementation necessary
		}

		@Override
		public void onPageScrolled(int arg0, float arg1, int arg2) {
			// No implementation necessary
		}

		@Override
		// called every time the tab changed, whether by swiping or clicking
		public void onPageSelected(int position) {
			Context context = MainActivity.this;
			ActionBarController controller = getActionBarView().getController();
			if (mCurrentTabPosition == COMMENTS_POS) mCommentsFragment.cleanupActionBar(context, controller);
			if (mCurrentTabPosition == ARTICLE_POS) mWebFragment.cleanupActionBar(context, controller);
			if (position == COMMENTS_POS) mCommentsFragment.setupActionBar(context, controller);
			if (position == ARTICLE_POS) mWebFragment.setupActionBar(context, controller);

			View child = mTabManager.getChildAt(position);
			mTabManager.protectedCheck(child.getId());
			getActionBarView().hideOverflow();
			mCurrentTabPosition = position;
		}
	};

	/**
	 * Used to add a back pressed listener that fires AFTER overlay manager gets a chance to handle back presses in
	 * ActionBarActivity.
	 **/
	private OnBackPressedListener mBackListener = new OnBackPressedListener() {
		@Override
		public boolean onBackPressed() {
			if (mActivityType == ActivityType.COMMENTS) {
				Intent intent = new Intent(MainActivity.this, MainActivity.class);
				intent.putExtra(PAGE, mPage);
				startActivity(intent);

				// Start a new MainActivity in StoryMode. We're faking the animations to look like the app is going back

				// Retrieve the animations set in the theme applied to this activity in the manifest.
				TypedArray activityStyle = getTheme().obtainStyledAttributes(new int[] { android.R.attr.windowAnimationStyle });
				int windowAnimationStyleResId = activityStyle.getResourceId(0, 0);
				activityStyle.recycle();

				// Now retrieve the resource ids of the actual animations used in the animation style pointed to by
				// the window animation resource id.
				activityStyle = getTheme().obtainStyledAttributes(windowAnimationStyleResId,
						new int[] { android.R.attr.activityCloseEnterAnimation, android.R.attr.activityCloseExitAnimation });
				int enterAnim = activityStyle.getResourceId(0, 0);
				int exitAnim = activityStyle.getResourceId(1, 0);
				activityStyle.recycle();

				if (enterAnim != 0 && exitAnim != 0) overridePendingTransition(enterAnim, exitAnim);

				finish();
				return true;
			} else return false;
		}
	};

	private View.OnClickListener mUpListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			MainActivity.this.onBackPressed();
		}
	};

	private View.OnLongClickListener mUpLongListener = new View.OnLongClickListener() {
		@Override
		public boolean onLongClick(View v) {
			toggle();
			return true;
		}
	};

	// Public methods

	@Override
	public void createActionBarController() {
		// determine what kind of device we're on & which layout to show (width > 5 inches)
		PointF size = Utils.pixelsToInches(this, Utils.getScreenSize(this));
		if (size.x >= 5.0) {
			mActivityType = ActivityType.TABLET;
			getActionBarView().setController(new TwoPaneController(this, getActionBarView()));
		} else {
			super.createActionBarController(); // create the default OnePaneController
		}
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		super.addOnBackPressedListener(mBackListener);
		retrieveUserData();
		retrieveBundles(savedInstanceState, getIntent().getExtras());
		mSharePopup = new SharePopup(this, null, getOverlayManager());
		setupSearchAndTextSettingsOverflow();
		retrieveFragmentsByTag();
		updateUserCookie();

		// determine ActivityType and setup appropriately
		if (mActivityType == ActivityType.TABLET) { // two-pane
			setupTabletLayout(savedInstanceState);
		} else {
			if (mStory != null) { // CommentsActivity
				mActivityType = ActivityType.COMMENTS;
				setupCommentsLayout(savedInstanceState);
				if (mStoryFragment != null) mStoryFragment.onDestroy();
			} else { // StoryActivity
				mActivityType = ActivityType.STORY;
				setupStoryLayout(savedInstanceState);
			}
		}
	}

	private void updateUserCookie() {
		AsyncLoginUpdater task = new AsyncLoginUpdater(getApplicationContext());
		task.execute();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		outState.putSerializable(PAGE, mPage);
		outState.putSerializable(STORY, mStory);
		outState.putSerializable(INITIAL_TAB_POSITION, mInitialTabPosition);
		super.onSaveInstanceState(outState);
	}

	// private methods

	private void setupTabletLayout(Bundle savedInstanceState) {
		setContentView(R.layout.act_tablet);
		setupStoryFragment(savedInstanceState);
		setupCommentsViewPager(savedInstanceState);

		// enable the sliding menu swipe only in the margin
		SlidingMenu menu = super.getSlidingMenu();
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_MARGIN);
	}

	private void setupStoryLayout(Bundle savedInstanceState) {
		setContentView(R.layout.act_storylist);
		setupStoryFragment(savedInstanceState);

		// enable sliding menu swipe from fullscreen
		SlidingMenu menu = super.getSlidingMenu();
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
	}

	private void setupCommentsLayout(Bundle savedInstanceState) {
		if (mIsJobsPost && mOpenInBrowser) {
			// send it straight to browser
			startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse(mStory.url)));
		}

		super.setActiveMenuItem(-1);
		setContentView(R.layout.act_comments);
		setupCommentsViewPager(savedInstanceState);

		if (mIsJobsPost) {
			// ensure WebFragment is shown
			mPageListener.onPageSelected(ARTICLE_POS);
			mCommentsViewPager.setCurrentItem(ARTICLE_POS);
		}

		// 'Up' version of ActionBar
		ActionBarView ab = getActionBarView();
		IconView upIndicator = ab.getUpIndicator();
		upIndicator.iconSource(R.drawable.ic_actionup_back);
		View upButton = ab.getUpButton();
		upButton.setOnClickListener(mUpListener);
		upButton.setOnLongClickListener(mUpLongListener);

		// disable sliding menu touchmode from the margin
		SlidingMenu menu = super.getSlidingMenu();
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_NONE);
	}

	/** Creates the StoryFragment, attaches it to the Activity, and sets up the ActionBar. **/
	private void setupStoryFragment(Bundle savedInstanceState) {
		// setup story fragment
		if (mStoryFragment == null) {
			FragmentManager fm = getSupportFragmentManager();
			FragmentTransaction ft = fm.beginTransaction();
			mStoryFragment = StoryFragment.newInstance(mPage);
			ft.replace(R.id.frg_storylist, mStoryFragment, STORY_FRAG_TAG);
			ft.disallowAddToBackStack();
			ft.commit();
		}

		mStoryFragment.setPage(mPage);

		ActionBarController controller = getActionBarView().getController();
		// if it's a TwoPaneController, set the left side to be active while StoryFragment is setting up
		if (controller instanceof TwoPaneController) {
			TwoPaneController twoPane = (TwoPaneController) controller;
			twoPane.setActiveSide(Side.LEFT);
			mStoryFragment.setupActionBar(this, twoPane);
			setupStoryTitleAndSlideoutMenuState();
			twoPane.setActiveSide(Side.RIGHT);
		} else {
			mStoryFragment.setupActionBar(this, controller);
			setupStoryTitleAndSlideoutMenuState();
		}
	}

	/** Sets the ActionBar title & the checked menu item in the sliding menu based on the value of mPage. **/
	private void setupStoryTitleAndSlideoutMenuState() {
		// set SlidingMenu to display correct Page
		int titleResId = -1;
		int menuItemResId = -1;
		switch (mPage) {
		case FRONT:
			titleResId = R.string.front_page;
			menuItemResId = R.id.front_page_button;
			break;
		case ASK:
			titleResId = R.string.ask;
			menuItemResId = R.id.ask_button;
			break;
		case BEST:
			titleResId = R.string.best;
			menuItemResId = R.id.best_button;
			break;
		case ACTIVE:
			titleResId = R.string.active;
			menuItemResId = R.id.active_button;
			break;
		case NEW:
			titleResId = R.string.new_page;
			menuItemResId = R.id.new_button;
			break;
		default:
			break;
		}

		ActionBarController controller = getActionBarView().getController();
		controller.setTitleText(getString(titleResId));
		setActiveMenuItem(menuItemResId);
	}

	/**
	 * Find the ViewPager for Comments & WebFragment, attach the adapter, create the fragments if necessary, and
	 * create the tabs for switching between them.
	 **/
	private void setupCommentsViewPager(Bundle savedInstanceState) {
		// setup ViewPager
		mCommentsViewPager = (DisableableViewPager) findViewById(VIEWPAGER_ID);
		mCommentsViewPager.setOnPageChangeListener(mPageListener);
		mCommentsViewPagerAdapter = new FragmentPagerArrayAdapter(getSupportFragmentManager());
		mCommentsViewPager.setAdapter(mCommentsViewPagerAdapter);

		// add Comments & Web Fragments
		setupCommentsFragment(savedInstanceState);
		setupWebFragment(savedInstanceState);
		setupCommentsTabs();
		refreshTabsVisibility();
	}

	private void setupCommentsFragment(Bundle savedInstanceState) {
		if (mCommentsFragment == null) {
			mCommentsFragment = (CommentsFragment) Fragment.instantiate(this, CommentsFragment.class.getName());
		}
		if (mStory != null) mCommentsFragment.setStory(mStory);
		mCommentsViewPagerAdapter.addItem(mCommentsFragment);
		mCommentsViewPagerAdapter.notifyDataSetChanged();
	}

	private void setupWebFragment(Bundle savedInstanceState) {
		if (mWebFragment == null) {
			mWebFragment = (WebFragment) Fragment.instantiate(this, WebFragment.class.getName());
		}
		if (mStory != null) mWebFragment.setUrl(mStory.url);
		mCommentsViewPagerAdapter.addItem(mWebFragment);
		mCommentsViewPagerAdapter.notifyDataSetChanged();
	}

	private void setupCommentsTabs() {
		// inflate tab layout & add to ActionBar
		ActionBarController controller = getActionBarView().getController();
		ViewGroup titleGroup = controller.getTitleGroup();

		mTabs = (ViewGroup) getLayoutInflater().inflate(R.layout.vw_actionbar_tabs_comments, titleGroup, false);
		titleGroup.addView(mTabs);

		// setup tab manager
		mTabManager = new CheckableViewManager();
		for (View tab : ViewUtils.directChildViews(mTabs)) {
			mTabManager.register((CheckableView) tab);
		}
		mTabManager.setOnCheckedChangedListener(mTabListener);

		// make sure the correct tabs is checked
		int position = mInitialTabPosition == CommentsTab.COMMENTS || mOpenInBrowser ? COMMENTS_POS : ARTICLE_POS;
		mPageListener.onPageSelected(position);
		mCommentsViewPager.setCurrentItem(position);
	}

	private void setupSearchAndTextSettingsOverflow() {
		ActionBarController controller = getActionBarView().getController();
		// setup search box
		if (mSearch == null) mSearch = new SearchOverflow(this, this);
		controller.addOverflowView(mSearch);
		// setup text settings
		if (mTextSettings == null) mTextSettings = new TextSettingsOverflow(this, this);
		controller.addOverflowView(mTextSettings);
		mTextSettings.refreshVisibility();
	}

	/**
	 * Shows / hides the ActionBar tabs for CommentsFragment / WebFragment based on the current state. Also enables /
	 * disables paging.
	 **/
	private void refreshTabsVisibility() {
		boolean tabsEnabled = mStory != null && StringUtils.isNotBlank(mStory.url) && !mOpenInBrowser && !mIsJobsPost;
		mTabs.setVisibility(tabsEnabled ? View.VISIBLE : View.GONE);
		mCommentsViewPager.setPagingEnabled(tabsEnabled);
	}

	/** Retrieves data from the Extras or savedInstanceState bundles. **/
	private void retrieveBundles(Bundle savedInstanceState, Bundle extras) {
		Bundle bundle = null;
		if (savedInstanceState != null) bundle = savedInstanceState;
		else if (extras != null) bundle = extras;

		if (bundle != null) {
			mPage = (Page) bundle.getSerializable(PAGE);
			setStory((Story) bundle.getSerializable(STORY));
			mInitialTabPosition = (CommentsTab) bundle.getSerializable(INITIAL_TAB_POSITION);
		}

		// default values
		if (mInitialTabPosition == null) mInitialTabPosition = CommentsTab.COMMENTS;
		if (mPage == null) mPage = Page.FRONT;
	}

	/** Get data from UserData shared preferences. **/
	private void retrieveUserData() {
		UserPrefs userData = new UserPrefs(this);
		// check if we should open stories in the browser or in the app
		mOpenInBrowser = userData.getOpenInBrowser();
	}

	/** Sets the Story to display in CommentsFragment & WebFragment **/
	private void setStory(Story story) {
		mStory = story;
		mIsJobsPost = Story.isYCombinatorJobPost(mStory);
	}

	/**
	 * Try to retrieve fragments by tag, will be null if not found (and thus will get created by their respective
	 * "setup" methods
	 **/
	private void retrieveFragmentsByTag() {
		FragmentManager fm = getSupportFragmentManager();
		mStoryFragment = (StoryFragment) fm.findFragmentByTag(STORY_FRAG_TAG);
		mCommentsFragment = (CommentsFragment) fm.findFragmentByTag(COMMENTS_FRAG_TAG);
		mWebFragment = (WebFragment) fm.findFragmentByTag(WEB_FRAG_TAG);
	}

	// SharePopup interface
	@Override
	public SharePopup getSharePopup() {
		return mSharePopup;
	}

	// TabletLayout interface
	@Override
	public boolean isTabletLayout() {
		return mActivityType == ActivityType.TABLET;
	}

	// CommentsFragment.Callbacks interface
	@Override
	/** Receive notification when CommentsActivity has received a new Story from it's loader
	 * @param story - the story to load. Cannot be null.**/
	public void receivedStory(Story story) {
		setStory(story);

		if (mActivityType == ActivityType.TABLET) {

			// load new URL in webFragment
			if (!mOpenInBrowser && StringUtils.isNotBlank(mStory.url)) {
				mWebFragment.loadNewUrl(mStory.url);
			}

			// set active story in StoryAdapter
			StoryAdapter adapter = mStoryFragment.getStoryAdapter();
			adapter.setActiveStory(mStory);
		}
	}

	@Override
	public boolean commentsFragmentIsInLayout() {
		return mActivityType != ActivityType.STORY;
	}

	// StoryFragment.Callbacks interface
	@Override
	public void showCommentsForStory(Story story, CommentsTab initialTab) {
		if (mActivityType == ActivityType.TABLET) {
			setStory(story);

			// send new data to fragments
			if (!mIsJobsPost) mCommentsFragment.loadNewStory(story);
			if (StringUtils.isNotBlank(mStory.url)) mWebFragment.loadNewUrl(mStory.url);

			// refresh whether tabs are visible
			refreshTabsVisibility();

			// make sure the correct fragment is shown
			int currentFragment = (initialTab == CommentsTab.COMMENTS && !mIsJobsPost) ? COMMENTS_POS : ARTICLE_POS;
			mCommentsViewPager.setCurrentItem(currentFragment);

		} else {
			// Don't use startCommentsActivity. It causes an animation we don't want because it sets
			// Intent.FLAG_ACTIVITY_CLEAR_TOP.
			// We don't need that flag because we can finish the MainActivity from here.
			Intent intent = getCommentsIntent(this, mPage, story, initialTab);
			startActivity(intent);
			finish();
		}
	}

	@Override
	public Story getActiveStory() {
		if (mCommentsFragment != null) return mCommentsFragment.getStory();
		else return null;
	}

	@Override
	public boolean storyFragmentIsInLayout() {
		return mActivityType != ActivityType.COMMENTS;
	}

	// Static startup methods for MainActivity with Comments layout.

	/** Starts a new MainActivity with data for showing a CommentsFragment. **/
	public static void startCommentsActivity(Context context, Page page, int storyId, String url, CommentsTab initialTab) {
		Story story = new Story();
		story.storyId = storyId;
		story.url = url;
		startCommentsActivity(context, page, story, initialTab);
	}

	/** Starts a new MainActivity with data for showing a CommentsFragment. **/
	public static void startCommentsActivity(Context context, Page page, Story story, CommentsTab initialTab) {
		Intent intent = getCommentsIntent(context, page, story, initialTab);
		// whenever you go back to the MainActivity, make sure that it's at the top of the Activity stack
		// this prevents the user from going through a circular path of Activities (which will eventually crash the app)
		intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
		context.startActivity(intent);
		// prevent this from going on the back stack
		if (context instanceof Activity) ((Activity) context).finish();
	}

	/** Returns an intent for starting a new MainActivity with data for showing a CommentsFragment. **/
	public static Intent getCommentsIntent(Context context, Page page, Story story, CommentsTab initialTab) {
		Intent intent = new Intent(context, MainActivity.class);
		intent.putExtra(MainActivity.INITIAL_TAB_POSITION, initialTab);
		intent.putExtra(MainActivity.STORY, story);
		intent.putExtra(MainActivity.PAGE, page);
		return intent;
	}

}
