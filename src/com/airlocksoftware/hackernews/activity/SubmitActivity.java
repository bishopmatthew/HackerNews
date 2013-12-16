package com.airlocksoftware.hackernews.activity;

import com.airlocksoftware.hackernews.utils.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.EditText;
import android.widget.Toast;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.activity.LoginActivity.PostAction;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.loader.SubmitLoader;
import com.airlocksoftware.hackernews.model.Page;
import com.airlocksoftware.hackernews.model.Result;
import com.airlocksoftware.holo.actionbar.ActionBarButton;
import com.airlocksoftware.holo.actionbar.ActionBarButton.Priority;
import com.airlocksoftware.holo.utils.ViewUtils;

/**
 * Activity for submitting stories to HN. It is exported in the manifest so that it can receive ACTION_SEND share
 * Intents. Submission is performed in background by SubmitLoader.
 */
public class SubmitActivity extends SlideoutMenuActivity implements LoaderManager.LoaderCallbacks<Result> {

  private boolean mFromShareIntent = false;

  private SendMode mSendMode = SendMode.EMPTY;

  private String mTitleText, mSelfText, mUrlText;

  private EditText mTitleEditText, mSelfEditText, mUrlEditText;

  private ActionBarButton mSendButton, mCancelButton;

  public enum SendMode {
    EMPTY, SELF_TEXT, URL;
  }

  public static final String TITLE_STRING = SearchActivity.class.getSimpleName() + ".title";

  public static final String SELF_STRING = SearchActivity.class.getSimpleName() + ".text";

  public static final String URL_STRING = SearchActivity.class.getSimpleName() + ".url";

  private OnClickListener mSendListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      retrieveInputFromEditTexts();

      // validate input
      boolean validTitle = StringUtils.isNotBlank(mTitleText) && mTitleText.length() < 80;
      boolean validSelfText = StringUtils.isNotBlank(mSelfText);
      boolean validUrl = validateURL(mUrlText);
      boolean oneOrTheOther = validSelfText != validUrl;

