package com.airlocksoftware.v3.actionbar;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;

import com.airlocksoftware.hackernews.R;
import com.airlocksoftware.v3.dagger.HNApp;
import com.airlocksoftware.v3.otto.TabSelectedEvent;
import com.squareup.otto.Bus;

import javax.inject.Inject;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Displays and transforms the tabs in the ActionBar for Comments & Articles
 * Created by matthewbbishop on 12/19/13.
 */
public class ActionBarTabsView extends FrameLayout {

  private final float UNDERLINE_HEIGHT;

  private final OnTabClickListener mListener;

  @Inject Bus mBus;

  @InjectView(R.id.tab_comments) View mCommentsTab;
  @InjectView(R.id.tab_article) View mArticleTab;

  private Paint mUnderlinePaint;

  private float mPositionOffset;

  public ActionBarTabsView(Context context, OnTabClickListener listener) {
    super(context);

    mListener = listener;
    UNDERLINE_HEIGHT = getResources().getDimensionPixelSize(R.dimen.half);

    LayoutInflater.from(context).inflate(R.layout.vw_action_bar_tabs, this);
    ButterKnife.inject(this, this);
    HNApp.getInstance().getApplicationGraph().inject(this);

    setWillNotDraw(false);
    setupPaint();
  }

  private void setupPaint() {
    mUnderlinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    mUnderlinePaint.setColor(getResources().getColor(R.color.bright_accent));
  }

  @Override protected void onDraw(Canvas canvas) {
    /* draw the underline */
    /* get sizes of tabs */
    float leftWidth = mCommentsTab.getWidth();
    float rightWidth = mArticleTab.getWidth();
    float height = getHeight();

    /* Calculate edges of the underline based on mPositionOffset (from the ViewPager) */
    float left = leftWidth * mPositionOffset;
    float right = leftWidth + (rightWidth * mPositionOffset);
    float top = height - UNDERLINE_HEIGHT;
    
    /* Draw a rectangle at the calculated position */
    canvas.drawRect(left, top, right, height, mUnderlinePaint);

    /* Do regular drawing */
    super.onDraw(canvas);
  }

  @OnClick(R.id.tab_comments) void onCommentsClicked() { mBus.post(new TabSelectedEvent(MainFragmentTab.COMMENTS)); }
  @OnClick(R.id.tab_article) void onArticleClicked() { mBus.post(new TabSelectedEvent(MainFragmentTab.ARTICLE)); }

  /** Controls the offset of the underline (0 means under the left tab, 1 means under the right tab) **/
  public void transformUnderline(float positionOffset) {
    mPositionOffset = positionOffset;
    invalidate();
  }

  /** Called when one of the tabs is clicked (alternatively this should probably be an Otto event) **/
  public interface OnTabClickListener {
    public void onTabClick(MainFragmentTab page);
  }
}
