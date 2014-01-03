package com.airlocksoftware.hackernews.activity;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.LoaderManager.LoaderCallbacks;
import android.support.v4.content.Loader;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TextView.OnEditorActionListener;
import android.widget.Toast;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.loader.LoginLoader;
import com.airlocksoftware.hackernews.model.Comment;
import com.airlocksoftware.hackernews.model.Result;
import com.airlocksoftware.hackernews.model.Story;
import com.airlocksoftware.holo.utils.ViewUtils;

/**
 * Activity for logging in to news.ycombinator.com. The user will be forwarded to this activity if they try to perform a
 * operation that requires authentication while not logged in. The action the user was attempting to perform is passed
 * in via the extras bundle as a PostAction, which is then performed automatically once the user is authenticated.
 * 
 * Currently username & passwords are stored in SharedPreferences, which seems to be ok as per [1]. It doesn't make
 * much sense to encrypt the data when the encryption algorithm would be client side and the source would be available.
 * [1]: http://stackoverflow.com/questions/785973/what-is-the-most-appropriate-way-to-store-user-settings-in-android-
 * application
 * 
 * TODO Add a checkbox for "remember my password" and only save credentials if checked
 **/
public class LoginActivity extends SlideoutMenuActivity implements LoaderCallbacks<Result> {

	private String mUsername = null;
	private String mPassword = null;

	private PostAction mPostAction;

	// used for post-actions VOTE & REPLY
	private Story mStory;
	private Comment mComment;

	// use for post-actions SUBMIT
	private String mSubTitle;
	private String mSubText;

	private EditText mEditUsername;
	private EditText mEditPassword;
	private View mLoginButton;
	private View mLoginIndicator;

	private View.OnClickListener mSubmitBtnListener = new View.OnClickListener() {
		@Override
		public void onClick(View v) {
			performSubmit();
		}
	};

