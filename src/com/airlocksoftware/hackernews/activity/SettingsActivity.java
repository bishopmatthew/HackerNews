package com.airlocksoftware.hackernews.activity;

import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.holo.utils.ViewUtils;

public class SettingsActivity extends SlideoutMenuActivity {

	private boolean mOpenInBrowser;
	private boolean mSubmitBugReports;
	private boolean mCompressData;

	private View mBrowserButton;
	private View mBugReportsButton;
	private View mCompressDataButton;

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
			mUserPrefs.saveBugsenseEnabled(mSubmitBugReports);
			notifyDataSetChanged();
		}
	};

	private OnClickListener mCompressDataListener = new OnClickListener() {

		@Override
		public void onClick(View v) {
			mCompressData = !mCompressData;
			mUserPrefs.saveCompressData(mCompressData);
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
		getActionBarView().getController().setTitleText(getString(R.string.settings));
		setActiveMenuItem(R.id.settings_button);
	}

	private void retrieveUserPrefs() {
		mUserPrefs = new UserPrefs(this);
		mOpenInBrowser = mUserPrefs.getOpenInBrowser();
		mSubmitBugReports = mUserPrefs.getBugsenseEnabled();
		mCompressData = mUserPrefs.getCompressData();
	}

	private void findAndBindViews() {
		mBrowserButton = findViewById(R.id.btn_browser);
		mBugReportsButton = findViewById(R.id.btn_bug_reports);
		mCompressDataButton = findViewById(R.id.btn_compress_data);

		mBrowserButton.setOnClickListener(mBrowserListener);
		mBugReportsButton.setOnClickListener(mBugReportsListener);
		mCompressDataButton.setOnClickListener(mCompressDataListener);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
	}

	private void notifyDataSetChanged() {
		ImageView browserCheckbox = (ImageView) mBrowserButton.findViewById(R.id.img_checkbox_browser);
		if (mOpenInBrowser) browserCheckbox.setImageResource(R.drawable.chkbox_chkd_dark);
		else browserCheckbox.setImageResource(R.drawable.chkbox_default_dark);

		ImageView bugReportCheckbox = (ImageView) mBugReportsButton.findViewById(R.id.img_checkbox_bug_reports);
		if (mSubmitBugReports) bugReportCheckbox.setImageResource(R.drawable.chkbox_chkd_dark);
		else bugReportCheckbox.setImageResource(R.drawable.chkbox_default_dark);

		ImageView compressDataCheckbox = (ImageView) mCompressDataButton.findViewById(R.id.img_checkbox_compress_data);
		if (mCompressData) compressDataCheckbox.setImageResource(R.drawable.chkbox_chkd_dark);
		else compressDataCheckbox.setImageResource(R.drawable.chkbox_default_dark);
	}
}
