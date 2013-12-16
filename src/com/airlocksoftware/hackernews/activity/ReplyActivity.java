package com.airlocksoftware.hackernews.activity;

import com.airlocksoftware.hackernews.utils.StringUtils;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Toast;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.hackernews.activity.LoginActivity.PostAction;
import com.airlocksoftware.hackernews.data.UserPrefs;
import com.airlocksoftware.hackernews.loader.ReplyLoader;
import com.airlocksoftware.hackernews.model.Comment;
import com.airlocksoftware.hackernews.model.Result;
import com.airlocksoftware.hackernews.model.Story;
import com.airlocksoftware.holo.actionbar.ActionBarButton;
import com.airlocksoftware.holo.type.FontEdit;
import com.airlocksoftware.holo.type.FontText;

/**
 * Activity for replying to Comments or Stories (i.e. top-level comments). Uses ReplyLoader to send the reply in the
 * background. If the user is not logged in, pass them to the LoginActivity.
 */
public class ReplyActivity extends BackActivity implements LoaderManager.LoaderCallbacks<Result> {

  // State
  private Story mStory;

  private Comment mComment;

  private String mReplyText;

  private boolean mReadyToSend = false;

  // Views
  FontText mParentUsername, mParentTitle, mParentComment, mReplyUsername;

  FontEdit mReplyComment;

  // Listeners
  private OnClickListener mSendListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      mReplyText = mReplyComment.getText()
              .toString();
      if (StringUtils.isNotBlank(mReplyText)) {
        mReadyToSend = true;
        getSupportLoaderManager().restartLoader(0, null, ReplyActivity.this);
      } else {
        Toast.makeText(getApplicationContext(), getString(R.string.reply_is_blank), Toast.LENGTH_LONG)
                .show();
      }
    }
  };

  private OnClickListener mCancelListener = new OnClickListener() {
    @Override
    public void onClick(View v) {
      Toast.makeText(ReplyActivity.this, "Reply canceled.", Toast.LENGTH_SHORT)
              .show();
      finish();
    }
  };

  // Constants
  public static final String COMMENT = ReplyActivity.class.getSimpleName() + ".comment";

  public static final String STORY = ReplyActivity.class.getSimpleName() + ".story";

  public static final String REPLY_TEXT = ReplyActivity.class.getSimpleName() + ".replyText";

  @Override
  public void onCreate(Bundle savedState) {
    super.onCreate(savedState);
    setContentView(R.layout.act_reply);
    findViews();
    retrieveBundles(savedState, getIntent().getExtras());
    retrieveUserPrefs();
    setupActionBar();
    bindViews();

    // startup loader (but it won't really be run)
    getSupportLoaderManager().initLoader(0, null, ReplyActivity.this);
  }

  private void bindViews() {
    if (mReplyText != null) {
      mReplyComment.setText(mReplyText);
    }
    mReplyUsername.setText(new UserPrefs(this).getUsername());

    if (mStory != null) {
      mParentTitle.setText(mStory.title);
      mParentUsername.setText(mStory.username);
      if (mStory.selfText != null) {
        mParentComment.setText(mStory.selfText);
      } else {
        mParentComment.setVisibility(View.GONE);
      }

    } else if (mComment != null) {
      mParentUsername.setText(mComment.username);
      mParentComment.setText(mComment.generateSpannedHtml());
      mParentTitle.setVisibility(View.GONE);

    } else {
      throw new RuntimeException("Neither a comment nor a story was passed to reply activity.");
    }
  }

  private void setupActionBar() {
    getActionBarView().getController()
            .setTitleText(getString(R.string.reply));
    ActionBarButton send = new ActionBarButton(this).icon(R.drawable.ic_action_send);
    ActionBarButton cancel = new ActionBarButton(this).icon(R.drawable.ic_action_cancel);
    send.text("Send")
            .setOnClickListener(mSendListener);
    cancel.text("Cancel")
            .setOnClickListener(mCancelListener);
    getActionBarView().getController()
            .addButton(send);
    getActionBarView().getController()
            .addButton(cancel);
  }

  /**
   * Check UserPrefs for logged in state. *
   */
  private void retrieveUserPrefs() {
    if (!new UserPrefs(this).isLoggedIn()) {
      // start LoginActivity with appropriate PostAction
      Intent intent = new Intent(this, LoginActivity.class);
      if (mComment != null) {
        intent.putExtra(LoginActivity.POST_COMMENT, mComment);
      } else if (mStory != null) {
        intent.putExtra(LoginActivity.POST_STORY, mStory);
      }
      intent.putExtra(LoginActivity.POST_ACTION, PostAction.REPLY);
      startActivity(intent);
    }
  }

  private void retrieveBundles(Bundle savedState, Bundle extras) {
    if (savedState != null) {
      mStory = (Story) savedState.getSerializable(STORY);
      mComment = (Comment) savedState.getSerializable(COMMENT);
      mReplyText = savedState.getString(REPLY_TEXT);
    }

    if (extras != null) {
      mStory = (Story) extras.getSerializable(STORY);
      mComment = (Comment) extras.getSerializable(COMMENT);
    }
  }

  private void findViews() {
    mParentUsername = (FontText) findViewById(R.id.txt_parent_username);
    mParentTitle = (FontText) findViewById(R.id.txt_parent_title);
    mParentComment = (FontText) findViewById(R.id.txt_parent_comment);
    mReplyUsername = (FontText) findViewById(R.id.txt_reply_username);
    mReplyComment = (FontEdit) findViewById(R.id.edit_reply_comment);
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    mReplyText = mReplyComment.getText()
            .toString();
    if (mStory != null) {
      outState.putSerializable(STORY, mStory);
    }
    if (mComment != null) {
      outState.putSerializable(COMMENT, mComment);
    }
    if (StringUtils.isNotBlank(mReplyText)) {
      outState.putString(REPLY_TEXT, mReplyText);
    }
    super.onSaveInstanceState(outState);
  }

  @Override
  public Loader<Result> onCreateLoader(int id, Bundle args) {
    // setup replyId
    long replyId = -1;
    if (mStory != null) {
      replyId = mStory.storyId;
    } else if (mComment != null) {
      replyId = mComment.commentId;
    }

    if (mReadyToSend) {
      return new ReplyLoader(this, replyId, mReplyText);
    } else {
      return new ReplyLoader(this); // no data to send reply with
    }
  }

  @Override
  public void onLoadFinished(Loader<Result> loader, Result result) {
    switch (result) {
      case FAILURE:
        Toast.makeText(this, getString(R.string.reply_no_connection), Toast.LENGTH_LONG)
                .show();
        break;
      case SUCCESS:
        Toast.makeText(this, getString(R.string.reply_sent), Toast.LENGTH_SHORT)
                .show();
        finish();
        break;
      default:
        // no data was passed to loader aka we weren't ready to send data (do nothing)
        break;
    }
  }

  @Override
  public void onLoaderReset(Loader<Result> loader) {
    // No implementation necessary
  }

  public static void startCommentReplyActivity(Context context, Comment comment) {
    Intent intent = new Intent(context, ReplyActivity.class);
    intent.putExtra(COMMENT, comment);
    context.startActivity(intent);
  }

  public static void startStoryReplyActivity(Context context, Story story) {
    Intent intent = new Intent(context, ReplyActivity.class);
    intent.putExtra(ReplyActivity.STORY, story);
    context.startActivity(intent);
  }

}
