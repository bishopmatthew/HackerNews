package com.airlocksoftware.hackernews.activity;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;
import android.widget.Toast;
import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.data.UserPrefs.Theme;
import com.airlocksoftware.hackernews.interfaces.RestartableActivity;
import com.airlocksoftware.hackernews.model.Page;
import com.airlocksoftware.holo.activities.ActionBarActivity;
import com.airlocksoftware.holo.checkable.CheckableView;
import com.airlocksoftware.holo.checkable.CheckableViewManager;
import com.airlocksoftware.holo.checkable.CheckableViewManager.OnCheckedViewChangedListener;
import com.airlocksoftware.holo.utils.Utils;
import com.airlocksoftware.holo.utils.ViewUtils;
import com.slidingmenu.lib.SlidingMenu;
import com.slidingmenu.lib.SlidingMenu.OnOpenListener;

/** Extends the ActionBarActivity from HoloTheme to add a SlidingMenu **/
public abstract class SlideoutMenuActivity extends ActionBarActivity implements RestartableActivity {

	private CheckableViewManager mSlideCheckManager;
	private UserPrefs mUserPrefs;
	private boolean mNeedRefresh = false;
	private boolean mInitialized = false;

	private OnOpenListener hideKeyboardListener = new SlidingMenu.OnOpenListener() {
		@Override
		public void onOpen() {
			InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
			inputManager.hideSoftInputFromWindow(getSlidingMenu().getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
		}
	};
	private OnClickListener mUpListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			toggle();
		}
	};

	// Constants
	private static final String TAG = SlideoutMenuActivity.class.getSimpleName();
	// private static final String NEED_REFRESH = SlideoutMenuActivity.class.getSimpleName() + ".needRefresh";
	private static final int UNCHECKED_ID = -1;
	private static final boolean HONEYCOMB_OR_GREATER = android.os.Build.VERSION.SDK_INT >= 11;

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		mUserPrefs = new UserPrefs(this);
		retrieveTheme();
		setWindowBackground();
		
		/* TODO I'm over the quota anyways -- going to move to Crashlytics in the next version.
		 * BugSenseHandler.initAndStartSession(getApplicationContext(), getString(R.string.bugsense_api_key)); 
		 */

		// initialize ActionBarActivity layout after setting theme and window background
		super.initialize();
		initializeSlidingMenu();
		setBehindContentView(R.layout.vw_slideoutmenu_content);

		// set up button action
		View upButton = getActionBarView().getUpButton();
		upButton.setOnClickListener(mUpListener);

		setupSlidingMenuItems();
		refreshLoginState();
		mInitialized = true;
	}

	private void setupSlidingMenuItems() {
		mSlideCheckManager = new CheckableViewManager();
		ViewGroup checkGroup = (ViewGroup) findViewById(R.id.slideout_checkable_group);
		for (View lvl1 : ViewUtils.directChildViews(checkGroup)) {
			if (lvl1 instanceof CheckableView) {
				mSlideCheckManager.register((CheckableView) lvl1);
				ViewUtils.fixBackgroundRepeat(lvl1);
			} else if (lvl1 instanceof ViewGroup) {
				for (View lvl2 : ViewUtils.directChildViews((ViewGroup) lvl1)) {
					mSlideCheckManager.register((CheckableView) lvl2);
					ViewUtils.fixBackgroundRepeat(lvl2);
				}
			}
		}
		setActiveMenuItem(-1); // clear check
		mSlideCheckManager.setOnCheckedChangedListener(mMenuListener);
	}

	private void retrieveTheme() {
		Theme theme = mUserPrefs.getTheme();
		switch (theme) {
		case LIGHT:
			setTheme(R.style.Light);
			break;
		case DARK:
			setTheme(R.style.Dark);
			break;
		}
	}

	private void setWindowBackground() {
		TypedValue windowBg = new TypedValue();
		if (getTheme() != null) getTheme().resolveAttribute(android.R.attr.windowBackground, windowBg, true);
		getWindow().setBackgroundDrawableResource(windowBg.resourceId);
	}

	private void initializeSlidingMenu() {
		SlidingMenu menu = super.getSlidingMenu();
		menu.setTouchModeAbove(SlidingMenu.TOUCHMODE_FULLSCREEN);
		menu.setTouchModeBehind(SlidingMenu.TOUCHMODE_MARGIN);
		menu.setBehindScrollScale(1.0f);
		menu.setFadeDegree(0.0f);
		menu.setShadowDrawable(R.drawable.grad_slideout_shadow);
		menu.setShadowWidth(Utils.dpToPixels(this, 15));

		// hide keyboard when sliding menu opens
		menu.setOnOpenListener(hideKeyboardListener);

		// figure out the width of the SlideoutMenu
		boolean landscape = getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE;
		int maxWidth = Utils.dpToPixels(this, (landscape) ? 560 : 280); // max width of behindview in dp
		int minWidth = Utils.dpToPixels(this, 58); // approx. width of up button in ActionBar
		Point size = Utils.getScreenSize(this);
		int offset = Math.max(size.x - maxWidth, minWidth);
		menu.setBehindOffset(offset);
	}

	@Override
	public void onResume() {
		super.onResume();

		// Update Login/Logout text and menu buttons
		refreshLoginState();

		// if theme has changed, restart activity TODO text size?
		Theme theme = mUserPrefs.getTheme();
		TypedValue outValue = new TypedValue();
		if (getTheme() != null) getTheme().resolveAttribute(R.attr.themeName, outValue, true);
		String themeName = theme.toString();
		if (!themeName.equals(outValue.string)) restartActivity();

		if (!mInitialized) {
			throw new RuntimeException(getString(R.string.slideoutMenuNotInitializedError));
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (event.getKeyCode() == KeyEvent.KEYCODE_SEARCH) {
			Intent intent = new Intent(SlideoutMenuActivity.this, SearchActivity.class);
			startActivity(intent);
		}
		return super.onKeyDown(keyCode, event);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	protected boolean needRefresh() {
		return mNeedRefresh;
	}

	/** Set which item should have checked state in the SlidingMenu. **/
	public void setActiveMenuItem(int id) {
		if (id == UNCHECKED_ID) mSlideCheckManager.clearCheck();
		else mSlideCheckManager.protectedCheck(id);
	}

	/** Removes the checked state from any items in the SlidingMenu. **/
	public void removeMenuCheckState() {
		setActiveMenuItem(UNCHECKED_ID);
	}

	public void showMenuItem(int id) {
		CheckableView child = mSlideCheckManager.findViewById(id);
		if (child != null) {
			child.setVisibility(View.VISIBLE);
			mSlideCheckManager.register(child);
		}
	}

	public void hideMenuItem(int id) {
		CheckableView child = mSlideCheckManager.findViewById(id);
		if (child != null) {
			child.setVisibility(View.GONE);
			mSlideCheckManager.deregister(child);
		}
	}

	/** change login / logout button & set username of user button **/
	private void refreshLoginState() {
		TextView username = (TextView) findViewById(R.id.txt_user);
		TextView txt = (TextView) findViewById(R.id.txt_login);
		if (mUserPrefs.isLoggedIn()) {
			txt.setText("Logout");
			username.setText(mUserPrefs.getUsername());
			showMenuItem(R.id.user_button);
		} else {
			hideMenuItem(R.id.user_button);
			txt.setText("Login");
		}
	}

	@Override
	public void restartActivity() {
		Intent intent = getIntent();
		overridePendingTransition(0, 0);
		intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
		finish();

		overridePendingTransition(0, 0);
		startActivity(intent);
	}

	private OnCheckedViewChangedListener mMenuListener = new OnCheckedViewChangedListener() {
		@Override
		public void onCheckedViewChanged(CheckableViewManager group, int newIndex, int oldIndex) {
			Context context = SlideoutMenuActivity.this;
			SlideoutMenuActivity activity = SlideoutMenuActivity.this;
			Intent intent = null;
			View child = group.getChildAt(newIndex);

			switch (child.getId()) {
			case R.id.front_page_button:
				intent = new Intent(activity, MainActivity.class);
				intent.putExtra(MainActivity.PAGE, Page.FRONT);
				break;
			case R.id.ask_button:
				intent = new Intent(activity, MainActivity.class);
				intent.putExtra(MainActivity.PAGE, Page.ASK);
				break;
			case R.id.best_button:
				intent = new Intent(activity, MainActivity.class);
				intent.putExtra(MainActivity.PAGE, Page.BEST);
				break;
			case R.id.active_button:
				intent = new Intent(activity, MainActivity.class);
				intent.putExtra(MainActivity.PAGE, Page.ACTIVE);
				break;
			case R.id.new_button:
				intent = new Intent(activity, MainActivity.class);
				intent.putExtra(MainActivity.PAGE, Page.NEW);
				break;
			case R.id.user_button:
				intent = new Intent(activity, UserActivity.class);
				intent.putExtra(UserActivity.USERNAME, mUserPrefs.getUsername());
				break;
			case R.id.submit_button:
				intent = new Intent(activity, SubmitActivity.class);
				break;
			case R.id.settings_button:
				intent = new Intent(activity, SettingsActivity.class);
				break;
			case R.id.about_button:
				intent = new Intent(activity, AboutActivity.class);
				break;
			case R.id.login_button:
				UserPrefs data = new UserPrefs(activity);
				if (data.isLoggedIn()) {
					data.logout();
					Toast.makeText(context, "Logging out", Toast.LENGTH_SHORT).show();
					refreshLoginState();
				} else {
					startActivity(new Intent(activity, LoginActivity.class));
					activity.overridePendingTransition(0, 0);
				}

				setActiveMenuItem(-1);
				break;
			}

			if (intent != null) {
				finish(); // top level activities won't go onto the back stack
				activity.overridePendingTransition(0, 0);
				startActivity(intent);
			}

			showAbove();
		}
	};

}