	private OnEditorActionListener mSubmitKeyboardListener = new OnEditorActionListener() {
		@Override
		public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
			if (actionId == EditorInfo.IME_ACTION_DONE) {

				performSubmit();

				// hide keyboard
				InputMethodManager inputManager = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
				if (getCurrentFocus() != null) {
					inputManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(),
														 InputMethodManager.HIDE_NOT_ALWAYS);
			    }

				return true;
			} else return false;
		}
	};

	public enum PostAction {
		UPVOTE, REPLY, SUBMIT;
	}
	
	public enum LoginResult {
		EMPTY, SUCCESS, FAILURE;
	}

	// CONSTANTS
	private static final String TAG = LoginActivity.class.getSimpleName();
	public static final String POST_ACTION = TAG + ".postAction";
	public static final String POST_STORY = TAG + ".postStory";
	public static final String POST_COMMENT = TAG + ".postComment";
	public static final String POST_SUB_TITLE = TAG + ".postSubTitle";
	public static final String POST_SUB_TEXT = TAG + ".postSubText";
	public static final String USERNAME = TAG + ".username";
	public static final String PASSWORD = TAG + ".password";

	// ACTIVITY LIFECYCLE
	@Override
	public void onCreate(Bundle savedState) {
		super.onCreate(savedState);

		setContentView(R.layout.act_login);
		ViewUtils.fixBackgroundRepeat(findViewById(R.id.root_act_login));

		findViews();
		retrieveBundles(savedState, getIntent().getExtras());

		// get settings from SharedPrefs
		UserPrefs userData = new UserPrefs(this);
		if (userData.isLoggedIn()) {
			Toast t = Toast.makeText(this, "Already logged in", Toast.LENGTH_SHORT);
			t.show();
			finish();
		}

		// setup UI
		getActionBarView().getController()
											.setTitleText(getString(R.string.login));
		setActiveMenuItem(-1); // clear check
		showContent();

		// start loader
		getSupportLoaderManager().initLoader(0, null, this);
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		if (mEditUsername.getText() != null) mUsername = mEditUsername.getText().toString();
		if (mEditPassword.getText() != null) mPassword = mEditPassword.getText().toString();
		if (mPostAction != null) outState.putSerializable(POST_ACTION, mPostAction);
		if (mStory != null) outState.putSerializable(POST_STORY, mStory);
		if (mComment != null) outState.putSerializable(POST_COMMENT, mComment);
		if (mSubTitle != null) outState.putSerializable(POST_SUB_TITLE, mSubTitle);
		if (mSubText != null) outState.putSerializable(POST_SUB_TEXT, mSubText);
		if (mUsername != null) outState.putString(USERNAME, mUsername);
		if (mPassword != null) outState.putString(PASSWORD, mPassword);
		super.onSaveInstanceState(outState);
	}

	@Override
	public void finish() {
		super.finish();
		// TODO need to restart other activities in back stack if we've logged in
	}

	// Loader callbacks
	@Override
	public Loader<Result> onCreateLoader(int id, Bundle args) {
		return new LoginLoader(this, mUsername, mPassword);
	}

	@Override
	public void onLoadFinished(Loader<Result> loader, Result result) {
		if (result == Result.EMPTY) {
			return; // this means the request was from initLoader()
		}

		if (result == Result.SUCCESS) {
			Toast t = Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT);
			t.show();
			doPostAction();
			finish();
		} else {
			showError();
			Toast t = Toast.makeText(this, getString(R.string.login_failure), Toast.LENGTH_LONG);
			t.show();
		}

	}

	private void doPostAction() {
		if (mPostAction == null) return;

		switch (mPostAction) {
		case UPVOTE:
			if (mComment != null) mStory.upvote(this);
			else if (mStory != null) mStory.upvote(this);
			break;
		case REPLY:
			if (mComment != null) ReplyActivity.startCommentReplyActivity(this, mComment);
			else if (mStory != null) ReplyActivity.startStoryReplyActivity(this, mStory);
			break;
		case SUBMIT:
			SubmitActivity.startSubmitActivity(this, mSubTitle, mSubText);
			break;
		}
	}

	@Override
	public void onLoaderReset(Loader<Result> loader) {
		// No implementation necessary
	}

	// private methods
	protected void performSubmit() {
		if (mEditUsername.getText() != null) mUsername = mEditUsername.getText().toString();
		if (mEditPassword.getText() != null) mPassword = mEditPassword.getText().toString();

		showLoading();

		getSupportLoaderManager().restartLoader(0, null, LoginActivity.this);
	}

	private void findViews() {
		mEditUsername = (EditText) findViewById(R.id.edit_username);
		mEditPassword = (EditText) findViewById(R.id.edit_password);
		mLoginButton = findViewById(R.id.btn_login);
		mLoginIndicator = findViewById(R.id.login_indicator);

		mLoginButton.setOnClickListener(mSubmitBtnListener);
		mEditPassword.setOnEditorActionListener(mSubmitKeyboardListener);
	}

	private void retrieveBundles(Bundle savedState, Bundle extras) {
		if (savedState != null) {
			mPostAction = (PostAction) savedState.getSerializable(POST_ACTION);
			mStory = (Story) savedState.getSerializable(POST_STORY);
			mComment = (Comment) savedState.getSerializable(POST_COMMENT);
			mSubTitle = savedState.getString(POST_SUB_TITLE);
			mSubText = savedState.getString(POST_SUB_TEXT);
			mUsername = savedState.getString(USERNAME);
			mPassword = savedState.getString(PASSWORD);
		}

		if (extras != null) {
			mPostAction = (PostAction) extras.getSerializable(POST_ACTION);
			mStory = (Story) extras.getSerializable(POST_STORY);
			mComment = (Comment) extras.getSerializable(POST_COMMENT);
			mSubTitle = extras.getString(POST_SUB_TITLE);
			mSubText = extras.getString(POST_SUB_TEXT);
		}
	}

	private void showLoading() {
		if (mEditUsername != null) mEditUsername.setEnabled(false);
		if (mEditPassword != null) mEditPassword.setEnabled(false);
		mLoginButton.setVisibility(View.GONE);
		mLoginIndicator.setVisibility(View.VISIBLE);
	}

	private void showContent() {
		if (mEditUsername != null) mEditUsername.setEnabled(true);
		if (mEditPassword != null) mEditPassword.setEnabled(true);
		mLoginButton.setVisibility(View.VISIBLE);
		mLoginIndicator.setVisibility(View.GONE);
	}

	private void showError() {
		if (mEditUsername != null) mEditUsername.setEnabled(true);
		if (mEditPassword != null) mEditPassword.setEnabled(true);
		mLoginButton.setVisibility(View.VISIBLE);
		mLoginIndicator.setVisibility(View.GONE);
	}

}
