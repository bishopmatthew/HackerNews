package com.airlocksoftware.hackernews.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.holo.utils.ViewUtils;

public class SettingsActivity extends SlideoutMenuActivity {

	private View mBrowserButton;
	private View mBugReportsButton;

	private boolean mOpenInBrowser;
	private boolean mSubmitBugReports;
	private UserPrefs mUserPrefs;

	private OnClickListener mBrowserListener = new OnClickListener() {
		@Override
		public void onClick(View v) {
			mOpenInBrowser = !mOpenInBrowser;
			mUserPrefs.saveOpenInBrowser(mOpenInBrowser);
			notifyDataSetChanged();
		}
	};

	private OnClickListener mBugReportsListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mSubmitBugReports = !mSubmitBugReports;
			mUserPrefs.saveOpenInBrowser(mSubmitBugReports);
			notifyDataSetChanged();
		}
	};

	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);
		setContentView(R.layout.act_settings);
		ViewUtils.fixBackgroundRepeat(findViewById(R.id.scrollview));
		findAndBindViews();
		retrieveUserPrefs();
		notifyDataSetChanged();
		setupActionBar();
	}

	private void setupActionBar() {
		getActionBarView().getController()
											.setTitleText(getString(R.string.settings));
		setActiveMenuItem(R.id.settings_button);
	}

	private void retrieveUserPrefs() {
		mUserPrefs = new UserPrefs(this);
		mOpenInBrowser = mUserPrefs.getOpenInBrowser();
		mSubmitBugReports = mUserPrefs.getBugsenseEnabled();
	}

	private void findAndBindViews() {
		mBrowserButton = findViewById(R.id.btn_browser);
		mBugReportsButton = findViewById(R.id.btn_bug_reports);

		mBrowserButton.setOnClickListener(mBrowserListener);
		mBugReportsButton.setOnClickListener(mBugReportsListener);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private void notifyDataSetChanged() {
		ImageView browserCheckbox = (ImageView) mBrowserButton.findViewById(R.id.img_checkbox);
		if (mOpenInBrowser) browserCheckbox.setImageResource(R.drawable.chkbox_chkd_dark);
		else browserCheckbox.setImageResource(R.drawable.chkbox_default_dark);

		ImageView bugReportCheckbox = (ImageView) mBugReportsButton.findViewById(R.id.img_checkbox);
		if (mSubmitBugReports) bugReportCheckbox.setImageResource(R.drawable.chkbox_chkd_dark);
		else bugReportCheckbox.setImageResource(R.drawable.chkbox_default_dark);
	}
}