      if (!validTitle) {
        Toast.makeText(getApplicationContext(), getString(R.string.submitting_title_too_long), Toast.LENGTH_LONG)
                .show();
      } else if (!oneOrTheOther) {
        Toast.makeText(getApplicationContext(), getString(R.string.error_submitting), Toast.LENGTH_LONG)
                .show();
      } else {
        // input is valid, send via SubmitLoader
        mSendButton.setVisibility(View.GONE);
        mCancelButton.showProgress(true)
                .onClick(null);
        mSendMode = validSelfText ? SendMode.SELF_TEXT : SendMode.URL;
        getSupportLoaderManager().restartLoader(0, null, SubmitActivity.this);
        Toast.makeText(getApplicationContext(), getString(R.string.submitting), Toast.LENGTH_LONG)
                .show();
      }
    }
  };

  private OnClickListener mCancelListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      if (!mFromShareIntent) {
        startActivity(new Intent(SubmitActivity.this, MainActivity.class));
      }
      finish();
    }
  };

  private TextWatcher mEditWatcher = new TextWatcher() {
    public void afterTextChanged(Editable s) {
      refreshEditTextEnabledState();
    }

    public void beforeTextChanged(CharSequence s, int start, int count, int after) {
    }

    public void onTextChanged(CharSequence s, int start, int before, int count) {
    }
  };

  @Override
  public void onCreate(Bundle savedState) {
    super.onCreate(savedState);
    setContentView(R.layout.act_submit);
    ViewUtils.fixBackgroundRepeat(findViewById(R.id.root_scroll));
    retrieveBundles(savedState, getIntent().getExtras());
    retrieveShareIntent(getIntent(), getIntent().getAction(), getIntent().getType());

    UserPrefs userPrefs = new UserPrefs(this);
    if (!userPrefs.isLoggedIn()) {
      sendToLoginActivity();
    }

    super.setActiveMenuItem(R.id.submit_button);
    setupActionBar();
    findAndBindViews();
    refreshEditTextEnabledState();

    // startup loader (but it won't actually be run because we have nothing to submit yet)
    getSupportLoaderManager().initLoader(0, null, SubmitActivity.this);
  }

  private void findAndBindViews() {
    // find views
    mTitleEditText = (EditText) findViewById(R.id.edit_title);
    mSelfEditText = (EditText) findViewById(R.id.edit_selftext);
    mUrlEditText = (EditText) findViewById(R.id.edit_url);

    // set text
    if (StringUtils.isNotBlank(mTitleText)) {
      mTitleEditText.setText(mTitleText);
    }
    if (StringUtils.isNotBlank(mSelfText)) {
      mSelfEditText.setText(mSelfText);
    }
    if (StringUtils.isNotBlank(mUrlText)) {
      mUrlEditText.setText(mUrlText);
    }

    // attach text watchers
    mSelfEditText.addTextChangedListener(mEditWatcher);
    mUrlEditText.addTextChangedListener(mEditWatcher);
  }

  private void setupActionBar() {
    getActionBarView().getController()
            .setTitleText(getString(R.string.submit));

    mSendButton = new ActionBarButton(this);
    mSendButton.icon(R.drawable.ic_action_send)
            .priority(Priority.HIGH)
            .onClick(mSendListener)
            .text(getString(R.string.submit));
    getActionBarView().getController()
            .addButton(mSendButton);

    mCancelButton = new ActionBarButton(this);
    mCancelButton.icon(R.drawable.ic_action_cancel)
            .priority(Priority.HIGH)
            .onClick(mCancelListener)
            .text(getString(R.string.cancel));
    getActionBarView().getController()
            .addButton(mCancelButton);
  }

  private void sendToLoginActivity() {
    Intent loginIntent = new Intent(this, LoginActivity.class);
    loginIntent.putExtra(LoginActivity.POST_ACTION, PostAction.SUBMIT);
    if (StringUtils.isNotBlank(mSelfText)) {
      loginIntent.putExtra(LoginActivity.POST_SUB_TEXT, mSelfText);
    } else if (StringUtils.isNotBlank(mUrlText)) {
      loginIntent.putExtra(LoginActivity.POST_SUB_TEXT, mSelfText);
    }
    startActivity(loginIntent);
    finish();
  }

  private void retrieveShareIntent(Intent intent, String action, String type) {
    if (Intent.ACTION_SEND.equals(action) && type != null) {
      if ("text/plain".equals(type)) {
        mFromShareIntent = true;
        String title = intent.getStringExtra(android.content.Intent.EXTRA_SUBJECT);
        String text = intent.getStringExtra(android.content.Intent.EXTRA_TEXT);
        if (StringUtils.isNotBlank(title)) {
          mTitleText = title;
        }

        // check for url or text
        if (StringUtils.isNotBlank(text)) {
          if (validateURL(text)) {
            mUrlText = text;
          } else {
            mSelfText = text;
          }
        }
      }
    }
  }

  private void retrieveBundles(Bundle savedState, Bundle extras) {
    // restoring saved state
    if (savedState != null) {
      mTitleText = savedState.getString(TITLE_STRING);
      mSelfText = savedState.getString(SELF_STRING);
      mUrlText = savedState.getString(URL_STRING);
    }

    // getting intent extras - can overwrite savedState - is that correct?
    if (extras != null) {
      mTitleText = extras.getString(TITLE_STRING);
      mSelfText = extras.getString(SELF_STRING);
      mUrlText = extras.getString(URL_STRING);
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    outState.putString(TITLE_STRING, mTitleText);
    outState.putString(SELF_STRING, mSelfText);
    outState.putString(URL_STRING, mUrlText);
    super.onSaveInstanceState(outState);
  }

  protected boolean validateURL(String url) {
    return StringUtils.isNotBlank(url) && URLUtil.isValidUrl(url);
  }

  protected void refreshEditTextEnabledState() {
    if (mSelfEditText.getText()
            .length() > 0) {
      mSelfEditText.setEnabled(true);
      mUrlEditText.setEnabled(false);

    } else if (mUrlEditText.getText()
            .length() > 0) {
      mSelfEditText.setEnabled(false);
      mUrlEditText.setEnabled(true);

    } else {
      mSelfEditText.setEnabled(true);
      mUrlEditText.setEnabled(true);
    }
  }

  private void retrieveInputFromEditTexts() {
    mTitleText = mTitleEditText.getText()
            .toString();
    mSelfText = mSelfEditText.getText()
            .toString();
    mUrlText = mUrlEditText.getText()
            .toString();
  }

  // Loader callbacks
  @Override
  public Loader<Result> onCreateLoader(int id, Bundle args) {
    String content = null;
    if (mSendMode == SendMode.SELF_TEXT) {
      content = mSelfText;
    } else if (mSendMode == SendMode.URL) {
      content = mUrlText;
    }

    return new SubmitLoader(this, mSendMode, mTitleText, content);
  }

  @Override
  public void onLoadFinished(Loader<Result> loader, Result result) {

    if (result == Result.SUCCESS) {
      Toast.makeText(getApplicationContext(), getString(R.string.submitted), Toast.LENGTH_LONG)
              .show();

      if (!mFromShareIntent) {
        Intent intent = new Intent(SubmitActivity.this, MainActivity.class);
        intent.putExtra(MainActivity.PAGE, Page.NEW);
        startActivity(intent);
      }
      finish();

    } else if (result == Result.FAILURE) {
      Toast.makeText(getApplicationContext(), getString(R.string.error_loading), Toast.LENGTH_LONG)
              .show();

      mSendButton.setVisibility(View.VISIBLE);
      mCancelButton.showProgress(false)
              .onClick(mCancelListener);
    }

  }

  @Override
  public void onLoaderReset(Loader<Result> loader) {
    // no implementation necessary
  }

  // Static startup method
  public static void startSubmitActivity(Context context, String submissionTitle, String submissionText) {
    Intent intent = new Intent(context, SubmitActivity.class);
    intent.putExtra(android.content.Intent.EXTRA_SUBJECT, submissionTitle);
    intent.putExtra(android.content.Intent.EXTRA_TEXT, submissionText);
    context.startActivity(intent);
  }

}
